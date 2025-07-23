package org.weaver.bean;

import java.util.Date;

public class TestFieldEntity {
	private Long id;
	private Integer deptId;
	private Integer userId;
	private Date createTime;
	private String createBy;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Integer getDeptId() {
		return deptId;
	}
	public void setDeptId(Integer deptId) {
		this.deptId = deptId;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	@Override
	public String toString() {
		return "TestFieldEntity [id=" + id + ", deptId=" + deptId + ", userId=" + userId + ", createTime=" + createTime
				+ ", createBy=" + createBy + "]";
	}

	
}
