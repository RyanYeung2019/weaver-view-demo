package org.weaver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.alibaba.druid.pool.DruidDataSource;


@Configuration
public class Config{
	
	private static final Logger log = LoggerFactory.getLogger(Config.class);

	@Value("${spring.datasource.url}")
	String url;

	@Value("${spring.datasource.username}")
	String username;
	
	@Value("${spring.datasource.password}")
	String password;
	
	@Value("${spring.datasource.driver-class-name}")
	String driverClassName;
	
	@Bean
	public DataSource dataSource() {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setDriverClassName(driverClassName);
		chkAndRunIniData(dataSource,"view_demo.view_demo.department","/ini_data.sql");
		return dataSource;
	}

    private void chkAndRunIniData(DataSource dataSource,String checkTable,String iniData){
        try (Connection connection = dataSource.getConnection();
        	Statement statement = connection.createStatement();
        	InputStream sqlFile = this.getClass().getResourceAsStream(iniData);
        	InputStreamReader sqlFileIs = new InputStreamReader(sqlFile,Charset.forName("UTF-8"));
        	BufferedReader sqlReader = new BufferedReader(sqlFileIs)) {
			DatabaseMetaData metaData = connection.getMetaData();
			String[] cst = checkTable.split("[.]");

            try (ResultSet tables = metaData.getTables(cst[0].isEmpty()?null:cst[0], cst[1].isEmpty()?null:cst[1], cst[2].isEmpty()?null:cst[2], null)) {
//            	if(tables.next()) return;
            }
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = sqlReader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("--")) {
                    sqlBuilder.append(line);
                    if (line.endsWith(";")) {
                        String sql = sqlBuilder.toString().replace(";", "");
                        statement.execute(sql);
                        sqlBuilder.setLength(0);
                    }
                }
            }
            if (sqlBuilder.length() > 0) {
                String sql = sqlBuilder.toString();
                statement.execute(sql);
            }
        }catch(Exception e) {
        	e.printStackTrace();
        	
        	throw new RuntimeException(e);
        }
        log.info("Data is initializated!");    	
    	
    }
	
}
