package ru.otus.geolocation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.otus.geolocation.domain.ShopPoint;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopPointCoord {

    private ShopPoint shopPoint;

    private Coord coord;
}
