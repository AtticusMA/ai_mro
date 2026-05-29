package com.mro.aircraft.engine;

import com.mro.aircraft.parser.FlightDataMessage;
import org.springframework.stereotype.Component;

/**
 * ATA28 — Fuel quantity low warning.
 * Triggers if qar.fuel_qty falls below minimum safe level.
 */
@Component
public class FuelQuantityRule implements FaultRule {

    private static final String METRIC = "qar.fuel_qty";
    private static final double WARNING_THRESHOLD = 3000.0;
    private static final double CRITICAL_THRESHOLD = 1500.0;

    @Override
    public FaultRuleResult evaluate(FlightDataMessage message) {
        Object raw = message.metrics().get(METRIC);
        if (raw == null) return null;
        double value = toDouble(raw);
        if (value < CRITICAL_THRESHOLD) {
            return new FaultRuleResult("ATA28-001", "critical", "燃油系统", "燃油量严重不足: " + value + " kg");
        }
        if (value < WARNING_THRESHOLD) {
            return new FaultRuleResult("ATA28-001", "minor", "燃油系统", "燃油量偏低: " + value + " kg");
        }
        return null;
    }

    private double toDouble(Object val) {
        return val instanceof Number n ? n.doubleValue() : Double.parseDouble(val.toString());
    }
}
