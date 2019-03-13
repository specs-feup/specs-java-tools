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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.suikasoft.GsonPlus.SpecsGson;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.SpecsXml;

public class XmlCollector implements Collector {
    private static final String ID_KEY = "id";
    private static final String TYPE_KEY = "type";

    private static final Pattern END_DIGITS = Pattern.compile("^.*\\s\\d\\d\\d\\d$");
    /*
    public static String fromDblpUsers(List<String> dblpUsers) {
        // Create temporary folder for XMLs
        File tempFolder = SpecsIo.getTempFolder("specs_xmls");
    
        for (String user : dblpUsers) {
            String xmlUrl = SpecsDblp.getDblpXmlUrl(user);
    
            File xmlFile = SpecsIo.download(xmlUrl, tempFolder);
            Document document = SpecsXml.getXmlRoot(xmlFile);
            System.out.println("XML: " + document);
        }
    
        return null;
    }
    */

    @Override
    public List<File> collectFromDblp(List<String> dblpUsers) {
        // Create temporary folder for XMLs
        File tempFolder = SpecsIo.getTempFolder("specs_info");

        Map<String, Map<String, Object>> entries = new HashMap<>();
        Set<String> memberNames = new HashSet<>();
        for (String user : dblpUsers) {
            String xmlUrl = SpecsDblp.getDblpXmlUrl(user);

            File xmlFile = SpecsIo.download(xmlUrl, tempFolder);

            Document document = SpecsXml.getXmlRoot(xmlFile);

            parseEntries(document, entries, memberNames);
        }

        // Make sure output file exists, clear it
        File outputFile = new File("specs_bib.json");

        SpecsLogs.info("Writing JSON file '" + outputFile.getAbsolutePath() + "' obtained from the XMLs");
        SpecsIo.write(outputFile, SpecsGson.toJson(entries));
        // System.out.println("JSON:\n" + SpecsIo.read(outputFile));

        List<String> membersList = new ArrayList<>(memberNames);
        File membersFile = new File("specs_members.json");
        SpecsLogs.info("Writing JSON file '" + membersFile.getAbsolutePath() + "' obtained from the XMLs");
        SpecsIo.write(membersFile, SpecsGson.toJson(membersList));

        return Arrays.asList(outputFile, membersFile);
    }

    private void parseEntries(Document document, Map<String, Map<String, Object>> parsedEntries,
            Set<String> memberNames) {

        // Get root element (dblpperson)
        Element element = document.getDocumentElement();
        memberNames.add(SpecsXml.getAttribute(element, "name")
                .map(XmlCollector::parseAuthorName)
                .orElseThrow(() -> new RuntimeException("Expected to find attribute 'name")));
        // int counter = 0;
        for (Element entry : SpecsXml.getElementChildren(element, "r")) {

            Map<String, Object> parsedEntry = new HashMap<>();

            List<Element> fields = SpecsXml.getElementChildren(entry);
            Element typeField = fields.get(0);
            String id = SpecsXml.getAttribute(typeField, "key").orElseThrow(
                    () -> new RuntimeException("Expected first element of entry to have a 'key' attribute"));

            // Check if key is already present
            if (parsedEntries.containsKey(id)) {
                continue;
            }

            parsedEntries.put(id, parsedEntry);
            parsedEntry.put(ID_KEY, id);

            // Name of the first node is the type
            parsedEntry.put(TYPE_KEY, typeField.getNodeName());

            // Add remaining children
            for (int i = 1; i < fields.size(); i++) {
                Element field = fields.get(i);
                addValue(parsedEntry, field.getNodeName(), field.getTextContent());
            }

            // parsedEntries.put(key, new HashMap<>());
            // System.out.println("KEY:" + id);
            // System.out.println("PARSED ENTRY:" + parsedEntry);

            // for (Element entryField : SpecsXml.getElementChildren(entry)) {
            // System.out.println("EntryField: " + entryField.getNodeName());
            // System.out.println("EntryField text: " + entryField.getTextContent());
            //
            // }
            // List<Element> entryType = SpecsXml.getElementChildren(entry);

            // System.out.println("Entry: " + entry.getNodeName());
            // for (Element entryTypeChild : entryType) {
            // System.out.println("EntryTypeChild: " + entryTypeChild.getNodeName());
            // }
            // Each entry has a single child, an element named after the type of the entry
            // SpecsCheck.checkArgument(entryType.size() == 1,
            // () -> "Expected entry to have a single element, has " + entryType.size());

        }
    }

    private void addValue(Map<String, Object> parsedEntry, String key, String value) {

        if (key.equals("author")) {
            value = parseAuthorName(value);
        }

        Object previousValue = parsedEntry.get(key);
        if (previousValue == null) {
            parsedEntry.put(key, value);
            return;
        }

        // There is already a value, update it
        if (previousValue instanceof String) {
            List<String> values = new ArrayList<>();
            values.add((String) previousValue);
            values.add(value);
            parsedEntry.put(key, values);
            return;
        }

        if (previousValue instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> values = (List<String>) previousValue;
            values.add(value);
            return;
        }
    }

    /**
     * Custom corrections to some values.
     * 
     * @param value
     * @return
     */
    public static String parseAuthorName(String value) {
        // if (value.endsWith(" 0001")) {
        // System.out.println("PARSING " + value);
        if (SpecsStrings.matches(value, END_DIGITS)) {
            // if (value.equals("Luís Reis 0001")) {
            // System.out.println("MATCHED! " + value);
            // System.out.println("Returning " + value.substring(0, value.length() - 5));
            return value.substring(0, value.length() - 5);
        }

        return value;
    }

    @Override
    public String getFormat() {
        return "json";
    }

}
