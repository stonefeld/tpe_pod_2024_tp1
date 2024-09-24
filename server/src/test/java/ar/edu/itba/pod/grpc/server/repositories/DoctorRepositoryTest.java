package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Availability;
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
    void addDoctorTest() {
        assertDoesNotThrow(() -> doctorRepository.addDoctor("Martin", 5));
        assertThrows(DoctorAlreadyExistsException.class, () -> doctorRepository.addDoctor("Martin", 5));
        assertThrows(InvalidLevelException.class, () -> doctorRepository.addDoctor("Jose", 6));
    }
    @Test
    void doctorAvailabilityTest(){
        doctorRepository.addDoctor("Martin", 5);
        assertEquals(1, doctorRepository.getAvailableDoctors().size());
        doctorRepository.setDoctorAvailability("Martin", Availability.AVAILABILITY_UNAVAILABLE);
        assertEquals(0, doctorRepository.getAvailableDoctors().size());
    }
    @Test
    void checkDoctorTest(){
        doctorRepository.addDoctor("Martin", 5);
        assertEquals(5, doctorRepository.checkDoctor("Martin").getLevel());
        assertThrows(DoctorAlreadyExistsException.class, () -> doctorRepository.addDoctor("Martin", 5));
    }
}
