package com.jms.geofencinghh;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.jms.geofencinghh.model.HubGeoFenceJsonModel;
import com.jms.geofencinghh.model.LatLngModel;
import com.jms.geofencinghh.utils.CommonUtility;
import com.jms.geofencinghh.utils.PolygonUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int ERROR_VALUE = -999;
    private static final String TAG = "GEO_FENCE";
    ArrayList<String> logs = new ArrayList<>();
    ArrayAdapter<String> logAdapter;
    //Most of fields are unused, still keeping for future use.
    ArrayList<HubGeoFenceJsonModel> hubGeoFenceJsonModels = new ArrayList<>();
    ArrayList<ArrayList<LatLng>> hubGeoFences = new ArrayList<>();
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLogView();
        loadAssets();
        initListeners();
    }

    private void showLogs(String logData) {
        logs.add(logData);
        logAdapter.notifyDataSetChanged();
        scrollMyListViewToBottom();
        Log.v(TAG, logData);
    }

    private void clearLogs() {
        logs.clear();
        logAdapter.notifyDataSetChanged();
    }

    private void initLogView() {
        logAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_item, logs);
        listView = findViewById(R.id.logRecyclerView);
        listView.setAdapter(logAdapter);
    }

    private void scrollMyListViewToBottom() {
        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view..
                listView.setSelection(logAdapter.getCount() - 1);
            }
        });
    }

    private void loadAssets() {
        String[] hubJsonFileNames = {"hub.json", "hub2.json", "hub3.json"};
        for (String hubJsonFile : hubJsonFileNames) {
            String jsonString = CommonUtility.loadJsonFromAssets(hubJsonFile, getAssets());//loadJsonFromAssets(hubJsonFile);
            HubGeoFenceJsonModel hubGeoFenceJsonModel = new Gson().fromJson(jsonString, HubGeoFenceJsonModel.class);
            hubGeoFenceJsonModels.add(hubGeoFenceJsonModel);
            hubGeoFences.add(getLatLngOnly(hubGeoFenceJsonModel));
            showLogs(hubJsonFile + " (" + hubGeoFenceJsonModel.results.tags.name + ")\t Loaded..!!");
        }
    }

    private ArrayList<LatLng> getLatLngOnly(HubGeoFenceJsonModel hubGeoFenceJsonModel) {
        ArrayList<LatLng> geoFenceList = new ArrayList<>();
        if (hubGeoFenceJsonModel != null && hubGeoFenceJsonModel.results != null && hubGeoFenceJsonModel.results.geometry != null && hubGeoFenceJsonModel.results.geometry.size() > 0) {
            ArrayList<LatLngModel> hubGeoLatLngList = hubGeoFenceJsonModel.results.geometry.get(0);
            for (LatLngModel latLngModel : hubGeoLatLngList) {
                geoFenceList.add(latLngModel.getLatLng());
            }
        }
        return geoFenceList;
    }

    private void initListeners() {
        findViewById(R.id.checkNow).setOnClickListener(this);
        findViewById(R.id.resetAll).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkNow:
                validateInputAndCheck();
                break;
            case R.id.resetAll:
                resetAll();
                break;
            default:
                break;
        }
    }

    private void resetAll() {
        clearLogs();
        ((EditText) findViewById(R.id.enter_lat)).setText("");
        ((EditText) findViewById(R.id.enter_lang)).setText("");
        ((EditText) findViewById(R.id.enter_accuracy)).setText("");
    }

    private void validateInputAndCheck() {
        double latitude = readDoubleFromEditText(R.id.enter_lat);
        double longitude = readDoubleFromEditText(R.id.enter_lang);
        if (PolygonUtils.getInstance().isValidGpsCoordinate(latitude, longitude)) {
            double accuracy = getAccuracy();
            findCheckPointOnFence(new LatLng(latitude, longitude), accuracy);
        } else {
            showLogs("Invalid Co-ordinates");
        }
    }

    private double getAccuracy() {
        String val = ((EditText) findViewById(R.id.enter_accuracy)).getText().toString();
        if (!TextUtils.isEmpty(val)) {
            return Double.parseDouble(val);
        }
        return 0;
    }

    private double readDoubleFromEditText(int resId) {
        String val = ((EditText) findViewById(resId)).getText().toString();
        if (!TextUtils.isEmpty(val)) {
            return Double.parseDouble(val);
        }
        ((EditText) findViewById(resId)).setError("Enter valid data..!!");
        return ERROR_VALUE;
    }

    private void findCheckPointOnFence(LatLng geoPoint, double accuracy) {
        //Checking point for each GeoFenceList
        for (int i = 0; i < hubGeoFences.size(); i++) {
            boolean isPointInsidePolygon = PolygonUtils.getInstance().isLocationWithinArea(geoPoint, accuracy, hubGeoFences.get(i));
            showLogs(isPointInsidePolygon ? ("InSide " + hubGeoFenceJsonModels.get(i).results.tags.name) :
                    ("OutSide " + hubGeoFenceJsonModels.get(i).results.tags.name + "\nDistance from fence >> " + PolygonUtils.getInstance().findMinimumDistance(geoPoint, hubGeoFences.get(i))));
        }
    }
}
