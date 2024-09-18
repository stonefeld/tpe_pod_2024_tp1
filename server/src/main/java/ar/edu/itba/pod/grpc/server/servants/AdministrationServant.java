package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.administration.AdministrationServiceGrpc;
import ar.edu.itba.pod.grpc.server.repositories.RoomRepository;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class AdministrationServant extends AdministrationServiceGrpc.AdministrationServiceImplBase {

    private final RoomRepository roomRepository;

    public AdministrationServant(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public void addRoom(Empty request, StreamObserver<Room> responseObserver) {
        responseObserver.onNext(roomRepository.addRoom());
        responseObserver.onCompleted();
    }

}
