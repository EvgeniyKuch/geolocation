package ru.otus.geolocation.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.otus.geolocation.domain.ShopAddress;
import ru.otus.geolocation.domain.ShopPoint;
import ru.otus.geolocation.repository.ShopPointRepository;
import ru.otus.geolocation.service.util.MapperShopPoint;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {QueryService.class})
class QueryServiceTest {
    @MockBean
    private CityRuleService cityRuleService;

    @MockBean
    private ShopPointRepository shopPointRepository;

    @MockBean
    private MapperShopPoint mapper;

    @MockBean
    private YandexGeoAPIService yandexGeoAPIService;

    @Autowired
    private QueryService queryService;

    @Test
    void shouldSortShopPointByDistanceAsc() {
        assertThat(queryService.nearestShops(55.755246, 37.617779, getTestShopPoints())).isEqualTo(getExpectedShopPoints());
    }

    private List<ShopPoint> getTestShopPoints() {
        return Arrays.asList(
                mockShop(new ShopAddress(null, "addressMostRemote", 55.755981, 37.635027)),
                mockShop(new ShopAddress(null, "addressAverage", 55.755773, 37.627175)),
                mockShop(new ShopAddress(null, "addressNearest", 55.755864, 37.621255)));
    }

    private List<ShopPoint> getExpectedShopPoints() {
        return Arrays.asList(
                mockShop(new ShopAddress(null, "addressNearest", 55.755864, 37.621255)),
                mockShop(new ShopAddress(null, "addressAverage", 55.755773, 37.627175)),
                mockShop(new ShopAddress(null, "addressMostRemote", 55.755981, 37.635027)));
    }

    private ShopPoint mockShop(ShopAddress shopAddress) {
        return new ShopPoint(null, "city", "addressForSystem", "workTimeRaw", "workTime", shopAddress);
    }
}
