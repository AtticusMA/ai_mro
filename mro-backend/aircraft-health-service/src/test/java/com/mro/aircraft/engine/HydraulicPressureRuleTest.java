package com.mro.aircraft.engine;

import com.mro.aircraft.parser.FlightDataMessage;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class HydraulicPressureRuleTest {

    private final HydraulicPressureRule rule = new HydraulicPressureRule();

    @Test
    void noMetric_returnsNull() {
        assertThat(rule.evaluate(msg(Map.of()))).isNull();
    }

    @Test
    void normalPressure_returnsNull() {
        assertThat(rule.evaluate(msg(Map.of("hydraulic_pressure_a", 3100.0)))).isNull();
    }

    @Test
    void lowPressure_returnsMajor() {
        FaultRuleResult r = rule.evaluate(msg(Map.of("hydraulic_pressure_a", 2400.0)));
        assertThat(r).isNotNull();
        assertThat(r.severity()).isEqualTo("major");
        assertThat(r.faultCode()).isEqualTo("ATA27-001");
    }

    @Test
    void criticalPressure_returnsCritical() {
        FaultRuleResult r = rule.evaluate(msg(Map.of("hydraulic_pressure_a", 1800.0)));
        assertThat(r).isNotNull();
        assertThat(r.severity()).isEqualTo("critical");
    }

    private FlightDataMessage msg(Map<String, Object> metrics) {
        return new FlightDataMessage("B-1234", "ACMS", Instant.now(), metrics, "{}");
    }
}
