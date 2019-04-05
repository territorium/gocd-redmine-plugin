package cd.go.artifact.webdav;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import cd.go.artifact.webdav.model.ArtifactStoreConfig;

public class WebDAVClientFactory {
    private static final WebDAVClientFactory WEBDAV_CLIENT_FACTORY = new WebDAVClientFactory();

    public Sardine create(ArtifactStoreConfig artifactStoreConfig) {
        return createClient(artifactStoreConfig);
    }

    public static WebDAVClientFactory instance() {
        return WEBDAV_CLIENT_FACTORY;
    }

    private static Sardine createClient(ArtifactStoreConfig artifactStoreConfig) {
        String user = artifactStoreConfig.getUsername();
        String pass = artifactStoreConfig.getPassword();
        return ((user == null) || (pass == null)) ? SardineFactory.begin() : SardineFactory.begin(user, pass);
    }
}
