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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.eclipsebuild.EclipseBuildLog;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.parsing.ListParser;
import pt.up.fe.specs.util.parsing.arguments.ArgumentsParser;
import pt.up.fe.specs.util.utilities.LineStream;

public class EclipseBuildArgumentsParser {

    private static final Map<String, BiConsumer<ListParser<String>, DataStore>> ARGUMENTS_PARSER;
    static {
        ARGUMENTS_PARSER = new HashMap<>();
        ARGUMENTS_PARSER.put("--help", EclipseBuildArgumentsParser.addBool(EclipseBuildKeys.SHOW_HELP));
        ARGUMENTS_PARSER.put("-h", EclipseBuildArgumentsParser.addBool(EclipseBuildKeys.SHOW_HELP));
        ARGUMENTS_PARSER.put("--list", EclipseBuildArgumentsParser.addBool(EclipseBuildKeys.SHOW_LIST));
        ARGUMENTS_PARSER.put("--jvm-javac", EclipseBuildArgumentsParser.addBool(EclipseBuildKeys.JVM_JAVAC));
        ARGUMENTS_PARSER.put("--build", EclipseBuildArgumentsParser.addBool(EclipseBuildKeys.BUILD));
        ARGUMENTS_PARSER.put("--clean", EclipseBuildArgumentsParser.addBool(EclipseBuildKeys.CLEAN));
        ARGUMENTS_PARSER.put("--project", EclipseBuildArgumentsParser.addString(EclipseBuildKeys.PROJECT_NAME));
        ARGUMENTS_PARSER.put("--main", EclipseBuildArgumentsParser.addString(EclipseBuildKeys.MAIN_CLASS));
        ARGUMENTS_PARSER.put("--jar", EclipseBuildArgumentsParser.addEnum(EclipseBuildKeys.JAR_TYPE));
        ARGUMENTS_PARSER.put("--jarname", EclipseBuildArgumentsParser.addString(EclipseBuildKeys.JAR_NAME));
        ARGUMENTS_PARSER.put("--config", EclipseBuildArgumentsParser::parseConfig);
        ARGUMENTS_PARSER.put("--test", EclipseBuildArgumentsParser.addBool(EclipseBuildKeys.TEST));
    }

    /*
    private static final Map<String, BiConsumer<ListParser<String>, EclipseRepoBuilder>> ECLIPSE_REPO_PARSER;
    static {
        ECLIPSE_REPO_PARSER = new HashMap<>();
        ECLIPSE_REPO_PARSER.put("-u", EclipseBuildArgumentsParser::parseUserLibrary);
        ECLIPSE_REPO_PARSER.put("-i", EclipseBuildArgumentsParser::parseIvySettings);
    }
    */

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

    /**
     * Helper method for options that just return a value for a given key.
     * 
     * @param key
     * @param value
     * @return
     */
    private static <V> BiConsumer<ListParser<String>, DataStore> addValue(DataKey<V> key, V value) {
        // return (list, dataStore) -> dataStore.add(key, value);
        return (list, dataStore) -> addElement(dataStore, key, value);
    }

    private static <V> BiConsumer<ListParser<String>, DataStore> addValueFromList(DataKey<V> key,
            Function<ListParser<String>, V> processArgs) {

        return (list, dataStore) -> {
            V value = processArgs.apply(list);
            // dataStore.add(key, value);
            addElement(dataStore, key, value);
        };

    }

    private static <V> void addElement(DataStore dataStore, DataKey<V> key, V value) {
        if (dataStore.hasValue(key)) {
            var previousValue = dataStore.get(key);
            SpecsLogs.info("Duplicated flag for key '" + key + "', replacing value '" + previousValue + "' with '"
                    + value + "'");
        }
        dataStore.set(key, value);
    }

    private static <V> BiConsumer<ListParser<String>, DataStore> addString(DataKey<String> key) {
        return addValueFromList(key, ListParser::popSingle);
    }

    private static <E extends Enum<E>, V> BiConsumer<ListParser<String>, DataStore> addEnum(DataKey<E> key) {
        return addValueFromList(key, list -> {
            var enumValue = list.popSingle();
            return key.decode(enumValue.toUpperCase());
        });
    }

    /**
     * Utility method which adds the value 'true' for a given boolean key.
     * 
     * @param key
     * @return
     */
    private static <V> BiConsumer<ListParser<String>, DataStore> addBool(DataKey<Boolean> key) {
        return addValue(key, true);
    }

    private static void parseConfig(ListParser<String> arguments, DataStore config) {
        // Next element should be the configuration file
        Preconditions.checkArgument(!arguments.isEmpty(), "Expected configuration file after config flag");

        String configFilename = arguments.popSingle();

        // Get file
        File configFile = getConfigFile(configFilename);
        // Read file
        // File configFile = SpecsIo.existingFile(configFilename);

        String argsLine = "";
        try (LineStream lines = LineStream.newInstance(configFile)) {
            argsLine = lines.stream().collect(Collectors.joining(" "));
        }

        List<String> configArguments = ArgumentsParser.newCommandLine().parse(argsLine);

        // Add config arguments to current arguments
        arguments.add(configArguments);
    }

    private static File getConfigFile(String configFilename) {
        // Check if path represents a remote file
        String configFilenameLowerCase = configFilename.toLowerCase();
        if (configFilenameLowerCase.startsWith("http://") || configFilenameLowerCase.startsWith("https://")) {
            // Download file to repositories folder
            return SpecsIo.download(configFilename, EclipseRepo.getRepositoriesFolder());
        }

        // Interpret path as a local folder
        return SpecsIo.existingFile(configFilename);
    }

    public EclipseBuildArgumentsParser() {
    }

    public DataStore parseArguments(String[] args) {
        return parseArguments(Arrays.asList(args));
    }

    public DataStore parseArguments(List<String> args) {
        DataStore eclipseBuildConfig = DataStore.newInstance("EclipseBuild Config");

        // List with items that will be consumed during parsing of arguments
        ListParser<String> currentArgs = new ListParser<>(args);
        List<EclipseRepo> eclipseRepos = new ArrayList<>();
        while (!currentArgs.isEmpty()) {
            String currentArg = currentArgs.popSingle();

            // Check if there is a flag for the current string
            if (ARGUMENTS_PARSER.containsKey(currentArg)) {
                ARGUMENTS_PARSER.get(currentArg).accept(currentArgs, eclipseBuildConfig);
                continue;
            }

            // No mapping found, argument must be the path to a repository
            EclipseRepo eclipseRepo = new EclipseRepoParser(currentArg).parse(currentArgs);
            eclipseRepos.add(eclipseRepo);
        }

        // Add found repositories
        eclipseBuildConfig.add(EclipseBuildKeys.ECLIPSE_REPOS, eclipseRepos);

        // Add build number
        eclipseBuildConfig.add(EclipseBuildKeys.BUILD_NUMBER, SpecsSystem.createBuildNumber());

        return eclipseBuildConfig;
    }

    public static void printHelpMessage() {
        StringBuilder message = new StringBuilder();
        message.append("EclipseBuild");
        var buildNumber = SpecsSystem.getBuildNumber();
        if (buildNumber != null) {
            message.append(" (build " + buildNumber + ")");
        }
        message.append("\n");

        message.append("Generates and runs ANT scripts for Eclipse Java projects\n\n");

        message.append("Usage: <folder> [-i <ivySetting>] [-u <userLibraries>] <folder> [-i...\n\n");
        message.append(
                "Default files that will be searched for in the root of the repository folders if no flag is specified:\n");
        message.append(" ").append(getDefaultUserLibraries()).append(" - Eclipse user libraries\n");
        message.append(" ").append(getDefaultIvySettingsFile()).append(" - Ivy settings file\n");
        message.append(" ").append(getDefaultIgnoreProjectsFile())
                .append(" - Text file with list of projects to ignore (one project name per line)\n");

        message.append("\nAdditional options:\n");
        message.append(" --help: shows this help message\n");
        message.append(" --build: builds the project\n");
        message.append(" --clean: cleans the temporary folder\n");
        message.append(" --list: shows the dependencies of the specified project\n");
        message.append(" --project <name>: the name of the Eclipse project to compile\n");
        message.append(" --main <full classname>: the class of the project with the main function\n");
        message.append(" --jar <type>: the type of JAR to generate. Available options: repack, subfolder, zip\n");
        message.append(" --jarname <name>: the name of the generated JAR (or zip file)\n");
        message.append(
                " --config <config>: specify a configuration file (local or remote). A configuration file is a plain text file with EclipseBuild commands\n");
        message.append(
                " --jvm-javac: [DEPRECATED - This option is now always on] executes 'javac' in the current JVM. If flag is not active, each 'javac' execution is forked. This can make builds slower, but it is usually necessary when EclipseBuild is used to execute the build (instead of just generating the ANT file)\n");
        message.append(
                " --test: Executes the unit tests (implies --build). All classes whose name ends in '...Test' are considered to be classes that contain JUnit test cases.\n");
        /*
        
        ARGUMENTS_PARSER.put("--main", EclipseBuildArgumentsParser.addString(EclipseBuildKeys.MAIN_CLASS));
        ARGUMENTS_PARSER.put("--config", EclipseBuildArgumentsParser::parseConfig);
        }
         */

        EclipseBuildLog.info(message.toString());
    }

}
