package com.example.rik.rikvantoorn_pset6;

/**
 * Created by Rik on 9-12-2016.
 */

public class ArtistEvents {
    private String title;
    private String date;
    private String venue;

    public ArtistEvents(String title, String date, String venue) {
        this.title = title;
        this.date = date;
        this.venue = venue;
    }

    public String gettitle() {
        return title;
    }

    public String getdate() {
        return date;
    }

    public String getvenue() {
        return venue;
    }
}
