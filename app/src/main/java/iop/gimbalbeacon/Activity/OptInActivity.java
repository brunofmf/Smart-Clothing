package iop.gimbalbeacon.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import iop.gimbalbeacon.Model.GimbalDAO;
import iop.gimbalbeacon.Controller.GimbalIntegration;
import iop.gimbalbeacon.Controller.LocationPermissions;
import iop.gimbalbeacon.Controller.PushRegistrationHelper;
import iop.gimbalbeacon.R;

public class OptInActivity extends AppCompatActivity {

    LocationPermissions permissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opt_in);
    }

    public void onEnableClicked(View view) {
        GimbalDAO.setOptInShown(getApplicationContext());
        enableGimbalMonitoring();
    }

    private void enableGimbalMonitoring(){
        permissions = new LocationPermissions(this);
        permissions.checkAndRequestPermission();
        PushRegistrationHelper.registerForPush();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        this.permissions.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    public void onNotNowClicked(View view) {
        GimbalDAO.setOptInShown(getApplicationContext());
        GimbalIntegration.instance().onTerminate();
        finish();
    }

    public void onPrivacyPolicyClicked(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://your-privacy-policy-url")));
    }

    public void onTermsOfServiceClicked(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://your-terms-of-use-url")));
    }

}
