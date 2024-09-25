package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.doctorpager.Event;
import ar.edu.itba.pod.grpc.hospital.doctorpager.Type;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorAlreadyRegisteredException;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorNotRegisteredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


public class EventRepositoryTest {

    private static EventRepository eventRepository;

    private static final int THREAD_COUNT = 1000;
    private static final int EVENTS_BY_THREAD = 1000;
    private static final int EXPECTED_EVENTS = THREAD_COUNT * EVENTS_BY_THREAD;

    @BeforeEach
    public void setUp() {
        eventRepository = new EventRepository();

    }

    @Test
    void registerDoctorTest() {
        assertDoesNotThrow(() -> eventRepository.registerDoctor("Martin"));
        assertThrows(DoctorAlreadyRegisteredException.class, () -> eventRepository.registerDoctor("Martin"));
    }

    @Test
    void unregisterDoctorTest() {
        assertDoesNotThrow(() -> eventRepository.registerDoctor("Martin"));
        assertDoesNotThrow(() -> eventRepository.unregisterDoctor("Martin"));
        assertThrows(DoctorNotRegisteredException.class, () -> eventRepository.unregisterDoctor("Martin"));
    }

    @Test
    void eventsTest() {
        eventRepository.registerDoctor("Martin");
        eventRepository.addEvent("Martin", Event.newBuilder().setType(Type.UNSPECIFIED).build());
        assertEquals(Type.UNSPECIFIED, eventRepository.getEvent("Martin").getType());
        assertThrows(DoctorNotRegisteredException.class, () -> eventRepository.getEvent("Lucas"));
    }

    @Test
    void concurrentAddEventTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        String name = "John Doe";
        eventRepository.registerDoctor(name);

        Runnable addEvent = () -> {
            for (int i = 0; i < EVENTS_BY_THREAD; i++) {
                eventRepository.addEvent(name, Event.newBuilder().setType(Type.TREATMENT).build());
            }
        };

        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(addEvent);
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(20, TimeUnit.SECONDS);
        }

        int i = 0;
        while (eventRepository.getEvent(name) != null) {
            i++;
        }
        assertEquals(EXPECTED_EVENTS, i);
    }
}
