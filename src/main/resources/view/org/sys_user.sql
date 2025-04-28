/*
name: 用户
dataSource: dataSource
remark: |
  用户视图
param:
  departmentId: string
fields:
  enum:
    departmentId:
      viewId: org.department
      value: depKey
      text: depName
    positionId: department.field.positionId.enum
    status: common.status
  key: domainKey,userKey
  hidden: domainKey,userKey
  file: photo
tree:
  id: userKey
  parent: parentUser
  search: userKey,userName
meta:
  group: security
  tables: view_demo.sys_user
  setting: for Backend
props:
  setting: for Backend and Frontend
*/
select * from view_demo.sys_user
  where domain_key = :currentDomain
  and department_id = :departmentId