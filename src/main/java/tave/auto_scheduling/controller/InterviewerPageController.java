package tave.auto_scheduling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tave.auto_scheduling.dto.request.InterviewerCreateRequest;
import tave.auto_scheduling.service.InterviewerService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/interviewers")
public class InterviewerPageController {

    private final InterviewerService interviewerService;

    @GetMapping
    public String listPage(Model model) {
        model.addAttribute("interviewers", interviewerService.findAll());
        return "interviewers";
    }

    @PostMapping
    public String create(
            @RequestParam String name,
            @RequestParam String part,
            @RequestParam(defaultValue = "") String availableTimesRaw,
            RedirectAttributes ra) {
        try {
            List<LocalDateTime> times = parseAvailableTimes(availableTimesRaw);
            interviewerService.create(new InterviewerCreateRequest(name, part, times));
        } catch (DateTimeParseException e) {
            ra.addFlashAttribute("error",
                    "날짜 형식 오류: '" + e.getParsedString() + "' — 올바른 형식: 2024-03-01T10:00");
        }
        return "redirect:/interviewers";
    }

    @PostMapping("/{id}/update")
    public String update(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String part,
            @RequestParam(defaultValue = "") String availableTimesRaw,
            RedirectAttributes ra) {
        try {
            List<LocalDateTime> times = parseAvailableTimes(availableTimesRaw);
            interviewerService.update(id, new InterviewerCreateRequest(name, part, times));
        } catch (DateTimeParseException e) {
            ra.addFlashAttribute("error",
                    "날짜 형식 오류: '" + e.getParsedString() + "' — 올바른 형식: 2024-03-01T10:00");
        }
        return "redirect:/interviewers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        interviewerService.delete(id);
        return "redirect:/interviewers";
    }

    private List<LocalDateTime> parseAvailableTimes(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .toList();
    }
}
