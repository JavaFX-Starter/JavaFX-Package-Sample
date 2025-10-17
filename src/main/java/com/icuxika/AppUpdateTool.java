package com.icuxika;

import com.google.gson.Gson;
import com.icuxika.model.FileInfo;
import com.icuxika.model.UpdateIndex;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class AppUpdateTool {

    static void main(String[] args) {
        Path target;
        String appVersion;
        try {
            Path jarPath = Path.of(AppUpdateTool.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (jarPath.toString().contains("classes")) {
                target = Path.of(jarPath.toFile().getParent()).resolve("buildImage").resolve("JavaFXSample");
            } else {
                target = Path.of(jarPath.toFile().getParentFile().getParent());
            }
            System.out.println(target);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            Document document = builder.parse(target.resolve("app").resolve(".jpackage.xml").toFile());

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            appVersion = xPath.evaluate("/jpackage-state/app-version", document);
        } catch (Exception e) {
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

            UpdateIndex updateIndex = new UpdateIndex(appVersion, fileInfoList);

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

}
