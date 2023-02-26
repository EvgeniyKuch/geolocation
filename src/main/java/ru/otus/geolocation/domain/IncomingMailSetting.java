package ru.otus.geolocation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "`incoming_mail_setting`")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomingMailSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "`user`")
    String user;

    @Column(name = "`password`")
    String password;

    @Column(name = "`host`")
    String host;

    @Column(name = "`port`")
    String port;
}
