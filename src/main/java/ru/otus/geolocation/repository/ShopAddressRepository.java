package ru.otus.geolocation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.geolocation.domain.ShopAddress;

import java.util.List;

public interface ShopAddressRepository extends JpaRepository<ShopAddress, Long> {

    List<ShopAddress> findAllByAddressInAndLatitudeGreaterThanAndLongitudeGreaterThan(List<String> address, Double latitude, Double longitude);
}
