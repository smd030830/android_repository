package com.example.dogapp;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DiaryStore {
    private static final String PREF_NAME = "DiaryPrefs";

    private DiaryStore() {
    }

    public static void addDiary(Context context, DiaryEntry diaryEntry) {
        ArrayList<DiaryEntry> diaries = getDiaries(context, diaryEntry.getDogName());
        diaries.add(0, diaryEntry);
        saveDiaries(context, diaryEntry.getDogName(), diaries);
    }

    public static ArrayList<DiaryEntry> getDiaries(Context context, String dogName) {
        ArrayList<DiaryEntry> diaries = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String rawJson = prefs.getString(keyFor(dogName), "[]");

        try {
            JSONArray array = new JSONArray(rawJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                diaries.add(new DiaryEntry(
                        item.optInt("id"),
                        item.optString("fosterId"),
                        item.optString("dogName", dogName),
                        item.optString("dateText"),
                        item.optInt("foodAmount"),
                        item.optInt("poopCount"),
                        item.optString("content"),
                        item.optString("photoData", item.optString("photoUri"))));
            }
        } catch (JSONException ignored) {
            saveDiaries(context, dogName, diaries);
        }

        return diaries;
    }

    private static void saveDiaries(Context context, String dogName, ArrayList<DiaryEntry> diaries) {
        JSONArray array = new JSONArray();
        for (DiaryEntry diary : diaries) {
            JSONObject item = new JSONObject();
            try {
                item.put("id", diary.getId());
                item.put("fosterId", diary.getFosterId());
                item.put("dogName", diary.getDogName());
                item.put("dateText", diary.getDateText());
                item.put("foodAmount", diary.getFoodAmount());
                item.put("poopCount", diary.getPoopCount());
                item.put("content", diary.getContent());
                item.put("photoData", diary.getPhotoData());
                array.put(item);
            } catch (JSONException ignored) {
            }
        }

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(keyFor(dogName), array.toString())
                .apply();
    }

    private static String keyFor(String dogName) {
        return "diary_" + dogName;
    }
}
