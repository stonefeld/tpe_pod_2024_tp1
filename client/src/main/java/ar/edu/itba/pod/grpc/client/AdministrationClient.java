package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.hospital.Availability;
import ar.edu.itba.pod.grpc.hospital.Doctor;
import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.administration.AdministrationServiceGrpc;
import ar.edu.itba.pod.grpc.hospital.administration.AdministrationServiceGrpc.AdministrationServiceBlockingStub;
import ar.edu.itba.pod.grpc.hospital.administration.DoctorAvailabilityUpdate;
import ar.edu.itba.pod.grpc.hospital.administration.DoctorCreation;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
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

        final String action = System.getProperty("action");
        final String doctorName;
        final int levelNumber;
        final Availability availability;

        try {
            AdministrationServiceBlockingStub blockingStub = AdministrationServiceGrpc.newBlockingStub(channel);

            switch (action) {
                case "addRoom" -> {
                    final Room room = blockingStub.addRoom(Empty.newBuilder().build());

                    logger.info("Room #{} added successfully", room.getNumber());
                }
                case "addDoctor" -> {
                    doctorName = System.getProperty("doctor");
                    levelNumber = Integer.parseInt(System.getProperty("level"));

                    final Doctor doctor = blockingStub.addDoctor(DoctorCreation.newBuilder().setName(doctorName).setLevel(levelNumber).build());
                    logger.info("Doctor {} ({}) added successfully", doctor.getName(), doctor.getLevel());
                }
                case "setDoctor" -> {
                    doctorName = System.getProperty("doctor");
                    availability = Availability.valueOf(System.getProperty("availability"));

                    final Doctor doctor = blockingStub.setDoctor(DoctorAvailabilityUpdate.newBuilder().setDoctorName(doctorName).setAvailability(availability).build());
                    logger.info("Doctor {} ({}) is {}", doctor.getName(), doctor.getLevel(), doctor.getAvailability());
                }
                case "checkDoctor" -> {
                    doctorName = System.getProperty("doctor");

                    final Doctor doctor = blockingStub.checkDoctor(StringValue.newBuilder(StringValue.of(doctorName)).build());
                    logger.info("Doctor {} ({}) is {}", doctor.getName(), doctor.getLevel(), doctor.getAvailability());
                }
                default -> logger.error("Invalid action: {}", action);
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

}
