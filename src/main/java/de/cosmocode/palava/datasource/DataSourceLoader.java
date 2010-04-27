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

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.enhydra.jdbc.standard.StandardXADataSource;
import org.infinispan.transaction.lookup.JBossStandaloneJTAManagerLookup;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;

/**
 * TODO add comment
 * 
 * @author Tobias Sarnowski
 */
class DataSourceLoader implements Initializable {

    // FIXME why static?
    private static final JBossStandaloneJTAManagerLookup lookup = new JBossStandaloneJTAManagerLookup();

    // FIXME why static?
    private static Context ctx;
    private String jndiName;
    private String driver;
    private String url;
    private String user;
    private String password;

    @Inject
    public DataSourceLoader(Context ctx,
        @Named(DataSourceConfig.JNDI_NAME) String jndiName,
        @Named(DataSourceConfig.DRIVER) String driver,
        @Named(DataSourceConfig.URL) String url,
        @Named(DataSourceConfig.USER) String user,
        @Named(DataSourceConfig.PASSWORD) String password) {
        this.ctx = ctx;
        this.jndiName = jndiName;
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public void initialize() throws LifecycleException {
        final ExtendedXADataSource dataSource = new ExtendedXADataSource();

        try {
            dataSource.setDriverName(driver);
        } catch (SQLException e) {
            throw new LifecycleException(e);
        }
        
        dataSource.setUrl(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);

        try {
            ctx.bind(jndiName, dataSource);
        } catch (NamingException e) {
            throw new LifecycleException(e);
        }
    }

    /**
     * TODO comment
     *
     * @author Tobias Sarnowski
     */
    //FIXME why public?
    public static class ExtendedXADataSource extends StandardXADataSource {
        
        private static final long serialVersionUID = 1186545063691205746L;

        @Override
        public Connection getConnection() throws SQLException {
            // although already set before, it results null again after retrieving the datasource by jndi
            if (getTransactionManager() == null) {
                // this is because the TransactionManager information is not serialized.
                final TransactionManager manager;
                try {
                    manager = lookup.getTransactionManager();
                /* CHECKSTYLE:OFF */
                } catch (Exception e) {
                /* CHECKSTYLE:ON */
                    throw new SQLException(e);
                }
                // resets the TransactionManager on the datasource retrieved by jndi,
                setTransactionManager(manager);
                // this makes the datasource JTA-aware
            }

            // According to Enhydra documentation, here we must return the connection of our XAConnection
            // see http://cvs.forge.objectweb.org/
            // cgi-bin/viewcvs.cgi/xapool/xapool/examples/xapooldatasource/DatabaseHelper.java?sortby=rev
            return getXAConnection().getConnection();
        }

        //FIXME useless method
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        //FIXME useless method
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
}
