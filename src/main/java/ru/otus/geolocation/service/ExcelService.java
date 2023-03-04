package ru.otus.geolocation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.otus.geolocation.domain.ShopAddress;
import ru.otus.geolocation.domain.ShopPoint;
import ru.otus.geolocation.service.util.WorktimeParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExcelService {

    private final DataFormatter dataFormatter = new DataFormatter();

    private final WorktimeParser worktimeParser;

    public List<ShopPoint> readFile(byte[] file) {
        if (file.length == 0) {
            return new ArrayList<>();
        }
        log.info("Парсим файл");
        List<ShopPoint> shopPoints = new ArrayList<>();
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(file))) {
            XSSFSheet sheet = wb.getSheetAt(0);
            int rowsQnt = sheet.getLastRowNum();
            for (int rowIndex = 1; rowIndex <= rowsQnt; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                String city = row.getCell(0) != null ? this.dataFormatter.formatCellValue(row.getCell(0)).trim() : "";
                city = city.toLowerCase();
                city = city.replace("ё", "е");
                String address = row.getCell(1) != null ? this.dataFormatter.formatCellValue(row.getCell(1)).trim() : "";
                String addressForSystem = row.getCell(2) != null ? this.dataFormatter.formatCellValue(row.getCell(2)).trim() : "";
                String workTimeRaw = row.getCell(3) != null ? this.dataFormatter.formatCellValue(row.getCell(3)).trim() : "";
                String workTime = workTimeRaw.isEmpty() ? "" : worktimeParser.correctWorkTime(workTimeRaw) ? "correct" : "incorrect";
                if (!isAnyEmpty(city, address)) {
                    ShopAddress shopAddress = new ShopAddress(null, address, -1.0D, -1.0D);
                    ShopPoint shopPoint = new ShopPoint(null, city, addressForSystem, workTimeRaw, workTime, shopAddress);
                    shopPoints.add(shopPoint);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        log.info("Извлечено {} записей", shopPoints.size());
        return shopPoints;
    }

    private boolean isAnyEmpty(String... fields) {
        return Stream.of(fields).anyMatch(String::isEmpty);
    }
}
