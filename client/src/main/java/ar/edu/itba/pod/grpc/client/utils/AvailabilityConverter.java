package ar.edu.itba.pod.grpc.client.utils;

import ar.edu.itba.pod.grpc.hospital.Availability;

public class AvailabilityConverter {

    public static Availability strToAvailability(String availability) {
        return switch (availability.toUpperCase()) {
            case "AVAILABLE" -> Availability.AVAILABILITY_AVAILABLE;
            case "UNAVAILABLE" -> Availability.AVAILABILITY_UNAVAILABLE;
            case "ATTENDING" -> Availability.AVAILABILITY_ATTENDING;
            default -> Availability.AVAILABILITY_UNSPECIFIED;
        };
    }

    public static String availabilityToStr(Availability availability) {
        return switch (availability) {
            case AVAILABILITY_AVAILABLE -> "Available";
            case AVAILABILITY_UNAVAILABLE -> "Unavailable";
            case AVAILABILITY_ATTENDING -> "Attending";
            default -> "Unspecified";
        };
    }

}
