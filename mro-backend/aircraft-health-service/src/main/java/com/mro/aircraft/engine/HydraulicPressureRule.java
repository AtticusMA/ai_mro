package com.mro.aircraft.engine;

import com.mro.aircraft.parser.FlightDataMessage;
import org.springframework.stereotype.Component;

/**
 * ATA27 — Flight controls: detects abnormal aileron actuator pressure.
 * Triggers if hydraulic_pressure_a < 2500 PSI.
 */
@Component
public class HydraulicPressureRule implements FaultRule {

    private static final String METRIC = "hydraulic_pressure_a";
    private static final double LOW_THRESHOLD = 2500.0;
    private static final double CRITICAL_THRESHOLD = 2000.0;

    @Override
    public FaultRuleResult evaluate(FlightDataMessage message) {
        Object raw = message.metrics().get(METRIC);
        if (raw == null) return null;
        double value = toDouble(raw);
        if (value < CRITICAL_THRESHOLD) {
            return new FaultRuleResult("ATA27-001", "critical", "液压系统A", "液压压力严重偏低: " + value + " PSI");
        }
        if (value < LOW_THRESHOLD) {
            return new FaultRuleResult("ATA27-001", "major", "液压系统A", "液压压力偏低: " + value + " PSI");
        }
        return null;
    }

    private double toDouble(Object val) {
        return val instanceof Number n ? n.doubleValue() : Double.parseDouble(val.toString());
    }
}
