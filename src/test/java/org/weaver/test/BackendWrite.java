package org.weaver.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.weaver.bean.TestFieldEntity;
import org.weaver.query.entity.RequestConfig;
import org.weaver.service.TableService;
import org.weaver.service.ViewQuery;

@SpringBootTest
@DisplayName("BackendWrite")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackendWrite {
	
	private static final Logger log = LoggerFactory.getLogger(BackendWrite.class);
	
	@Autowired
	private TableService tableService;

	
	@Test
	@DisplayName("dataModify")
	@Order(1)
	public void dataModify() throws Exception {
		long id = insertTest();
		updateTest(id);
		deleteTest(id);
	}
	
	private long insertTest() throws Exception {
		TestFieldEntity testFieldEntity = new TestFieldEntity();
		testFieldEntity.setDeptId(3333);
		testFieldEntity.setUserId(3333);
		//testFieldEntity.setCreateTime(new Date());
		
		testFieldEntity.setCreateBy("Ryan");
		
		log.info(testFieldEntity.toString());
		RequestConfig requestConfig = new RequestConfig();
		requestConfig.getParams().put("createBy", "ryan");
		requestConfig.getParams().put("createTime", new Date());
		requestConfig.getParams().put("status", "0");
		requestConfig.getParams().put("delFlag", 0);
	    
		Integer affected = tableService.insertTable(null,"view_demo.test_field", testFieldEntity,requestConfig);
		log.info(testFieldEntity.toString());
		log.info("affected:"+affected);
		assertEquals(affected,1);
		return testFieldEntity.getId();
	}
	
	private void updateTest(long id) throws Exception {
		TestFieldEntity testFieldEntity = new TestFieldEntity();
		testFieldEntity.setId(id);
		testFieldEntity.setDeptId(55555);
		testFieldEntity.setUserId(55555);
		RequestConfig requestConfig = new RequestConfig();
		requestConfig.getParams().put("updateBy", "ryan");
		requestConfig.getParams().put("updateTime", new Date());
		Integer affected = tableService.updateTable(null,"view_demo.test_field", testFieldEntity,requestConfig);
		log.info(testFieldEntity.toString());
		log.info("affected:"+affected);
		assertEquals(affected,1);
	}	

	private void deleteTest(long id) throws Exception {
		TestFieldEntity testFieldEntity = new TestFieldEntity();
		testFieldEntity.setId(id);
		RequestConfig requestConfig = new RequestConfig();
		Integer affected = tableService.deleteTable(null,"view_demo.test_field", testFieldEntity,requestConfig);
		log.info(testFieldEntity.toString());
		log.info("affected:"+affected);
		assertEquals(affected,1);
	}	
}
