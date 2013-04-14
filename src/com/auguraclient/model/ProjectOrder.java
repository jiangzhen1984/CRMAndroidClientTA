package com.auguraclient.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.auguraclient.util.Constants;
import com.auguraclient.util.GlobalHolder;
import com.auguraclient.util.Util;

public class ProjectOrder implements ProjectJSONParser {

    private Integer nID;


    private String id;

    private String name;

    private String quantity;

    private String qcStatus;

    private String quantityChecked;

    private String qcComment;

    private Date dateDodified;

    private JSONObject jsonObject;

    private Project project;

    private String photoName;

    private String photoPath;

    private String photoBigPath;

    private String originPhotoPath;

    private String description;

    private boolean isLoadedCheckpointFromDB;



    private List<ProjectCheckpoint> checkpointList;

    public ProjectOrder() {
        checkpointList =new ArrayList<ProjectCheckpoint>();
    }

    public ProjectOrder(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        checkpointList =new ArrayList<ProjectCheckpoint>();
    }


    public void parser(JSONObject jsonObject) throws JSONParserException {
        try {
            this.id = jsonObject.getString("id");

            JSONObject NameValue = (JSONObject) jsonObject.getJSONObject("name_value_list");
            this.name =((JSONObject) NameValue.get("name")).getString("value");
            this.quantity =( (JSONObject)NameValue.get("quantity")).getString("value");

            this.qcStatus =( (JSONObject)NameValue.get("qc_status")).getString("value");



            this.quantityChecked =( (JSONObject)NameValue.get("quantity_checked")).getString("value");

            String lastDate =( (JSONObject)NameValue.get("date_modified")).getString("value");
            if (lastDate !=null && !lastDate.isEmpty()) {
                DateFormat da = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                this.dateDodified  = da.parse(lastDate);
            }

            this.photoName = ( (JSONObject)NameValue.get("photo_c")).getString("value");
            this.photoPath = Constants.CommonConfig.PIC_DIR+"p_70_70_"+photoName ; //Constants.PHTOT_COMPRESSED_API_URL +photoName+"&h=70&w=70";
            this.photoBigPath = Constants.CommonConfig.PIC_DIR+"p_150_150_"+photoName ; // Constants.PHTOT_COMPRESSED_API_URL +photoName+"&h=150&w=150";

            this.originPhotoPath = Constants.CommonConfig.PIC_DIR+photoName ;
            Util.loadImageFromURL(new URL(Constants.PHTOT_API_URL+photoName), GlobalHolder.GLOBAL_STORAGE_PATH+this.originPhotoPath);

            Util.loadImageFromURL(new URL(Constants.PHTOT_COMPRESSED_API_URL +photoName+"&h=70&w=70"), GlobalHolder.GLOBAL_STORAGE_PATH+this.photoPath);
            Util.loadImageFromURL(new URL(Constants.PHTOT_COMPRESSED_API_URL +photoName+"&h=150&w=150"), GlobalHolder.GLOBAL_STORAGE_PATH+this.photoBigPath);


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

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getQcStatus() {
        return qcStatus;
    }

    public void setQcStatus(String qcStatus) {
        this.qcStatus = qcStatus;
    }

    public String getQuantityChecked() {
        return quantityChecked;
    }

    public void setQuantityChecked(String quantityChecked) {
        this.quantityChecked = quantityChecked;
    }

    public String getQcComment() {
        return qcComment;
    }

    public void setQcComment(String qcComment) {
        this.qcComment = qcComment;
    }

    public Date getDateDodified() {
        return dateDodified;
    }

    public void setDateDodified(Date dateDodified) {
        this.dateDodified = dateDodified;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
        this.originPhotoPath = Constants.CommonConfig.PIC_DIR+photoName;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }


    public void addOrderCheckpoint(ProjectCheckpoint po) {
        if(po != null) {
            this.checkpointList.add(po);
            po.setProjectItem(this);
        }
    }

    public void addOrderCheckpoint(List<ProjectCheckpoint> poList) {
        if (poList == null ) {
            return;
        }
        for(int i=0;i<poList.size(); i++) {
            ProjectCheckpoint pi = poList.get(i);
            this.checkpointList.add(pi);
            pi.setProjectItem(this);
        }
    }

    public ProjectCheckpoint getOrderCheckpointrByIndex(int pos) {
        if(pos <0 || pos >= checkpointList.size()) {
            return null;
        }
        return checkpointList.get(pos);
    }


    public int getCheckpointCount() {
        return checkpointList.size();
    }

    public Integer getnID() {
        return nID;
    }

    public void setnID(Integer nID) {
        this.nID = nID;
    }

    public String getPhotoBigPath() {
        return photoBigPath;
    }

    public void setPhotoBigPath(String photoBigPath) {
        this.photoBigPath = photoBigPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isLoadedCheckpointFromDB() {
        return isLoadedCheckpointFromDB;
    }

    public void setLoadedCheckpointFromDB(boolean isLoadedCheckpointFromDB) {
        this.isLoadedCheckpointFromDB = isLoadedCheckpointFromDB;
    }

    public String getOriginPhotoPath() {
        return originPhotoPath;
    }

    public void setOriginPhotoPath(String originPhotoPath) {
        this.originPhotoPath = originPhotoPath;
    }

       public void removeCheckpoint(int pos) {
           if(pos >=0 && pos <this.checkpointList.size())
           this.checkpointList.remove(pos);
       }


}
