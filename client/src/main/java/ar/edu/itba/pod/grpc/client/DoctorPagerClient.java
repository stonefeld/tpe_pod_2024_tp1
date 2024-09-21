package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.client.utils.ChannelBuilder;
import ar.edu.itba.pod.grpc.hospital.doctorpager.DoctorPagerServiceGrpc;
import ar.edu.itba.pod.grpc.hospital.doctorpager.DoctorRegistration;
import ar.edu.itba.pod.grpc.hospital.doctorpager.Event;
import ar.edu.itba.pod.grpc.hospital.doctorpager.Type;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class DoctorPagerClient {

    private static final Logger logger = LoggerFactory.getLogger(DoctorPagerClient.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("tpe1-g2 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");
        ManagedChannel channel = ChannelBuilder.buildChannel();

        final String action = System.getProperty("action");
        final String doctorName = System.getProperty("doctor");

        try {
            DoctorPagerServiceGrpc.DoctorPagerServiceBlockingStub stub = DoctorPagerServiceGrpc.newBlockingStub(channel);
            DoctorRegistration request = DoctorRegistration.newBuilder().setName(doctorName).build();

            switch (action) {
                case "register" -> {
                    Iterator<Event> events;

                    try {
                        logger.info("Solicito registro para Doctor {}", request.getName());
                        events = stub.register(request);
                        while (events.hasNext()) {
                            Event event = events.next();

                            switch (event.getType()) {
                                case REGISTER -> logger.info("Doctor {} registrado", doctorName);
                                case UNREGISTER -> logger.info("Doctor {} desregistrado", doctorName);
                                case AVAILABILITY -> logger.info("Doctor {} esperando paciente", doctorName);
                                case TREATMENT -> logger.info("Doctor {} atendiendo paciente", doctorName);
                                case DISCHARGE -> logger.info("Doctor {} dio de alta al paciente", doctorName);
                                default -> logger.error("Invalid event type: {}", event.getType());
                            }
                        }
                    } catch (StatusRuntimeException e) {
                        logger.error("An error occurred: {}", e.getStatus());
                    }
                }
                case "unregister" -> {
                    Event event = stub.unregister(request);
                    if (event.getType().equals(Type.UNREGISTER)) {
                        logger.info("Doctor {} desregistrado", doctorName);
                    } else {
                        logger.error("Invalid event type: {}", event.getType());
                    }
                }
                default -> logger.error("Invalid action: {}", action);
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

}
