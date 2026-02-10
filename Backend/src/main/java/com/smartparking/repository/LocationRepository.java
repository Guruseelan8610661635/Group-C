package com.smartparking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smartparking.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
