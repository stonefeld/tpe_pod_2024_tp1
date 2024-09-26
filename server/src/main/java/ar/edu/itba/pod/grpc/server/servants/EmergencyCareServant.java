package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.hospital.*;
import ar.edu.itba.pod.grpc.hospital.doctorpager.Event;
import ar.edu.itba.pod.grpc.hospital.doctorpager.Type;
import ar.edu.itba.pod.grpc.hospital.emergencycare.EmergencyCareServiceGrpc.EmergencyCareServiceImplBase;
import ar.edu.itba.pod.grpc.hospital.emergencycare.TreatmentEnding;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorDoesNotExistException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientDoesNotExistException;
import ar.edu.itba.pod.grpc.server.exceptions.RoomDoesNotExistException;
import ar.edu.itba.pod.grpc.server.repositories.*;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

public class EmergencyCareServant extends EmergencyCareServiceImplBase {

    private final RoomRepository roomRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TreatmentRepository treatmentRepository;
    private final EventRepository eventRepository;

    public EmergencyCareServant(RoomRepository roomRepository, PatientRepository patientRepository, DoctorRepository doctorRepository, TreatmentRepository treatmentRepository, EventRepository eventRepository) {
        this.roomRepository = roomRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.treatmentRepository = treatmentRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void carePatient(TreatmentRoom request, StreamObserver<Treatment> responseObserver) {
        try {
            Room room = roomRepository.getRoom(request.getRoomNumber());
            if (room.getStatus().equals(Status.STATUS_OCCUPIED)) {
                responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION
                        .withDescription("Room is already occupied")
                        .asRuntimeException());
                return;
            }

            Treatment treatment = createTreatment(room);
            if (!treatment.hasDoctor() && !treatment.hasPatient()) {
                responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION
                        .withDescription("Room #" + room.getNumber() + " remains Free")
                        .asRuntimeException());
                return;
            }

            responseObserver.onNext(treatment);
            responseObserver.onCompleted();
        } catch (RoomDoesNotExistException e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Room does not exist")
                    .asRuntimeException());
        }
    }

    @Override
    public void careAllPatients(Empty request, StreamObserver<Treatments> responseObserver) {
        List<Room> rooms = roomRepository.getRooms();
        List<Treatment> treatments = new ArrayList<>();

        rooms.forEach(r -> treatments.add(createTreatment(r)));
        responseObserver.onNext(Treatments.newBuilder().addAllTreatments(treatments).build());
        responseObserver.onCompleted();
    }

    @Override
    public void dischargePatient(TreatmentEnding request, StreamObserver<Treatment> responseObserver) {
        try {
            Treatment treatment = treatmentRepository.dischargePatient(request.getRoomNumber(), request.getPatientName(), request.getDoctorName());
            roomRepository.setRoomStatus(treatment.getRoom().getNumber(), Status.STATUS_FREE);
            doctorRepository.setDoctorAvailability(treatment.getDoctor().getName(), Availability.AVAILABILITY_AVAILABLE);
            eventRepository.addEvent(treatment.getDoctor().getName(), Event.newBuilder()
                    .setType(Type.DISCHARGE)
                    .setTreatment(treatment)
                    .build());
            responseObserver.onNext(treatment);
            responseObserver.onCompleted();
        } catch (DoctorDoesNotExistException e) {
            String message;
            if (doctorRepository.doctorExists(request.getDoctorName())) {
                message = "Doctor is not attending in that room";
            } else {
                message = "Doctor does not exist";
            }
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(message)
                    .asRuntimeException());

        } catch (PatientDoesNotExistException e) {
            String message;
            if (patientRepository.patientExists(request.getPatientName())) {
                message = "Patient is not being attended by that doctor";
            } else {
                message = "Patient does not exist";
            }
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(message)
                    .asRuntimeException());
        } catch (RoomDoesNotExistException e) {
            String message;
            if (roomRepository.roomExists(request.getRoomNumber())) {
                message = "Room is not occupied by that doctor";
            } else {
                message = "Room does not exist";
            }
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(message)
                    .asRuntimeException());
        }
    }

    private Treatment createTreatment(Room room) {
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
                        eventRepository.addEvent(d.getName(), Event.newBuilder()
                                .setType(Type.TREATMENT)
                                .setTreatment(treatment)
                                .build());
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
