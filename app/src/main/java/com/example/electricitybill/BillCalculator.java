package com.example.electricitybill;

public class BillCalculator {

    /**
     * Calculate total charges based on TNB block rate.
     * Block 1: 1–200 kWh  @ RM 0.218/kWh
     * Block 2: 201–300    @ RM 0.334/kWh
     * Block 3: 301–600    @ RM 0.516/kWh
     * Block 4: 601–1000   @ RM 0.546/kWh
     */
    public static double calculateTotal(double units) {
        double total = 0;

        if (units > 600) {
            total += (units - 600) * 0.546;
            units = 600;
        }
        if (units > 300) {
            total += (units - 300) * 0.516;
            units = 300;
        }
        if (units > 200) {
            total += (units - 200) * 0.334;
            units = 200;
        }
        total += units * 0.218;

        return total;
    }

    /**
     * Apply rebate percentage (0–5%) to get final cost.
     */
    public static double calculateFinal(double total, int rebatePct) {
        return total - (total * rebatePct / 100.0);
    }
}
