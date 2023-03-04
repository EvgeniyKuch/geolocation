package ru.otus.geolocation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.geolocation.domain.IncomingMailSetting;

public interface IncomingMailSettingRepository extends JpaRepository<IncomingMailSetting, Long> {
}
