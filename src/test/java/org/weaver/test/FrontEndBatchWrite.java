package org.weaver.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.weaver.view.util.Utils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("FrontEndBatchWrite")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FrontEndBatchWrite {
	private static final Logger log = LoggerFactory.getLogger(FrontendWrite.class);
	
	@Autowired
    private TestRestTemplate restTemplate;
	
	@Test
	@DisplayName("batchModify")
	@Order(1)
	public void dataModify() throws Exception {
		
		ResponseEntity<JSONArray> result1 = put("/table/view_demo/position",new HttpHeaders(),JSONArray.parseArray("""
				[{
				"domainKey":"domainKey1111",
				"depKey":"depKey1111",
				"posKey":"posKey1111",
				"posName":"posName1111",
				"createTime":"2025-07-30 12:12:12",
				"updateTime":"2025-07-30 12:12:12",
				"createUser":"Ryan",
				"updateUser":"Ryan"
			},
			{
				"domainKey":"domainKey1111",
				"depKey":"depKey1111",
				"posKey":"posKey2222",
				"posName":"posName2222",
				"createTime":"2025-07-30 12:12:12",
				"updateTime":"2025-07-30 12:12:12",
				"createUser":"Ryan",
				"updateUser":"Ryan"
			},
			{
				"domainKey":"domainKey1111",
				"depKey":"depKey1111",
				"posKey":"posKey3333",
				"posName":"posName3333",
				"createTime":"2025-07-30 12:12:12",
				"updateTime":"2025-07-30 12:12:12",
				"createUser":"Ryan",
				"updateUser":"Ryan"
			}]
		"""),JSONArray.class);

		ResponseEntity<JSONObject> resultRead = get("/table/view_demo/position",
				new HttpHeaders(),Map.of("domainKey","domainKey1111",
						"depKey","depKey1111",
						"posKey","posKey3333"),
				JSONObject.class);
		log.info(resultRead.getBody().toJSONString());
		
		assertEquals(resultRead.getBody().getJSONObject("data").getString("posName"),"posName3333");
		
		result1 = put("/table/view_demo/position",new HttpHeaders(),JSONArray.parseArray("""
				[{
				"domainKey":"domainKey1111",
				"depKey":"depKey1111",
				"posKey":"posKey1111",
				"posName":"posName11112222",
				"createTime":"2025-07-30 12:12:12",
				"updateTime":"2025-07-30 12:12:12",
				"createUser":"Ryan",
				"updateUser":"Ryan"
			},
			{
				"domainKey":"domainKey1111",
				"depKey":"depKey1111",
				"posKey":"posKey2222",
				"createTime":"2025-07-30 12:12:12",
				"updateTime":"2025-07-30 12:12:12",
				"createUser":"Ryan",
				"updateUser":"Ryan"
			},
			{
				"domainKey":"domainKey1111",
				"depKey":"depKey1111",
				"posKey":"posKey3333",
				"posName":"posName33332222",
				"createTime":"2025-07-30 12:12:12",
				"updateTime":"2025-07-30 12:12:12",
				"createUser":"Ryan",
				"updateUser":"Ryan"
			}]
		"""),JSONArray.class);		
		log.info(result1.getBody().toJSONString());
		log.info(result1.getHeaders().toString());
		
		resultRead = get("/table/view_demo/position",
				new HttpHeaders(),Map.of("domainKey","domainKey1111",
						"depKey","depKey1111",
						"posKey","posKey3333"),
				JSONObject.class);
		log.info(resultRead.getBody().toJSONString());
		
		assertEquals(resultRead.getBody().getJSONObject("data").getString("posName"),"posName33332222");
		
		resultRead = get("/table/view_demo/position",
				new HttpHeaders(),Map.of("domainKey","domainKey1111",
						"depKey","depKey1111",
						"posKey","posKey2222"),
				JSONObject.class);
		log.info(resultRead.getBody().toJSONString());
		assertEquals(resultRead.getBody().getJSONObject("data").getString("posName"),null);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("whereFields", "domainKey,depKey");
		headers.add("assertMaxRecordAffected", "3");
		ResponseEntity<Integer> patchResult = patch("/table/view_demo/position",headers,JSONObject.parseObject("""
				{
					"domainKey":"domainKey1111",
					"depKey":"depKey1111",
					"posName":"posName99999999",
				}
				"""),Integer.class);
		assertEquals(patchResult.getBody(),3);
		
		resultRead = get("/table/view_demo/position",
				new HttpHeaders(),Map.of("domainKey","domainKey1111",
						"depKey","depKey1111",
						"posKey","posKey3333"),
				JSONObject.class);
		log.info(resultRead.getBody().toJSONString());	
		assertEquals(resultRead.getBody().getJSONObject("data").getString("posName"),"posName99999999");
		
		ResponseEntity<Integer> deleteResult = delete("/table/view_demo/position",headers,JSONObject.parseObject("""
				{
					"domainKey":"domainKey1111",
					"depKey":"depKey1111"
				}
				"""),Integer.class);
		assertEquals(deleteResult.getBody(),3);
	}
	
	private <T> ResponseEntity<T> patch(String url,HttpHeaders headers,JSONObject data,Class<T> clazz){
	    HttpEntity<Map<String,Object>> request = new HttpEntity<>(data, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
	    return restTemplate.exchange(builder.build().toString(),HttpMethod.PATCH,request,clazz);
	}	

	private <T> ResponseEntity<T> delete(String url,HttpHeaders headers,JSONObject data,Class<T> clazz){
	    HttpEntity<Map<String,Object>> request = new HttpEntity<>(data, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
	    return restTemplate.exchange(builder.build().toString(),HttpMethod.DELETE,request,clazz);
	}	
	
    private <T> ResponseEntity<T> put(String url, HttpHeaders headers, JSONArray data, Class<T> clazz) {
        HttpEntity<JSONArray> request = new HttpEntity<>(data, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        return restTemplate.exchange(builder.build().toString(), HttpMethod.PUT, request, clazz);
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
