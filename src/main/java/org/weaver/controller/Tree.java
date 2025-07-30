package org.weaver.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.weaver.Utils;
import org.weaver.query.entity.RequestConfig;
import org.weaver.query.entity.TreeData;
import org.weaver.query.entity.ViewData;
import org.weaver.service.ViewQuery;
import org.weaver.service.ViewStatement;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tree/")
public class Tree {

	private static final Logger log = LoggerFactory.getLogger(Tree.class);

	@Autowired
	private ViewQuery viewQuery;

	@GetMapping("**")
	public ResponseEntity<ViewData<TreeData<Map<String, Object>>>> queryViewData(HttpServletRequest request,
			@RequestParam Map<String, Object> params, 
			@RequestParam(required = false) String value,
			@RequestParam(required = false) Integer level, 
			@RequestParam(required = false) String[] sort,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String lang,
			@RequestParam(required = false) Boolean translate) throws Exception {
		
		log.debug(Utils.showSampleUrlInDebugLog(request));
		String viewId = request.getRequestURL().toString().split("/tree/")[1].replace("/", ".");
		
		RequestConfig viewReqConfig = new RequestConfig();
		viewReqConfig.setLanguage(lang);
		viewReqConfig.setTranslate(translate);
		Map<String, Object> queryParams = new HashMap<>();
		queryParams.put("currentDomain", "domain1");
		queryParams.put("currentUser", "admin");
		viewReqConfig.setParams(queryParams);
		
		ViewStatement statement = viewQuery.prepareTree(viewId,sort);
		statement.setParams(params);
		statement.setValue(value);
		statement.setLevel(level);
		statement.setSearch(search);
		statement.setViewReqConfig(viewReqConfig);
		ViewData<TreeData<Map<String, Object>>> data = statement.queryTree();
		
		return new ResponseEntity<>(data, HttpStatus.OK);
	}
}
