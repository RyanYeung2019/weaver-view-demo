package org.weaver.controller;

import java.util.Date;
import java.util.HashMap;
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
import org.weaver.Utils;
import org.weaver.view.query.ViewQuery;
import org.weaver.view.query.ViewStatement;
import org.weaver.view.query.entity.ViewData;
import org.weaver.view.query.entity.RequestConfig;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/view/")
public class View {

	private static final Logger log = LoggerFactory.getLogger(View.class);

	@Autowired
	private ViewQuery viewQuery;
	
	
	@GetMapping("**")
	public ResponseEntity<ViewData<Map<String, Object>>> queryViewData(HttpServletRequest request,
			@RequestParam Map<String,Object> params, 
			@RequestParam(required = false) String[] sort,
			@RequestParam(required = false) Integer page, 
			@RequestParam(required = false) Integer size,
			@RequestParam(required = false) String filter, 
			@RequestParam(required = false) String[] aggrs,
			@RequestParam(required = false) String lang, 
			@RequestParam(required = false) Boolean translate)
			throws Exception {
		log.debug(Utils.showSampleUrlInDebugLog(request));
		String viewId = request.getRequestURL().toString().split("/view/")[1].replace("/", ".");

		RequestConfig viewReqConfig = new RequestConfig();
		viewReqConfig.setLanguage(lang);
		viewReqConfig.setTranslate(translate);
		Map<String, Object> queryParams = new HashMap<>();
		queryParams.put("currentDomain", "domain1");
		queryParams.put("currentUser", "admin");
		viewReqConfig.setParams(queryParams);
		
		ViewStatement statement = viewQuery.prepareView(viewId,sort,page,size,filter,aggrs);
		statement.setParams(params);
		statement.setViewReqConfig(viewReqConfig);
		ViewData<Map<String, Object>> data = statement.query();

		return new ResponseEntity<>(data, HttpStatus.OK);
	}
	
	@PostMapping("**")
	public ResponseEntity<Map<String,Object>> createNew(
			HttpServletRequest request,
			@RequestBody Map<String,Object> data
			){
		String viewId = request.getRequestURL().toString().split("/view/")[1].replace("/", ".");
		
		RequestConfig reqConfig = new RequestConfig();
		reqConfig.getParams().put("createBy", "ryan");
		reqConfig.getParams().put("createTime", new Date());
		reqConfig.getParams().put("status", "0");
		reqConfig.getParams().put("delFlag", 0);
		
		Integer result = viewQuery.insertViewTable(viewId, data,reqConfig);
		
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("rows-affected", result.toString());
		return new ResponseEntity<>(data,headers, HttpStatus.OK);
	}
	
	@PutMapping("**")
	public ResponseEntity<Map<String, Object>> modifyEdit(
			HttpServletRequest request,
			@RequestBody Map<String,Object> data
			){
		String viewId = request.getRequestURL().toString().split("/view/")[1].replace("/", ".");
		
		RequestConfig reqConfig = new RequestConfig();
        reqConfig.getParams().put("updateBy", "ryan");
        reqConfig.getParams().put("updateTime", new Date());
		
		Integer result = viewQuery.updateViewTable(viewId, data,reqConfig);
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
		String viewId = request.getRequestURL().toString().split("/view/")[1].replace("/", ".");
		Integer result = viewQuery.deleteViewTable(viewId, data,reqConfig);
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("rows-affected", result.toString());
		return new ResponseEntity<>(data,headers, HttpStatus.OK);
	}
	


}
