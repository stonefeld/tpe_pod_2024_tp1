package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Room;
import com.google.protobuf.Empty;

import java.util.*;

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

    public List<Room> getRooms() {
        return List.copyOf(rooms);
    }

}
