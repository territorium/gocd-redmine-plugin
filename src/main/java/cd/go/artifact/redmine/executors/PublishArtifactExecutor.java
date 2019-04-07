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

import static cd.go.artifact.redmine.RedmineArtifactPlugin.LOG;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import cd.go.artifact.redmine.ConsoleLogger;
import cd.go.artifact.redmine.RedmineClientFactory;
import cd.go.artifact.redmine.model.ArtifactPlan;
import cd.go.artifact.redmine.model.ArtifactStoreConfig;
import cd.go.artifact.redmine.model.PublishArtifactRequest;
import cd.go.artifact.redmine.model.PublishArtifactResponse;
import cd.go.artifact.redmine.utils.Redmine;

public class PublishArtifactExecutor implements RequestExecutor {
    private final PublishArtifactRequest publishArtifactRequest;
    private final PublishArtifactResponse publishArtifactResponse;
    private final ConsoleLogger consoleLogger;
    private final RedmineClientFactory clientFactory;

    public PublishArtifactExecutor(GoPluginApiRequest request, ConsoleLogger consoleLogger) {
        this(request, consoleLogger, RedmineClientFactory.instance());
    }

    PublishArtifactExecutor(GoPluginApiRequest request, ConsoleLogger consoleLogger, RedmineClientFactory clientFactory) {
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
            final Redmine redmine = clientFactory.create(artifactStoreConfig);
            final String sourceFile = artifactPlan.getArtifactPlanConfig().getSource();
            final String targetFile = artifactPlan.getArtifactPlanConfig().getDestination();
            final String workingDir = publishArtifactRequest.getAgentWorkingDir();

            redmine.upload(workingDir, sourceFile, targetFile, consoleLogger);

            consoleLogger.info(String.format("Source file `%s` successfully pushed to Redmine.", sourceFile));
            return DefaultGoPluginApiResponse.success(publishArtifactResponse.toJSON());
        } catch (Exception e) {
            consoleLogger.error(String.format("Failed to publish %s: %s", artifactPlan, e));
            LOG.error(String.format("Failed to publish %s: %s", artifactPlan, e.getMessage()), e);
            return DefaultGoPluginApiResponse.error(String.format("Failed to publish %s: %s", artifactPlan, e.getMessage()));
        }
    }
}
