package ru.otus.geolocation.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.geolocation.domain.IncomingMailSetting;
import ru.otus.geolocation.repository.IncomingMailSettingRepository;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class IncomingMailSettingService {

    private final IncomingMailSettingRepository incomingMailSettingRepository;

    @Transactional(readOnly = true)
    public Optional<IncomingMailSetting> getSet() {
        return incomingMailSettingRepository.findById(1L);
    }
}
