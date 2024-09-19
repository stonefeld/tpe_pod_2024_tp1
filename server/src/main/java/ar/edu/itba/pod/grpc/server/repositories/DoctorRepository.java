package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Availability;
import ar.edu.itba.pod.grpc.hospital.Doctor;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class DoctorRepository {

    private final SortedSet<Doctor> doctors = new TreeSet<>(Comparator.comparingInt(Doctor::getLevel).thenComparing(Doctor::getName));

    public Doctor addDoctor(String name, int level) {
        Doctor doctor = Doctor.newBuilder().setName(name).setLevel(level).setAvailability(Availability.AVAILABLE).build();
        synchronized (doctors) {
            doctors.add(doctor);
        }
        return doctor;
    }

    // TODO: REVISE THIS BULLSHIT
    public Doctor setDoctorAvailability(String doctorName, Availability availability) {
        Doctor doctor = Doctor.newBuilder().setName(doctorName).setAvailability(availability).build();
        synchronized (doctors) {
            if (doctors.removeIf(d -> d.getName().equals(doctorName))) {
                doctors.add(doctor);
                return doctor;
            }
        }
        return null;
    }

    public Doctor checkDoctor(String doctorName) {
        for (Doctor d : doctors) {
            if (d.getName().equals(doctorName))
                return d;
        }
        return null;
    }

    public Doctor getDoctorForLevel(int level) {
        for (Doctor d : doctors) {
            if (d.getAvailability().equals(Availability.AVAILABLE) && d.getLevel() >= level)
                return d;
        }
        return null;
    }

}
