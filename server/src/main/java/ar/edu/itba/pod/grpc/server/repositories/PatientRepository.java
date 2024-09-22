package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.waitingroom.PatientQueueInfo;
import ar.edu.itba.pod.grpc.server.exceptions.InvalidLevelException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientDoesNotExistException;


import java.util.*;

public class PatientRepository {

    private final SortedMap<Integer, Queue<Patient>> patients = new TreeMap<>(Comparator.reverseOrder());
    private final Set<String> historicPatients = new HashSet<>();

    public Patient addPatient(String name, int level) {
        if (historicPatients.contains(name))
            throw new PatientAlreadyExistsException();
        if (level < 1 || level > 5)
            throw new InvalidLevelException();

        historicPatients.add(name);
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
        if (level < 1 || level > 5)
            throw new InvalidLevelException();

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
        throw new PatientDoesNotExistException();
    }

    public PatientQueueInfo checkPatient(String name) {
        synchronized (patients) {
            int count = 0;
            for (Map.Entry<Integer, Queue<Patient>> entry : patients.entrySet()) {
                Queue<Patient> queue = entry.getValue();
                for (Patient patient : queue) {
                    if (patient.getName().equals(name))
                        return PatientQueueInfo.newBuilder().setPatient(patient).setQueueLength(count).build();
                    count++;
                }
            }
        }
        throw new PatientDoesNotExistException();
    }
}
