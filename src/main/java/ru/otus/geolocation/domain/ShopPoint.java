package ru.otus.geolocation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "`shop_point`")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "`city`")
    private String city;

    @Column(name = "`address_for_system`")
    private String addressForSystem;

    @Column(name = "`work_time_raw`")
    private String workTimeRaw;

    @Column(name = "`work_time`", length = 1023)
    private String workTime;

    @ManyToOne
    @JoinColumn(name = "shop_address_id")
    private ShopAddress shopAddress;
}
