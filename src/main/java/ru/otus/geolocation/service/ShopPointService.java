package ru.otus.geolocation.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.geolocation.domain.ShopAddress;
import ru.otus.geolocation.domain.ShopPoint;
import ru.otus.geolocation.repository.ShopPointRepository;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class ShopPointService {

    private final ShopAddressService shopAddressService;

    private final ShopPointRepository shopPointRepository;

    /**
     * Сохраняет новые ShopAddress, подставляет их в shopPoints, удаляет все старые ShopPoint и сохраняет новые
     *
     * @param shopAddressesForSave ShopAddress для сохранения
     * @param shopPoints           shopPoints для сохранения
     */
    @Transactional
    public void save(Set<ShopAddress> shopAddressesForSave, List<ShopPoint> shopPoints) {
        shopAddressService.saveShopAddresses(shopAddressesForSave, shopPoints);
        shopPointRepository.deleteAll();
        shopPointRepository.saveAll(shopPoints);
        log.info("Сохранили {} уникальных записей", shopPoints.size());
    }
}
