package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Availability;
import ar.edu.itba.pod.grpc.hospital.Doctor;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorDoesNotExistException;
import ar.edu.itba.pod.grpc.server.exceptions.InvalidLevelException;

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class DoctorRepository {

    private final SortedSet<Doctor> doctors = new TreeSet<>(Comparator.comparingInt(Doctor::getLevel).thenComparing(Doctor::getName));

    public Doctor addDoctor(String name, int level) {
        if (doctorExists(name))
            throw new DoctorAlreadyExistsException();
        if (level < 1 || level > 5)
            throw new InvalidLevelException();

        Doctor doctor = Doctor.newBuilder().setName(name).setLevel(level).setAvailability(Availability.AVAILABILITY_AVAILABLE).build();
        synchronized (doctors) {
            doctors.add(doctor);
        }
        return doctor;
    }

    // TODO: REVISE THIS BULLSHIT
    public Doctor setDoctorAvailability(String doctorName, Availability availability) {
        synchronized (doctors) {
            Doctor.Builder doctorBuilder = Doctor.newBuilder().setName(doctorName).setAvailability(availability);
            for (Doctor d : doctors) {
                if (d.getName().equals(doctorName)) {
                    Doctor doctor = doctorBuilder.setLevel(d.getLevel()).build();
                    doctors.remove(d);
                    doctors.add(doctor);
                    return doctor;
                }
            }
        }
        throw new DoctorDoesNotExistException();
    }

    public Doctor checkDoctor(String doctorName) {
        for (Doctor d : doctors) {
            if (d.getName().equals(doctorName))
                return d;
        }
        throw new DoctorDoesNotExistException();
    }

    public List<Doctor> getAvailableDoctors() {
        synchronized (doctors) {
            return doctors.stream().filter(d -> d.getAvailability().equals(Availability.AVAILABILITY_AVAILABLE)).toList();
        }
    }

    private boolean doctorExists(String doctorName) {
        for (Doctor d : doctors) {
            if (d.getName().equals(doctorName))
                return true;
        }
        return false;
    }

}
