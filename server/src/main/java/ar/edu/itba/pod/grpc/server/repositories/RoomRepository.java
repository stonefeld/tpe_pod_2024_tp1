package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Doctor;
import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.Room;

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class RoomRepository {

    private final SortedSet<Room> rooms = new TreeSet<>(Comparator.comparingInt(Room::getNumber));

    public Room addRoom() {
        Room room;
        synchronized (rooms) {
            room = Room.newBuilder().setNumber(rooms.size() + 1).build();
            rooms.add(room);
        }
        return room;
    }

    public Room updateRoom(int number, Patient patient, Doctor doctor) {
        Room room = null;
        synchronized (rooms) {
            if (rooms.removeIf(r -> r.getNumber() == number && !r.hasPatient() && !r.hasDoctor())) {
                room = Room.newBuilder().setNumber(number).setPatient(patient).setDoctor(doctor).build();
                rooms.add(room);
            }
        }
        return room;
    }

    public List<Room> getRooms() {
        return List.copyOf(rooms);
    }

}
