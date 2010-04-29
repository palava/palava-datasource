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
import java.sql.Wrapper;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.enhydra.jdbc.standard.StandardXADataSource;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;

/**
 * Guicified {@link StandardXADataSource}.
 * 
 * @author Tobias Sarnowski
 * @author Willi Schoenborn
 */
public class XADataSource extends StandardXADataSource implements Initializable, Wrapper {
    
    private static final long serialVersionUID = -6866388110619809098L;

    @Inject
    private static Provider<TransactionManager> transactionManagerProvider;

    private Provider<Context> contextProvider;

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
        @Named(DataSourceConfig.DRIVER) String driver,
        @Named(DataSourceConfig.URL) String url,
        @Named(DataSourceConfig.USER) String user,
        @Named(DataSourceConfig.PASSWORD) String password,
        @Named(DataSourceConfig.POOL_MAX) int poolMax,
        @Named(DataSourceConfig.POOL_MIN) int poolMin) {
        
        this.contextProvider = Preconditions.checkNotNull(contextProvider, "ContextProvider");
        this.jndiName = Preconditions.checkNotNull(jndiName, "JndiName");
        this.driver = Preconditions.checkNotNull(driver, "Driver");
        this.url = Preconditions.checkNotNull(url, "Url");
        this.user = Preconditions.checkNotNull(user, "User");
        this.password = Preconditions.checkNotNull(password, "Password");
        this.poolMax = Preconditions.checkNotNull(poolMax, "PoolMax");
        this.poolMin = Preconditions.checkNotNull(poolMin, "PoolMin");
    }

    @Override
    public void initialize() throws LifecycleException {
        final Context context = contextProvider.get();

        try {
            this.setDriverName(driver);
        } catch (SQLException e) {
            throw new LifecycleException(e);
        }

        setUrl(url);
        setUser(user);
        setPassword(password);

        setMaxCon(poolMax);
        setMinCon(poolMin);

        setTransactionManager(transactionManagerProvider.get());

        try {
            context.bind(jndiName, this);
        } catch (NamingException e) {
            throw new LifecycleException(e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        setTransactionManager(transactionManagerProvider.get());
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
