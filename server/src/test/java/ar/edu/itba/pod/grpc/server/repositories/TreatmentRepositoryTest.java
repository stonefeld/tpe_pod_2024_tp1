package ar.edu.itba.pod.grpc.server.repositories;

import ar.edu.itba.pod.grpc.hospital.Doctor;
import ar.edu.itba.pod.grpc.hospital.Patient;
import ar.edu.itba.pod.grpc.hospital.Room;
import ar.edu.itba.pod.grpc.hospital.Treatment;
import ar.edu.itba.pod.grpc.server.exceptions.DoctorDoesNotExistException;
import ar.edu.itba.pod.grpc.server.exceptions.PatientDoesNotExistException;
import ar.edu.itba.pod.grpc.server.exceptions.RoomDoesNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TreatmentRepositoryTest   {
    private static TreatmentRepository treatmentRepository;
    @BeforeEach
    public void setUp() {
        treatmentRepository = new TreatmentRepository();
    }
    @Test
    void addTreatmentTest() {
        Room room1= Room.newBuilder().setNumber(1).build();
        Room room2= Room.newBuilder().setNumber(2).build();
        assertDoesNotThrow(() -> treatmentRepository.addTreatment(Treatment.newBuilder().setRoom(room1).build()));
        assertDoesNotThrow(() -> treatmentRepository.addTreatment(Treatment.newBuilder().setRoom(room2).build()));
        List<Room> rooms= new ArrayList<>();
        rooms.add(room1);
        rooms.add(room2);
        assertEquals(2, treatmentRepository.getTreatmentsByRoom(rooms).size());

    }
    @Test
    void dischargePatientTest() {
        Room room= Room.newBuilder().setNumber(1).build();
        Room room2= Room.newBuilder().setNumber(2).build();
        Doctor doctor= Doctor.newBuilder().setName("Martin").build();
        Patient patient= Patient.newBuilder().setName("Lucas").build();
        Doctor doctor2= Doctor.newBuilder().setName("Joselu").build();
        Patient patient2= Patient.newBuilder().setName("Martin").build();
        List<Room> rooms= new ArrayList<>();
        rooms.add(room);
        rooms.add(room2);
        Treatment treatment= Treatment.newBuilder().setRoom(room).setDoctor(doctor).setPatient(patient).build();
        Treatment treatment2= Treatment.newBuilder().setRoom(room2).setDoctor(doctor2).setPatient(patient2).build();
        treatmentRepository.addTreatment(treatment);
        treatmentRepository.addTreatment(treatment2);
        assertThrows(DoctorDoesNotExistException.class, () -> treatmentRepository.dischargePatient(1,"Lucas","Jose"));
        assertThrows(RoomDoesNotExistException.class, () -> treatmentRepository.dischargePatient(2,"Lucas","Martin"));
        assertThrows(PatientDoesNotExistException.class, () -> treatmentRepository.dischargePatient(1,"Jose","Martin"));
        assertDoesNotThrow(() -> treatmentRepository.dischargePatient(1,"Lucas","Martin"));
        assertEquals(1, treatmentRepository.getCompletedTreatments().size());
        assertDoesNotThrow(() -> treatmentRepository.dischargePatient(2,"Martin","Joselu"));
        assertEquals(2, treatmentRepository.getCompletedTreatments().size());
        assertFalse(treatmentRepository.getTreatmentsByRoom(rooms).getFirst().hasPatient());

    }


}
