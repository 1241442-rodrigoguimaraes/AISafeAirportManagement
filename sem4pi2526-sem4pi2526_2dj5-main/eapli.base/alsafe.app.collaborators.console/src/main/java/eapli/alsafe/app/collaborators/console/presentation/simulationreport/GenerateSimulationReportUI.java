package eapli.alsafe.app.collaborators.console.presentation.simulationreport;

import eapli.alsafe.simulationreport.application.GenerateSimulationReportController;
import eapli.framework.presentation.console.AbstractUI;

import java.io.IOException;
import java.nio.file.Path;

@SuppressWarnings("squid:S106")
public class GenerateSimulationReportUI extends AbstractUI {

    private final GenerateSimulationReportController controller = new GenerateSimulationReportController();

    @Override
    protected boolean doShow() {
        try {
            final Path reportPath = controller.generateReport();
            System.out.printf("%nSimulation report generated successfully: %s%n", reportPath);
        } catch (final SecurityException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (final IOException e) {
            System.out.println("Error generating simulation report: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Generate Simulation Report";
    }
}
