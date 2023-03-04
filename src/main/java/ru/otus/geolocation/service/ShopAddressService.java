package ru.otus.geolocation.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.geolocation.domain.ShopAddress;
import ru.otus.geolocation.domain.ShopPoint;
import ru.otus.geolocation.repository.ShopAddressRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ShopAddressService {

    private final ShopAddressRepository shopAddressRepository;

    /**
     * Ищет существующие в БД ShopAddress, подставляет их в shopPoints, возвращает несуществующие
     *
     * @param addressForSearchExisted адреса для поиска уже существующих в БД ShopAddress c координатами
     * @param shopPoints              точки продаж от заказчика
     * @return множество несуществующих в БД ShopAddress'ов
     */
    @Transactional(readOnly = true)
    public Set<ShopAddress> getShopAddressesForSave(List<String> addressForSearchExisted, List<ShopPoint> shopPoints) {
        Map<String, ShopAddress> shopAddressesExisted =
                shopAddressRepository.findAllByAddressInAndLatitudeGreaterThanAndLongitudeGreaterThan(addressForSearchExisted, 0D, 0D)
                        .stream().collect(Collectors.toMap(ShopAddress::getAddress, Function.identity()));
        Set<ShopAddress> shopAddressesForSave = new HashSet<>(addressForSearchExisted.size());
        shopPoints.forEach(shopPoint -> {
            ShopAddress shopAddressExisted = shopAddressesExisted.get(shopPoint.getShopAddress().getAddress());
            if (shopAddressExisted != null) {
                shopPoint.setShopAddress(shopAddressExisted);
            } else {
                shopAddressesForSave.add(shopPoint.getShopAddress());
            }
        });
        return shopAddressesForSave;
    }

    /**
     * Сохраняет несуществущие address, подставляет их в shopPoints
     *
     * @param shopAddressesForSave множество несуществующих адресов с координатами
     * @param shopPoints           точки продаж от заказчика
     */
    public void saveShopAddresses(Set<ShopAddress> shopAddressesForSave, List<ShopPoint> shopPoints) {
        Map<String, ShopAddress> shopAddressesNew = shopAddressRepository.saveAll(shopAddressesForSave).stream()
                .collect(Collectors.toMap(ShopAddress::getAddress, Function.identity()));
        shopPoints.forEach(shopPoint -> {
            if (shopPoint.getShopAddress().getId() == null) {
                shopPoint.setShopAddress(shopAddressesNew.get(shopPoint.getShopAddress().getAddress()));
            }
        });
    }
}
