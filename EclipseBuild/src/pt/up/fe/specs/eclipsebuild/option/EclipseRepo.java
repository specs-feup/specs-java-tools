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

import pt.up.fe.specs.git.SpecsGit;
import pt.up.fe.specs.util.SpecsLogs;

public class EclipseRepo {

    // private static final String ECLIPSE_REPOS_FOLDER = "eclipse_build_repos";

    private static final String DEFAULT_IGNORE_PROJECTS_FILE = "projects.buildignore";
    private static final String DEFAULT_IVY_SETTINGS_FILE = "ivysettings.xml";
    private static final String DEFAULT_USER_LIBRARIES = "repo.userlibraries";

    public static String getDefaultIgnoreProjectsFile() {
        return DEFAULT_IGNORE_PROJECTS_FILE;
    }

    public static String getDefaultIvySettingsFile() {
        return DEFAULT_IVY_SETTINGS_FILE;
    }

    public static String getDefaultUserLibraries() {
        return DEFAULT_USER_LIBRARIES;
    }

    // private final File repositoryFolder;
    private final String repositoryPath;
    private final File ivySettings;
    private final File userLibraries;
    private final File ignoreProjects;

    private File processedRepositoryFolder;

    public EclipseRepo(File repositoryFolder, File ivySettings, File userLibraries, File ignoreProjects) {
        this(repositoryFolder.getPath(), ivySettings, userLibraries, ignoreProjects);
    }

    public EclipseRepo(File repositoryFolder) {
        this(repositoryFolder.getPath());
    }

    public EclipseRepo(String repositoryPath) {
        this(repositoryPath, null, null, null);
    }

    public EclipseRepo(String repositoryPath, File ivySettings, File userLibraries, File ignoreProjects) {
        this.repositoryPath = repositoryPath;
        this.ivySettings = ivySettings;
        this.userLibraries = userLibraries;
        this.ignoreProjects = ignoreProjects;

        this.processedRepositoryFolder = null;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();

        string.append("Repo Path: ").append(repositoryPath).append("\n");
        string.append("Ivy Settings: ").append(ivySettings).append("\n");
        string.append("User Libraries: ").append(userLibraries).append("\n");
        string.append("Ignore Projects: ").append(ignoreProjects);

        return string.toString();
    }

    public File getRepositoryFolder() {
        if (processedRepositoryFolder != null) {
            return processedRepositoryFolder;
        }

        // Check if path represents a remote repository
        String pathLowerCase = repositoryPath.toLowerCase();
        if (pathLowerCase.startsWith("http://") || pathLowerCase.startsWith("https://")) {
            // processedRepositoryFolder = parseRepositoryUrl(repositoryPath);
            processedRepositoryFolder = SpecsGit.parseRepositoryUrl(repositoryPath);

        }
        // Interpret path as a local folder
        else {
            processedRepositoryFolder = new File(repositoryPath);
        }

        return processedRepositoryFolder;
    }

    // private static File parseRepositoryUrl(String repositoryPath) {
    // String repoName = getRepoName(repositoryPath);
    //
    // // Get repo folder
    // File eclipseBuildFolder = getRepositoriesFolder();
    // File repoFolder = new File(eclipseBuildFolder, repoName);
    //
    // // If folder does not exist, or if it exists and is empty, clone repository
    // if (!repoFolder.exists() || SpecsIo.isEmptyFolder(repoFolder)) {
    // try {
    // SpecsLogs.msgInfo("Cloning repo '" + repositoryPath + "' to folder '" + repoFolder + "'");
    // Git.cloneRepository()
    // .setURI(repositoryPath)
    // .setDirectory(repoFolder)
    // .call();
    //
    // return repoFolder;
    // } catch (GitAPIException e) {
    // throw new RuntimeException("Could not clone repository '" + repositoryPath + "'", e);
    // }
    // }
    //
    // // Repository already exists, pull
    //
    // try {
    // SpecsLogs.msgInfo("Pulling repo '" + repositoryPath + "' in folder '" + repoFolder + "'");
    // Git gitRepo = Git.open(repoFolder);
    // PullCommand pullCmd = gitRepo.pull();
    // pullCmd.call();
    // } catch (GitAPIException | IOException e) {
    // throw new RuntimeException("Could not pull repository '" + repositoryPath + "'", e);
    // }
    //
    // return repoFolder;
    // }

    public static File getRepositoriesFolder() {
        return SpecsGit.getRepositoriesFolder();
        // return new File(SpecsIo.getTempFolder(), ECLIPSE_REPOS_FOLDER);
    }

    // private static String getRepoName(String repositoryPath) {
    // try {
    // String repoPath = new URI(repositoryPath).getPath();
    //
    // Preconditions.checkArgument(!repoPath.endsWith("/"),
    // "Did not expect path to end with '/', take care of this case");
    //
    // if (repoPath.toLowerCase().endsWith(".git")) {
    // repoPath = repoPath.substring(0, repoPath.length() - ".git".length());
    // }
    //
    // int slashIndex = repoPath.lastIndexOf('/');
    // if (slashIndex != -1) {
    // repoPath = repoPath.substring(slashIndex + 1);
    // }
    //
    // return repoPath;
    //
    // } catch (URISyntaxException e) {
    // throw new RuntimeException(e);
    // }
    //
    // }

    private File getDefaultFile(File currentFile, String defaultFilename) {
        // Check if current file is set
        if (currentFile != null) {
            return currentFile;
        }

        // Current file is null, check if default file exists
        File defaultFile = new File(getRepositoryFolder(), defaultFilename);
        if (defaultFile.isFile()) {
            SpecsLogs.msgInfo("Using default file " + defaultFile.getAbsolutePath());
            return defaultFile;
        }
        return null;
    }

    public File getIvySettings() {
        return getDefaultFile(ivySettings, DEFAULT_IVY_SETTINGS_FILE);
        // return ivySettings;
    }

    public File getUserLibraries() {
        return getDefaultFile(userLibraries, DEFAULT_USER_LIBRARIES);
        // return userLibraries;
    }

    public File getIgnoreProjects() {
        return getDefaultFile(ignoreProjects, DEFAULT_IGNORE_PROJECTS_FILE);
        // return ignoreProjects;
    }

}
