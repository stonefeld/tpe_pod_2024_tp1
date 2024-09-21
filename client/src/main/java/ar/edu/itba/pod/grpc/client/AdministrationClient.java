package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.client.utils.ChannelBuilder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class AdministrationClient {

    private static final Logger logger = LoggerFactory.getLogger(AdministrationClient.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("tpe1-g2 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");
        ManagedChannel channel = ChannelBuilder.buildChannel();

        final String action = System.getProperty("action");
        final String doctorName;
        final int levelNumber;
        final Availability availability;

        try {
            AdministrationServiceBlockingStub blockingStub = AdministrationServiceGrpc.newBlockingStub(channel);

            switch (action) {
                case "addRoom" -> {
                    final Room room = blockingStub.addRoom(Empty.newBuilder().build());
                    System.out.printf("Room #%d added successfully\n", room.getNumber());
                }
                case "addDoctor" -> {
                    doctorName = System.getProperty("doctor");
                    levelNumber = Integer.parseInt(System.getProperty("level"));

                    final Doctor doctor = blockingStub.addDoctor(DoctorCreation.newBuilder().setName(doctorName).setLevel(levelNumber).build());
                    System.out.printf("Doctor %s (%d) added successfully\n", doctor.getName(), doctor.getLevel());
                }
                case "setDoctor" -> {
                    doctorName = System.getProperty("doctor");
                    availability = Availability.valueOf("AVAILABILITY_" + System.getProperty("availability").toUpperCase());

                    final Doctor doctor = blockingStub.setDoctor(DoctorAvailabilityUpdate.newBuilder().setDoctorName(doctorName).setAvailability(availability).build());
                    System.out.printf("Doctor %s (%d) is %s\n", doctor.getName(), doctor.getLevel(), doctor.getAvailability());
                }
                case "checkDoctor" -> {
                    doctorName = System.getProperty("doctor");

                    final Doctor doctor = blockingStub.checkDoctor(StringValue.newBuilder().setValue(doctorName).build());
                    System.out.printf("Doctor %s (%d) is %s\n", doctor.getName(), doctor.getLevel(), doctor.getAvailability());
                }
                default -> logger.error("Invalid action: {}", action);
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

}
