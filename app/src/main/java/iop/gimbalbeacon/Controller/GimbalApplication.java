package iop.gimbalbeacon.Controller;

import android.app.Application;
import android.util.Log;

import iop.gimbalbeacon.Model.GimbalDAO;

/**
 * Created by Bruno Fernandes on 01/03/2018.
 */

public class GimbalApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("-----", "GimbalApplication created");
        //Init Listeners
        GimbalDAO.clearEventsList(this.getApplicationContext());
        GimbalIntegration.init(this).onCreate();
    }

    @Override
    public void onTerminate(){
        super.onTerminate();
        GimbalIntegration.init(this).onTerminate();
    }

}
