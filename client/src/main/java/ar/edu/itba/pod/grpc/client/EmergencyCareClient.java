package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.client.utils.ChannelBuilder;
import ar.edu.itba.pod.grpc.hospital.Status;
import ar.edu.itba.pod.grpc.hospital.Treatment;
import ar.edu.itba.pod.grpc.hospital.TreatmentRoom;
import ar.edu.itba.pod.grpc.hospital.Treatments;
import ar.edu.itba.pod.grpc.hospital.emergencycare.EmergencyCareServiceGrpc;
import ar.edu.itba.pod.grpc.hospital.emergencycare.TreatmentEnding;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class EmergencyCareClient {

    private static final Logger logger = LoggerFactory.getLogger(EmergencyCareClient.class);

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ChannelBuilder.buildChannel();

        final String action = System.getProperty("action");
        final String doctorName = System.getProperty("doctor", "");
        final String patientName = System.getProperty("patient", "");
        final String room = System.getProperty("room", "");

        try {
            EmergencyCareServiceGrpc.EmergencyCareServiceBlockingStub blockingStub = EmergencyCareServiceGrpc.newBlockingStub(channel);

            switch (action) {
                case "carePatient" -> {
                    if (room.isEmpty()) {
                        System.out.println("Room number is required");
                        return;
                    }

                    int roomNumber;
                    try {
                        roomNumber = Integer.parseInt(room);
                    } catch (NumberFormatException e) {
                        System.out.println("Room number must be a number");
                        return;
                    }

                    try {
                        final Treatment treatment = blockingStub.carePatient(TreatmentRoom.newBuilder().setRoomNumber(roomNumber).build());
                        System.out.printf("Patient %s (%d) and Doctor %s (%d) are now in Room #%d\n",
                                treatment.getPatient().getName(),
                                treatment.getPatient().getLevel(),
                                treatment.getDoctor().getName(),
                                treatment.getDoctor().getLevel(),
                                treatment.getRoom().getNumber());
                    } catch (StatusRuntimeException e) {
                        System.out.println(e.getStatus().getDescription());
                    }
                }
                case "careAllPatients" -> {
                    final Treatments treatments = blockingStub.careAllPatients(Empty.newBuilder().build());

                    for (Treatment treatment : treatments.getTreatmentsList()) {
                        if (treatment.getRoom().getStatus().equals(Status.STATUS_FREE)) {
                            System.out.printf("Room #%d remains Free\n", treatment.getRoom().getNumber());
                        } else if (treatment.getRoom().getStatus().equals(Status.STATUS_OCCUPIED) && treatment.hasDoctor() && treatment.hasPatient()) {
                            System.out.printf("Patient %s (%d) and Doctor %s (%d) are now in Room #%d\n",
                                    treatment.getPatient().getName(),
                                    treatment.getPatient().getLevel(),
                                    treatment.getDoctor().getName(),
                                    treatment.getDoctor().getLevel(),
                                    treatment.getRoom().getNumber());
                        } else if (treatment.getRoom().getStatus().equals(Status.STATUS_OCCUPIED) && !treatment.hasDoctor() && !treatment.hasPatient()) {
                            System.out.printf("Room #%d remains Occupied\n", treatment.getRoom().getNumber());
                        }
                    }
                }
                case "dischargePatient" -> {
                    if (doctorName.isEmpty()) {
                        System.out.println("Doctor name is required");
                        return;
                    }
                    if (patientName.isEmpty()) {
                        System.out.println("Patient name is required");
                        return;
                    }
                    if (room.isEmpty()) {
                        System.out.println("Room number is required");
                        return;
                    }

                    int roomNumber;
                    try {
                        roomNumber = Integer.parseInt(room);
                    } catch (NumberFormatException e) {
                        System.out.println("Room number must be a number");
                        return;
                    }

                    try {
                        final Treatment treatment = blockingStub.dischargePatient(TreatmentEnding.newBuilder()
                                .setRoomNumber(roomNumber)
                                .setPatientName(patientName)
                                .setDoctorName(doctorName)
                                .build());

                        System.out.printf("Patient %s (%d) has been discharged from Doctor %s (%d) and the Room #%d is now Free\n",
                                treatment.getPatient().getName(),
                                treatment.getPatient().getLevel(),
                                treatment.getDoctor().getName(),
                                treatment.getDoctor().getLevel(),
                                treatment.getRoom().getNumber());
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
