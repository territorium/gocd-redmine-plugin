/*
 * Copyright 2018 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.artifact.webdav.utils;

import com.github.sardine.Sardine;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import cd.go.artifact.webdav.ConsoleLogger;

public class WebDAV {

  private final Sardine       sardine;
  private final ConsoleLogger logger;


  public WebDAV(Sardine sardine, ConsoleLogger logger) {
    this.sardine = sardine;
    this.logger = logger;
  }


  public final Sardine getSardine() {
    return sardine;
  }


  public final ConsoleLogger getConsole() {
    return logger;
  }

  public final void uploadFile(String url, File file) throws IOException {
    getConsole().info(String.format("Upload file %s", url));

    try (InputStream stream = new FileInputStream(file)) {
      byte[] data = IOUtils.toByteArray(stream);
      getSardine().put(url, data);
    }
  }

  public void uploadFiles(String url, File file) throws IOException {
    String resource = String.format("%s/%s", url, file.getName());
    if (file.isDirectory()) {
      createDirectory(resource);
      for (File f : file.listFiles())
        uploadFiles(resource, f);
    } else {
      uploadFile(resource, file);
    }
  }

  /**
   * Create all directories recursively.
   *
   * @param url
   * @param path
   */
  public final void createDirectories(String url, String path) {
    for (String name : path.split("/")) {
      if (!name.contains(".")) {
        url = String.format("%s/%s", url, name);
        createDirectory(url);
      }
    }
  }


  private boolean createDirectory(String resource) {
    try {
      if (!getSardine().exists(resource)) {
        getConsole().info("Create directory " + resource);
        getSardine().createDirectory(resource);
        return true;
      }
    } catch (IOException e) {
      WebDAV.printStackTrace(getConsole(), e, "Couldn't create directory %s", resource);
    }
    return false;
  }

  public static void printStackTrace(ConsoleLogger console, Exception exception, String message, Object... arguments) {
    console.error(String.format(message, arguments));
    for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
      console.error("   at: " + stackTraceElement.toString());
    }
  }
}
