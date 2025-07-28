package tave.auto_scheduling.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import tave.auto_scheduling.dto.response.ApplicantDto;
import tave.auto_scheduling.dto.response.ExcelFileInputStreamDto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelGenerateService {

    private final String[] headers = {"이름", "성별", "이메일", "지원 파트", "대학", "면접 날짜", "면접 시간", "기수"};
    private static final String WORKBOOK_NAME = "INTERVIEW_SCHEDULE";

    public ExcelFileInputStreamDto generateInterviewScheduleExcel(List<ApplicantDto> dataList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(WORKBOOK_NAME);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (ApplicantDto applicant : dataList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(applicant.name());
                row.createCell(1).setCellValue(applicant.sex());
                row.createCell(2).setCellValue(applicant.email());
                row.createCell(3).setCellValue(applicant.part());
                row.createCell(4).setCellValue(applicant.univ());
                row.createCell(5).setCellValue(applicant.interviewDate());
                row.createCell(6).setCellValue(applicant.interviewTime());
                row.createCell(7).setCellValue(16);
            }

            workbook.write(out);
            ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            String filename = "interview_schedule_" + System.currentTimeMillis() + ".xlsx";
            responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

            return ExcelFileInputStreamDto.from(bis, responseHeaders, bis.available());
        }
    }

}