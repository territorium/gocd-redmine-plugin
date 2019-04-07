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

package cd.go.artifact.redmine.executors;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import java.io.*;
import java.nio.file.Paths;
import java.util.Map;

import cd.go.artifact.redmine.ConsoleLogger;
import cd.go.artifact.redmine.RedmineClientFactory;
import cd.go.artifact.redmine.model.ArtifactStoreConfig;
import cd.go.artifact.redmine.utils.Redmine;
import cd.go.artifact.redmine.utils.Util;

import static cd.go.artifact.redmine.RedmineArtifactPlugin.LOG;
import static java.lang.String.format;

public class FetchArtifactExecutor implements RequestExecutor {
    private FetchArtifactRequest fetchArtifactRequest;
    private final ConsoleLogger consoleLogger;
    private RedmineClientFactory clientFactory;

    public FetchArtifactExecutor(GoPluginApiRequest request, ConsoleLogger consoleLogger) {
        this(request, consoleLogger, RedmineClientFactory.instance());
    }

    FetchArtifactExecutor(GoPluginApiRequest request, ConsoleLogger consoleLogger, RedmineClientFactory clientFactory) {
        this.fetchArtifactRequest = FetchArtifactRequest.fromJSON(request.requestBody());
        this.consoleLogger = consoleLogger;
        this.clientFactory = clientFactory;
    }

    @Override
    public GoPluginApiResponse execute() {
        try {
            final Map<String, String> artifactMap = fetchArtifactRequest.getMetadata();
            validateMetadata(artifactMap);

            final String workingDir = fetchArtifactRequest.getAgentWorkingDir();
            final String sourceFileToGet = artifactMap.get("Source");

            consoleLogger.info(String.format("Retrieving file `%s` from WebDAV `%s`.", sourceFileToGet, fetchArtifactRequest.getArtifactStoreConfig().getUrl()));
            LOG.info(String.format("Retrieving file `%s` from WebDAV `%s`.", sourceFileToGet, fetchArtifactRequest.getArtifactStoreConfig().getUrl()));

            Redmine redmine = clientFactory.create(fetchArtifactRequest.getArtifactStoreConfig());
//            String resource = String.format("%s/%s", fetchArtifactRequest.getArtifactStoreConfig().getUrl(), sourceFileToGet);
//            InputStream fileReader = sardine.get(resource);
//            File outFile = new File(Paths.get(workingDir, sourceFileToGet).toString());
//            OutputStream writer = new BufferedOutputStream(new FileOutputStream(outFile));
//
//            int read_length = -1;
//
//            while ((read_length = fileReader.read()) != -1) {
//                writer.write(read_length);
//            }
//
//            writer.flush();
//            writer.close();
//            fileReader.close();
//
//            consoleLogger.info(String.format("Source `%s` successfully pulled from WebDAV `%s`.", sourceFileToGet, fetchArtifactRequest.getArtifactStoreConfig().getUrl()));

//          return DefaultGoPluginApiResponse.success("");
          return DefaultGoPluginApiResponse.error("Download not supported");
        } catch (Exception e) {
            final String message = format("Failed pull source file: %s", e);
            consoleLogger.error(message);
            LOG.error(message);
            return DefaultGoPluginApiResponse.error(message);
        }
    }

    public void validateMetadata(Map<String, String> artifactMap) {
        if (artifactMap == null) {
            throw new RuntimeException(String.format("Cannot fetch the source file from WebDAV: Invalid metadata received from the GoCD server. The artifact metadata is null."));
        }

        if (!artifactMap.containsKey("Source")) {
            throw new RuntimeException(String.format("Cannot fetch the source file from WebDAV: Invalid metadata received from the GoCD server. The artifact metadata must contain the key `%s`.", "Source"));
        }
    }

    // TODO Diogomrorl: Maybe this can be moved to a separate file under model to keep coherence
    protected static class FetchArtifactRequest {
        @Expose
        @SerializedName("store_configuration")
        private ArtifactStoreConfig artifactStoreConfig;
        @Expose
        @SerializedName("artifact_metadata")
        private Map<String, String> metadata;

        @Expose
        @SerializedName("agent_working_directory")
        private String agentWorkingDir;

        public FetchArtifactRequest() {
        }

        public FetchArtifactRequest(ArtifactStoreConfig artifactStoreConfig, Map<String, String> metadata, String agentWorkingDir) {
            this.artifactStoreConfig = artifactStoreConfig;
            this.metadata = metadata;
            this.agentWorkingDir = agentWorkingDir;
        }

        public ArtifactStoreConfig getArtifactStoreConfig() {
            return artifactStoreConfig;
        }

        public String getAgentWorkingDir() {
            return agentWorkingDir;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public static FetchArtifactRequest fromJSON(String json) {
            return Util.GSON.fromJson(json, FetchArtifactRequest.class);
        }
    }
}
