package iop.gimbalbeacon.Controller;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Bruno Fernandes on 01/03/2018.
 */

public class LocationPermissions implements DialogInterface.OnClickListener{

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static Activity activity;

    public LocationPermissions(Activity activity){
        LocationPermissions.activity = activity;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(which == DialogInterface.BUTTON_POSITIVE){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else if(which == DialogInterface.BUTTON_NEGATIVE){
            activity.finish();
        }
    }

    public void checkAndRequestPermission() {
        if(isLocationPermissionEnabled()){
            enableGimbalMonitoring();
        }else{
            requestLocationPermission();
        }
    }

    public boolean isLocationPermissionEnabled() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            showMessageOKCancel("Gimbal SDK requires location permission. Please grant such permission!", activity, this, this);
            return;
        }
        activityRequestPermission();
    }

    private static void showMessageOKCancel(String message, Activity activity, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", cancelListener)
                .create().show();
    }

    private void activityRequestPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults){
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableGimbalMonitoring();
            }
        }
    }

    private void enableGimbalMonitoring() {
        GimbalIntegration.instance().startListening(true, true, true, true, GimbalIntegration.DEBUG_LOGGING);
        PushRegistrationHelper.registerForPush();
        if(activity.getClass().getSimpleName().compareTo("BeaconScannerActivity") != 0)
            activity.finish();
    }

}
