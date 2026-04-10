package org.weaver.test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Locale;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.weaver.view.util.Utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public abstract class FrontendTestSupport {

    protected static final String DATASOURCE_HEADER = "dataSource";

    @Autowired
    protected TestRestTemplate restTemplate;

    // Used by non-parameterized tests (FrontendWrite / FrontEndBatchWrite) to decide sqlite-specific assertions.
    @Autowired
    protected DataSource dataSource;

    protected HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        // Keep compatibility with controllers: if headerValue is "dataSource", they use default alias bean.
        headers.add(DATASOURCE_HEADER, "dataSource");
        return headers;
    }

    protected Stream<DatabaseCase> databaseCases() {
        // Allow narrowing the DBs via -Dweaver.test.dbs=postgresql,sqlite
        String override = System.getProperty("weaver.test.dbs");
        if (override == null || override.isBlank()) {
            override = System.getenv("WEAVER_TEST_DBS");
        }
        List<String> dbKeys = parseDbKeys(override);
        return dbKeys.stream().map(dbKey -> new DatabaseCase(dbKey, dbKey.toLowerCase().contains("sqlite")));
    }

    protected DatabaseCase detectDatabaseCase() {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            if (conn != null && !conn.isClosed()) {
                DatabaseMetaData metaData = conn.getMetaData();
                String dbName = metaData.getDatabaseProductName();
                String normalized = dbName == null ? "" : dbName.toLowerCase(Locale.ROOT);
                return new DatabaseCase(normalized, normalized.contains("sqlite"));
            }
            return new DatabaseCase("unknown", false);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to detect database product.", e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private List<String> parseDbKeys(String override) {
        List<String> all = List.of("postgresql", "mysql", "oracle", "sqlserver", "sqlite");
        if (override == null || override.isBlank()) {
            return all;
        }
        List<String> parsed = Arrays.stream(override.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
        return parsed.isEmpty() ? all : parsed;
    }

    protected HttpHeaders headersFor(DatabaseCase db) {
        HttpHeaders headers = new HttpHeaders();
        // Controllers resolve headerValue to "dataSource.{key}" when it isn't "dataSource".
        headers.add(DATASOURCE_HEADER, db.databaseProductName());
        return headers;
    }

    protected <T> ResponseEntity<T> get(String url, HttpHeaders headers, Map<String, String> urlParams, Class<T> clazz) {
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        Map<String, String> queryParams = new LinkedHashMap<>(urlParams);
        // Keep inner queries on the same DB: controllers support app.demo-db.query-param=db.
        String headerDb = resolveDbKeyFromHeader(headers);
        if (headerDb != null && !queryParams.containsKey("db")) {
            queryParams.put("db", headerDb);
        }
        for (String urlParam : queryParams.keySet()) {
            if (urlParam.equals("filter") || urlParam.equals("search") || urlParam.equals("value")) {
                builder.queryParam(urlParam, Utils.urlEncoder(queryParams.get(urlParam)));
            } else {
                builder.queryParam(urlParam, queryParams.get(urlParam));
            }
        }
        return restTemplate.exchange(builder.build().toString(), HttpMethod.GET, request, clazz);
    }

    private String resolveDbKeyFromHeader(HttpHeaders headers) {
        if (headers == null) {
            return null;
        }
        String headerValue = headers.getFirst(DATASOURCE_HEADER);
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        if ("dataSource".equals(headerValue)) {
            return null;
        }
        if (headerValue.startsWith("dataSource.")) {
            return headerValue.substring("dataSource.".length());
        }
        return headerValue;
    }

    protected <T> ResponseEntity<T> post(String url, HttpHeaders headers, JSONObject data, Class<T> clazz) {
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        return restTemplate.exchange(builder.build().toString(), HttpMethod.POST, request, clazz);
    }

    protected <T> ResponseEntity<T> post(String url, HttpHeaders headers, JSONArray data, Class<T> clazz) {
        HttpEntity<JSONArray> request = new HttpEntity<>(data, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        return restTemplate.exchange(builder.build().toString(), HttpMethod.POST, request, clazz);
    }

    protected <T> ResponseEntity<T> put(String url, HttpHeaders headers, JSONArray data, Class<T> clazz) {
        HttpEntity<JSONArray> request = new HttpEntity<>(data, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        return restTemplate.exchange(builder.build().toString(), HttpMethod.PUT, request, clazz);
    }

    protected <T> ResponseEntity<T> patch(String url, HttpHeaders headers, JSONObject data, Class<T> clazz) {
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        return restTemplate.exchange(builder.build().toString(), HttpMethod.PATCH, request, clazz);
    }

    protected <T> ResponseEntity<T> delete(String url, HttpHeaders headers, JSONObject data, Class<T> clazz) {
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        return restTemplate.exchange(builder.build().toString(), HttpMethod.DELETE, request, clazz);
    }

    public record DatabaseCase(String databaseProductName, boolean sqlite) {
        public boolean isSqlite() {
            return sqlite;
        }
    }
}
