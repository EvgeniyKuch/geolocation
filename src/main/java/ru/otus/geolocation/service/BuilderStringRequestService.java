package ru.otus.geolocation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.otus.geolocation.domain.ShopPoint;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class BuilderStringRequestService {

    /**
     * Ключ для обращения к API Яндекса.
     */
    private final String apikey;

    /**
     * Максимальный размер матрицы
     */
    private final Integer batchSize;

    public BuilderStringRequestService(@Value(value = "${yandex.geo-apikey}") String apikey,
                                       @Value(value = "${yandex.max-batch-size}") Integer batchSize) {
        this.apikey = apikey;
        this.batchSize = batchSize;
    }

    /**
     * Разбивает матрицу на блоки по batchSize, т.к. у Яндекс api ограничение на размер матрицы.
     * origins остаётся 1, разбиваются destination
     */
    public List<String> getRequestsDistanceMatrix(double latitude, double longitude, List<ShopPoint> shopPoints) {
        String origin = getCoords(latitude, longitude);
        return getDestinations(shopPoints).stream().map(destination ->
                "https://api.routing.yandex.net/v2/distancematrix?apikey="
                        .concat(apikey).concat("&origins=").concat(origin)
                        .concat("&destinations=").concat(destination)).collect(Collectors.toList());
    }

    public String getRequestGeoLocation(String address) throws UnsupportedEncodingException {
        return "https://geocode-maps.yandex.ru/1.x/?format=json&results=10&lang=ru_RU&geocode="
                .concat(URLEncoder.encode(address, "UTF-8"))
                .concat("&apikey=").concat(apikey);
    }

    /**
     * Разбивает destinations на порции по batchSize в каждой
     * @param shopPoints целевые точки
     * @return массив строк из координат <lat1,lon1|lat2,lon2|...> размером <= batchSize в каждой строке
     */
    private List<String> getDestinations(List<ShopPoint> shopPoints) {
        List<String> destinations = new ArrayList<>();
        List<String> coords = shopPoints.stream().map(this::getCoords).collect(Collectors.toList());
        IntStream.rangeClosed(0, coords.size() / batchSize)
                .filter(i -> batchSize * i != coords.size()).forEachOrdered(i -> destinations.add(
                coords.stream().skip(batchSize * i).limit(batchSize).collect(Collectors.joining("|"))));
        return destinations;
    }

    private String getCoords(ShopPoint shopPoint) {
        return getCoords(shopPoint.getShopAddress().getLatitude(), shopPoint.getShopAddress().getLongitude());
    }

    private String getCoords(double latitude, double longitude) {
        return String.join(",", String.valueOf(latitude), String.valueOf(longitude));
    }
}
