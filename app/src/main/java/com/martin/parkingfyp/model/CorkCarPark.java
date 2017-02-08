package com.martin.parkingfyp.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Martin on 30/01/2017.
 */

public class CorkCarPark {

    /*@SerializedName("help")
    @Expose
    private String help;

    @SerializedName("success")
    @Expose
    private boolean success;
    */
    @SerializedName("result")
    @Expose
    public Result result;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public ArrayList<Result.Records> getRecords(){
        return result.getRecords();
    }

    public class Result{

        /*@SerializedName("sort")
        @Expose
        private String sort;

        @SerializedName("resource_id")
        @Expose
        private String resource_id;

        @SerializedName("fields")
        @Expose
        private ArrayList<Fields> fields;
        */
        public ArrayList<Records> records;

        public ArrayList<Records> getRecords() {
            return records;
        }

        public void setRecords(ArrayList<Records> records) {
            this.records = records;
        }
        /*private class Fields{
            @SerializedName("type")
            @Expose
            private String type;

            @SerializedName("id")
            @Expose
            private String id;
        }*/

        public class Records{
            @SerializedName("name")
            @Expose
            private String name;

            @SerializedName("free_spaces")
            @Expose
            private int free_spaces;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public int getFree_spaces() {
                return free_spaces;
            }

            public void setFree_spaces(int free_spaces) {
                this.free_spaces = free_spaces;
            }
        }
    }
}

/*
    /*private int _id;
    private int identifier;*/
    //@SerializedName("nae")
   // @Expose
   // private String nae;
    //private int spaces;
    /*
    @SerializedName("free_spaces")
    @Expose
    private int free_spaces;

    public CorkCarPark(String name, int free_spaces) {
        this.name = name;
        this.free_spaces = free_spaces;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFree_spaces() {
        return free_spaces;
    }

    public void setFree_spaces(int free_spaces) {
        this.free_spaces = free_spaces;
    }

    /*private String opening_times;
    private String notes;
    private float latitude;
    private float longitude;



    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSpaces() {
        return spaces;
    }

    public void setSpaces(int spaces) {
        this.spaces = spaces;
    }

    public int getFree_spaces() {
        return free_spaces;
    }

    public void setFree_spaces(int free_spaces) {
        this.free_spaces = free_spaces;
    }

    public String getOpening_times() {
        return opening_times;
    }

    public void setOpening_times(String opening_times) {
        this.opening_times = opening_times;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String date;

    public CorkCarPark(int _id, int identifier, String name, int spaces, int free_spaces, String
            opening_times, String notes, float latitude, float longitude, String date){
        this._id = _id;
        this.identifier = identifier;
        this.name = name;
        this.spaces = spaces;
        this.free_spaces = free_spaces;
        this.opening_times = opening_times;
        this.notes = notes;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }*/
//}
