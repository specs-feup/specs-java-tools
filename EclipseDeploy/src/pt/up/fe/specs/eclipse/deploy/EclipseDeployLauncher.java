/**
 * Copyright 2023 SPeCS.
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

package pt.up.fe.specs.eclipse.deploy;

import java.util.Arrays;

import org.suikasoft.jOptions.JOptionsUtils;
import org.suikasoft.jOptions.app.App;
import org.suikasoft.jOptions.persistence.XmlPersistence;
import org.suikasoft.jOptions.storedefinition.StoreDefinition;

import pt.up.fe.specs.util.SpecsSystem;

public class EclipseDeployLauncher {

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        var name = "EclipseDeploy " + SpecsSystem.getBuildNumber();
        var definition = StoreDefinition.newInstanceFromInterface(EclipseDeploy.class);
        var persistence = new XmlPersistence(definition);
        var kernel = new EclipseDeploy();

        var app = App.newInstance(name, definition, persistence, kernel);

        JOptionsUtils.executeApp(app, Arrays.asList(args));
    }
}
