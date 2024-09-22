package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.client.utils.ChannelBuilder;
import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.waitingroom.PatientQueueInfo;
import ar.edu.itba.pod.grpc.hospital.waitingroom.WaitingRoomServiceGrpc;
import ar.edu.itba.pod.grpc.hospital.waitingroom.WaitingRoomServiceGrpc.WaitingRoomServiceBlockingStub;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class WaitingRoomClient {

    private static final Logger logger = LoggerFactory.getLogger(WaitingRoomClient.class);

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ChannelBuilder.buildChannel();

        final String action = System.getProperty("action");
        final String name = System.getProperty("patient", "");
        final int level;

        if (name.isEmpty()) {
            System.out.println("Patient name is required");
            return;
        }

        try {
            WaitingRoomServiceBlockingStub blockingStub = WaitingRoomServiceGrpc.newBlockingStub(channel);

            switch (action) {
                case "addPatient" -> {
                    level = Integer.parseInt(System.getProperty("level", "0"));
                    if (level == 0) {
                        System.out.println("Patient level is required");
                        return;
                    }

                    try {
                        final Patient patient = blockingStub.addPatient(Patient.newBuilder().setName(name).setLevel(level).build());
                        System.out.printf("Patient %s (%d) is in the waiting room\n", patient.getName(), patient.getLevel());
                    } catch (StatusRuntimeException e) {
                        System.out.println(e.getStatus().getDescription());
                    }
                }
                case "updateLevel" -> {
                    level = Integer.parseInt(System.getProperty("level", "0"));
                    if (level == 0) {
                        System.out.println("Patient level is required");
                        return;
                    }

                    try {
                        final Patient patient = blockingStub.updateLevel(Patient.newBuilder().setName(name).setLevel(level).build());
                        System.out.printf("Patient %s (%d) is in the waiting room\n", patient.getName(), patient.getLevel());
                    } catch (StatusRuntimeException e) {
                        System.out.println(e.getStatus().getDescription());
                    }
                }
                case "checkPatient" -> {
                    try {
                        final PatientQueueInfo patientQueueInfo = blockingStub.checkPatient(StringValue.newBuilder().setValue(name).build());
                        System.out.printf("Patient %s (%d) is in the waiting room with %d patients ahead\n", patientQueueInfo.getPatient().getName(), patientQueueInfo.getPatient().getLevel(), patientQueueInfo.getQueueLength());
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
