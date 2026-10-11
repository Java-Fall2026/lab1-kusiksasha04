package ua.restaurant.model;

public enum Zone {
    MAIN_HALL(1.0), TERRACE(1.1), VIP(1.5), BAR(0.9);

    private final double priceFactor;

    Zone(double priceFactor) {
        this.priceFactor = priceFactor;
    }

    public double getPriceFactor() {
        return priceFactor;
    }
}
