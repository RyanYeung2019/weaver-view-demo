package org.weaver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import com.alibaba.druid.pool.DruidDataSource;

@Configuration
public class AutoBeanRegistry implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, ApplicationContextAware, SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(AutoBeanRegistry.class);

    private static final String DEFAULT_CHECK_TABLE = "view_demo.department";

    private Environment environment;

    private ApplicationContext applicationContext;

    private Map<String, Object> datasourceMap = new LinkedHashMap<>();

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        datasourceMap = Binder.get(environment)
                .bind("app.datasources", Bindable.mapOf(String.class, Object.class))
                .orElseGet(LinkedHashMap::new);
        String defaultKey = environment.getProperty("app.demo-db.default-key", "");

        for (Map.Entry<String, Object> entry : datasourceMap.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> item = asMap(entry.getValue());
            if (!toBoolean(item.get("enabled"))) {
                continue;
            }
            registerDataSource(registry, key, item, key.equals(defaultKey));
        }

        String defaultBeanName = "dataSource." + defaultKey;
        if (!defaultKey.isBlank() && registry.containsBeanDefinition(defaultBeanName) && !registry.isAlias("dataSource")
                && !registry.containsBeanDefinition("dataSource")) {
            registry.registerAlias(defaultBeanName, "dataSource");
        }
    }

    private void registerDataSource(BeanDefinitionRegistry registry, String key, Map<String, Object> item, boolean primary) {
        String beanName = "dataSource." + key;
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }

        String url = asString(item.get("url"));
        // Local Windows run often doesn't have docker's `/data` mount.
        // For sqlite we rewrite `/data/<file>` to `<projectRoot>/data/<file>`.
        if ("sqlite".equals(detectDialect(url))) {
            url = rewriteSqliteUrlIfNeeded(url);
            url = toAbsoluteSqliteJdbcUrl(url);
        }
        String username = asString(item.get("username"));
        String password = asString(item.get("password"));
        String driverClassName = asString(item.get("driver-class-name"));

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DruidDataSource.class);
        builder.addPropertyValue("url", url);
        if ("sqlite".equals(detectDialect(url))) {
            String filePath = sqliteFilesystemPathFromJdbcUrl(url);
            if (!filePath.isBlank()) {
                String attachSql = "ATTACH DATABASE '" + escapeSqlStringLiteral(filePath) + "' AS view_demo";
                builder.addPropertyValue("connectionInitSqls", List.of(attachSql));
            }
        }
        builder.addPropertyValue("username", username);
        builder.addPropertyValue("password", password);
        builder.addPropertyValue("driverClassName", driverClassName);
        BeanDefinition definition = builder.getBeanDefinition();
        definition.setPrimary(primary);

        log.info("url:{}", url);
        log.info("username:{}", username);
        log.info("password:{}", password);
        registry.registerBeanDefinition(beanName, definition);
        log.info("I am registered datasource bean {}", beanName);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // No-op
    }

    @Override
    public void afterSingletonsInstantiated() {
    	log.info("afterSingletonsInstantiated");
        boolean initEnabled = environment.getProperty("app.datasource-init.enabled", Boolean.class, true);
        if (!initEnabled) {
            log.info("datasource init disabled by app.datasource-init.enabled=false");
            return;
        }
        for (Map.Entry<String, Object> entry : datasourceMap.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> item = asMap(entry.getValue());
            if (!toBoolean(item.get("enabled"))) {
                continue;
            }
            initializeDataSourceIfRequired(key, item);
        }
    }

    private void initializeDataSourceIfRequired(String key, Map<String, Object> item) {
        String beanName = "dataSource." + key;
        String initScript = asString(item.get("init-script"));
        String checkTable = asString(item.getOrDefault("check-table", DEFAULT_CHECK_TABLE));
        String url = asString(item.get("url"));
        String driverClassName = asString(item.get("driver-class-name"));
        String username = asString(item.get("username"));
        String password = asString(item.get("password"));

        if ("sqlserver".equals(detectDialect(url))) {
            // SQL Server init script assumes database exists; create it if missing.
            ensureSqlServerDatabaseExists(url, driverClassName, username, password);
        }

        if ("sqlite".equals(detectDialect(url))) {
            // SQLite init requires the parent directory to exist; JDBC won't create it.
            ensureSqliteFileParentDirExists(url);
        }

        DataSource dataSource = applicationContext.getBean(beanName, DataSource.class);

        if (initScript.isBlank()) {
            log.info("skip init for {} because init-script is empty", beanName);
            return;
        }

        boolean exists = tableExists(dataSource, url, checkTable);
        if (!exists) {
            runSqlScript(dataSource, initScript);
            log.info("initialized {} with script {}", beanName, initScript);
            return;
        }

        // Older Oracle scripts created unquoted identifiers (DEP_KEY). weaver-view emits quoted "dep_key".
        if ("oracle".equals(detectDialect(url)) && oracleDemoNeedsQuotedLowercaseColumns(dataSource)) {
            log.info("re-initializing {}: Oracle {} has legacy column naming; running {}", beanName, checkTable, initScript);
            runSqlScript(dataSource, initScript);
            return;
        }

        log.info("skip init for {} because table {} already exists", beanName, checkTable);
    }

    /**
     * True when DEPARTMENT exists but has no case-sensitive {@code dep_key} column (quoted DDL uses {@code "dep_key"}).
     */
    private boolean oracleDemoNeedsQuotedLowercaseColumns(DataSource dataSource) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT 1 FROM user_tab_columns WHERE UPPER(table_name) = 'DEPARTMENT' AND column_name = 'dep_key'")) {
            return !rs.next();
        } catch (Exception ex) {
            log.warn("oracle column layout check failed, assuming no migration needed: {}", ex.getMessage());
            return false;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void ensureSqlServerDatabaseExists(String url, String driverClassName, String username, String password) {
        String dbName = extractSqlServerDatabaseName(url);
        if (dbName.isBlank() || "master".equalsIgnoreCase(dbName)) {
            return;
        }

        String masterUrl = buildSqlServerMasterUrl(url, dbName);
        try {
            if (driverClassName != null && !driverClassName.isBlank()) {
                Class.forName(driverClassName);
            }
            try (Connection connection = DriverManager.getConnection(masterUrl, username, password);
                    Statement st = connection.createStatement()) {
                String escapedLiteral = escapeSqlStringLiteral(dbName);
                String escapedIdentifier = escapeSqlServerIdentifier(dbName);
                st.execute("IF DB_ID('" + escapedLiteral + "') IS NULL CREATE DATABASE " + escapedIdentifier);
            }
        } catch (Exception ex) {
            throw new RuntimeException("ensure sqlserver database exists failed. db=" + dbName + ", url=" + url, ex);
        }
    }

    private String extractSqlServerDatabaseName(String url) {
        if (url == null) {
            return "";
        }
        // example: jdbc:sqlserver://host:1433;databaseName=view_demo;encrypt=false;trustServerCertificate=true
        Pattern pattern = Pattern.compile("(?i)(?:;|^)databaseName=([^;]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    private String buildSqlServerMasterUrl(String url, String currentDbName) {
        // replace the databaseName segment only
        if (url == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("(?i)(databaseName=)" + Pattern.quote(currentDbName));
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.replaceFirst(matcher.group(1) + "master");
        }
        return url;
    }

    private String escapeSqlStringLiteral(String s) {
        return s == null ? "" : s.replace("'", "''");
    }

    private String escapeSqlServerIdentifier(String identifier) {
        // use brackets: [name]
        String safe = identifier == null ? "" : identifier.replace("]", "]]");
        return "[" + safe + "]";
    }

    private boolean tableExists(DataSource dataSource, String url, String checkTable) {
        String[] tableArray = checkTable.split("\\.");
        String schema = tableArray.length > 1 ? tableArray[0] : null;
        String table = tableArray.length > 1 ? tableArray[1] : tableArray[0];

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            String dialect = detectDialect(url);
            if ("sqlite".equals(dialect)) {
                // sqlite init scripts use names like `view_demo.department` (with dot in table name).
                // So we must check using the original checkTable string.
                return sqliteTableExists(connection, checkTable);
            }

            DatabaseMetaData metaData = connection.getMetaData();
            if ("oracle".equals(dialect)) {
                String oracleSchema = schema == null || schema.isBlank()
                        ? safeUpper(connection.getSchema())
                        : safeUpper(schema);
                return metaTableExists(metaData, null, oracleSchema, safeUpper(table))
                        || metaTableExists(metaData, null, oracleSchema, table);
            }

            String catalog = connection.getCatalog();
            String lookupSchema = schema;
            if ("mysql".equals(dialect)) {
                catalog = schema != null ? schema : catalog;
                lookupSchema = null;
            }
            return metaTableExists(metaData, catalog, lookupSchema, table)
                    || metaTableExists(metaData, catalog, lookupSchema, safeUpper(table));
        } catch (Exception ex) {
            throw new RuntimeException("check table exists failed for " + checkTable, ex);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private boolean metaTableExists(DatabaseMetaData metaData, String catalog, String schema, String table) throws Exception {
        try (ResultSet resultSet = metaData.getTables(catalog, schema, table, new String[] { "TABLE", "VIEW" })) {
            return resultSet.next();
        }
    }

    private boolean sqliteTableExists(Connection connection, String tableName) throws Exception {
        // View SQL uses qualified names like view_demo.department. That requires the
        // view_demo alias on every pooled connection (see connectionInitSqls on the
        // sqlite DataSource). Detect init completion via view_demo.sqlite_master, not
        // main.sqlite_master (a bare "department" table would be a false positive).
        String pureTable = tableName;
        if (tableName != null && tableName.contains(".")) {
            String[] parts = tableName.split("\\.", 2);
            pureTable = parts[1];
        }
        String sqlAttached = "SELECT 1 FROM view_demo.sqlite_master WHERE type IN ('table','view') AND name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sqlAttached)) {
            ps.setString(1, pureTable);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            log.debug("sqlite table check via view_demo failed: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Use the same absolute file path the JDBC URL opens so ATTACH points at one shared file.
     */
    private String toAbsoluteSqliteJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null) {
            return null;
        }
        String lower = jdbcUrl.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("jdbc:sqlite:")) {
            return jdbcUrl;
        }
        String pathPart = jdbcUrl.substring("jdbc:sqlite:".length()).trim();
        if (pathPart.isEmpty()) {
            return jdbcUrl;
        }
        Path p = Paths.get(pathPart);
        if (!p.isAbsolute()) {
            p = Paths.get(System.getProperty("user.dir")).resolve(p).normalize();
        }
        return "jdbc:sqlite:" + p.toString().replace('\\', '/');
    }

    private String sqliteFilesystemPathFromJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null) {
            return "";
        }
        String lower = jdbcUrl.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("jdbc:sqlite:")) {
            return "";
        }
        return jdbcUrl.substring("jdbc:sqlite:".length()).trim();
    }

    private String rewriteSqliteUrlIfNeeded(String sqliteUrl) {
        if (sqliteUrl == null) {
            return null;
        }
        String raw = sqliteUrl.trim();
        String pathPart = raw;
        if (raw.toLowerCase(Locale.ROOT).startsWith("jdbc:sqlite:")) {
            pathPart = raw.substring("jdbc:sqlite:".length());
        }
        if (pathPart.isBlank()) {
            return sqliteUrl;
        }

        // Rewrite `/data/...` -> `<projectRoot>/data/...`
        if (pathPart.startsWith("/data/") || pathPart.equals("/data")) {
            Path projectRoot = Paths.get(System.getProperty("user.dir"));
            String fileName = Paths.get(pathPart).getFileName() == null ? "weaver.sqlite" : Paths.get(pathPart).getFileName().toString();
            // keep original file name after `/data/`
            if (pathPart.startsWith("/data/")) {
                fileName = pathPart.substring("/data/".length());
            }
            Path target = projectRoot.resolve("data").resolve(fileName);
            String targetPath = target.toString().replace('\\', '/');
            ensureParentDirExists(target);
            return "jdbc:sqlite:" + targetPath;
        }

        // If user configured relative path (e.g. `database.db`), keep it unchanged.
        // This matters because `ini_data_sqlite.sql` attaches `database.db` by relative name.
        if (!pathPart.contains(":") && !pathPart.startsWith("/")) {
            Path target = Paths.get(pathPart);
            ensureParentDirExists(target);
            return "jdbc:sqlite:" + pathPart;
        }

        return sqliteUrl;
    }

    private void ensureParentDirExists(Path targetFile) {
        Path parent = targetFile.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (Exception ex) {
                throw new RuntimeException("create parent dir failed: " + parent, ex);
            }
        }
        try {
            if (!Files.exists(targetFile)) {
                Files.createFile(targetFile);
            }
        } catch (Exception ex) {
            // ignore touch errors; sqlite may create on connect if permissions allow.
            log.debug("sqlite db file may not be creatable now: {} ({})", targetFile, ex.getMessage());
        }
    }

    private void ensureSqliteFileParentDirExists(String sqliteUrl) {
        // Only handle the url forms we actually use:
        // - jdbc:sqlite:database.db
        // - jdbc:sqlite:/abs/path/to/database.db (or /data/...)
        // No `jdbc:sqlite:file:...` and no `:memory:` compatibility needed.
        String rewritten = rewriteSqliteUrlIfNeeded(sqliteUrl);
        if (rewritten == null || rewritten.isBlank()) {
            return;
        }
        String raw = rewritten.trim();
        String pathPart = raw;
        if (raw.toLowerCase(Locale.ROOT).startsWith("jdbc:sqlite:")) {
            pathPart = raw.substring("jdbc:sqlite:".length());
        }
        if (pathPart.isBlank()) {
            return;
        }

        Path dbPath = Paths.get(pathPart);
        ensureParentDirExists(dbPath);
    }

    private void runSqlScript(DataSource dataSource, String scriptLocation) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        Statement statement = null;
        try {
            statement = connection.createStatement();
            Resource resource = new PathMatchingResourcePatternResolver().getResource(scriptLocation);
            try (InputStream sqlFile = resource.getInputStream();
                    InputStreamReader sqlFileIs = new InputStreamReader(sqlFile, StandardCharsets.UTF_8);
                    BufferedReader sqlReader = new BufferedReader(sqlFileIs)) {
                StringBuilder sqlBuilder = new StringBuilder();
                String line;
                while ((line = sqlReader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("--")) {
                        continue;
                    }
                    sqlBuilder.append(line).append(" ");
                    if (line.endsWith(";")) {
                        executeSql(statement, sqlBuilder.toString().replace(";", "").trim());
                        sqlBuilder.setLength(0);
                    }
                }
                if (sqlBuilder.length() > 0) {
                    executeSql(statement, sqlBuilder.toString().trim());
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("execute script failed: " + scriptLocation, ex);
        } finally {
            JdbcUtils.closeStatement(statement);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void executeSql(Statement statement, String sql) throws Exception {
        if (sql.isBlank()) {
            return;
        }
        try {
            statement.execute(sql);
        } catch (Exception ex) {
            // Oracle init script uses plain `DROP TABLE ...` (no IF EXISTS).
            // On first run the objects might not exist; ignore ORA-00942 for DROP statements only.
            String upper = sql.toUpperCase(Locale.ROOT).trim();
            if (upper.startsWith("DROP")) {
                if (ex instanceof java.sql.SQLException sqlEx
                        && sqlEx.getErrorCode() == 942) {
                    log.info("ignore ORA-00942 for DROP statement: {}", sql);
                    return;
                }
                if (ex.getMessage() != null && ex.getMessage().contains("ORA-00942")) {
                    log.info("ignore ORA-00942 for DROP statement: {}", sql);
                    return;
                }
            }
            throw ex;
        }
    }

    private String detectDialect(String url) {
        String lower = url == null ? "" : url.toLowerCase(Locale.ROOT);
        if (lower.contains(":postgresql:")) {
            return "postgresql";
        }
        if (lower.contains(":mysql:")) {
            return "mysql";
        }
        if (lower.contains(":oracle:")) {
            return "oracle";
        }
        if (lower.contains(":sqlserver:")) {
            return "sqlserver";
        }
        if (lower.contains(":sqlite:")) {
            return "sqlite";
        }
        return "unknown";
    }

    private String safeUpper(String value) {
        return value == null ? null : value.toUpperCase(Locale.ROOT);
    }

    private boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new LinkedHashMap<>();
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
