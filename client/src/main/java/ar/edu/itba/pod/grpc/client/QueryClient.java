package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.query.QueryServiceGrpc;
import ar.edu.itba.pod.grpc.hospital.query.QueryServiceGrpc.QueryServiceBlockingStub;
import ar.edu.itba.pod.grpc.hospital.query.Rooms;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class QueryClient {

    private static Logger logger = LoggerFactory.getLogger(QueryClient.class);

    public static void main(String[] args) throws InterruptedException {
        final String[] serverAddress = System.getProperty("serverAddress").split(":");
        final String ip = serverAddress[0];
        final int port = Integer.parseInt(serverAddress[1]);

        logger.info("tpe1-g2 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");
        ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port)
                .usePlaintext()
                .build();

        final String action = System.getProperty("action");
        final String fileName;

        try {
            QueryServiceBlockingStub blockingStub = QueryServiceGrpc.newBlockingStub(channel);

            switch (action) {
                case "queryRooms" -> {
                    fileName = System.getProperty("outPath");
                    final Rooms rooms = blockingStub.queryRooms(Empty.newBuilder().build());

                    try {
                        writeRoomsToCSV(fileName, rooms);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                default -> logger.error("Invalid action: {}", action);
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    public static void writeRoomsToCSV(String fileName, Rooms rooms) throws IOException {
        List<String> lines = new ArrayList<>();

        lines.add("Room,Status,Patient,Doctor");

        for (Room room : rooms.getRoomsList()) {
            String roomNumber = String.valueOf(room.getNumber());
            String status = (!room.hasPatient() && !room.hasDoctor()) ? "Free" : "Occupied";
            String patient = room.hasPatient()
                    ? room.getPatient().getName() + " (" + room.getPatient().getLevel() + ")"
                    : "";
            String doctor = room.hasDoctor()
                    ? room.getDoctor().getName() + " (" + room.getDoctor().getLevel() + ")"
                    : "";

            String csvLine = String.join(",", roomNumber, status, patient, doctor);
            lines.add(csvLine);
        }

        Files.write(Paths.get(fileName), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}

