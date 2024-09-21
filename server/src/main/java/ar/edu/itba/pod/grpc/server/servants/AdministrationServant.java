package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.Doctor;
import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.administration.AdministrationServiceGrpc.AdministrationServiceImplBase;
import ar.edu.itba.pod.grpc.hospital.administration.DoctorAvailabilityUpdate;
import ar.edu.itba.pod.grpc.hospital.administration.DoctorCreation;
import ar.edu.itba.pod.grpc.hospital.doctorpager.Event;
import ar.edu.itba.pod.grpc.hospital.doctorpager.Type;
import ar.edu.itba.pod.grpc.server.repositories.DoctorRepository;
import ar.edu.itba.pod.grpc.server.repositories.EventRepository;
import ar.edu.itba.pod.grpc.server.repositories.RoomRepository;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;

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
        responseObserver.onNext(doctorRepository.addDoctor(request.getName(), request.getLevel()));
        responseObserver.onCompleted();
    }

    @Override
    public void setDoctor(DoctorAvailabilityUpdate request, StreamObserver<Doctor> responseObserver) {
        Doctor doctor = doctorRepository.setDoctorAvailability(request.getDoctorName(), request.getAvailability());
        // TODO: chequear que se haya hecho correcto
        eventRepository.addEvent(doctor.getName(), Event.newBuilder().setType(Type.AVAILABILITY).build());
        responseObserver.onNext(doctor);
        responseObserver.onCompleted();
    }

    @Override
    public void checkDoctor(StringValue request, StreamObserver<Doctor> responseObserver) {
        responseObserver.onNext(doctorRepository.checkDoctor(request.getValue()));
        responseObserver.onCompleted();
    }


}
