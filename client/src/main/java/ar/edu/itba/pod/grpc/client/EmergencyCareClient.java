package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.hospital.Availability;
import ar.edu.itba.pod.grpc.hospital.Doctor;
import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.administration.AdministrationServiceGrpc;
import ar.edu.itba.pod.grpc.hospital.administration.DoctorAvailabilityUpdate;
import ar.edu.itba.pod.grpc.hospital.administration.DoctorCreation;
import ar.edu.itba.pod.grpc.hospital.emergencycare.EmergencyCareServiceGrpc;
import ar.edu.itba.pod.grpc.hospital.emergencycare.TreatmentRoom;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class EmergencyCareClient {

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

        final String action = System.getProperty("action");
        final String doctorName, patientName;
        final int roomNumber;

        try {
            EmergencyCareServiceGrpc.EmergencyCareServiceBlockingStub blockingStub = EmergencyCareServiceGrpc.newBlockingStub(channel);

            switch (action) {
                case "carePatient" -> {
                    roomNumber = Integer.parseInt(System.getProperty("room"));
                    final Room room = blockingStub.carePatient(TreatmentRoom.newBuilder().setRoomNumber(roomNumber).build());

                    logger.info("Patient {} ({}) and Doctor {} ({}) are now in Room #{}",
                            room.getPatient().getName(),
                            room.getPatient().getLevel(),
                            room.getDoctor().getName(),
                            room.getDoctor().getLevel(),
                            room.getNumber());
                }
                default -> logger.error("Invalid action: {}", action);
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

}
