DROP DATABASE IF EXISTS view_demo;
CREATE DATABASE IF NOT EXISTS view_demo DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

create table view_demo.department(
    domain_key   VARCHAR(100) NOT NULL,
	dep_key      VARCHAR(100) NOT NULL,
	dep_name     VARCHAR(100) NOT NULL,
	member_count Integer NULL,
	stopped      BOOLEAN,
    create_time  DATETIME NOT NULL,
    create_user  VARCHAR(100) NOT NULL,
    update_time  DATETIME NOT NULL,
    update_user  VARCHAR(100) NOT NULL,
	PRIMARY KEY (domain_key,dep_key)
);

INSERT INTO view_demo.department (domain_key, dep_key, dep_name, member_count, stopped, create_time, create_user, update_time, update_user)
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



create table view_demo.position(
  domain_key   VARCHAR(100) NOT NULL,
  dep_key      VARCHAR(100) NOT NULL,
  pos_key      VARCHAR(100) NOT NULL,
  pos_name     VARCHAR(100) NOT NULL,
  create_time  DATETIME NOT NULL,
  create_user  VARCHAR(100) NOT NULL,
  update_time  DATETIME NOT NULL,
  update_user  VARCHAR(100) NOT NULL,
  PRIMARY KEY (domain_key,dep_key,pos_key)
);

INSERT INTO view_demo.position (domain_key, dep_key, pos_key, pos_name, create_time, create_user, update_time, update_user)
VALUES 
('domain1', 'dep03', 'pos1', '开发组长', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep03', 'pos2', '软件工程师', NOW(), 'admin', NOW(), 'admin');

INSERT INTO view_demo.position (domain_key, dep_key, pos_key, pos_name, create_time, create_user, update_time, update_user)
VALUES 
('domain1', 'dep02', 'pos1', '后勤组长', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'dep02', 'pos2', '后勤保障员', NOW(), 'admin', NOW(), 'admin');

create table view_demo.sys_user(
  domain_key    VARCHAR(100) NOT NULL,
  user_key      VARCHAR(100) NOT NULL,
  parent_user   VARCHAR(100) NULL,
  user_name     VARCHAR(100) NOT NULL,
  position_id   VARCHAR(100) NOT NULL,
  department_id VARCHAR(100) NOT NULL,
  status        VARCHAR(10)  NULL,
  photo         VARCHAR(100) NULL,
  remark        VARCHAR(1000)  NULL,
  create_time   DATETIME NOT NULL,
  create_user   VARCHAR(100) NOT NULL,
  update_time   DATETIME NOT NULL,
  update_user   VARCHAR(100) NOT NULL,
  PRIMARY KEY (user_key)
);

INSERT INTO view_demo.sys_user (domain_key, user_key, parent_user, user_name, department_id, position_id, status, remark, create_time, create_user, update_time, update_user)
VALUES 
('domain1', 'STF001',null, 'Horne', 'dep03', 'pos1', null,'', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'STF002','STF001', 'Sharp', 'dep03', 'pos2', 'C','', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'STF003','STF002', 'Johnston', 'dep03', 'pos2', null,'', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'STF004','STF003', 'Hahn', 'dep03', 'pos2', 'C','', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'STF005','STF001', 'Pickett', 'dep03', 'pos1', 'A','{{label.position}}:{{ department.field.positionId.enum {"value":"pos2","depKey":"dep03"} }}({{common.status{"value":"C"}}})', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'STF006','STF005', 'Dawson', 'dep03', 'pos2', 'C','参数化提示信息：{{message.demo{"text":"文本","number":"123"}}}', NOW(), 'admin', NOW(), 'admin'),
('domain1', 'STF007','STF006', 'Hahn', 'dep03', 'pos2', 'C','', NOW(), 'admin', NOW(), 'admin');



