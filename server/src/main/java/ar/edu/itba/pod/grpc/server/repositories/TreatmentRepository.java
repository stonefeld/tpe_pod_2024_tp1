package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Treatment;

import java.util.*;

public class TreatmentRepository {

    private final SortedSet<Treatment> currentTreatments = new TreeSet<>(Comparator.comparingInt(t -> t.getRoom().getNumber()));
    private final List<Treatment> completedTreatments = new ArrayList<>();

    public Treatment addTreatment(Treatment treatment) {
        synchronized (currentTreatments) {
            currentTreatments.add(treatment);
        }
        return treatment;
    }

    public Treatment completeTreatment(Treatment treatment) {
        synchronized (currentTreatments) {
            currentTreatments.removeIf(t -> t.getRoom().getNumber() == treatment.getRoom().getNumber());
        }
        synchronized (completedTreatments) {
            completedTreatments.add(treatment);
        }
        return treatment;
    }

    public List<Treatment> getCurrentTreatments() {
        return List.copyOf(currentTreatments);
    }

    public List<Treatment> getCompletedTreatments() {
        return List.copyOf(completedTreatments);
    }

}
