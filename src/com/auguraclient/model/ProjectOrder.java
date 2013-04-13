package com.auguraclient.model;

import com.auguraclient.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private String description;

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


    private List<ProjectCheckpoint> orderList;

    public ProjectOrder() {
        orderList =new ArrayList<ProjectCheckpoint>();
    }

    public ProjectOrder(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        orderList =new ArrayList<ProjectCheckpoint>();
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
            this.photoPath = Constants.PHTOT_COMPRESSED_API_URL +photoName+"&h=70&w=70";
            this.photoBigPath = Constants.PHTOT_COMPRESSED_API_URL +photoName+"&h=150&w=150";


        } catch (JSONException e) {
            throw new JSONParserException(e);
        } catch (ParseException e) {
            e.printStackTrace();
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
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }


    public void addItemOrder(ProjectCheckpoint po) {
        this.orderList.add(po);
    }

    public void addItemOrder(List<ProjectCheckpoint> poList) {
        if (poList == null ) {
            return;
        }
        for(int i=0;i<poList.size(); i++) {
            ProjectCheckpoint pi = poList.get(i);
            this.orderList.add(pi);
            pi.setProjectItem(this);
        }
    }

    public ProjectCheckpoint getItemOrderByIndex(int pos) {
        if(pos <0 || pos >= orderList.size()) {
            return null;
        }
        return orderList.get(pos);
    }


    public int getItemOrderCount() {
        return orderList.size();
    }

    public Integer getnID() {
        return nID;
    }

    public void setnID(Integer nID) {
        this.nID = nID;
    }




}
