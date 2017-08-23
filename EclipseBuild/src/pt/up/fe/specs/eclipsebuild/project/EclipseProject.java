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

package pt.up.fe.specs.eclipsebuild.project;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import pt.up.fe.specs.eclipse.Classpath.ClasspathFiles;
import pt.up.fe.specs.eclipse.Classpath.ClasspathParser;
import pt.up.fe.specs.eclipse.Utilities.EclipseProjects;
import pt.up.fe.specs.eclipse.Utilities.UserLibraries;
import pt.up.fe.specs.eclipse.builder.BuildUtils;
import pt.up.fe.specs.eclipse.builder.CreateBuildXml;
import pt.up.fe.specs.eclipsebuild.option.EclipseRepo;
import pt.up.fe.specs.util.SpecsLogs;

public class EclipseProject {

    private final String name;
    private final File projectRepo;
    private final ClasspathParser projectData;
    private final File ivySettings;

    public EclipseProject(String name, File projectRepo, ClasspathParser classpath, File ivySettings) {
        this.name = name;
        this.projectRepo = projectRepo;
        this.projectData = classpath;
        this.ivySettings = ivySettings;
    }

    public String getName() {
        return name;
    }

    public File getProjectRepo() {
        return projectRepo;
    }

    public ClasspathParser getProjectData() {
        return projectData;
    }

    public File getIvySettings() {
        return ivySettings;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();

        string.append(name);
        if (ivySettings != null) {
            string.append(", ivy settings: ").append(ivySettings);
        }

        return string.toString();
    }

    public static Map<String, EclipseProject> build(List<EclipseRepo> options) {
        // Create EclipseProjects and UserLibraries that represent all the repositories specified

        // Start with EclipseProjects
        EclipseProjects eclipseProjects = EclipseProjects.newEmpty();
        for (EclipseRepo option : options) {
            Set<String> projectsToIgnore = getProjectsToIgnore(option.getIgnoreProjects());
            eclipseProjects.addRepository(option.getRepositoryFolder(), projectsToIgnore, option.getIvySettings());
        }

        // Build UserLibraries
        Map<File, UserLibraries> userLibraries = new HashMap<>();
        for (EclipseRepo option : options) {
            if (option.getUserLibraries() == null) {
                continue;
            }

            UserLibraries repoUserLibraries = UserLibraries.newInstance(eclipseProjects, option.getUserLibraries());
            userLibraries.put(option.getRepositoryFolder(), repoUserLibraries);
        }

        // Classpath data that represents all repositories
        ClasspathParser classpathData = new ClasspathParser(eclipseProjects, userLibraries);

        return build(eclipseProjects, classpathData);

    }

    // public static List<EclipseProject> build(MultiBuildOption option, ClasspathParser parser) {
    public static Map<String, EclipseProject> build(EclipseProjects eclipseProjects, ClasspathParser parser) {
        Map<String, EclipseProject> eclipseProjectsMap = new HashMap<>();

        // Create ClasspathParser for this set of projects
        // ClasspathParser parser = ClasspathParser.newInstance(options.getRepositoryFolder(),
        // options.getUserLibraries());

        // Initialize project ignore list
        // Set<String> projectsToIgnore = getProjectsToIgnore(option.getIgnoreProjects());

        // List<EclipseProject> projects = new ArrayList<>();

        // Filter projects
        for (String projectName : parser.getEclipseProjects().getProjectNames()) {

            // Check if project was already added
            if (eclipseProjectsMap.containsKey(projectName)) {
                continue;
            }

            // If cannot get classpath files for any reason, ignore it
            // (i.e., project is not supposed to be built and does not contain a .classpath file.
            Optional<ClasspathFiles> classpathFiles = parser.getClasspathTry(projectName);
            if (!classpathFiles.isPresent()) {
                SpecsLogs.msgInfo("Skipping project '" + projectName + "' (could not get classpath information)");
                continue;
            }

            // Ignore project if it does not have sources
            if (classpathFiles.get().getSourceFolders().isEmpty()) {
                SpecsLogs.msgInfo("Skipping project '" + projectName + "' (no source folder found)");
                continue;
            }

            // projects.add(new EclipseProject(projectName, parser, option.getIvySettings()));
            eclipseProjectsMap.put(projectName,
                    new EclipseProject(projectName, eclipseProjects.getProjectRepository(projectName), parser,
                            eclipseProjects.getIvySettings(projectName)));
        }

        return eclipseProjectsMap;
        // return projects;
    }

    private static Set<String> getProjectsToIgnore(File ignoreProjects) {
        if (ignoreProjects == null) {
            return Collections.emptySet();
        }

        return new HashSet<>(CreateBuildXml.parseProjectsList(ignoreProjects));
    }

    public File getProjectFolder() {
        return getClasspath().getProjectFolder();
    }

    public ClasspathFiles getClasspath() {
        return projectData.getClasspath(name);
    }

    public File getBinFolder() {
        return BuildUtils.getBinFolder(getProjectFolder());
    }

    public boolean usesIvy() {
        return getClasspath().getIvyPath().isPresent();
    }

    public File getIvyJarFolder() {
        return BuildUtils.getIvyJarFolder(getProjectFolder());
    }

    public File getIvyFile() {
        return new File(getProjectFolder(), getClasspath().getIvyPath().get());
    }

    public String getIvyId() {
        return XmlGenerators.buildIvyId(projectRepo);
    }

    public boolean hasIvySettings() {
        return ivySettings != null;
    }
}
