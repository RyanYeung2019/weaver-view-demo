package org.weaver.test;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("FrontendTrx")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FrontendTrx extends FrontendTestSupport {

	@ParameterizedTest(name = "[{index}] db={0}")
	@MethodSource("databaseCases")
	@DisplayName("modifyDataWithTrx")
	@Order(1)
	public void modifyDataWithTrx(DatabaseCase db) throws Exception {
		HttpHeaders headers = headersFor(db);
        get("/table/emptyTableCacheNow",headersFor(db),new LinkedHashMap<>(),JSONObject.class);

		// roll back case
		trxData("/modifyDataWithTrx/approveAction", JSONArray.parseArray("""
				[
				    {
				        "command": "insert",
				        "tableName": "view_demo.test_field",
				        "data": {
				            "deptId": 111,
				            "userId": 333,
				            "createTime": "{{currentDate}}"
				        }
				    },
				    {
				        "command": "insert",
				        "tableName": "view_demo.test_field",
				        "data": {
				            "deptId": 111,
				            "userId": "33中文33",
				            "createTime": "2025-07-30 12:12:12"
				        }
				    }
				]
			"""), headers); // "userId": "33中文33", 触发NumberFormatException，测试回滚效果

		trxData("/modifyDataWithTrx/approveAction", JSONArray.parseArray("""
				[
				    {
				        "command": "insert",
				        "tableName": "view_demo.test_field",
				        "data": {
				            "deptId": 222,
				            "userId": 333,
				            "remark": "currentNickName: {{currentNickName}}",
				            "createTime": "{{currentDate}}"
				        }
				    },
				    {
				        "command": "insert",
				        "tableName": "view_demo.test_field",
				        "data": {
				            "deptId": 222,
				            "userId": 111,
				            "remark": "currentNickName: {{currentNickName}}",
				            "createTime": "2025-07-30 12:12:12"
				        }
				    }
				]
			"""), headers);
	}

	private void trxData(String path, JSONArray params, HttpHeaders headers) {
		ResponseEntity<Object> result1 = post(path, headers, params, Object.class);
		System.out.println(result1.getBody());
	}
}
