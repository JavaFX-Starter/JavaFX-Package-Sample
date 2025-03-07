package com.icuxika;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SingleInstanceManager {
    private static final String PORT_FILE = System.getProperty("user.home") + "/.jfxport";
    private static ServerSocket serverSocket;

    public static boolean isFirstInstance(String[] args) {
        try {
            Path portFilePath = Paths.get(PORT_FILE);
            if (Files.exists(portFilePath)) {
                String portString = new String(Files.readAllBytes(portFilePath)).trim();
                int port = Integer.parseInt(portString);
                if (tryConnect(port, args)) {
                    // 已有实例运行
                    return false;
                }
                // 删除无效记录
                Files.delete(portFilePath);
            }
            serverSocket = new ServerSocket(0);
            Files.writeString(portFilePath, String.valueOf(serverSocket.getLocalPort()),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            handleSecondInstanceArgs();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean tryConnect(int port, String[] args) {
        try (Socket socket = new Socket("127.0.0.1", port)) {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF(String.join("|||", args));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void handleSecondInstanceArgs() {
        new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try (Socket socket = serverSocket.accept()) {
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    String args = inputStream.readUTF();
                    System.out.println("收到第二个实例传来的参数: " + args);
                    URI uri = new URI(args);
                    System.out.println(uri.getScheme());
                    System.out.println(uri.getHost());
                    System.out.println(uri.getPath());
                    System.out.println(uri.getQuery());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public static void cleanup() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            Files.deleteIfExists(Paths.get(PORT_FILE));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
