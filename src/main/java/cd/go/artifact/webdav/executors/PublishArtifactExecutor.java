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

package cd.go.artifact.webdav.executors;

import static cd.go.artifact.webdav.WebDAVArtifactPlugin.LOG;

import com.github.sardine.Sardine;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.io.File;
import java.nio.file.Paths;

import cd.go.artifact.webdav.ConsoleLogger;
import cd.go.artifact.webdav.WebDAVClientFactory;
import cd.go.artifact.webdav.model.ArtifactPlan;
import cd.go.artifact.webdav.model.ArtifactStoreConfig;
import cd.go.artifact.webdav.model.PublishArtifactRequest;
import cd.go.artifact.webdav.model.PublishArtifactResponse;
import cd.go.artifact.webdav.utils.WebDAV;

public class PublishArtifactExecutor implements RequestExecutor {
    private final PublishArtifactRequest publishArtifactRequest;
    private final PublishArtifactResponse publishArtifactResponse;
    private final ConsoleLogger consoleLogger;
    private final WebDAVClientFactory clientFactory;

    public PublishArtifactExecutor(GoPluginApiRequest request, ConsoleLogger consoleLogger) {
        this(request, consoleLogger, WebDAVClientFactory.instance());
    }

    PublishArtifactExecutor(GoPluginApiRequest request, ConsoleLogger consoleLogger, WebDAVClientFactory clientFactory) {
        this.publishArtifactRequest = PublishArtifactRequest.fromJSON(request.requestBody());
        this.consoleLogger = consoleLogger;
        this.clientFactory = clientFactory;
        publishArtifactResponse = new PublishArtifactResponse();
    }

    @Override
    public GoPluginApiResponse execute() {
        ArtifactPlan artifactPlan = publishArtifactRequest.getArtifactPlan();
        final ArtifactStoreConfig artifactStoreConfig = publishArtifactRequest.getArtifactStore().getArtifactStoreConfig();
        try {
            final Sardine sardine = clientFactory.create(artifactStoreConfig);
            final String sourceFile = artifactPlan.getArtifactPlanConfig().getSource();
            final String destinationFolder = artifactPlan.getArtifactPlanConfig().getDestination();
            final String url = artifactStoreConfig.getUrl();
            final String workingDir = publishArtifactRequest.getAgentWorkingDir();

            String path = url;

            WebDAV webdav = new WebDAV(sardine, consoleLogger);
            if(!destinationFolder.isEmpty()) {
                webdav.createDirectories(url, destinationFolder);
                path += (url.endsWith("/")? "":"/") + destinationFolder;
            }

            File file = new File(Paths.get(workingDir, sourceFile).toString());
            if (file.isFile()) {
              webdav.uploadFile(path, file);
            } else {
              for (File f : file.listFiles()) {
                webdav.uploadFiles(path, f);
              }
            }

            publishArtifactResponse.addMetadata("Source", sourceFile);
            consoleLogger.info(String.format("Source file `%s` successfully pushed to WebDAV `%s`.", sourceFile, artifactStoreConfig.getUrl()));

            return DefaultGoPluginApiResponse.success(publishArtifactResponse.toJSON());
        } catch (Exception e) {
            WebDAV.printStackTrace(consoleLogger,e, "Failed to publish %s", artifactPlan);
            consoleLogger.error(String.format("Failed to publish %s: %s", artifactPlan, e));
            LOG.error(String.format("Failed to publish %s: %s", artifactPlan, e.getMessage()), e);
            return DefaultGoPluginApiResponse.error(String.format("Failed to publish %s: %s", artifactPlan, e.getMessage()));
        }
    }
}
