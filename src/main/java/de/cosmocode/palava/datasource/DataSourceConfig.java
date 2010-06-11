/**
 * Copyright 2010 CosmoCode GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cosmocode.palava.datasource;

import com.google.common.base.Preconditions;

/**
 * Static constant holder class for datasource config key names.
 * 
 * @author Tobias Sarnowski
 */
public final class DataSourceConfig {

    public static final String PREFIX = "datasource.";

    public static final String UNIQUE = "unique";

    public static final String JNDI_NAME = "jndiName";
    
    public static final String DRIVER = "driver";

    public static final String PROPERTIES = "properties";
    
    public static final String POOL_MAX = "pool.max";
    
    public static final String POOL_MIN = "pool.min";

    private String prefix;

    private DataSourceConfig(String name) {
        Preconditions.checkNotNull(name, "Name");
        this.prefix = PREFIX + name + ".";
    }

    public static DataSourceConfig named(String name) {
        Preconditions.checkNotNull(name, "Name");
        return new DataSourceConfig(name);
    }

    public String jndiName() {
        return prefix + JNDI_NAME;
    }

    public String driver() {
        return prefix + DRIVER;
    }

    public String properties() {
        return prefix + PROPERTIES;
    }

    public String poolMax() {
        return prefix + POOL_MAX;
    }

    public String poolMin() {
        return prefix + POOL_MIN;
    }
    
}
