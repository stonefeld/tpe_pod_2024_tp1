package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Treatment;

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
                    .filter(t -> t.getRoom().getNumber() == roomNumber)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        }

        if (treatment.hasPatient() && treatment.getPatient().getName().equals(patientName)) {
            if (treatment.hasDoctor() && treatment.getDoctor().getName().equals(doctorName)) {
                synchronized (currentTreatments) {
                    currentTreatments.remove(treatment);
                }
                synchronized (completedTreatments) {
                    completedTreatments.add(treatment);
                }
                return treatment;
            } else {
                throw new IllegalArgumentException("Doctor not found");
            }
        } else {
            throw new IllegalArgumentException("Patient not found");
        }
    }

    public List<Treatment> getCurrentTreatments() {
        return List.copyOf(currentTreatments);
    }

    public List<Treatment> getCompletedTreatments() {
        return List.copyOf(completedTreatments);
    }

}
