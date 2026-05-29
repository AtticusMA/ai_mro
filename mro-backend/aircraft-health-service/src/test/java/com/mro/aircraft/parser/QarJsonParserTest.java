package com.mro.aircraft.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class QarJsonParserTest {

    private QarJsonParser parser;

    @BeforeEach
    void setUp() {
        parser = new QarJsonParser();
    }

    @Test
    void supports_qarTopicWithJson_returnsTrue() {
        assertThat(parser.supports("aircraft/B-1234/qar", "{}")).isTrue();
    }

    @Test
    void supports_dataTopic_returnsFalse() {
        assertThat(parser.supports("aircraft/B-1234/data", "{}")).isFalse();
    }

    @Test
    void parse_fullPayload_extractsParametersWithPrefix() {
        String payload = """
                {
                  "aircraft_id": "B-1234",
                  "flight_no": "CA1234",
                  "ts": 1748304000000,
                  "parameters": {
                    "altitude": 35000,
                    "speed": 450,
                    "fuel_qty": 12000
                  }
                }
                """;
        FlightDataMessage msg = parser.parse("aircraft/B-1234/qar", payload);

        assertThat(msg.aircraftId()).isEqualTo("B-1234");
        assertThat(msg.dataSource()).isEqualTo("QAR");
        assertThat(msg.collectedAt().toEpochMilli()).isEqualTo(1748304000000L);
        // QAR parameters are prefixed with "qar."
        assertThat(msg.metrics()).containsKey("qar.altitude");
        assertThat(msg.metrics()).containsKey("qar.speed");
        assertThat(msg.metrics()).containsKey("qar.fuel_qty");
    }

    @Test
    void parse_aircraftIdFallbackFromTopic() {
        String payload = "{\"ts\":1748304000000,\"parameters\":{\"altitude\":10000}}";
        FlightDataMessage msg = parser.parse("aircraft/B-9999/qar", payload);
        assertThat(msg.aircraftId()).isEqualTo("B-9999");
    }

    @Test
    void parse_reservedFieldsExcludedFromMetrics() {
        String payload = """
                {
                  "aircraft_id": "B-1234",
                  "flight_no": "CA1234",
                  "ts": 1748304000000,
                  "parameters": { "speed": 400 }
                }
                """;
        FlightDataMessage msg = parser.parse("aircraft/B-1234/qar", payload);
        assertThat(msg.metrics()).doesNotContainKey("qar.aircraft_id");
        assertThat(msg.metrics()).doesNotContainKey("qar.flight_no");
        assertThat(msg.metrics()).doesNotContainKey("qar.ts");
    }

    @Test
    void parse_rawPayloadPreserved() {
        String payload = "{\"aircraft_id\":\"B-1234\",\"ts\":1748304000000,\"parameters\":{\"speed\":400}}";
        FlightDataMessage msg = parser.parse("aircraft/B-1234/qar", payload);
        assertThat(msg.rawPayload()).isEqualTo(payload);
    }
}
