package org.weaver.test;

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
import org.weaver.view.query.ViewQuery;
import org.weaver.view.query.entity.RequestConfig;

@SpringBootTest
@DisplayName("BackendWrite")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackendWrite {
	
	private static final Logger log = LoggerFactory.getLogger(BackendWrite.class);
	
	@Autowired
	private ViewQuery viewQuery;

	@Test
	@DisplayName("Insert")
	@Order(1)
	public void insertTest() throws Exception {
		TestFieldEntity testFieldEntity = new TestFieldEntity();
		testFieldEntity.setDeptId(3333);
		testFieldEntity.setUserId(3333);
		testFieldEntity.setCreateTime(new Date());
		
		testFieldEntity.setCreateBy("Ryan");
		
		log.info(testFieldEntity.toString());
		RequestConfig requestConfig = new RequestConfig();
		requestConfig.getParams().put("createBy", "ryan");
		Integer affected = viewQuery.insertViewTable("org.test_field", testFieldEntity,requestConfig);
		log.info(testFieldEntity.toString());
		log.info("affected:"+affected);
	}
	
	@Test
	@DisplayName("Update")
	@Order(2)
	public void updateTest() throws Exception {
		TestFieldEntity testFieldEntity = new TestFieldEntity();
		testFieldEntity.setId(1l);
		testFieldEntity.setDeptId(55555);
		testFieldEntity.setUserId(55555);
		RequestConfig requestConfig = new RequestConfig();
		Integer affected = viewQuery.updateViewTable("org.test_field", testFieldEntity,requestConfig);
		log.info(testFieldEntity.toString());
		log.info("affected:"+affected);
	}	
	
	
	@Test
	@DisplayName("Delete")
	@Order(3)
	public void deleteTest() throws Exception {
		TestFieldEntity testFieldEntity = new TestFieldEntity();
		testFieldEntity.setId(1l);
		RequestConfig requestConfig = new RequestConfig();
		Integer affected = viewQuery.deleteViewTable("org.test_field", testFieldEntity,requestConfig);
		log.info(testFieldEntity.toString());
		log.info("affected:"+affected);
	}	
}
