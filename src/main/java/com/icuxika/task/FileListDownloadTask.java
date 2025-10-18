package com.icuxika.task;

import javafx.concurrent.Task;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileListDownloadTask extends Task<Void> {

    private final String baseUrl;
    private final String version;
    private final List<String> fileUrlList;
    private final Path targetPath;

    public FileListDownloadTask(String baseUrl, String version, List<String> fileUrlList, Path targetPath) {
        this.baseUrl = baseUrl;
        this.version = version;
        this.fileUrlList = fileUrlList;
        this.targetPath = targetPath;
    }

    @Override
    protected Void call() throws Exception {
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath);
        }
        int size = fileUrlList.size();
        int index = 0;
        for (String fileUrl : fileUrlList) {
            index++;
            updateMessage(String.format("正在下载文件 %d/%d：%s", index, size, fileUrl));

            URL url = URI.create(baseUrl + "/" + version + "/" + fileUrl).toURL();
            String fileName = Paths.get(url.getPath()).getFileName().toString();
            Path localFile = targetPath.resolve(version).resolve(fileUrl);
            System.out.printf("下载文件 %s[%s] 到 %s%n", fileName, url, localFile);
            if (!Files.exists(localFile.getParent())) {
                Files.createDirectories(localFile.getParent());
            }
            try (InputStream inputStream = url.openStream();
                 OutputStream outputStream = Files.newOutputStream(localFile)) {
                byte[] buffer = new byte[1024 * 8];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            updateProgress(index, size);
        }

        updateMessage("所有文件下载完成");
        updateProgress(100, 100);
        return null;
    }

}
