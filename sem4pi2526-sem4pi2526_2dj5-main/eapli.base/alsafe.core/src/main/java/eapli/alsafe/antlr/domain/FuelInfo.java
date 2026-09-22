package eapli.alsafe.antlr.domain;

/**
 * Represents the fuel information for a flight leg.
 */
public class FuelInfo {

    public enum FuelUnit { KG, L }

    private final double quantity;
    private final FuelUnit unit;

    public FuelInfo(double quantity, FuelUnit unit) {
        this.quantity = quantity;
        this.unit = unit;
    }

    public double getQuantity() { return quantity; }
    public FuelUnit getUnit() { return unit; }

    @Override
    public String toString() {
        return quantity + " " + unit.name().toLowerCase();
    }
}