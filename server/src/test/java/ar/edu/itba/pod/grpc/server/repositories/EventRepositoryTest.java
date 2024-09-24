package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.doctorpager.Event;
import ar.edu.itba.pod.grpc.hospital.doctorpager.Type;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorAlreadyRegisteredException;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorNotRegisteredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class EventRepositoryTest {
   private static EventRepository eventRepository;

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
}
