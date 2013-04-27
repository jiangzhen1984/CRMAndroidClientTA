package com.auguraclient.model;


public class UpdateRecord extends AbstractModel {
	private Integer nID;

	private String type;

	private String proId;

	private String proOrderId;

	private String relatedId;

	private String flag;
	
	
	

	public UpdateRecord(Integer nID, String type, String proId,
			String proOrderId, String relatedId, String flag) {
		super();
		this.nID = nID;
		this.type = type;
		this.proId = proId;
		this.proOrderId = proOrderId;
		this.relatedId = relatedId;
		this.flag = flag;
	}
	
	
	

	public UpdateRecord(String type, String proId,
			String proOrderId, String relatedId, String flag) {
		super();
		this.type = type;
		this.proId = proId;
		this.proOrderId = proOrderId;
		this.relatedId = relatedId;
		this.flag = flag;
	}




	public Integer getnID() {
		return nID;
	}

	public void setnID(Integer nID) {
		this.nID = nID;
	}


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProId() {
		return proId;
	}

	public void setProId(String proId) {
		this.proId = proId;
	}

	public String getProOrderId() {
		return proOrderId;
	}

	public void setProOrderId(String proOrderId) {
		this.proOrderId = proOrderId;
	}

	public String getRelatedId() {
		return relatedId;
	}

	public void setRelatedId(String relatedId) {
		this.relatedId = relatedId;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

}
