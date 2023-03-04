package ru.otus.geolocation.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.geolocation.domain.ShopAddress;
import ru.otus.geolocation.domain.ShopPoint;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {BuilderStringRequestService.class})
class BuilderStringRequestServiceTest {

    @Autowired
    BuilderStringRequestService urlService;

    @Test
    void shouldReturnRequestsWithCorrectDestinationSize() {
        assertThat(urlService.getRequestsDistanceMatrix(55.755481, 37.638027, getTestShopPoints()))
                .isEqualTo(expectedStringRequest());
    }

    @Test
    void getRequestGeoLocation() throws UnsupportedEncodingException {
        assertThat(urlService.getRequestGeoLocation("москва, театральная площадь 1"))
                .isEqualTo("https://geocode-maps.yandex.ru/1.x/?format=json&results=10&lang=ru_RU" +
                        "&geocode=%D0%BC%D0%BE%D1%81%D0%BA%D0%B2%D0%B0%2C+%D1%82%D0%B5%D0%B0%D1%82%D1%80" +
                        "%D0%B0%D0%BB%D1%8C%D0%BD%D0%B0%D1%8F+%D0%BF%D0%BB%D0%BE%D1%89%D0%B0%D0%B4%D1%8C" +
                        "+1&apikey=1234");
    }

    private List<ShopPoint> getTestShopPoints() {
        return Arrays.asList(
                mockShop(new ShopAddress(null, "address", 55.755981, 37.635027)),
                mockShop(new ShopAddress(null, "address", 55.755773, 37.627175)),
                mockShop(new ShopAddress(null, "address", 55.755864, 37.621255)));
    }

    private ShopPoint mockShop(ShopAddress shopAddress) {
        return new ShopPoint(null, "city", "addressForSystem", "workTimeRaw", "workTime", shopAddress);
    }

    private List<String> expectedStringRequest() {
        return Arrays.asList(
                "https://api.routing.yandex.net/v2/distancematrix?apikey=1234&origins=55.755481,37.638027&destinations=55.755981,37.635027|55.755773,37.627175",
                "https://api.routing.yandex.net/v2/distancematrix?apikey=1234&origins=55.755481,37.638027&destinations=55.755864,37.621255"
        );
    }
}
