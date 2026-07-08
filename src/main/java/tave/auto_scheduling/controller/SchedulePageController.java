package tave.auto_scheduling.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tave.auto_scheduling.config.TerminateType;
import tave.auto_scheduling.dto.response.ApplicantAssignmentDto;
import tave.auto_scheduling.dto.response.ExcelFileInputStreamDto;
import tave.auto_scheduling.service.ExcelGenerateService;
import tave.auto_scheduling.service.ScheduleService;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class SchedulePageController {

    private final ScheduleService scheduleService;
    private final ExcelGenerateService excelGenerateService;

    @GetMapping("/")
    public String root() {
        return "redirect:/schedule";
    }

    @GetMapping("/schedule")
    public String schedulePage(Model model, HttpSession session) {
        ApplicantAssignmentDto result =
                (ApplicantAssignmentDto) session.getAttribute("scheduleResult");
        if (result != null) {
            model.addAttribute("result", result);
        }
        return "schedule";
    }

    @PostMapping("/schedule")
    public String runSchedule(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") TerminateType type,
            HttpSession session,
            RedirectAttributes ra) {
        try {
            ApplicantAssignmentDto dto = scheduleService.startSchedule(file, type);
            session.setAttribute("scheduleResult", dto);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/schedule";
    }

    @GetMapping("/schedule/download")
    public Object download(HttpSession session, RedirectAttributes ra) {
        ApplicantAssignmentDto result =
                (ApplicantAssignmentDto) session.getAttribute("scheduleResult");
        if (result == null) {
            ra.addFlashAttribute("error", "다운로드할 결과가 없습니다. 먼저 스케줄링을 실행하세요.");
            return "redirect:/schedule";
        }
        try {
            ExcelFileInputStreamDto excelDto =
                    excelGenerateService.generateInterviewScheduleExcel(result.dataList());
            return ResponseEntity.ok()
                    .headers(excelDto.headers())
                    .contentLength(excelDto.contentLength())
                    .body(excelDto.inputStreamResource());
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Excel 파일 생성 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/schedule";
        }
    }
}
