
package cd.go.artifact.redmine.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link PathParser} is a utility that tries to get the matches of a file.
 */
class PathParser {

  private final Path             absolutePath;
  private final List<PathMapper> matches = new ArrayList<>();

  /**
   * Constructs an instance of {@link PathParser}.
   *
   * @param absolutePath
   */
  public PathParser(Path absolutePath) {
    this.absolutePath = absolutePath;
  }

  /**
   * Gets the {@link File} matches.
   */
  public final List<PathMapper> getMatches() {
    return matches;
  }

  /**
   * Replaces the placeholder's $[NUMBER] with the group values.
   *
   * @param target
   * @param groups
   */
  public static String replace(String target, List<String> groups) {
    for (int index = 0; index < groups.size(); index++) {
      target = target.replace("$" + (index + 1), groups.get(index));
    }
    return target;
  }

  /**
   * Processes the input path from the offset to calculate the specific path and the groups.
   *
   * @param path
   * @param offset
   * @param groups
   */
  public void process(Path path, int offset, List<String> groups) {
    String text = "^" + path.getName(offset).toString().replace('%', '*') + "$";
    Pattern pattern = Pattern.compile(text);
    File folder = ((offset == 0) ? absolutePath : absolutePath.resolve(path.subpath(0, offset))).toFile();
    for (File file : folder.listFiles(new PatternFilter(pattern))) {
      List<String> values = new ArrayList<String>(groups);
      Matcher matcher = pattern.matcher(file.getName());
      if (matcher.find()) {
        for (int index = 0; index < matcher.groupCount(); index++) {
          values.add(matcher.group(index + 1));
        }
      }
      Path newPath =
          (offset == 0) ? Paths.get(file.getName()) : path.subpath(0, offset).resolve(Paths.get(file.getName()));
      if (offset + 1 < path.getNameCount())
        newPath = newPath.resolve(path.subpath(offset + 1, path.getNameCount()));

      if (offset + 1 >= path.getNameCount())
        matches.add(new PathMapper(absolutePath, newPath, values));
      else {
        process(newPath, offset + 1, values);
      }
    }
  }

  /**
   * The {@link PatternFilter} class implements a {@link FilenameFilter} using a {@link Pattern} to
   * find the valid files.
   */
  private class PatternFilter implements FilenameFilter {

    private final Pattern pattern;

    /**
     * Constructs an instance of {@link PatternFilter}.
     *
     * @param pattern
     */
    private PatternFilter(Pattern pattern) {
      this.pattern = pattern;
    }

    /**
     * Tests if a specified file should be included in a file list.
     * 
     * @param dir
     * @param name
     */
    @Override
    public final boolean accept(File dir, String name) {
      return pattern.matcher(name).find();
    }
  }
}
