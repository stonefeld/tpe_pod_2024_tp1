package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.Status;

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class RoomRepository {

    private final SortedSet<Room> rooms = new TreeSet<>(Comparator.comparingInt(Room::getNumber));

    public Room addRoom() {
        Room room;
        synchronized (rooms) {
            room = Room.newBuilder().setNumber(rooms.size() + 1).setStatus(Status.STATUS_FREE).build();
            rooms.add(room);
        }
        return room;
    }

    public Room getRoom(int roomNumber) {
        synchronized (rooms) {
            return rooms.stream().filter(r -> r.getNumber() == roomNumber).findFirst().orElse(null);
        }
    }

    public Room setRoomStatus(int number, Status status) {
        Room room = null;
        synchronized (rooms) {
            if (rooms.removeIf(r -> r.getNumber() == number)) {
                room = Room.newBuilder().setNumber(number).setStatus(status).build();
                rooms.add(room);
            }
        }
        return room;
    }

    public List<Room> getRooms() {
        return List.copyOf(rooms);
    }

}
