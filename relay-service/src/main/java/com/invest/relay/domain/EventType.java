package com.invest.relay.domain;

import lombok.Getter;

@Getter
public enum EventType {

    PLAN_CREATED("PlanCreated", "plan-events"),
    EXECUTE_PLAN("ExecutePlan", "plan-commands"),
    CASH_BALANCE_UPDATED("CashBalanceUpdated", "cash-events"),
    ORDER_FILLED("OrderFilled", "order-filled"),
    UNKNOWN("Unknown", "events");

    private final String value;
    private final String topic;

    EventType(String value, String topic) {
        this.value = value;
        this.topic = topic;
    }

    public static EventType fromValue(String value) {
        for (EventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
