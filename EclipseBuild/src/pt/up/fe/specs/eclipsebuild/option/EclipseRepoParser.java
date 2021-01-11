/**
 * Copyright 2017 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.specs.eclipsebuild.option;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.parsing.ListParser;

public class EclipseRepoParser {

    private static final Map<String, BiConsumer<ListParser<String>, EclipseRepoParser>> ECLIPSE_REPO_PARSER;
    static {
        ECLIPSE_REPO_PARSER = new HashMap<>();
        ECLIPSE_REPO_PARSER.put("-u", EclipseRepoParser::parseUserLibrary);
        ECLIPSE_REPO_PARSER.put("-i", EclipseRepoParser::parseIvySettings);
    }

    private static final String DEFAULT_IGNORE_PROJECTS_FILE = "projects.buildignore";
    private static final String DEFAULT_IVY_SETTINGS_FILE = "ivysettings.xml";
    // private static final String DEFAULT_USER_LIBRARIES = "repo.userlibraries";

    public static String getDefaultIgnoreProjectsFile() {
        return DEFAULT_IGNORE_PROJECTS_FILE;
    }

    public static String getDefaultIvySettingsFile() {
        return DEFAULT_IVY_SETTINGS_FILE;
    }

    // public static String getDefaultUserLibraries() {
    // return DEFAULT_USER_LIBRARIES;
    // }

    private final String repositoryPath;
    private File ivySettings;
    private File userLibraries;
    private File ignoreProjects;

    public EclipseRepoParser(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public EclipseRepo parse(ListParser<String> currentArgs) {
        // Parse arguments, until a non-supported flag or another repo appears
        while (!currentArgs.isEmpty()) {
            Optional<String> nextArg = currentArgs.popSingleIf(ECLIPSE_REPO_PARSER::containsKey);

            if (nextArg.isPresent()) {
                ECLIPSE_REPO_PARSER.get(nextArg.get()).accept(currentArgs, this);
                continue;
            }

            // Found argument that is not part of repository definition, stop
            break;
        }

        // Create EclipseRepo

        return new EclipseRepo(repositoryPath, ivySettings, userLibraries, ignoreProjects);
    }

    private static void parseUserLibrary(ListParser<String> args, EclipseRepoParser builder) {
        // Next argument must be the user libraries file
        Preconditions.checkArgument(!args.isEmpty(), "Expected an Eclipse user libraries file after flag");

        builder.userLibraries = new File(args.popSingle());
    }

    private static void parseIvySettings(ListParser<String> args, EclipseRepoParser builder) {
        // Next argument must be an Ivy settings file
        Preconditions.checkArgument(!args.isEmpty(), "Expected an Ivy settings file after flag");

        builder.ivySettings = new File(args.popSingle());
    }

}
