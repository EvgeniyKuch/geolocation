package ru.otus.geolocation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopPointDTO {

    private String addressForSystem;

    private boolean haveCorrectWorkTime;

    private boolean holiday;

    private Integer hourStart;

    private Integer minuteStart;

    private Integer hourEnd;

    private Integer minuteEnd;

}
