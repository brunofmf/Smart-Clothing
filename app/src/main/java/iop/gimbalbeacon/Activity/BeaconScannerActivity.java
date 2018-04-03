package iop.gimbalbeacon.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.bluetooth.*;
import android.widget.ProgressBar;
import android.widget.TextView;

import iop.gimbalbeacon.Controller.GimbalIntegration;
import iop.gimbalbeacon.Controller.LocationPermissions;
import iop.gimbalbeacon.Model.GimbalDAO;
import iop.gimbalbeacon.Controller.PushRegistrationHelper;
import iop.gimbalbeacon.R;

public class BeaconScannerActivity extends AppCompatActivity {

    private GimbalEventReceiver gimbalEventReceiver;
    private GimbalEventListAdapter adapter;
    private ProgressBar pb;
    private FloatingActionButton fab;

    LocationPermissions permissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_scanner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pb = (ProgressBar)findViewById(R.id.progressBar);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        configureFab(fab);

        permissions = new LocationPermissions(this);

        //if (GimbalDAO.showOptIn(getApplicationContext())) {
        //    startActivity(new Intent(this, OptInActivity.class));
        //}

        adapter = new GimbalEventListAdapter(this);
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);
        configureViewListener(listView);
    }

    public void configureFab(final FloatingActionButton fab){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    // Device does not support Bluetooth
                    Snackbar.make(view, "Device does not support Bluetooth LE", Snackbar.LENGTH_LONG).show();
                } else {
                    if (!mBluetoothAdapter.isEnabled()) {
                        // Bluetooth is not enabled
                        Snackbar.make(view, "Bluetooth is Off. Turn it on and scan?", Snackbar.LENGTH_LONG)
                                .setAction("Yes!", new BluetoothOnListener()).show();
                    } else{
                        Snackbar.make(view, "Scanning for nearby beacons...", Snackbar.LENGTH_LONG).show();
                        permissions.checkAndRequestPermission();
                        PushRegistrationHelper.registerForPush();
                        pb.setVisibility(View.VISIBLE);
                        fab.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    public void configureViewListener(ListView listView){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id){
                TextView beaconName = (TextView) view.findViewById(R.id.beaconName);
                TextView beaconBattery = (TextView) view.findViewById(R.id.beaconBattery);
                TextView beaconRSSI = (TextView) view.findViewById(R.id.beaconRSSI);
                TextView beaconDistance = (TextView) view.findViewById(R.id.beaconDistance);
                TextView beaconTemperature = (TextView) view.findViewById(R.id.beaconTemperature);

                if(beaconName.getVisibility() == View.GONE && (beaconName.getText().toString().compareTo("") != 0 && beaconName.getText().toString().compareTo("false") != 0)){
                    beaconName.setVisibility(View.VISIBLE);
                    beaconBattery.setVisibility(View.VISIBLE);
                    beaconRSSI.setVisibility(View.VISIBLE);
                    beaconDistance.setVisibility(View.VISIBLE);
                    beaconTemperature.setVisibility(View.VISIBLE);
                }
                else{
                    beaconName.setVisibility(View.GONE);
                    beaconBattery.setVisibility(View.GONE);
                    beaconRSSI.setVisibility(View.GONE);
                    beaconDistance.setVisibility(View.GONE);
                    beaconTemperature.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setEvents(GimbalDAO.getEvents(getApplicationContext()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        this.permissions.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    public void onProgressBarClicked(View view){
        pb.setVisibility(View.GONE);
        Snackbar.make(view, "Scanning for nearby beacons is now stopped!", Snackbar.LENGTH_LONG).show();
        fab.setVisibility(View.VISIBLE);
        GimbalIntegration.instance().stopServices();
    }

    @Override
    protected void onStart() {
        super.onStart();

        gimbalEventReceiver = new GimbalEventReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GimbalDAO.GIMBAL_NEW_EVENT_ACTION);
        registerReceiver(gimbalEventReceiver, intentFilter);

        adapter.setEvents(GimbalDAO.getEvents(getApplicationContext()));
}

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(gimbalEventReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        GimbalIntegration.instance().onTerminate();
        super.onDestroy();
    }

    // SETTINGS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_beacon_scanner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // EVENT RECEIVER
    class GimbalEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().compareTo(GimbalDAO.GIMBAL_NEW_EVENT_ACTION) == 0) {
                    adapter.setEvents(GimbalDAO.getEvents(getApplicationContext()));
                }
            }
        }
    }

    // Bluetooth Listener
    class BluetoothOnListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothAdapter.enable();
            Snackbar.make(v, "Scanning for nearby beacons...", Snackbar.LENGTH_LONG).show();
            permissions.checkAndRequestPermission();
            PushRegistrationHelper.registerForPush();
            pb.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
        }
    }

}
