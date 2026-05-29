package com.mro.aircraft.engine;

/**
 * Result of a FaultRule evaluation when a fault is detected.
 */
public record FaultRuleResult(
        String faultCode,
        /** critical / major / minor */
        String severity,
        String component,
        String description
) {}
