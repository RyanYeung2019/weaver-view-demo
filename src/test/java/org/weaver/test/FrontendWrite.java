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
import org.springframework.web.util.UriComponentsBuilder;
import org.weaver.view.query.entity.RequestConfig;
import com.alibaba.fastjson.JSONObject;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("FrontendWrite")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FrontendWrite {

	private static final Logger log = LoggerFactory.getLogger(FrontendWrite.class);
	
	@Autowired
    private TestRestTemplate restTemplate;
	
	@Test
	@DisplayName("Insert")
	@Order(1)
    public void insertTest()  {
		HttpHeaders headers = new HttpHeaders();
		RequestConfig viewReqConfig = new RequestConfig();
		JSONObject params = new JSONObject();
		params.put("deptId", 3333);
		params.put("userId", 3333);
		params.put("createTime", viewReqConfig.getDatetimeFormat().format(new Date()));
		params.put("createBy", "Ryan");
		ResponseEntity<JSONObject> result1 = post("/view/org/test_field",headers,params,JSONObject.class);
		log.info(result1.getBody().toString());
		log.info(result1.getHeaders().toString());
	}		

	@Test
	@DisplayName("Update")
	@Order(2)
    public void updateTest()  {
		HttpHeaders headers = new HttpHeaders();
		JSONObject params = new JSONObject();
		RequestConfig viewReqConfig = new RequestConfig();
		params.put("id", 1);
		params.put("deptId", 5555);
		params.put("userId", 5555);
		params.put("updateTime", viewReqConfig.getDatetimeFormat().format(new Date()));
		params.put("updateBy", "Ryan");
		ResponseEntity<JSONObject> result1 = put("/view/org/test_field",headers,params,JSONObject.class);
		log.info(result1.getBody().toString());
		log.info(result1.getHeaders().toString());
	}

	@Test
	@DisplayName("Delete")
	@Order(3)
    public void deleteTest()  {
		HttpHeaders headers = new HttpHeaders();
		JSONObject params = new JSONObject();
		params.put("id", 1);
		ResponseEntity<JSONObject> result1 = delete("/view/org/test_field",headers,params,JSONObject.class);
		log.info(result1.getBody().toString());
		log.info(result1.getHeaders().toString());
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

}
