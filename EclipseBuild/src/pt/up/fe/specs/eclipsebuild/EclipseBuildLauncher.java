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

package pt.up.fe.specs.eclipsebuild;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.eclipse.Classpath.ClasspathParser;
import pt.up.fe.specs.eclipse.Utilities.DeployUtils;
import pt.up.fe.specs.eclipse.builder.BuildUtils;
import pt.up.fe.specs.eclipsebuild.option.EclipseBuildArgumentsParser;
import pt.up.fe.specs.eclipsebuild.option.EclipseBuildKeys;
import pt.up.fe.specs.eclipsebuild.option.EclipseRepo;
import pt.up.fe.specs.eclipsebuild.project.EclipseProject;
import pt.up.fe.specs.eclipsebuild.project.XmlGenerators;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.utilities.Replacer;

public class EclipseBuildLauncher {

    // TODO: Expose as option
    private static final boolean IGNORE_TEST_FOLDERS = false;

    public static void main(String[] args) {
        execute(Arrays.asList(args));
    }

    public static void execute(List<String> args) {
        SpecsSystem.programStandardInit();

        // Parse input
        EclipseBuildArgumentsParser argsParser = new EclipseBuildArgumentsParser();
        DataStore config = argsParser.parseArguments(args);

        // If --help, show message and return
        if (config.get(EclipseBuildKeys.SHOW_HELP)) {
            EclipseBuildArgumentsParser.printHelpMessage();
            return;
        }

        // If --clean, delete contents of temporary folder
        if (config.get(EclipseBuildKeys.CLEAN)) {
            File reposFolder = EclipseRepo.getRepositoriesFolder();
            SpecsLogs.msgInfo("Cleaning repository folder '" + reposFolder.getAbsolutePath() + "'");
            SpecsIo.deleteFolderContents(reposFolder);
        }

        // If no repositories, assume all folders of current folder
        List<EclipseRepo> eclipseRepos = config.get(EclipseBuildKeys.ECLIPSE_REPOS);
        if (eclipseRepos.isEmpty()) {
            EclipseBuildLog.info("No arguments, assuming all folders inside current path");
            SpecsIo.getFolders(SpecsIo.getWorkingDir()).stream()
                    .map(EclipseRepo::new)
                    .forEach(eclipseRepos::add);

        }

        // List<EclipseRepo> options = argsParser.parseArguments(args);

        if (eclipseRepos.isEmpty()) {
            EclipseBuildLog.info("No repositories found");
            return;
        }

        Map<String, EclipseProject> eclipseProjects = EclipseProject.build(eclipseRepos);
        if (eclipseProjects.isEmpty()) {
            EclipseBuildLog.info("No Eclipse projects found");
            return;
        }

        // Check if it is only to list
        if (config.get(EclipseBuildKeys.SHOW_LIST)) {
            listOption(config, eclipseProjects);
            return;
        }

        Collection<EclipseProject> eclipseProjectsValues = eclipseProjects.values();
        ClasspathParser projectData = eclipseProjectsValues.stream().findFirst().get().getProjectData();
        List<String> projectNames = eclipseProjectsValues.stream()
                .map(project -> project.getName())
                .collect(Collectors.toList());
        // StringBuilder buildXml = new StringBuilder();

        // Ivy import
        String ivyImport = BuildUtils.getIvyDependency(projectData);

        // Ivy settings
        String ivySettings = XmlGenerators.getIvySettings(eclipseRepos);

        // Clean
        String clean = XmlGenerators.getCleanXml(eclipseProjectsValues);

        // Compile
        boolean jvmJavac = config.get(EclipseBuildKeys.JVM_JAVAC);
        String compileTargets = XmlGenerators.getCompileXml(eclipseProjectsValues, IGNORE_TEST_FOLDERS, jvmJavac);

        // Bechmarker
        String benchmarker = XmlGenerators.getBenchmarkerXml(eclipseProjects);

        String junitTargets = getJUnitTargets(config, projectNames, projectData);

        Replacer antBuild = new Replacer(EclipseBuildResource.MAIN_TEMPLATE);

        antBuild.replace("<USE_IVY>", ivyImport);
        antBuild.replace("<IVY_SETTINGS>", ivySettings);
        antBuild.replace("<CLEAN>", clean);
        antBuild.replace("<ALL_COMPILE_TARGETS>", BuildUtils.getDependenciesSuffix(projectNames));
        antBuild.replace("<COMPILE_TARGETS>", compileTargets);
        antBuild.replace("<ALL_JUNIT_TARGETS>", junitTargets);
        antBuild.replace("<JUNIT_TARGETS>", XmlGenerators.buildJUnitTarget(eclipseProjectsValues));
        antBuild.replace("<BENCH_TARGETS>", benchmarker);

        // Save script
        // File buildFile = new File(repFolder, "build_test.xml");

        // Save script
        File buildFile = new File(SpecsIo.getWorkingDir(), "build.xml");
        SpecsIo.write(buildFile, antBuild.toString());
        SpecsLogs.msgInfo("ANT Build file written (" + buildFile.getAbsolutePath() + ")");

        // Build
        if (config.get(EclipseBuildKeys.BUILD) || config.get(EclipseBuildKeys.TEST)) {

            String compileTarget = config.hasValue(EclipseBuildKeys.PROJECT_NAME)
                    ? BuildUtils.getCompileTargetName(config.get(EclipseBuildKeys.PROJECT_NAME))
                    : null;

            String mainTarget = config.get(EclipseBuildKeys.TEST) ? "junit" : compileTarget;

            // Add new target for build

            // If project is defined, set here the target

            // If there is a main class, the target xml should reflect it

            // DeployUtils.runAntOnProcess(buildFile, targetName);

            // Compile. This must run before we create the JAR XML, otherwise it will not know
            // which JAR files are needed from Ivy dependencies
            DeployUtils.runAnt(buildFile, mainTarget);

            // If a project was specified, create and run jar target
            File jarTargetFile = null;
            if (config.hasValue(EclipseBuildKeys.PROJECT_NAME)) {
                jarTargetFile = createJarFile(config, eclipseProjects);
                String targetName = XmlGenerators.getCreateJarTarget();
                DeployUtils.runAnt(jarTargetFile, targetName);
            }

        }

    }

    private static String getJUnitTargets(DataStore config, List<String> projectNames,
            ClasspathParser projectData) {

        // If test flag and project name, run only junit targets to that project
        if (config.get(EclipseBuildKeys.TEST) && config.hasValue(EclipseBuildKeys.PROJECT_NAME)) {
            var projectName = config.get(EclipseBuildKeys.PROJECT_NAME);
            var dependentProjectsAndSelf = projectData.getDependentProjectsAndSelf(projectName);
            SpecsLogs.debug(() -> "EclipseBuild: dependent projects and self -> " + dependentProjectsAndSelf);
            var projectTests = BuildUtils.getJUnitTargetDependencies(dependentProjectsAndSelf);
            SpecsLogs.debug(() -> "EclipseBuild: junits -> " + projectTests);
            return projectTests;
            // return BuildUtils.getJUnitTargetDependencies(projectData.getDependentProjectsAndSelf(projectName));
        }

        // Otherwise, return all junit targets
        return BuildUtils.getJUnitTargetDependencies(projectNames);

    }

    private static File createJarFile(DataStore config, Map<String, EclipseProject> eclipseProjects) {

        // Build jar
        String jarTargets = XmlGenerators.getJarXml(eclipseProjects, config);

        Replacer jarXml = new Replacer(EclipseBuildResource.JAR_TEMPLATE);
        jarXml.replace("<JAR_TARGET>", jarTargets);

        File jarTargetFile = new File(SpecsIo.getWorkingDir(), "jar.xml");
        SpecsIo.write(jarTargetFile, jarXml.toString());
        SpecsLogs.msgInfo("ANT JAR file written (" + jarTargetFile.getAbsolutePath() + ")");

        return jarTargetFile;

    }

    private static void listOption(DataStore config, Map<String, EclipseProject> eclipseProjects) {
        if (!config.hasValue(EclipseBuildKeys.PROJECT_NAME)) {
            SpecsLogs.msgInfo("Specified 'list' option but without 'project', showing all available projects:");
            List<String> projectNames = new ArrayList<>(eclipseProjects.keySet());
            Collections.sort(projectNames);
            projectNames.stream().forEach(projectName -> SpecsLogs.msgInfo(" - " + projectName));
            return;
        }

        String projectName = config.get(EclipseBuildKeys.PROJECT_NAME);
        EclipseProject project = eclipseProjects.get(projectName);

        if (project == null) {
            SpecsLogs.msgInfo("Could not find a project with name '" + projectName + "'");
            return;
        }

        SpecsLogs.msgInfo("Dependencies for project '" + projectName + "':");
        project.getClasspath().getDependentProjects().stream()
                .forEach(name -> SpecsLogs
                        .msgInfo(" - " + name + " (" + eclipseProjects.get(name).getProjectRepo().getName() + ")"));

        return;
    }

    /*
    private static void listShowProject(String projectName, Map<String, EclipseProject> eclipseProjects) {
      // With repo prefixed?  
    }
    */

}
