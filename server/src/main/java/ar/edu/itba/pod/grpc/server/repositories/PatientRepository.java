package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.waitingroom.PatientQueueInfo;


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

    public Patient updateLevel(String name, int level) {
        Patient patient = Patient.newBuilder().setName(name).setLevel(level).build();
        synchronized (patients) {
            for (Queue<Patient> patientQueue : patients.values()) {
                for (Patient p : patientQueue) {
                    if (p.getName().equals(name)) {
                        patientQueue.remove(p);
                        patients.computeIfAbsent(level, k -> new LinkedList<>()).add(patient);
                        return patient;
                    }
                }
            }
        }
        return null;
    }

    public PatientQueueInfo checkPatient(String name) {

        synchronized (patients) {
            int count = 0;


            for (Map.Entry<Integer, Queue<Patient>> entry : patients.entrySet()) {
                Queue<Patient> queue = entry.getValue();

                for (Patient patient : queue) {
                    if (patient.getName().equals(name)) {

                        return PatientQueueInfo.newBuilder().setPatient(patient).setQueueLength(count).build();
                    }
                    count++;
                }
            }
        }
        return null;
    }
}
