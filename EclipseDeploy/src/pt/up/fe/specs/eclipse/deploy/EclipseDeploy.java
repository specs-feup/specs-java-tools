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

import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Utils.GuiHelperConverter;
import org.suikasoft.jOptions.app.AppKernel;
import org.suikasoft.jOptions.values.SetupList;

import pt.up.fe.specs.eclipse.JarType;
import pt.up.fe.specs.eclipse.Tasks.TaskUtils;

public class EclipseDeploy implements AppKernel {

    public static final DataKey<String> WORKSPACE_FOLDER = KeyFactory.string("WorkspaceFolder");
    public static final DataKey<String> PROJECT_NAME = KeyFactory.string("ProjectName");
    public static final DataKey<String> NAME_OF_OUTPUT_JAR = KeyFactory.string("NameOfOutputJar");
    public static final DataKey<String> CLASS_WITH_MAIN = KeyFactory.string("ClassWithMain");
    public static final DataKey<JarType> OUTPUT_JAR_TYPE = KeyFactory.enumeration("OutputJarType", JarType.class);
    public static final DataKey<String> POM_INFO_FILE = KeyFactory.string("PomInfoFile");
    public static final DataKey<String> DEVELOPERS_XML = KeyFactory.string("DevelopersXml");
    public static final DataKey<SetupList> TASKS = KeyFactory.setupList("Tasks",
            GuiHelperConverter.toStoreDefinition(TaskUtils.getTasksList()));

    // public static final DataKey<DataStore> TASKS = KeyFactory.dataStore("Task_test",
    // GuiHelperConverter.toStoreDefinition(TaskUtils.getTasksList()).get(0));

    @Override
    public int execute(DataStore options) {

        return 0;
    }

}
