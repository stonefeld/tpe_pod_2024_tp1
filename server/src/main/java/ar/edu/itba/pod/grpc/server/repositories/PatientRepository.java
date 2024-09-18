package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientRepository {
    private final List<Patient> patients = new ArrayList<>();

    public Patient addPatient(String name, int level) {
        Patient patient;
        patient = Patient.newBuilder().setName(name).setLevel(level).build();
        synchronized (patients) {
            patients.add(patient);
        }
        return patient;
    }
}
