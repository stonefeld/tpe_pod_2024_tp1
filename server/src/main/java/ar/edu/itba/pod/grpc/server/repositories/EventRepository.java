package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.doctorpager.Event;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorNotRegisteredException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class EventRepository {

    private final Map<String, Queue<Event>> events = new HashMap<>();

    public void registerDoctor(String doctor) {
        synchronized (events) {
            events.put(doctor, new LinkedList<>());
        }
    }

    public void unregisterDoctor(String doctor) {
        synchronized (events) {
            events.remove(doctor);
        }
    }

    public void addEvent(String doctor, Event event) {
        synchronized (events) {
            if (!events.containsKey(doctor))
                return;
            events.get(doctor).add(event);
        }
    }

    public Event getEvent(String doctor) {
        synchronized (events) {
            if (!events.containsKey(doctor))
                throw new DoctorNotRegisteredException("Doctor not registered");
            return events.get(doctor).poll();
        }
    }

}
