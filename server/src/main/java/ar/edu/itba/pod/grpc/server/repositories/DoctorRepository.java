package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Availability;
import ar.edu.itba.pod.grpc.hospital.Doctor;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorDoesNotExistException;
import ar.edu.itba.pod.grpc.server.exceptions.InvalidLevelException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorRepository {

    private final Map<String, Doctor> doctors = new HashMap<>();

    public Doctor addDoctor(String name, int level) {
        if (level < 1 || level > 5)
            throw new InvalidLevelException();
        if (doctors.containsKey(name))
            throw new DoctorAlreadyExistsException();

        Doctor doctor = Doctor.newBuilder()
                .setName(name)
                .setLevel(level)
                .setAvailability(Availability.AVAILABILITY_AVAILABLE)
                .build();

        synchronized (doctors) {
            doctors.put(name, doctor);
        }

        return doctor;
    }

    public Doctor setDoctorAvailability(String doctorName, Availability availability) {
        if (!doctors.containsKey(doctorName))
            throw new DoctorDoesNotExistException();

        Doctor.Builder doctorBuilder = Doctor.newBuilder().setName(doctorName).setAvailability(availability);
        Doctor doctor;

        synchronized (doctors) {
            doctor = doctorBuilder.setLevel(doctors.get(doctorName).getLevel()).build();
            doctors.put(doctorName, doctor);
        }

        return doctor;
    }

    public Doctor checkDoctor(String doctorName) {
        synchronized (doctors) {
            if (doctors.containsKey(doctorName))
                return doctors.get(doctorName);
        }
        throw new DoctorDoesNotExistException();
    }

    public List<Doctor> getAvailableDoctors() {
        List<Doctor> availableDoctors;
        synchronized (doctors) {
            availableDoctors = doctors.values().stream()
                    .filter(d -> d.getAvailability().equals(Availability.AVAILABILITY_AVAILABLE))
                    .sorted(Comparator.comparingInt(Doctor::getLevel).thenComparing(Doctor::getName))
                    .toList();
        }
        return availableDoctors;
    }

    public boolean doctorExists(String doctorName) {
        synchronized (doctors) {
            return doctors.containsKey(doctorName);
        }
    }

}
