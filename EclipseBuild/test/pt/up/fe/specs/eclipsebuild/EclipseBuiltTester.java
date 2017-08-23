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

import java.util.Arrays;

import org.junit.Test;

public class EclipseBuiltTester {

    private static void testBuilder(String... args) {
        String[] realArgs = new String[args.length + 1];

        for (int i = 0; i < args.length; i++) {
            realArgs[i] = args[i];
        }

        // realArgs[args.length] = "--fork-javac";
        //
        // EclipseBuildLauncher.main(realArgs);
        EclipseBuildLauncher.main(args);
    }

    @Test
    public void buildSpecsUtils() {
        testBuilder("https://github.com/specs-feup/specs-java-libs.git", "--project", "jOptions", "--build", "--clean");

        // Option to fork javac
        // ProcessBuilder processBuilder = SpecsSystem.buildJavaProcess(EclipseBuildLauncher.class,
        // Arrays.asList("https://github.com/specs-feup/specs-java-libs.git", "--project",
        // "jOptions", "--build"));
        //
        // Map<String, String> env = processBuilder.environment();
        //
        // env.put("JAVA_HOME", "C:\\Program Files\\Java\\jdk1.8.0_131");
        //
        // SpecsSystem.runProcess(processBuilder, false, true);

        // String javaExecutable = "C:\\Program Files\\Java\\jdk1.8.0_131\\bin\\java.exe";
        // SpecsSystem.executeOnProcessAndWait(EclipseBuildLauncher.class, javaExecutable,
        // "https://github.com/specs-feup/specs-java-libs.git", "--project",
        // "jOptions", "--build");
        // System.out.println("FOLDER:" + SpecsIo.getWorkingDir().getAbsolutePath());
        // EclipseBuildLauncher
        // .main(new String[] { "https://github.com/specs-feup/specs-java-libs.git", "--project",
        // "jOptions", "--build" });

        // System.getProperties().put("java.home", "C:\\Program Files\\Java\\jdk1.8.0_131");

        // DeployUtils.runAnt(new File("build.xml"), "compile_SpecsUtils");
        // .main(new String[] { "https://github.com/specs-feup/specs-java-libs.git", "-project", "SpecsUtils" });
    }

    @Test
    public void buildSpecsJavaLibs() {
        EclipseBuildLauncher
                .main(new String[] { "https://github.com/specs-feup/specs-java-libs.git" });
    }

    @Test
    public void listDependencies() {
        // List without project
        EclipseBuildLauncher.execute(Arrays.asList("https://github.com/specs-feup/specs-java-libs.git", "--list"));

        // List with a project
        EclipseBuildLauncher.execute(Arrays.asList("https://github.com/specs-feup/specs-java-libs.git", "--project",
                "jOptions", "--list"));

        // List with a non-existing project
        EclipseBuildLauncher.execute(Arrays.asList("https://github.com/specs-feup/specs-java-libs.git", "--project",
                "dummy", "--list"));

        // For ClavaWeaver, for instance
        // Arrays.asList("https://github.com/specs-feup/specs-java-libs.git", "-list", "SpecsUtils");
    }

    @Test
    public void testConfigFile() {
        // Just a config file
        EclipseBuildLauncher.execute(Arrays.asList("--config", "eclipse.build"));

        // Add option after config file
        EclipseBuildLauncher.execute(Arrays.asList("--config", "eclipse.build", "--list"));
    }

    @Test
    public void testCommands() {
        // Project with commands
        EclipseBuildLauncher.execute(Arrays.asList("https://github.com/specs-feup/lara-framework",
                "https://github.com/specs-feup/specs-java-libs.git", "--build", "--project", "LARAC"));
    }

    @Test
    public void testMain() {
        // Project with main
        EclipseBuildLauncher.execute(Arrays.asList("https://github.com/specs-feup/lara-framework",
                "https://github.com/specs-feup/specs-java-libs.git", "--build", "--project", "LARAI",
                "--main",
                "larai.LaraI"));
    }
}
