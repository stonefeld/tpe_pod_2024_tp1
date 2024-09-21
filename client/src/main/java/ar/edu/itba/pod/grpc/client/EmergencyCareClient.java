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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class EmergencyCareClient {

    private static final Logger logger = LoggerFactory.getLogger(EmergencyCareClient.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("tpe1-g2 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");
        ManagedChannel channel = ChannelBuilder.buildChannel();

        final String action = System.getProperty("action");
        final String doctorName, patientName;
        final int roomNumber;

        try {
            EmergencyCareServiceGrpc.EmergencyCareServiceBlockingStub blockingStub = EmergencyCareServiceGrpc.newBlockingStub(channel);

            switch (action) {
                case "carePatient" -> {
                    roomNumber = Integer.parseInt(System.getProperty("room"));
                    final Treatment treatment = blockingStub.carePatient(TreatmentRoom.newBuilder().setRoomNumber(roomNumber).build());

                    System.out.printf("Patient %s (%d) and Doctor %s (%d) are now in Room #%d\n",
                            treatment.getPatient().getName(),
                            treatment.getPatient().getLevel(),
                            treatment.getDoctor().getName(),
                            treatment.getDoctor().getLevel(),
                            treatment.getRoom().getNumber());
                }
                case "careAllPatients" -> {
                    final Treatments treatments = blockingStub.careAllPatients(Empty.newBuilder().build());

                    for (Treatment treatment : treatments.getTreatmentsList()) {
                        if (treatment.getRoom().getStatus().equals(Status.STATUS_FREE)) {
                            System.out.printf("Room #%d remains free\n", treatment.getRoom().getNumber());
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
                    roomNumber = Integer.parseInt(System.getProperty("room"));
                    patientName = System.getProperty("patient");
                    doctorName = System.getProperty("doctor");

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
                }
                default -> logger.error("Invalid action: {}", action);
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

}
