package ru.otus.geolocation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopPointCorrectDTO {

    private String city;

    private String address;

    private String addressForSystem;

}
