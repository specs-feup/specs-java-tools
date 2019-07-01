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

public enum UpRepoLinkTag {

    NUM_RESULTS("20"),
    OFFSET("20");

    private final String defaultValue;

    private UpRepoLinkTag(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getTag() {
        return "<" + name() + ">";
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
