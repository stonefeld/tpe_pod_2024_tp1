package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.client.utils.ChannelBuilder;
import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.Treatment;
import ar.edu.itba.pod.grpc.hospital.Treatments;
import ar.edu.itba.pod.grpc.hospital.query.QueryServiceGrpc;
import ar.edu.itba.pod.grpc.hospital.query.QueryServiceGrpc.QueryServiceBlockingStub;
import ar.edu.itba.pod.grpc.hospital.query.WaitingPatients;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class QueryClient {

    private static final Logger logger = LoggerFactory.getLogger(QueryClient.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("tpe1-g2 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");
        ManagedChannel channel = ChannelBuilder.buildChannel();

        final String action = System.getProperty("action");
        final String fileName;

        try {
            QueryServiceBlockingStub blockingStub = QueryServiceGrpc.newBlockingStub(channel);

            switch (action) {
                case "queryRooms" -> {
                    fileName = System.getProperty("outPath");
                    final Treatments treatments = blockingStub.queryRooms(Empty.newBuilder().build());

                    try {
                        writeRoomsToCSV(fileName, treatments);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                case "queryWaitingRoom" -> {
                    fileName = System.getProperty("outPath");
                    final WaitingPatients patients = blockingStub.queryWaitingRoom(Empty.newBuilder().build());

                    try {
                        writePatientsToCSV(fileName, patients);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                default -> logger.error("Invalid action: {}", action);
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    public static void writeRoomsToCSV(String fileName, Treatments treatments) throws IOException {
        List<String> lines = new ArrayList<>();

        lines.add("Room,Status,Patient,Doctor");

        for (Treatment treatment : treatments.getTreatmentsList()) {
            String roomNumber = String.valueOf(treatment.getRoom().getNumber());
            String status = (!treatment.hasPatient() && !treatment.hasDoctor()) ? "Free" : "Occupied";
            String patient = treatment.hasPatient()
                    ? treatment.getPatient().getName() + " (" + treatment.getPatient().getLevel() + ")"
                    : "";
            String doctor = treatment.hasDoctor()
                    ? treatment.getDoctor().getName() + " (" + treatment.getDoctor().getLevel() + ")"
                    : "";

            String csvLine = String.join(",", roomNumber, status, patient, doctor);
            lines.add(csvLine);
        }

        Files.write(Paths.get(fileName), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // TODO: Modularizar
    public static void writePatientsToCSV(String fileName, WaitingPatients patients) throws IOException {
        List<String> lines = new ArrayList<>();

        lines.add("Patient,Level");

        for (Patient patient : patients.getPatientsList()) {
            String roomNumber = patient.getName();
            String level = String.valueOf(patient.getLevel());

            String csvLine = String.join(",", roomNumber, level);
            lines.add(csvLine);
        }

        Files.write(Paths.get(fileName), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}

