package org.weaver.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.weaver.query.entity.RequestConfig;
import org.weaver.service.TableService;

import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/table/")
public class Table {

	private static final Logger log = LoggerFactory.getLogger(Table.class);
	
	private static final String HEADER_WHERE_FIELDS = "whereFields";

	private static final String HEADER_ASSERT_MAX_RECORD_AFFECTED = "assertMaxRecordAffected";
	
	static final String HEADER_DATA_SOURCE = "dataSource";

	@Autowired
	private TableService tableService;
	
	String classLevelMapping = "/table/";
	
	@GetMapping("**")
	public ResponseEntity<JSONObject> readTableData(
			HttpServletRequest request,
			@RequestParam Map<String,Object> data
			) throws Exception{
		RequestConfig reqConfig = new RequestConfig();
		tableService.setTableReqConfig(reqConfig);
		String table = request.getRequestURL().toString().split(classLevelMapping)[1].replace("/", ".");
		havePermission(table,"list");		
		String datasource = request.getHeader(HEADER_DATA_SOURCE);
		Date startTime = new Date();
		JSONObject tableInfo = tableService.readTable(datasource, table, data, reqConfig);
		tableInfo.put("data", data);
		tableInfo.put("startTime", startTime);
		tableInfo.put("endTime", new Date());
		return new ResponseEntity<>(tableInfo,HttpStatus.OK);
	}
	
	@PostMapping("**")
	public ResponseEntity<Map<String,Object>> addRecord(
			HttpServletRequest request,
			@RequestBody Map<String,Object> data
			){
		RequestConfig reqConfig = new RequestConfig();
		tableService.setTableReqConfig(reqConfig);
		String table = request.getRequestURL().toString().split(classLevelMapping)[1].replace("/", ".");
		havePermission(table,"add");
		String datasource = request.getHeader(HEADER_DATA_SOURCE);
		Map<String, Object> params = getSystemInfoForParams();
		params.put("createBy", params.get("currentNickName"));
		params.put("createTime", params.get("currentDate"));
		params.put("status", "0");
		params.put("delFlag", 0);
		reqConfig.setParams(params);
		Integer result = tableService.insertTable(datasource, table, data, reqConfig);
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("rows-affected", result.toString());
		return new ResponseEntity<>(data,headers, HttpStatus.OK);
	}
	
	@PutMapping("**")
	public ResponseEntity<int[]> persistenTableBatch(
			HttpServletRequest request,
			@RequestBody List<Map<String,Object>> datas
			){
		String table = request.getRequestURL().toString().split(classLevelMapping)[1].replace("/", ".");
		havePermission(table,"add");
		havePermission(table,"edit");
		String datasource = request.getHeader(HEADER_DATA_SOURCE);
		RequestConfig reqConfig = new RequestConfig();
		tableService.setTableReqConfig(reqConfig);
		Map<String, Object> params = getSystemInfoForParams();
		//当提交数据没有赋值情况下才会自动补上以下值
		params.put("createBy", params.get("currentNickName"));
		params.put("createTime", params.get("currentDate"));
		params.put("status", "0");
		params.put("delFlag", 0);		
		params.put("updateBy", params.get("currentNickName"));
		params.put("updateTime", new Date());
		reqConfig.setParams(params);
		int[] result = tableService.persistenTableBatch(datasource,table, datas,reqConfig);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@PatchMapping("**")
	public ResponseEntity<Integer> edit(
			HttpServletRequest request,
			@RequestBody Map<String,Object> data
			){
		String table = request.getRequestURL().toString().split(classLevelMapping)[1].replace("/", ".");
		havePermission(table,"edit");
		String datasource = request.getHeader(HEADER_DATA_SOURCE);
		String whereFields = request.getHeader(HEADER_WHERE_FIELDS);
		String assertMaxRecordAffected = request.getHeader(HEADER_ASSERT_MAX_RECORD_AFFECTED);
		RequestConfig reqConfig = new RequestConfig();
		tableService.setTableReqConfig(reqConfig);
		Map<String, Object> params = getSystemInfoForParams();
		params.put("updateBy", params.get("currentNickName"));
		params.put("updateTime", params.get("currentDate"));        
		reqConfig.setParams(params);
		Integer result = 0;
		if(whereFields!=null && assertMaxRecordAffected!=null) {
			String[] fields = whereFields.split(",");
			result = tableService.updateTableBatch(datasource,table,data,Long.valueOf(assertMaxRecordAffected),reqConfig,fields);
		}else {
			result = tableService.updateTable(datasource,table,data,reqConfig);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@DeleteMapping("**")
	public ResponseEntity<Integer> remove(
			HttpServletRequest request,
			@RequestBody Map<String,Object> data
			){
		RequestConfig reqConfig = new RequestConfig();
		tableService.setTableReqConfig(reqConfig);
		String table = request.getRequestURL().toString().split(classLevelMapping)[1].replace("/", ".");
		havePermission(table,"remove");
		String datasource = request.getHeader(HEADER_DATA_SOURCE);
		String whereFields = request.getHeader(HEADER_WHERE_FIELDS);
		String assertMaxRecordAffected = request.getHeader(HEADER_ASSERT_MAX_RECORD_AFFECTED);
		Integer result = 0;
		if(whereFields!=null && assertMaxRecordAffected!=null) {
			String[] fields = whereFields.split(",");
			result = tableService.deleteTableBatch(datasource,table,data,Long.valueOf(assertMaxRecordAffected),reqConfig,fields);
		}else {
			result = tableService.deleteTable(datasource,table,data,reqConfig);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	private Map<String, Object> getSystemInfoForParams() {
		Map<String, Object> params = new HashMap<>();
        LoginUser loginUser = LoginHelper.getLoginUser();
        Long userId = loginUser.getUserId();
        String userName = loginUser.getUsername();
        Long deptId = loginUser.getDeptId();
        Long workshopId = loginUser.getWorkshopId();
        String nickName = LoginHelper.getNickName();
        params.put("currentUserId",userId);
        params.put("currentUserName",userName);
        params.put("currentNickName", nickName);
        params.put("currentDeptId",deptId);
        params.put("currentWorkshopId",workshopId);
        params.put("currentDate", new Date());		
        return params;
	}
	
    private void havePermission(String tableName,String action){
        log.info(String.format("find permission mapping for '%s' action '%s'",tableName,action));
    }
	
}
