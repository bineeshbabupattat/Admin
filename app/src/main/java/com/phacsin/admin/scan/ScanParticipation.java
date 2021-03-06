package com.phacsin.admin.scan;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.Result;
import com.phacsin.admin.R;

import cn.pedant.SweetAlert.SweetAlertDialog;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Bineesh P Babu on 22-09-2016.
 */
public class ScanParticipation extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView scannerView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_participation);
        scannerView = (ZXingScannerView) findViewById(R.id.scanner_view);
        scannerView.setResultHandler(this);
        scannerView.startCamera();
        sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);

    }

    private void participation(final String uuid) {
        String URL = "http://entreprenia.org/app/participation.php?uuid="+uuid;
        StringRequest strReq = new StringRequest(Request.Method.GET,
                URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if(response.equals("Success")) {
                    new SweetAlertDialog(ScanParticipation.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(uuid)
                            .setContentText("Successful")
                            .show();
                }
                else
                {
                    new SweetAlertDialog(ScanParticipation.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(uuid)
                            .setContentText(response)
                            .show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMsg;
                Log.d("volley_error",error.toString());
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
                                participation(uuid);
                            }
                        }).show();
            }

        });

// Adding request to request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(strReq);
    }

    @Override
    public void handleResult(Result rawResult) {
        final String Contents=rawResult.getText();
        scannerView.stopCamera();
        new SweetAlertDialog(ScanParticipation.this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(Contents)
                .setContentText("Confirm Participation")
                .setCancelText("Cancel")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        participation(Contents);
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        scannerView.startCamera();
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .show();
    }
}
