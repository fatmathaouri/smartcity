package tn.esprit.spring.smartcity.map;

import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public boolean isWithinRadius(double lat1, double lng1, double lat2, double lng2, double radiusKm) {
        return calculateDistance(lat1, lng1, lat2, lng2) <= radiusKm;
    }
}
