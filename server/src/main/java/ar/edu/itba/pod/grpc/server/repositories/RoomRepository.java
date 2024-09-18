package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Room;

import java.util.ArrayList;
import java.util.List;

public class RoomRepository {

    private final List<Room> rooms = new ArrayList<>();

    public Room addRoom() {
        Room room;
        synchronized (rooms) {
            room = Room.newBuilder().setNumber(rooms.size() + 1).build();
            rooms.add(room);
        }
        return room;
    }

}
