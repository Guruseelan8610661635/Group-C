package com.smartparking.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.smartparking.dto.SlotResponse;
import com.smartparking.model.Slot;
import com.smartparking.repository.SlotRepository;
import com.smartparking.service.SlotService;

@RestController
@RequestMapping("/api/slots")
@CrossOrigin
public class SlotController {

    private final SlotRepository slotRepo;
    private final SlotService slotService;

    public SlotController(SlotRepository slotRepo, SlotService slotService) {
        this.slotRepo = slotRepo;
        this.slotService = slotService;
    }

    // ✅ UPDATED: Return slots with location info AND maintenance status
    @GetMapping
    public List<SlotResponse> getSlotsByLocation(
            @RequestParam(required = false) Long locationId) {

        System.out.println("📍 [SlotController.getSlotsByLocation] Requested with locationId: " + locationId);
        
        List<Slot> slots;
        if (locationId != null) {
            slots = slotRepo.findByLocationId(locationId);
            System.out.println("   Found " + slots.size() + " slots for location " + locationId);
        } else {
            slots = slotRepo.findAll();
            System.out.println("   Found " + slots.size() + " slots (no location filter)");
        }

        return slots.stream()
                .map(slotService::convertToResponse)
                .collect(Collectors.toList());
    }

    // ✅ PUT /api/slots/{id}  (ADMIN only)
    @PutMapping("/{id}")
    public Slot updateAvailability(
            @PathVariable Long id,
            @RequestBody Slot updatedSlot) {

        Slot slot = slotRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        slot.setAvailable(updatedSlot.isAvailable());
        return slotRepo.save(slot);
    }
}
