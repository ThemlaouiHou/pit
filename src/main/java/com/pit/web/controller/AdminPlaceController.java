package com.pit.web.controller;

import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.service.PlaceService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Admin UI endpoints for moderation.
@Controller
@RequestMapping("/admin/places")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlaceController {

    private final PlaceService placeService;

    @GetMapping
    // Handles dashboard request operation
    public String dashboard(@RequestParam(defaultValue = "0") @Min(0) int page,
                            Model model) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), 10,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Place> pending = placeService.findByStatus(PlaceStatus.PENDING, pageable);
        Page<Place> rejected = placeService.findByStatus(PlaceStatus.REJECTED, pageable);

        model.addAttribute("pendingPage", pending);
        model.addAttribute("rejectedPage", rejected);
        return "admin/places";
    }

    @PostMapping("/{id}/approve")
    // Handles approve request operation
    public String approve(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            placeService.approve(id);
            redirect.addFlashAttribute("success", "Lieu publié avec succès.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", "Lieu introuvable.");
        }
        return "redirect:/admin/places";
    }

    @PostMapping("/{id}/reject")
    // Handles reject request operation
    public String reject(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            placeService.reject(id);
            redirect.addFlashAttribute("success", "Lieu mis en attente / refusé.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", "Lieu introuvable.");
        }
        return "redirect:/admin/places";
    }

    @PostMapping("/{id}/delete")
    // Handles delete request operation
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            placeService.delete(id);
            redirect.addFlashAttribute("success", "Lieu supprimé définitivement.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", "Lieu introuvable.");
        }
        return "redirect:/admin/places";
    }
}
