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

package pt.up.fe.specs.info.medcalc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import pt.up.fe.specs.util.SpecsIo;

public class MedCalc {

    private static final String APPLICATION_NAME = "MedCalc Google Sheet Fetch";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart. If modifying these scopes, delete your previously
     * saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    public static MedsInfo getMedsInfo(String spreadsheetId, File credentials) {

        List<String> meds = new ArrayList<>();
        List<String> doses = new ArrayList<>();

        try {

            final String range = "Meds!A2:B";
            Sheets service = getSheets(credentials);

            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                System.out.println("No data found.");
            } else {
                System.out.println("Found " + values.size() + " values");
                for (List<?> row : values) {
                    // System.out.println("ROW SIZE: " + row.size());

                    if (row.size() < 2) {
                        continue;
                    }

                    // Print columns I and K, which correspond to indices 0 and 2.
                    System.out.printf("%s, %s\n", row.get(0), row.get(1));
                    meds.add(row.get(0).toString());
                    doses.add(row.get(1).toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Problems while retrieving members DBLPs", e);
        }

        MedsInfo medsInfo = new MedsInfo();
        medsInfo.set(MedsInfo.MEDS, meds);
        medsInfo.set(MedsInfo.DOSES, doses);

        return medsInfo;
    }

    public static Sheets getSheets(File credentials) {

        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                    getCredentials(HTTP_TRANSPORT, credentials))
                            .setApplicationName(APPLICATION_NAME)
                            .build();
        } catch (Exception e) {
            throw new RuntimeException("Problems while retrieving members Google Sheet", e);
        }
    }

    /**
     * Creates an authorized Credential object.
     * 
     * @param HTTP_TRANSPORT
     *            The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException
     *             If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, File credentials)
            throws IOException {
        InputStream in = SpecsIo.toInputStream(credentials);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                        .setAccessType("offline")
                        .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
