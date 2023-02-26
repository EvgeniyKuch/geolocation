package ru.otus.geolocation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerShop {

    public enum AnswerStatus {
        ZERO, // нет точек продаж в насел. пункте
        ONLY, // только одна точка продаж
        MANY // больше 1
    }

    private AnswerShop.AnswerStatus status;
    
    private String rule;

    private String city;

    private List<ShopPointDTO> shopPoints;

}
