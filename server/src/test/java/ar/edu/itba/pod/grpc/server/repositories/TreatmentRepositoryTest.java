package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Doctor;
import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.Treatment;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorDoesNotExistException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientDoesNotExistException;
import ar.edu.itba.pod.grpc.server.exceptions.RoomDoesNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class TreatmentRepositoryTest {

    private static TreatmentRepository treatmentRepository;

    private static final int THREAD_COUNT = 1000;
    private static final int TREATMENTS_BY_THREAD = 1000;
    private static final int EXPECTED_TREATMENTS = THREAD_COUNT * TREATMENTS_BY_THREAD;

    @BeforeEach
    public void setUp() {
        treatmentRepository = new TreatmentRepository();
    }

    @Test
    void addTreatmentTest() {
        Room room1 = Room.newBuilder().setNumber(1).build();
        Room room2 = Room.newBuilder().setNumber(2).build();
        assertDoesNotThrow(() -> treatmentRepository.addTreatment(Treatment.newBuilder().setRoom(room1).build()));
        assertDoesNotThrow(() -> treatmentRepository.addTreatment(Treatment.newBuilder().setRoom(room2).build()));
        List<Room> rooms = new ArrayList<>();
        rooms.add(room1);
        rooms.add(room2);
        assertEquals(2, treatmentRepository.getTreatmentsByRoom(rooms).size());
    }

    @Test
    void dischargePatientTest() {
        Room room = Room.newBuilder().setNumber(1).build();
        Room room2 = Room.newBuilder().setNumber(2).build();
        Doctor doctor = Doctor.newBuilder().setName("Martin").build();
        Patient patient = Patient.newBuilder().setName("Lucas").build();
        Doctor doctor2 = Doctor.newBuilder().setName("Joselu").build();
        Patient patient2 = Patient.newBuilder().setName("Martin").build();
        List<Room> rooms = new ArrayList<>();
        rooms.add(room);
        rooms.add(room2);
        Treatment treatment = Treatment.newBuilder().setRoom(room).setDoctor(doctor).setPatient(patient).build();
        Treatment treatment2 = Treatment.newBuilder().setRoom(room2).setDoctor(doctor2).setPatient(patient2).build();
        treatmentRepository.addTreatment(treatment);
        treatmentRepository.addTreatment(treatment2);
        assertThrows(DoctorDoesNotExistException.class, () -> treatmentRepository.dischargePatient(1, "Lucas", "Jose"));
        assertThrows(RoomDoesNotExistException.class, () -> treatmentRepository.dischargePatient(2, "Lucas", "Martin"));
        assertThrows(PatientDoesNotExistException.class, () -> treatmentRepository.dischargePatient(1, "Jose", "Martin"));
        assertDoesNotThrow(() -> treatmentRepository.dischargePatient(1, "Lucas", "Martin"));
        assertEquals(1, treatmentRepository.getCompletedTreatments().size());
        assertDoesNotThrow(() -> treatmentRepository.dischargePatient(2, "Martin", "Joselu"));
        assertEquals(2, treatmentRepository.getCompletedTreatments().size());
        assertFalse(treatmentRepository.getTreatmentsByRoom(rooms).getFirst().hasPatient());
    }

    @Test
    void concurrentAddTreatment() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        Function<Integer, Runnable> addition = (i) -> () -> {
            for (int j = 0; j < TREATMENTS_BY_THREAD; j++) {
                Room room = Room.newBuilder().setNumber(i * TREATMENTS_BY_THREAD + j).build();
                treatmentRepository.addTreatment(Treatment.newBuilder().setRoom(room).build());
            }
        };

        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(addition.apply(i));
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(20, TimeUnit.SECONDS);
        }

        assertEquals(EXPECTED_TREATMENTS, treatmentRepository.getCurrentTreatments().size());
    }

    @Test
    void concurrentDischargePatient() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        Function<Integer, Runnable> discharge = (i) -> () -> {
            for (int j = 0; j < TREATMENTS_BY_THREAD; j++) {
                Room room = Room.newBuilder().setNumber(i * TREATMENTS_BY_THREAD + j).build();
                Doctor doctor = Doctor.newBuilder().setName("Doctor" + i * TREATMENTS_BY_THREAD + j).build();
                Patient patient = Patient.newBuilder().setName("Patient" + i * TREATMENTS_BY_THREAD + j).build();
                treatmentRepository.addTreatment(Treatment.newBuilder().setRoom(room).setDoctor(doctor).setPatient(patient).build());
                try {
                    treatmentRepository.dischargePatient(i * TREATMENTS_BY_THREAD + j, "Patient" + i * TREATMENTS_BY_THREAD + j, "Doctor" + i * TREATMENTS_BY_THREAD + j);
                } catch (Exception e) {
                    fail();
                }
            }
        };

        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(discharge.apply(i));
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(20, TimeUnit.SECONDS);
        }

        assertEquals(EXPECTED_TREATMENTS, treatmentRepository.getCompletedTreatments().size());
    }

}
