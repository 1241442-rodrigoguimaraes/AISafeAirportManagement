package eapli.alsafe.utils.provider.weatherParsers;

import eapli.alsafe.utils.provider.interfaces.WeatherDataImportProvider;
import eapli.alsafe.weather.domain.WeatherParsing.ParseResult;
import eapli.alsafe.weather.domain.WeatherParsing.ParsedWeatherDataLine;
import eapli.alsafe.weather.domain.WeatherParsing.WeatherImportError;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExcelWeatherDataImportProvider implements WeatherDataImportProvider {

    private final String filePath;

    public ExcelWeatherDataImportProvider(final String filePath) {
        this.filePath = Objects.requireNonNull(filePath, "File path cannot be null.");
    }

    @Override
    public ParseResult getWeatherData() {
        final List<ParsedWeatherDataLine> parsedLines = new ArrayList<>();
        final List<WeatherImportError> errors = new ArrayList<>();

        final File file = new File(filePath);
        if (!file.exists()) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    filePath, 0, "File not found: " + filePath));
            return new ParseResult(parsedLines, errors);
        }

        try (Workbook workbook = WorkbookFactory.create(file)) {
            final Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() <= 1) {
                if (sheet.getPhysicalNumberOfRows() == 0) {
                    errors.add(new WeatherImportError(
                            WeatherImportError.Category.PARSE_ERROR,
                            filePath, 0, "Excel sheet is empty."));
                }
                return new ParseResult(parsedLines, errors);
            }

            int rowIndex = 0;
            for (Row row : sheet) {
                rowIndex++;
                if (rowIndex == 1) {
                    continue;
                }
                parseExcelRow(row, rowIndex, parsedLines, errors);
            }
        } catch (IOException e) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    filePath, 0, "Failed to read Excel file: " + e.getMessage()));
        } catch (RuntimeException e) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    filePath, 0, "Unexpected error reading Excel: " + e.getMessage()));
        }

        return new ParseResult(parsedLines, errors);
    }

    private void parseExcelRow(final Row row, final int rowNumber,
                                final List<ParsedWeatherDataLine> parsedLines,
                                final List<WeatherImportError> errors) {
        final String code = getCellString(row, 0);
        if (code == null || code.isEmpty()) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    rowToString(row), rowNumber,
                    "Empty or missing air control area code in column A."));
            return;
        }

        final String date = getCellString(row, 1);
        if (date == null || date.isEmpty()) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    rowToString(row), rowNumber,
                    "Empty or missing date in column B."));
            return;
        }

        try {
            final double windDirection = getCellNumeric(row, 2);
            final double windSpeed = getCellNumeric(row, 3);
            parsedLines.add(new ParsedWeatherDataLine(code, date, windDirection, windSpeed, rowNumber));
        } catch (NumberFormatException e) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    rowToString(row), rowNumber,
                    "Invalid numeric value: " + e.getMessage()));
        }
    }

    private String getCellString(final Row row, final int colIndex) {
        final Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            final String value = cell.getStringCellValue();
            return value == null ? null : value.trim();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        }
        return null;
    }

    private double getCellNumeric(final Row row, final int colIndex) {
        final Cell cell = row.getCell(colIndex);
        if (cell == null) {
            throw new NumberFormatException("Cell is empty.");
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            return Double.parseDouble(cell.getStringCellValue().trim());
        }
        throw new NumberFormatException("Cannot parse numeric from cell type: " + cell.getCellType());
    }

    private String rowToString(final Row row) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (i > 0) {
                sb.append(";");
            }
            final Cell cell = row.getCell(i);
            if (cell != null) {
                if (cell.getCellType() == CellType.STRING) {
                    sb.append(cell.getStringCellValue());
                } else if (cell.getCellType() == CellType.NUMERIC) {
                    sb.append(cell.getNumericCellValue());
                }
            }
        }
        return sb.toString();
    }
}
