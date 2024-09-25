package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.Availability;
import ar.edu.itba.pod.grpc.hospital.Doctor;
import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.administration.AdministrationServiceGrpc.AdministrationServiceImplBase;
import ar.edu.itba.pod.grpc.hospital.administration.DoctorAvailabilityUpdate;
import ar.edu.itba.pod.grpc.hospital.administration.DoctorCreation;
import ar.edu.itba.pod.grpc.hospital.doctorpager.Event;
import ar.edu.itba.pod.grpc.hospital.doctorpager.Type;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorDoesNotExistException;
import ar.edu.itba.pod.grpc.server.exceptions.InvalidLevelException;
import ar.edu.itba.pod.grpc.server.repositories.DoctorRepository;
import ar.edu.itba.pod.grpc.server.repositories.EventRepository;
import ar.edu.itba.pod.grpc.server.repositories.RoomRepository;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.List;

public class AdministrationServant extends AdministrationServiceImplBase {

    private final RoomRepository roomRepository;
    private final DoctorRepository doctorRepository;
    private final EventRepository eventRepository;

    public AdministrationServant(RoomRepository roomRepository, DoctorRepository doctorRepository, EventRepository eventRepository) {
        this.roomRepository = roomRepository;
        this.doctorRepository = doctorRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void addRoom(Empty request, StreamObserver<Room> responseObserver) {
        responseObserver.onNext(roomRepository.addRoom());
        responseObserver.onCompleted();
    }

    @Override
    public void addDoctor(DoctorCreation request, StreamObserver<Doctor> responseObserver) {
        try {
            responseObserver.onNext(doctorRepository.addDoctor(request.getName(), request.getLevel()));
            responseObserver.onCompleted();
        } catch (InvalidLevelException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid level")
                    .asRuntimeException());
        } catch (DoctorAlreadyExistsException e) {
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("Doctor already exists")
                    .asRuntimeException());
        }
    }

    @Override
    public void setDoctor(DoctorAvailabilityUpdate request, StreamObserver<Doctor> responseObserver) {
        if (!List.of(Availability.AVAILABILITY_AVAILABLE, Availability.AVAILABILITY_UNAVAILABLE).contains(request.getAvailability())) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid availability")
                    .asRuntimeException());
            return;
        }

        try {
            Doctor d = doctorRepository.checkDoctor(request.getDoctorName());
            if (d.getAvailability().equals(Availability.AVAILABILITY_ATTENDING)) {
                responseObserver.onError(Status.FAILED_PRECONDITION
                        .withDescription("Doctor is attending")
                        .asRuntimeException());
                return;
            }
        } catch (DoctorDoesNotExistException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Doctor does not exist")
                    .asRuntimeException());
        }

        Doctor doctor = doctorRepository.setDoctorAvailability(request.getDoctorName(), request.getAvailability());
        eventRepository.addEvent(doctor.getName(), Event.newBuilder()
                .setType(Type.AVAILABILITY)
                .setDoctor(doctor)
                .build());
        responseObserver.onNext(doctor);
        responseObserver.onCompleted();
    }

    @Override
    public void checkDoctor(StringValue request, StreamObserver<Doctor> responseObserver) {
        try {
            responseObserver.onNext(doctorRepository.checkDoctor(request.getValue()));
            responseObserver.onCompleted();
        } catch (DoctorDoesNotExistException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Doctor does not exist")
                    .asRuntimeException());
        }
    }


}
