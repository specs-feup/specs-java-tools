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
    public static final DataKey<String> LOGIN = KeyFactory.string("login");
    public static final DataKey<String> PASS = KeyFactory.string("pass");
    public static final DataKey<String> HOST = KeyFactory.string("host");
    public static final DataKey<String> PORT = KeyFactory.string("port");

    private final static String DESTINATION_FOLDER = "/var/www/html/resources";

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        File propertiesFile = SpecsIo.existingFile("bibtex-merger.properties");
        SpecsProperties properties = SpecsProperties.newInstance(propertiesFile);

        String spreadsheetId = properties.get(SPREADSHEET_ID);

        List<String> dblpLinks = GoogleSheets.getMembersDblp(spreadsheetId);

        SpecsLogs.info("Found " + dblpLinks.size() + " DBLP links");
        List<String> dblpUsers = dblpLinks.stream()
                .map(SpecsDblp::getDblpUserFromUrl)
                .filter(user -> user != null)
                .collect(Collectors.toList());

        // List<String> dblpUsers = Arrays.asList("Bispo:Jo=atilde=o", "Paulino:Nuno_Miguel_Cardanha");
        String mergedBib = BibtexCollector.fromDblpUsers(dblpUsers);

        // Make sure output file exists, clear it
        File outputFile = new File("specs.bib");

        SpecsLogs.info("Writing Bibtex file '" + outputFile.getAbsolutePath() + "'");
        SpecsIo.write(outputFile, mergedBib);

        // Upload Bibtex file
        new Sftp().set(Sftp.LOGIN, properties.get(LOGIN))
                .set(Sftp.PASS, properties.get(PASS))
                .set(Sftp.HOST, properties.get(HOST))
                .set(Sftp.PORT, properties.get(PORT))
                .set(Sftp.DESTINATION_FOLDER, DESTINATION_FOLDER)
                .set(Sftp.FILE_TO_TRANSFER, outputFile)
                .run();
        //

        // int exitCode = success ? 0 : 1;
        // System.exit(exitCode);

        // Clean
        SpecsIo.delete(outputFile);
    }

}
