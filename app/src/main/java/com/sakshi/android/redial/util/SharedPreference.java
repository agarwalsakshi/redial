package com.sakshi.android.redial.util;

import android.content.Context;

/**
 * Created by sakshiagarwal on 09/11/16.
 */

public class SharedPreference {

    public void saveRemainingCallNumber(Context context, int remaining_number){
        context.getSharedPreferences("REMAINING_CALL", Context.MODE_PRIVATE).edit().putInt("NUMBER", remaining_number).apply();
    }

    public int fetchRemainingCallNumber(Context context){
        return context.getSharedPreferences("REMAINING_CALL", Context.MODE_PRIVATE).getInt("NUMBER", 0);
    }


    public void saveContactNumber(Context context, String contact_number){
        context.getSharedPreferences("CONTACT_NUMBER", Context.MODE_PRIVATE).edit().putString("CONTACT", contact_number).apply();
    }

    public void removeContactNumber(Context context){
        context.getSharedPreferences("CONTACT_NUMBER", Context.MODE_PRIVATE).edit().putString("CONTACT", "").apply();
    }

    public String fetchContactNumber(Context context){
        return context.getSharedPreferences("CONTACT_NUMBER", Context.MODE_PRIVATE).getString("CONTACT", "");
    }
}
