package com.icuxika.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public record UpdateResult(List<FileInfo> added, List<FileInfo> updated, List<FileInfo> deleted, String version) {

    public static UpdateResult compareUpdateIndex(UpdateIndex local, UpdateIndex remote) {
        List<FileInfo> added = new ArrayList<>();
        List<FileInfo> updated = new ArrayList<>();
        List<FileInfo> deleted = new ArrayList<>();

        int result = compareVersion(local.version(), remote.version());
        if (result >= 0) {
            return new UpdateResult(added, updated, deleted, remote.version());
        }

        Map<String, FileInfo> localMap = local.files().stream().collect(Collectors.toMap(FileInfo::path, Function.identity()));
        Map<String, FileInfo> remoteMap = remote.files().stream().collect(Collectors.toMap(FileInfo::path, Function.identity()));

        for (var entry : remoteMap.entrySet()) {
            String path = entry.getKey();
            FileInfo remoteFileInfo = entry.getValue();
            FileInfo localFileInfo = localMap.get(path);
            if (localFileInfo == null) {
                added.add(remoteFileInfo);
            } else if (!Objects.equals(localFileInfo.hash(), remoteFileInfo.hash())) {
                updated.add(remoteFileInfo);
            }
        }

        for (var entry : localMap.entrySet()) {
            if (!remoteMap.containsKey(entry.getKey())) {
                deleted.add(entry.getValue());
            }
        }

        return new UpdateResult(added, updated, deleted, remote.version());
    }

    private static int compareVersion(String v1, String v2) {
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
}
