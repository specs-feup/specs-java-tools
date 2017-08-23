/**
 * Copyright 2017 SPeCS.
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

import pt.up.fe.specs.util.lazy.Lazy;
import pt.up.fe.specs.util.logging.SpecsLogger;

public class EclipseBuildLog extends SpecsLogger {

    private static final String LOGGER_NAME = buildLoggerName(EclipseBuildLog.class);
    private static final Lazy<EclipseBuildLog> LOGGER = buildLazy(EclipseBuildLog::new);

    public static EclipseBuildLog logger() {
        return LOGGER.get();
    }

    private EclipseBuildLog() {
        super(LOGGER_NAME);
    }

    public static void info(String message) {
        logger().msgInfo(message);
    }
}
