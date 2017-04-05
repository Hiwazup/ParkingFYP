package com.martin.parkingfyp.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CorkCarPark {
    @SerializedName("result")
    @Expose
    public Result result;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public ArrayList<Result.Records> getRecords() {
        return result.getRecords();
    }

    public class Result {
        public ArrayList<Records> records;

        public ArrayList<Records> getRecords() {
            return records;
        }

        public void setRecords(ArrayList<Records> records) {
            this.records = records;
        }

        public class Records {
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
