package eapli.alsafe.simulationreport.application;

import java.io.IOException;
import java.nio.file.Path;
import java.time.YearMonth;

public interface ReportGenerator {

    Path generate(YearMonth period) throws IOException;

    String reportType();
}
