
DROP TABLE IF EXISTS test_field;
CREATE TABLE test_field
(
    id                                                                         SERIAL  ,
    dept_id              bigint                                                     NULL DEFAULT NULL,
    user_id              bigint                                                     NULL DEFAULT NULL,
    status               character varying(100) NULL DEFAULT '0' ,
    del_flag             int                                                        NULL DEFAULT 0 ,
    create_time          timestamp without time zone                                                   NULL DEFAULT NULL ,
    create_by            character varying(64)   NULL DEFAULT NULL,
    update_time          timestamp without time zone                                                   NULL DEFAULT NULL ,
    update_by            character varying(64)   NULL DEFAULT NULL,
    remark               character varying(500)  NULL DEFAULT NULL,
    PRIMARY KEY (id)
);  

COMMENT ON TABLE test_field IS  '测试表'; 
COMMENT ON COLUMN test_field.id IS '主键';
COMMENT ON COLUMN test_field.dept_id IS '部门id';
COMMENT ON COLUMN test_field.user_id IS '用户id';
COMMENT ON COLUMN test_field.status IS '状态（0正常 1停用）';
COMMENT ON COLUMN test_field.del_flag IS '删除标志';
COMMENT ON COLUMN test_field.create_time IS '创建时间';
COMMENT ON COLUMN test_field.create_by IS '创建人';
COMMENT ON COLUMN test_field.update_time IS '更新时间';
COMMENT ON COLUMN test_field.update_by IS '更新人';
COMMENT ON COLUMN test_field.remark IS '备注';

DROP TABLE IF EXISTS sys_dict_data;
create table sys_dict_data
(
    dict_code  SERIAL,
    dict_sort int4 default 0,
    dict_label  varchar(100) default '':: varchar,
    dict_value  varchar(100) default '':: varchar,
    dict_type   varchar(100) default '':: varchar,
    css_class   varchar(100) default null:: varchar,
    list_class  varchar(100) default null:: varchar,
    is_default  char         default 'N'::bpchar,
    status      char         default '0'::bpchar,
    create_by   varchar(64)  default '':: varchar,
    create_time timestamp,
    update_by   varchar(64)  default '':: varchar,
    update_time timestamp,
    remark      varchar(500) default null:: varchar,
    constraint sys_dict_data_pk primary key (dict_code)
);

DROP TABLE IF EXISTS department;
create table department(
    domain_key   character varying(100) NOT NULL,
	dep_key      character varying(100) NOT NULL,
	dep_name     character varying(100) NOT NULL,
	member_count int NULL,
	stopped      BOOLEAN,
    create_time  timestamp without time zone NOT NULL,
    create_user  character varying(100) NOT NULL,
    update_time  timestamp without time zone NOT NULL,
    update_user  character varying(100) NOT NULL,
	PRIMARY KEY (domain_key,dep_key)
);

INSERT INTO department (domain_key, dep_key, dep_name, member_count, stopped, create_time, create_user, update_time, update_user)
VALUES 
('domain1', 'dep01', '行政管理部',5, null, NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep02', '业务拓展部',8, null, NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep03', '技术支持部',6, false, NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep04', '行政事务组',7, false, NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep05', '后勤保障组',8, null, NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep06', '国内业务组',2, false, NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep07', '国际业务组',2, false, NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep08', '软件开发组',5, null, NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep09', '系统维护组',3, false, NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep10', '文件管理小组',2, false, NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep11', '会议安排小组',2, false, NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep12', '东部区域小组',2, false, NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep13', '西部区域小组',2, false, NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep14', '前端开发小组',3, false, NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep15', '后端开发小组',3, false, NOW(), 'admin', NOW(), 'admin');

DROP TABLE IF EXISTS position;
create table position(
  domain_key   character varying(100) NOT NULL,
  dep_key      character varying(100) NOT NULL,
  pos_key      character varying(100) NOT NULL,
  pos_name     character varying(100) NULL,
  create_time  timestamp without time zone NOT NULL,
  create_user  character varying(100) NOT NULL,
  update_time  timestamp without time zone NOT NULL,
  update_user  character varying(100) NOT NULL,
  PRIMARY KEY (domain_key,dep_key,pos_key)
);

INSERT INTO position (domain_key, dep_key, pos_key, pos_name, create_time, create_user, update_time, update_user)
VALUES 
('domain1', 'dep03', 'pos1', '开发组长', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep03', 'pos2', '软件工程师', NOW(), 'admin', NOW(), 'admin');

INSERT INTO position (domain_key, dep_key, pos_key, pos_name, create_time, create_user, update_time, update_user)
VALUES 
('domain1', 'dep02', 'pos1', '后勤组长', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep02', 'pos2', '后勤保障员', NOW(), 'admin', NOW(), 'admin');

DROP TABLE IF EXISTS sys_user;
create table sys_user(
  domain_key    character varying(100) NOT NULL,
  user_key      character varying(100) NOT NULL,
  parent_user   character varying(100) NULL,
  user_name     character varying(100) NOT NULL,
  position_id   character varying(100) NOT NULL,
  department_id character varying(100) NOT NULL,
  status        character varying(10)  NULL,
  photo         character varying(100) NULL,
  remark        character varying(1000)  NULL,
  create_time   timestamp without time zone NOT NULL,
  create_user   character varying(100) NOT NULL,
  update_time   timestamp without time zone NOT NULL,
  update_user   character varying(100) NOT NULL,
  PRIMARY KEY (user_key)
);

INSERT INTO sys_user (domain_key, user_key, parent_user, user_name, department_id, position_id, status, remark, create_time, create_user, update_time, update_user)
VALUES 
('domain1', 'STF001',null, 'Horne', 'dep03', 'pos1', null,'', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'STF002','STF001', 'Sharp', 'dep03', 'pos2', 'C','', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'STF003','STF002', 'Johnston', 'dep03', 'pos2', null,'', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'STF004','STF003', 'Hahn', 'dep03', 'pos2', 'C','', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'STF005','STF001', 'Pickett', 'dep03', 'pos1', 'A','{{label.position}}:{{ department.field.positionId.enum {"value":"pos2","depKey":"dep03"} }}({{common.status{"value":"C"}}})', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'STF006','STF005', 'Dawson', 'dep03', 'pos2', 'C','参数化提示信息：{{message.demo{"text":"文本","number":"123"}}}', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'STF007','STF006', 'Hahn', 'dep03', 'pos2', 'C','', NOW(), 'admin', NOW(), 'admin');



