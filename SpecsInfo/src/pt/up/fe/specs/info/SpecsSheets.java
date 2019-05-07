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

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

public class SpecsSheets {

    private final String spreadsheetId;
    private final Sheets sheets;

    public SpecsSheets(String spreadsheetId, Sheets sheets) {
        this.spreadsheetId = spreadsheetId;
        this.sheets = sheets;
    }

    public Sheets getSheets() {
        return sheets;
    }

    public ValueRange getValueRange(String range) {

        try {
            return sheets.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
        } catch (Exception e) {
            throw new RuntimeException("Problem while retrieving range '" + range + "'", e);
        }
    }

}
