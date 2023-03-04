package ru.otus.geolocation.dto.geo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderShop {

    private Integer order;

    private Double distance;
}
