package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.Doctor;
import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.administration.AdministrationServiceGrpc.AdministrationServiceImplBase;
import ar.edu.itba.pod.grpc.hospital.administration.DoctorAvailabilityUpdate;
import ar.edu.itba.pod.grpc.hospital.administration.DoctorCreation;
import ar.edu.itba.pod.grpc.server.repositories.DoctorRepository;
import ar.edu.itba.pod.grpc.server.repositories.RoomRepository;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;

public class AdministrationServant extends AdministrationServiceImplBase {

    private final RoomRepository roomRepository;
    private final DoctorRepository doctorRepository;

    public AdministrationServant(RoomRepository roomRepository, DoctorRepository doctorRepository) {
        this.roomRepository = roomRepository;
        this.doctorRepository = doctorRepository;
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
        responseObserver.onNext(doctorRepository.setDoctorAvailability(request.getDoctorName(), request.getAvailability()));
        responseObserver.onCompleted();
    }

    @Override
    public void checkDoctor(StringValue request, StreamObserver<Doctor> responseObserver) {
        responseObserver.onNext(doctorRepository.checkDoctor(request.getValue()));
        responseObserver.onCompleted();
    }


}
