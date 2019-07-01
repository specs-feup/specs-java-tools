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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.suikasoft.jOptions.DataStore.ADataClass;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;

import com.google.gson.Gson;

public class MedsInfo extends ADataClass<MedsInfo> {

    public static final DataKey<List<String>> MEDS = KeyFactory.list("MEDS", String.class);
    public static final DataKey<List<String>> DOSES = KeyFactory.list("DOSES", String.class);

    public String toJson() {
        var meds = get(MEDS);
        var doses = get(DOSES);

        Map<String, Object> mainObject = new HashMap<String, Object>();

        mainObject.put("meds", meds);
        mainObject.put("doses", doses);

        Gson gson = new Gson();

        return gson.toJson(mainObject);
    }

}
