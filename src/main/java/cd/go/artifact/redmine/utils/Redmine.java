/*
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cd.go.artifact.redmine.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cd.go.artifact.redmine.ConsoleLogger;

public class Redmine {

  public static final Gson GSON =
      new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).serializeNulls().create();


  private final String url;
  private final String key;
  private final String projectId;
  private final String versionId;

  public Redmine(String url, String key, String projectId, String versionId) {
    this.url = url;
    this.key = key;
    this.projectId = projectId;
    this.versionId = versionId;
  }

  public void upload(String workingDir, String sourceFile, String targetFile, ConsoleLogger console)
      throws IOException {
    for (PathMapper match : PathMapper.list(workingDir, sourceFile)) {
      AttachmentUpload upload = uploadFile(match.toFile(), sourceFile, console);
      if (upload == null || upload.upload == null || upload.upload.token == null
          || upload.upload.token.trim().isEmpty()) {
        console.error("Redmine didn't accept the file upload. Check API key, URL, artifact path...");
      } else {
        linkUploadToVersion(upload, match.remap(targetFile), console);
      }
    }
  }

  private AttachmentUpload uploadFile(File file, String sourceFile, ConsoleLogger console) throws IOException {
    String url = this.url + "/uploads.json";
    console.info(String.format("Uploading %s to %s", sourceFile, url));

    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Type", "application/octet-stream");
    connection.setRequestProperty("X-Redmine-API-Key", this.key);

    try (OutputStream output = connection.getOutputStream()) {
      Files.copy(file.toPath(), output);
    }

    InputStream response = connection.getInputStream();
    String uploadTokenJson = Redmine.getText(response);

    return Redmine.GSON.fromJson(uploadTokenJson, AttachmentUpload.class);
  }

  private void linkUploadToVersion(AttachmentUpload upload, String targetFile, ConsoleLogger console)
      throws IOException {
    String description = String.format(Locale.getDefault(), "File generated on %s",
        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));

    // Prepare JSON
    FileUpload linkToVersion = new FileUpload();
    linkToVersion.file.description = description;
    linkToVersion.file.token = upload.upload.token;
    linkToVersion.file.versionId = this.versionId;
    linkToVersion.file.filename = targetFile;
    String json = Redmine.GSON.toJson(linkToVersion, FileUpload.class);

    // Link attachment to version
    String url = this.url + "/projects/" + this.projectId + "/files.json";
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty("X-Redmine-API-Key", this.key);

    console.info(String.format("Uploading %s to %s", json, url));

    try (OutputStream output = connection.getOutputStream()) {
      output.write(json.getBytes());
    }

    InputStream response = connection.getInputStream();
    console.info(Redmine.getText(response));
  }


  private static String getText(InputStream is) throws IOException {
    StringBuilder response = new StringBuilder();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
    }
    return response.toString();
  }

  /**
   * Redmine response for the file upload:
   * {"upload":{"token":"7167.ed1ccdb093229ca1bd0b043618d88743"}}
   */
  public static class AttachmentUpload {

    Upload upload;

    static class Upload {

      String token;
    }
  }

  /**
   * POST body when linking attachment with version
   */
  public static class FileUpload {

    RedmineFile file;

    FileUpload() {
      file = new RedmineFile();
    }

    static class RedmineFile {

      String token;
      String versionId;
      String filename;
      String description;
    }
  }
}
