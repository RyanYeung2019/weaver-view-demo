package org.weaver.test;

import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import org.weaver.view.query.entity.RequestConfig;
import org.weaver.view.util.Utils;

import com.alibaba.fastjson.JSONObject;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("Frontend Resuful Call For test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FrontendWriteTable {
	//org.position
	private static final Logger log = LoggerFactory.getLogger(Frontend.class);
	
	@Autowired
    private TestRestTemplate restTemplate;

	@Test
	@DisplayName("Insert")
	@Order(1)
    public void insertTest()  {
		HttpHeaders headers = new HttpHeaders();
		headers.add("datasource", "dataSource");
		RequestConfig viewReqConfig = new RequestConfig();
		JSONObject params = new JSONObject();
		params.put("deptId", 3333);
		params.put("userId", 3333);
		params.put("createTime", viewReqConfig.getDatetimeFormat().format(new Date()));
		ResponseEntity<JSONObject> result1 = post("/table/view_demo/test_field",headers,params,JSONObject.class);
		System.out.println(result1.getBody());
		System.out.println(result1.getHeaders());
	}		

	@Test
	@DisplayName("Update")
	@Order(2)
    public void updateTest()  {
		HttpHeaders headers = new HttpHeaders();
		headers.add("datasource", "dataSource");
		RequestConfig viewReqConfig = new RequestConfig();
		JSONObject params = new JSONObject();
		params.put("id", 1);
		params.put("deptId", 5555);
		params.put("userId", 5555);
		params.put("createTime", viewReqConfig.getDatetimeFormat().format(new Date()));
		ResponseEntity<JSONObject> result1 = put("/table/view_demo/test_field",headers,params,JSONObject.class);
		System.out.println(result1.getBody());
		System.out.println(result1.getHeaders());
	}
	
	@Test
	@DisplayName("Delete")
	@Order(3)
    public void deleteTest()  {
		HttpHeaders headers = new HttpHeaders();
		headers.add("datasource", "dataSource");
		JSONObject params = new JSONObject();
		params.put("id", 1);
		ResponseEntity<JSONObject> result1 = delete("/table/view_demo/test_field",headers,params,JSONObject.class);
		System.out.println(result1.getBody());
		System.out.println(result1.getHeaders());
	}		
	
	
	private <T> ResponseEntity<T> post(String url,HttpHeaders headers,JSONObject data,Class<T> clazz){
	    HttpEntity<Map<String,Object>> request = new HttpEntity<>(data, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
	    return restTemplate.exchange(builder.build().toString(),HttpMethod.POST,request,clazz);
	}

	private <T> ResponseEntity<T> put(String url,HttpHeaders headers,JSONObject data,Class<T> clazz){
	    HttpEntity<Map<String,Object>> request = new HttpEntity<>(data, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
	    return restTemplate.exchange(builder.build().toString(),HttpMethod.PUT,request,clazz);
	}
	private <T> ResponseEntity<T> delete(String url,HttpHeaders headers,JSONObject data,Class<T> clazz){
	    HttpEntity<Map<String,Object>> request = new HttpEntity<>(data, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
	    return restTemplate.exchange(builder.build().toString(),HttpMethod.DELETE,request,clazz);
	}
	
	
	private <T> ResponseEntity<T> get(String url,HttpHeaders headers,Map<String,String> urlParams,Class<T> clazz){
	    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        for(String urlParam:urlParams.keySet()) {
        	if(urlParam.equals("filter")||urlParam.equals("search")||urlParam.equals("value")) {
           		builder.queryParam(urlParam,Utils.urlEncoder(urlParams.get(urlParam)));//url encode JSON String
        	}else {
           		builder.queryParam(urlParam,urlParams.get(urlParam));
        	}
        }
	    return restTemplate.exchange(builder.build().toString(),HttpMethod.GET,request,clazz);
	}
}
