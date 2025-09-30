package org.weaver.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.alibaba.fastjson.JSONArray;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("FrontendTrx")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FrontendTrx {
	
	@Autowired
    private TestRestTemplate restTemplate;

	@Test
	@DisplayName("modifyDataWithTrx")
	@Order(1)
	public void modifyDataWithTrx() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add("datasource", "dataSource");
		insertData("/modifyDataWithTrx/approveAction",JSONArray.parseArray("""
				[
				    {
				        "command": "insert",
				        "tableName": "view_demo.test_field",
				        "data": {
				            "deptId": 111,
				            "userId": 111,
				            "createTime": "2025-07-30 12:12:12"
				        }
				    },
				    {
				        "command": "insert",
				        "tableName": "view_demo.test_field",
				        "data": {
				            "deptId": 222,
				            "userId": "33中文33",
				            "createTime": "2025-07-30 12:12:12"
				        }
				    }
				]
			"""),headers);
		
		//readData("/table/view_demo/test_field",Map.of("id",String.valueOf(id)),headers);
		
	}
	
	private void insertData(String path,JSONArray params,HttpHeaders headers) {
		ResponseEntity<Object> result1 = post(path,headers,params,Object.class);
		System.out.println(result1.getBody());
	}
	
	private <T> ResponseEntity<T> post(String url,HttpHeaders headers,JSONArray data,Class<T> clazz){
	    HttpEntity<JSONArray> request = new HttpEntity<>(data, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
	    return restTemplate.exchange(builder.build().toString(),HttpMethod.POST,request,clazz);
	}
	
}
