package org.weaver.bean;

import java.util.Date;

public class DepartmentEntity {
	private String domainKey;
	private String depKey;
	private String depName;
	private Integer memberCount;
	private Boolean stopped;
	private Date createTime;
	private String createUser;
	private Date updateTime;
	private String updateUser;
	public String getDomainKey() {
		return domainKey;
	}
	public void setDomainKey(String domainKey) {
		this.domainKey = domainKey;
	}
	public String getDepKey() {
		return depKey;
	}
	public void setDepKey(String depKey) {
		this.depKey = depKey;
	}
	public String getDepName() {
		return depName;
	}
	public void setDepName(String depName) {
		this.depName = depName;
	}
	public Integer getMemberCount() {
		return memberCount;
	}
	public void setMemberCount(Integer memberCount) {
		this.memberCount = memberCount;
	}
	public Boolean getStopped() {
		return stopped;
	}
	public void setStopped(Boolean stopped) {
		this.stopped = stopped;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getCreateUser() {
		return createUser;
	}
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}


}
