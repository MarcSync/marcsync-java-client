package MarcSync.classes;

import com.google.gson.Gson;

import java.util.HashMap;

public class EntryData extends HashMap<String, Object> {
    public static EntryData fromJson(String json) {
        return new Gson().fromJson(json, EntryData.class);
    }
}