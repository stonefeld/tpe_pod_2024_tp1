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
        ManagedChannel channel = ChannelBuilder.buildChannel();

        final String action = System.getProperty("action");
        final String doctorName = System.getProperty("doctor", "");

        if (doctorName.isEmpty()) {
            System.out.println("Doctor name is required");
            return;
        }

        try {
            DoctorPagerServiceGrpc.DoctorPagerServiceBlockingStub stub = DoctorPagerServiceGrpc.newBlockingStub(channel);
            DoctorRegistration request = DoctorRegistration.newBuilder().setName(doctorName).build();

            switch (action) {
                case "register" -> {
                    Iterator<Event> events;

                    try {
                        events = stub.register(request);
                        while (events.hasNext()) {
                            Event event = events.next();

                            switch (event.getType()) {
                                case REGISTER -> System.out.printf(
                                        "Doctor %s (%d) registered successfully for pager\n",
                                        event.getDoctor().getName(),
                                        event.getDoctor().getLevel()
                                );
                                case UNREGISTER -> System.out.printf(
                                        "Doctor %s (%d) unregistered successfully from pager\n",
                                        event.getDoctor().getName(),
                                        event.getDoctor().getLevel()
                                );
                                case AVAILABILITY -> System.out.printf(
                                        "Doctor %s (%d) is %s\n",
                                        event.getDoctor().getName(),
                                        event.getDoctor().getLevel(),
                                        event.getDoctor().getAvailability().name()
                                );
                                case TREATMENT -> System.out.printf(
                                        "Patient %s (%d) and Doctor %s (%d) are now in Room #%d\n",
                                        event.getTreatment().getPatient().getName(),
                                        event.getTreatment().getPatient().getLevel(),
                                        event.getTreatment().getDoctor().getName(),
                                        event.getTreatment().getDoctor().getLevel(),
                                        event.getTreatment().getRoom().getNumber()
                                );
                                case DISCHARGE -> System.out.printf(
                                        "Patient %s (%d) has been discharged from Doctor %s (%d) and the Room #%d is now Free\n",
                                        event.getTreatment().getPatient().getName(),
                                        event.getTreatment().getPatient().getLevel(),
                                        event.getTreatment().getDoctor().getName(),
                                        event.getTreatment().getDoctor().getLevel(),
                                        event.getTreatment().getRoom().getNumber()
                                );
                            }
                        }
                    } catch (StatusRuntimeException e) {
                        System.out.println(e.getStatus().getDescription());
                    }
                }
                case "unregister" -> {
                    try {
                        Event event = stub.unregister(request);
                        if (event.getType().equals(Type.UNREGISTER)) {
                            System.out.printf(
                                    "Doctor %s (%d) unregistered successfully from pager\n",
                                    event.getDoctor().getName(),
                                    event.getDoctor().getLevel()
                            );
                        }
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
