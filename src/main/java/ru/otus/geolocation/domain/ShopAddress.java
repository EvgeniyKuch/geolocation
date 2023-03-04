package ru.otus.geolocation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "`shop_address`")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "`address`")
    private String address;

    @Column(name = "`latitude`")
    private Double latitude;

    @Column(name = "`longitude`")
    private Double longitude;
}
