package com.auguraclient.model;

import com.auguraclient.util.Constants;
import com.auguraclient.util.GlobalHolder;
import com.auguraclient.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProjectCheckpoint implements ProjectJSONParser, Serializable, Comparable<ProjectCheckpoint> {

	/**
     *
     */
	private static final long serialVersionUID = -4928950133984734211L;

	private Integer nID;

	private String id;

	private String name;

	private String category;

	private String checkType;

	private String description;

	private String qcStatus;

	private String numberDefect;

	private String qcAction;

	private String qcComments;

	private boolean executedDate;

	private String photoName;

	private String photoPath;

	private Date lastDate;

	private ProjectOrder projectItem;

	private String uploadPhotoAbsPath;

	public ProjectCheckpoint() {

	}

	public ProjectCheckpoint(JSONObject jsonObject) {

	}

	/*
	 * @see com.auguraclient.model.ProjectJSONParser#parser(org.json.JSONObject)
	 */
	public void parser(JSONObject jsonObject) throws JSONParserException {
		try {
			this.id = jsonObject.getString("id");
			JSONObject NameValue = (JSONObject) jsonObject
					.getJSONObject("name_value_list");

			this.name = ((JSONObject) NameValue.get("name")).getString("value");
			this.category = ((JSONObject) NameValue.get("category"))
					.getString("value");

			this.checkType = ((JSONObject) NameValue.get("checktype"))
					.getString("value");

			this.description = ((JSONObject) NameValue.get("description"))
					.getString("value");
			this.qcStatus = ((JSONObject) NameValue.get("qc_status"))
					.getString("value");

			this.executedDate = Boolean.parseBoolean(((JSONObject) NameValue
					.get("executed_date")).getString("value"));

			this.numberDefect = ((JSONObject) NameValue.get("number_defect"))
					.getString("value");

			this.qcComments = ((JSONObject) NameValue.get("qc_comment"))
					.getString("value");

			String lastDate = ((JSONObject) NameValue.get("date_modified"))
					.getString("value");

			if (lastDate != null && !lastDate.isEmpty()) {
				DateFormat da = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				this.lastDate = da.parse(lastDate);
			}

			this.photoName = ((JSONObject) NameValue.get("visual"))
					.getString("value");
			if (this.photoName != null && !this.photoName.isEmpty()) {
				this.photoPath = Constants.CommonConfig.PIC_DIR + photoName;
				Util.loadImageFromURL(new URL(Constants.PHTOT_API_URL
						+ photoName), GlobalHolder.GLOBAL_STORAGE_PATH
						+ this.photoPath);
			} else {
				this.photoPath = null;
			}

		} catch (JSONException e) {
			throw new JSONParserException(e);
		} catch (ParseException e) {
			throw new JSONParserException(e);
		} catch (MalformedURLException e) {
			throw new JSONParserException(e);
		} catch (IOException e) {
			throw new JSONParserException(e);
		}
	}

	public JSONArray toJSONArray() throws JSONParserException {
		return null;
	}

	public String getPhotoPath() {
		return photoPath;
	}

	public void setPhotoPath(String photoPath) {
		this.photoPath = photoPath;
	}

	public Date getLastDate() {
		return lastDate;
	}

	public void setLastDate(Date lastDate) {
		this.lastDate = lastDate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public String getCategoryLabel() {
		for (int i = 0; i < GlobalHolder.CATEGORY_ENUM_VALUE.length; i++) {
			if (this.category != null
					&& this.category
							.equals(GlobalHolder.CATEGORY_ENUM_VALUE[i])) {
				return GlobalHolder.CATEGORY_ENUM_VALUE[i];
			}
		}
		return null;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCheckType() {
		return checkType;
	}

	public String getCheckTypeLabel() {
		for (int i = 0; i < GlobalHolder.CHECK_TYPE_ENUM_VALUE.length; i++) {
			if (this.checkType != null
					&& this.checkType
							.equals(GlobalHolder.CHECK_TYPE_ENUM_VALUE[i])) {
				return GlobalHolder.CHECK_TYPE_ENUM_VALUE[i];
			}
		}
		return null;
	}

	public void setCheckType(String checkType) {
		this.checkType = checkType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getQcStatus() {
		return qcStatus;
	}

	public void setQcStatus(String qcStatus) {
		this.qcStatus = qcStatus;
	}

	public String getNumberDefect() {
		return numberDefect;
	}

	public void setNumberDefect(String numberDefect) {
		this.numberDefect = numberDefect;
	}

	public String getQcAction() {
		return qcAction;
	}

	public void setQcAction(String qcAction) {
		this.qcAction = qcAction;
	}

	public String getQcComments() {
		return qcComments;
	}

	public void setQcComments(String qcComments) {
		this.qcComments = qcComments;
	}

	public boolean isExecutedDate() {
		return executedDate;
	}

	public void setExecutedDate(boolean executedDate) {
		this.executedDate = executedDate;
	}

	public String getPhotoName() {
		return photoName;
	}

	public void setPhotoName(String photoName) {
		this.photoName = photoName;
	}

	public ProjectOrder getProjectItem() {
		return projectItem;
	}

	public void setProjectItem(ProjectOrder projectItem) {
		this.projectItem = projectItem;
	}

	public Integer getnID() {
		return nID;
	}

	public void setnID(Integer nID) {
		this.nID = nID;
	}

	public String getUploadPhotoAbsPath() {
		return uploadPhotoAbsPath;
	}

	public void setUploadPhotoAbsPath(String uploadPhotoAbsPath) {
		this.uploadPhotoAbsPath = uploadPhotoAbsPath;
	}

	public boolean isCompleted() {
		if (this.qcStatus == null || this.qcStatus.equals("")) {
			return false;
		}

		return true;
	}

	@Override
	public int compareTo(ProjectCheckpoint pc) {
		if(pc == null || pc.getCategory() == null || pc.getCategory().equals("")) {
			return 1;
		} else if(this.category == null || this.category.equals("")) {
			return -1;
		} else {
			int ret = compare(this.category.toCharArray(), pc.getCategory().toCharArray());
			if( ret== 0)  {
				if(pc.getName() == null || pc.getName().equals("")) {
					return 1;
				} else if(this.name == null || this.name.equals("")) {
					return -1;
				} else {
					return compare(this.name.toCharArray(), pc.getName().toCharArray());
				}
				
			} else {
				return ret;
			}
		}
		
	}
	
	
	private int compare(char[] oldC, char[] newC) {
		int i= 0;
		int j=0;
		for(; i<newC.length && j <oldC.length; i++, j++) {
			if(newC[i] == oldC[j]) {
				continue;
			} else if(newC[i] < oldC[j]) {
				return 1;
			} else {
				return -1;
			}
		}
		
		if(i == newC.length) {
			return 1;
		} else if(j == oldC.length) {
			return -1;
		}
		
		return 0;
	}
	
	
	

}
