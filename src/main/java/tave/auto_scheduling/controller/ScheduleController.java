package tave.auto_scheduling.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tave.auto_scheduling.config.TerminateType;
import tave.auto_scheduling.dto.response.ApplicantAssignmentDto;
import tave.auto_scheduling.dto.response.ApplicantDto;
import tave.auto_scheduling.dto.response.ExcelFileInputStreamDto;
import tave.auto_scheduling.service.ExcelGenerateService;
import tave.auto_scheduling.service.ScheduleService;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ExcelGenerateService excelGenerateService;

    @RequestMapping(value = "/api/auto-matching/{type}", method = RequestMethod.POST)
    public ResponseEntity<ApplicantAssignmentDto> startAutoSchedule(
            @RequestPart(value = "file") MultipartFile file,
            @PathVariable String type
    ) {

        TerminateType terminateType = TerminateType.valueOf(type.toUpperCase());
        ApplicantAssignmentDto response = scheduleService.startSchedule(file, terminateType);

        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/api/matching-excel", method = RequestMethod.POST)
    public ResponseEntity<InputStreamResource> createSchedulingExcel(@RequestBody List<ApplicantDto> dataList) throws IOException {

        ExcelFileInputStreamDto excelFileDto = excelGenerateService.generateInterviewScheduleExcel(dataList);

        return ResponseEntity.ok()
                .headers(excelFileDto.headers())
                .contentLength(excelFileDto.contentLength())
                .body(excelFileDto.inputStreamResource());
    }

}
