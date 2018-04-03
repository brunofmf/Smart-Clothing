package iop.gimbalbeacon.Model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import iop.gimbalbeacon.Model.GimbalEvent;

/**
 * Created by Bruno Fernandes on 17/01/2018.
 */

public class GimbalDAO {

    //class tag
    private final static String TAG = "GimbalDAO";
    //Class Constants
    public static final String GIMBAL_NEW_EVENT_ACTION = "GIMBAL_EVENT_ACTION";
    private static final String EVENTS_KEY = "events";
    //Preferences
    public static final String SHOW_OPT_IN_PREFERENCE = "pref_show_opt_in";

    //Opt In methods
    public static boolean showOptIn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SHOW_OPT_IN_PREFERENCE, true);
    }

    public static void setOptInShown(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(SHOW_OPT_IN_PREFERENCE, false);
        editor.commit();
    }

    //Get/Store the events
    public static List<GimbalEvent> getEvents(Context context) {
        List<GimbalEvent> events = new ArrayList<>();
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String jsonString = prefs.getString(EVENTS_KEY, null);
            if (jsonString != null) {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    GimbalEvent event = new GimbalEvent();
                    event.setType(GimbalEvent.TYPE.valueOf(jsonObject.getString("type")));
                    event.setTitle(jsonObject.getString("title"));
                    event.setDate(new Date(jsonObject.getLong("date")));
                    try {
                        event.setBeaconName(jsonObject.getString("beaconName"));
                        event.setBeaconBattery(jsonObject.getString("beaconBattery"));
                        event.setRSSI(jsonObject.getInt("RSSI"));
                        event.setDistance(jsonObject.getDouble("distance"));
                        event.setTemperature(jsonObject.getInt("temperature"));
                    } catch (JSONException e) {
                        event.setBeaconName("Beacon Name");
                        event.setBeaconBattery("Beacon Battery");
                        event.setRSSI(-1);
                        event.setDistance(-1.0);
                        event.setTemperature(-1);
                    }
                    events.add(event);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return events;
    }

    public static void setEvents(Context context, List<GimbalEvent> events) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            JSONArray jsonArray = new JSONArray();
            for (GimbalEvent event : events) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", event.getType().name());
                jsonObject.put("title", event.getTitle());
                jsonObject.put("date", event.getDate().getTime());
                jsonObject.put("beaconName", event.getBeaconName());
                jsonObject.put("beaconBattery", event.getBeaconBattery());
                jsonObject.put("RSSI", event.getRSSI());
                jsonObject.put("distance", event.getDistance());
                jsonObject.put("temperature", event.getTemperature());
                jsonArray.put(jsonObject);
            }
            String jstr = jsonArray.toString();
            Editor editor = prefs.edit();
            editor.putString(EVENTS_KEY, jstr);
            editor.commit();

            // Notify activity
            Intent intent = new Intent();
            intent.setAction(GIMBAL_NEW_EVENT_ACTION);
            context.sendBroadcast(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void clearEventsList(Context context){
        try{
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            JSONArray jsonArray = new JSONArray();

            List<GimbalEvent> events = getEvents(context);

            for (GimbalEvent event : events) {
                //Only remove beacon sightings
                if(event.getType() != GimbalEvent.TYPE.BEACON_SIGHTING){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("type", event.getType().name());
                    jsonObject.put("title", event.getTitle());
                    jsonObject.put("date", event.getDate().getTime());
                    jsonObject.put("beaconName", event.getBeaconName());
                    jsonObject.put("beaconBattery", event.getBeaconBattery());
                    jsonObject.put("RSSI", event.getRSSI());
                    jsonObject.put("distance", event.getDistance());
                    jsonObject.put("temperature", event.getTemperature());
                    jsonArray.put(jsonObject);
                }
            }
            String jstr = jsonArray.toString();
            Editor editor = prefs.edit();
            editor.putString(EVENTS_KEY, jstr);
            editor.commit();

            Log.d(TAG, "*** Clear Events List: events cleared... ***");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
