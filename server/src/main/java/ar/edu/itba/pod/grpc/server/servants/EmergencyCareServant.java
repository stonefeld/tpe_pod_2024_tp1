package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.*;
import ar.edu.itba.pod.grpc.hospital.emergencycare.EmergencyCareServiceGrpc.EmergencyCareServiceImplBase;
import ar.edu.itba.pod.grpc.server.repositories.DoctorRepository;
import ar.edu.itba.pod.grpc.server.repositories.PatientRepository;
import ar.edu.itba.pod.grpc.server.repositories.RoomRepository;
import ar.edu.itba.pod.grpc.server.repositories.TreatmentRepository;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

public class EmergencyCareServant extends EmergencyCareServiceImplBase {

    private final RoomRepository roomRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TreatmentRepository treatmentRepository;

    public EmergencyCareServant(RoomRepository roomRepository, PatientRepository patientRepository, DoctorRepository doctorRepository, TreatmentRepository treatmentRepository) {
        this.roomRepository = roomRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.treatmentRepository = treatmentRepository;
    }

    @Override
    public void carePatient(TreatmentRoom request, StreamObserver<Treatment> responseObserver) {
        // TODO: chequear si la ROOM ya esta usada
        // TODO: chequear caso de error

        Room room = roomRepository.getRoom(request.getRoomNumber());
        responseObserver.onNext(createTreatment(room));
        responseObserver.onCompleted();
    }

    @Override
    public void careAllPatients(Empty request, StreamObserver<Treatments> responseObserver) {
        List<Room> rooms = roomRepository.getRooms();
        List<Treatment> treatments = new ArrayList<>();

        rooms.forEach(r -> treatments.add(createTreatment(r)));
        responseObserver.onNext(Treatments.newBuilder().addAllTreatments(treatments).build());
        responseObserver.onCompleted();
    }

//    @Override
//    public void dischargePatient(TreatmentEnding request, StreamObserver<Room> responseObserver) {
//         TODO: chequear los parametros
//
//        Room room = roomRepository.getRoom();
//
//    }

    private Treatment createTreatment(Room room) {
        // TODO: se re coge la concurrencia
        if (room.getStatus().equals(Status.STATUS_FREE)) {
            List<Patient> waitingPatients = patientRepository.getFirstPatientFromEveryLevel();
            List<Doctor> availableDoctors = doctorRepository.getAvailableDoctors();

            for (Patient p : waitingPatients) {
                for (Doctor d : availableDoctors) {
                    if (d.getLevel() >= p.getLevel()) {
                        Treatment treatment = Treatment.newBuilder()
                                .setRoom(roomRepository.setRoomStatus(room.getNumber(), Status.STATUS_OCCUPIED))
                                .setDoctor(doctorRepository.setDoctorAvailability(d.getName(), Availability.AVAILABILITY_ATTENDING))
                                .setPatient(patientRepository.attendPatient(p))
                                .build();
                        treatmentRepository.addTreatment(treatment);
                        return treatment;
                    }
                }
            }
        }

        return Treatment.newBuilder()
                .setRoom(room)
                .build();
    }

}
