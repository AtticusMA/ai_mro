package com.mro.aircraft.parser;

import java.time.Instant;
import java.util.Map;

/**
 * Parsed flight data message, format-agnostic output from any parser.
 */
public record FlightDataMessage(
        String aircraftId,
        /** ACMS / QAR / FDR */
        String dataSource,
        Instant collectedAt,
        /** Decoded metric key-value pairs */
        Map<String, Object> metrics,
        /** Original raw payload for archiving */
        String rawPayload
) {}
