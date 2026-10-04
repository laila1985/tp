package com.example.ledger.domain;

public enum Day {
    DAY_1(1),
    DAY_2(2),
    DAY_3(3),
    DAY_4(4),
    DAY_5(5),
    DAY_6(6);

    private final int number;

    Day(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static Day of(int number) {
        for (Day day : values()) {
            if (day.number == number) {
                return day;
            }
        }
        throw new IllegalArgumentException("Unknown day number: " + number);
    }
}
