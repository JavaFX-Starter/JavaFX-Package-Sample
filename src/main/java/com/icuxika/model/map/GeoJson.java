package com.icuxika.model.map;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeoJson {
    @SerializedName("type")
    private String type;
    @SerializedName("features")
    private List<Feature> features;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}
