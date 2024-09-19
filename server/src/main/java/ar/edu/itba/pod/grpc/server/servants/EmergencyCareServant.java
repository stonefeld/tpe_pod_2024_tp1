package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.Doctor;
import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.Treatment;
import ar.edu.itba.pod.grpc.hospital.emergencycare.EmergencyCareServiceGrpc.EmergencyCareServiceImplBase;
import ar.edu.itba.pod.grpc.hospital.emergencycare.MultiTreatment;
import ar.edu.itba.pod.grpc.hospital.emergencycare.TreatmentEnding;
import ar.edu.itba.pod.grpc.hospital.emergencycare.TreatmentRoom;
import ar.edu.itba.pod.grpc.server.repositories.DoctorRepository;
import ar.edu.itba.pod.grpc.server.repositories.PatientRepository;
import ar.edu.itba.pod.grpc.server.repositories.RoomRepository;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class EmergencyCareServant extends EmergencyCareServiceImplBase {

    private final RoomRepository roomRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public EmergencyCareServant(RoomRepository roomRepository, PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.roomRepository = roomRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public void carePatient(TreatmentRoom request, StreamObserver<Room> responseObserver) {
        // TODO: chequear caso de error
        Doctor doctor = null;
        Patient patient = null;

        while (doctor == null) {
            patient = patientRepository.getNextPatient();
            doctor = doctorRepository.getDoctorForLevel(patient.getLevel());
        }

        responseObserver.onNext(roomRepository.updateRoom(request.getRoomNumber(), patient, doctor));
        responseObserver.onCompleted();
    }

    @Override
    public void careAllPatients(Empty request, StreamObserver<MultiTreatment> responseObserver) {
        super.careAllPatients(request, responseObserver);
    }

    @Override
    public void dischargePatient(TreatmentEnding request, StreamObserver<Treatment> responseObserver) {
        super.dischargePatient(request, responseObserver);
    }
}
