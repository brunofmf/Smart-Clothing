package iop.gimbalbeacon.Controller;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gimbal.android.BeaconEventListener;
import com.gimbal.android.BeaconManager;
import com.gimbal.android.BeaconSighting;
import com.gimbal.android.Communication;
import com.gimbal.android.CommunicationListener;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.EstablishedLocationsManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.GimbalDebugger;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Push;
import com.gimbal.android.Visit;
import com.gimbal.logging.GimbalLogConfig;
import com.gimbal.logging.GimbalLogLevel;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import iop.gimbalbeacon.Activity.BeaconScannerActivity;
import iop.gimbalbeacon.Model.GimbalDAO;
import iop.gimbalbeacon.Model.GimbalEvent;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Bruno Fernandes on 01/03/2018.
 */

public class GimbalIntegration {

    //class tag
    private final static String TAG = "GimbalIntegration";

    //constants for Gimbal SDK logging levels
    public final static int DEBUG_LOGGING = 0;
    public final static int WARNING_LOGGING = 1;
    public final static int ERROR_LOGGING = 2;
    public final static int INFO_LOGGING = 3;
    public final static int TRACE_LOGGING = 4;

    //class constants
    private static final String API_KEY = "YOUR_API_KEY";
    private static final int MAX_NUM_EVENTS = 100;

    private Application app;
    private Context appContext;

    //The time that has to pass between beacon sightings - by default 30 seconds
    public static int sightingTimeFrame = 30000;

    private LinkedList<GimbalEvent> events;

    //class Listeners
    private BeaconManager beaconManager;
    private BeaconEventListener beaconEventListener;
    private PlaceEventListener placeEventListener;
    private CommunicationListener communicationListener;

    private static GimbalIntegration instance;

    private GimbalIntegration(Application app) {
        this.app = app;
        this.appContext = app.getApplicationContext();
    }

    public static GimbalIntegration instance() {
        if (instance == null) {
            throw new IllegalStateException("Gimbal integration not initialized from Application");
        }
        return instance;
    }

    public static GimbalIntegration init(Application app) {
        if (instance == null) {
            instance = new GimbalIntegration(app);
        }
        return instance;
    }

    public void onCreate() {
        //set YOUR ApiKey
        Gimbal.setApiKey(app, API_KEY);
        //create events
        events = new LinkedList<>(GimbalDAO.getEvents(app));
        //Create Listeners
        createListeners(true, true, true);
    }

    public void createListeners(boolean beacon, boolean place, boolean comm){
        if(beacon)
            setupGimbalBeaconManager();
        if(place)
            setupGimbalPlaceManager();
        if(comm)
            setupGimbalCommunicationManager();
    }

    public void startListening(boolean beacon, boolean place, boolean comm, boolean estLoc, int logging){
        if(logging > -1){
            setLoggingLevel(logging);
            GimbalDebugger.enableBeaconSightingsLogging();
        }
        if(beacon)
            beaconManager.startListening();
        if(place && !PlaceManager.getInstance().isMonitoring()){
            PlaceManager.getInstance().startMonitoring();
            Log.d(TAG, "*** Current Visits.isEmpty(): " + PlaceManager.getInstance().currentVisits().isEmpty() + " ***");
            //System.out.println("*** Current Visits.isEmpty(): " + PlaceManager.getInstance().currentVisits().isEmpty() + " ***");
        }
        if(comm && !CommunicationManager.getInstance().isReceivingCommunications()){
            CommunicationManager.getInstance().startReceivingCommunications();
            Log.d(TAG, "*** CommunicationManager.getNotificationChannelId(): " + CommunicationManager.getInstance().getNotificationChannelId() + " ***");
            //System.out.println("*** CommunicationManager.getNotificationChannelId(): " + CommunicationManager.getInstance().getNotificationChannelId() + " ***");
        }
        if(estLoc && !EstablishedLocationsManager.getInstance().isMonitoring()) {
            EstablishedLocationsManager.getInstance().startMonitoring();
            Log.d(TAG, "*** EstablishedLocationsManager.getEstablishedLocations(): " + EstablishedLocationsManager.getInstance().getEstablishedLocations().toString() + " ***");
            //System.out.println("*** EstablishedLocationsManager.getEstablishedLocations(): " + EstablishedLocationsManager.getInstance().getEstablishedLocations().toString() + " ***");
        }
    }

    public void setupGimbalBeaconManager(){
        this.beaconManager = new BeaconManager();

        this.beaconEventListener = new BeaconEventListener() {
            @Override
            public void onBeaconSighting(BeaconSighting beaconSighting) {
                String beaconName = beaconSighting.getBeacon().getName();
                Date currentDate = new Date();
                boolean newSighting = true;

                for(int i = 0; i < events.size(); i++){
                    if(events.get(i).getBeaconName().compareTo(beaconName) == 0){
                        //if 30s haven't passed since the last sighting of this beacon than cancel new sighting
                        if((currentDate.getTime() - events.get(i).getDate().getTime() < sightingTimeFrame))
                            newSighting = false;
                        //it is an ordered list, do not need to inspect older sightings
                        break;
                    }
                }

                if(newSighting) {
                    super.onBeaconSighting(beaconSighting);
                    addEvent(new GimbalEvent(GimbalEvent.TYPE.BEACON_SIGHTING, "Sighting Beacon " + beaconName, currentDate, beaconSighting));
                    //LOG - beaconSighting.getRSSI()
                    Log.d(TAG, "*** Beacon Sighting: " + beaconSighting.getBeacon().getName() + "***");
                    Log.d(TAG, "*** Beacon Sighting RSSI: " + beaconSighting.getRSSI() + "***");
                }
            }
        };
        beaconManager.addListener(beaconEventListener);
        Log.d(TAG, "*** Beacon Manager: onBeaconSighting listener added ***");
        //System.out.println("*** Beacon Manager: onBeaconSighting listener added ***");
    }

    public void setupGimbalPlaceManager() {
        placeEventListener = new PlaceEventListener() {
            @Override
            public void onVisitStart(Visit visit) {
                addEvent(new GimbalEvent(GimbalEvent.TYPE.PLACE_ENTER, visit.getPlace().getName(), new Date(visit.getArrivalTimeInMillis())));
                Log.i(TAG, "Enter: " + visit.getPlace().getName() + ", at: " + new Date(visit.getArrivalTimeInMillis()));
            }
            @Override
            public void onVisitStartWithDelay(Visit visit, int delayTimeInSeconds) {
                if (delayTimeInSeconds > 0) {
                    addEvent(new GimbalEvent(GimbalEvent.TYPE.PLACE_ENTER_DELAY, visit.getPlace().getName(), new Date(System.currentTimeMillis())));
                    Log.i(TAG, "Enter: " + visit.getPlace().getName() + ", at: " + new Date(visit.getArrivalTimeInMillis()));
                }
            }
            @Override
            public void onVisitEnd(Visit visit) {
                addEvent(new GimbalEvent(GimbalEvent.TYPE.PLACE_EXIT, visit.getPlace().getName(), new Date(visit.getDepartureTimeInMillis())));
                System.out.println("**** visit.getPlace().getAttributes().getAllKeys()" + visit.getPlace().getAttributes().getAllKeys().toString());
                Log.i(TAG, "Exit: " + visit.getPlace().getName() + ", at: " + new Date(visit.getDepartureTimeInMillis()));
            }
            @Override
            public void onBeaconSighting(BeaconSighting beaconSighting, List<Visit> visits){
                String beaconName = beaconSighting.getBeacon().getName();
                Date currentDate = new Date();
                boolean newSighting = true;

                for(int i = 0; i < events.size(); i++){
                    if(events.get(i).getBeaconName().compareTo(beaconName) == 0){
                        //if 30s haven't passed since the last sighting of this beacon than cancel new sighting
                        if((currentDate.getTime() - events.get(i).getDate().getTime() < sightingTimeFrame))
                            newSighting = false;
                        //it is an ordered list, do not need to inspect older sightings
                        break;
                    }
                }

                if(newSighting) {
                    addEvent(new GimbalEvent(GimbalEvent.TYPE.BEACON_SIGHTING, "Sighting Geofence Beacon " + beaconName, currentDate, beaconSighting));
                    //LOG - beaconSighting.getRSSI()
                    Log.d(TAG, "*** Beacon Sighting: " + beaconSighting.getBeacon().getName() + "***");
                    Log.d(TAG, "*** Beacon Sighting RSSI: " + beaconSighting.getRSSI() + "***");
                }
            }
        };
        PlaceManager.getInstance().addListener(placeEventListener);
        Log.d(TAG, "*** Place Manager: all listeners added ***");
        //System.out.println("*** Place Manager: all listeners added ***");
    }

    public void setupGimbalCommunicationManager() {
        communicationListener = new CommunicationListener() {
            @Override
            public Notification.Builder prepareCommunicationForDisplay(Communication communication, Visit visit, int notificationId) {
                addEvent(new GimbalEvent(GimbalEvent.TYPE.COMMUNICATION_PRESENTED,communication.getTitle() + ":  CONTENT_DELIVERED", new Date()));
                // If you want a custom notification create and return it here
                return null;
            }
            @Override
            public Notification.Builder prepareCommunicationForDisplay(Communication communication, Push push, int notificationId) {
                addEvent(new GimbalEvent(GimbalEvent.TYPE.COMMUNICATION_INSTANT_PUSH,communication.getTitle() + ":  CONTENT_DELIVERED", new Date()));
                // communication.getAttributes()
                return null;
            }
            @Override
            public void onNotificationClicked(List<Communication> communications) {
                for (Communication communication : communications) {
                    if(communication != null) {
                        addEvent(new GimbalEvent(GimbalEvent.TYPE.NOTIFICATION_CLICKED,communication.getTitle() + ": CONTENT_CLICKED", new Date()));
                        Intent intent  = new Intent(appContext, BeaconScannerActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(intent);
                    }
                }
            }
        };
        CommunicationManager.getInstance().addListener(communicationListener);
        Log.d(TAG, "*** Communication Manager: all listeners added ***");
        //System.out.println("*** Communication Manager: all listeners added ***");
    }

    public void addEvent(GimbalEvent event) {
        while (events.size() >= MAX_NUM_EVENTS) {
            events.removeLast();
        }
        events.add(0, event);
        GimbalDAO.setEvents(appContext, events);
    }

    public void onTerminate() {
        this.stopServices();
    }

    public void stopServices(){
        if(beaconManager != null && beaconEventListener != null){
            //beaconManager.removeListener(beaconEventListener);
            beaconManager.stopListening();
        }
        if(placeEventListener != null){
            //PlaceManager.getInstance().removeListener(placeEventListener);
            PlaceManager.getInstance().stopMonitoring();
        }
        if(communicationListener != null){
            //CommunicationManager.getInstance().removeListener(communicationListener);
            CommunicationManager.getInstance().stopReceivingCommunications();
        }
        if(EstablishedLocationsManager.getInstance().isMonitoring()){
            EstablishedLocationsManager.getInstance().stopMonitoring();
        }
    }

    public void setLoggingLevel(int loggingLevel) {
        Log.d(TAG, "Setting logging level.");
        switch (loggingLevel) {
            case DEBUG_LOGGING:
                GimbalLogConfig.setLogLevel(GimbalLogLevel.DEBUG);
                break;
            case WARNING_LOGGING:
                GimbalLogConfig.setLogLevel(GimbalLogLevel.WARN);
                break;
            case ERROR_LOGGING:
                GimbalLogConfig.setLogLevel(GimbalLogLevel.ERROR);
                break;
            case INFO_LOGGING:
                GimbalLogConfig.setLogLevel(GimbalLogLevel.INFO);
                break;
            case TRACE_LOGGING:
                GimbalLogConfig.setLogLevel(GimbalLogLevel.TRACE);
                break;
            default:
                break;
        }
    }

}
