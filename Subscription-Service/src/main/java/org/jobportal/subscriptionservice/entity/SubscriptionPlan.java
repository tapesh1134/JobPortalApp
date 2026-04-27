package org.jobportal.subscriptionservice.entity;

public enum SubscriptionPlan {

    MONTHLY(999, 1),
    QUARTERLY(2499, 3),
    HALF_YEARLY(4499, 6),
    FULL_YEAR(7999, 12);

    private final double price;
    private final int durationInMonths;

    SubscriptionPlan(double price, int durationInMonths) {
        this.price = price;
        this.durationInMonths = durationInMonths;
    }

    public double getPrice() {
        return price;
    }

    public int getDurationInMonths() {
        return durationInMonths;
    }
}