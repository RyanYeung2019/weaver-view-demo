package org.weaver.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.weaver.view.query.ViewQuery;

@RestController
@RequestMapping("/lang")
public class Lang {

	
	@Autowired
	private ViewQuery viewQuery;
	
	@GetMapping()
	public ResponseEntity<Map<String,String>> getLang(
			@RequestParam(required = false) String lang,
			@RequestParam(required = true) String key){
		Map<String,String> result = new HashMap<>();
		result.put("value", viewQuery.getLang(lang,key));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

}
