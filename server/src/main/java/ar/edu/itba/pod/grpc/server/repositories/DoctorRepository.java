package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Availability;
import ar.edu.itba.pod.grpc.hospital.Doctor;

import java.util.ArrayList;
import java.util.List;

public class DoctorRepository {
    private final List<Doctor> doctors = new ArrayList<>();

    public Doctor addDoctor(String name, int level) {
        Doctor doctor;
        doctor = Doctor.newBuilder().setName(name).setLevel(level).setAvailability(Availability.AVAILABLE).build();
        synchronized (doctors) {
            doctors.add(doctor);
        }
        return doctor;
    }

    // TODO: REVISE THIS BULLSHIT
    public Doctor setDoctorAvailability(String doctorName, Availability availability) {
        synchronized (doctors) {
            for (int i = 0; i < doctors.size(); i++) {
                Doctor d = doctors.get(i);
                if (d.getName().equals(doctorName)) {
                    Doctor updatedDoctor = d.toBuilder().setAvailability(availability).build();
                    doctors.set(i, updatedDoctor);
                    return updatedDoctor;
                }
            }
        }
        return null;
    }

    public Doctor checkDoctor(String doctorName) {
        for (Doctor d : doctors) {
            if (d.getName().equals(doctorName)) {
                return d;
            }
        }
        return null;
    }
}
