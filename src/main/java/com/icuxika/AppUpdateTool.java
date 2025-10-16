package com.icuxika;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AppUpdateTool {

    static void main(String[] args) {
        Path target;
        try {
            Path jarPath = Path.of(AppUpdateTool.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (jarPath.toString().contains("classes")) {
                target = Path.of(jarPath.toFile().getParent()).resolve("buildImage").resolve("JavaFXSample");
            } else {
                target = Path.of(jarPath.toFile().getParentFile().getParent());
            }
            System.out.println(target);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try (var files = Files.walk(target)) {
            List<FileInfo> fileInfoList = files
                    .filter(p -> !p.toString().contains("runtime"))
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        Path relativePath = target.relativize(path);
                        return new FileInfo(relativePath.toString(), getFileHash(path), getFileSize(path));
                    })
                    .toList();

            UpdateIndex updateIndex = new UpdateIndex("1.0.0", fileInfoList);

            Gson gson = new Gson();
            String json = gson.toJson(updateIndex);
            Files.writeString(target.resolve("update-index.json"), json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFileHash(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[1024 * 8];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static long getFileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private UpdateResult compareUpdateIndex(UpdateIndex local, UpdateIndex remote) {
        List<FileInfo> added = new ArrayList<>();
        List<FileInfo> updated = new ArrayList<>();
        List<FileInfo> deleted = new ArrayList<>();

        int result = compareVersion(local.version, remote.version);
        if (result >= 0) {
            return new UpdateResult(added, updated, deleted);
        }

        Map<String, FileInfo> localMap = local.files().stream().collect(Collectors.toMap(FileInfo::path, Function.identity()));
        Map<String, FileInfo> remoteMap = remote.files().stream().collect(Collectors.toMap(FileInfo::path, Function.identity()));

        for (var entry : remoteMap.entrySet()) {
            String path = entry.getKey();
            FileInfo remoteFileInfo = entry.getValue();
            FileInfo localFileInfo = localMap.get(path);
            if (localFileInfo == null) {
                added.add(remoteFileInfo);
            } else if (!Objects.equals(localFileInfo.hash, remoteFileInfo.hash)) {
                updated.add(remoteFileInfo);
            }
        }

        for (var entry : localMap.entrySet()) {
            if (!remoteMap.containsKey(entry.getKey())) {
                deleted.add(entry.getValue());
            }
        }

        return new UpdateResult(added, updated, deleted);
    }

    private int compareVersion(String v1, String v2) {
        String[] v1Parts = v1.split("\\.");
        String[] v2Parts = v2.split("\\.");
        int length = Math.max(v1Parts.length, v2Parts.length);
        for (int i = 0; i < length; i++) {
            int v1Part = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
            int v2Part = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;
            if (v1Part != v2Part) {
                return Integer.compare(v1Part, v2Part);
            }
        }
        return 0;
    }

    private record FileInfo(String path, String hash, long size) {
    }

    private record UpdateIndex(String version, List<FileInfo> files) {
    }

    private record UpdateResult(List<FileInfo> added, List<FileInfo> updated, List<FileInfo> deleted) {
    }
}
