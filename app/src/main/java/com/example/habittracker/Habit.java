package com.example.habittracker;

public class Habit {
    private String id;
    private String name;

    public Habit() {
    }

    public Habit(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

