package com.icuxika.model.map;

import com.google.gson.annotations.SerializedName;

public class Feature {
    @SerializedName("type")
    private String type;
    @SerializedName("properties")
    private Properties properties;
    @SerializedName("geometry")
    private Geometry geometry;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
}
