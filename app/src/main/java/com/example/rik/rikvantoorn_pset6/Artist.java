package com.example.rik.rikvantoorn_pset6;

/**
 * Created by Rik on 9-12-2016.
 */

public class Artist {
    public String name;
    public String events;

    public Artist(String name, String events) {
        this.name = name;
        this.events = events;
    }

    public String getname() {
        return name;
    }

    public String getevents() {
        return events;
    }
}
