package ru.otus.geolocation.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.geolocation.domain.CityRule;
import ru.otus.geolocation.domain.ShopPoint;
import ru.otus.geolocation.dto.ShopPointCoord;
import ru.otus.geolocation.dto.ShopPointCorrectDTO;
import ru.otus.geolocation.repository.CityRuleRepository;
import ru.otus.geolocation.repository.ShopPointRepository;
import ru.otus.geolocation.service.util.MapperShopPoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CorrectService {

    private final ShopPointRepository shopPointRepository;

    private final CityRuleRepository cityRuleRepository;

    private final MapperShopPoint mapperShopPoint;

    @Transactional(readOnly = true)
    public List<List<ShopPointCorrectDTO>> findDuplicates() {
        return findDuplicate(shopPointRepository.findAllByShopAddressLatitudeGreaterThanAndShopAddressLongitudeGreaterThan(0.0, 0.0));
    }

    @Transactional(readOnly = true)
    public List<ShopPointCorrectDTO> findIncorrect() {
        return shopPointRepository.findAllByShopAddressLatitudeLessThanOrShopAddressLongitudeLessThan(0.0, 0.0).stream()
                .map(mapperShopPoint::toCorrectDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShopPointCorrectDTO> findAll() {
        return shopPointRepository.findAll().stream().map(mapperShopPoint::toCorrectDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getNewCitiesForRegexp() {
        Set<String> citiesFromShopPoints = shopPointRepository.findAll().stream().map(ShopPoint::getCity).collect(Collectors.toSet());
        Set<String> citiesFromCityRules = cityRuleRepository.findAll().stream().map(CityRule::getCity).collect(Collectors.toSet());
        List<String> result = new ArrayList<>();
        citiesFromShopPoints.forEach(cityFromShopPoint -> {
            if (!citiesFromCityRules.contains(cityFromShopPoint)) {
                result.add(cityFromShopPoint);
            }
        });
        result.sort(String::compareTo);
        return result;
    }

    private List<List<ShopPointCorrectDTO>> findDuplicate(List<ShopPoint> shopPoints) {
        return shopPoints.stream()
                .map(mapperShopPoint::toShopPointCoord)
                .collect(Collectors.groupingBy(ShopPointCoord::getCoord))
                .values().stream()
                .filter(shopPointCoords -> shopPointCoords.size() > 1)
                .sorted(Comparator.comparingInt(shopPointCoords -> - shopPointCoords.size()))
                .map(mapperShopPoint::toShopPointCorrectDTOList)
                .collect(Collectors.toList())
                ;
    }
}
