package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Patient;

import java.util.*;

public class PatientRepository {

    private final SortedMap<Integer, Queue<Patient>> patients = new TreeMap<>(Comparator.reverseOrder());

    public Patient addPatient(String name, int level) {
        Patient patient = Patient.newBuilder().setName(name).setLevel(level).build();
        synchronized (patients) {
            patients.computeIfAbsent(level, k -> new LinkedList<>()).add(patient);
        }
        return patient;
    }

    public Patient getNextPatient() {
        for (Map.Entry<Integer, Queue<Patient>> entry : patients.entrySet()) {
            if (!entry.getValue().isEmpty())
                return entry.getValue().poll();
        }
        return null;
    }

    public List<Patient> getPatients() {
        List<Patient> allPatients = new ArrayList<>();
        for (Queue<Patient> patientQueue : patients.values())
            allPatients.addAll(patientQueue);
        return allPatients;
    }

    public List<Patient> getFirstPatientFromEveryLevel() {
        List<Patient> firstPatients = new ArrayList<>();
        for (Queue<Patient> patientQueue : patients.values()) {
            if (!patientQueue.isEmpty())
                firstPatients.add(patientQueue.peek());
        }
        return firstPatients;
    }

    public Patient attendPatient(Patient patient) {
        synchronized (patients) {
            patients.get(patient.getLevel()).removeIf(p -> patient.getName().equals(p.getName()));
        }
        return patient;
    }

}
