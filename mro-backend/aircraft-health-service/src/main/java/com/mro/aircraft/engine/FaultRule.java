package com.mro.aircraft.engine;

import com.mro.aircraft.parser.FlightDataMessage;

/**
 * Strategy interface for fault detection rules.
 */
public interface FaultRule {

    /**
     * Returns a fault code if this rule triggers on the message, otherwise null.
     */
    FaultRuleResult evaluate(FlightDataMessage message);
}
