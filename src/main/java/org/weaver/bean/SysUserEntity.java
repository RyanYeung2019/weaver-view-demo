package org.weaver.bean;

import java.util.Date;

public class SysUserEntity {
	private String domainKey;
	private String userKey;
	private String parentUser;
	private String userName;
	private String positionId;
	private String departmentId;
	private String photo;
	private String remark;
	private String otherField;
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
	public String getUserKey() {
		return userKey;
	}
	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}
	public String getParentUser() {
		return parentUser;
	}
	public void setParentUser(String parentUser) {
		this.parentUser = parentUser;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPositionId() {
		return positionId;
	}
	public void setPositionId(String positionId) {
		this.positionId = positionId;
	}
	public String getDepartmentId() {
		return departmentId;
	}
	public void setDepartmentId(String departmentId) {
		this.departmentId = departmentId;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getOtherField() {
		return otherField;
	}
	public void setOtherField(String otherField) {
		this.otherField = otherField;
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
	
	@Override
	public String toString() {
		return "SysUserEntity [domainKey=" + domainKey + ", userKey=" + userKey + ", parentUser=" + parentUser
				+ ", userName=" + userName + ", positionId=" + positionId + ", departmentId=" + departmentId
				+ ", photo=" + photo + ", remark=" + remark + ", otherField=" + otherField + ", createTime="
				+ createTime + ", createUser=" + createUser + ", updateTime=" + updateTime + ", updateUser="
				+ updateUser + "]";
	}
	

}
