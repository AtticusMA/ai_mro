package com.mro.aircraft.engine;

import com.mro.aircraft.parser.FlightDataMessage;
import org.springframework.stereotype.Component;

/**
 * ATA71 — Engine N1 speed anomaly detection.
 * Triggers if engine_n1_1 or engine_n1_2 deviation exceeds 5% from expected range.
 */
@Component
public class EngineN1Rule implements FaultRule {

    private static final double N1_MIN = 15.0;
    private static final double N1_MAX = 105.0;

    @Override
    public FaultRuleResult evaluate(FlightDataMessage message) {
        FaultRuleResult r = checkN1(message, "engine_n1_1", "发动机1");
        if (r != null) return r;
        return checkN1(message, "engine_n1_2", "发动机2");
    }

    private FaultRuleResult checkN1(FlightDataMessage message, String metric, String component) {
        Object raw = message.metrics().get(metric);
        if (raw == null) return null;
        double value = toDouble(raw);
        if (value < N1_MIN || value > N1_MAX) {
            String severity = value > N1_MAX ? "critical" : "major";
            return new FaultRuleResult("ATA71-001", severity, component,
                    component + " N1转速异常: " + value + "%");
        }
        return null;
    }

    private double toDouble(Object val) {
        return val instanceof Number n ? n.doubleValue() : Double.parseDouble(val.toString());
    }
}
