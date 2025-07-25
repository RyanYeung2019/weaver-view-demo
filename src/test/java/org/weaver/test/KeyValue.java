package org.weaver.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.weaver.view.query.ViewQuery;
import org.weaver.view.query.entity.KeyValueSettingEn;

@SpringBootTest
@DisplayName("KeyValue")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KeyValue {
	
	private static final Logger log = LoggerFactory.getLogger(KeyValue.class);

	@Autowired
	private ViewQuery viewQuery;

	@Test
	@DisplayName("Base Usage")
	@Order(1)
	public void baseUsage() throws Exception {
		KeyValueSettingEn keyValSetting = new KeyValueSettingEn(
		        "view_demo.sys_dict_data",
		        "dict_value",
		        "dict_label",
		        new LinkedHashMap<String, Object>(){
					private static final long serialVersionUID = 1L;
				{put("dict_type", "my_defaul_type_base");}}
		       );
		
		log.info(viewQuery.getValue(keyValSetting, "key"));
		assertEquals(viewQuery.getValue(keyValSetting, "key"),null);
		
		viewQuery.setValue(keyValSetting, "key","value");
		log.info(viewQuery.getValue(keyValSetting, "key"));
		assertEquals(viewQuery.getValue(keyValSetting, "key"),"value");
		
		viewQuery.setValue(keyValSetting, "key","value1");
		log.info(viewQuery.getValue(keyValSetting, "key"));
		assertEquals(viewQuery.getValue(keyValSetting, "key"),"value1");
	}
	
	@Test
	@DisplayName("extend Usage")
	@Order(2)
	public void extendUsage() throws Exception {
		KeyValueSettingEn keyValSetting = new KeyValueSettingEn(
		        "view_demo.sys_dict_data",
		        "dict_value",
		        "dict_label",
		        "create_time",
		        "update_time",
		        "create_by",
		        "update_by",
		        new LinkedHashMap<String, Object>(){
					private static final long serialVersionUID = 1L;
				{put("dict_type", "my_defaul_type_extend");}}
		       );
		
		LinkedHashMap<String, Object> myData1 = new LinkedHashMap<String, Object>(){
			private static final long serialVersionUID = 1L;
		{
			put("dict_label", "my1 label");
			put("css_class", "my1 css class");
			put("list_class", "my1 class");
			put("is_default", "N");
			put("status", "0");
			put("remark", "my1 Remark");
			put("dict_sort", 0);
		}};
		
		LinkedHashMap<String, Object> myData2 = new LinkedHashMap<String, Object>(){
			private static final long serialVersionUID = 1L;
		{
			put("dict_label", "my2 label");
			put("css_class", "my2 css class");
			put("list_class", "my2 class");
			put("is_default", "Y");
			put("status", "2");
			put("remark", "my2 Remark");
			put("dict_sort", 1);
		}};
		
		
		assertEquals(viewQuery.getData(keyValSetting, "key"),null);
		
		viewQuery.setData(keyValSetting, "key",myData1,"myUserId1");
		log.info(viewQuery.getData(keyValSetting, "key").toString());
		assertEquals(viewQuery.getData(keyValSetting, "key").get("status"),"0");
		
		viewQuery.setData(keyValSetting, "key",myData2,"myUserId2");
		log.info(viewQuery.getData(keyValSetting, "key").toString());
		assertEquals(viewQuery.getData(keyValSetting, "key").get("status"),"2");
	}
}
