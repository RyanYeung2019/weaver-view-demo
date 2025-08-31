package org.weaver.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.weaver.query.entity.QueryCriteria;
import org.weaver.query.entity.QueryFilter;
import org.weaver.view.util.Utils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.MethodOrderer;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("Frontend")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Frontend {

	private static final Logger log = LoggerFactory.getLogger(Frontend.class);
	
	@Autowired
    private TestRestTemplate restTemplate;

	@Autowired
	private DataSource dataSource;
	
	@Test
	@DisplayName("First Demo")
	@Order(0)
    public void firstDemo()  {
		HttpHeaders headers = new HttpHeaders();
		Map<String,String> params = new LinkedHashMap<>();
		//中文版本
		params.put("lang", "zh");
		params.put("page", "1");
		params.put("size", "5");
		params.put("sort", "domainKey-d,depKey-d");
		params.put("aggrs", "");
		JSONObject respPage = get("/view/demo/department",headers,params,JSONObject.class).getBody();
		log.info("zh");
		log.info(respPage.toString());
		//英文版本
		params.put("lang", "en-us");
		JSONObject respPageEn = get("/view/demo/department",headers,params,JSONObject.class).getBody();
		log.info("en-us");
		log.info(respPageEn.toString());
	}	
	
/*
 * 以下测试src/main/resources/view/department.sql文件中的内容：
 * ```SQL
 * select * from department
 * ```
 * src/main/resources/view/department.sql 文件映射到 restfulApi的路径为：http://xxxx/view/department
 * 
 */
	@Test
	@DisplayName("Fetch Data Structure Only")
	@Order(1)
    public void sqlToApiFetchDataStructureOnly()  {
		HttpHeaders headers = new HttpHeaders();
		Map<String,String> params = new HashMap<>();
		//中文版本
		params.put("lang", "zh");
		JSONObject respPage = get("/view/department",headers,params,JSONObject.class).getBody();
		AssertDepartmentStructure(respPage);
		//英文版本
		params.put("lang", "en-us");
		JSONObject respPageEn = get("/view/department",headers,params,JSONObject.class).getBody();
		//检查数据结构
		AssertDepartmentStructureEN(respPageEn);
	}


	
	@Test
	@DisplayName("Fetch Data")
	@Order(3)
    public void sqlToApiFetchData()  {
		HttpHeaders headers = new HttpHeaders();
		Map<String,String> params = new LinkedHashMap<>();
		//使用中午版本
		params.put("lang", "zh");
		//获取第零页，每页三行数据
		params.put("page", "0");
		params.put("size", "3");
		//d为倒序，a为顺序，不声明默认为a顺序
		params.put("sort", "domainKey-a,depKey-d");
		//可以继续增加其他字段的排序
		//params.put("sort", "depKey.d,otherField.a,otherField");

		JSONObject respPage0 = get("/view/department",headers,params,JSONObject.class).getBody();
		//检查返回数据结构信息
		AssertDepartmentStructure(respPage0);
		JSONArray data = respPage0.getJSONArray("data");
		//检查返回数据
		AssertData(data,3,"depKey","dep15","dep13");
		//第一页，与第零页具有相同的内容，相同的含义
		params.put("page", "1");
		params.put("size", "3");
		JSONObject respPage1 = get("/view/department",headers,params,JSONObject.class).getBody();
		//检查数据是否相同
		assertEquals(respPage0.get("data"),respPage1.get("data"));
		//检查字段信息是否相同
		assertEquals(respPage0.get("fields"),respPage1.get("fields"));
		//第二页
		params.put("page", "2");
		params.put("size", "3");
		JSONObject respPage2 = get("/view/department",headers,params,JSONObject.class).getBody();
		//检查数据是否相同
		assertNotEquals(respPage1.get("data"),respPage2.get("data"));
		//第二页开始不返回字段信息
		assertNull(respPage2.get("fields"));
		//第二页开始不返回视图名信息
		assertNull(respPage2.get("name"));
		//第二页开始不返回视图描述段信息
		assertNull(respPage2.get("desc"));
		
		//获取第零页，每页三行数据
		params.put("page", "0");
		params.put("size", "3");
		params.put("sort", "depKey");
		JSONObject respPageA = get("/view/department",headers,params,JSONObject.class).getBody();
		JSONArray dataA = respPageA.getJSONArray("data");
		//检查返回数据
		AssertData(dataA,3,"depKey","dep01","dep03");	
	}	
	
	@Test
	@DisplayName("Fetch Data Aggrs")
	@Order(4)
    public void sqlToApiFetchDataAggrs()  {
		HttpHeaders headers = new HttpHeaders();
		Map<String,String> params = new LinkedHashMap<>();
		params.put("lang", "zh");
		//获得返回记录总数
		params.put("aggrs", "");
		JSONObject respData0 = get("/view/department",headers,params,JSONObject.class).getBody();
		JSONObject aggrs0 = respData0.getJSONObject("aggrs");
		assertEquals(aggrs0.get("size"),15);
		//根据字段获得统计数据
		params.put("aggrs", "domainKey-count,memberCount-sum,memberCount-avg");
		JSONObject respData1 = get("/view/department",headers,params,JSONObject.class).getBody();
		JSONObject aggr1 = respData1.getJSONObject("aggrs");
		assertEquals(aggr1.get("size"),15);
		assertEquals(aggr1.get("domainKeyCount"),15);
		assertEquals(aggr1.get("memberCountSum"),60);
		assertEquals(aggr1.get("memberCountAvg"),4.0);
	}
	
	@Test
	@DisplayName("Fetch Data With Filter")
	@Order(5)
    public void sqlToApiFetchDataFilter()  {
		HttpHeaders headers = new HttpHeaders();
		
		Map<String,String> params = new LinkedHashMap<>();
		params.put("lang", "zh");
		params.put("page", "1");
		params.put("size", "3");
		//QueryFilter辅助生成filter并产生对应的JSON值
		
		SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		QueryCriteria queryCriteria = new QueryCriteria("createTime",simpleDateFormat.format( new Date((new Date()).getTime() + (1000*60*5))));
		queryCriteria.setOp(QueryCriteria.OP_LESS_THAN);
		
		QueryFilter enumQueryFilter = new QueryFilter(new QueryCriteria("depKey", "dep01"),queryCriteria);
		
		params.put("filter", enumQueryFilter.toJSONObject().toJSONString());
		JSONObject respData = get("/view/department",headers,params,JSONObject.class).getBody();
		//检查返回数据结构信息
		AssertDepartmentStructure(respData);
		JSONArray data = respData.getJSONArray("data");
		//检查返回数据
		AssertData(data,1,"depKey","dep01","dep01");
				
		Map<String,String> paramsAggr = new LinkedHashMap<>();
		paramsAggr.put("lang", "zh");
		//请求获得返回记录总数
		paramsAggr.put("aggrs", "");
		//QueryFilter辅助生成filter并产生对应的JSON值
		QueryFilter enumQueryFilterAggr = new QueryFilter(new QueryCriteria("depKey", "dep01"),queryCriteria);
		paramsAggr.put("filter", enumQueryFilterAggr.toJSONObject().toJSONString());
		JSONObject respDataAggr = get("/view/department",headers,paramsAggr,JSONObject.class).getBody();
		JSONObject aggrs = respDataAggr.getJSONObject("aggrs");
		//检查返回数据的记录数
		assertEquals(aggrs.get("size"),1);
	}
	
   /*
    * 以下测试src/main/resources/view/org/sys_user.sql文件中的内容：
    * ```SQL
    * /注解开始*
    * title: 用户
    * dataSource: dataSource
    * remark: |
    *   用户视图
    * param:
    *   depKey: string
    * fields:
    *   enum:
    *     departmentId:
    *       viewId: org.department
    *       value: depKey
    *       text: depName
    *     positionId:
    *       viewId: org.position
    *       param: 
    *         domainKey: domainKey
    *         depKey: departmentId
    *       value: posKey
    *       text: posName
    *     status: common.status
    *   key: domainKey,userKey
    *   hidden: domainKey,userKey
    *   file: photo
    * meta:
    *   group: security
    *   tables: sys_user
    *   setting: for Backend
    * props:
    *   setting: for Backend and Frontend
    * *注解结束/
    * select * from sys_user
    *   where domain_key = :currentDomain
    * ```
    */
	@Test
	@DisplayName("Fetch Data By Remark Yaml")
	@Order(6)
    public void sqlToApiFetchDataByYamlRemark()  {
		Map<String,String> params = new LinkedHashMap<>();
		params.put("lang", "zh");
		params.put("page", "1");
		params.put("size", "3");
		params.put("sort", "domainKey,userKey-d");
		params.put("departmentId", "dep03");
		JSONObject respData = get("/view/org/sys_user",new HttpHeaders(),params,JSONObject.class).getBody();
		log.info(respData.toString());
		/*
		 * # data 返回查询数据
		 */
		JSONArray data = respData.getJSONArray("data");
		AssertData(data,3,"userKey","STF007","STF005");//检查返回数据
		/*
		 * # 对字段中的值进行动态调整
         * 用法：
         * 1. 直接引用翻译
         *   '{{label.position}}'
         * 1. 引用静态枚举 
         *   '{{common.status{"value":"C"}}}'
         * 2. 引用动态枚举api
         *   '{{ department.field.positionId.enum {"value":"level2_dep3_1_pos1","depKey":"level2_dep3_1"} }}'
		 * 
		 */
		params.put("translate", "false");
		JSONObject respData1 = get("/view/org/sys_user",new HttpHeaders(),params,JSONObject.class).getBody();
		
		//翻译前：
		String nonTranRemark1 = respData1.getJSONArray("data").getJSONObject(1).getString("remark");
		assertEquals(nonTranRemark1.trim(),"""
				参数化提示信息：{{message.demo{"text":"文本","number":"123"}}}
				""".trim());
		
		//使用的模板：
		Map<String,String> paramsLang = new LinkedHashMap<>();
		paramsLang.put("lang", "zh");
		paramsLang.put("key", "message.demo");
		JSONObject dataLang = get("/lang",new HttpHeaders(),paramsLang,JSONObject.class).getBody();
		String langTemp = dataLang.getString("value");
		assertEquals(langTemp.trim(),"""
				文本: [${text}] 数字: [${number}] 再一次文本: [${text}]
				""".trim());
		
		//翻译后：
		String tranRemark1 = respData.getJSONArray("data").getJSONObject(1).getString("remark");
		assertEquals(tranRemark1,"参数化提示信息：文本: [文本] 数字: [123] 再一次文本: [文本]");
		
		//翻译前：
		String nonTranRemark2 = respData1.getJSONArray("data").getJSONObject(2).getString("remark");
		assertEquals(nonTranRemark2.trim(),"""
				{{label.position}}:{{ department.field.positionId.enum {"value":"pos2","depKey":"dep03"} }}({{common.status{"value":"C"}}})
				""".trim());
		//翻译后：
		String tranRemark2 = respData.getJSONArray("data").getJSONObject(2).getString("remark");
		assertEquals(tranRemark2,"职位:软件工程师(失效)");
		
		
		/*
		 * # valueMapping 值匹配
		 */
		JSONObject valueMapping = respData.getJSONObject("valueMapping");
		/* 字段departmentId查出来的只是部门代码，希望通过api匹配到对应的部门名称用如下设置：
		 * ```YAML
		 * fields: 
		 *   enum:
         *     departmentId:
         *       viewId: org.department
         *       value: depKey
         *       text: depName
		 * ```
		 * data中departmentId字段中返回的部门代码（value: depKey）逐一通过viewId:org.department查出相应的部门名称（text: depName）
		 * 查询得到的depKey与depName的值匹配保存到JSON中的valueMapping字段
		 */
		JSONObject departmentIdMapping = valueMapping.getJSONObject("departmentId");
		for(int i = 0; i < data.size(); i++) {
			//通过api返回的valueMapping字段获取部门名
			JSONObject item = data.getJSONObject(i);
			String departmentId = item.getString("departmentId");
			String departmentName1 = departmentIdMapping.getString(departmentId);
			//等价的视图api访问获取部门名
			Map<String,String> paramsDepartment = new LinkedHashMap<>();
			paramsDepartment.put("lang", "zh");
			paramsDepartment.put("page", "1");
			paramsDepartment.put("size", "1");
			QueryFilter queryFilter = new QueryFilter(new QueryCriteria("depKey", departmentId));
			paramsDepartment.put("filter", queryFilter.toJSONObject().toJSONString());			
			JSONObject departDepartmentData = get("/view/org/department",new HttpHeaders(),paramsDepartment,JSONObject.class).getBody();
			String departmentName2 = departDepartmentData.getJSONArray("data").getJSONObject(0).getString("depName");
			assertEquals(departmentName1,departmentName2);
		} 
		/*
		 * 由于职位从属于部门，positionId字段需要根据departmentId的内容去确定如何获取positionId对应的名称。
		 * 查找职位名称需要引用domainKey，departmentId作为参数：
		 * ```YAML
		 * fields: 
		 *   enum:
		 *     positionId:
		 *       viewId: org.position
		 *       param: 
		 *         domainKey: domainKey
		 *         depKey: departmentId
		 *       value: posKey
		 *       text: posName
		 * ```
		 */
		JSONObject positionIdMapping = valueMapping.getJSONObject("positionId");
		for(int i = 0; i < data.size(); i++) {
			//通过api返回的valueMapping字段获取职位名
			JSONObject item = data.getJSONObject(i);
			String positionId = item.getString("positionId");
			String positionName1 = positionIdMapping.getString(positionId);
			//等价的视图api访问获取职位名
			Map<String,String> paramsForView = new LinkedHashMap<>();
			paramsForView.put("lang", "zh");
			paramsForView.put("page", "1");
			paramsForView.put("size", "1");
			String domainKey = item.getString("domainKey");
			String departmentId = item.getString("departmentId");
			paramsForView.put("domainKey", domainKey);
			paramsForView.put("depKey", departmentId);
			String perfix = domainKey+"_"+departmentId+"_";
			QueryFilter queryFilter = new QueryFilter(new QueryCriteria("posKey", positionId.replaceFirst(perfix, "")));
			paramsForView.put("filter", queryFilter.toJSONObject().toJSONString());			
			JSONObject dataForView = get("/view/org/position",new HttpHeaders(),paramsForView,JSONObject.class).getBody();
			String positionName2 = dataForView.getJSONArray("data").getJSONObject(0).getString("posName");
			assertEquals(positionName1,positionName2);
		}
		
		/*
		 * 状态枚举字段，引用了翻译配置。对应状态值的显示内容。
		 * ```YAML
		 * fields: 
		 *   enum:
		 *     status: common.status
		 * ```
		 * 对应的翻译配置文件定义：
		 * \src\main\resources\lang\en-us.yml
		 * ```YAML
		 * common.status:
		 *   A: Active
		 *   C: Closed 
		 * ```
		 * \src\main\resources\lang\zh.yml
		 * ```YAML
		 * common.status:
		 *   A: 有效
		 *   C: 失效
		 * ```
		 */
		JSONObject statusMapping = valueMapping.getJSONObject("status");
		for(int i = 0; i < data.size(); i++) {
			//通过api返回的valueMapping字段获取状态值
			JSONObject item = data.getJSONObject(i);
			String status = item.getString("status");
			//data中如果有null值，需要转换为"null"字符串去valueMapping中进行匹配
			String statusName1 = statusMapping.getString(status==null?"null":status);
			//等价的翻译api访问获取状态值
			Map<String,String> paramsForView = new LinkedHashMap<>();
			paramsForView.put("lang", "zh");
			paramsForView.put("key", "common.status."+status);
			JSONObject dataForView = get("/lang",new HttpHeaders(),paramsForView,JSONObject.class).getBody();
			String statusName2 = dataForView.getString("value");
			assertEquals(statusName1,statusName2);
		}
		/*
		 * # fields的其他设置
		 *   可以根据需要对字段属性增加标记，如那些字段是key，那些字段对前端隐藏显示，那些字段属于文件。达到指导前端如何处理返回数据的效果。
		 *   enum会向前端提供对应的视图api查询方法或直接提供枚举值。
		 * fields: 
		 *   enum:
		 *     departmentId：xxxx
		 *     positionId：xxxxxx
		 *     status：xxxxxxxxxx
	     *   key: domainKey,userKey
	     *   hidden: domainKey,userKey
	     *   file: photo		
		*/
		JSONArray fields = respData.getJSONArray("fields");
		for(int i = 0; i < fields.size(); i++) {
			JSONObject field = fields.getJSONObject(i);
			String fieldId = field.getString("field");
			if(fieldId.equals("domainKey")||fieldId.equals("userKey")) {
				assertTrue(field.getJSONObject("props").getBoolean("key"));
				assertTrue(field.getJSONObject("props").getBoolean("hidden"));
			}else {
				assertFalse(field.getJSONObject("props").getBoolean("key"));
				assertFalse(field.getJSONObject("props").getBoolean("hidden"));
			}
			if(fieldId.equals("photo")) {
				assertTrue(field.getJSONObject("props").getBoolean("file"));
			}else {
				assertFalse(field.getJSONObject("props").getBoolean("file"));
			}
			
			if(fieldId.equals("departmentId")) {
				JSONObject value = (JSONObject) JSONObject.parse( """
					{ "viewId": "org.department", "valueField": "depKey", "textField": "depName" }
						""");
				JSONObject enumApi = field.getJSONObject("enumApi");
				assertTrue(compareJSONObjects(enumApi,value));
			}
			if(fieldId.equals("positionId")) {
				JSONObject value = (JSONObject) JSONObject.parse("""
					{
					    "viewId": "org.position",
					    "param": { "domainKey": "domainKey", "depKey": "departmentId" },
					    "valueField": "posKey",
					    "textField": "posName"
					}						
						""");
				JSONObject enumApi = field.getJSONObject("enumApi");
				assertTrue(compareJSONObjects(enumApi,value));
				
			}
			if(fieldId.equals("status")) {
				JSONArray value = (JSONArray) JSONArray.parse("""
					[
					    { "value": "null", "text": "不适用" },
					    { "value": "A", "text": "有效" },
					    { "value": "C", "text": "失效" }
					]
						""");
				JSONArray enumDataList = field.getJSONArray("enumDataList");
				assertTrue(compareJSONArrays(enumDataList,value));
			}
		}

		/*
		 * # 其他属性设置： 
		 *    meta中定义的属性通过restful访问不能获取其内容。主要提供权限定义，视图设计相关敏感信息。只提供后端获取。
		 *    props中定义的属性前后端都可以访问，起到指导前端如何去处理返回数据的作用。
         * meta:
         *   group: security
         *   tables: sys_user
         *   setting: for Backend
         * props:
         *   setting: for Backend and Frontend
		 */
		JSONObject meta = respData.getJSONObject("meta");
		assertNull(meta);
		JSONObject props = respData.getJSONObject("props");
		String setting = props.getString("setting");
		assertEquals(setting,"for Backend and Frontend");
	}
	
	/*
	 * src/main/resources/view/org/sys_user.sql文件中加入树结构描述：
	 * ```YAML
	 * tree:
	 *   id: userKey
	 *   parent: parentUser
	 *   search: userKey,userName
	 * ```
	 * 分别是树的ID字段，数的上级ID字段和搜索字段，其中搜索字段非必须。
	 * 加入树结构描述后可以通过tree的API进行递归运算返回树结构数据。
	 */
	@Test
	@DisplayName("Tree View Api")
	@Order(7)
    public void treeViewApi()  {
		Map<String,String> paramsTree = new LinkedHashMap<>();
		paramsTree.put("lang", "zh");
		//定义那个树节点开始遍历，不传值测从父节点为null的顶级节点开始。
		paramsTree.put("value", "STF001");
		//遍历深度为三层，不传值会完全遍历到最末端
		paramsTree.put("level", "3");
		//每一层节点的排序方式
		paramsTree.put("sort", "userKey-d");
		//在多个字段中搜索数据
		paramsTree.put("search", "Hahn");
		//sql中用到的参数
		paramsTree.put("departmentId", "dep03");
		JSONObject dataTree = get("/tree/org/sys_user",new HttpHeaders(),paramsTree,JSONObject.class).getBody();
		
		JSONArray nodeData = dataTree.getJSONArray("data");
		AssertData(nodeData,2,"node.userKey","STF005","STF002");
		
		JSONArray children1Level1 = nodeData.getJSONObject(0).getJSONArray("children");
		AssertData(children1Level1,1,"node.userKey","STF006","STF006");

		JSONArray children1Level2 = children1Level1.getJSONObject(0).getJSONArray("children");
		AssertData(children1Level2,1,"node.userKey","STF007","STF007");

		JSONArray children2Level1 = nodeData.getJSONObject(1).getJSONArray("children");
		AssertData(children2Level1,1,"node.userKey","STF003","STF003");

		JSONArray children2Level2 = children2Level1.getJSONObject(0).getJSONArray("children");
		AssertData(children2Level2,1,"node.userKey","STF004","STF004");
		
		
		Map<String,String> paramsView = new LinkedHashMap<>();
		paramsView.put("lang", "zh");
		paramsView.put("page", "1");
		paramsView.put("size", "30");
		paramsView.put("sort", "domainKey,userKey-d");
		paramsView.put("departmentId", "dep03");
		JSONObject dataView = get("/view/org/sys_user",new HttpHeaders(),paramsView,JSONObject.class).getBody();
		
		assertEquals(dataTree.getJSONObject("valueMapping").toString(),dataView.getJSONObject("valueMapping").toString());
		assertEquals(dataTree.getJSONArray("fields").toString(),dataView.getJSONArray("fields").toString());
		assertEquals(dataTree.getString("name"),dataView.getString("name"));
		assertEquals(dataTree.getString("desc"),dataView.getString("desc"));
		assertEquals(dataTree.getJSONObject("props").toString(),dataView.getJSONObject("props").toString());
		
		
		Map<String,String> paramsTreePath = new LinkedHashMap<>();
		paramsTreePath.put("lang", "zh");
		//定义那个树节点开始遍历，不传值测从父节点为null的顶级节点开始。
		paramsTreePath.put("value", "STF007");
		//每一层节点的排序方式
		//paramsTreePath.put("sort", "userKey-d");
		//sql中用到的参数
		paramsTreePath.put("departmentId", "dep03");
		JSONObject dataTreePath = get("/tree/org/sys_user",new HttpHeaders(),paramsTreePath,JSONObject.class).getBody();
		JSONArray pathNodeData = dataTreePath.getJSONArray("data");
		
		AssertData(pathNodeData,4,"node.userKey","STF007","STF001");
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

	private boolean compareJSONObjects(JSONObject obj1, JSONObject obj2) {
        for (String key : obj1.keySet()) {
            Object value1 = obj1.get(key);
            Object value2 = obj2.get(key);
            if (value1 instanceof JSONObject && value2 instanceof JSONObject) {
                if (!compareJSONObjects((JSONObject) value1, (JSONObject) value2)) {
                    return false;
                }
            } else if (value1 instanceof JSONArray && value2 instanceof JSONArray) {
                if (!compareJSONArrays((JSONArray) value1, (JSONArray) value2)) {
                    return false;
                }
            } else if (value1!=null && value2!=null && !value1.equals(value2)) {
            	log.error("obj not equals");
                return false;
            }
        }
        return true;
    }	
	
	private boolean compareJSONArrays(JSONArray array1, JSONArray array2) {
	        if (array1.size() != array2.size()) {
	        	log.error("array size not match");
	            return false;
	        }
	        for (int i = 0; i < array1.size(); i++) {
	            Object obj1 = array1.get(i);
	            Object obj2 = array2.get(i);
	            if (obj1 instanceof JSONObject && obj2 instanceof JSONObject) {
	                if (!compareJSONObjects((JSONObject) obj1, (JSONObject) obj2)) {
	                    return false;
	                }
	            } else if (!obj1.equals(obj2)) {
	            	log.error("array obj not equals:"+obj1+":"+obj2);
	                return false;
	            }
	        }
	        return true;
	    }
	
	
	private void AssertData(JSONArray data,Integer pageSize,String checkField,Object first,Object last) {
		JSONObject firstJO = data.getJSONObject(0);
		Object firstVal = getJSONField(firstJO,checkField);
		JSONObject lastJO = data.getJSONObject(data.size()-1);
		Object lastVal = getJSONField(lastJO,checkField);
		assertEquals(pageSize, data.size());
		assertEquals(firstVal,first);
		assertEquals(lastVal,last);
	}
	
	@SuppressWarnings("unchecked")
	private String getJSONField(JSONObject value,String field) {
		String[] fields = field.split("[.]");
		LinkedHashMap<String,Object> temp = value.toJavaObject(LinkedHashMap.class);
		for(String f:fields) {
			Object v = temp.get(f);
			if(v instanceof LinkedHashMap) {
				temp = (LinkedHashMap<String, Object>) v;
			}
			
			if(v instanceof ArrayList) {
				
			}
			
			
			if(v instanceof String) {
				return (String) v;
			}
		}
		return null;
	}
	
	/*
	 * 以下测试返回的视图结构内容是否正常按照翻译逻辑获取配置。在英文翻译中找不到对应项，会到默认翻译中寻找。
	 * 表，字段对应的解析通过以下文件进行配置：
	 * /src/main/resources/lang/en-us.yml
	 * ```YAML
     *department:
     *  name: Department
     *  field:
     *    domainKey: Domain Name
     *    domainKey.desc: Divide the region
     *    depKey: Department Code
     *    depKey.desc: The code that marks this department
	 * ```
	 */	
	private void AssertDepartmentStructureEN(JSONObject value){
        assertEquals("Department", value.getString("name"));
        assertEquals("Stores company department-related information", value.getString("desc"));
	}

	/*
	 * 以下测试返回的视图结构内容是否正常。
	 * 表，字段对应的解析通过以下文件进行配置：
	 * /src/main/resources/lang/zh.yml
	 * ```YAML
     *department:
     *  name: 部门
     *  desc: 保存公司部门相关资料
     *  field:
     *    domainKey: 域名
     *    domainKey.desc: 划分区域
     *    depKey: 部门代码
     *    depKey.desc: 标记该部门的代码
	 * ```	 
	 */	
	private void AssertDepartmentStructure(JSONObject value){
		if(isSqlite())return;//sqlite不断言字段类型
        // 比较顶级字段
        assertEquals("部门", value.getString("name"));
        assertEquals("保存公司部门相关资料", value.getString("desc"));
		Assertions.assertTrue(value.getLong("endTime")>=value.getLong("startTime"));

		JSONArray fields = value.getJSONArray("fields");
        // 比较 fields 数组中的每个元素
        assertEquals(9, fields.size());
        int idx = 0;
        assertEquals("domainKey", fields.getJSONObject(idx).getString("field"));
        assertEquals(false, fields.getJSONObject(idx).getBoolean("nullable"));
        assertEquals(100, fields.getJSONObject(idx).getIntValue("preci"));
        assertEquals("域名", fields.getJSONObject(idx).getString("name"));
        assertEquals(0, fields.getJSONObject(idx).getIntValue("scale"));
        assertEquals("string", fields.getJSONObject(idx).getString("type"));
        assertEquals("划分区域", fields.getJSONObject(idx).getString("desc"));
        idx++;
        
        assertEquals("depKey", fields.getJSONObject(idx).getString("field"));
        assertEquals(false, fields.getJSONObject(idx).getBoolean("nullable"));
        assertEquals(100, fields.getJSONObject(idx).getIntValue("preci"));
        assertEquals("部门代码", fields.getJSONObject(idx).getString("name"));
        assertEquals(0, fields.getJSONObject(idx).getIntValue("scale"));
        assertEquals("string", fields.getJSONObject(idx).getString("type"));
        assertEquals("标记该部门的代码", fields.getJSONObject(idx).getString("desc"));
        idx++;

      //翻译文件中没有匹配项，返回原始值
        assertEquals("depName", fields.getJSONObject(idx).getString("field"));
        assertEquals(false, fields.getJSONObject(idx).getBoolean("nullable"));
        assertEquals(100, fields.getJSONObject(idx).getIntValue("preci"));
        assertEquals("depName", fields.getJSONObject(idx).getString("name"));
        assertEquals(0, fields.getJSONObject(idx).getIntValue("scale"));
        assertEquals("string", fields.getJSONObject(idx).getString("type"));
        assertEquals("depName", fields.getJSONObject(idx).getString("desc"));
        idx++;

        assertEquals("memberCount", fields.getJSONObject(idx).getString("field"));
        assertEquals(true, fields.getJSONObject(idx).getBoolean("nullable"));
        assertEquals(10, fields.getJSONObject(idx).getIntValue("preci"));
        assertEquals("memberCount", fields.getJSONObject(idx).getString("name"));
        assertEquals(0, fields.getJSONObject(idx).getIntValue("scale"));
        assertEquals("number", fields.getJSONObject(idx).getString("type"));
        assertEquals("memberCount", fields.getJSONObject(idx).getString("desc"));
        idx++;

        assertEquals("stopped", fields.getJSONObject(idx).getString("field"));
        assertEquals(true, fields.getJSONObject(idx).getBoolean("nullable"));
        assertEquals(1, fields.getJSONObject(idx).getIntValue("preci"));
        assertEquals("停止", fields.getJSONObject(idx).getString("name"));
        assertEquals(0, fields.getJSONObject(idx).getIntValue("scale"));
        assertEquals("boolean", fields.getJSONObject(idx).getString("type"));
        assertEquals("stopped", fields.getJSONObject(idx).getString("desc"));
        idx++;

        assertEquals("createTime", fields.getJSONObject(idx).getString("field"));
        assertEquals(false, fields.getJSONObject(idx).getBoolean("nullable"));
        //assertEquals(19, fields.getJSONObject(idx).getIntValue("preci"));
        assertEquals("createTime", fields.getJSONObject(idx).getString("name"));
        //assertEquals(0, fields.getJSONObject(idx).getIntValue("scale"));
        assertEquals("datetime", fields.getJSONObject(idx).getString("type"));
        assertEquals("createTime", fields.getJSONObject(idx).getString("desc"));
        idx++;

        assertEquals("createUser", fields.getJSONObject(idx).getString("field"));
        assertEquals(false, fields.getJSONObject(idx).getBoolean("nullable"));
        assertEquals(100, fields.getJSONObject(idx).getIntValue("preci"));
        assertEquals("createUser", fields.getJSONObject(idx).getString("name"));
        assertEquals(0, fields.getJSONObject(idx).getIntValue("scale"));
        assertEquals("string", fields.getJSONObject(idx).getString("type"));
        assertEquals("createUser", fields.getJSONObject(idx).getString("desc"));
        idx++;

        assertEquals("updateTime", fields.getJSONObject(idx).getString("field"));
        assertEquals(false, fields.getJSONObject(idx).getBoolean("nullable"));
        //assertEquals(19, fields.getJSONObject(idx).getIntValue("preci"));
        assertEquals("updateTime", fields.getJSONObject(idx).getString("name"));
        //assertEquals(0, fields.getJSONObject(idx).getIntValue("scale"));
        assertEquals("datetime", fields.getJSONObject(idx).getString("type"));
        assertEquals("updateTime", fields.getJSONObject(idx).getString("desc"));
        idx++;

        assertEquals("updateUser", fields.getJSONObject(idx).getString("field"));
        assertEquals(false, fields.getJSONObject(idx).getBoolean("nullable"));
        assertEquals(100, fields.getJSONObject(idx).getIntValue("preci"));
        assertEquals("updateUser", fields.getJSONObject(idx).getString("name"));
        assertEquals(0, fields.getJSONObject(idx).getIntValue("scale"));
        assertEquals("string", fields.getJSONObject(idx).getString("type"));
        assertEquals("updateUser", fields.getJSONObject(idx).getString("desc"));
	}
	

	private boolean isSqlite() {
		Connection conn = DataSourceUtils.getConnection(dataSource);
		try {
			if (conn != null && (!conn.isClosed())) {
				DatabaseMetaData metaData = conn.getMetaData();
				String databaseProductName = metaData.getDatabaseProductName().toLowerCase();
				if (databaseProductName.matches("(?i).*sqlite*")) {
					return true;
				}
			}
			return false;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to create tables for dashboards database.", e);
		}catch (Exception ex) {
			DataSourceUtils.releaseConnection(conn, dataSource);
			conn = null;
			throw new RuntimeException(ex);
		}finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
		}		
	}
	
}
