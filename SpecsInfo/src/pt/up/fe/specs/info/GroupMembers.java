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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.suikasoft.GsonPlus.JsonPersistence;

import com.google.api.services.sheets.v4.model.ValueRange;

public class GroupMembers {

	private static final String SPECS_MEMBERS_JSON_PATH = "real_specs_members.json";
	private final String spreadsheetId;
	private final File credentials;

	public GroupMembers(String spreadsheetId, File credentials) {
		this.spreadsheetId = spreadsheetId;
		this.credentials = credentials;
	}

	public List<File> collectInformation() {

		List<File> infoFiles = new ArrayList<>();

		SpecsSheets service = GoogleSheets.getSpecsSheets(spreadsheetId, credentials);

		ValueRange response = service.getValueRange("Members!B3:T");
		List<List<Object>> values = response.getValues();
		if (values == null || values.isEmpty()) {
			throw new RuntimeException("No data found.");
		}

		List<Map<String, String>> mapsave = new ArrayList<>();
		for (List<?> row : values) {
			Map<String, String> map = new HashMap<>();

			if (row.size() == 0) {
				continue;
			}

			map.put("Name", (String) row.get(0));

			map.put("Affiliation", (String) row.get(1));

			map.put("Context", (String) row.get(4));

			map.put("Current Status", (String) row.get(6));

			map.put("Position", (String) row.get(2));

			map.put("Visiting Period", getValue(3, row));

			mapsave.add(map);
		}

		JsonPersistence jp = new JsonPersistence();

		File membersFile = new File(SPECS_MEMBERS_JSON_PATH);
		jp.write(membersFile, mapsave);

		infoFiles.add(membersFile);
		return infoFiles;
	}

	private String getValue(int index, List<?> row) {

		if (row.size() > index) {

			String value = (String) row.get(index);
			if (!value.isEmpty()) {
				return value;
			}
		}

		return null;
	}
}
