package controller.weather;

import eapli.alsafe.utils.provider.weatherParsers.ExcelWeatherDataImportProvider;
import eapli.alsafe.weather.domain.WeatherParsing.ParseResult;
import eapli.alsafe.weather.domain.WeatherParsing.ParsedWeatherDataLine;
import eapli.alsafe.weather.domain.WeatherParsing.WeatherImportError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExcelWeatherDataImportProviderTest {

    @TempDir
    Path tempDir;

    private Path createExcelFile(final String[][] rows) throws IOException {
        final Path file = tempDir.resolve("data.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            final XSSFSheet sheet = workbook.createSheet("WeatherData");
            for (int i = 0; i < rows.length; i++) {
                final Row row = sheet.createRow(i);
                for (int j = 0; j < rows[i].length; j++) {
                    row.createCell(j).setCellValue(rows[i][j]);
                }
            }
            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                workbook.write(fos);
            }
        }
        return file;
    }

    @Test
    void validExcel_returnsParsedLines() throws IOException {
        final Path file = createExcelFile(new String[][]{
                {"airControlAreaCode", "date", "windDirection", "windSpeed"},
                {"ACA-0001", "2026-05-20", "180", "12.5"},
                {"ACA-0002", "2026-05-21", "90", "8.0"}
        });

        final ExcelWeatherDataImportProvider provider = new ExcelWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertEquals(2, result.parsedLines().size());
        assertFalse(result.hasErrors());
        assertEquals("ACA-0001", result.parsedLines().get(0).getCode());
        assertEquals("ACA-0002", result.parsedLines().get(1).getCode());
    }

    @Test
    void excelWithInvalidNumber_returnsParseError() throws IOException {
        final Path file = createExcelFile(new String[][]{
                {"airControlAreaCode", "date", "windDirection", "windSpeed"},
                {"ACA-0001", "2026-05-20", "abc", "12.5"}
        });

        final ExcelWeatherDataImportProvider provider = new ExcelWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertFalse(result.hasParsedLines());
        assertTrue(result.hasErrors());
    }

    @Test
    void excelWithMissingFields_returnsParseError() throws IOException {
        final Path file = createExcelFile(new String[][]{
                {"airControlAreaCode", "date", "windDirection", "windSpeed"},
                {"ACA-0001", "2026-05-20", "180", "12.5"},
                {"ACA-0002", "2026-05-21", "90", ""}
        });

        final ExcelWeatherDataImportProvider provider = new ExcelWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertEquals(1, result.parsedLines().size());
        assertTrue(result.hasErrors());
    }

    @Test
    void excelEmptyHeaderOnly_returnsNoLinesNoErrors() throws IOException {
        final Path file = createExcelFile(new String[][]{
                {"airControlAreaCode", "date", "windDirection", "windSpeed"}
        });

        final ExcelWeatherDataImportProvider provider = new ExcelWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertFalse(result.hasParsedLines());
        assertFalse(result.hasErrors());
    }

    @Test
    void excelEmptySheet_returnsNoLinesNoErrors() throws IOException {
        final Path file = tempDir.resolve("empty.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("WeatherData");
            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                workbook.write(fos);
            }
        }

        final ExcelWeatherDataImportProvider provider = new ExcelWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertFalse(result.hasParsedLines());
        assertTrue(result.hasErrors());
    }

    @Test
    void fileNotFound_returnsParseError() {
        final ExcelWeatherDataImportProvider provider =
                new ExcelWeatherDataImportProvider(tempDir.resolve("nonexistent.xlsx").toString());
        final ParseResult result = provider.getWeatherData();

        assertFalse(result.hasParsedLines());
        assertTrue(result.hasErrors());
        assertEquals(WeatherImportError.Category.PARSE_ERROR, result.errors().get(0).getCategory());
    }

    @Test
    void excelWithMixedValidAndInvalid_returnsPartialResult() throws IOException {
        final Path file = createExcelFile(new String[][]{
                {"airControlAreaCode", "date", "windDirection", "windSpeed"},
                {"ACA-0001", "2026-05-20", "180", "12.5"},
                {"", "2026-05-21", "90", "8.0"},
                {"ACA-0003", "2026-05-22", "45", "15.3"}
        });

        final ExcelWeatherDataImportProvider provider = new ExcelWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertEquals(2, result.parsedLines().size());
        assertEquals(1, result.errors().size());
    }
}
