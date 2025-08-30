DROP TABLE IF EXISTS test_field;
CREATE TABLE test_field
(
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    dept_id              INTEGER                                                     NULL DEFAULT NULL,
    user_id              INTEGER                                                     NULL DEFAULT NULL,
    status               VARCHAR(100) NULL DEFAULT '0' ,
    del_flag             INTEGER                                                        NULL DEFAULT 0 ,
    create_time          timestamp                                                   NULL DEFAULT NULL ,
    create_by            VARCHAR(64)   NULL DEFAULT NULL,
    update_time          timestamp                                                   NULL DEFAULT NULL ,
    update_by            VARCHAR(64)   NULL DEFAULT NULL,
    remark               VARCHAR(500)  NULL DEFAULT NULL
);  


DROP TABLE IF EXISTS sys_dict_data;
create table sys_dict_data
(
    dict_code  SERIAL,
    dict_sort INTEGER default 0,
    dict_label  varchar(100),
    dict_value  varchar(100),
    dict_type   varchar(100),
    css_class   varchar(100),
    list_class  varchar(100),
    is_default  TEXT        ,
    status      TEXT        ,
    create_by   varchar(64) ,
    create_time timestamp,
    update_by   varchar(64) ,
    update_time timestamp,
    remark      varchar(500),
    constraint sys_dict_data_pk primary key (dict_code)
);



DROP TABLE IF EXISTS department;
create table department(
    domain_key   VARCHAR(100) NOT NULL,
	dep_key      VARCHAR(100) NOT NULL,
	dep_name     VARCHAR(100) NOT NULL,
	member_count INTEGER NULL,
	stopped      INTEGER,
    create_time  timestamp NOT NULL,
    create_user  VARCHAR(100) NOT NULL,
    update_time  timestamp NOT NULL,
    update_user  VARCHAR(100) NOT NULL,
	PRIMARY KEY (domain_key,dep_key)
);

INSERT INTO department (domain_key, dep_key, dep_name, member_count, stopped, create_time, create_user, update_time, update_user)
VALUES 
('domain1', 'dep01', '行政管理部',5, null, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep02', '业务拓展部',8, null, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep03', '技术支持部',6, false, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep04', '行政事务组',7, false, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep05', '后勤保障组',8, null, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep06', '国内业务组',2, false, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep07', '国际业务组',2, false, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep08', '软件开发组',5, null, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep09', '系统维护组',3, false, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep10', '文件管理小组',2, false, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep11', '会议安排小组',2, false, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep12', '东部区域小组',2, false, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep13', '西部区域小组',2, false, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep14', '前端开发小组',3, false, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep15', '后端开发小组',3, false, datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin');



DROP TABLE IF EXISTS position;
create table position(
  domain_key   VARCHAR(100) NOT NULL,
  dep_key      VARCHAR(100) NOT NULL,
  pos_key      VARCHAR(100) NOT NULL,
  pos_name     VARCHAR(100) NULL,
  create_time  timestamp NOT NULL,
  create_user  VARCHAR(100) NOT NULL,
  update_time  timestamp NOT NULL,
  update_user  VARCHAR(100) NOT NULL,
  PRIMARY KEY (domain_key,dep_key,pos_key)
);

INSERT INTO position (domain_key, dep_key, pos_key, pos_name, create_time, create_user, update_time, update_user)
VALUES 
('domain1', 'dep03', 'pos1', '开发组长', datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep03', 'pos2', '软件工程师', datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin');

INSERT INTO position (domain_key, dep_key, pos_key, pos_name, create_time, create_user, update_time, update_user)
VALUES 
('domain1', 'dep02', 'pos1', '后勤组长', datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'dep02', 'pos2', '后勤保障员', datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin');

DROP TABLE IF EXISTS sys_user;
create table sys_user(
  domain_key    VARCHAR(100) NOT NULL,
  user_key      VARCHAR(100) NOT NULL,
  parent_user   VARCHAR(100) NULL,
  user_name     VARCHAR(100) NOT NULL,
  position_id   VARCHAR(100) NOT NULL,
  department_id VARCHAR(100) NOT NULL,
  status        VARCHAR(10)  NULL,
  photo         VARCHAR(100) NULL,
  remark        VARCHAR(1000)  NULL,
  create_time   timestamp NOT NULL,
  create_user   VARCHAR(100) NOT NULL,
  update_time   timestamp NOT NULL,
  update_user   VARCHAR(100) NOT NULL,
  PRIMARY KEY (user_key)
);

INSERT INTO sys_user (domain_key, user_key, parent_user, user_name, department_id, position_id, status, remark, create_time, create_user, update_time, update_user)
VALUES 
('domain1', 'STF001',null, 'Horne', 'dep03', 'pos1', null,'', datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'STF002','STF001', 'Sharp', 'dep03', 'pos2', 'C','', datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'STF003','STF002', 'Johnston', 'dep03', 'pos2', null,'', datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'STF004','STF003', 'Hahn', 'dep03', 'pos2', 'C','', datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'STF005','STF001', 'Pickett', 'dep03', 'pos1', 'A','{{label.position}}:{{ department.field.positionId.enum {"value":"pos2","depKey":"dep03"} }}({{common.status{"value":"C"}}})', datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'STF006','STF005', 'Dawson', 'dep03', 'pos2', 'C','参数化提示信息：{{message.demo{"text":"文本","number":"123"}}}', datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin'),
('domain1', 'STF007','STF006', 'Hahn', 'dep03', 'pos2', 'C','', datetime('now', 'localtime'), 'admin', datetime('now', 'localtime'), 'admin');



