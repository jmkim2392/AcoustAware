package ca.bcit.acoustaware;


import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class CrashSite {
    private String city;
    private int CrashCount;
    private String CrashType;
    private double latitude;
    private String location;
    private double longtitude;
    private int year;
    private static Context context;
    private int radius;


    public static ArrayList<CrashSite> getCrashSites() {
        return crashSites;
    }


    public static void setContext(Context context) {
        CrashSite.context = context;
    }


    public static ArrayList<CrashSite> crashSites;
//    public static CrashSite[] crashSites=init();

    public static void init(){
        try {
            crashSites = new ArrayList<>();
            InputStream in = context.getAssets().open("locations.txt");
            Scanner scanner = new Scanner(in);
            scanner.nextLine();
            while (scanner.hasNextLine()){
                String [] stringArray = scanner.nextLine().split(",");
                String city = stringArray[0];
                int crashCount = Integer.parseInt(stringArray[1]);
                String crashType = stringArray[2];
                double latitude = Double.parseDouble(stringArray[3]);
                String location = stringArray[4];
                double longitude = Double.parseDouble(stringArray[5]);
                int year = Integer.parseInt(stringArray[6]);

                crashSites.add(new CrashSite(city,crashCount,crashType,latitude,location,longitude,year));

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("error",crashSites.toString());

    }
    public CrashSite(String city, int crashCount, String crashType, double latitude, String location, double longitude, int year) {
        this.city = city;
        CrashCount = crashCount;
        CrashType = crashType;
        this.latitude = latitude;
        this.location = location;
        this.longtitude = longitude;
        this.year = year;
        this.radius = 0;
    }



    public String getCity() {
        return city;
    }

    public int getCrashCount() {
        return CrashCount;
    }

    public String getCrashType() {
        return CrashType;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getLocation() {
        return location;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public int getYear() {
        return year;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCrashCount(int crashCount) {
        CrashCount = crashCount;
    }

    public void setCrashType(String crashType) {
        CrashType = crashType;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setLongtitude(double longitude) {
        this.longtitude = longitude;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
