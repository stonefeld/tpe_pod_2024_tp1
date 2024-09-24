package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.client.utils.ChannelBuilder;
import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.Treatment;
import ar.edu.itba.pod.grpc.hospital.query.QueryServiceGrpc;
import ar.edu.itba.pod.grpc.hospital.query.QueryServiceGrpc.QueryServiceBlockingStub;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class QueryClient {

    private static final Logger logger = LoggerFactory.getLogger(QueryClient.class);

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ChannelBuilder.buildChannel();

        final String action = System.getProperty("action");
        final String fileName = System.getProperty("outPath", "");

        try {
            QueryServiceBlockingStub blockingStub = QueryServiceGrpc.newBlockingStub(channel);

            switch (action) {
                case "queryRooms" -> {
                    if (fileName.isEmpty()) {
                        System.out.println("Output file is required");
                        return;
                    }

                    Iterator<Treatment> treatments;

                    try {
                        treatments = blockingStub.queryRooms(Empty.newBuilder().build());

                        String header = "Room,Status,Patient,Doctor";

                        Function<Treatment, String> csvLineMapper = treatment -> {
                            String roomNumber = String.valueOf(treatment.getRoom().getNumber());
                            String status = (!treatment.hasPatient() && !treatment.hasDoctor()) ? "Free" : "Occupied";
                            String patient = treatment.hasPatient()
                                    ? treatment.getPatient().getName() + " (" + treatment.getPatient().getLevel() + ")"
                                    : "";
                            String doctor = treatment.hasDoctor()
                                    ? treatment.getDoctor().getName() + " (" + treatment.getDoctor().getLevel() + ")"
                                    : "";
                            return String.join(",", roomNumber, status, patient, doctor);
                        };

                        writeToCSV(fileName, header, treatments, csvLineMapper);
                    } catch (IOException e) {
                        logger.error("Error writing to file: {}", e.getMessage());
                    } catch (StatusRuntimeException e) {
                        System.out.println(e.getStatus().getDescription());
                    }
                }
                case "queryWaitingRoom" -> {
                    if (fileName.isEmpty()) {
                        System.out.println("Output file is required");
                        return;
                    }

                    Iterator<Patient> patients;

                    try {
                        patients = blockingStub.queryWaitingRoom(Empty.newBuilder().build());

                        String header = "Patient,Level";
                        Function<Patient, String> csvLineMapper = patient -> {
                            String patientName = patient.getName();
                            String level = String.valueOf(patient.getLevel());
                            return String.join(",", patientName, level);
                        };

                        writeToCSV(fileName, header, patients, csvLineMapper);
                    } catch (IOException e) {
                        logger.error("Error writing to file: {}", e.getMessage());
                    } catch (StatusRuntimeException e) {
                        System.out.println(e.getStatus().getDescription());
                    }
                }
                case "queryCares" -> {
                    if (fileName.isEmpty()) {
                        System.out.println("Output file is required");
                        return;
                    }

                    Iterator<Treatment> treatments;

                    try {
                        treatments = blockingStub.queryCares(Empty.newBuilder().build());

                        String header = "Room,Patient,Doctor";

                        Function<Treatment, String> csvLineMapper = treatment -> {
                            String roomNumber = String.valueOf(treatment.getRoom().getNumber());
                            String patient = treatment.hasPatient()
                                    ? treatment.getPatient().getName() + " (" + treatment.getPatient().getLevel() + ")"
                                    : "";
                            String doctor = treatment.hasDoctor()
                                    ? treatment.getDoctor().getName() + " (" + treatment.getDoctor().getLevel() + ")"
                                    : "";
                            return String.join(",", roomNumber, patient, doctor);
                        };

                        writeToCSV(fileName, header, treatments, csvLineMapper);
                    } catch (IOException e) {
                        logger.error("Error writing to file: {}", e.getMessage());
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

    private static <T> void writeToCSV(String fileName, String header, Iterator<T> dataList, Function<T, String> csvLineMapper) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(header);

        while (dataList.hasNext()) {
            lines.add(csvLineMapper.apply(dataList.next()));
        }

        Files.write(Paths.get(fileName), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

}

