package ar.edu.itba.pod.grpc.server.exceptions;

public class DoctorNotRegisteredException extends RuntimeException {

    public DoctorNotRegisteredException(String message) {
        super(message);
    }

}
