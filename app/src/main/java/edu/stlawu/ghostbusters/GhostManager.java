package edu.stlawu.ghostbusters;

import android.location.Location;
import java.util.ArrayList;
import java.util.Random;

public class GhostManager {
    // this is where we control the level of ghosts and where they are generated

    // Number of ghosts to generate
    private int ghosts;

    // Keep track of ghost locations in an array
    private ArrayList<Location> ghostList = new ArrayList<>();

    // Points of SLU Polygon from Google Maps
    private double minLat = 44.5789;
    private double maxLat = 44.5987;
    private double minLon = -75.1741;
    private double maxLon = -75.1494;

    public GhostManager(int numberOfGhost){

        this.ghosts = numberOfGhost;

        // Generate ghosts at random locations
        for (int i = 0; i < ghosts; i++) {
            // Get a random coordinate (lat & lon) for the ghost
            addGhost();
        }
    }

    public ArrayList<Location> getGhostList() {
        return ghostList;
    }

    // method to add new ghost
    public void addGhost(){
        Random r = new Random();
        double ghostLat = minLat + (maxLat - minLat) * r.nextDouble();
        double ghostLon = minLon + (maxLon - minLon) * r.nextDouble();

        Location ghostLocation = new Location("labghost");
        ghostLocation.setLatitude(ghostLat);
        ghostLocation.setLongitude(ghostLon);

        ghostList.add(ghostLocation);
    }
}
