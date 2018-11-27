package edu.stlawu.ghostbusters;

import android.location.Location;
import java.util.ArrayList;
import java.util.Random;

public class GhostManager {
    // this is where we control the level of ghosts and where they are generated

    private Boolean newRandom;

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
        // generate first random location
        Location ghostLocation = randomLocation();

        // initialize to false
        newRandom = false;

        // check for first randomly generated location
        for(int i = 0; i < ghostList.size(); i++){
            int distance = (int) ghostLocation.distanceTo(ghostList.get(i));

            if(distance < 45){
                // make it go through the while loop to generate a new random location
                newRandom = true;
            }
        }

        // make sure it does not overlap with another ghost
        while(newRandom){
            newRandom = false;
            ghostLocation = randomLocation();
            
            for(int i = 0; i < ghostList.size(); i++){
                int distance = (int) ghostLocation.distanceTo(ghostList.get(i));

                if(distance < 45){
                    ghostLocation = randomLocation();
                    newRandom = true;
                }
            }
        }

        // add ghost to list once it is not overlapping
        ghostList.add(ghostLocation);
    }

    // generate a random location
    private Location randomLocation(){
        Random r = new Random();
        double ghostLat = minLat + (maxLat - minLat) * r.nextDouble();
        double ghostLon = minLon + (maxLon - minLon) * r.nextDouble();

        Location ghostLocation = new Location("labghost");
        ghostLocation.setLatitude(ghostLat);
        ghostLocation.setLongitude(ghostLon);

        return ghostLocation;
    }
}
