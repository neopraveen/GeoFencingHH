package com.jms.geofencinghh.utils;

import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

public class CommonUtility {
    public static String loadJsonFromAssets(String fileName, AssetManager assetManager) {
        String json = null;
        try {
            InputStream is = assetManager.open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
