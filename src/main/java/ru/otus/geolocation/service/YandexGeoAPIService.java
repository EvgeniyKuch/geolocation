package ru.otus.geolocation.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestOperations;
import ru.otus.geolocation.domain.GeoRequest;
import ru.otus.geolocation.domain.ShopAddress;
import ru.otus.geolocation.domain.ShopPoint;
import ru.otus.geolocation.dto.geo.AnswerGeoMatrix;
import ru.otus.geolocation.dto.geo.OrderShop;
import ru.otus.geolocation.repository.GeoRequestRepository;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Поиск координат по строке адреса через сервис Яндекса.
 *
 * @author Evgeniy Kuchumov
 * @version 0.1
 * @since 2021-11-08
 */
@Service
@Slf4j
public class YandexGeoAPIService {

    /**
     * Ключ для обращения к API Яндекса.
     */
    private final String apikey;

    private final String apiKeyMasked;

    private final GeoRequestRepository geoRepository;

    private final BuilderStringRequestService urlService;

    private final RestOperations restOperations;

    /**
     * Конструктор с заданными параметрами.
     *
     * @param apikey ключ, для обращения к API Яндекса.
     */
    public YandexGeoAPIService(@Value(value = "${yandex.geo-apikey}") String apikey,
                               GeoRequestRepository geoRepository,
                               BuilderStringRequestService builderStringRequestService,
                               RestOperations restOperations) {
        this.apikey = apikey;
        this.apiKeyMasked = apikey.replaceAll("(?<=^.{5}).*(?=.{5}$)", new String(new char[apikey.length() - 10]).replace("\0", "*"));
        this.geoRepository = geoRepository;
        this.urlService = builderStringRequestService;
        this.restOperations = restOperations;
    }

    /**
     * Поиск координат в сервисе Яндекса, добавление координат в структуру ShopAddress для дальнейшего сохранения в БД.
     * Может длиться долго, поэтому исключен из транзакции.
     *
     * @param addresses Множество ShopAddress, полученное из файла и не существовавшее до этого в БД.
     */
    public void findCoordinates(Set<ShopAddress> addresses) {
        for (ShopAddress address : addresses) {
            log.trace("Данные адреса до обновления координат: {}", address);
            YandexGeoAPIService.GeoPoints geoPoints = getLatLng(address.getAddress());
            address.setLatitude(geoPoints.lat);
            address.setLongitude(geoPoints.lng);
            log.trace("Определены координаты: широта {} - долгота {}", geoPoints.lat, geoPoints.lng);
            log.trace("Обновлены данные адреса: {}", address);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GeoPoints getLatLng(String address) {
        GeoPoints result = new GeoPoints(-1, -1);
        GeoRequest geoRequest = new GeoRequest(apiKeyMasked, address, Instant.now(), -1D, -1D);
        try {
            // Яндекс сначала возвращает долготу, а потом широту
            HttpGet httpget = new HttpGet(urlService.getRequestGeoLocation(address));
            HttpClient httpClient = getHttpClient();
            HttpResponse response = null;
            response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
                StringBuilder buf = new StringBuilder();
                String str;
                while ((str = in.readLine()) != null) {
                    buf.append(str);
                }
                String answer = buf.toString();
                geoRequest.setAnswer(answer);
                JsonObject yandexAnswer = new JsonParser().parse(answer).getAsJsonObject();
                JsonArray results = getJsonObjectByPath(yandexAnswer, "response", "GeoObjectCollection").get("featureMember").getAsJsonArray();
                if (results.size() > 0) {
                    log.trace("Результат запроса координат: {}", results);
                    JsonObject geoObject = results.get(0).getAsJsonObject().get("GeoObject").getAsJsonObject();
                    String[] cords = getJsonStringByPath(geoObject, "Point", "pos").split(" ");
                    GeoPoints pp = new GeoPoints(Double.parseDouble(cords[1]), Double.parseDouble(cords[0]));
                    result = pp;
                    geoRequest.setLatitude(pp.getLat());
                    geoRequest.setLongitude(pp.getLng());
                }
            }
        } catch (Exception e) {
            log.error("getLatLng: ", e);
        } finally {
            geoRepository.save(geoRequest);
        }
        return result;
    }

    /**
     * Запрашивает у Яндекса расстояния от точки, которую назвал абонент, до магазинов в этом городе.
     * Яндекс возвращает матрицу расстояний в том же порядке, в каком перечислены магазины.
     * Метод сортирует по увеличению расстояния.
     * @param latitude широта абонент
     * @param longitude долгота абонента
     * @param shopPoints магазины в городе абонента
     * @return отсортированные по возрастанию расстояния (по дорогам) от абонента магазины
     */
    public List<ShopPoint> getOrderedShopPoint(double latitude, double longitude, List<ShopPoint> shopPoints) {
        List<ShopPoint> result = new ArrayList<>();
        AtomicInteger order = new AtomicInteger(0);
        urlService.getRequestsDistanceMatrix(latitude, longitude, shopPoints)
                .stream().map(url -> restOperations.getForObject(url, AnswerGeoMatrix.class))
                .filter(Objects::nonNull)
                .flatMap(answer -> Stream.of(answer.getRows()))
                .flatMap(row -> Stream.of(row.getElements()))
                .filter(row -> "OK".equals(row.getStatus()))
                .map(element -> new OrderShop(order.getAndIncrement(), element.getDistance().getValue()))
                .sorted(Comparator.comparingDouble(OrderShop::getDistance))
                .forEach(orderShop -> result.add(shopPoints.get(orderShop.getOrder())));
        return result;
    }

    private JsonObject getJsonObjectByPath(JsonObject object, String... path) {
        try {
            for (int i = 0; i < path.length - 1; i++) {
                object = object.get(path[i]).getAsJsonObject();
            }
            return object.get(path[path.length - 1]).getAsJsonObject();
        } catch (Exception e) {
            log.error("getJsonObjectByPath: ", e);
        }
        return new JsonObject();
    }

    /**
     * Получение значения поля вложенного json объекта
     *
     * @param object
     * @param path
     * @return
     */
    private String getJsonStringByPath(JsonObject object, String... path) {
        try {
            for (int i = 0; i < path.length - 1; i++) {
                object = object.get(path[i]).getAsJsonObject();
            }
            return object.get(path[path.length - 1]).getAsString();
        } catch (Exception e) {
            log.error("getJsonStringByPath: ", e);
        }
        return "";
    }

    /**
     * Описание географической координаты: широта / долгота.
     */
    @Setter
    @Getter
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class GeoPoints {
        /**
         * Широта (latitude).
         */
        double lat;

        /**
         * Долгота (longitude).
         */
        double lng;
    }

    private static HttpClient getHttpClient() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null,
                    new TrustManager[]{new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(
                                X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                X509Certificate[] certs, String authType) {
                        }
                    }}, new SecureRandom());
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            return HttpClientBuilder.create().setSSLSocketFactory(socketFactory).build();
        } catch (Exception e) {
            log.error("getHttpClient: ", e);
            return HttpClientBuilder.create().build();
        }
    }
}
