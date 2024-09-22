package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.waitingroom.PatientQueueInfo;
import ar.edu.itba.pod.grpc.hospital.waitingroom.WaitingRoomServiceGrpc.WaitingRoomServiceImplBase;
import ar.edu.itba.pod.grpc.server.exceptions.InvalidLevelException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientDoesNotExistException;
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
        try {
            responseObserver.onNext(patientRepository.addPatient(request.getName(), request.getLevel()));
            responseObserver.onCompleted();
        } catch (PatientAlreadyExistsException e) {
            responseObserver.onError(io.grpc.Status.ALREADY_EXISTS
                    .withDescription("Patient already exists")
                    .asRuntimeException());
        } catch (InvalidLevelException e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Invalid level")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateLevel(Patient request, StreamObserver<Patient> responseObserver) {
        try {
            responseObserver.onNext(patientRepository.updateLevel(request.getName(), request.getLevel()));
            responseObserver.onCompleted();
        } catch (PatientDoesNotExistException e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Patient does not exist")
                    .asRuntimeException());
        } catch (InvalidLevelException e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Invalid level")
                    .asRuntimeException());
        }
    }

    @Override
    public void checkPatient(StringValue request, StreamObserver<PatientQueueInfo> responseObserver) {
        try {
            responseObserver.onNext(patientRepository.checkPatient(request.getValue()));
            responseObserver.onCompleted();
        } catch (PatientDoesNotExistException e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Patient does not exist")
                    .asRuntimeException());
        }
    }
}
