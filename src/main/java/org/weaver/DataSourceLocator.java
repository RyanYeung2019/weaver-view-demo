package org.weaver;

import javax.sql.DataSource;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DataSourceLocator {

    private final ApplicationContext applicationContext;

    public DataSourceLocator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public DataSource get(String key) {
        String beanName = "dataSource." + key;
        try {
            return applicationContext.getBean(beanName, DataSource.class);
        } catch (NoSuchBeanDefinitionException ex) {
            throw new IllegalArgumentException("unknown datasource key: " + key, ex);
        }
    }
}
