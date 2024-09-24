package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.server.exceptions.InvalidLevelException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientDoesNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PatientRepositoryTest {

    private static PatientRepository patientRepository;

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

}
