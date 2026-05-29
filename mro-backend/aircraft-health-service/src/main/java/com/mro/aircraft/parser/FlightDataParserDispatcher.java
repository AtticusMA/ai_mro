package com.mro.aircraft.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Dispatches raw MQTT messages to the appropriate FlightDataParser implementation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlightDataParserDispatcher {

    private final List<FlightDataParser> parsers;

    public FlightDataMessage dispatch(String topic, String payload) {
        for (FlightDataParser parser : parsers) {
            if (parser.supports(topic, payload)) {
                log.debug("Dispatching topic={} to parser={}", topic, parser.getClass().getSimpleName());
                return parser.parse(topic, payload);
            }
        }
        throw new IllegalArgumentException("No parser found for topic: " + topic);
    }
}
