package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.waitingroom.PatientQueueInfo;
import ar.edu.itba.pod.grpc.server.exceptions.InvalidLevelException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientDoesNotExistException;
import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.util.*;

public class PatientRepository {

    private static final Comparator<Patient> COMPARATOR = Comparator
            .comparingLong((Patient p) -> p.getArrivalTime().getSeconds())
            .thenComparingInt(p -> p.getArrivalTime().getNanos())
            .thenComparing(Patient::getName);

    private final SortedMap<Integer, Queue<Patient>> patients = new TreeMap<>(Comparator.reverseOrder());
    private final Set<String> historicPatients = new HashSet<>();

    public Patient addPatient(String name, int level) {
        if (level < 1 || level > 5)
            throw new InvalidLevelException();

        synchronized (historicPatients) {
            if (historicPatients.contains(name))
                throw new PatientAlreadyExistsException();
            historicPatients.add(name);
        }

        Instant instant = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
        Patient patient = Patient.newBuilder()
                .setName(name)
                .setLevel(level)
                .setArrivalTime(timestamp)
                .build();

        synchronized (patients) {
            patients.computeIfAbsent(level, k -> new PriorityQueue<>(COMPARATOR)).add(patient);
        }
        return patient;
    }

    public List<Patient> getPatients() {
        List<Patient> allPatients = new ArrayList<>();
        synchronized (patients) {
            for (Queue<Patient> patientQueue : patients.values())
                allPatients.addAll(patientQueue);
            return allPatients;
        }
    }

    public List<Patient> getFirstPatientFromEveryLevel() {
        List<Patient> firstPatients = new ArrayList<>();
        synchronized (patients) {
            for (Queue<Patient> patientQueue : patients.values()) {
                if (!patientQueue.isEmpty())
                    firstPatients.add(patientQueue.stream().findFirst().get());
            }
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

        Patient.Builder patientBuilder = Patient.newBuilder().setName(name).setLevel(level);
        synchronized (patients) {
            for (Queue<Patient> patientQueue : patients.values()) {
                for (Patient p : patientQueue) {
                    if (p.getName().equals(name)) {
                        Patient patient = patientBuilder.setArrivalTime(p.getArrivalTime()).build();
                        patientQueue.remove(p);
                        patients.computeIfAbsent(level, k -> new PriorityQueue<>(COMPARATOR)).add(patient);
                        return patient;
                    }
                }
            }
        }
        throw new PatientDoesNotExistException();
    }

    public PatientQueueInfo checkPatient(String name) {
        int count = 0;

        synchronized (patients) {
            for (Queue<Patient> queue : patients.values()) {
                SortedSet<Patient> sortedQueue = new TreeSet<>(COMPARATOR);
                sortedQueue.addAll(queue);

                for (Patient patient : sortedQueue) {
                    if (patient.getName().equals(name))
                        return PatientQueueInfo.newBuilder().setPatient(patient).setQueueLength(count).build();
                    count++;
                }
            }
        }

        throw new PatientDoesNotExistException();
    }

    public boolean patientExists(String name) {
        synchronized (patients) {
            for (Queue<Patient> queue : patients.values()) {
                for (Patient patient : queue) {
                    if (patient.getName().equals(name))
                        return true;
                }
            }
        }
        return false;
    }

}
