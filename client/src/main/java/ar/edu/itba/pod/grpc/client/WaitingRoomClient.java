package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.client.utils.ChannelBuilder;
import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.waitingroom.PatientQueueInfo;
import ar.edu.itba.pod.grpc.hospital.waitingroom.WaitingRoomServiceGrpc;
import ar.edu.itba.pod.grpc.hospital.waitingroom.WaitingRoomServiceGrpc.WaitingRoomServiceBlockingStub;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

public class WaitingRoomClient {

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ChannelBuilder.buildChannel();

        final String action = System.getProperty("action", "");
        final String name = System.getProperty("patient", "");
        final String level = System.getProperty("level", "");

        if (action.isEmpty()) {
            System.out.println("Action is required");
            return;
        }

        if (name.isEmpty()) {
            System.out.println("Patient name is required");
            return;
        }

        try {
            WaitingRoomServiceBlockingStub blockingStub = WaitingRoomServiceGrpc.newBlockingStub(channel);

            switch (action) {
                case "addPatient" -> {
                    if (level.isEmpty()) {
                        System.out.println("Patient level is required");
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
                        final Patient patient = blockingStub.addPatient(Patient.newBuilder().setName(name).setLevel(levelNumber).build());
                        System.out.printf("Patient %s (%d) is in the waiting room\n", patient.getName(), patient.getLevel());
                    } catch (StatusRuntimeException e) {
                        System.out.println(e.getStatus().getDescription());
                    }
                }
                case "updateLevel" -> {
                    if (level.isEmpty()) {
                        System.out.println("Patient level is required");
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
                        final Patient patient = blockingStub.updateLevel(Patient.newBuilder().setName(name).setLevel(levelNumber).build());
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
                default -> System.out.println("Invalid action");
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

}
