package com.example.nextour.classes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.nextour.PaginaCriarGuia;
import com.example.nextour.R;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class GereGuias {

    private static final String TAG = "BOOOAS";

    public GereGuias() {
    }


    public void inserirGuiaTuristico(final String guiaTuristicoJSON, final Context aContext, Activity aActivity) {
        String url = "http://" + aContext.getResources().getString(R.string.ip_e_porto) + "/resources/gereguias/" + aContext.getResources().getString(R.string.rest_key) + "/guia";
        SharedPreferences sharedPreferences = aContext.getSharedPreferences("nextour", Context.MODE_PRIVATE);
        StringRequest strRequest = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(aContext, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("guiaTuristico", guiaTuristicoJSON);
                params.put("email", sharedPreferences.getString("email", ""));
                return params;
            }
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                switch (response.statusCode) {
                    case 200: {
                        aActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(aContext, "Guia inserido com sucesso", Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                    default: {
                        aActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(aContext, "Erro ao inserir guia", Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                }
                return super.parseNetworkResponse(response);
            }

        };
        strRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(aContext);
        requestQueue.add(strRequest);
    }


    public void editarGuiaTuristico(final String guiaTuristicoJSON, final Context aContext, Activity aActivity) {
        String url = "http://" + aContext.getResources().getString(R.string.ip_e_porto) + "/resources/gereguias/" + aContext.getResources().getString(R.string.rest_key) + "/guia";
        SharedPreferences sharedPreferences = aContext.getSharedPreferences("nextour", Context.MODE_PRIVATE);
        StringRequest strRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(aContext, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("guiaTuristico", guiaTuristicoJSON);
                params.put("email", sharedPreferences.getString("email", ""));
                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                switch (response.statusCode) {
                    case 200: {
                        aActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(aContext, "Guia editado com sucesso", Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                    default: {
                        aActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(aContext, "Erro ao editar o guia", Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                }
                return super.parseNetworkResponse(response);
            }
        };
        strRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(aContext);
        requestQueue.add(strRequest);
    }


    public void obterGuiaParaEdicao(final String idGuia, final Context aContext) {
        String url = "http://" + aContext.getResources().getString(R.string.ip_e_porto) + "/resources/gereguias/" + aContext.getResources().getString(R.string.rest_key) + "/guia?id=" + idGuia;
        SharedPreferences sharedPreferences = aContext.getSharedPreferences("nextour", Context.MODE_PRIVATE);
        StringRequest strRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                GuiaTuristico guiaTuristico = gson.fromJson(response, GuiaTuristico.class);
                guiaTuristico.setAudio(null);
                guiaTuristico.setListaImagens(null);
                for (int x = 0; x < guiaTuristico.getListaPontosParagem().size(); x++) {
                    guiaTuristico.getListaPontosParagem().get(x).setAudio(null);
                    guiaTuristico.getListaPontosParagem().get(x).setListaImagens(null);
                }
                String json = gson.toJson(guiaTuristico, GuiaTuristico.class);
                aContext.startActivity(new Intent(aContext, PaginaCriarGuia.class).putExtra("guiaTuristico", json));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(aContext, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", idGuia);
                return params;
            }
        };
        strRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(aContext);
        requestQueue.add(strRequest);
    }


    public void eliminarGuiaTuristico(final String aIdJson, final Context aContext, Activity aActivity) {
        Log.e(TAG, "eliminarGuiaTuristico: " + aIdJson);
        String url = "http://" + aContext.getResources().getString(R.string.ip_e_porto) + "/resources/gereguias/" + aContext.getResources().getString(R.string.rest_key) + "/guia?id="+ aIdJson;
        StringRequest strRequest = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(aContext, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", aIdJson);
                return params;
            }
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                switch (response.statusCode) {
                    case 200: {
                        aActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(aContext, "Guia eliminado", Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                    default: {
                        aActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(aContext, "Erro ao eliminar guia", Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                }
                return super.parseNetworkResponse(response);
            }
        };
        strRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(aContext);
        requestQueue.add(strRequest);
    }
}
