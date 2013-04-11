package com.auguraclient.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class ProjectItem implements ProjectJSONParser {


    private String id;

    private String name;

    private String quantity;

    private String qcStatus;

    private String quantityChecked;

    private String qcComment;

    private Date dateDodified;

    private JSONObject jsonObject;

    private Project project;

    public ProjectItem() {

    }

    public ProjectItem(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }


    public void parser(JSONObject jsonObject) throws JSONParserException {
        try {
            this.id = jsonObject.getString("id");
            this.name =( (JSONObject)jsonObject.get("name")).getString("value");
            this.quantity =( (JSONObject)jsonObject.get("quantity")).getString("value");

            this.qcStatus =( (JSONObject)jsonObject.get("qc_status")).getString("value");

            this.quantityChecked =( (JSONObject)jsonObject.get("quantity_checked")).getString("value");

            String lastDate =( (JSONObject)jsonObject.get("date_modified")).getString("value");
            DateFormat da = DateFormat.getInstance();
            this.dateDodified  = da.parse(lastDate);


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





}
