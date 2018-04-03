package iop.gimbalbeacon.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceManager;

import iop.gimbalbeacon.Controller.GimbalIntegration;
import iop.gimbalbeacon.Controller.LocationPermissions;
import iop.gimbalbeacon.R;

public class SettingsActivity extends Activity {

    private CheckBox gimbalMonitoringCheckBox;
    LocationPermissions locationPermissions;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    NumberPicker np;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        locationPermissions = new LocationPermissions(this);

        gimbalMonitoringCheckBox = (CheckBox) findViewById(R.id.gimbal_monitoring_checkbox);
        gimbalMonitoringCheckBox.setChecked(PlaceManager.getInstance().isMonitoring());

        np = findViewById(R.id.numberPicker);
        np.setMinValue(10);
        np.setMaxValue(120);
        np.setValue(GimbalIntegration.instance().sightingTimeFrame/1000);
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                GimbalIntegration.instance().sightingTimeFrame = numberPicker.getValue()*1000;
                np.setValue(numberPicker.getValue());
            }
        });
    }

    public void onGimbalMonitoringClicked(View view) {
        gimbalMonitoringCheckBox.setChecked(!gimbalMonitoringCheckBox.isChecked());
        if (gimbalMonitoringCheckBox.isChecked()) {
            locationPermissions = new LocationPermissions(this);
            locationPermissions.checkAndRequestPermission();
        }
        else {
            GimbalIntegration.instance().onTerminate();
        }
    }

    public void onResetAppInstance(View view) {
        Gimbal.resetApplicationInstanceIdentifier();
        Toast.makeText(this, "App Instance ID reset successful", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        gimbalMonitoringCheckBox.setChecked(PlaceManager.getInstance().isMonitoring());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        locationPermissions.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

}
