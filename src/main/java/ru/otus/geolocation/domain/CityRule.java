package ru.otus.geolocation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.regex.Pattern;

@Entity
@Table(name = "`city_rule`")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city")
    private String city;

    @Column(name = "rule")
    private String rule;

    @Column(name= "grregexp")
    private String grregexp;

    @Transient
    private Pattern pattern;
}
