package org.weaver.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.weaver.query.entity.RequestConfig;
import org.weaver.service.TableService;
import org.weaver.table.entity.UpdateCommand;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/modifyDataWithTrx/")
public class ModifyDataWithTrx {
	private static final Logger log = LoggerFactory.getLogger(ModifyDataWithTrx.class);

	@Autowired
	private TableService tableService;

	String classLevelMapping = "/modifyDataWithTrx/";
	
	@PostMapping("**")
	public ResponseEntity<List<UpdateCommand<Map<String,Object>>>> modifyDataWithTrx(
			HttpServletRequest request,
			@RequestBody List<UpdateCommand<Map<String,Object>>> updateCommands
			){
		RequestConfig reqConfig = new RequestConfig();
		tableService.setTableReqConfig(reqConfig);
		String actionId = request.getRequestURL().toString().split(classLevelMapping)[1].replace("/", ".");
		havePermission(actionId,"edit");
		String datasource = request.getHeader(Table.HEADER_DATA_SOURCE);
        reqConfig.getParams().put("createBy", LoginHelper.getUsername());
        reqConfig.getParams().put("createTime", new Date());
        reqConfig.getParams().put("status", "0");
        reqConfig.getParams().put("delFlag", 0);
        reqConfig.getParams().put("updateBy", LoginHelper.getUsername());
        reqConfig.getParams().put("updateTime", new Date());
        tableService.modifyDataWithTrx(datasource, updateCommands, reqConfig);
		return new ResponseEntity<>(updateCommands, HttpStatus.OK);
	}
	
    private void havePermission(String tableName,String action){
        log.info(String.format("find permission mapping for '%s' action '%s'",tableName,action));
    }
}
