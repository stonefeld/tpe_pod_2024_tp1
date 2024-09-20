package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.Treatments;
import ar.edu.itba.pod.grpc.hospital.query.QueryServiceGrpc.QueryServiceImplBase;
import ar.edu.itba.pod.grpc.hospital.query.WaitingPatients;
import ar.edu.itba.pod.grpc.server.repositories.PatientRepository;
import ar.edu.itba.pod.grpc.server.repositories.RoomRepository;
import ar.edu.itba.pod.grpc.server.repositories.TreatmentRepository;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class QueryServant extends QueryServiceImplBase {

    private final TreatmentRepository treatmentRepository;
    private final PatientRepository patientRepository;

    public QueryServant(TreatmentRepository treatmentRepository, PatientRepository patientRepository) {
        this.treatmentRepository = treatmentRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public void queryRooms(Empty request, StreamObserver<Treatments> responseObserver) {
        responseObserver.onNext(Treatments.newBuilder().addAllTreatments(treatmentRepository.getCurrentTreatments()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryWaitingRoom(Empty request, StreamObserver<WaitingPatients> responseObserver) {
        responseObserver.onNext(WaitingPatients.newBuilder().addAllPatients(patientRepository.getPatients()).build());
        responseObserver.onCompleted();
    }

}
