/*
 * Copyright (c) 2001-2019 Territorium Online Srl / TOL GmbH. All Rights Reserved.
 *
 * This file contains Original Code and/or Modifications of Original Code as defined in and that are
 * subject to the Territorium Online License Version 1.0. You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at http://www.tol.info/license/
 * and read it before using this file.
 *
 * The Original Code and all software distributed under the License are distributed on an 'AS IS'
 * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TERRITORIUM ONLINE HEREBY
 * DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT. Please see the License for
 * the specific language governing rights and limitations under the License.
 */

package cd.go.artifact.redmine.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * The {@link PathMapper} class.
 */
public class PathMapper {

  private final Path         root;
  private final Path         path;
  private final List<String> groups;

  /**
   * Constructs an instance of {@link PathMapper}.
   *
   * @param root
   * @param path
   * @param groups
   */
  PathMapper(Path root, Path path, List<String> groups) {
    this.root = root;
    this.path = path;
    this.groups = groups;
  }

  /**
   * Gets the {@link #path}.
   */
  public final Path getPath() {
    return path;
  }

  /**
   * Gets the absolute {@link File}.
   */
  public final File toFile() {
    return root.resolve(path).toFile();
  }

  /**
   * Replaces the placeholder's $[NUMBER] with the group values.
   *
   * @param target
   */
  public String remap(String target) {
    for (int index = 0; index < groups.size(); index++) {
      target = target.replace("$" + (index + 1), groups.get(index));
    }
    return target;
  }


  /**
   * Provides a list of matching {@link File}'s.
   *
   * @param workingDir
   * @param sourceName
   */
  public static List<PathMapper> list(String workingDir, String sourceName) {
    PathParser matcher = new PathParser(new File(workingDir).toPath());
    matcher.process(Paths.get(sourceName), 0, Collections.emptyList());
    return matcher.getMatches();
  }
}
