package edu.tufts.contours.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trcolgrove.contours.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.tufts.contours.synths.SynthInfo;

/**
 * FileIO Functions
 *
 * source: http://www.java2s.com/Code/Java/File-Input-Output/ConvertInputStreamtoString.htm
 *
 * Created by Thomas on 6/16/16.
 */
public class FileIO {

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static ArrayList<SynthInfo> getAvailableSynths(Context context) {
        InputStream is = context.getResources().openRawResource(R.raw.synths);
        String rawJsonString = "";

        try {
            rawJsonString = FileIO.convertStreamToString(is);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                is.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        Gson gson = new Gson();
        ArrayList<SynthInfo> synths = gson.fromJson(rawJsonString, new TypeToken<ArrayList<SynthInfo>>() {
        }.getType());

        return synths;
    }
}
