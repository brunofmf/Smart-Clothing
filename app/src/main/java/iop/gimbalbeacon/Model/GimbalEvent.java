package iop.gimbalbeacon.Model;

import com.gimbal.android.BeaconSighting;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Bruno Fernandes on 01/03/2018.
 */

public class GimbalEvent implements Serializable {

    private static final long serialVersionUID = 1L;
    private static int TXPOWER = -62;

    public enum TYPE {
        PLACE_ENTER,
        PLACE_ENTER_DELAY,
        PLACE_EXIT,
        BEACON_SIGHTING,
        COMMUNICATION_PRESENTED,
        COMMUNICATION_ENTER,
        COMMUNICATION_EXIT,
        COMMUNICATION_INSTANT_PUSH,
        COMMUNICATION_TIME_PUSH,
        APP_INSTANCE_ID_RESET,
        COMMUNICATION,
        NOTIFICATION_CLICKED
    };

    private TYPE type;
    private String title;
    private Date date;
    private String beaconName;
    private String beaconBattery;
    private Integer RSSI;
    private double distance;
    private int temperature;

    public GimbalEvent() {
    }

    public GimbalEvent(TYPE type, String title, Date date) {
        this.type = type;
        this.title = title;
        this.date = date;
        this.beaconName = "";
        this.beaconBattery = "";
        this.RSSI = -1;
        this.distance = -1.0;
        this.temperature = -1;
    }

    public GimbalEvent(TYPE type, String title, Date date, String beaconName, String beaconBattery, Integer RSSI, double distance, int temperature) {
        this.type = type;
        this.title = title;
        this.date = date;
        this.beaconName = beaconName;
        this.beaconBattery = beaconBattery;
        this.RSSI = RSSI;
        this.distance = distance;
        this.temperature = temperature;
    }

    public GimbalEvent(TYPE type, String title, Date date, BeaconSighting sighting) {
        this.type = type;
        this.title = title;
        this.date = date;
        this.beaconName = sighting.getBeacon().getName();
        this.beaconBattery = String.valueOf(sighting.getBeacon().getBatteryLevel());
        this.RSSI = sighting.getRSSI();
        Double rssi = this.RSSI * 1.0;
        this.distance = calculateAccuracy(TXPOWER, rssi);
        this.temperature = ((sighting.getBeacon().getTemperature()- 32)*5/9); //Fahrenheit to Celsius
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getBeaconName() {
        return beaconName;
    }

    public void setBeaconName(String beaconName) {
        this.beaconName = beaconName;
    }

    public String getBeaconBattery() {
        return beaconBattery;
    }

    public void setBeaconBattery(String beaconBattery) {
        this.beaconBattery = beaconBattery;
    }

    public Integer getRSSI() {
        return RSSI;
    }

    public void setRSSI(Integer RSSI) {
        this.RSSI = RSSI;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    private double calculateAccuracy(int txPower, Double rssi) {
        if (rssi == 0) {
            return -1.0; // if cannot calculate accuracy, return -1.
        }
        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }

    @Override
    public String toString() {
        //StringBuilder sb = new StringBuilder();
        //sb.append();
        return "BeaconObject{" +
                "beaconType='" + type + '\'' +
                ", name='" + beaconName + '\'' +
                ", title='" + title + '\'' +
                ", RSSI=" + RSSI +
                ", distance=" + distance +
                ", temperature=" + temperature +
                ", date='" + date + '\'' +
                ", batteryLevel='" + beaconBattery + '\'' +
                '}';
    }

}
