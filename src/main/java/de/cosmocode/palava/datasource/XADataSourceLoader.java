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

import bitronix.tm.resource.jdbc.PoolingDataSource;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import de.cosmocode.palava.core.lifecycle.Disposable;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author Tobias Sarnowski
 */
final class XADataSourceLoader implements Initializable, Disposable {
    private static final Logger LOG = LoggerFactory.getLogger(XADataSourceLoader.class);

    public Provider<Context> contextProvider;

    // configuration
    private String unique;
    private String jndiName;
    private String driver;
    private Properties properties;
    private int poolMax;
    private int poolMin;

    private PoolingDataSource ds;

    @Inject
    public XADataSourceLoader(
            Provider<Context> contextProvider,
            @Named(DataSourceConfig.UNIQUE) String unique,
            @Named(DataSourceConfig.JNDI_NAME) String jndiName,
            @Named(DataSourceConfig.DRIVER) String driver,
            @Named(DataSourceConfig.PROPERTIES) Properties properties,
            @Named(DataSourceConfig.POOL_MAX) int poolMax,
            @Named(DataSourceConfig.POOL_MIN) int poolMin
    ) {
        super();
        this.contextProvider = contextProvider;
        this.unique = unique;
        this.jndiName = jndiName;
        this.driver = driver;
        this.properties = properties;
        this.poolMax = poolMax;
        this.poolMin = poolMin;
    }

    @Override
    public void initialize() throws LifecycleException {
        Context ctx = contextProvider.get();

        ds = new PoolingDataSource();

        ds.setUniqueName(unique);
        ds.setClassName(driver);

        ds.setMaxPoolSize(poolMax);
        ds.setMinPoolSize(poolMin);

        ds.getDriverProperties().putAll(properties);

        ds.init();

        try {
            LOG.trace("Binding XADataSource {} to {} [{}]", new Object[]{unique, jndiName, ds});
            ctx.bind(jndiName, ds);
        } catch (NamingException e) {
            throw new LifecycleException(e);
        }
    }

    @Override
    public void dispose() throws LifecycleException {
        ds.close();
    }
}
