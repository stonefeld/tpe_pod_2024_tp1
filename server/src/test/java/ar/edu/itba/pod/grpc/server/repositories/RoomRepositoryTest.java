package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.Status;
import ar.edu.itba.pod.grpc.server.exceptions.RoomDoesNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class RoomRepositoryTest {
    private static RoomRepository roomRepository;

    @BeforeEach
    public void setUp() {
        roomRepository = new RoomRepository();
    }

    private static final int THREAD_COUNT = 1000;
    private static final int ROOMS_BY_THREAD = 1000;
    private static final int EXPECTED_ROOMS = THREAD_COUNT * ROOMS_BY_THREAD;


    @Test
    void addRoomTest() {
        Room room = roomRepository.addRoom();
        assertEquals(1, room.getNumber());
        assertEquals(Status.STATUS_FREE, room.getStatus());
    }

    @Test
    void getRoomTest() {
        Room room = roomRepository.addRoom();
        Room room2 = roomRepository.getRoom(room.getNumber());
        assertEquals(room.getNumber(), room2.getNumber());
        assertEquals(room.getStatus(), room2.getStatus());
    }

    @Test
    void getRoomNotExistsTest() {
        assertThrows(RoomDoesNotExistException.class, () -> roomRepository.getRoom(1));
    }

    @Test
    void setRoomStatusTest() {
        Room room = roomRepository.addRoom();
        Room room2 = roomRepository.setRoomStatus(1, Status.STATUS_OCCUPIED);
        assertEquals(room.getNumber(), room2.getNumber());
        assertEquals(Status.STATUS_OCCUPIED, room2.getStatus());
    }

    @Test
    void getRoomsTest() {
        List<Room> rooms = new ArrayList<>();
        Room room = roomRepository.addRoom();
        rooms.add(room);
        Room room2 = roomRepository.addRoom();
        rooms.add(room2);

        List<Room> r = roomRepository.getRooms();
        assertEquals(2, r.size());
        assertEquals(room.getNumber(), r.get(0).getNumber());
        assertEquals(room2.getNumber(), r.get(1).getNumber());
        assertEquals(rooms, r);
    }

    @Test
    void roomExistsTest() {
        roomRepository.addRoom();
        assertTrue(roomRepository.roomExists(1));
    }

    @Test
    void roomDoesNotExistTest() {
        assertFalse(roomRepository.roomExists(1));
    }

    @Test
    void concurrentAddRoomTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        Runnable addition = () -> {
            for (int i = 0; i < ROOMS_BY_THREAD; i++) {
                roomRepository.addRoom();
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

        List<Room> rooms = roomRepository.getRooms();
        assertEquals(EXPECTED_ROOMS, rooms.size());
        for (int i = 1; i <= EXPECTED_ROOMS; i++) {
            assertEquals(i, rooms.get(i - 1).getNumber());
        }
    }

}