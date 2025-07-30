package org.weaver.controller;

import java.util.Date;
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

	private static final Logger log = LoggerFactory.getLogger(View.class);

	@Autowired
	private TableService tableService;
	
	@GetMapping("**")
	public ResponseEntity<JSONObject> readTableData(
			HttpServletRequest request,
			@RequestParam Map<String,Object> data
			){
		RequestConfig reqConfig = new RequestConfig();
		String table = request.getRequestURL().toString().split("/table/")[1].replace("/", ".");
		String datasource = request.getHeader("datasource");
		log.info("datasource:"+datasource);
		Date startTime = new Date();
		JSONObject tableInfo = tableService.readTable(datasource, table, data, reqConfig);
		tableInfo.put("data", data);
		tableInfo.put("startTime", startTime);
		tableInfo.put("endTime", new Date());
		return new ResponseEntity<>(tableInfo,HttpStatus.OK);
	}	
	
	@PostMapping("**")
	public ResponseEntity<Map<String,Object>> createNew(
			HttpServletRequest request,
			@RequestBody Map<String,Object> data
			){
		RequestConfig reqConfig = new RequestConfig();
		String table = request.getRequestURL().toString().split("/table/")[1].replace("/", ".");
		String datasource = request.getHeader("datasource");
		log.info("datasource:"+datasource);
		
        reqConfig.getParams().put("createBy", "ryan");
        reqConfig.getParams().put("createTime", new Date());
        reqConfig.getParams().put("status", "0");
        reqConfig.getParams().put("delFlag", 0);	
        
        System.out.println("reqConfig.getParams()"+reqConfig.getParams());
		
		Integer result = tableService.insertTable(datasource, table, data, reqConfig);
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("rows-affected", result.toString());
		return new ResponseEntity<>(data,headers, HttpStatus.OK);
	}
	
	@PutMapping("**")
	public ResponseEntity<Map<String, Object>> modifyEdit(
			HttpServletRequest request,
			@RequestBody Map<String,Object> data
			){
		String tableId = request.getRequestURL().toString().split("/table/")[1].replace("/", ".");
		String datasource = request.getHeader("datasource");
		log.info("datasource:"+datasource);

		RequestConfig reqConfig = new RequestConfig();
        reqConfig.getParams().put("updateBy", "ryan");
        reqConfig.getParams().put("updateTime", new Date());
        
		Integer result = tableService.updateTable(datasource,tableId, data,reqConfig);
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("rows-affected", result.toString());
		return new ResponseEntity<>(data,headers, HttpStatus.OK);
	}
	
	@DeleteMapping("**")
	public ResponseEntity<Map<String, Object>> deleteErase(
			HttpServletRequest request,
			@RequestBody Map<String,Object> data
			){
		RequestConfig reqConfig = new RequestConfig();
		String tableId = request.getRequestURL().toString().split("/table/")[1].replace("/", ".");
		String datasource = request.getHeader("datasource");
		log.info("datasource:"+datasource);
		Integer result = tableService.deleteTable(datasource,tableId,data,reqConfig);
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("rows-affected", result.toString());
		return new ResponseEntity<>(data,headers, HttpStatus.OK);
	}
}
