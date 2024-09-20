package ar.edu.itba.pod.grpc.client.utils;

import io.grpc.ManagedChannel;

public class ChannelBuilder {

    public static ManagedChannel buildChannel() {
        final String[] serverAddress = System.getProperty("serverAddress").split(":");
        final String ip = serverAddress[0];
        final int port = Integer.parseInt(serverAddress[1]);

        return io.grpc.ManagedChannelBuilder.forAddress(ip, port)
                .usePlaintext()
                .build();
    }

}
