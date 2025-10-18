package com.icuxika.task;

import com.google.gson.Gson;
import com.icuxika.AppUpdateTool;
import com.icuxika.model.Latest;
import com.icuxika.model.UpdateIndex;
import com.icuxika.model.UpdateResult;
import javafx.concurrent.Task;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class UpdateResultDownloadTask extends Task<UpdateResult> {

    private final Gson gson = new Gson();

    private final String baseUrl;

    public UpdateResultDownloadTask(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    protected UpdateResult call() throws Exception {
        URL latestUrl = URI.create(baseUrl + "/latest.json").toURL();
        Latest latest;
        try (InputStream inputStream = latestUrl.openStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        ) {
            latest = gson.fromJson(inputStreamReader, Latest.class);
        }

        UpdateIndex remoteUpdateIndex;
        URL url = URI.create(baseUrl + "/" + latest.version() + "/update-index.json").toURL();
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
        Path localUpdateIndexPath = target.resolve("update-index.json");
        try (InputStream inputStream = url.openStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             InputStreamReader localReader = new InputStreamReader(new FileInputStream(localUpdateIndexPath.toFile()), StandardCharsets.UTF_8)) {
            remoteUpdateIndex = gson.fromJson(inputStreamReader, UpdateIndex.class);
            UpdateIndex localUpdateIndex = gson.fromJson(localReader, UpdateIndex.class);
            return UpdateResult.compareUpdateIndex(localUpdateIndex, remoteUpdateIndex);
        }
    }

}
