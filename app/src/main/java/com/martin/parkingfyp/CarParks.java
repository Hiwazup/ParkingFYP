package com.martin.parkingfyp;

import android.content.res.Resources;
import android.util.Log;

import com.martin.parkingfyp.model.CorkCarPark;

import java.util.ArrayList;

/**
 * Created by Martin on 18/01/2017.
 */

public class CarParks {
    private String title;
    private int spaces;

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    private boolean open;


    public CarParks(String title, int spaces, boolean open) {
        this.title = title;
        this.spaces = spaces;
        this.open = open;
    }

    public int getSpaces() {
        return spaces;
    }

    public void setSpaces(int spaces) {
        this.spaces = spaces;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static ArrayList<CarParks> createList(Resources res){
        ArrayList<CarParks> list = new ArrayList<>();
        boolean open;
        String [] carParkNames =  res.getStringArray(R.array.cork_car_parks_names);
        int [] carParkSpaces = res.getIntArray(R.array.cork_car_parks_spaces);
        for(int i = 0; i < carParkNames.length; i++){
            open = false;
            if ((int)(Math.random()*2) % 2 == 0) { open = true; }
            list.add(new CarParks(carParkNames[i], carParkSpaces[i], open));
        }
        return list;
    }
}
