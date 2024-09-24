package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.server.exceptions.DoctorAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exceptions.InvalidLevelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DoctorRepositoryTest {

    private static DoctorRepository doctorRepository;

    @BeforeEach
    public void setUp() {
        doctorRepository = new DoctorRepository();
    }

    @Test
    public void invalidLevelTest() {
        assertThrows(InvalidLevelException.class, () -> doctorRepository.addDoctor("Martin", 6));
        assertDoesNotThrow(() -> doctorRepository.addDoctor("Martin", 5));
    }

    @Test
    public void doctorExistsTest() {
        assertDoesNotThrow(() -> doctorRepository.addDoctor("Martin", 5));
        assertThrows(DoctorAlreadyExistsException.class, () -> doctorRepository.addDoctor("Martin", 5));
        assertTrue(doctorRepository.doctorExists("Martin"));
    }

}
