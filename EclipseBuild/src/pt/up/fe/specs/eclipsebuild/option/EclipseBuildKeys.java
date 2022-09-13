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

import java.util.ArrayList;
import java.util.List;

import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;

import pt.up.fe.specs.eclipsebuild.JarType;

public interface EclipseBuildKeys {

    /**
     * List of repositories with Eclipse projects.
     */
    DataKey<List<EclipseRepo>> ECLIPSE_REPOS = KeyFactory.generic("eclipse_build_repos",
            new ArrayList<>());

    /**
     * If true, just the help message instead of running the tool (--help, -h).
     */
    DataKey<Boolean> SHOW_HELP = KeyFactory.bool("eclipse_build_show_help");

    /**
     * If true, just shows list of dependencies of the specified project.
     */
    DataKey<Boolean> SHOW_LIST = KeyFactory.bool("eclipse_build_show_list");

    /**
     * If true, uses ANT to build the project.
     */
    DataKey<Boolean> BUILD = KeyFactory.bool("eclipse_build_build");

    /**
     * If true, uses ANT to build the project.
     */
    DataKey<Boolean> TEST = KeyFactory.bool("eclipse_build_test");

    /**
     * If true, cleans temporary folder before building the project.
     */
    DataKey<Boolean> CLEAN = KeyFactory.bool("eclipse_build_clean");

    /**
     * If specified, the name of the project to compile.
     */
    DataKey<String> PROJECT_NAME = KeyFactory.string("eclipse_build_project_name");

    /**
     * If specified, the main class of the project.
     */
    DataKey<String> MAIN_CLASS = KeyFactory.string("eclipse_build_project_main_class");

    /**
     * If true, executes javac in the current JVM. By default each javac execution is forked. This can make builds
     * slower, but it is usually necessary if EclipseBuild is used to execute the build.
     */
    DataKey<Boolean> JVM_JAVAC = KeyFactory.bool("eclipse_build_jvm_javac");

    DataKey<JarType> JAR_TYPE = KeyFactory.enumeration("jarType", JarType.class).setDefault(() -> JarType.REPACK);

    /**
     * The name of the JAR (or zip file)
     */
    DataKey<String> JAR_NAME = KeyFactory.string("jarName");

    /**
     * A build number that is created at the beginning of the execution and that can be used during the program.
     */
    DataKey<String> BUILD_NUMBER = KeyFactory.string("buildNumber");
}
