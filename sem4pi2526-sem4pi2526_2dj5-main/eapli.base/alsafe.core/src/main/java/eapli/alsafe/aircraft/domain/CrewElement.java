package eapli.alsafe.aircraft.domain;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "aircraft_crew_element")
public class CrewElement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Aircraft aircraft;

    @Column(nullable = false, length = 120)
    private String role;

    protected CrewElement() {
    }

    public CrewElement(final String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Crew role cannot be null or blank");
        }
        this.role = role.trim();
    }

    void setAircraft(final Aircraft aircraft) {
        this.aircraft = aircraft;
    }

    public Aircraft aircraft() {
        return aircraft;
    }

    public String role() {
        return role;
    }
}
