package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.query.QueryServiceGrpc.QueryServiceImplBase;
import ar.edu.itba.pod.grpc.hospital.query.Rooms;
import ar.edu.itba.pod.grpc.hospital.query.WaitingPatients;
import ar.edu.itba.pod.grpc.server.repositories.PatientRepository;
import ar.edu.itba.pod.grpc.server.repositories.RoomRepository;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class QueryServant extends QueryServiceImplBase {

    private final RoomRepository roomRepository;
    private final PatientRepository patientRepository;

    public QueryServant(RoomRepository roomRepository, PatientRepository patientRepository) {
        this.roomRepository = roomRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public void queryRooms(Empty request, StreamObserver<Rooms> responseObserver) {
        responseObserver.onNext(Rooms.newBuilder().addAllRooms(roomRepository.getRooms()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryWaitingRoom(Empty request, StreamObserver<WaitingPatients> responseObserver) {
        responseObserver.onNext(WaitingPatients.newBuilder().addAllPatients(patientRepository.getPatients()).build());
        responseObserver.onCompleted();
    }

}
