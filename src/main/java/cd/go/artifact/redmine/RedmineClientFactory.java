
package cd.go.artifact.redmine;

import cd.go.artifact.redmine.model.ArtifactStoreConfig;
import cd.go.artifact.redmine.utils.Redmine;

public class RedmineClientFactory {

  private static final RedmineClientFactory REDMINE_CLIENT_FACTORY = new RedmineClientFactory();

  public Redmine create(ArtifactStoreConfig artifactStoreConfig) {
    return createClient(artifactStoreConfig);
  }

  public static RedmineClientFactory instance() {
    return REDMINE_CLIENT_FACTORY;
  }

  private static Redmine createClient(ArtifactStoreConfig artifactStoreConfig) {
    String url = artifactStoreConfig.getUrl();
    String key = artifactStoreConfig.getKey();
    String project = artifactStoreConfig.getProject();
    String version = artifactStoreConfig.getVersion();
    return new Redmine(url, key, project, version);
  }
}
