package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.waitingroom.WaitingRoomServiceGrpc.WaitingRoomServiceImplBase;
import ar.edu.itba.pod.grpc.server.repositories.PatientRepository;
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

}
