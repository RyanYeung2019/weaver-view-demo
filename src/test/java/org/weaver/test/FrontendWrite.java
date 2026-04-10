package org.weaver.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.alibaba.fastjson.JSONObject;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("FrontendWrite")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FrontendWrite extends FrontendTestSupport {

	private static final Logger log = LoggerFactory.getLogger(FrontendWrite.class);
	
	@ParameterizedTest(name = "[{index}] db={0}")
	@MethodSource("databaseCases")
	@DisplayName("dataModify")
	@Order(1)
	public void dataModify(DatabaseCase db) throws Exception {
        get("/table/emptyTableCacheNow",headersFor(db),new LinkedHashMap<>(),JSONObject.class);
        HttpHeaders headers = headersFor(db);
        //sqlserver 不能自动获取主键，在这里可以特别声明用哪些字段进行操作
        if (db.databaseProductName().equals("sqlserver")) {
            headers.add("whereFields","id");
        }
		int id = insertData("/table/view_demo/test_field",JSONObject.parseObject("""
					{
					"deptId":111,
					"userId":111,
					"createTime":"2025-07-30 12:12:12"
				}
				"""), headers);
		readData("/table/view_demo/test_field",Map.of("id",String.valueOf(id)),headers);
		updateTest("/table/view_demo/test_field",JSONObject.parseObject(String.format("""
				{
				"id":%d,
				"deptId":5555,
				"userId":5555,
				"createTime":"2025-07-30 12:12:12"
			}
			""",id)),headers);
		deleteTest("/table/view_demo/test_field",JSONObject.parseObject(String.format("""
				{
				"id":%d
			}
			""",id)),headers);	
		insertData("/table/view_demo/test_field",JSONObject.parseObject("""
				{
				"deptId":222,
				"userId":222,
				"createTime":"2025-07-30 12:12:12"
			}
			"""), headers);		
		insertData("/table/view_demo/test_field",JSONObject.parseObject("""
				{
				"deptId":333,
				"userId":333,
				"createTime":"2025-07-30 12:12:12"
			}
			"""), headers);		
		insertData("/table/view_demo/test_field",JSONObject.parseObject("""
				{
				"deptId":444,
				"userId":444,
				"createTime":"2025-07-30 12:12:12"
			}
			"""), headers);		
		insertData("/table/view_demo/test_field",JSONObject.parseObject("""
				{
				"deptId":555,
				"userId":555,
				"createTime":"2025-07-30 12:12:12"
			}
			"""), headers);		
		insertData("/table/view_demo/test_field",JSONObject.parseObject("""
				{
				"deptId":666,
				"userId":666,
				"createTime":"2025-07-30 12:12:12"
			}
			"""), headers);		
	}	

	@ParameterizedTest(name = "[{index}] db={0}")
	@MethodSource("databaseCases")
	@DisplayName("dataModifyMultipleKeys")
	@Order(2)
	public void dataModifyMultipleKeys(DatabaseCase db) throws Exception {
		get("/view/reloadAllTheViewsDefineNow", headersFor(db), new LinkedHashMap<>(), JSONObject.class);
		HttpHeaders headers = headersFor(db);
        //sqlserver 不能自动获取主键，在这里可以特别声明用哪些字段进行操作
        if (db.databaseProductName().equals("sqlserver")) {
            headers.add("whereFields","domainKey,depKey,posKey");
        }
		String uniqueSuffix = String.valueOf(System.currentTimeMillis());
		String domainKey = "domainKey" + uniqueSuffix;
		String depKey = "depKey" + uniqueSuffix;
		String posKey = "posKey" + uniqueSuffix;
		insertData("/table/view_demo/position",JSONObject.parseObject(String.format("""
					{
					"domainKey":"%s",
					"depKey":"%s",
					"posKey":"%s",
					"posName":"posName1111",
					"createTime":"2025-07-30 12:12:12",
					"updateTime":"2025-07-30 12:12:12",
					"createUser":"Ryan",
					"updateUser":"Ryan"
				}
				""", domainKey, depKey, posKey)), headers);
		
		readData("/table/view_demo/position",
				Map.of(
						"domainKey",domainKey,
						"depKey",depKey,
						"posKey",posKey),headers);
		
		updateTest("/table/view_demo/position",JSONObject.parseObject(String.format("""
				{
					"domainKey":"%s",
					"depKey":"%s",
					"posKey":"%s",
					"posName":"posName22222",
					"createTime":"2025-07-30 12:12:12",
					"updateTime":"2025-07-30 12:12:12",
					"createUser":"Ryan",
					"updateUser":"Ryan"
			}
			""", domainKey, depKey, posKey)),headers);
		
		deleteTest("/table/view_demo/position",JSONObject.parseObject(String.format("""
				{
					"domainKey":"%s",
					"depKey":"%s",
					"posKey":"%s"
			}
			""", domainKey, depKey, posKey)),headers);		
	}
	
	@ParameterizedTest(name = "[{index}] db={0}")
	@MethodSource("databaseCases")
	@DisplayName("Fetch Data from table")
	@Order(3)
    public void ListTableTest(DatabaseCase db)  {
		get("/view/reloadAllTheViewsDefineNow", headersFor(db), new LinkedHashMap<>(), JSONObject.class);
		HttpHeaders headers = headersFor(db);
		Map<String,String> params = new LinkedHashMap<>();
		params.put("page", "1");
		params.put("size", "3");
		params.put("sort", "deptId-d");
		params.put("aggrs", "_");
		params.put("type", "table");
		JSONObject respPage = get("/view/view_demo/test_field",headers,params,JSONObject.class).getBody();
		//获取数据结构中的表COMMENT内容
		if(!db.isSqlite()) assertEquals(respPage.getString("remark"),"测试表");
		//默认带出表名
		assertEquals(respPage.getString("name"),"view_demo.test_field");
		//获取数据结构中的字段COMMENT内容
		if(!db.isSqlite()) assertEquals(respPage.getJSONArray("fields").getJSONObject(0).get("remark"),"主键");
		//统计总记录数
		assertTrue(Integer.parseInt(respPage.getJSONObject("aggrs").getString("size")) >= 1);
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
		assertEquals("1", result1.getHeaders().getFirst("rows-affected"));
		try {
			return result1.getBody().getInteger("id");
		}catch(Exception e) {
			return 0;
		}
	}
	
    private void updateTest(String path,JSONObject params,HttpHeaders headers)  {
		ResponseEntity<Integer> result1 = patch(path,headers,params,Integer.class);
		assertEquals(1, result1.getBody());
		log.info(result1.getBody().toString());
	}

    private void deleteTest(String path,JSONObject params,HttpHeaders headers)  {
		ResponseEntity<Integer> result1 = delete(path,headers,params,Integer.class);
		log.info(result1.getBody().toString());
		assertEquals(1, result1.getBody());
	}		

}
