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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.suikasoft.GsonPlus.SpecsGson;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.collections.AccumulatorMap;
import pt.up.fe.specs.util.utilities.ProgressCounter;

public class UpRepoParseHtml {

    private static final String UP_REPO_URL = "https://repositorio-aberto.up.pt";

    private static final Type FILE_LIST_TYPE = new TypeToken<List<File>>() {
    }.getType();

    private static final String ROMAN_NUMERALS = "M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})";
    private static final Pattern ROMAN_NUMERALS_REGEX = Pattern.compile(ROMAN_NUMERALS);

    // private static final Pattern QUOTE_REGEX = Pattern
    // .compile("[\"“'](.+)[\"”']\\W*,?\\W*(.+)\\W*(" + ROMAN_NUMERALS + ")?\\W*\\n",
    // Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern QUOTE_REGEX = Pattern
            .compile("[\"“']((.|\\W)+)[\"”']\\W*,?\\W*(.+)\\W*(" + ROMAN_NUMERALS + ")?\\W*\\n",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern TOC_REGEX = Pattern.compile("índice|indice|index|resumo|contents",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern QUOTES_CHARACTERS = Pattern.compile("[\"“”']");

    private static final Map<File, List<String>> QUOTES = new HashMap<>();
    private static final List<File> NO_QUOTES = new ArrayList<>();

    private static final Map<File, String> QUOTES_V2 = new HashMap<>();
    private static final Map<File, String> DROPPED_QUOTE_CANDIDATES = new HashMap<>();

    private static final List<String> QUOTES_V3 = new ArrayList<>();

    private static final int PAGES_TO_TEXT_THRESHOLD = 10;

    private static int skippedThesis = 0;
    private static int downloadedThesis = 0;
    private static int downloadedFiles = 0;

    private final Map<String, String> thesisHandles;
    private int parsedHandles;

    public UpRepoParseHtml() {
        this.thesisHandles = new LinkedHashMap<>();
        this.parsedHandles = 0;
    }

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();
        // extractThesisHandles();
        // downloadThesisWebpages();
        // downloadThesis();
        // processThesisPfds();
        // extractThesisPfdsToTxt();
        // processThesisPdf(new File("H:\\Scrapping\\UpRepoThesis\\thesisInfo\\100069\\35590.pdf"));
        // tesctThesisPdf();

        // processThesisTxts();

        List<Quote> quotes = extractQuotes();
        quotesStats(quotes);

        // countFeupThesis();
    }

    public static void countFeupThesis() {
        var thesisFolder = new File("H:\\Scrapping\\UpRepoThesis\\thesisTxt");

        Set<String> feupThesis = SpecsIo.getFiles(thesisFolder).stream()
                .map(file -> file.getName().split("_")[0])
                .collect(Collectors.toSet());

        System.out.println("NUMBER OF THESIS: " + feupThesis.size());
    }

    public static void quotesStats(List<Quote> quotes) {
        var accMap = new AccumulatorMap<String>();

        quotes.stream().forEach(quote -> accMap.add(quote.getAuthor()));

        System.out.println("TOTAL Quotes: " + quotes.size());
        System.out.println("Diff authors: " + accMap.getAccMap().size());

        int threshold = 10;
        for (String author : accMap.getAccMap().keySet()) {
            if (accMap.getCount(author) < threshold) {
                continue;
            }

            System.out.println(author + ": " + accMap.getCount(author));
        }

        int max = -1;
        String maxAuthor = null;
        for (String author : accMap.getAccMap().keySet()) {
            if (accMap.getCount(author) > max) {
                max = accMap.getCount(author);
                maxAuthor = author;
            }
        }

        System.out.println("Max author: " + maxAuthor + " (" + max + ")");
    }

    public static List<Quote> extractQuotes() {
        List<String> pages = new Gson().fromJson(SpecsIo.read(new File("H:\\Scrapping\\UpRepoThesis\\quotes_v3.json")),
                List.class);

        List<Quote> quotes = new ArrayList<>();
        for (var page : pages) {
            var quote = SpecsStrings.getRegex(page, QUOTE_REGEX);

            if (quote.isEmpty()) {
                continue;
            }

            // Remove all elements that are empty, or roman numerals
            // quote = quote.stream()
            // .filter(string -> !string.isBlank())
            // .filter(string -> !SpecsStrings.matches(string, ROMAN_NUMERALS_REGEX))
            // .collect(Collectors.toList());
            System.out.println("\nQUOTE:\n");
            // System.out.println(quote.stream().collect(Collectors.joining("'\n'", "'", "'")));

            List<String> parsedQuote = new ArrayList<>();
            for (var quotePart : quote) {
                if (quotePart.isBlank()) {
                    // System.out.println("FILTERED");
                    continue;
                }

                // Skip very short lines
                if (quotePart.length() < 5) {
                    continue;
                }

                // System.out.println("'" + quotePart + "'");

                parsedQuote.add(quotePart);
            }

            // Ignore if no text
            if (parsedQuote.isEmpty()) {
                continue;
            }

            Quote newQuote = Quote.parse(parsedQuote);

            quotes.add(newQuote);
            System.out.println(newQuote);
            // quotes.add(quote);
        }

        // System.out.println(quotes.stream().map(List::toString).collect(Collectors.joining("\n")));
        System.out.println("Found " + quotes.size() + " quotes");

        return quotes;
    }

    public static void processThesisTxts() {
        List<File> thesisTxts = SpecsIo.getFiles(new File("H:\\Scrapping\\UpRepoThesis\\thesisTxt"));

        var counter = new ProgressCounter(thesisTxts.size());
        for (var thesisTxt : thesisTxts) {
            System.out.println("Processing " + thesisTxt + " " + counter.next());
            processThesisTxtV2(thesisTxt);
        }

        // Save complete version
        SpecsIo.write(new File("H:\\Scrapping\\UpRepoThesis\\quotes_v3.json"), SpecsGson.toJson(QUOTES_V3));
    }

    public static void processThesisTxtV2(File thesisTxt) {
        // System.out.println("FUILE: " + SpecsIo.read(thesisTxt));
        List<String> pages = new Gson().fromJson(SpecsIo.read(thesisTxt), List.class);
        // System.out.println("PAGES: " + pages);

        int minimumSize = 50;
        int maximumSize = 300;
        int initialIgnorePages = 3;
        int saveEvery = 100;

        int currentPage = 0;
        int smallestPage = -1;
        int smallestPageIndex = -1;
        for (var page : pages) {
            currentPage++;

            // Ignore a set of initial pages
            if (currentPage <= initialIgnorePages) {
                continue;
            }

            // Strip white space
            // var normalizedPage = page.strip();
            var normalizedPage = page.replaceAll("\\s", "");

            // Ignore if empty
            if (normalizedPage.isEmpty()) {
                continue;
            }

            // Ignore if less than minimum size or higher than maximum size
            if (normalizedPage.length() < minimumSize || normalizedPage.length() > maximumSize) {
                // System.out.println("Ignoring page " + currentPage + " since it is smaller (" +
                // normalizedPage.length()
                // + ") than the minimum size (" + minimumSize + "): " + normalizedPage);
                continue;
            }

            // Ignore if does not contain a quote
            if (!SpecsStrings.matches(normalizedPage, QUOTES_CHARACTERS)) {
                continue;
            }

            // Store page
            QUOTES_V3.add(page);
        }

        // Save quotes
        if (QUOTES_V3.size() % saveEvery == 0) {
            SpecsIo.write(new File("H:\\Scrapping\\UpRepoThesis\\quotes_v3.json"), SpecsGson.toJson(QUOTES_V3));
        }

    }

    public static void processThesisTxt(File thesisTxt) {
        // System.out.println("FUILE: " + SpecsIo.read(thesisTxt));
        List<String> pages = new Gson().fromJson(SpecsIo.read(thesisTxt), List.class);
        // System.out.println("PAGES: " + pages);

        int minimumSize = 50;
        int maximumSize = 300;
        int initialIgnorePages = 3;

        int currentPage = 0;
        int smallestPage = -1;
        int smallestPageIndex = -1;
        for (var page : pages) {
            currentPage++;

            // Ignore a set of initial pages
            if (currentPage <= initialIgnorePages) {
                continue;
            }

            // Strip white space
            // var normalizedPage = page.strip();
            var normalizedPage = page.replaceAll("\\s", "");

            // Ignore if empty
            if (normalizedPage.isEmpty()) {
                continue;
            }

            // Ignore if less than minimum size or higher than maximum size
            if (normalizedPage.length() < minimumSize || normalizedPage.length() > maximumSize) {
                // System.out.println("Ignoring page " + currentPage + " since it is smaller (" +
                // normalizedPage.length()
                // + ") than the minimum size (" + minimumSize + "): " + normalizedPage);
                continue;
            }

            // Store if smallest
            if (smallestPage == -1 || normalizedPage.length() < smallestPage) {
                smallestPage = normalizedPage.length();
                smallestPageIndex = currentPage - 1;
            }

        }

        if (smallestPageIndex == -1) {
            System.out.println("No page found");
            return;
        }

        var citationCandidate = pages.get(smallestPageIndex);

        if (citationCandidate.length() > maximumSize) {
            System.out.println(
                    "Dropping citation candidate, size (" + citationCandidate.length()
                            + ") larger than maximum allowed (" + maximumSize + "):\n" + citationCandidate);

            DROPPED_QUOTE_CANDIDATES.put(thesisTxt, citationCandidate);
            SpecsIo.write(new File("H:\\Scrapping\\UpRepoThesis\\dropped_quote_candidates.json"),
                    SpecsGson.toJson(DROPPED_QUOTE_CANDIDATES));

            return;
        }

        System.out.println("CITATION CANDIDATE:");
        System.out.println(citationCandidate);
        System.out.println("SIZE: " + pages.get(smallestPageIndex).length());

        QUOTES_V2.put(thesisTxt, citationCandidate);
        SpecsIo.write(new File("H:\\Scrapping\\UpRepoThesis\\quotes_v2.json"), SpecsGson.toJson(QUOTES_V2));

        /*
        if (foundQuote) {
            System.out.println("\nFound quote " + QUOTES.size() + " at page " + currentPage + ", saving");
            System.out.println(QUOTES.get(thesisTxt));
            SpecsIo.write(new File("H:\\Scrapping\\UpRepoThesis\\quotes.json"), SpecsGson.toJson(QUOTES));
        } else {
            System.out.println("No quote found");
            NO_QUOTES.add(thesisTxt);
            SpecsIo.write(new File("H:\\Scrapping\\UpRepoThesis\\no_quotes_list.json"),
                    SpecsGson.toJson(NO_QUOTES));
        }
        */
    }

    /**
     * Extracts PDF pages to strings.
     * 
     * @param thesisPdf
     */
    public static void extractThesisPfdsToTxt(File thesisPdf) {
        File outputFolder = SpecsIo.mkdir("H:\\Scrapping\\UpRepoThesis\\thesisTxt");

        PDFParser parser;
        try {
            parser = new PDFParser(new RandomAccessFile(thesisPdf, "r"));

            // Setting this to false, otherwise it might hang while attempting self-healing
            parser.setLenient(false);
            parser.parse();
        } catch (Exception e) {
            SpecsLogs.msgInfo("Could not parse PDF file '" + thesisPdf + "':\n" + e);
            e.printStackTrace();
            return;
        }

        try (COSDocument cosDoc = parser.getDocument(); PDDocument pdDoc = new PDDocument(cosDoc)) {

            PDFTextStripper pdfStripper = new PDFTextStripper();

            int currentPage = 0;
            List<String> pages = new ArrayList<>();
            for (var page : pdDoc.getPages()) {
                currentPage++;

                var onePageDoc = new PDDocument();
                onePageDoc.addPage(page);

                var pageText = pdfStripper.getText(onePageDoc);

                pages.add(pageText);

                if (currentPage >= PAGES_TO_TEXT_THRESHOLD) {
                    break;
                }

                onePageDoc.close();
            }

            String suffix = SpecsIo.removeExtension(thesisPdf);
            String parent = thesisPdf.getParentFile().getName();

            var filename = parent + "_" + suffix + ".json";

            // var filename = SpecsIo.removeExtension(thesisPdf) + ".json";
            var file = new File(outputFolder, filename);
            SpecsIo.write(file, SpecsGson.toJson(pages));
            SpecsLogs.info("Written " + file);
            cosDoc.close();
            pdDoc.close();
        } catch (Exception e) {
            SpecsLogs.msgInfo("Could not save PDF pages to TXT:\n" + e);
            e.printStackTrace();
            return;
        }

    }

    private static boolean parsePdf(PDFParser parser) {
        try {
            parser.parse();
        } catch (Exception e) {
            throw new RuntimeException("Could not parse PDF", e);
        }
        return true;
    }

    public static void processThesisPdf(File thesisPdf) {
        // File thesisPdf = new File("H:\\Scrapping\\UpRepoThesis\\thesisInfo\\99986\\138145.pdf");

        // PDFParser parser = null;
        // PDDocument pdDoc = null;
        // COSDocument cosDoc = null;
        // PDFTextStripper pdfStripper;

        PDFParser parser;
        try {
            parser = new PDFParser(new RandomAccessFile(thesisPdf, "r"));
            // Setting this to false, otherwise it might hang while attempting self-healing
            parser.setLenient(false);
            parser.parse();
        } catch (Exception e) {
            // throw new RuntimeException("Could not open PDF file '" + thesisPdf + "'", e);
            SpecsLogs.msgInfo("Could not parse PDF file '" + thesisPdf + "':\n" + e);
            e.printStackTrace();
            return;
        }

        // PDF parsing may sometimes "hang"
        // Boolean parseResult = SpecsSystem.get(SpecsSystem.getFuture(() -> UpRepoParseHtml.parsePdf(parser)), 1,
        // TimeUnit.SECONDS);
        //
        // if (parseResult == null) {
        // SpecsLogs.info("Timeout while parsing the PDF, stopping");
        // return;
        // }

        try (COSDocument cosDoc = parser.getDocument(); PDDocument pdDoc = new PDDocument(cosDoc)) {

            // parser.parse();
            // cosDoc = parser.getDocument();
            PDFTextStripper pdfStripper = new PDFTextStripper();
            // pdDoc = new PDDocument(cosDoc);

            int pagesThreshold = 20;
            int currentPage = 0;
            boolean foundQuote = false;
            for (var page : pdDoc.getPages()) {
                currentPage++;

                var onePageDoc = new PDDocument();
                onePageDoc.addPage(page);

                var pageText = pdfStripper.getText(onePageDoc);

                // System.out.println(pageText);

                // Check if we are at table of contents
                if (SpecsStrings.matches(pageText, TOC_REGEX)) {
                    SpecsLogs.info("Found table of contents at page " + currentPage + " , stopping");
                    break;
                } else {
                    // SpecsLogs.info("No TOC:\n" + pageText);
                }

                List<String> matches = SpecsStrings.getRegex(pageText, QUOTE_REGEX);

                if (!matches.isEmpty()) {
                    foundQuote = true;
                    QUOTES.put(thesisPdf, matches);
                    break;
                }

                if (currentPage > pagesThreshold) {
                    break;
                }

                onePageDoc.close();
            }

            if (foundQuote) {
                System.out.println("\nFound quote " + QUOTES.size() + " at page " + currentPage + ", saving");
                System.out.println(QUOTES.get(thesisPdf));
                SpecsIo.write(new File("H:\\Scrapping\\UpRepoThesis\\quotes.json"), SpecsGson.toJson(QUOTES));
            } else {
                System.out.println("No quote found");
                NO_QUOTES.add(thesisPdf);
                SpecsIo.write(new File("H:\\Scrapping\\UpRepoThesis\\no_quotes_list.json"),
                        SpecsGson.toJson(NO_QUOTES));
            }
            // String parsedText = pdfStripper.getText(pdDoc);
            // System.out.println("SIZE: " + parsedText.length());

            // System.out.println(parsedText.replaceAll("[^A-Za-z0-9. ]+", ""));
        } catch (Exception e) {
            throw new RuntimeException("Could not extract quote from PDF", e);
            // e.printStackTrace();
            // try {
            // if (cosDoc != null)
            // cosDoc.close();
            // if (pdDoc != null)
            // pdDoc.close();
            // } catch (Exception e1) {
            // e1.printStackTrace();
            // }

        }
    }

    public static void processThesisPfds() {
        var thesisPdfs = getThesisPdfs();

        var counter = new ProgressCounter(thesisPdfs.size());
        for (var thesisPdf : thesisPdfs) {
            System.out.println("Processing " + thesisPdf + " " + counter.next());
            // processThesisPdf(thesisPdf);
            extractThesisPfdsToTxt(thesisPdf);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<File> getThesisPdfs() {
        // Check if cached file exists
        File cachedFile = new File("H:\\Scrapping\\UpRepoThesis\\thesisPdfs.json");

        if (cachedFile.isFile()) {
            return (List<File>) new Gson().fromJson(SpecsIo.read(cachedFile), FILE_LIST_TYPE);
        }

        File inputFolder = SpecsIo.existingFolder("H:\\Scrapping\\UpRepoThesis\\thesisInfo");

        // Collect PDF files inside
        System.out.println("Collecting PDFs...");
        var thesisPdfs = SpecsIo.getFolders(inputFolder).stream()
                .flatMap(thesisFolder -> SpecsIo.getFiles(thesisFolder, "pdf").stream())
                .collect(Collectors.toList());

        // Save file
        System.out.println("THESIS PDFS: " + thesisPdfs);
        System.out.println("THESIS PDF: " + thesisPdfs.get(0).getClass());

        SpecsIo.write(cachedFile, new Gson().toJson(new ArrayList<>(thesisPdfs), FILE_LIST_TYPE));

        return thesisPdfs;
    }

    public static void downloadThesis() {
        File inputFolder = SpecsIo.existingFolder("H:\\Scrapping\\UpRepoThesis\\thesisHtml");
        File outputFolder = SpecsIo.mkdir("H:\\Scrapping\\UpRepoThesis\\thesisInfo");

        // For each thesis, create a folder with the files and metadata
        var thesisHtmls = SpecsIo.getFiles(inputFolder);
        var counter = new ProgressCounter(thesisHtmls.size());

        for (var thesisHtml : thesisHtmls) {
            System.out.println("Processing thesis html " + counter.next());

            processThesisHtml(thesisHtml, outputFolder);

            // Check if disk space is running to low
            var freeSpaceMb = outputFolder.getFreeSpace() / (1024 * 1024);
            // 1Gb
            if (freeSpaceMb < 1024) {
                System.out.println("Stopping since available disk space is low");
                break;
            }
        }

        System.out.println("Skipped thesis: " + skippedThesis);
        System.out.println("Downloaded thesis: " + downloadedThesis);
        System.out.println("Downloaded files: " + downloadedFiles);
    }

    private static void processThesisHtml(File thesisHtml, File baseFolder) {

        var document = Jsoup.parse(SpecsIo.read(thesisHtml));

        var tables = document.getElementsByTag("table");
        if (tables.size() != 2) {
            SpecsLogs.warn("Expected 2 tables, found " + tables.size());
            return;
        }

        var metadataTable = tables.get(0);
        var metadataRows = metadataTable.getElementsByTag("tr");

        Map<String, String> metadata = new HashMap<>();
        for (var metadataRow : metadataRows) {
            var metadataCols = metadataRow.getElementsByTag("td");
            if (metadataCols.size() != 2) {
                SpecsLogs.warn("Expected 2 columns, found " + metadataCols.size());
                return;
            }

            metadata.put(metadataCols.get(0).text(), metadataCols.get(1).text());
        }

        // Save information
        File outputFolder = SpecsIo.mkdir(new File(baseFolder, thesisHtml.getName()));
        File metadataJson = new File(outputFolder, "data.json");
        SpecsIo.write(metadataJson, SpecsGson.toJson(metadata));

        // For now, only save files of FEUP thesis

        String collections = metadata.get("Appears in Collections:");

        if (collections == null) {
            SpecsLogs.warn("Could not find field 'Appears in Collections:'");
            skippedThesis++;
            return;
        }

        if (!collections.contains("FEUP")) {
            SpecsLogs.info("Skipping: " + collections);
            skippedThesis++;
            return;
        }

        downloadedThesis++;

        var filesTable = tables.get(1);
        for (var file : filesTable.getElementsByAttributeValue("headers", "t1")) {
            String fileHtml = file.html();
            int hrefIndex = fileHtml.indexOf("href=\"");
            if (hrefIndex == -1) {
                SpecsLogs.warn("Expected to find 'href=\"': " + fileHtml);
                continue;
            }

            String fileUrl = fileHtml.substring(hrefIndex + "href=\"".length());

            int endIndex = fileUrl.indexOf("\">");
            if (endIndex == -1) {
                SpecsLogs.warn("Expected to find '\">': " + fileUrl);
                continue;
            }

            fileUrl = fileUrl.substring(0, endIndex);

            String url = UP_REPO_URL + fileUrl;
            SpecsIo.download(url, outputFolder);
            downloadedFiles++;
        }

    }

    public static void downloadThesisWebpages() {
        File inputFile = new File("H:\\Scrapping\\UpRepoThesis\\thesisHandles.json");

        @SuppressWarnings("unchecked")
        var handles = (Map<String, String>) new Gson().fromJson(SpecsIo.read(inputFile), Map.class);

        File outputFolder = SpecsIo.mkdir("H:\\Scrapping\\UpRepoThesis\\thesisHtml");
        var counter = new ProgressCounter(handles.values().size());
        System.out.println("Downloading webpages for " + counter.getMaxCount() + " thesis");
        for (var handle : handles.values()) {
            System.out.println(counter.next());
            String url = UP_REPO_URL + handle;
            SpecsIo.download(url, outputFolder);
        }

    }

    public static void extractThesisHandles() {
        File inputFolder = new File("H:\\Scrapping\\UpRepoThesis\\searchHtml");

        var htmlFiles = SpecsIo.getFiles(inputFolder, "html");
        var upParser = new UpRepoParseHtml();
        var counter = new ProgressCounter(htmlFiles.size());
        for (var htmlFile : htmlFiles) {
            System.out.println(counter.next());
            Document doc = Jsoup.parse(SpecsIo.read(htmlFile));

            Elements thesisHandles = doc.getElementsByAttributeValue("headers", "t2");

            thesisHandles.stream().forEach(upParser::parseThesisHandle);

        }

        System.out.println("HANDLES: " + upParser.parsedHandles);
        SpecsIo.write(new File("H:\\Scrapping\\UpRepoThesis\\thesisHandles.json"),
                SpecsGson.toJson(upParser.thesisHandles));
    }

    public void parseThesisHandle(Element thesisTd) {
        // HTML should be an href to the thesis handle
        String thesisHandle = thesisTd.html().strip();

        if (!thesisHandle.startsWith("<a href=\"")) {
            SpecsLogs.warn("Expected string to start with '<a href=\\\"': " + thesisHandle);
            return;
        }

        thesisHandle = thesisHandle.substring("<a href=\"".length());
        int endIndex = thesisHandle.indexOf("\">");

        if (endIndex == -1) {
            SpecsLogs.warn("Expected string to have '\">': " + thesisHandle);
            return;
        }

        String handle = thesisHandle.substring(0, endIndex);

        String title = thesisHandle.substring(endIndex + "\">".length());
        int titleEndIndex = title.indexOf("</a>");
        if (endIndex == -1) {
            SpecsLogs.warn("Expected string to have '</a>': " + title);
            return;
        }

        title = title.substring(0, titleEndIndex);

        thesisHandles.put(title, handle);

        parsedHandles++;
    }
}
