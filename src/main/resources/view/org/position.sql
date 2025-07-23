/*
name: 职位
dataSource: dataSource
remark: |
  职位视图
param:
  depKey: string
fields:
  key: depKey,posKey
  cat:
    hidden: depKey
meta:
  group: security
  tables: view_demo.test_field
*/
select * from view_demo.position 
  where domain_key = :currentDomain 
    and create_user = :currentUser 
    and dep_key = :depKey