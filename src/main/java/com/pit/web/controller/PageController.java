package com.pit.web.controller;

import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.domain.Rating;
import com.pit.service.AuthService;
import com.pit.service.PlaceService;
import com.pit.service.RatingService;
import com.pit.web.view.PlaceForm;
import com.pit.web.view.RatingForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@Controller
@RequiredArgsConstructor
public class PageController {

    private final PlaceService placeService;
    private final RatingService ratingService;
    private final AuthService authService;

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "0") int page,
                       Model model) {
        int pageIndex = Math.max(page, 0);
        Page<Place> places = placeService.findApproved(PageRequest.of(pageIndex, 9,
                Sort.by(Sort.Direction.DESC, "createdAt")));
        model.addAttribute("placesPage", places);
        authService.getCurrentUser().ifPresent(user -> model.addAttribute("currentUser", user));

        if (!model.containsAttribute("placeForm")) {
            model.addAttribute("placeForm", new PlaceForm());
        }
        return "home";
    }

    @GetMapping("/places/{id}")
    public String place(@PathVariable Long id,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(name = "newRating", required = false) String newRating,
                        Model model) {
        Place place;
        try {
            place = placeService.findById(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (!canView(place)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("place", place);
        model.addAttribute("canRate", place.getStatus() == PlaceStatus.APPROVED);
        boolean isAdmin = authService.isCurrentUserAdmin();
        model.addAttribute("canDelete", isAdmin);

        int pageIndex = Math.max(page, 0);
        PageRequest pageable = PageRequest.of(pageIndex, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Rating> ratingsPage = ratingService.findByPlace(id, pageable);
        model.addAttribute("ratingsPage", ratingsPage);

        RatingForm ratingForm = model.containsAttribute("ratingForm")
                ? (RatingForm) model.asMap().get("ratingForm")
                : new RatingForm();

        authService.getCurrentUser().ifPresent(user -> {
            model.addAttribute("currentUser", user);
            if (newRating == null) {
                ratingService.findByUserAndPlace(id, user.getId()).ifPresent(existing -> {
                    if (ratingForm.getScore() == null) {
                        ratingForm.setScore(existing.getScore());
                    }
                    if (ratingForm.getComment() == null || ratingForm.getComment().isBlank()) {
                        ratingForm.setComment(existing.getComment());
                    }
                });
            }
        });

        model.addAttribute("ratingForm", ratingForm);
        return "places/detail";
    }

    @PostMapping("/places")
    @PreAuthorize("isAuthenticated()")
    public String submitPlace(@Valid @ModelAttribute("placeForm") PlaceForm form,
                              BindingResult binding,
                              RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.placeForm", binding);
            redirect.addFlashAttribute("placeForm", form);
            redirect.addFlashAttribute("error", "Merci de corriger les erreurs du formulaire.");
            return "redirect:/";
        }
        Long userId = authService.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        placeService.create(form.getName(), form.getDescription(),
                form.getLat(), form.getLng(), userId);
        redirect.addFlashAttribute("success", "Lieu soumis pour validation !");
        return "redirect:/";
    }

    @PostMapping("/places/{id}/ratings")
    @PreAuthorize("isAuthenticated()")
    public String submitRating(@PathVariable Long id,
                               @Valid @ModelAttribute("ratingForm") RatingForm form,
                               BindingResult binding,
                               RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.ratingForm", binding);
            redirect.addFlashAttribute("ratingForm", form);
            redirect.addFlashAttribute("error", "Merci de corriger les erreurs de notation.");
            return "redirect:/places/" + id;
        }
        Long userId = authService.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            ratingService.rate(id, userId, form.getScore(), form.getComment());
            redirect.addFlashAttribute("success", "Merci pour votre avis !");
            return "redirect:/places/" + id + "?newRating=1";
        } catch (IllegalStateException ex) {
            redirect.addFlashAttribute("error", "Seuls les lieux publiés peuvent être évalués.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", "Impossible d'enregistrer la note.");
        }
        return "redirect:/places/" + id;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    private boolean canView(Place place) {
        if (place.getStatus() == PlaceStatus.APPROVED) {
            return true;
        }
        Long currentUserId = authService.getCurrentUserId();
        boolean isAdmin = authService.isCurrentUserAdmin();
        boolean isOwner = currentUserId != null && place.getCreatedBy() != null
                && place.getCreatedBy().getId().equals(currentUserId);
        return isAdmin || isOwner;
    }
}
