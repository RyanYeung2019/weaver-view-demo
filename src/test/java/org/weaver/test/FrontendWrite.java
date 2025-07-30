package org.weaver.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.LinkedHashMap;
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
import org.weaver.query.entity.RequestConfig;
import org.weaver.view.util.Utils;

import com.alibaba.fastjson.JSONObject;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("FrontendWrite")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FrontendWrite {

	private static final Logger log = LoggerFactory.getLogger(FrontendWrite.class);
	
	@Autowired
    private TestRestTemplate restTemplate;

	@Test
	@DisplayName("dataModify")
	@Order(1)
	public void dataModify() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add("datasource", "dataSource");
		RequestConfig viewReqConfig = new RequestConfig();
		int id = insertData("/table/view_demo/test_field",JSONObject.parseObject(String.format("""
					{
					"deptId":%d,
					"userId":%d,
					"createTime":"%s"
				}
				""", 111, 111, viewReqConfig.getDatetimeFormat().format(new Date()))), headers);
		readData("/table/view_demo/test_field",Map.of("id",String.valueOf(id)),headers);
		updateTest("/table/view_demo/test_field",JSONObject.parseObject(String.format("""
				{
				"id":%d,
				"deptId":%d,
				"userId":%d,
				"createTime":"%s"
			}
			""",id,5555,5555, viewReqConfig.getDatetimeFormat().format(new Date()))),headers);
		deleteTest("/table/view_demo/test_field",JSONObject.parseObject(String.format("""
				{
				"id":%d
			}
			""",id)),headers);	
		insertData("/table/view_demo/test_field",JSONObject.parseObject(String.format("""
				{
				"deptId":%d,
				"userId":%d,
				"createTime":"%s"
			}
			""", 222, 222, viewReqConfig.getDatetimeFormat().format(new Date()))), headers);		
		insertData("/table/view_demo/test_field",JSONObject.parseObject(String.format("""
				{
				"deptId":%d,
				"userId":%d,
				"createTime":"%s"
			}
			""", 333, 333, viewReqConfig.getDatetimeFormat().format(new Date()))), headers);		
		insertData("/table/view_demo/test_field",JSONObject.parseObject(String.format("""
				{
				"deptId":%d,
				"userId":%d,
				"createTime":"%s"
			}
			""", 444, 444, viewReqConfig.getDatetimeFormat().format(new Date()))), headers);		
		insertData("/table/view_demo/test_field",JSONObject.parseObject(String.format("""
				{
				"deptId":%d,
				"userId":%d,
				"createTime":"%s"
			}
			""", 555, 555, viewReqConfig.getDatetimeFormat().format(new Date()))), headers);		
		insertData("/table/view_demo/test_field",JSONObject.parseObject(String.format("""
				{
				"deptId":%d,
				"userId":%d,
				"createTime":"%s"
			}
			""", 666, 666, viewReqConfig.getDatetimeFormat().format(new Date()))), headers);		
	}	


	@Test
	@DisplayName("dataModifyMultipleKeys")
	@Order(2)
	public void dataModifyMultipleKeys() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add("datasource", "dataSource");
		RequestConfig viewReqConfig = new RequestConfig();
		insertData("/table/view_demo/position",JSONObject.parseObject(String.format("""
					{
					"domainKey":"%s",
					"depKey":"%s",
					"posKey":"%s",
					"posName":"%s",
					"createTime":"%s",
					"updateTime":"%s",
					"createUser":"%s",
					"updateUser":"%s"
				}
				""", "domainKey1111", "depKey1111","posKey1111","posName1111",
				viewReqConfig.getDatetimeFormat().format(new Date()),
				viewReqConfig.getDatetimeFormat().format(new Date()),
				"Ryan","Ryan"
				)), headers);
		
		readData("/table/view_demo/position",
				Map.of(
						"domainKey","domainKey1111",
						"depKey","depKey1111",
						"posKey","posKey1111"),headers);
		
		updateTest("/table/view_demo/position",JSONObject.parseObject(String.format("""
				{
					"domainKey":"%s",
					"depKey":"%s",
					"posKey":"%s",
					"posName":"%s",
					"createTime":"%s",
					"updateTime":"%s",
					"createUser":"%s",
					"updateUser":"%s"
			}
			""","domainKey1111", "depKey1111","posKey1111","posName22222",
			viewReqConfig.getDatetimeFormat().format(new Date()),
			viewReqConfig.getDatetimeFormat().format(new Date()),
			"Ryan","Ryan"
			)),headers);
		
		deleteTest("/table/view_demo/position",JSONObject.parseObject(String.format("""
				{
					"domainKey":"%s",
					"depKey":"%s",
					"posKey":"%s"
			}
			""","domainKey1111", "depKey1111","posKey1111")),headers);		
	}

	@Test
	@DisplayName("List")
	@Order(3)
    public void ListTableTest()  {
		HttpHeaders headers = new HttpHeaders();
		Map<String,String> params = new LinkedHashMap<>();
		params.put("page", "1");
		params.put("size", "3");
		params.put("sort", "deptId-d");
		params.put("aggrs", "_");
		params.put("type", "table");
		JSONObject respPage = get("/view/view_demo/test_field",headers,params,JSONObject.class).getBody();
		//获取数据结构中的表COMMENT内容
		assertEquals(respPage.getString("remark"),"测试表");
		//默认带出表名
		assertEquals(respPage.getString("name"),"view_demo.test_field");
		//获取数据结构中的字段COMMENT内容
		assertEquals(respPage.getJSONArray("fields").getJSONObject(0).get("remark"),"主键");
		//统计总记录数
		assertEquals(respPage.getJSONObject("aggrs").getString("size"),"5");
		log.info(respPage.toString());		
	}	

	private void readData(String path,Map<String,String> params,HttpHeaders headers) {
		ResponseEntity<JSONObject> result1 = get(path,headers,params,JSONObject.class);
		log.info("readData:::::"+result1.getBody().toJSONString());
	}
	
	private int insertData(String path,JSONObject params,HttpHeaders headers) {
		ResponseEntity<JSONObject> result1 = post(path,headers,params,JSONObject.class);
		log.info(result1.getBody().toJSONString());
		log.info(result1.getHeaders().toString());
		assertEquals(result1.getHeaders().getFirst("rows-affected"),"1");
		try {
			return result1.getBody().getInteger("id");
		}catch(Exception e) {
			return 0;
		}
	}
	
    private void updateTest(String path,JSONObject params,HttpHeaders headers)  {
		ResponseEntity<JSONObject> result1 = put(path,headers,params,JSONObject.class);
		assertEquals(result1.getHeaders().getFirst("rows-affected"),"1");
		log.info(result1.getBody().toJSONString());
		log.info(result1.getHeaders().toString());
	}

    private void deleteTest(String path,JSONObject params,HttpHeaders headers)  {
		ResponseEntity<JSONObject> result1 = delete(path,headers,params,JSONObject.class);
		log.info(result1.getBody().toJSONString());
		log.info(result1.getHeaders().toString());
		assertEquals(result1.getHeaders().getFirst("rows-affected"),"1");
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
