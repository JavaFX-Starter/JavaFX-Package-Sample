package com.icuxika.model.map;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GeometryDeserializer implements JsonDeserializer<Geometry> {
    private final Gson gson = new Gson();

    @Override
    public Geometry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        JsonElement coordinates = jsonObject.get("coordinates");

        Geometry geometry = new Geometry();

        if ("MultiPolygon".equals(type)) {
            geometry.setCoordinates(gson.fromJson(coordinates, new TypeToken<>() {
            }.getType()));
        } else if ("Polygon".equals(type)) {
            List<List<List<List<Double>>>> wrapper = new ArrayList<>();
            wrapper.add(gson.fromJson(coordinates, new TypeToken<>() {
            }.getType()));
            geometry.setCoordinates(wrapper);
        }

        return geometry;
    }
}
