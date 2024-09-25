package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.server.exceptions.InvalidLevelException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientDoesNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class PatientRepositoryTest {

    private static PatientRepository patientRepository;

    private static final int THREAD_COUNT = 1000;
    private static final int PATIENTS_BY_THREAD = 1000;
    private static final int EXPECTED_PATIENTS = THREAD_COUNT * PATIENTS_BY_THREAD;

    private final Random r = new Random();

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
    void updateLevelTest() {
        Patient p1 = patientRepository.addPatient("Martin", 5);
        Patient p2 = patientRepository.updateLevel("Martin", 4);

        assertEquals(p1.getName(), p2.getName());
        assertEquals(4, p2.getLevel());
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
    void getFirstPatientFromEveryLevelTest() {
        patientRepository.addPatient("A", 5);
        patientRepository.addPatient("B", 5);
        patientRepository.addPatient("C", 3);
        patientRepository.addPatient("D", 3);

        List<Patient> patients = patientRepository.getFirstPatientFromEveryLevel();

        assertEquals(2, patients.size());
        assertEquals("A", patients.getFirst().getName());
        assertEquals("C", patients.getLast().getName());
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
    void checkAndUpdatePatientTest() {
        patientRepository.addPatient("Primero", 1);
        patientRepository.addPatient("Segundo", 1);
        assertEquals(0, patientRepository.checkPatient("Primero").getQueueLength());
        assertEquals(1, patientRepository.checkPatient("Segundo").getQueueLength());

        patientRepository.updateLevel("Primero", 2);
        patientRepository.addPatient("Tercero", 2);
        assertEquals(0, patientRepository.checkPatient("Primero").getQueueLength());
        assertEquals(1, patientRepository.checkPatient("Tercero").getQueueLength());
        assertEquals(2, patientRepository.checkPatient("Segundo").getQueueLength());

        patientRepository.updateLevel("Segundo", 2);
        assertEquals(0, patientRepository.checkPatient("Primero").getQueueLength());
        assertEquals(1, patientRepository.checkPatient("Segundo").getQueueLength());
        assertEquals(2, patientRepository.checkPatient("Tercero").getQueueLength());
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
                patientRepository.addPatient("Patient" + UUID.randomUUID(), r.nextInt(1, 5));
            }
        };

        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(addition);
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(20, TimeUnit.SECONDS);
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
            } catch (PatientAlreadyExistsException e) {
                // Expected
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

        assertEquals(1, patientRepository.getPatients().size());
    }
}
