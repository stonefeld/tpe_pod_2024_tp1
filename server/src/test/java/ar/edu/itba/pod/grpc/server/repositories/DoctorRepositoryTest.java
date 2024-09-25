package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Availability;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exceptions.InvalidLevelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DoctorRepositoryTest {

    private static DoctorRepository doctorRepository;

    private static final int THREAD_COUNT = 1000;
    private static final int DOCTORS_BY_THREAD = 1000;
    private static final int EXPECTED_DOCTORS = THREAD_COUNT * DOCTORS_BY_THREAD;

    private final Random r = new Random();

    @BeforeEach
    void setUp() {
        doctorRepository = new DoctorRepository();
    }

    @Test
    void addDoctorTest() {
        assertDoesNotThrow(() -> doctorRepository.addDoctor("Martin", 5));
        assertThrows(DoctorAlreadyExistsException.class, () -> doctorRepository.addDoctor("Martin", 5));
        assertThrows(InvalidLevelException.class, () -> doctorRepository.addDoctor("Jose", 6));
    }

    @Test
    void doctorAvailabilityTest() {
        doctorRepository.addDoctor("Martin", 5);
        assertEquals(1, doctorRepository.getAvailableDoctors().size());
        doctorRepository.setDoctorAvailability("Martin", Availability.AVAILABILITY_UNAVAILABLE);
        assertEquals(0, doctorRepository.getAvailableDoctors().size());
    }

    @Test
    void checkDoctorTest() {
        doctorRepository.addDoctor("Martin", 5);
        assertEquals(5, doctorRepository.checkDoctor("Martin").getLevel());
        assertThrows(DoctorAlreadyExistsException.class, () -> doctorRepository.addDoctor("Martin", 5));
    }

    @Test
    void concurrentAddDoctorTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        Runnable addition = () -> {
            for (int i = 0; i < DOCTORS_BY_THREAD; i++) {
                doctorRepository.addDoctor("Doctor" + UUID.randomUUID(), r.nextInt(1, 5));
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

        assertEquals(EXPECTED_DOCTORS, doctorRepository.getAvailableDoctors().size());
    }

    @Test
    void concurrentAddSameDoctorTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        String name = "John Doe";

        Runnable addSamePatient = () -> {
            try {
                doctorRepository.addDoctor(name, r.nextInt(1, 5));
            } catch (DoctorAlreadyExistsException e) {
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

        assertEquals(1, doctorRepository.getAvailableDoctors().size());
    }

}
