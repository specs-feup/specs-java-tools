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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXFormatter;
import org.jbibtex.BibTeXObject;
import org.jbibtex.BibTeXParser;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;

public class BibtexCollector implements Collector {

    public static String fromDblpUsers(List<String> dblpUsers) {

        // Create temporary folder for bibs
        File tempFolder = SpecsIo.getTempFolder("specs_bibtexs");

        BibTeXDatabase database = new BibTeXDatabase();

        // Download bibtexs
        // boolean success = true;
        for (String dblpUser : dblpUsers) {
            String url = SpecsDblp.getDblpBibtexUrl(dblpUser);
            File bibtex = SpecsIo.download(url, tempFolder);

            if (bibtex == null) {
                SpecsLogs.msgInfo("Could not download bibtex for user '" + dblpUser + "'");
                // success = false;
                continue;
            }
            // System.out.println("BIBTEX:" + SpecsIo.read(bibtex));

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(SpecsIo.toInputStream(bibtex),
                    SpecsIo.DEFAULT_CHAR_SET));) {

                BibTeXParser bibtexParser = new BibTeXParser();
                BibTeXDatabase currentDb = bibtexParser
                        .parse(reader);

                for (BibTeXObject obj : currentDb.getObjects()) {

                    if (!(obj instanceof BibTeXEntry)) {
                        database.addObject(obj);
                        continue;
                    }

                    BibTeXEntry entry = (BibTeXEntry) obj;

                    // Filter entries that do not have an author field
                    if (entry.getField(BibTeXEntry.KEY_AUTHOR) == null) {
                        continue;
                    }

                    database.addObject(entry);
                }

                // currentDb.getObjects().stream()
                // .filter(obj -> obj instanceof BibTeXEntry)
                // .map(obj -> (BibTeXEntry) obj)
                // .filter(entry -> entry.getField(BibTeXEntry.KEY_AUTHOR))
                // .forEach(database::addObject);
                // database.addObject(object);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                Writer writer = new BufferedWriter(new OutputStreamWriter(byteArray, SpecsIo.DEFAULT_CHAR_SET));) {

            new BibTeXFormatter().format(database, writer);

            return byteArray.toString(SpecsIo.DEFAULT_CHAR_SET);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // return success;
    }

    @Override
    public List<File> collectFromDblp(List<String> dblpUsers) {
        String mergedBib = BibtexCollector.fromDblpUsers(dblpUsers);

        // Make sure output file exists, clear it
        File outputFile = new File("specs.bib");

        SpecsLogs.info("Writing Bibtex file '" + outputFile.getAbsolutePath() + "'");
        SpecsIo.write(outputFile, mergedBib);

        return Arrays.asList(outputFile);
    }

    @Override
    public String getFormat() {
        return "bib";
    }

}
