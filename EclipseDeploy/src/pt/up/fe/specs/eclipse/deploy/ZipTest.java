/**
 * Copyright 2023 SPeCS.
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

package pt.up.fe.specs.eclipse.deploy;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;

import pt.up.fe.specs.util.SpecsIo;

public class ZipTest {

    public static void main(String[] args) {
        var file = new File(
                "C:\\Users\\JBispo\\Work\\Repos\\Lab\\deploys\\JavaDeploys2\\ClavaWeaver\\eclipse-deploy.zip");

        Path zipFilePath = Paths.get(file.getAbsolutePath());

        try (FileSystem fs = FileSystems.newFileSystem(zipFilePath)) {

            // for (var rootPath : fs.getRootDirectories()) {
            // System.out.println("OATH: " + rootPath);
            // }
            var jarFiles = new ArrayList<Path>();

            Files.walkFileTree(fs.getRootDirectories().iterator().next(), Collections.emptySet(), 1,
                    new FileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                                throws IOException {
                            System.out.println("preVisitDirectory: " + dir);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            System.out.println("visitFile: " + file);

                            var ext = SpecsIo.getExtension(file.toString()).toLowerCase();
                            if ("jar".equals(ext)) {
                                jarFiles.add(file);
                            }

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc)
                                throws IOException {
                            System.out.println("visitFileFailed: " + file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                                throws IOException {
                            System.out.println("postVisitDirectory: " + dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });

            System.out.println("JAR FILES: " + jarFiles);
            /*
            Path source = fs.getPath("EclipseDeploy.jar");
            
            SpecsCheck.checkArgument(Files.exists(source), () -> "Could not find");
            
            try (FileSystem jar = FileSystems.newFileSystem(source)) {
                var manifest = jar.getPath("META-INF/MANIFEST.MF");
                System.out.println("MANIFEST EXISTS: " + Files.exists(manifest));
            
            }
            */

            // var zipInputStream = new ZipInputStream(Files.newInputStream(source));
            //
            // ZipEntry zipEntry;
            // while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            // System.out.println("ZIP ENTRY: " + zipEntry);
            // }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
