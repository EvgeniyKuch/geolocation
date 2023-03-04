package ru.otus.geolocation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.geolocation.domain.CityRule;

public interface CityRuleRepository extends JpaRepository<CityRule, Long> {

}
