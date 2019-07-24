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

import java.util.List;
import java.util.stream.Collectors;

import pt.up.fe.specs.util.SpecsCollections;

public class Quote {

    private final String quote;
    private final String author;

    public Quote(String quote, String author) {
        this.quote = quote;
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public String getQuote() {
        return quote;
    }

    @Override
    public String toString() {
        return "\"" + quote + "\" - " + author;
    }

    public static Quote parse(List<String> quoteParts) {
        // Consider last element to be the author, separate other elements with spaces
        String author = SpecsCollections.last(quoteParts).strip();

        String quote = quoteParts.subList(0, quoteParts.size() - 1).stream()
                .map(String::strip)
                .collect(Collectors.joining(" "));

        return new Quote(quote, author);
    }
}
