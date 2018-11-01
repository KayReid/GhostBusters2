package edu.stlawu.ghostbusters;

import android.location.Location;

public class Ghost {

    private Location location;

    // constructor
    public Ghost(Location ghostLocation){
        this.location = ghostLocation;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
