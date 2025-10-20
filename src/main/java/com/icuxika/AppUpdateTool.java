package com.icuxika;

import com.google.gson.Gson;
import com.icuxika.constant.SystemConstant;
import com.icuxika.model.FileInfo;
import com.icuxika.model.UpdateIndex;
import com.icuxika.util.SystemUtil;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class AppUpdateTool {

    private static final List<String> excludePath = List.of(
            "runtime", // Java运行时
            "auto-update-helper.exe", // 程序更新助手
            "index.html", // NSIS
            "readme.txt", // NSIS
            "Uninstall.exe" // NSIS
    );

    static void main(String[] args) {
        Path target = SystemUtil.getExePath();
        UpdateIndex updateIndex = generateUpdateIndex();
        Gson gson = new Gson();
        String json = gson.toJson(updateIndex);
        try {
            Files.writeString(target.resolve(SystemConstant.UPDATE_INDEX_FILE), json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static UpdateIndex generateUpdateIndex() {
        Path target = SystemUtil.getExePath();
        try (var files = Files.walk(target)) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            Document document = builder.parse(target.resolve("app").resolve(".jpackage.xml").toFile());

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            String appVersion = xPath.evaluate("/jpackage-state/app-version", document);

            List<FileInfo> fileInfoList = files
                    .filter(p -> excludePath.stream().noneMatch(k -> p.toString().contains(k)))
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        Path relativePath = target.relativize(path);
                        return new FileInfo(relativePath.toString().replace(File.separator, "/"), getFileHash(path), getFileSize(path));
                    })
                    .toList();
            return new UpdateIndex(appVersion, fileInfoList);
        } catch (Exception e) {
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
