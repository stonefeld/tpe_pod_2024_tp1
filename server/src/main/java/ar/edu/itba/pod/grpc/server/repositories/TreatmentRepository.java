package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.Treatment;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorDoesNotExistException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientDoesNotExistException;
import ar.edu.itba.pod.grpc.server.exceptions.RoomDoesNotExistException;

import java.util.*;

public class TreatmentRepository {

    private final SortedSet<Treatment> currentTreatments = new TreeSet<>(Comparator.comparingInt(t -> t.getRoom().getNumber()));
    private final List<Treatment> completedTreatments = new ArrayList<>();

    public void addTreatment(Treatment treatment) {
        synchronized (currentTreatments) {
            currentTreatments.add(treatment);
        }
    }

    public Treatment dischargePatient(int roomNumber, String patientName, String doctorName) {
        Treatment treatment;
        synchronized (currentTreatments) {
            treatment = currentTreatments.stream()
                    .filter(t -> t.getDoctor().getName().equals(doctorName))
                    .findFirst()
                    .orElseThrow(DoctorDoesNotExistException::new);
        }

        if (treatment.hasPatient() && treatment.getPatient().getName().equals(patientName)) {
            if (treatment.getRoom().getNumber() == roomNumber) {
                synchronized (currentTreatments) {
                    currentTreatments.remove(treatment);
                }
                synchronized (completedTreatments) {
                    completedTreatments.add(treatment);
                }
                return treatment;
            } else {
                throw new RoomDoesNotExistException();
            }
        } else {
            throw new PatientDoesNotExistException();
        }
    }

    public List<Treatment> getTreatmentsByRoom(List<Room> rooms) {
        List<Treatment> treatments = new ArrayList<>();
        synchronized (currentTreatments) {
            for (Room room : rooms) {
                treatments.add(currentTreatments.stream()
                        .filter(t -> t.getRoom().getNumber() == room.getNumber())
                        .findFirst()
                        .orElse(Treatment.newBuilder().setRoom(room).build()));
            }
        }
        return treatments;
    }

    public List<Treatment> getCurrentTreatments() {
        synchronized (currentTreatments) {
            return List.copyOf(currentTreatments);
        }
    }

    public List<Treatment> getCompletedTreatments() {
        return List.copyOf(completedTreatments);
    }

}
