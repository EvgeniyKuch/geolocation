package ru.otus.geolocation.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "`geo_request`")
@Data
@NoArgsConstructor
public class GeoRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "`api_key`")
    private String apiKey;

    @Column(name = "`address`")
    private String address;

    @Column(name = "`date_time`")
    private Instant dateTime;

    @Column(name = "`latitude`")
    private Double latitude;

    @Column(name = "`longitude`")
    private Double longitude;

    @Column(name = "`answer`")
    private String answer;

    public GeoRequest(String apiKey, String address, Instant dateTime, Double latitude, Double longitude) {
        this.apiKey = apiKey;
        this.address = address;
        this.dateTime = dateTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
