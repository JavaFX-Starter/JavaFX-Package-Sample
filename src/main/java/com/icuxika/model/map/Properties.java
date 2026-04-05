package com.icuxika.model.map;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Properties {
    @SerializedName("adcode")
    private String adCode;
    @SerializedName("name")
    private String name;
    @SerializedName("center")
    private List<Double> center;
    @SerializedName("centroid")
    private List<Double> centroid;
    @SerializedName("childrenNum")
    private long childrenNum;
    @SerializedName("level")
    private String level;
    @SerializedName("parent")
    private Parent parent;

    public String getAdCode() {
        return adCode;
    }

    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Double> getCenter() {
        return center;
    }

    public void setCenter(List<Double> center) {
        this.center = center;
    }

    public List<Double> getCentroid() {
        return centroid;
    }

    public void setCentroid(List<Double> centroid) {
        this.centroid = centroid;
    }

    public long getChildrenNum() {
        return childrenNum;
    }

    public void setChildrenNum(long childrenNum) {
        this.childrenNum = childrenNum;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }
}
