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
import com.google.inject.Key;
import com.google.inject.name.Names;
import de.cosmocode.palava.core.inject.AbstractRebindModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.Properties;

/**
 * @author Tobias Sarnowski
 */
public abstract class AbstractDataSourceModule extends AbstractRebindModule {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractDataSourceModule.class);

	private final String name;
	private final Key<DataSource> key;

	private final DataSourceConfig config;


	/**
	 * @return provides the datasource provider to use
	 */
	protected abstract Class<? extends DataSourceProvider> getDataSourceProvider();


    public AbstractDataSourceModule(String name) {
        this(Names.named(name), name);
    }

    public AbstractDataSourceModule(Class<? extends Annotation> annotation, String name) {
        this.key = Key.get(DataSource.class, Preconditions.checkNotNull(annotation, "Annotation"));
        this.name = Preconditions.checkNotNull(name, "Name");
        this.config = DataSourceConfig.named(name);
    }

    public AbstractDataSourceModule(Annotation annotation, String name) {
        this.key = Key.get(DataSource.class, Preconditions.checkNotNull(annotation, "Annotation"));
        this.name = Preconditions.checkNotNull(name, "Name");
        this.config = DataSourceConfig.named(name);
    }

    @Override
    protected void configuration() {
        LOG.trace("Binding DataSource from Provider {} with configuration for {} using name {}", new Object[] {
            getDataSourceProvider(), key, name});

        bind(String.class).annotatedWith(Names.named(DataSourceConfig.UNIQUE)).toInstance(name);

	    bind(String.class).annotatedWith(Names.named(DataSourceConfig.JNDI_NAME))
			    .to(Key.get(String.class, Names.named(config.jndiName())));

	    bind(String.class).annotatedWith(Names.named(DataSourceConfig.DRIVER))
			    .to(Key.get(String.class, Names.named(config.driver())));
	    bind(Properties.class).annotatedWith(Names.named(DataSourceConfig.PROPERTIES))
			    .to(Key.get(Properties.class, Names.named(config.properties())));

        bind(int.class).annotatedWith(Names.named(DataSourceConfig.POOL_MAX))
		        .to(Key.get(int.class, Names.named(config.poolMax())));
	    bind(int.class).annotatedWith(Names.named(DataSourceConfig.POOL_MIN))
			    .to(Key.get(int.class, Names.named(config.poolMin())));
    }

    @Override
    protected void optionals() {

    }

    @Override
    protected void bindings() {
        bind(getDataSourceProvider()).asEagerSingleton();
        bind(key).toProvider(getDataSourceProvider());
    }	

    @Override
    protected void expose() {
        expose(key);
    }

}
