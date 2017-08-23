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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.eclipse.Classpath.ClasspathFiles;
import pt.up.fe.specs.eclipse.Classpath.ClasspathParser;
import pt.up.fe.specs.eclipse.Utilities.DeployUtils;
import pt.up.fe.specs.eclipse.builder.BuildResource;
import pt.up.fe.specs.eclipse.builder.BuildUtils;
import pt.up.fe.specs.eclipse.builder.CreateBuildXml;
import pt.up.fe.specs.eclipsebuild.EclipseBuildResource;
import pt.up.fe.specs.eclipsebuild.option.EclipseBuildKeys;
import pt.up.fe.specs.eclipsebuild.option.EclipseRepo;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.utilities.Replacer;

public class XmlGenerators {

    private static final String CREATE_JAR_TARGET = "create_jar";

    public static String getCreateJarTarget() {
        return CREATE_JAR_TARGET;
    }

    public static String getCleanXml(Collection<EclipseProject> eclipseProjects) {
        StringBuilder cleanXml = new StringBuilder();

        cleanXml.append("<target name=\"clean\">\n\n");

        for (EclipseProject eclipseProject : eclipseProjects) {
            // Add delete for /bin folders
            Replacer template = new Replacer(BuildResource.DELETE_TEMPLATE);
            template.replace("<FOLDER>", eclipseProject.getBinFolder().getAbsolutePath());
            cleanXml.append(template).append("\n");

            // Add delete for /ivy folders, if project uses it
            if (eclipseProject.usesIvy()) {
                Replacer ivyTemplate = new Replacer(BuildResource.DELETE_TEMPLATE);
                String ivyFolder = eclipseProject.getIvyJarFolder().getAbsolutePath();

                // Make sure folder exists, to avoid errors during ANT run
                SpecsIo.mkdir(ivyFolder);
                ivyTemplate.replace("<FOLDER>", ivyFolder);
                cleanXml.append(ivyTemplate).append("\n");
            }
        }

        cleanXml.append("</target>");

        return cleanXml.toString();
    }

    public static String getCompileXml(Collection<EclipseProject> eclipseProjects, boolean ignoreTestFolders,
            boolean jvmJavac) {

        StringBuilder compileXml = new StringBuilder();

        for (EclipseProject eclipseProject : eclipseProjects) {

            ClasspathFiles classpathFiles = eclipseProject.getClasspath();
            String projectName = eclipseProject.getName();

            String targetName = BuildUtils.getCompileTargetName(projectName);
            String projectDependencies = BuildUtils.getDependencies(classpathFiles);

            String outputJar = BuildUtils.getOutputJar(projectName).getAbsolutePath();
            String fileset = BuildUtils.buildFileset(projectName, eclipseProject.getProjectData());
            String binFoldername = BuildUtils.getBinFoldername(classpathFiles);
            String sourcePath = BuildUtils.getSourcePath(classpathFiles, ignoreTestFolders);
            String copyTask = BuildUtils.getCopyTask(classpathFiles);
            String ivyResolve = getIvyResolveXml(eclipseProject);
            String commands = BuildUtils.getCommandsTask(classpathFiles);
            // String ivyResolve = BuildUtils.getResolveTask(parser, projectName);
            String forkJavacXml = jvmJavac ? "" : "fork=\"true\"";

            Replacer projectBuild = new Replacer(BuildResource.COMPILE_TEMPLATE);

            projectBuild.replace("<COMPILE_TARGET_NAME>", targetName);
            projectBuild.replace("<PROJECT_DEPENDENCIES>", projectDependencies);
            projectBuild.replace("<COMMANDS>", commands);
            projectBuild.replace("<OUTPUT_JAR_FILE>", outputJar);
            projectBuild.replace("<FILESET>", fileset);
            projectBuild.replace("<PROJECT_NAME>", projectName);
            projectBuild.replace("<BIN_FOLDER>", binFoldername);
            projectBuild.replace("<SOURCE_PATH>", sourcePath);
            projectBuild.replace("<COPY_TASK>", copyTask);
            projectBuild.replace("<IVY_RESOLVE>", ivyResolve);
            projectBuild.replace("<JAVAC_FORK>", forkJavacXml);

            compileXml.append(projectBuild).append("\n");

        }
        return compileXml.toString();

        // return "";
    }

    public static String buildIvyId(File projectRepo) {
        return "ivy." + projectRepo.getAbsoluteFile().getName() + ".settings";
    }

    public static String getIvySettings(List<EclipseRepo> options) {
        StringBuilder ivySettingsXml = new StringBuilder();

        Set<File> repoFolders = new HashSet<>();

        for (EclipseRepo option : options) {
            // Check if it has Ivy settings
            if (option.getIvySettings() == null) {
                continue;
            }

            // Check if repo was already added
            if (repoFolders.contains(option.getRepositoryFolder())) {
                continue;
            }

            // Declare ivy settings
            ivySettingsXml
                    .append("<ivy:settings id=\"" + buildIvyId(option.getRepositoryFolder()) + "\" file=\""
                            + SpecsIo.getCanonicalPath(option.getIvySettings()) + "\"/>\n");
        }

        return ivySettingsXml.toString();

    }

    public static String getIvyResolveXml(EclipseProject eclipseProject) {

        if (!eclipseProject.usesIvy()) {
            return "";
        }

        File ivyFile = eclipseProject.getIvyFile();

        // String ivySettings = eclipseProject.hasIvySettings() ? eclipseProject.getIvyId() : "ivy.instance";
        String ivySettings = eclipseProject.hasIvySettings() ? getIvySettingsString(eclipseProject.getIvyId()) : "";

        Replacer replacer = new Replacer(EclipseBuildResource.RESOLVE_TEMPLATE);

        replacer.replace("<RESOLVE_TARGET_NAME>", BuildUtils.getIvyTargetName(eclipseProject.getName()));
        replacer.replace("<IVY_FILE_LOCATION>", ivyFile.getAbsolutePath());
        replacer.replace("<IVY_FOLDER_LOCATION>", eclipseProject.getIvyJarFolder().getAbsolutePath());
        // replacer.replace("<IVY_SETTINGS_ID>", ivySettings);
        replacer.replace("<IVY_SETTINGS>", ivySettings);

        return replacer.toString();
    }

    private static String getIvySettingsString(String ivyId) {
        return "settingsRef=\"" + ivyId + "\"";
    }

    public static String buildJUnitTarget(Collection<EclipseProject> eclipseProjects) {
        StringBuilder junitXml = new StringBuilder();

        for (EclipseProject eclipseProject : eclipseProjects) {
            ClasspathFiles classpathFiles = eclipseProject.getClasspath();

            String targetName = BuildUtils.getJUnitTargetName(eclipseProject.getName());
            String testsFolder = eclipseProject.getProjectFolder().getAbsolutePath();
            String binFoldername = eclipseProject.getBinFolder().getAbsolutePath();
            String fileset = BuildUtils.buildFileset(eclipseProject.getName(), eclipseProject.getProjectData());
            String junitSourceFolders = BuildUtils.buildJUnitSources(classpathFiles);

            // Single reports folder for all tests
            // File reportsFolder = IoUtils.safeFolder(eclipseProject.getProjectRepo(), "reports");
            File reportsFolder = SpecsIo.mkdir(SpecsIo.getWorkingDir(), "reports");

            // Clean reports
            SpecsIo.deleteFolderContents(reportsFolder);

            String reportsDir = SpecsIo.getCanonicalPath(reportsFolder);

            Replacer projectBuild = new Replacer(BuildResource.JUNIT_TEMPLATE);

            projectBuild.replace("<JUNIT_TARGET_NAME>", targetName);
            projectBuild.replace("<PROJECT_NAME>", eclipseProject.getName());
            projectBuild.replace("<TESTS_FOLDER>", testsFolder);
            projectBuild.replace("<FILESET>", fileset);
            projectBuild.replace("<BIN_FOLDER>", binFoldername);
            projectBuild.replace("<SOURCE_FOLDERS>", junitSourceFolders);
            projectBuild.replace("<REPORT_DIR>", reportsDir);

            junitXml.append(projectBuild).append("\n");
        }

        return junitXml.toString();
    }

    public static String getBenchmarkerXml(Map<String, EclipseProject> eclipseProjects) {

        List<String> projectNames = CreateBuildXml.parseProjectsList(CreateBuildXml.getBenchmarkerProjectsFile());

        StringBuilder benchTargets = new StringBuilder();
        for (String projectName : projectNames) {
            EclipseProject eclipseProject = eclipseProjects.get(projectName);
            Preconditions.checkNotNull(eclipseProject);
            String benchTarget = buildBenchmarkerTarget(eclipseProject);
            benchTargets.append(benchTarget);
            benchTargets.append("\n");
        }

        // Build target benchmarker that call all benchmarker targets

        String benchmarkerTarget = projectNames.stream()
                .map(projectName -> BuildUtils.getBenchmarkerTargetName(projectName))
                .collect(Collectors.joining(",", "<target name=\"benchmarker\" depends=\"", "\"></target>"));

        benchTargets.append(benchmarkerTarget);

        return benchTargets.toString();

    }

    private static String buildBenchmarkerTarget(EclipseProject eclipseProject) {
        String projectName = eclipseProject.getName();

        String targetName = BuildUtils.getBenchmarkerTargetName(projectName);
        String testsFolder = eclipseProject.getProjectFolder().getAbsolutePath();
        String binFoldername = eclipseProject.getBinFolder().getAbsolutePath();
        String fileset = BuildUtils.buildFileset(projectName, eclipseProject.getProjectData());
        String junitSourceFolders = BuildUtils.buildBenchmarkerSources(eclipseProject.getClasspath());

        File reportsFolder = SpecsIo.mkdir(eclipseProject.getProjectRepo(), "reports");

        // Clean reports
        SpecsIo.deleteFolderContents(reportsFolder);

        String reportsDir = reportsFolder.getAbsolutePath();

        Replacer projectBuild = new Replacer(BuildResource.BENCHMARKER_TEMPLATE);

        projectBuild.replace("<BENCHMARKER_TARGET_NAME>", targetName);
        projectBuild.replace("<PROJECT_NAME>", projectName);
        projectBuild.replace("<TESTS_FOLDER>", testsFolder);
        projectBuild.replace("<FILESET>", fileset);
        projectBuild.replace("<BIN_FOLDER>", binFoldername);
        projectBuild.replace("<SOURCE_FOLDERS>", junitSourceFolders);
        projectBuild.replace("<REPORT_DIR>", reportsDir);

        return projectBuild.toString();
    }

    public static String getJarXml(Map<String, EclipseProject> eclipseProjects, DataStore config) {

        // Only create jar target if there is a project specified
        if (!config.hasValue(EclipseBuildKeys.PROJECT_NAME)) {
            return "";
        }

        String projectName = config.get(EclipseBuildKeys.PROJECT_NAME);

        String compileTarget = BuildUtils.getCompileTargetName(projectName);
        String outputJarFile = projectName + ".jar";

        String mainClassAttribute = config.hasValue(EclipseBuildKeys.MAIN_CLASS)
                ? getMainClassAttribute(config.get(EclipseBuildKeys.MAIN_CLASS))
                : "";

        String fileset = buildFileset(projectName, eclipseProjects, true);

        Replacer jarTarget = new Replacer(EclipseBuildResource.DEPLOY_REPACK_TEMPLATE);

        jarTarget.replace("<COMPILE_TARGET>", compileTarget);
        jarTarget.replace("<OUTPUT_JAR_FILE>", outputJarFile);
        jarTarget.replace("<MAIN_CLASS>", mainClassAttribute);
        jarTarget.replace("<FILESET>", fileset);

        return jarTarget.toString();
    }

    private static String getMainClassAttribute(String mainClass) {
        return "<attribute name=\"Main-Class\" value=\"" + mainClass + "\" />";
    }

    public static String buildFileset(String projetName, Map<String, EclipseProject> eclipseProjects,
            boolean extractJars) {

        ClasspathFiles classpathFiles = eclipseProjects.get(projetName).getClasspath();

        final String prefix = "         ";
        StringBuilder fileset = new StringBuilder();

        // Add JAR Files
        for (File jarFile : classpathFiles.getJarFiles()) {
            String line = extractJars ? DeployUtils.getZipfilesetExtracted(jarFile)
                    : DeployUtils.getZipfileset(jarFile);

            fileset.append(prefix);
            fileset.append(line);
            fileset.append("\n");
        }

        // Add Filesets
        for (File projectFolder : classpathFiles.getBinFolders()) {
            String line = DeployUtils.getFileset(projectFolder);

            fileset.append(prefix);
            fileset.append(line);
            fileset.append("\n");
        }

        // Get ivy folders
        List<String> ivyFolders = classpathFiles.getDependentProjects().stream()
                .map(eclipseProjects::get)
                .map(EclipseProject::getClasspath)
                .filter(classpath -> classpath.usesIvy())
                .map(classpath -> classpath.getIvyJarFolder().get().getAbsolutePath())
                .collect(Collectors.toList());

        // Add self, if present
        classpathFiles.getIvyJarFolder().map(File::getAbsolutePath).ifPresent(ivyFolders::add);

        for (String ivyFolder : ivyFolders) {
            String ivySet = DeployUtils.getIvySet(ivyFolder, extractJars);
            fileset.append(ivySet).append("\n");
        }

        return fileset.toString();
    }

    public static String buildFileset(ClasspathParser parser, String projetName, Collection<String> ivyFolders,
            boolean extractJars) {

        ClasspathFiles classpathFiles = parser.getClasspath(projetName);

        final String prefix = "         ";
        StringBuilder fileset = new StringBuilder();

        // Add JAR Files
        for (File jarFile : classpathFiles.getJarFiles()) {
            String line = extractJars ? DeployUtils.getZipfilesetExtracted(jarFile)
                    : DeployUtils.getZipfileset(jarFile);

            fileset.append(prefix);
            fileset.append(line);
            fileset.append("\n");
        }

        // Add Filesets
        for (File projectFolder : classpathFiles.getBinFolders()) {
            String line = DeployUtils.getFileset(projectFolder);

            fileset.append(prefix);
            fileset.append(line);
            fileset.append("\n");
        }

        // Add Ivy folders
        for (String ivyFolder : ivyFolders) {
            // String ivySet = extractJars ? getIvyJarsExtracted(ivyFolder) : getIvyJars(ivyFolder);

            // fileset.append(ivySet).append("\n");
        }

        return fileset.toString();
    }
}
