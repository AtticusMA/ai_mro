package com.mro.aircraft.parser;

/**
 * Strategy interface for ACMS/QAR/FDR payload parsers.
 */
public interface FlightDataParser {

    /**
     * Returns true if this parser can handle the given raw payload.
     */
    boolean supports(String topic, String payload);

    /**
     * Parse the raw payload into a structured FlightDataMessage.
     */
    FlightDataMessage parse(String topic, String payload);
}
