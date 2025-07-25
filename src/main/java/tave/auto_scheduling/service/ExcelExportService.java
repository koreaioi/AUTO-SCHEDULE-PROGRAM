package tave.auto_scheduling.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tave.auto_scheduling.domain.Applicant;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
public class ExcelExportService {

    public List<Applicant> loadApplicantsFromExcel(MultipartFile file) {
        try (InputStream excelInputStream = file.getInputStream()) {
            return parseExcel(excelInputStream);
        } catch (IOException e) {
            throw new UncheckedIOException("[Excel]파일을 읽을 수 없습니다.", e);
        }
    }

    public List<Applicant> parseExcel(InputStream excelInputStream) {
        List<Applicant> applicants = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(excelInputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            if (rows.hasNext()) rows.next(); // 헤더 스킵

            while (rows.hasNext()) {
                Row row = rows.next();
                String name = getStringCellValue(row.getCell(0));
                String sex = getStringCellValue(row.getCell(1));
                String email = getStringCellValue(row.getCell(2));
                String part = getStringCellValue(row.getCell(3));
                String univ = getStringCellValue(row.getCell(4));

                List<LocalDateTime> availableTimes = new ArrayList<>();
                for (int i = 5; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null && cell.getCellType() == CellType.STRING) {
                        String[] timeStrings = cell.getStringCellValue().split("[,\\n]");
                        String dateStr = sheet.getRow(0).getCell(i).getStringCellValue();
                        LocalDate date = LocalDate.parse(dateStr);

                        for (String t : timeStrings) {
                            String cleaned = t.replaceAll("[^0-9:]", "").trim();
                            if (!cleaned.isEmpty()) {
                                try {
                                    LocalTime time = LocalTime.parse(cleaned);
                                    availableTimes.add(LocalDateTime.of(date, time));
                                } catch (DateTimeParseException e) {
                                    log.error("잘못된 시간 형식: \"{}\" at column {} for {}", cleaned, i, name);
                                }
                            }
                        }
                    }
                }

                applicants.add(Applicant.of(name, sex, email, part, univ, availableTimes));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("[Excel]파일을 읽을 수 없습니다.", e);
        }
        return applicants;
    }


    private String getStringCellValue(Cell cell) {
        return cell == null ? "" : cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : cell.toString();
    }
}