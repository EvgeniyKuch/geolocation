package ru.otus.geolocation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.geolocation.domain.GeoRequest;

public interface GeoRequestRepository extends JpaRepository<GeoRequest, Long> {
}
