package iop.gimbalbeacon.Controller;

import com.gimbal.android.Gimbal;

/**
 * Created by Bruno Fernandes on 01/03/2018.
 */

public class PushRegistrationHelper {

    public static void registerForPush() {
        // Setup Push Communication
        String gcmSenderId = null; // <--- SET THIS STRING TO YOUR PUSH SENDER ID HERE (Google API project #) ##

        if (gcmSenderId != null) {
            Gimbal.registerForPush(gcmSenderId);
        }
    }

}
