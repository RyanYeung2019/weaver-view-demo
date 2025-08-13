package org.weaver.controller;

import java.util.Date;
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
import org.weaver.config.ViewDefine;
import org.weaver.query.entity.RequestConfig;
import org.weaver.query.entity.ViewData;
import org.weaver.service.ViewQuery;
import org.weaver.service.ViewStatement;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/view/")
public class View {

	private static final Logger log = LoggerFactory.getLogger(View.class);

	@Autowired
	private ViewQuery viewQuery;
	
	@Autowired
	ViewDefine viewDefine;
	
	String classLevelMapping = "/view/";
	
    @GetMapping("**")
    public ResponseEntity<ViewData<Map<String, Object>>> queryViewData(HttpServletRequest request,
                                                                       @RequestParam Map<String,Object> params,
                                                                       @RequestParam(required = false) String[] sort,
                                                                       @RequestParam(required = false) Integer page,
                                                                       @RequestParam(required = false) Integer size,
                                                                       @RequestParam(required = false) String filter,
                                                                       @RequestParam(required = false) String[] aggrs,
                                                                       @RequestParam(required = false) String lang,
                                                                       @RequestParam(required = false) String type,
                                                                       @RequestParam(required = false) Boolean translate)
        throws Exception {
        String viewId = request.getRequestURL().toString().split(classLevelMapping)[1].replace("/", ".");
        String reloadViewDefine = "reloadAllTheViewsDefineNow";
        if(viewId.endsWith(reloadViewDefine)) {
        	Date startTime = new Date();
        	String result = viewDefine.loadView();
        	ViewData<Map<String, Object>> data = new ViewData<>();
    		data.setStartTime(startTime);
    		data.setEndTime(new Date());
    		data.setMessage(result);
    		data.setName(reloadViewDefine);
        	return new ResponseEntity<>(data, HttpStatus.OK);
        }
        ViewStatement statement = null;
        if("table".equals(type)) {
            havePermissionTable(viewId,"query");
            statement = viewQuery.prepareTable(viewId,sort,page,size,filter,aggrs);
        }else {
            havePermission(viewId,"query");
            statement = viewQuery.prepareView(viewId,sort,page,size,filter,aggrs);
        }
        statement.setParams(params);
        RequestConfig viewReqConfig = new RequestConfig();
        viewReqConfig.setLanguage(lang);
        viewReqConfig.setTranslate(translate);
        Map<String, Object> queryParams = new HashMap<>();

        LoginUser loginUser = LoginHelper.getLoginUser();
        Long userId = loginUser.getUserId();
        String userName = loginUser.getUsername();
        Long deptId = loginUser.getDeptId();
        Long workshopId = loginUser.getWorkshopId();
        queryParams.put("currentUserId",userId);
        queryParams.put("currentUserName",userName);
        queryParams.put("currentDeptId",deptId);
        queryParams.put("currentWorkshopId",workshopId);
        
		queryParams.put("currentDomain", "domain1");
		queryParams.put("currentUser", "admin");
		
        viewReqConfig.setParams(queryParams);
        statement.setViewReqConfig(viewReqConfig);
        ViewData<Map<String, Object>> data = statement.query();
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
    
    private void havePermissionTable(String tableName,String action){
        log.info(String.format("find permission mapping for '%s' action '%s'",tableName,action));
    }		
    
    private void havePermission(String tableName,String action){
        log.info(String.format("find permission mapping for '%s' action '%s'",tableName,action));
    }		
	
}
