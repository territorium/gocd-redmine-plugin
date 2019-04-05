package cd.go.artifact.webdav.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;
import java.util.Optional;

import cd.go.artifact.webdav.annotation.FieldMetadata;

public class WebDavFileArtifactPlanConfig extends ArtifactPlanConfig {
    @Expose
    @SerializedName("Source")
    @FieldMetadata(key = "Source")
    private String source;

    @Expose
    @SerializedName("Destination")
    @FieldMetadata(key = "Destination")
    private String destination;

    public WebDavFileArtifactPlanConfig(String source, Optional<String> destination) {
        this.source = source;
        this.destination = destination.orElse("");
    }

    @Override
    public String getSource() { return source; }

    @Override
    public String getDestination() {
        return destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebDavFileArtifactPlanConfig that = (WebDavFileArtifactPlanConfig) o;
        return Objects.equals(source, that.source) && Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination);
    }
}
