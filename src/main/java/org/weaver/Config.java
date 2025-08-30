package org.weaver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

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
		chkAndRunIniData(dataSource,"department","/ini_data.sql");
		return dataSource;
	}

    private void chkAndRunIniData(DataSource dataSource,String checkTable,String iniData){
		Connection connection = DataSourceUtils.getConnection(dataSource);
		Statement statement = null;
		try {
			 statement = connection.createStatement();
			 try(InputStream sqlFile = this.getClass().getResourceAsStream(iniData);
	        	InputStreamReader sqlFileIs = new InputStreamReader(sqlFile,Charset.forName("UTF-8"));
	        	BufferedReader sqlReader = new BufferedReader(sqlFileIs)){
					DatabaseMetaData metaData = connection.getMetaData();
		    		String[] tableArray = checkTable.split("[.]");
		    		String catalog = connection.getCatalog();
		    		String database = null;
		    		String tableName = null;
		    		if(tableArray.length==1) {
		        		tableName = tableArray[0];
		    		}else {
		    			catalog = tableArray[0];
		        		tableName = tableArray[1];
		    		}
		        	try(ResultSet tables = metaData.getTables(catalog, database,tableName,null)){					
//		            	if(tables.next()) return;
		            }
		            StringBuilder sqlBuilder = new StringBuilder();
		            String line;
		            while ((line = sqlReader.readLine()) != null) {
		                line = line.trim();
		                if (!line.isEmpty() && !line.startsWith("--")) {
		                    sqlBuilder.append(line);
		                    if (line.endsWith(";")) {
		                    	try {
		                            String sql = sqlBuilder.toString().replace(";", "");
		                            log.debug(sql);
		                            statement.execute(sql);
		                    	}catch(Exception e) {
		                            	throw new RuntimeException(e);
		                    	}
		                        sqlBuilder.setLength(0);
		                    }
		                }
		            }
		            if (sqlBuilder.length() > 0) {
		                String sql = sqlBuilder.toString();
		                statement.execute(sql);
		            }				 
			 }catch(Exception e) {
				 throw new RuntimeException(e);
			 }
		}catch (SQLException ex) {
			JdbcUtils.closeStatement(statement);
			statement = null;			
			DataSourceUtils.releaseConnection(connection, dataSource);
			connection = null;
			log.error("error:",ex);
        	if(!ex.getMessage().contains("already attached")) {
            	throw new RuntimeException(ex);
        	}			
			throw new RuntimeException(ex);
		}finally {
			JdbcUtils.closeStatement(statement);
			DataSourceUtils.releaseConnection(connection, dataSource);
		}
        log.info("Data is initializated!");    	
    	
    }
	
}
