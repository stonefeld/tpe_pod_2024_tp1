package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.waitingroom.WaitingRoomServiceGrpc.WaitingRoomServiceBlockingStub;
import ar.edu.itba.pod.grpc.hospital.waitingroom.WaitingRoomServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class WaitingRoomClient {

    private static Logger logger = LoggerFactory.getLogger(WaitingRoomClient.class);

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
        final String name;
        final int level;

        try {
            WaitingRoomServiceBlockingStub blockingStub = WaitingRoomServiceGrpc.newBlockingStub(channel);

            switch (action) {
                case "addPatient" -> {
                    name = System.getProperty("patient");
                    level = Integer.parseInt(System.getProperty("level"));

                    final Patient patient = blockingStub.addPatient(Patient.newBuilder().setName(name).setLevel(level).build());
                    logger.info("Patient {} ({}) is in the waiting room", patient.getName(), patient.getLevel());
                }
                default -> logger.error("Invalid action: {}", action);
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

}
