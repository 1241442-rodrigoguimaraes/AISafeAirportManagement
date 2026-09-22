package eapli.alsafe.aircraft.application;

public class AircraftDTO {
    public final String registrationID;
    public final String aircraftModel;
    public final String company;
    public final String cabinConfiguration;
    public final String maintenanceStatus;

    public AircraftDTO(final String registrationID, final String aircraftModel, final String company,
                       final String cabinConfiguration, final String maintenanceStatus) {
        this.registrationID = registrationID;
        this.aircraftModel = aircraftModel;
        this.company = company;
        this.cabinConfiguration = cabinConfiguration;
        this.maintenanceStatus = maintenanceStatus;
    }

    @Override
    public String toString() {
        return String.format("Registration: %s | Model: %s | Company: %s | Config: %s | Status: %s",
                registrationID, aircraftModel, company, cabinConfiguration, maintenanceStatus);
    }
}
