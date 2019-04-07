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

package cd.go.artifact.redmine.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import cd.go.artifact.redmine.annotation.FieldMetadata;
import cd.go.artifact.redmine.annotation.Validatable;
import cd.go.artifact.redmine.utils.Util;

public class ArtifactStoreConfig implements Validatable {

    @Expose
    @SerializedName("URL")
    @FieldMetadata(key = "URL", required = true)
    private String url;

    @Expose
    @SerializedName("KEY")
    @FieldMetadata(key = "KEY", required = true, secure = true)
    private String key;

    @Expose
    @SerializedName("Project")
    @FieldMetadata(key = "Project", required = true)
    private String project;

    @Expose
    @SerializedName("Version")
    @FieldMetadata(key = "Version", required = true)
    private String version;


    public ArtifactStoreConfig() {
    }

    public ArtifactStoreConfig(String url, String key, String project, String version) {
        this.url = url;
        this.key = key;
        this.project = project;
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public String getKey() {
        return key;
    }

    public String getProject() {
        return project;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArtifactStoreConfig)) return false;

        ArtifactStoreConfig that = (ArtifactStoreConfig) o;

        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (key != null ? !key.equals(that.key) : that.key!= null) return false;
        if (project != null ? !project.equals(that.project) : that.project != null) return false;
        return version != null ? version.equals(that.version) : that.version == null;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    public static ArtifactStoreConfig fromJSON(String json) {
        return Util.GSON.fromJson(json, ArtifactStoreConfig.class);
    }
}
