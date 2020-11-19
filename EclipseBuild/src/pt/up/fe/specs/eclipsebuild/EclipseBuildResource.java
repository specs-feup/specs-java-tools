/**
 * Copyright 2013 SPeCS Research Group.
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

package pt.up.fe.specs.eclipsebuild;

import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum EclipseBuildResource implements ResourceProvider {

    RESOLVE_TEMPLATE("resolve.xml.template"),
    JAR_TEMPLATE("jar.xml.template"),
    MAIN_TEMPLATE("main.xml.template"),
    DEPLOY_REPACK_TEMPLATE("deploy_repack.xml.template"),
    DEPLOY_SUBFOLDER_TEMPLATE("deploy_subfolder.xml.template"),
    DEPLOY_ZIP_TEMPLATE("zip.xml.template"),
    DEPLOY_COPY_TEMPLATE("copy.xml.template");

    private final static String RESOURCE_FOLDER = "eclipsebuild";

    private final String resource;

    private EclipseBuildResource(String resource) {
        this.resource = EclipseBuildResource.RESOURCE_FOLDER + "/" + resource;
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.Interfaces.ResourceProvider#getResource()
     */
    @Override
    public String getResource() {
        return resource;
    }

}
