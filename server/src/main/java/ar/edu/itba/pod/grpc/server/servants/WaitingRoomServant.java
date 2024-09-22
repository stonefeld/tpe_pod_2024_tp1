package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.waitingroom.PatientQueueInfo;
import ar.edu.itba.pod.grpc.hospital.waitingroom.WaitingRoomServiceGrpc.WaitingRoomServiceImplBase;
import ar.edu.itba.pod.grpc.server.repositories.PatientRepository;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;

public class WaitingRoomServant extends WaitingRoomServiceImplBase {

    private final PatientRepository patientRepository;

    public WaitingRoomServant(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public void addPatient(Patient request, StreamObserver<Patient> responseObserver) {
        responseObserver.onNext(patientRepository.addPatient(request.getName(), request.getLevel()));
        responseObserver.onCompleted();
    }

    @Override
    public void updateLevel(Patient request, StreamObserver<Patient> responseObserver) {
        responseObserver.onNext(patientRepository.updateLevel(request.getName(), request.getLevel()));
        responseObserver.onCompleted();
    }

    @Override
    public void checkPatient(StringValue request, StreamObserver<PatientQueueInfo> responseObserver) {
        responseObserver.onNext(patientRepository.checkPatient(request.getValue()));
        responseObserver.onCompleted();
    }
}
