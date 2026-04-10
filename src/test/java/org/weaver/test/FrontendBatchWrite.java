package org.weaver.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("FrontendBatchWrite")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FrontendBatchWrite extends FrontendTestSupport {
	private static final Logger log = LoggerFactory.getLogger(FrontendBatchWrite.class);

	@ParameterizedTest(name = "[{index}] db={0}")
	@MethodSource("databaseCases")
	@DisplayName("batchModify")
	@Order(1)
	public void dataModify(DatabaseCase db) throws Exception {
		HttpHeaders headers = headersFor(db);
        get("/table/emptyTableCacheNow",headersFor(db),new LinkedHashMap<>(),JSONObject.class);
        //sqlserver 不能自动获取主键，在这里可以特别声明用哪些字段进行操作
        if (db.databaseProductName().equals("sqlserver")) {
            headers.add("whereFields","domainKey,depKey,posKey");
        }

		String uniqueSuffix = String.valueOf(System.currentTimeMillis());
		String domainKey = "domainKey" + uniqueSuffix;
		String depKey = "depKey" + uniqueSuffix;
		
		ResponseEntity<JSONArray> result1 = put("/table/view_demo/position",headers,JSONArray.parseArray(String.format("""
				[{
				"domainKey":"%s",
				"depKey":"%s",
				"posKey":"posKey1111",
				"posName":"posName1111",
				"createTime":"2025-07-30 12:12:12",
				"updateTime":"2025-07-30 12:12:12",
				"createUser":"Ryan",
				"updateUser":"Ryan"
			},
			{
				"domainKey":"%s",
				"depKey":"%s",
				"posKey":"posKey2222",
				"posName":"posName2222",
				"createTime":"2025-07-30 12:12:12",
				"updateTime":"2025-07-30 12:12:12",
				"createUser":"Ryan",
				"updateUser":"Ryan"
			},
			{
				"domainKey":"%s",
				"depKey":"%s",
				"posKey":"posKey3333",
				"posName":"posName3333",
				"createTime":"2025-07-30 12:12:12",
				"updateTime":"2025-07-30 12:12:12",
				"createUser":"Ryan",
				"updateUser":"Ryan"
			}]
		""", domainKey, depKey, domainKey, depKey, domainKey, depKey)),JSONArray.class);

		ResponseEntity<JSONObject> resultRead = get("/table/view_demo/position",
				headers,Map.of("domainKey",domainKey,
						"depKey",depKey,
						"posKey","posKey3333"),
				JSONObject.class);
		log.info(resultRead.getBody().toJSONString());
		
		assertEquals("posName3333", resultRead.getBody().getJSONObject("data").getString("posName"));
		
		result1 = put("/table/view_demo/position",headers,JSONArray.parseArray(String.format("""
				[{
				"domainKey":"%s",
				"depKey":"%s",
				"posKey":"posKey1111",
				"posName":"posName11112222",
				"createTime":"2025-07-30 12:12:12",
				"updateTime":"2025-07-30 12:12:12",
				"createUser":"Ryan",
				"updateUser":"Ryan"
			},
			{
				"domainKey":"%s",
				"depKey":"%s",
				"posKey":"posKey2222",
				"createTime":"2025-07-30 12:12:12",
				"updateTime":"2025-07-30 12:12:12",
				"createUser":"Ryan",
				"updateUser":"Ryan"
			},
			{
				"domainKey":"%s",
				"depKey":"%s",
				"posKey":"posKey3333",
				"posName":"posName33332222",
				"createTime":"2025-07-30 12:12:12",
				"updateTime":"2025-07-30 12:12:12",
				"createUser":"Ryan",
				"updateUser":"Ryan"
			}]
		""", domainKey, depKey, domainKey, depKey, domainKey, depKey)),JSONArray.class);		
		log.info(result1.getBody().toJSONString());
		log.info(result1.getHeaders().toString());
		
		resultRead = get("/table/view_demo/position",
				headers,Map.of("domainKey",domainKey,
						"depKey",depKey,
						"posKey","posKey3333"),
				JSONObject.class);
		log.info(resultRead.getBody().toJSONString());
		
		assertEquals("posName33332222", resultRead.getBody().getJSONObject("data").getString("posName"));
		
		resultRead = get("/table/view_demo/position",
				headers,Map.of("domainKey",domainKey,
						"depKey",depKey,
						"posKey","posKey2222"),
				JSONObject.class);
		log.info(resultRead.getBody().toJSONString());
		assertEquals(null, resultRead.getBody().getJSONObject("data").getString("posName"));

        HttpHeaders patchHeader = headersFor(db);
        patchHeader.add("whereFields", "domainKey,depKey");
        patchHeader.add("assertMaxRecordAffected", "3");
		ResponseEntity<Integer> patchResult = patch("/table/view_demo/position",patchHeader,JSONObject.parseObject(String.format("""
				{
					"domainKey":"%s",
					"depKey":"%s",
					"posName":"posName99999999",
				}
				""", domainKey, depKey)),Integer.class);
		assertEquals(3, patchResult.getBody());


		resultRead = get("/table/view_demo/position",
                headers,Map.of("domainKey",domainKey,
						"depKey",depKey,
						"posKey","posKey3333"),
				JSONObject.class);
		log.info(resultRead.getBody().toJSONString());	
		assertEquals("posName99999999", resultRead.getBody().getJSONObject("data").getString("posName"));
		
		ResponseEntity<Integer> deleteResult = delete("/table/view_demo/position",patchHeader,JSONObject.parseObject(String.format("""
				{
					"domainKey":"%s",
					"depKey":"%s"
				}
				""", domainKey, depKey)),Integer.class);
		assertEquals(3, deleteResult.getBody());
	}
	
}
