/*
name: 部门
dataSource: dataSource
remark: |
  部门视图
fields:
  enum:
    stopped: department.field.stopped.enum
meta:
  group: system
  tables: view_demo.department
*/
select * from view_demo.department 
  where domain_key = :currentDomain 
    and create_user = :currentUser 
    and (stopped = false or stopped is null)