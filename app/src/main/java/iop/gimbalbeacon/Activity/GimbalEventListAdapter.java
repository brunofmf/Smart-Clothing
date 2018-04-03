package iop.gimbalbeacon.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import iop.gimbalbeacon.Model.GimbalEvent;
import iop.gimbalbeacon.R;

public class GimbalEventListAdapter extends BaseAdapter {

    private Activity activity;
    private List<GimbalEvent> events = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, hh:mm:ss a", Locale.getDefault());

    public GimbalEventListAdapter(Activity activity){
        this.activity = activity;
    }

    public void setEvents(List<GimbalEvent> events) {
        this.events.clear();
        this.events.addAll(events);
        notifyDataSetChanged();
    }

    public void addEvent(GimbalEvent event) {
        events.add(event);
        notifyDataSetChanged();
    }

    /**
     * When the ListView is created and whatnot, it calls getCount().
     * If this returns a value different than 0, then it calls getView() enough times to fill the screen with the items
     * */
    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int i) {
        return events.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        GimbalEvent event = events.get(i);
        View view = convertView;
        if (view == null) {
            view = activity.getLayoutInflater().inflate(R.layout.list_item, null);
        }

        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        Integer iconRes = iconRes(event.getType());
        if (iconRes != null) {
            icon.setImageResource(iconRes);
        }
        else {
            icon.setImageDrawable(null);
        }

        TextView title = (TextView) view.findViewById(R.id.title);
        TextView subtitle = (TextView) view.findViewById(R.id.date);
        TextView beaconName = (TextView) view.findViewById(R.id.beaconName);
        TextView beaconBattery = (TextView) view.findViewById(R.id.beaconBattery);
        TextView beaconRSSI = (TextView) view.findViewById(R.id.beaconRSSI);
        TextView beaconDistance = (TextView) view.findViewById(R.id.beaconDistance);
        TextView beaconTemperature = (TextView) view.findViewById(R.id.beaconTemperature);

        title.setText(event.getTitle());
        subtitle.setText(dateFormat.format(event.getDate()));

        if(event.getType() == GimbalEvent.TYPE.BEACON_SIGHTING){
            beaconName.setText("Name: " + event.getBeaconName());
            beaconBattery.setText("Battery: " + event.getBeaconBattery());
            beaconRSSI.setText("RSSI: " + event.getRSSI().toString());
            beaconDistance.setText("Approx. Distance: " + Double.toString(event.getDistance()) + " m");
            beaconTemperature.setText("Temperature: " + Integer.toString(event.getTemperature()) + " ºC");
        }

        //By Default hide details
        if(beaconName.getVisibility() != View.GONE){
            beaconName.setVisibility(View.GONE);
            beaconBattery.setVisibility(View.GONE);
            beaconRSSI.setVisibility(View.GONE);
            beaconDistance.setVisibility(View.GONE);
            beaconTemperature.setVisibility(View.GONE);
        }

        return view;
    }

    private int iconRes(GimbalEvent.TYPE type) {
        switch (type) {
            case BEACON_SIGHTING:
                return R.drawable.sighting;
            case PLACE_ENTER:
                return R.drawable.place_enter;
            case PLACE_ENTER_DELAY:
                return R.drawable.place_enter_delay;
            case PLACE_EXIT:
                return R.drawable.place_exit;
            case COMMUNICATION_PRESENTED:
                return R.drawable.comm_presented;
            case COMMUNICATION_ENTER:
                return R.drawable.comm_enter;
            case COMMUNICATION_EXIT:
                return R.drawable.comm_exit;
            case COMMUNICATION_INSTANT_PUSH:
                return R.drawable.comm_presented;
            case COMMUNICATION_TIME_PUSH:
                return R.drawable.comm_presented;
            case NOTIFICATION_CLICKED:
                return R.drawable.comm_enter;
            default:
                return R.drawable.place_enter;
        }
    }

}
