package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.Status;
import ar.edu.itba.pod.grpc.server.exceptions.RoomDoesNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RoomRepositoryTest {
    private static RoomRepository roomRepository;

    @BeforeEach
    public void setUp() {
        roomRepository = new RoomRepository();
    }
    @Test
    void addRoomTest() {
         assertEquals(1, roomRepository.addRoom().getNumber());
         assertEquals(Status.STATUS_FREE,roomRepository.addRoom().getStatus());
         assertEquals(3, roomRepository.addRoom().getNumber());
         assertEquals(4, roomRepository.addRoom().getNumber());
    }
    @Test
    void getRoomTest() {
        roomRepository.addRoom();
        roomRepository.addRoom();
        roomRepository.addRoom();
        roomRepository.addRoom();
        assertEquals(1, roomRepository.getRoom(1).getNumber());
        assertEquals(2, roomRepository.getRoom(2).getNumber());
        assertEquals(3, roomRepository.getRoom(3).getNumber());
        assertEquals(4, roomRepository.getRoom(4).getNumber());
        assertThrows(RoomDoesNotExistException.class, () -> roomRepository.getRoom(5));
    }
    @Test
    void setRoomStatusTest() {
        roomRepository.addRoom();
        roomRepository.addRoom();
        roomRepository.addRoom();
        roomRepository.addRoom();
        assertEquals(Status.STATUS_FREE, roomRepository.getRoom(1).getStatus());
        assertEquals(Status.STATUS_OCCUPIED, roomRepository.setRoomStatus(1, Status.STATUS_OCCUPIED).getStatus());

    }
    @Test
    void getRoomsTest() {
        List<Room> rooms = new ArrayList<>();
        rooms.add(roomRepository.addRoom());
        rooms.add(roomRepository.addRoom());
        rooms.add(roomRepository.addRoom());
        rooms.add(roomRepository.addRoom());
        assertEquals(rooms, roomRepository.getRooms());
    }
    @Test
    void roomExistsTest() {
        roomRepository.addRoom();
        roomRepository.addRoom();
        roomRepository.addRoom();
        roomRepository.addRoom();
        assertTrue(roomRepository.roomExists(1));
        assertTrue(roomRepository.roomExists(2));
        assertTrue(roomRepository.roomExists(3));
        assertTrue(roomRepository.roomExists(4));
        assertFalse(roomRepository.roomExists(5));
    }
}
