package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.Treatment;
import ar.edu.itba.pod.grpc.hospital.Treatments;
import ar.edu.itba.pod.grpc.hospital.query.QueryServiceGrpc.QueryServiceImplBase;
import ar.edu.itba.pod.grpc.hospital.query.WaitingPatients;
import ar.edu.itba.pod.grpc.server.repositories.PatientRepository;
import ar.edu.itba.pod.grpc.server.repositories.RoomRepository;
import ar.edu.itba.pod.grpc.server.repositories.TreatmentRepository;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

import java.util.List;

public class QueryServant extends QueryServiceImplBase {

    private final TreatmentRepository treatmentRepository;
    private final PatientRepository patientRepository;
    private final RoomRepository roomRepository;

    public QueryServant(TreatmentRepository treatmentRepository, PatientRepository patientRepository, RoomRepository roomRepository) {
        this.treatmentRepository = treatmentRepository;
        this.patientRepository = patientRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public void queryRooms(Empty request, StreamObserver<Treatments> responseObserver) {
        List<Room> rooms = roomRepository.getRooms();
        if (rooms.isEmpty()) {
            responseObserver.onError(io.grpc.Status.UNAVAILABLE
                    .withDescription("No rooms available")
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(Treatments.newBuilder().addAllTreatments(treatmentRepository.getTreatmentsByRoom(rooms)).build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryWaitingRoom(Empty request, StreamObserver<WaitingPatients> responseObserver) {
        List<Patient> patients = patientRepository.getPatients();
        if (patients.isEmpty()) {
            responseObserver.onError(io.grpc.Status.UNAVAILABLE
                    .withDescription("No patients in the waiting room")
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(WaitingPatients.newBuilder().addAllPatients(patients).build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryCares(Empty request, StreamObserver<Treatments> responseObserver) {
        List<Treatment> treatments = treatmentRepository.getCompletedTreatments();
        if (treatments.isEmpty()) {
            responseObserver.onError(io.grpc.Status.UNAVAILABLE
                    .withDescription("No treatment records")
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(Treatments.newBuilder().addAllTreatments(treatments).build());
        responseObserver.onCompleted();
    }

}
