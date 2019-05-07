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

import com.google.api.services.sheets.v4.model.ValueRange;

public class GroupMembers {

    private final String spreadsheetId;
    private final File credentials;

    public GroupMembers(String spreadsheetId, File credentials) {
        this.spreadsheetId = spreadsheetId;
        this.credentials = credentials;
    }

    public List<File> collectInformation() {
        List<File> infoFiles = new ArrayList<>();

        SpecsSheets service = GoogleSheets.getSpecsSheets(spreadsheetId, credentials);

        ValueRange response = service.getValueRange("Members!I3:K");
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            throw new RuntimeException("No data found.");
        }

        System.out.println("Members test");
        for (List<?> row : values) {
            // System.out.println("ROW SIZE: " + row.size());

            if (row.size() < 3) {
                continue;
            }

            // Print columns I and K, which correspond to indices 0 and 2.
            System.out.printf("%s, %s\n", row.get(0), row.get(2));
        }

        return infoFiles;
    }

}
