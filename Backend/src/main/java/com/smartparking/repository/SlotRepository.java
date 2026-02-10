package com.smartparking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartparking.model.Slot;

import jakarta.persistence.LockModeType;
	
public interface SlotRepository extends JpaRepository<Slot, Long> {
	List<Slot> findByLocationId(Long locationId);
	
	@Query("SELECT s FROM Slot s JOIN FETCH s.location")
	List<Slot> findAllWithLocation();
	
	/**
	 * Find slot by ID with pessimistic write lock
	 * This ensures only one transaction can access this slot at a time,
	 * preventing race conditions during concurrent booking attempts
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM Slot s WHERE s.id = :id")
	Optional<Slot> findByIdWithLock(@Param("id") Long id);
}
