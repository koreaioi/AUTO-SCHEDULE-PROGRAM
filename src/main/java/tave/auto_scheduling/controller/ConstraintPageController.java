package tave.auto_scheduling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tave.auto_scheduling.dto.request.ConstraintConfigUpdateRequest;
import tave.auto_scheduling.service.ConstraintConfigService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/constraints")
public class ConstraintPageController {

    private final ConstraintConfigService constraintConfigService;

    @GetMapping
    public String listPage(Model model) {
        model.addAttribute("constraints", constraintConfigService.findAll());
        return "constraints";
    }

    @PostMapping("/{name}")
    public String update(
            @PathVariable String name,
            @RequestParam(required = false, defaultValue = "false") Boolean enabled,
            @RequestParam(required = false) Integer weight,
            @RequestParam(required = false) Integer threshold,
            @RequestParam(required = false) List<String> preferredDays) {
        String days = (preferredDays != null && !preferredDays.isEmpty())
                ? String.join(",", preferredDays) : null;
        constraintConfigService.update(name,
                new ConstraintConfigUpdateRequest(enabled, weight, threshold, days));
        return "redirect:/constraints";
    }
}
