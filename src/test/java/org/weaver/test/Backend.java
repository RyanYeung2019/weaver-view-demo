package org.weaver.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.weaver.bean.DepartmentEntity;
import org.weaver.bean.SysUserEntity;
import org.weaver.config.entity.EnumApiEn;
import org.weaver.view.query.ViewQuery;
import org.weaver.view.query.ViewStatement;
import org.weaver.view.query.entity.EnumItemEn;
import org.weaver.view.query.entity.FieldInfo;
import org.weaver.view.query.entity.QueryCriteria;
import org.weaver.view.query.entity.QueryFilter;
import org.weaver.view.query.entity.SortByField;
import org.weaver.view.query.entity.ViewData;
import org.weaver.view.query.entity.ViewRequestConfig;
import org.weaver.view.query.mapper.BeanPropRowMapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@SpringBootTest
@DisplayName("Backend Call For SQL")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Backend {

	@Autowired
	private ViewQuery viewQuery;

	@SuppressWarnings("unused")
	@Test
	@DisplayName("Base Usage")
	@Order(1)
	public void baseUsage() throws Exception {
		ViewStatement statement = viewQuery
				.prepareSql("select * from view_demo.department where member_count = :memberCount");
		statement.setPageNum(0);
		statement.setPageSize(15);
		statement.setAggrList(new ArrayList<String>());
		statement.putParam("memberCount", 2);

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		QueryCriteria queryCriteria = new QueryCriteria("createTime",
				simpleDateFormat.format(new Date((new Date()).getTime() + (1000 * 60 * 5))));
		queryCriteria.setOp(QueryCriteria.OP_LESS_THAN);
		QueryFilter enumQueryFilter = new QueryFilter(queryCriteria);
		statement.setQueryFilter(enumQueryFilter);

		ViewData<DepartmentEntity> dataBean = statement
				.query(new BeanPropRowMapper<DepartmentEntity>(DepartmentEntity.class));
		assertEquals(6l, dataBean.getAggrs().get("size"));
		assertEquals(dataBean.getData().size(), 6);

		// 获取视图名称
		String name = dataBean.getName(); 
		// 获取视图描述
		String desc = dataBean.getDesc(); 
		// 视图数据
		List<DepartmentEntity> data = dataBean.getData(); 
		// 视图相关的引用数据，枚举数据
		Map<String, Map<String, String>> valueMapping = dataBean.getValueMapping();
		// 相关消息
		String message = dataBean.getMessage();
		// 执行视图查询的开始时间
		Date startTime = dataBean.getStartTime();
		// 执行视图查询的结束时间
		Date endTime = dataBean.getEndTime();
		// 视图中字段相关信息
		List<FieldInfo> fieldInfos = dataBean.getFields();
		// 视图中有关统计数据
		Map<String, Object> aggrs = dataBean.getAggrs();
		// 配置属性
		JSONObject props = dataBean.getProps();
	}

	@SuppressWarnings("unused")
	@Test
	@DisplayName("Fetch Data Structure Only")
	@Order(1)
	public void viewFetchStructure() throws Exception{
		ViewStatement statement = viewQuery.prepareView("department");
		ViewData<Map<String, Object>> viewData = statement.query();
		List<FieldInfo> fields = viewData.getFields();
		assertEquals(fields.size(),9);
		for(FieldInfo fieldInfo:fields) {
			//字段变量名（通常为驼峰格式）
			String field = fieldInfo.getField();
			//显示给用户的字段名称，可以通过多语言字典配置其中内容
			String name = fieldInfo.getName();
			//显示给用户的字段描述内容，可以通过多语言字典配置其中内容
			String desc = fieldInfo.getDesc();
			//字段类型（已转换为 typeScript 类型）
			String type = fieldInfo.getType();
			//字段精度
			Integer preci = fieldInfo.getPreci();
			//字段标度
			Integer scale = fieldInfo.getScale();
			//字段是否允许空值
			Boolean nullable = fieldInfo.getNullable();
			//如果该字段有引用其他视图，这里会提供获取数据的api信息
			EnumApiEn enumApi = fieldInfo.getEnumApi();
			//如果该字段有设置为枚举，会根据当前的语言设置带出枚举数据内容
			List<EnumItemEn> enumDataList = fieldInfo.getEnumDataList();
			//保存有该字段相关的其他属性
			JSONObject props = fieldInfo.getProps();
		}
		//显示给用户的视图名称，可以通过多语言字典配置其中内容
		String name = viewData.getName();
		//显示给用户的视图描述，可以通过多语言字典配置其中内容
		String desc = viewData.getDesc();
		//视图相关的其他属性
		JSONObject props = viewData.getProps();
	}

	@Test
	@DisplayName("Fetch Data")
	@Order(2)
	public void viewFetchData() throws Exception{
		ViewStatement statement = viewQuery.prepareView("department");
		statement.setPageNum(1);
		statement.setPageSize(10);
		//默认查出数据保存为List<Map<String,Object>>格式
		ViewData<Map<String, Object>> viewData = statement.query();
		List<Map<String, Object>> data = viewData.getData();
		assertEquals(data.size(),10);
		//亦可以使用BeanPropRowMapper将数据匹配到Entity中去
		ViewData<DepartmentEntity> viewDataBean = statement.query(new BeanPropRowMapper<DepartmentEntity>(DepartmentEntity.class));
		List<DepartmentEntity> dataBean = viewDataBean.getData();
		assertEquals(dataBean.size(),10);
	}
	
	
	@Test
	@DisplayName("Fetch Aggrs")
	@Order(2)
	public void viewFetchAggrs() throws Exception{
		ViewStatement statement = viewQuery.prepareView("department");
		//分页设置
		statement.setPageNum(1);
		statement.setPageSize(10);
		//默认带出总记录数据
		statement.setAggrList(new ArrayList<String>());
		ViewData<Map<String, Object>> viewData = statement.query();
		//获取总记录数
		Long size = (Long) viewData.getAggrs().get(ViewData.AGGRS_SIZE);
		assertEquals(size,15l);
		//当前分页的记录数
		List<Map<String, Object>> data = viewData.getData();
		assertEquals(data.size(),10);
		
		//统计其他字段
		List<String> aggrList = new ArrayList<>();
		aggrList.add("memberCount-avg");
		aggrList.add("memberCount-sum");
		statement.setAggrList(aggrList);
		viewData = statement.query();
		//获取总记录数。
		size = (Long) viewData.getAggrs().get(ViewData.AGGRS_SIZE);
		assertEquals(size,15l);
		//获取统计其他字段的数据
		java.math.BigDecimal avg = (java.math.BigDecimal) viewData.getAggrs().get("memberCountAvg");
		assertEquals(avg.toString(),"4.0000");
		java.math.BigDecimal sum = (java.math.BigDecimal) viewData.getAggrs().get("memberCountSum");
		assertEquals(sum.toString(),"60");
	}	
	
	
	@Test
	@DisplayName("Fetch Data With Params")
	@Order(2)
	public void fetchDataWithParams() throws Exception{
		
		ViewStatement statement = viewQuery.prepareView("department_param");
		statement.setPageNum(1);
		statement.setPageSize(Integer.MAX_VALUE);

		//视图中的SQL需要传入参数，执行查询前要为视图赋予参数变量值
		statement.putParam("depKey", "dep07");
		
		//查找字段depKey中有"dep01"的数据。
		QueryCriteria queryCriteria1 = new QueryCriteria("memberCount", "2");
		
		//按日期过滤数据（日期格式默认为"yyyy-MM-dd HH:mm:ss",可以通过多语言数据字典更改日期格式）
		SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//查找字段createTime小于当前日期的数据
		QueryCriteria queryCriteria2 = new QueryCriteria("createTime",simpleDateFormat.format( new Date((new Date()).getTime()+5*60*1000 )        ));
		//默认为等于 QueryCriteria.OP_EQUAL
		queryCriteria2.setOp(QueryCriteria.OP_LESS_THAN);

		//将 memberCount=2 与 createTime<当前日期 这两个查询条件传入到QueryFilter。 两者之间的关系默认为 "AND"
		QueryFilter queryFilter = new QueryFilter(queryCriteria1,queryCriteria2);
		
		//将查询条件传入statement
		statement.setQueryFilter(queryFilter);
		ViewData<DepartmentEntity> viewData = statement.query(new BeanPropRowMapper<DepartmentEntity>(DepartmentEntity.class));
		
		assertEquals(viewData.getData().size(),1);
		
	}
	
	@Test
	@DisplayName("Fetch Data Use Entity Bean")
	@Order(3)
	public void sqlFetchData() throws Exception {
		ViewRequestConfig viewReqConfig = new ViewRequestConfig();
		viewReqConfig.setLanguage("zh");
		Map<String, Object> params = new HashMap<>();
		params.put("departmentId", "dep03");

		SortByField[] sort = new SortByField[] { new SortByField("domainKey"), new SortByField("userKey") };

		QueryFilter queryFilter = new QueryFilter(new QueryCriteria("userName", "Pickett"),
				new QueryCriteria("userName", "Dawson"));
		queryFilter.setType(QueryFilter.TYPE_OR);

		Map<String, Object> queryParams = new HashMap<>();
		queryParams.put("currentDomain", "domain1");
		queryParams.put("currentUser", "admin");
		viewReqConfig.setQueryParams(queryParams);

		ViewStatement statement = viewQuery.prepareView("org.sys_user");
		statement.setParams(params);
		statement.setSortField(sort);
		statement.setPageNum(1);
		statement.setPageSize(3);
		statement.setQueryFilter(queryFilter);
		statement.setViewReqConfig(viewReqConfig);
		ViewData<SysUserEntity> viewData = statement.query(new BeanPropRowMapper<SysUserEntity>(SysUserEntity.class));

		ViewStatement statementMap = viewQuery.prepareView("org.sys_user");
		statementMap.setParams(params);
		statementMap.setSortField(sort);
		statementMap.setPageNum(1);
		statementMap.setPageSize(3);
		statementMap.setQueryFilter(queryFilter);
		statementMap.setViewReqConfig(viewReqConfig);
		ViewData<Map<String, Object>> dataMap = statementMap.query();

		assertEquals(viewData.getData().size(), dataMap.getData().size());

		assertEquals(viewData.getData().get(0).getRemark(), dataMap.getData().get(0).get("remark"));
		assertEquals(viewData.getData().get(1).getRemark(), dataMap.getData().get(1).get("remark"));

		assertEquals(viewData.getData().get(0).getUserKey(), dataMap.getData().get(0).get("userKey"));
		assertEquals(viewData.getData().get(1).getUserKey(), dataMap.getData().get(1).get("userKey"));
	}

	@Test
	@DisplayName("I18n Messages")
	@Order(4)
	public void message() throws Exception {
		Map<String, Object> commonParams = new HashMap<>();
		commonParams.put("currentDomain", "domain1");
		commonParams.put("currentUser", "admin");

		Map<String, Object> msgParams = new HashMap<>();
		msgParams.put("text", "文本");
		msgParams.put("number", "123");

		Map<String, Object> positionParams = new HashMap<>();
		positionParams.put("value", "pos2");
		positionParams.put("depKey", "dep03");

		Map<String, Object> statusParams = new HashMap<>();
		statusParams.put("value", "C");
		String msgStatus = viewQuery.i18n("zh", "common.status", statusParams);
		assertEquals(msgStatus, "失效");

		String msg = viewQuery.i18n("zh", "message.demo", msgParams);
		assertEquals(msg, "文本: [文本] 数字: [123] 再一次文本: [文本]");

		String msgPosition = viewQuery.i18n("zh", "department.field.positionId.enum", positionParams, commonParams);
		assertEquals(msgPosition, "软件工程师");

		String msgText1 = viewQuery.text("zh", "参数化提示信息：{{message.demo}}", msgParams);
		String msgText2 = viewQuery.text("zh",
				String.format("参数化提示信息：{{message.demo%s}}", JSON.toJSONString(msgParams)));
		assertEquals(msgText1, msgText2);

		String enumText = viewQuery.text("zh",
				"""
						从枚举类型中获取值：{{label.position}}:{{ department.field.positionId.enum {"value":"pos2","depKey":"dep03"} }}({{common.status{"value":"C"}}})
						""",
				commonParams);
		assertEquals(enumText.trim(), "从枚举类型中获取值：职位:软件工程师(失效)");
	}
}
