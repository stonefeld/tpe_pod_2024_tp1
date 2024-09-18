package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.administration.AdministrationServiceGrpc;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class AdministrationClient {

    private static Logger logger = LoggerFactory.getLogger(AdministrationClient.class);

    public static void main(String[] args) throws InterruptedException {
        final String[] serverAddress = System.getProperty("serverAddress").split(":");
        final String ip = serverAddress[0];
        final int port = Integer.parseInt(serverAddress[1]);

        logger.info("tpe1-g2 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");
        ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port)
                .usePlaintext()
                .build();

        final String action = System.getProperty("actionName");

        try {
            AdministrationServiceGrpc.AdministrationServiceBlockingStub blockingStub = AdministrationServiceGrpc.newBlockingStub(channel);

            if (action.equals("addRoom")) {
                final Room room = blockingStub.addRoom(Empty.newBuilder().build());
                logger.info("Room #{} added successfully", room.getNumber());
            } else {
                logger.error("Invalid action: {}", action);
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

}
