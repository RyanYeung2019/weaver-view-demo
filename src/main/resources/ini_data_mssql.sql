-- Target database view_demo. Schema view_demo mirrors other databases' naming (view_demo.table).

IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = N'view_demo')
    EXEC(N'CREATE SCHEMA view_demo');

DROP TABLE IF EXISTS view_demo.sys_user;
DROP TABLE IF EXISTS view_demo.position;
DROP TABLE IF EXISTS view_demo.department;
DROP TABLE IF EXISTS view_demo.sys_dict_data;
DROP TABLE IF EXISTS view_demo.test_field;

CREATE TABLE view_demo.test_field
(
    id                   BIGINT IDENTITY(1,1) NOT NULL,
    dept_id              BIGINT            NULL,
    user_id              BIGINT            NULL,
    status               NVARCHAR(100)     NULL DEFAULT N'0',
    del_flag             INT               NULL DEFAULT 0,
    create_time          DATETIME2(0)      NULL,
    create_by            NVARCHAR(64)      NULL,
    update_time          DATETIME2(0)      NULL,
    update_by            NVARCHAR(64)      NULL,
    remark               NVARCHAR(500)     NULL
);
ALTER TABLE view_demo.test_field ADD CONSTRAINT PK_t PRIMARY KEY (id);

CREATE TABLE view_demo.sys_dict_data
(
    dict_code   BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    dict_sort   INT            NOT NULL DEFAULT 0,
    dict_label  NVARCHAR(100)  NOT NULL DEFAULT N'',
    dict_value  NVARCHAR(100)  NOT NULL DEFAULT N'',
    dict_type   NVARCHAR(100)  NOT NULL DEFAULT N'',
    css_class   NVARCHAR(100)  NULL,
    list_class  NVARCHAR(100)  NULL,
    is_default  NCHAR(1)       NOT NULL DEFAULT N'N',
    status      NCHAR(1)       NOT NULL DEFAULT N'0',
    create_by   NVARCHAR(64)   NOT NULL DEFAULT N'',
    create_time DATETIME2(0)   NULL,
    update_by   NVARCHAR(64)   NOT NULL DEFAULT N'',
    update_time DATETIME2(0)   NULL,
    remark      NVARCHAR(500)  NULL
);

CREATE TABLE view_demo.department
(
    domain_key   NVARCHAR(100) NOT NULL,
    dep_key      NVARCHAR(100) NOT NULL,
    dep_name     NVARCHAR(100) NOT NULL,
    member_count INT            NULL,
    stopped      BIT            NULL,
    create_time  DATETIME2(0)   NOT NULL,
    create_user  NVARCHAR(100) NOT NULL,
    update_time  DATETIME2(0)   NOT NULL,
    update_user  NVARCHAR(100) NOT NULL,
    PRIMARY KEY (domain_key, dep_key)
);

INSERT INTO view_demo.department (domain_key, dep_key, dep_name, member_count, stopped, create_time, create_user, update_time, update_user)
VALUES
(N'domain1', N'dep01', N'行政管理部', 5, NULL, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep02', N'业务拓展部', 8, NULL, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep03', N'技术支持部', 6, 0, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep04', N'行政事务组', 7, 0, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep05', N'后勤保障组', 8, NULL, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep06', N'国内业务组', 2, 0, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep07', N'国际业务组', 2, 0, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep08', N'软件开发组', 5, NULL, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep09', N'系统维护组', 3, 0, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep10', N'文件管理小组', 2, 0, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep11', N'会议安排小组', 2, 0, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep12', N'东部区域小组', 2, 0, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep13', N'西部区域小组', 2, 0, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep14', N'前端开发小组', 3, 0, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep15', N'后端开发小组', 3, 0, SYSDATETIME(), N'admin', SYSDATETIME(), N'admin');

CREATE TABLE view_demo.position
(
    domain_key   NVARCHAR(100) NOT NULL,
    dep_key      NVARCHAR(100) NOT NULL,
    pos_key      NVARCHAR(100) NOT NULL,
    pos_name     NVARCHAR(100) NULL,
    create_time  DATETIME2(0)  NOT NULL,
    create_user  NVARCHAR(100) NOT NULL,
    update_time  DATETIME2(0)  NOT NULL,
    update_user  NVARCHAR(100) NOT NULL,
    PRIMARY KEY (domain_key, dep_key, pos_key)
);

INSERT INTO view_demo.position (domain_key, dep_key, pos_key, pos_name, create_time, create_user, update_time, update_user)
VALUES
(N'domain1', N'dep03', N'pos1', N'开发组长', SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep03', N'pos2', N'软件工程师', SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep02', N'pos1', N'后勤组长', SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'dep02', N'pos2', N'后勤保障员', SYSDATETIME(), N'admin', SYSDATETIME(), N'admin');

CREATE TABLE view_demo.sys_user
(
    domain_key    NVARCHAR(100) NOT NULL,
    user_key      NVARCHAR(100) NOT NULL,
    parent_user   NVARCHAR(100) NULL,
    user_name     NVARCHAR(100) NOT NULL,
    position_id   NVARCHAR(100) NOT NULL,
    department_id NVARCHAR(100) NOT NULL,
    status        NVARCHAR(10)  NULL,
    photo         NVARCHAR(100) NULL,
    remark        NVARCHAR(1000) NULL,
    create_time   DATETIME2(0)  NOT NULL,
    create_user   NVARCHAR(100) NOT NULL,
    update_time   DATETIME2(0)  NOT NULL,
    update_user   NVARCHAR(100) NOT NULL,
    PRIMARY KEY (user_key)
);

INSERT INTO view_demo.sys_user (domain_key, user_key, parent_user, user_name, department_id, position_id, status, remark, create_time, create_user, update_time, update_user)
VALUES
(N'domain1', N'STF001', NULL, N'Horne', N'dep03', N'pos1', NULL, N'', SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'STF002', N'STF001', N'Sharp', N'dep03', N'pos2', N'C', N'', SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'STF003', N'STF002', N'Johnston', N'dep03', N'pos2', NULL, N'', SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'STF004', N'STF003', N'Hahn', N'dep03', N'pos2', N'C', N'', SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'STF005', N'STF001', N'Pickett', N'dep03', N'pos1', N'A', N'{{label.position}}:{{ department.field.positionId.enum {"value":"pos2","depKey":"dep03"} }}({{common.status{"value":"C"}}})', SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'STF006', N'STF005', N'Dawson', N'dep03', N'pos2', N'C', N'参数化提示信息：{{message.demo{"text":"文本","number":"123"}}}', SYSDATETIME(), N'admin', SYSDATETIME(), N'admin'),
(N'domain1', N'STF007', N'STF006', N'Hahn', N'dep03', N'pos2', N'C', N'', SYSDATETIME(), N'admin', SYSDATETIME(), N'admin');
