package eapli.alsafe.pilotmanagement.application;

public class PilotDTO {

    private final Long id;
    private final String name;
    private final String email;
    private final String phone;
    private final String company;
    private final String status;
    private final String certifiedModels;

    public PilotDTO(final Long id, final String name, final String email, final String phone, final String company,
                    final String status, final String certifiedModels) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.company = company;
        this.status = status;
        this.certifiedModels = certifiedModels;
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public String phone() {
        return phone;
    }

    public String company() {
        return company;
    }

    public String status() {
        return status;
    }

    public String certifiedModels() {
        return certifiedModels;
    }

    @Override
    public String toString() {
        return String.format("#%d | %s | %s | %s | %s | %s | Certified models: %s",
                id, name, email, phone, company, status, certifiedModels);
    }
}
