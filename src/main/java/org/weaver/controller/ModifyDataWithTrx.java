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
		Map<String, Object> params = getSystemInfoForParams();
		reqConfig.setParams(params);
        tableService.modifyDataWithTrx(datasource, updateCommands, reqConfig);
		return new ResponseEntity<>(updateCommands, HttpStatus.OK);
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
