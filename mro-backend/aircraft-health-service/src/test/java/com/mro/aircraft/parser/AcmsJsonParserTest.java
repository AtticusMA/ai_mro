package com.mro.aircraft.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class AcmsJsonParserTest {

    private AcmsJsonParser parser;

    @BeforeEach
    void setUp() {
        parser = new AcmsJsonParser();
    }

    @Test
    void supports_dataTopicWithJson_returnsTrue() {
        assertThat(parser.supports("aircraft/B-1234/data", "{}")).isTrue();
    }

    @Test
    void supports_qarTopic_returnsFalse() {
        assertThat(parser.supports("aircraft/B-1234/qar", "{}")).isFalse();
    }

    @Test
    void supports_nonJson_returnsFalse() {
        assertThat(parser.supports("aircraft/B-1234/data", "not-json")).isFalse();
    }

    @Test
    void parse_fullPayload_extractsAllFields() {
        String payload = """
                {
                  "aircraft_id": "B-1234",
                  "ts": 1748304000000,
                  "metrics": {
                    "hydraulic_pressure_a": 3000.0,
                    "engine_n1_1": 85.2
                  }
                }
                """;
        FlightDataMessage msg = parser.parse("aircraft/B-1234/data", payload);

        assertThat(msg.aircraftId()).isEqualTo("B-1234");
        assertThat(msg.dataSource()).isEqualTo("ACMS");
        assertThat(msg.collectedAt().toEpochMilli()).isEqualTo(1748304000000L);
        assertThat(msg.metrics()).containsEntry("hydraulic_pressure_a", 3000.0);
        assertThat(msg.metrics()).containsEntry("engine_n1_1", 85.2);
    }

    @Test
    void parse_aircraftIdFallbackFromTopic_extractsFromTopic() {
        String payload = """
                {
                  "ts": 1748304000000,
                  "metrics": { "fuel_qty": 5000 }
                }
                """;
        FlightDataMessage msg = parser.parse("aircraft/B-5678/data", payload);
        assertThat(msg.aircraftId()).isEqualTo("B-5678");
    }

    @Test
    void parse_flatPayload_treatsAllAsMetrics() {
        String payload = """
                {
                  "aircraft_id": "B-1234",
                  "ts": 1748304000000,
                  "hydraulic_pressure_a": 2800.0
                }
                """;
        FlightDataMessage msg = parser.parse("aircraft/B-1234/data", payload);
        assertThat(msg.metrics()).containsKey("hydraulic_pressure_a");
        assertThat(msg.metrics()).doesNotContainKey("aircraft_id");
        assertThat(msg.metrics()).doesNotContainKey("ts");
    }

    @Test
    void parse_rawPayloadPreserved() {
        String payload = "{\"aircraft_id\":\"B-1234\",\"ts\":1748304000000,\"metrics\":{}}";
        FlightDataMessage msg = parser.parse("aircraft/B-1234/data", payload);
        assertThat(msg.rawPayload()).isEqualTo(payload);
    }
}
