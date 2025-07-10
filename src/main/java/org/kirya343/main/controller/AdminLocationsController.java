package org.kirya343.main.controller;

import org.kirya343.main.model.listingModels.Location;
import org.kirya343.main.repository.LocationRepository;
import org.kirya343.main.services.UserService;
import org.kirya343.main.services.components.RoleCheckService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/locations")
public class AdminLocationsController {

    private final LocationRepository locationRepository;
    private final RoleCheckService roleCheckService;
    private final UserService userService;

    @GetMapping
    public String locationsList(Model model) {
        List<Location> locations = locationRepository.findAll() ;
        model.addAttribute("locations", locations);
        model.addAttribute("activePage", "admin-locations");
        return "admin/locations/locations-list";
    }

    @PostMapping("/add")
    public String addLocation(@RequestParam(required = false) Long countryId,
                                @RequestParam String locationName,
                                @AuthenticationPrincipal OAuth2User oauth2User,
                                RedirectAttributes redirectAttributes) {
        try {
            Location location = new Location();
            location.setName(locationName);
            location.setCity(countryId != null ? true : false);
            location.setCountry(countryId != null ? locationRepository.findById(countryId).orElse(null) : null);
            if (locationRepository.findByName(location.getName()) != null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Такая локация уже существует");
                return "redirect:/admin/locations";
            }
            locationRepository.save(location);
            redirectAttributes.addFlashAttribute("successMessage", "Локация добавлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при добавлении локации");
        }
        return "redirect:/admin/locations";
    }

    @GetMapping("/{id}/delete")
    public String deleteLocation(@PathVariable Long id, RedirectAttributes redirectAttributes, @AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            if (!roleCheckService.hasRoleAdmin(userService.findUserFromOAuth2(oAuth2User))) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении локации, вы не являетесь админом");
                return "redirect:/error";
            }
            locationRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Локация успешно удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении локации");
        }
        return "redirect:/admin/locations";
    }
}
