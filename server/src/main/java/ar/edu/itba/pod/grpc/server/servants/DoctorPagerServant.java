package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.Doctor;
import ar.edu.itba.pod.grpc.hospital.doctorpager.DoctorPagerServiceGrpc.DoctorPagerServiceImplBase;
import ar.edu.itba.pod.grpc.hospital.doctorpager.DoctorRegistration;
import ar.edu.itba.pod.grpc.hospital.doctorpager.Event;
import ar.edu.itba.pod.grpc.hospital.doctorpager.Type;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorNotRegisteredException;
import ar.edu.itba.pod.grpc.server.repositories.EventRepository;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DoctorPagerServant extends DoctorPagerServiceImplBase {

    Logger logger = LoggerFactory.getLogger(DoctorPagerServant.class);

    private final EventRepository eventRepository;

    public DoctorPagerServant(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void register(DoctorRegistration request, StreamObserver<Event> responseObserver) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        eventRepository.registerDoctor(request.getName());
        eventRepository.addEvent(request.getName(), Event.newBuilder().setType(Type.REGISTER).build());

        scheduler.scheduleAtFixedRate(() -> {
            try {
                Event event = eventRepository.getEvent(request.getName());
                responseObserver.onNext(event);
            } catch (DoctorNotRegisteredException e) {
                responseObserver.onCompleted();
            }
        }, 0, 250, TimeUnit.MILLISECONDS);
    }

    @Override
    public void unregister(DoctorRegistration request, StreamObserver<Event> responseObserver) {
        Event event = Event.newBuilder().setType(Type.UNREGISTER).build();
        eventRepository.addEvent(request.getName(), event);

        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }

        eventRepository.unregisterDoctor(request.getName());
        responseObserver.onNext(event);
        responseObserver.onCompleted();
    }
}
