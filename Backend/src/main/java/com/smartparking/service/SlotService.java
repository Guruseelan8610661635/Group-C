package com.smartparking.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartparking.dto.SlotRequest;
import com.smartparking.dto.SlotResponse;
import com.smartparking.model.Location;
import com.smartparking.model.Slot;
import com.smartparking.repository.LocationRepository;
import com.smartparking.repository.SlotRepository;

@Service
public class SlotService {

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private LocationRepository locationRepository;

    /**
     * Get all slots in the system
     */
    public List<Slot> getAllSlots() {
        return slotRepository.findAllWithLocation();
    }

    /**
     * Get all slots for a specific location
     */
    public List<Slot> getSlotsByLocation(Long locationId) {
        return slotRepository.findByLocationId(locationId);
    }

    /**
     * Get all available slots
     */
    public List<Slot> getAvailableSlots() {
        return slotRepository.findAll().stream()
                .filter(s -> s.isAvailable() && !s.isDisabled())
                .collect(Collectors.toList());
    }

    /**
     * Get available slots for a specific location
     */
    public List<Slot> getAvailableSlotsByLocation(Long locationId) {
        return slotRepository.findByLocationId(locationId).stream()
                .filter(s -> s.isAvailable() && !s.isDisabled())
                .collect(Collectors.toList());
    }

    /**
     * Add a new parking slot
     */
    public Slot addSlot(SlotRequest slotRequest) {
        Location location = locationRepository.findById(slotRequest.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + slotRequest.getLocationId()));

        Slot slot = new Slot();
        slot.setSlotNumber(slotRequest.getSlotNumber());
        slot.setLocation(location);
        // ✅ Handle null Boolean by defaulting to true
        Boolean availableFromRequest = slotRequest.isAvailable();
        boolean finalAvailable = availableFromRequest != null ? availableFromRequest : true;
        System.out.println("🔵 [SlotService.addSlot] Available from request: " + availableFromRequest);
        System.out.println("🔵 [SlotService.addSlot] Final available value: " + finalAvailable);
        slot.setAvailable(finalAvailable);
        
        Slot savedSlot = slotRepository.save(slot);
        System.out.println("🔵 [SlotService.addSlot] After save, slot.isAvailable(): " + savedSlot.isAvailable());

        return savedSlot;
    }

    /**
     * Get a slot by ID
     */
    public Slot getSlotById(Long id) {
        return slotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found with ID: " + id));
    }

    /**
     * Update a slot's details
     */
    public Slot updateSlot(Long id, SlotRequest slotRequest) {
        Slot slot = getSlotById(id);

        if (slotRequest.getLocationId() != null && !slotRequest.getLocationId().equals(slot.getLocation().getId())) {
            Location newLocation = locationRepository.findById(slotRequest.getLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + slotRequest.getLocationId()));
            slot.setLocation(newLocation);
        }

        if (slotRequest.getSlotNumber() != null && !slotRequest.getSlotNumber().isEmpty()) {
            slot.setSlotNumber(slotRequest.getSlotNumber());
        }

        if (slotRequest.getSlotType() != null && !slotRequest.getSlotType().isEmpty()) {
            slot.setSlotType(slotRequest.getSlotType());
        }

        return slotRepository.save(slot);
    }

    /**
     * Toggle slot availability
     */
    public Slot toggleSlotAvailability(Long id) {
        Slot slot = getSlotById(id);
        System.out.println("🔵 [SlotService.toggleSlotAvailability] Before toggle: available=" + slot.isAvailable());
        slot.setAvailable(!slot.isAvailable());
        System.out.println("🔵 [SlotService.toggleSlotAvailability] After setAvailable: available=" + slot.isAvailable());
        Slot savedSlot = slotRepository.save(slot);
        System.out.println("🔵 [SlotService.toggleSlotAvailability] After save: available=" + savedSlot.isAvailable());
        return savedSlot;
    }

    /**
     * Set slot availability status
     */
    public Slot setSlotAvailability(Long id, boolean available) {
        Slot slot = getSlotById(id);
        slot.setAvailable(available);
        return slotRepository.save(slot);
    }

    /**
     * Delete a slot
     */
    public void deleteSlot(Long id) {
        if (!slotRepository.existsById(id)) {
            throw new IllegalArgumentException("Slot not found with ID: " + id);
        }
        slotRepository.deleteById(id);
    }

    /**
     * Get slot count for a location
     */
    public long getSlotCountByLocation(Long locationId) {
        return slotRepository.findByLocationId(locationId).size();
    }

    /**
     * Get available slot count for a location
     */
    public long getAvailableSlotCountByLocation(Long locationId) {
        return slotRepository.findByLocationId(locationId).stream()
                .filter(s -> s.isAvailable() && !s.isDisabled())
                .count();
    }

    /**
     * Get disabled/maintenance slot count for a location
     */
    public long getDisabledSlotCountByLocation(Long locationId) {
        return slotRepository.findByLocationId(locationId).stream()
                .filter(Slot::isDisabled)
                .count();
    }

    /**
     * Disable a slot with maintenance notes
     */
    public Slot disableSlot(Long id, String maintenanceNotes) {
        Slot slot = getSlotById(id);
        slot.setDisabled(true);
        slot.setMaintenanceNotes(maintenanceNotes);
        // Don't change available status - disabled is separate from available/occupied
        System.out.println("🔧 [SlotService.disableSlot] Slot " + id + " disabled with notes: " + maintenanceNotes);
        return slotRepository.save(slot);
    }

    /**
     * Enable a slot (remove maintenance mode)
     */
    public Slot enableSlot(Long id) {
        Slot slot = getSlotById(id);
        slot.setDisabled(false);
        slot.setMaintenanceNotes(null);
        // Don't force available=true - let slot return to its actual availability state
        System.out.println("✅ [SlotService.enableSlot] Slot " + id + " enabled");
        return slotRepository.save(slot);
    }

    /**
     * Update maintenance notes for a slot
     */
    public Slot updateMaintenanceNotes(Long id, String maintenanceNotes) {
        Slot slot = getSlotById(id);
        slot.setMaintenanceNotes(maintenanceNotes);
        System.out.println("📝 [SlotService.updateMaintenanceNotes] Updated notes for Slot " + id);
        return slotRepository.save(slot);
    }

    /**
     * Convert Slot entity to SlotResponse DTO
     */
    public SlotResponse convertToResponse(Slot slot) {
        SlotResponse response = new SlotResponse(
                slot.getId(),
                slot.getSlotNumber(),
                slot.isAvailable(),
                slot.getLocation().getId(),
                slot.getLocation().getName()
        );
        response.setDisabled(slot.isDisabled());
        response.setMaintenanceNotes(slot.getMaintenanceNotes());
        response.setSlotType(slot.getSlotType());
        return response;
    }

    /**
     * Convert list of Slot entities to SlotResponse DTOs
     */
    public List<SlotResponse> convertToResponseList(List<Slot> slots) {
        return slots.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}
