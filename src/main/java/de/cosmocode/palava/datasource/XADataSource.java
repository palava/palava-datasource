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
public class XADataSource extends StandardXADataSource implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(XADataSource.class);

    @Inject
    public static Provider<TransactionManager> transactionManagerProvider;

    public Provider<Context> contextProvider;

    // configuration
    private String jndiName;
    private String driver;
    private String url;
    private String user;
    private String password;
    private int poolMax;
    private int poolMin;



    public XADataSource() {
        // used for jndi deserialization
    }

    @Inject
    public XADataSource(
            Provider<Context> contextProvider,
            @Named(DataSourceConfig.JNDI_NAME) String jndiName,
            @Named(DataSourceConfig.DRIVER)
            String driver,
            @Named(DataSourceConfig.URL) String url,
            @Named(DataSourceConfig.USER) String user,
            @Named(DataSourceConfig.PASSWORD) String password,
            @Named(DataSourceConfig.POOL_MAX) int poolMax,
            @Named(DataSourceConfig.POOL_MIN) int poolMin
    ) {
        super();
        this.contextProvider = contextProvider;
        this.jndiName = jndiName;
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        this.poolMax = poolMax;
        this.poolMin = poolMin;
    }

    @Override
    public void initialize() throws LifecycleException {
        Context ctx = contextProvider.get();

        try {
            this.setDriverName(driver);
        } catch (SQLException e) {
            throw new LifecycleException(e);
        }

        this.setUrl(url);
        this.setUser(user);
        this.setPassword(password);

        this.setMaxCon(poolMax);
        this.setMinCon(poolMin);

        this.setTransactionManager(transactionManagerProvider.get());

        try {
            ctx.bind(jndiName, this);
        } catch (NamingException e) {
            throw new LifecycleException(e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        this.setTransactionManager(transactionManagerProvider.get());
        return super.getConnection();
    }

    @Override
    public <T> T unwrap(Class<T> tClass) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return false;
    }
}
