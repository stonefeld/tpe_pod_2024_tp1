package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.server.exceptions.InvalidLevelException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientDoesNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class PatientRepositoryTest {

    private static PatientRepository patientRepository;

    private static final int THREAD_COUNT = 100;
    private static final int PATIENTS_BY_THREAD = 100;
    private static final int EXPECTED_PATIENTS = THREAD_COUNT * PATIENTS_BY_THREAD;
    Random r = new Random();

    @BeforeEach
    public void setUp() {
        patientRepository = new PatientRepository();
    }

    @Test
    void invalidLevelTest() {
        assertThrows(InvalidLevelException.class, () -> patientRepository.addPatient("Martin", 6));
        assertDoesNotThrow(() -> patientRepository.addPatient("Martin", 5));
        assertThrows(InvalidLevelException.class, () -> patientRepository.updateLevel("Martin", 6));
    }

    @Test
    void patientExistsTest() {
        assertDoesNotThrow(() -> patientRepository.addPatient("Martin", 5));
        assertThrows(PatientAlreadyExistsException.class, () -> patientRepository.addPatient("Martin", 5));
        assertTrue(patientRepository.patientExists("Martin"));
    }

    @Test
    void getPatientsTest() {
        assertDoesNotThrow(() -> patientRepository.addPatient("Martin", 5));
        assertDoesNotThrow(() -> patientRepository.addPatient("Marcos", 4));
        assertDoesNotThrow(() -> patientRepository.addPatient("Juan", 3));
        assertDoesNotThrow(() -> patientRepository.addPatient("Jose", 3));
        assertDoesNotThrow(() -> patientRepository.addPatient("Julio", 1));
        assertTrue(patientRepository.patientExists("Martin"));
        assertTrue(patientRepository.patientExists("Marcos"));
        assertTrue(patientRepository.patientExists("Juan"));
        assertTrue(patientRepository.patientExists("Jose"));
        assertTrue(patientRepository.patientExists("Julio"));
    }

    @Test
    void checkPatientTest() {
        assertDoesNotThrow(() -> patientRepository.addPatient("Lucas", 5));
        assertDoesNotThrow(() -> patientRepository.addPatient("Marcos", 4));
        assertDoesNotThrow(() -> patientRepository.addPatient("Juan", 3));
        assertDoesNotThrow(() -> patientRepository.addPatient("Julio", 1));
        assertEquals(2, patientRepository.checkPatient("Juan").getQueueLength());
        assertThrows(PatientDoesNotExistException.class, () -> patientRepository.checkPatient("Jose"));
    }

    @Test
    void attendPatientTest() {
        assertDoesNotThrow(() -> patientRepository.addPatient("Lucas", 5));
        assertDoesNotThrow(() -> patientRepository.addPatient("Marcos", 4));
        assertDoesNotThrow(() -> patientRepository.addPatient("Juan", 3));
        assertDoesNotThrow(() -> patientRepository.addPatient("Julio", 1));
        patientRepository.attendPatient(patientRepository.getPatients().getFirst());
        assertFalse(patientRepository.patientExists("Lucas"));
    }

    @Test
    void concurrentAddPatientTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        Runnable addition = () -> {
            for (int i = 0; i < PATIENTS_BY_THREAD; i++) {
                patientRepository.addPatient(String.valueOf(r.nextInt()), r.nextInt(1, 5));
            }
        };

        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(addition);
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }

        assertEquals(EXPECTED_PATIENTS, patientRepository.getPatients().size());
    }

    @Test
    void concurrentAddSamePatientTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        String name = "John Doe";

        Runnable addSamePatient = () -> {
            try {
                patientRepository.addPatient(name, r.nextInt(1, 5));
            } catch (PatientAlreadyExistsException ignored) {
            }
        };

        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(addSamePatient);
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }

        System.out.println(patientRepository.getPatients());
        assertEquals(1, patientRepository.getPatients().size());
    }


}
