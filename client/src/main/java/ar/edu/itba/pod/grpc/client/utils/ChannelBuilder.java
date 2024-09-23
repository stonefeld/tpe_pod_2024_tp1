package ar.edu.itba.pod.grpc.client.utils;

import io.grpc.ManagedChannel;

public class ChannelBuilder {

    public static ManagedChannel buildChannel() {
        final String serverAddress = System.getProperty("serverAddress", "");
        if (serverAddress.isEmpty()) {
            System.out.println("Server address is required");
            System.exit(1);
        }

        final String[] serverAddressParts = serverAddress.split(":");
        final String ip = serverAddressParts[0];
        final int port = Integer.parseInt(serverAddressParts[1]);

        return io.grpc.ManagedChannelBuilder.forAddress(ip, port)
                .usePlaintext()
                .build();
    }

}
