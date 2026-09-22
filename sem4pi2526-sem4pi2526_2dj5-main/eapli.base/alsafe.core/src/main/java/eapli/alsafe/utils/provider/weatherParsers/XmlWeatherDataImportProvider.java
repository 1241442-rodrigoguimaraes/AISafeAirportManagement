package eapli.alsafe.utils.provider.weatherParsers;

import eapli.alsafe.utils.provider.interfaces.WeatherDataImportProvider;
import eapli.alsafe.weather.domain.WeatherParsing.ParseResult;
import eapli.alsafe.weather.domain.WeatherParsing.ParsedWeatherDataLine;
import eapli.alsafe.weather.domain.WeatherParsing.WeatherImportError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XmlWeatherDataImportProvider implements WeatherDataImportProvider {

    private final String filePath;

    public XmlWeatherDataImportProvider(final String filePath) {
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

        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            final NodeList recordNodes = doc.getElementsByTagName("record");
            if (recordNodes.getLength() == 0) {
                final NodeList children = doc.getDocumentElement().getChildNodes();
                if (children.getLength() == 0) {
                    errors.add(new WeatherImportError(
                            WeatherImportError.Category.PARSE_ERROR,
                            filePath, 0, "No records found in XML."));
                    return new ParseResult(parsedLines, errors);
                }
            }

            for (int i = 0; i < recordNodes.getLength(); i++) {
                final Element record = (Element) recordNodes.item(i);
                final int lineNumber = i + 1;
                parseXmlRecord(record, lineNumber, parsedLines, errors);
            }
        } catch (SAXException e) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    filePath, 0, "Malformed XML: " + e.getMessage()));
        } catch (ParserConfigurationException | IOException e) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    filePath, 0, "Failed to read XML: " + e.getMessage()));
        }

        return new ParseResult(parsedLines, errors);
    }

    private void parseXmlRecord(final Element record, final int lineNumber,
                                 final List<ParsedWeatherDataLine> parsedLines,
                                 final List<WeatherImportError> errors) {
        final String code = getElementTextOrNull(record, "airControlAreaCode");
        if (code == null || code.isEmpty()) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    xmlRecordToString(record), lineNumber,
                    "Missing or empty 'airControlAreaCode'."));
            return;
        }

        final String date = getElementTextOrNull(record, "date");
        if (date == null || date.isEmpty()) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    xmlRecordToString(record), lineNumber,
                    "Missing or empty 'date'."));
            return;
        }

        try {
            final double windDirection = Double.parseDouble(
                    getElementTextOrThrow(record, "windDirection"));
            final double windSpeed = Double.parseDouble(
                    getElementTextOrThrow(record, "windSpeed"));
            parsedLines.add(new ParsedWeatherDataLine(code, date, windDirection, windSpeed, lineNumber));
        } catch (IllegalArgumentException e) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    xmlRecordToString(record), lineNumber,
                    "Invalid numeric value: " + e.getMessage()));
        }
    }

    private String getElementTextOrNull(final Element parent, final String tagName) {
        final NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() == 0) {
            return null;
        }
        final String text = list.item(0).getTextContent();
        if (text == null) {
            return null;
        }
        return text.trim();
    }

    private String getElementTextOrThrow(final Element parent, final String tagName) {
        final String value = getElementTextOrNull(parent, tagName);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Missing or empty element: " + tagName);
        }
        return value;
    }

    private String xmlRecordToString(final Element record) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<record>");
        final NodeList children = record.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                final Element el = (Element) children.item(i);
                sb.append("<").append(el.getTagName()).append(">")
                        .append(el.getTextContent())
                        .append("</").append(el.getTagName()).append(">");
            }
        }
        sb.append("</record>");
        return sb.toString();
    }
}
