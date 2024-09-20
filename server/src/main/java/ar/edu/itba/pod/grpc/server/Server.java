package ar.edu.itba.pod.grpc.server;

import ar.edu.itba.pod.grpc.server.repositories.DoctorRepository;
import ar.edu.itba.pod.grpc.server.repositories.PatientRepository;
import ar.edu.itba.pod.grpc.server.repositories.RoomRepository;
import ar.edu.itba.pod.grpc.server.repositories.TreatmentRepository;
import ar.edu.itba.pod.grpc.server.servants.AdministrationServant;
import ar.edu.itba.pod.grpc.server.servants.QueryServant;
import ar.edu.itba.pod.grpc.server.servants.EmergencyCareServant;
import ar.edu.itba.pod.grpc.server.servants.WaitingRoomServant;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        logger.info("Server Starting ...");

        final RoomRepository roomRepository = new RoomRepository();
        final DoctorRepository doctorRepository = new DoctorRepository();
        final PatientRepository patientRepository = new PatientRepository();
        final TreatmentRepository treatmentRepository = new TreatmentRepository();

        int port = Integer.parseInt(System.getProperty("port", "50051"));

        io.grpc.Server server = ServerBuilder.forPort(port)
                .addService(new AdministrationServant(roomRepository, doctorRepository))
                .addService(new WaitingRoomServant(patientRepository))
                .addService(new EmergencyCareServant(roomRepository, patientRepository, doctorRepository, treatmentRepository))
                .addService(new QueryServant(treatmentRepository, patientRepository))
                .build();
        server.start();
        logger.info("Server started, listening on {}", port);

        server.awaitTermination();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down gRPC server since JVM is shutting down");
            server.shutdown();
            logger.info("Server shut down");
        }));
    }

}
