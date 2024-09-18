package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.query.QueryServiceGrpc.QueryServiceImplBase;
import ar.edu.itba.pod.grpc.hospital.query.Rooms;
import ar.edu.itba.pod.grpc.server.repositories.PatientRepository;
import ar.edu.itba.pod.grpc.server.repositories.RoomRepository;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class QueryServant extends QueryServiceImplBase {

    private final RoomRepository roomRepository;

    public QueryServant(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public void queryRooms(Empty request, StreamObserver<Rooms> responseObserver) {
        responseObserver.onNext(Rooms.newBuilder().addAllRooms(roomRepository.getRooms()).build());
        responseObserver.onCompleted();
    }

}
