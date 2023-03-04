package ru.otus.geolocation.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.geolocation.domain.CityRule;
import ru.otus.geolocation.repository.CityRuleRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CityRuleService {

    private final CityRuleRepository cityRuleRepository;

    @Transactional(readOnly = true)
    public List<CityRule> findAll() {
        return cityRuleRepository.findAll();
    }
}
