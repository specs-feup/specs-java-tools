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

package pt.up.fe.specs.scrapper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.stringsplitter.StringSplitter;
import pt.up.fe.specs.util.stringsplitter.StringSplitterRules;
import pt.up.fe.specs.util.utilities.Replacer;

public class UpRepoDocuments {

    // TODO: Parameterize link
    private static final String BASE_URL = "https://repositorio-aberto.up.pt/browse?type=type&sort_by=1&order=ASC&rpp=<NUM_RESULTS>&etal=-1&value=Disserta%C3%A7%C3%A3o&offset=<OFFSET>";

    public static String getDefaultUrl() {
        return getUrl();
        // return BASE_URL.replace("<OFFSET>", "20");
    }

    public static String getUrl(String... tagValuePairs) {
        SpecsCheck.checkArgument(tagValuePairs.length % 2 == 0,
                () -> "Number of arguments must be even: " + tagValuePairs.length);

        Map<UpRepoLinkTag, String> tagsValues = new HashMap<>();
        for (int i = 0; i < tagValuePairs.length; i += 2) {
            var tag = UpRepoLinkTag.valueOf(tagValuePairs[i]);
            var value = tagValuePairs[i + 1];
            tagsValues.put(tag, value);
        }

        return getUrl(tagsValues);
    }

    public static String getUrl(Map<UpRepoLinkTag, String> tagsValues) {
        Replacer url = new Replacer(BASE_URL);

        for (var tag : UpRepoLinkTag.values()) {
            String value = tagsValues.get(tag);
            if (value == null) {
                value = tag.getDefaultValue();
            }

            url.replace(tag.getTag(), value);
        }
        return url.toString();
    }

    public static void main(String[] args) {
        File outputFolder = new File("H:\\Scrapping\\UpRepoThesis");

        String html = SpecsIo.getUrl(getDefaultUrl());

        Document doc = Jsoup.parse(html);

        Elements resultsElements = doc.getElementsByClass("panel-heading");
        if (resultsElements.size() != 1) {
            throw new RuntimeException(
                    "Expected one element with 'panel-heading' class, found " + resultsElements.size());
        }

        int totalDocuments = extractDocuments(resultsElements.get(0).text());

        int resultPerPage = 100;
        // int currentOffset = 0;
        System.out.println("Total docs: " + totalDocuments);

        for (int i = 0; i < totalDocuments; i += resultPerPage) {
            String url = getUrl("OFFSET", Integer.toString(i), "NUM_RESULTS", Integer.toString(resultPerPage));
            String newHtml = SpecsIo.getUrl(url);

            File outputFile = new File(outputFolder, "up_repo_" + i + "_" + (i + resultPerPage) + ".html");
            SpecsIo.write(outputFile, newHtml);
            // Document newDoc = Jsoup.parse(newHtml);
            // System.out.println("RESULTS TEXT: " + getResultText(newDoc));
        }

    }

    private static String getResultText(Document doc) {
        Elements resultsElements = doc.getElementsByClass("panel-heading");
        if (resultsElements.size() != 1) {
            throw new RuntimeException(
                    "Expected one element with 'panel-heading' class, found " + resultsElements.size());
        }

        return resultsElements.get(0).text();
    }

    private static int extractDocuments(String text) {
        // Example: 'Showing results 21 to 40 of 32805 < previous next >'
        // StringParser parser = new StringParser(text);
        // parser.apply(StringParsers::checkStringStarts, "Showing results ");
        // parser.apply(StringParsers.);

        // System.out.println("PARSER: " + parser);

        StringSplitter parser = new StringSplitter(text);
        // Total is after 6 strings
        parser.parse(StringSplitterRules::string);
        parser.parse(StringSplitterRules::string);
        parser.parse(StringSplitterRules::string);
        parser.parse(StringSplitterRules::string);
        parser.parse(StringSplitterRules::string);
        parser.parse(StringSplitterRules::string);
        Integer total = parser.parse(StringSplitterRules::integer);

        return total;
    }
}
