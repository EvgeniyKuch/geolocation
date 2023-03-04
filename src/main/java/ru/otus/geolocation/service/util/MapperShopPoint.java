package ru.otus.geolocation.service.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.geolocation.domain.ShopPoint;
import ru.otus.geolocation.dto.Coord;
import ru.otus.geolocation.dto.ShopPointCoord;
import ru.otus.geolocation.dto.ShopPointDTO;
import ru.otus.geolocation.dto.ShopPointCorrectDTO;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MapperShopPoint {

    private final WorktimeParser worktimeParser;

    public ShopPointDTO toDto(ShopPoint shopPoint) {
        ShopPointDTO result = new ShopPointDTO();
        result.setAddressForSystem(shopPoint.getAddressForSystem());
        worktimeParser.parseWorkTime(result, shopPoint.getWorkTimeRaw());
        return result;
    }

    public List<ShopPointCorrectDTO> toShopPointCorrectDTOList(List<ShopPointCoord> shopPointCoords) {
        return shopPointCoords.stream().map(this::toCorrectDTO).collect(Collectors.toList());
    }

    public ShopPointCorrectDTO toCorrectDTO(ShopPoint shopPoint) {
        return new ShopPointCorrectDTO(shopPoint.getCity(), shopPoint.getShopAddress().getAddress(),
                shopPoint.getAddressForSystem());
    }

    public ShopPointCoord toShopPointCoord(ShopPoint shopPoint) {
        return new ShopPointCoord(shopPoint, toCoord(shopPoint));
    }

    public Coord toCoord(ShopPoint shopPoint) {
        return new Coord(shopPoint.getShopAddress().getLatitude(), shopPoint.getShopAddress().getLongitude());
    }

    private ShopPointCorrectDTO toCorrectDTO(ShopPointCoord shopPointCoord) {
        return new ShopPointCorrectDTO(shopPointCoord.getShopPoint().getCity(),
                shopPointCoord.getShopPoint().getShopAddress().getAddress(),
                shopPointCoord.getShopPoint().getAddressForSystem());
    }

}
