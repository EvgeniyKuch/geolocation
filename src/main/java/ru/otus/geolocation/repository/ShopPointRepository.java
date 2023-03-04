package ru.otus.geolocation.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.geolocation.domain.ShopPoint;

import java.util.List;

public interface ShopPointRepository extends JpaRepository<ShopPoint, Long> {

    @EntityGraph(attributePaths = "shopAddress")
    List<ShopPoint> findAllByCityAndShopAddressLatitudeGreaterThanAndShopAddressLongitudeGreaterThan(String city, Double latitude, Double longitude);

    @EntityGraph(attributePaths = "shopAddress")
    List<ShopPoint> findAllByShopAddressLatitudeLessThanOrShopAddressLongitudeLessThan(Double latitude, Double longitude);

    @EntityGraph(attributePaths = "shopAddress")
    List<ShopPoint> findAllByShopAddressLatitudeGreaterThanAndShopAddressLongitudeGreaterThan(Double latitude, Double longitude);

    @EntityGraph(attributePaths = "shopAddress")
    List<ShopPoint> findAll();
}
