package com.sakshi.android.redial.util;

/**
 * Created by sakshiagarwal on 27/10/16.
 */

public class AppUtils {

    private static AppUtils instance;
    public static int NUMBER_OF_CALLS = 0;

    public static AppUtils getInstance(){
        if(instance == null){
            instance = new AppUtils();
        }
        return instance;
    }
}
