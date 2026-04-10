package org.weaver.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.weaver.view.util.Utils;

import com.alibaba.fastjson.JSONObject;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("MultiDatabaseSmokeTest")
public class MultiDatabaseSmokeTest {

    private static final Logger log = LoggerFactory.getLogger(MultiDatabaseSmokeTest.class);

    private static final List<String> DBS = List.of("postgresql", "mysql", "oracle", "sqlserver", "sqlite");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("run smoke checks on all databases")
    public void smokeAllDatabases() {
        for (String db : DBS) {
            log.info("Running smoke test for db={}", db);
            runSmokeForDb(db);
        }
    }

    private void runSmokeForDb(String db) {
        HttpHeaders headers = new HttpHeaders();
        List<String> dbList = new ArrayList<>();
        dbList.add(db);
        headers.put("dataSource",dbList);
        Map<String, String> listParams = Map.of(
                "db", db,
                "page", "0",
                "size", "1",
                "aggrs", "_");
        ResponseEntity<JSONObject> listResp = get("/view/department", headers, listParams, JSONObject.class);
        assertNotNull(listResp.getBody());
        assertNotNull(listResp.getBody().getString("name"));
        assertNotNull(listResp.getBody().getJSONObject("aggrs"));
        assertTrue(Integer.parseInt(listResp.getBody().getJSONObject("aggrs").getString("size")) >= 0);
    }

    private <T> ResponseEntity<T> get(String url, HttpHeaders headers, Map<String, String> urlParams, Class<T> clazz) {
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        for (String urlParam : urlParams.keySet()) {
            if (urlParam.equals("filter") || urlParam.equals("search") || urlParam.equals("value")) {
                builder.queryParam(urlParam, Utils.urlEncoder(urlParams.get(urlParam)));
            } else {
                builder.queryParam(urlParam, urlParams.get(urlParam));
            }
        }
        return restTemplate.exchange(builder.build().toString(), HttpMethod.GET, request, clazz);
    }
}
