package ru.otus.geolocation.service;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.otus.geolocation.domain.ShopAddress;
import ru.otus.geolocation.domain.ShopPoint;
import ru.otus.geolocation.dto.ShopPointCorrectDTO;
import ru.otus.geolocation.service.util.MapperShopPoint;

import java.util.*;
import java.util.stream.Collectors;

@Profile("prod")
@Service
@AllArgsConstructor
public class ProcessService {

    private final MailService mailService;

    private final ExcelService excelService;

    private final ShopPointService shopPointService;

    private final ShopAddressService shopAddressService;

    private final YandexGeoAPIService yandexGeoAPIService;

    @Scheduled(fixedRate = 300_000)
    public void process() {
        saveShopPoints(excelService.readFile(mailService.downloadEmailAttachment()));
    }

    /**
     * Проставляет в ShopPoint уже существующие в БД ShopAddress, находит координаты
     * новых и тоже проставляет их в ShopPoint.
     * Удаляет ShopPoint и сохраняет новые.
     *
     * @param shopPoints из файла для сохранения
     */
    private void saveShopPoints(List<ShopPoint> shopPoints) {
        if (shopPoints.isEmpty()) {
            return;
        }
        List<String> addressForSearchExisted = shopPoints.stream()
                .map(ShopPoint::getShopAddress)
                .map(ShopAddress::getAddress)
                .collect(Collectors.toList());
        Set<ShopAddress> shopAddressesForSave = shopAddressService.getShopAddressesForSave(addressForSearchExisted, shopPoints);
        yandexGeoAPIService.findCoordinates(shopAddressesForSave);
        shopPointService.save(shopAddressesForSave, shopPoints);
    }
}
