package com.icuxika.model.map;

import com.google.gson.annotations.SerializedName;

public class Parent {
    @SerializedName("adcode")
    private Object adCode;

    public Object getAdCode() {
        return adCode;
    }

    public void setAdCode(Object adCode) {
        this.adCode = adCode;
    }
}
