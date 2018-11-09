package edu.stlawu.ghostbusters;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap SLUMap;
    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_PURPLE_ARGB = 0xff81C784;
    private static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_BLUE_ARGB = 0xffF9A825;

    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);

    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
    private static final List<PatternItem> PATTERN_POLYGON_BETA =
            Arrays.asList(DOT, GAP, DASH, GAP);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this case, we just add a marker in the middle of the SLU campus.
     * We also added a polygon around campus to create the boundaries for the game.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        SLUMap = googleMap;

        // Add a marker in St. Lawrence University and move the camera
        LatLng stlawrence = new LatLng(44.5892, -75.1609);
        SLUMap.addMarker(new MarkerOptions().position(stlawrence).title("Marker in St. Lawrence University"));
        SLUMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.5892, -75.1609), 15));

        // Add polygons to indicate areas on the map.
        // Points: Price Chopper, Leberge & Curtis, Canton Recreation Office, Family Court
        Polygon SLUpolygon = googleMap.addPolygon(new PolygonOptions()
                .clickable(true)
                .add(
                        new LatLng(44.5981, -75.1494),
                        new LatLng(44.5789, -75.1510),
                        new LatLng(44.5850, -75.1741),
                        new LatLng(44.5987, -75.1696)));
        // Store a data object with the polygon, used here to indicate an arbitrary type.
        SLUpolygon.setTag("alpha");
        // Style the polygon.
        stylePolygon(SLUpolygon);

        // Points of SLUpolygon
        double minLat = 44.5789;
        double maxLat = 44.5987;
        double minLon = -75.1741;
        double maxLon = -75.1494;

        // Generate ghosts at random locations
        for(int i = 0; i < 100; i++) {
            // Get a random coordinate (lat & lon) for the ghost
            Random r = new Random();
            double ghostLat = minLat + (maxLat - minLat) * r.nextDouble();
            double ghostLon = minLon + (maxLon - minLon) * r.nextDouble();
            LatLng ghost = new LatLng(ghostLat, ghostLon);

            // TODO: Check if ghost is within the bounds of the polygon
            //if(SLUpolygon.Contains(ghost))
            // Add ghost onto map
            SLUMap.addMarker(new MarkerOptions().position(ghost));

            // Get coordinates
            System.out.println(ghost);
            //System.out.println(ghostLon);


        }
    }


    /**
     * Styles the polygon, based on type.
     * @param polygon The polygon object that needs styling.
     */
    private void stylePolygon(Polygon polygon) {
        String type = "";
        // Get the data object stored with the polygon.
        if (polygon.getTag() != null) {
            type = polygon.getTag().toString();
        }

        List<PatternItem> pattern = null;
        int strokeColor = COLOR_BLACK_ARGB;
        int fillColor = COLOR_WHITE_ARGB;

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "alpha":
                // Apply a stroke pattern to render a dashed line, and define colors.
                pattern = PATTERN_POLYGON_ALPHA;
                strokeColor = COLOR_GREEN_ARGB;
                fillColor = COLOR_PURPLE_ARGB;
                break;
            case "beta":
                // Apply a stroke pattern to render a line of dots and dashes, and define colors.
                pattern = PATTERN_POLYGON_BETA;
                strokeColor = COLOR_ORANGE_ARGB;
                fillColor = COLOR_BLUE_ARGB;
                break;
        }

        polygon.setStrokePattern(pattern);
        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        polygon.setStrokeColor(strokeColor);
        polygon.setFillColor(fillColor);
    }
}
