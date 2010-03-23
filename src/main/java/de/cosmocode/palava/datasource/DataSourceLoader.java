/**
 * palava - a java-php-bridge
 * Copyright (C) 2007-2010  CosmoCode GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package de.cosmocode.palava.datasource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import org.enhydra.jdbc.standard.StandardXADataSource;
import org.infinispan.transaction.lookup.JBossStandaloneJTAManagerLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Tobias Sarnowski
 */
class DataSourceLoader implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceLoader.class);

    private static final JBossStandaloneJTAManagerLookup lookup = new JBossStandaloneJTAManagerLookup();

    private static Context ctx;
    private String jndiName;
    private String driver;
    private String url;
    private String user;
    private String password;

    @Inject
    public DataSourceLoader(
            Context ctx,
            @Named(DataSourceConfig.JNDI_NAME) String jndiName,
            @Named(DataSourceConfig.DRIVER) String driver,
            @Named(DataSourceConfig.URL) String url,
            @Named(DataSourceConfig.USER) String user,
            @Named(DataSourceConfig.PASSWORD) String password
    ) {
        this.ctx = ctx;
        this.jndiName = jndiName;
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public void initialize() throws LifecycleException {
        ExtendedXADataSource xads = new ExtendedXADataSource();

        try {
            xads.setDriverName(driver);
        } catch (SQLException e) {
            throw new LifecycleException(e);
        }
        xads.setUrl(url);
        xads.setUser(user);
        xads.setPassword(password);

        try {
            ctx.bind(jndiName, xads);
        } catch (NamingException e) {
            throw new LifecycleException(e);
        }
    }

    private static class ExtendedXADataSource extends StandardXADataSource { // XAPOOL
        @Override
        public Connection getConnection() throws SQLException {

            if (getTransactionManager() == null) { // although already set before, it results null again after retrieving the datasource by jndi
                TransactionManager tm;  // this is because the TransactionManager information is not serialized.
                try {
                    tm = lookup.getTransactionManager();
                } catch (Exception e) {
                    throw new SQLException(e);
                }
                setTransactionManager(tm);  //  resets the TransactionManager on the datasource retrieved by jndi,
                //  this makes the datasource JTA-aware
            }

            // According to Enhydra documentation, here we must return the connection of our XAConnection
            // see http://cvs.forge.objectweb.org/cgi-bin/viewcvs.cgi/xapool/xapool/examples/xapooldatasource/DatabaseHelper.java?sortby=rev
            return super.getXAConnection().getConnection();
        }

        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
}
