package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.client.utils.ChannelBuilder;
import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.waitingroom.WaitingRoomServiceGrpc;
import ar.edu.itba.pod.grpc.hospital.waitingroom.WaitingRoomServiceGrpc.WaitingRoomServiceBlockingStub;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class WaitingRoomClient {

    private static final Logger logger = LoggerFactory.getLogger(WaitingRoomClient.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("tpe1-g2 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");
        ManagedChannel channel = ChannelBuilder.buildChannel();

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
                    System.out.printf("Patient %s (%d) is in the waiting room\n", patient.getName(), patient.getLevel());
                }
                default -> logger.error("Invalid action: {}", action);
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

}
