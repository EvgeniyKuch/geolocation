package ru.otus.geolocation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Coord {

    private Double latitude;

    private Double longitude;

}
