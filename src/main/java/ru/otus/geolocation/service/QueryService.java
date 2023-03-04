package ru.otus.geolocation.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.geolocation.domain.CityRule;
import ru.otus.geolocation.domain.ShopPoint;
import ru.otus.geolocation.dto.AnswerShop;
import ru.otus.geolocation.dto.Coord;
import ru.otus.geolocation.repository.ShopPointRepository;
import ru.otus.geolocation.service.util.CityRuleDataBase;
import ru.otus.geolocation.service.util.MapperShopPoint;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class QueryService {

    private final CityRuleService cityRuleService;

    private final ShopPointRepository shopPointRepository;

    private final MapperShopPoint mapper;

    private final YandexGeoAPIService yandexGeoAPIService;

    private final AtomicReference<CityRuleDataBase> cityRulesDb =
            new AtomicReference<>(new CityRuleDataBase(new ArrayList<>(), new HashMap<>()));

    @PostConstruct
    public void init() {
        updateCityRules();
    }

    public List<CityRule> updateCityRules() {
        List<CityRule> cityRuleList = cityRuleService.findAll().stream()
                .peek(cityRule -> cityRule.setPattern(Pattern.compile(cityRule.getGrregexp())))
                .sorted(Comparator.comparingInt(cityRule -> -cityRule.getCity().length()))
                .collect(Collectors.toList());
        Map<String, String> ruleCityMap = cityRuleList.stream().collect(Collectors.toMap(CityRule::getRule, CityRule::getCity));
        cityRulesDb.lazySet(new CityRuleDataBase(cityRuleList, ruleCityMap));
        return cityRulesDb.get().getCityRuleList();
    }

    @Transactional(readOnly = true)
    public AnswerShop getShopsByCityUtterance(String cityUtterance) {
        try {
            String cityUtteranceDecode = URLDecoder.decode(cityUtterance, "UTF-8");
            AtomicReference<AnswerShop> answerShop = new AtomicReference<>();
            getRule(cityUtteranceDecode).ifPresent(cityRule -> {
                List<ShopPoint> shopPoints = getShopPointsByCity(cityRule.getCity());
                if (shopPoints.size() == 0) {
                    log.error("В городе {} нет магазинов", cityRule.getCity());
                    answerShop.set(new AnswerShop(AnswerShop.AnswerStatus.ZERO, cityRule.getRule(), cityRule.getCity(), new ArrayList<>()));
                } else if (shopPoints.size() == 1) {
                    log.info("В городе {} найден единственный магазин: {}", cityRule.getCity(), shopPoints.get(0));
                    answerShop.set(new AnswerShop(AnswerShop.AnswerStatus.ONLY, cityRule.getRule(), cityRule.getCity(),
                            shopPoints.stream().map(mapper::toDto).collect(Collectors.toList())));
                } else {
                    log.info("В городе {} больше одного магазина", cityRule.getCity());
                    answerShop.set(new AnswerShop(AnswerShop.AnswerStatus.MANY, cityRule.getRule(), cityRule.getCity(), new ArrayList<>()));
                }
            });
            if (answerShop.get() == null) {
                log.error("Не найден город по utterance {}", cityUtteranceDecode);
                return new AnswerShop(AnswerShop.AnswerStatus.ZERO, "nomatch", "nomatch", new ArrayList<>());
            }
            return answerShop.get();
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            return new AnswerShop(AnswerShop.AnswerStatus.ZERO, e.getMessage(), "error", new ArrayList<>());
        }
    }

    @Transactional(readOnly = true)
    public AnswerShop getByRuleAndAddress(String rule, String addressUtterance) {
        String city = cityRulesDb.get().getRuleCityMap().get(rule);
        if (city == null) {
            log.error("Не найден город по rule {}", rule);
            return new AnswerShop(AnswerShop.AnswerStatus.ZERO, String.format("Not found city by rule %s", rule), null, new ArrayList<>());
        }
        try {
            String addressUtteranceDecode = URLDecoder.decode(addressUtterance, "UTF-8");
            YandexGeoAPIService.GeoPoints coords = yandexGeoAPIService.getLatLng(
                    String.format("%s, %s", city, addressUtteranceDecode));
            if (coords.getLat() < 0 || coords.getLng() < 0) {
                log.error("Не определены коордианты для: {}", String.format("%s, %s", city, addressUtteranceDecode));
                return new AnswerShop(AnswerShop.AnswerStatus.ZERO, String.format("Coordinates not defined for %s, %s",
                        city, addressUtteranceDecode), city, new ArrayList<>());
            }
            List<ShopPoint> shopPoints = nearestShops(coords.getLat(), coords.getLng(), getShopPointsByCity(city));
            if (!shopPoints.isEmpty()) {
                log.info("Для адреса: {} определены магазины: {}", String.format("%s, %s", city,
                        addressUtteranceDecode), shopPoints);
                return new AnswerShop(AnswerShop.AnswerStatus.MANY, rule, city,
                        shopPoints.stream().map(mapper::toDto).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new AnswerShop(AnswerShop.AnswerStatus.ZERO, e.getMessage(), city, new ArrayList<>());
        }
        log.error("Нет рядом магазинов с корректными координатами");
        return new AnswerShop(AnswerShop.AnswerStatus.ZERO, "The coordinates are determined, there are no shops nearby", city, new ArrayList<>());
    }

    private List<ShopPoint> getShopPointsByCity(String city) {
        List<ShopPoint> shopPoints = shopPointRepository.findAllByCityAndShopAddressLatitudeGreaterThanAndShopAddressLongitudeGreaterThan(city, 0.0D, 0.0D);
        return removeDuplicatesByCoordsAndByEmptyAddressForSystem(shopPoints);
    }

    private List<ShopPoint> removeDuplicatesByCoordsAndByEmptyAddressForSystem(List<ShopPoint> shopPoints) {
        Map<Coord, ShopPoint> shopPointMap = new HashMap<>(shopPoints.size());
        shopPoints.forEach(shopPoint -> {
            String addressForSystem = shopPoint.getAddressForSystem();
            if (addressForSystem != null && !addressForSystem.isEmpty()) {
                shopPointMap.put(mapper.toCoord(shopPoint), shopPoint);
            }
        });
        return new ArrayList<>(shopPointMap.values());
    }

    public List<ShopPoint> nearestShops(double latitude, double longitude, List<ShopPoint> shopPoints) {
        return yandexGeoAPIService.getOrderedShopPoint(latitude, longitude, shopPoints).stream()
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * Расстояние между точкой, которую назвал абонент, и магазином по формуле гаверсинусов.
     */
    private double distance(double latitude, double longitude, ShopPoint shopPoint) {
        double latShop = shopPoint.getShopAddress().getLatitude();
        double lonShop = shopPoint.getShopAddress().getLongitude();
        double radLat = Math.toRadians(latitude);
        double radLatShop = Math.toRadians(latShop);
        double a = radLat - radLatShop;
        double b = Math.toRadians(longitude) - Math.toRadians(lonShop);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat) * Math.cos(radLatShop) * Math.pow(Math.sin(b / 2), 2)));
        s = s * 6378137;
        return s;
//        return 6378137 * Math.acos(Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//                * Math.cos(Math.toRadians(lon2) - Math.toRadians(lon1))
//                + Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)));
    }

    private Optional<CityRule> getRule(String utterance) {
        for (CityRule cityRule : cityRulesDb.get().getCityRuleList()) {
            if (cityRule.getPattern().matcher(utterance).matches()) {
                log.info(String.format("QueryService.getRule(\"%s\"). Определён rule: %s", utterance, cityRule.getRule()));
                return Optional.of(cityRule);
            }
        }
        return Optional.empty();
    }
}
