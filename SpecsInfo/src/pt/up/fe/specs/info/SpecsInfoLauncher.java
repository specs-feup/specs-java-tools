/**
 * Copyright 2019 SPeCS.
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

package pt.up.fe.specs.info;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;

import pt.up.fe.specs.ant.tasks.Sftp;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperties;

public class SpecsInfoLauncher {

    public static final DataKey<String> SPREADSHEET_ID = KeyFactory.string("spreadsheetId");
    public static final DataKey<String> CREDENTIALS = KeyFactory.string("credentials");
    public static final DataKey<String> CLEAN = KeyFactory.string("clean");

    public static final DataKey<String> UPLOAD_TO_SERVER = KeyFactory.string("uploadToServer");
    public static final DataKey<String> LOGIN = KeyFactory.string("login");
    public static final DataKey<String> PASS = KeyFactory.string("pass");
    public static final DataKey<String> HOST = KeyFactory.string("host");
    public static final DataKey<String> PORT = KeyFactory.string("port");

    private final static String DESTINATION_FOLDER = "/var/www/html/test/db";

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        Collector collector = new XmlCollector();

        File propertiesFile = SpecsIo.existingFile("specs-info.properties");
        SpecsProperties properties = SpecsProperties.newInstance(propertiesFile);

        String spreadsheetId = properties.get(SPREADSHEET_ID);
        File credentials = properties.getExistingFile(CREDENTIALS).orElseThrow();

        List<String> dblpLinks = GoogleSheets.getMembersDblp(spreadsheetId, credentials);

        SpecsLogs.info("Found " + dblpLinks.size() + " DBLP links");

        List<String> dblpUsers = dblpLinks.stream().map(SpecsDblp::getDblpUserFromUrl).filter(user -> user != null)
                .collect(Collectors.toList());

        List<File> filesForUpload = new ArrayList<>();

        // Information about publications
        filesForUpload.addAll(collector.collectFromDblp(dblpUsers));

        // Information about members
        GroupMembers groupMembers = new GroupMembers(spreadsheetId, credentials);
        filesForUpload.addAll(groupMembers.collectInformation());

        // Upload files
        if (properties.getBoolean(UPLOAD_TO_SERVER)) {
            for (File outputFile : filesForUpload) {

                // Upload file
                new Sftp().set(Sftp.LOGIN, properties.get(LOGIN)).set(Sftp.PASS, properties.get(PASS))
                        .set(Sftp.HOST, properties.get(HOST)).set(Sftp.PORT, properties.get(PORT))
                        .set(Sftp.DESTINATION_FOLDER, DESTINATION_FOLDER).set(Sftp.FILE_TO_TRANSFER, outputFile).run();
            }
        }

        // Clean
        if (properties.getBoolean(CLEAN)) {
            for (File outputFile : filesForUpload) {
                SpecsIo.delete(outputFile);
            }
        }
    }
}

// TODO: remove
// testGson();
//
// }

// private static void testGson() {
//
//
// System.out.println("\n");
// Map<String, String> map = new HashMap<>();
//
//
// map.put("Name", "qwe");
// map.put("Affiliation", "asd");
// map.put("Context", "zxc");
// map.put("Twitter", "laskd");
//
//
// System.out.println(SpecsGson.toJson(map));
//
// }
//
// }

// --------------------------------
