package com.example.nextour;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.nextour.adicionais.VerGuiaRecyclerViewAdapter;
import com.example.nextour.classes.GuiaTuristico;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilizada para referenciar os componentes e executar as ações da página que vai mostrar o
 * guia que o utilizador pretende ver.
 */
public class PaginaVerGuia extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = "PaginaMeuGuia";
    private GuiaTuristico guiaTuristico;
    private String classificacao = null;

    private DrawerLayout aDrawerLayout;
    private ActionBarDrawerToggle aToggle;
    private NavigationView navView;
    private String email;
    private boolean isFavorito = false;


    /**
     * Método onCreate que será chamado quando esta atividade for iniciada pela primeira vez e serão
     * associados todos os componentes às variáveis em questão e ainda as variáveis necessárias para
     * iniciar esta página.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagina_ver_guia);
        email = getSharedPreferences("nextour", MODE_PRIVATE).getString("email", "");

        initImagensBitmaps();

        criarDrawerLayout();

        navView = findViewById(R.id.menu_cair);
        navView.setNavigationItemSelectedListener(this);
        botoesDisponiveis();

    }

    /**
     * Este método vai buscar os dados do guia e passar para as variáveis correspondentes.
     */
    private void initImagensBitmaps() {
        final long idGuiaEscolhido = getIntent().getLongExtra("idGuiaEscolhido", 0);
        StringRequest strRequest = new StringRequest(Request.Method.GET, "http://" + getResources().getString(R.string.ip_e_porto)
                + "/resources/gereguias/" + getResources().getString(R.string.rest_key) + "/guia?id="
                + idGuiaEscolhido, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                guiaTuristico = new Gson().fromJson(response, GuiaTuristico.class);

                StringRequest strRequest = new StringRequest(Request.Method.GET, "http://" + getResources().getString(R.string.ip_e_porto)
                        + "/resources/gereguias/" + getResources().getString(R.string.rest_key) + "/guia/classificacao/total?idGuiaTuristico="
                        + idGuiaEscolhido, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "onResponse: " + new Gson().fromJson(response, String.class));
                        classificacao = new Gson().fromJson(response, String.class);
                        if (!email.equals("")) {
                            StringRequest strRequest = new StringRequest(Request.Method.GET, "http://" + getResources().getString(R.string.ip_e_porto)
                                    + "/resources/gereguias/" + getResources().getString(R.string.rest_key) + "/guia/favorito?aEmail=" + email
                                    + "&idGuiaTuristico=" + idGuiaEscolhido, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if (new Gson().fromJson(response, String.class).equals("1")) {
                                        isFavorito = true;
                                    } else {
                                        isFavorito = false;
                                    }
                                    initRecyclerView();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("aEmail", email);
                                    params.put("idGuiaTuristico", String.valueOf(idGuiaEscolhido));
                                    return params;
                                }
                            };

                            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                            requestQueue.add(strRequest);
                        } else {
                            initRecyclerView();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("idGuiaTuristico", String.valueOf(idGuiaEscolhido));
                        return params;
                    }
                };

                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(strRequest);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(idGuiaEscolhido));
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(strRequest);

    }

    /**
     * Este método vai inicializar o adapter correspondente ao guia turístico.
     * Neste caso, é o VerGuiaRecyclerViewAdapter que terá como parâmetros de entrada os dados do
     * guia turítico.
     */
    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        Log.e(TAG, "tamanho do guia:" + guiaTuristico.getListaPontosParagem().size());
        VerGuiaRecyclerViewAdapter verGuiaRecyclerViewAdapter = new VerGuiaRecyclerViewAdapter(guiaTuristico, classificacao, this, isFavorito, getSupportFragmentManager(), this);
        recyclerView.setAdapter(verGuiaRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Este método irá inserir o menu lateral na página xml em questão.
     */
    private void criarDrawerLayout() {
        aDrawerLayout = findViewById(R.id.dropdown_menu);
        aToggle = new ActionBarDrawerToggle(this, aDrawerLayout, R.string.abrir, R.string.fechar);
        aDrawerLayout.addDrawerListener(aToggle);
        aToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Este método irá fazer com que o menu lateral seja clicável.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (aToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Este método irá verificar que botões no menu lateral poderão ser mostrados ao utilizador de
     * acordo com o seu estado de login.
     */
    private void botoesDisponiveis() {
        SharedPreferences sp = getSharedPreferences("nextour", MODE_PRIVATE);
        if (!sp.getBoolean("logado", false)) {
            MenuItem navigationMenuItemView = navView.getMenu().findItem(R.id.paginaSair);
            navigationMenuItemView.setVisible(false);
            navigationMenuItemView = navView.getMenu().findItem(R.id.paginaMeusGuias);
            navigationMenuItemView.setVisible(false);
            navigationMenuItemView = navView.getMenu().findItem(R.id.paginaLogin);
            navigationMenuItemView.setVisible(true);
        } else {
            MenuItem navigationMenuItemView = navView.getMenu().findItem(R.id.paginaLogin);
            navigationMenuItemView.setVisible(false);
        }
    }


    /**
     * Este método irá verificar que botão foi carregado no menu lateral e o de baixo e executar as ações necessárias.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        SharedPreferences sp = getSharedPreferences("nextour", MODE_PRIVATE);
        switch (item.getItemId()) {
            case R.id.paginaPerfil: {
                if (sp.getBoolean("logado", false)) {
                    startActivity(new Intent(PaginaVerGuia.this, PaginaPerfil.class));
                } else {
                    startActivity(new Intent(PaginaVerGuia.this, PaginaLogin.class));
                }
                break;
            }
            case R.id.paginaCriarGuia: {
                if (sp.getBoolean("logado", false)) {
                    startActivity(new Intent(PaginaVerGuia.this, PaginaCriarGuia.class));
                } else {
                    startActivity(new Intent(PaginaVerGuia.this, PaginaLogin.class));
                }
                break;
            }
            case R.id.paginaMeusGuias: {
                startActivity(new Intent(PaginaVerGuia.this, PaginaMeusGuias.class));
                break;
            }
            case R.id.paginaAjuda: {
                startActivity(new Intent(PaginaVerGuia.this, PaginaAjuda.class));
                break;
            }
            case R.id.paginaInicial: {
                startActivity(new Intent(PaginaVerGuia.this, PaginaInicial.class));
                break;
            }
            case R.id.paginaSair: {
                sp.edit().clear().commit();
                startActivity(new Intent(PaginaVerGuia.this, PaginaLogin.class));
                break;
            }
            case R.id.paginaLogin: {
                startActivity(new Intent(PaginaVerGuia.this, PaginaLogin.class));
                break;
            }
        }
        return true;
    }

    /**
     * Este método irá realizar uma atualização da página.
     */
    public void reload() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }
}
