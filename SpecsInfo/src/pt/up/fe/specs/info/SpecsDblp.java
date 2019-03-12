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

import pt.up.fe.specs.util.SpecsLogs;

public class SpecsDblp {

    public static String getDblpBibtexUrl(String user) {
        char lowerCaseFirstChar = Character.toLowerCase(user.charAt(0));
        return "https://dblp.uni-trier.de/pers/tb2/" + lowerCaseFirstChar + "/" + user + ".bib";
    }

    public static String getDblpUserFromUrl(String dblpUrl) {
        int lastIndexOfSlash = dblpUrl.lastIndexOf('/');

        if (lastIndexOfSlash == -1) {
            SpecsLogs.msgWarn("Could not decode " + dblpUrl);
            return null;
        }

        return dblpUrl.substring(lastIndexOfSlash + 1);
    }

}
