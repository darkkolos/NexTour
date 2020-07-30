package com.example.nextour.classes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.nextour.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class GereUtilizadores {

    private ProgressDialog progressDialog;

    public GereUtilizadores() {
    }

    /**
     *
     * @param aPath
     * @param aContext
     * @param aEmail
     * @param aActivity
     */
    public void uploadFotografiaPerfil(final String aPath, final Context aContext, final String aEmail, final Activity aActivity) {

        progressDialog = new ProgressDialog(aContext);
        progressDialog.setMessage("A alterar fotografia");
        progressDialog.show();

        String url = "http://" + aContext.getResources().getString(R.string.ip_e_porto) + "/resources/gereutilizadores/" + aContext.getResources().getString(R.string.rest_key) + "/utilizador/fotografia";
        StringRequest strRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.hide();
                Toast.makeText(aContext, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("pathFotografia", aPath);
                params.put("email", aEmail);
                return params;
            }
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                switch (response.statusCode) {
                    case 200: {
                        aActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(aContext, "Fotografia alterada", Toast.LENGTH_SHORT).show();
                                aActivity.finish();
                                aActivity.overridePendingTransition(0, 0);
                                aActivity.startActivity(aActivity.getIntent());
                                aActivity.overridePendingTransition(0, 0);
                            }
                        });
                        break;
                    }
                    default: {
                        aActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(aContext, "Erro ao alterar fotografia.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    }
                }
                return super.parseNetworkResponse(response);
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(aContext);
        requestQueue.add(strRequest);
    }


    public void alterarPasswordUtilizador(final String aPassword, final Context aContext, final String aEmail, final Activity aActivity) {

        progressDialog = new ProgressDialog(aContext);
        progressDialog.setMessage("A alterar password");
        progressDialog.show();

        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(aPassword.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest();
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        final String passwordFinal = generatedPassword;

        String url = "http://" + aContext.getResources().getString(R.string.ip_e_porto) + "/resources/gereutilizadores/" + aContext.getResources().getString(R.string.rest_key) + "/utilizador/password";
        StringRequest strRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.hide();
                Toast.makeText(aContext, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("password", passwordFinal);
                params.put("email", aEmail);
                return params;
            }
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                switch (response.statusCode) {
                    case 200: {
                        aActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(aContext, "Password alterada", Toast.LENGTH_SHORT).show();
                                aActivity.finish();
                                aActivity.overridePendingTransition(0, 0);
                                aActivity.startActivity(aActivity.getIntent());
                                aActivity.overridePendingTransition(0, 0);
                            }
                        });
                        break;
                    }
                    default: {
                        aActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(aContext, "Erro ao alterar password", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    }
                }
                return super.parseNetworkResponse(response);
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(aContext);
        requestQueue.add(strRequest);
    }


    public void alterarNomeUtilizador(final String aNome, final Context aContext, final String aEmail, final Activity aActivity) {

        progressDialog = new ProgressDialog(aContext);
        progressDialog.setMessage("A alterar nome");
        progressDialog.show();

        String url = "http://" + aContext.getResources().getString(R.string.ip_e_porto) + "/resources/gereutilizadores/" + aContext.getResources().getString(R.string.rest_key) + "/utilizador/nome";
        StringRequest strRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.hide();
                Toast.makeText(aContext, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("nome", aNome);
                params.put("email", aEmail);
                return params;
            }
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                switch (response.statusCode) {
                    case 200: {
                        aActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(aContext, "Nome alterado", Toast.LENGTH_SHORT).show();
                                aActivity.finish();
                                aActivity.overridePendingTransition(0, 0);
                                aActivity.startActivity(aActivity.getIntent());
                                aActivity.overridePendingTransition(0, 0);
                            }
                        });
                        break;
                    }
                    default: {
                        aActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(aContext, "Erro ao alterar nome", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    }
                }
                return super.parseNetworkResponse(response);
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(aContext);
        requestQueue.add(strRequest);
    }
}
