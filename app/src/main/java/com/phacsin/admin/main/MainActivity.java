package com.phacsin.admin.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.phacsin.admin.DBHandler;
import com.phacsin.admin.EventDetails;
import com.phacsin.admin.scan.ScanParticipation;
import com.phacsin.admin.scan.ScanPayment;
import com.phacsin.admin.scan.ScanPaymentView;
import com.phacsin.admin.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    public Button pay,reg,pay_view,participate;
    private Button push;
    DBHandler dbhandler;
    SweetAlertDialog sweetalert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         pay = (Button) findViewById(R.id.payment);
         reg = (Button) findViewById(R.id.btn_register);
        pay_view = (Button) findViewById(R.id.view_payment);
        participate = (Button) findViewById(R.id.participation);
        push = (Button) findViewById(R.id.btn_push);
        dbhandler = new DBHandler(getApplicationContext());
        checkCameraPermission();
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent r = new Intent(getApplicationContext(), ScanPayment.class);
                startActivity(r);
            }
        });
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent r = new Intent(getApplicationContext(), SpinnerPage.class);
                startActivity(r);
            }
        });
        pay_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent r = new Intent(getApplicationContext(), ScanPaymentView.class);
                startActivity(r);
            }
        });
        participate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent r = new Intent(getApplicationContext(), ScanParticipation.class);
                startActivity(r);
            }
        });
        push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbhandler.numberOfRows("registration")==0)
                {
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Push to Server")
                            .setContentText("No new IDs registered")
                            .setConfirmText("OK")
                            .show();
                }
                else
                {
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Push to Server")
                            .setContentText("Do you want to push your IDs to server...This may take a while")
                            .setConfirmText("OK")
                            .setCancelText("No")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                    sweetalert =  new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE)
                                            .setTitleText("Uploading Data")
                                            .setContentText("Wait a minute..");
                                    sweetalert.show();
                                    registerEvent();
                                }
                            })
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            })
                            .show();
                }
            }
        });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private void registerEvent() {
        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String admin_id=sharedPreferences.getString("uuid","");
        JSONArray jsonArray = new JSONArray();
        String jsonString = null;
        List<EventDetails> events =  dbhandler.getAllEvents();
        try {
            for (EventDetails event : events) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("admin_id", admin_id);
                jsonObj.put("uuid", event.uuid);
                jsonObj.put("event_id", event.event_id);
                jsonArray.put(jsonObj);
            }
            jsonString = URLEncoder.encode(jsonArray.toString(), "utf-8");
        }
        catch (JSONException e)
        {

        }
        catch (UnsupportedEncodingException e)
        {

        }
        final String URL = "http://entreprenia.org/app/registration_confirm.php?json_string="+jsonString;
        Log.d("URL",URL);
        StringRequest strReq = new StringRequest(Request.Method.GET,
                URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if(response.equals("Success")) {
                    sweetalert.dismiss();
                    dbhandler.removeEvent();
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(response)
                            .setContentText("Successful")
                            .show();                }
                else
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(response)
                            .setContentText("Successful")
                            .show();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("vError", "Error: " + error.getMessage());
                String errorMsg;
                if(error instanceof NoConnectionError)
                    errorMsg = "Network Error";
                else if(error instanceof TimeoutError)
                    errorMsg = "Timeout Error";
                else
                    errorMsg = "Unknown Error";
                Snackbar.make(findViewById(android.R.id.content), errorMsg, Snackbar.LENGTH_LONG)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                registerEvent();
                            }
                        }).show();
            }

        });

// Adding request to request queue
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(strReq);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0 : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {

                }
                return;
            }

        }
    }

}