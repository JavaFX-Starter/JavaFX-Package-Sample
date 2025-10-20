package com.icuxika.task;

import com.google.gson.Gson;
import com.icuxika.AppUpdateTool;
import com.icuxika.model.Latest;
import com.icuxika.model.UpdateIndex;
import com.icuxika.model.UpdateResult;
import javafx.concurrent.Task;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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

        try (
                InputStream inputStream = url.openStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        ) {
            remoteUpdateIndex = gson.fromJson(inputStreamReader, UpdateIndex.class);
            UpdateIndex localUpdateIndex = AppUpdateTool.generateUpdateIndex();
            return UpdateResult.compareUpdateIndex(localUpdateIndex, remoteUpdateIndex);
        }
    }

}
