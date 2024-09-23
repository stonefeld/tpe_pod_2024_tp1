package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.client.utils.AvailabilityConverter;
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
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class AdministrationClient {

    private static final Logger logger = LoggerFactory.getLogger(AdministrationClient.class);

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ChannelBuilder.buildChannel();

        final String action = System.getProperty("action");
        final String doctorName = System.getProperty("doctor", "");
        final String level = System.getProperty("level", "");
        final String availabilityName = System.getProperty("availability", "");

        try {
            AdministrationServiceBlockingStub blockingStub = AdministrationServiceGrpc.newBlockingStub(channel);

            switch (action) {
                case "addRoom" -> {
                    final Room room = blockingStub.addRoom(Empty.newBuilder().build());
                    System.out.printf("Room #%d added successfully\n", room.getNumber());
                }
                case "addDoctor" -> {
                    if (doctorName.isEmpty()) {
                        System.out.println("Doctor name is required");
                        return;
                    }
                    if (level.isEmpty()) {
                        System.out.println("Doctor level is required");
                        return;
                    }

                    int levelNumber;
                    try {
                        levelNumber = Integer.parseInt(level);
                    } catch (NumberFormatException e) {
                        System.out.println("Level must be a number");
                        return;
                    }

                    try {
                        final Doctor doctor = blockingStub.addDoctor(DoctorCreation.newBuilder().setName(doctorName).setLevel(levelNumber).build());
                        System.out.printf("Doctor %s (%d) added successfully\n", doctor.getName(), doctor.getLevel());
                    } catch (StatusRuntimeException e) {
                        System.out.println(e.getStatus().getDescription());
                    }
                }
                case "setDoctor" -> {
                    if (doctorName.isEmpty()) {
                        System.out.println("Doctor name is required");
                        return;
                    }
                    if (availabilityName.isEmpty()) {
                        System.out.println("Doctor availability is required");
                        return;
                    }

                    Availability availability = AvailabilityConverter.strToAvailability(availabilityName);
                    if (availability.equals(Availability.AVAILABILITY_UNSPECIFIED)) {
                        System.out.println("Invalid availability");
                        return;
                    }

                    try {
                        final Doctor doctor = blockingStub.setDoctor(DoctorAvailabilityUpdate.newBuilder().setDoctorName(doctorName).setAvailability(availability).build());
                        System.out.printf("Doctor %s (%d) is %s\n",
                                doctor.getName(), doctor.getLevel(),
                                AvailabilityConverter.availabilityToStr(doctor.getAvailability())
                        );
                    } catch (StatusRuntimeException e) {
                        System.out.println(e.getStatus().getDescription());
                    }
                }
                case "checkDoctor" -> {
                    if (doctorName.isEmpty()) {
                        System.out.println("Doctor name is required");
                        return;
                    }

                    try {
                        final Doctor doctor = blockingStub.checkDoctor(StringValue.newBuilder().setValue(doctorName).build());
                        System.out.printf("Doctor %s (%d) is %s\n",
                                doctor.getName(), doctor.getLevel(),
                                AvailabilityConverter.availabilityToStr(doctor.getAvailability())
                        );
                    } catch (StatusRuntimeException e) {
                        System.out.println(e.getStatus().getDescription());
                    }
                }
                default -> logger.error("Invalid action: {}", action);
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

}
