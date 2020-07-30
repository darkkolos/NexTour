package com.example.nextour;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.nextour.adicionais.ListaGuiasListAdapter;
import com.example.nextour.classes.GuiaTuristico;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilizada para referenciar os componentes e executar as ações da página que vai mostrar os
 * novos guias ao utilizador.
 */
public class PaginaInicial extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout aDrawerLayout;
    private ActionBarDrawerToggle aToggle;
    private NavigationView navView;
    private ListView mListView;
    private Context mContext;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        setContentView(R.layout.activity_pagina_inicial);

        mContext = getApplicationContext();

        //creating the dropdown menu
        criarDrawerLayout();

        //associating the listener to the navigation menu
        navView = findViewById(R.id.menu_cair);
        navView.setNavigationItemSelectedListener(this);
        botoesDisponiveis();

        BottomNavigationView navigationView = (BottomNavigationView) findViewById(R.id.navigationView);
        navigationView.setOnNavigationItemSelectedListener(this);
        navigationView.setSelectedItemId(R.id.paginaNovos);

        mListView = (ListView) findViewById(R.id.lista_ver_guias);

        swipeRefreshLayout = findViewById(R.id.pullToRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                carregarGuias();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        carregarGuias();

    }

    /**
     * Este método vai carregar os guias turísticos através de um pedido ao servidor e no final do pedido
     * irá inserir no ListView da sua página um ListaGuiasListAdapter que contem todos os guias e que
     * o utilizador irá ver.
     */
    private void carregarGuias() {

        final SharedPreferences sp = getSharedPreferences("nextour", MODE_PRIVATE);
        StringRequest strRequest = new StringRequest(Request.Method.GET, "http://" + getResources().getString(R.string.ip_e_porto)
                + "/resources/gereguias/" + getResources().getString(R.string.rest_key)
                + "/guias?escolha=1", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                ArrayList<GuiaTuristico> listaGuiasTuristicos = gson.fromJson(response, new TypeToken<ArrayList<GuiaTuristico>>() {
                }.getType());
                if (listaGuiasTuristicos == null) {
                    Toast.makeText(getApplicationContext(), "Ainda não existem guias para mostrar", Toast.LENGTH_LONG).show();
                } else {
                    ListaGuiasListAdapter listaGuiasListAdapter = new ListaGuiasListAdapter(mContext, R.layout.adapter_view_ver_guias, listaGuiasTuristicos, getSupportFragmentManager(), false);
                    mListView.setAdapter(listaGuiasListAdapter);
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
                params.put("escolha", "1");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(strRequest);

    }


    /**
     * Este método irá inserir o menu lateral na página xml em questão.
     */
    private void criarDrawerLayout() {//criar o hamburger menu
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
                    startActivity(new Intent(PaginaInicial.this, PaginaPerfil.class));
                } else {
                    startActivity(new Intent(PaginaInicial.this, PaginaLogin.class));
                }
                break;
            }
            case R.id.paginaCriarGuia: {
                if (sp.getBoolean("logado", false)) {
                    startActivity(new Intent(PaginaInicial.this, PaginaCriarGuia.class));
                } else {
                    startActivity(new Intent(PaginaInicial.this, PaginaLogin.class));
                }
                break;
            }
            case R.id.paginaMeusGuias: {
                startActivity(new Intent(PaginaInicial.this, PaginaMeusGuias.class));
                break;
            }
            case R.id.paginaAjuda: {
                startActivity(new Intent(PaginaInicial.this, PaginaAjuda.class));
                break;
            }
            case R.id.paginaInicial: {
                startActivity(new Intent(PaginaInicial.this, PaginaInicial.class));
                break;
            }
            case R.id.paginaSair: {
                sp.edit().clear().commit();
                startActivity(new Intent(PaginaInicial.this, PaginaLogin.class));
                break;
            }
            case R.id.paginaLogin: {
                startActivity(new Intent(PaginaInicial.this, PaginaLogin.class));
                break;
            }
            case R.id.paginaNovos: {
                return true;
            }
            case R.id.paginaPesquisar: {
                startActivity(new Intent(getApplicationContext(), PaginaPesquisar.class));
                overridePendingTransition(0, 0);
                return true;
            }
            case R.id.paginaMapa: {
                startActivity(new Intent(getApplicationContext(), PaginaMapa.class));
                overridePendingTransition(0, 0);
                return true;
            }
            case R.id.paginaFavoritos: {
                startActivity(new Intent(getApplicationContext(), PaginaFavoritos.class));
                overridePendingTransition(0, 0);
                return true;
            }
        }
        return true;
    }
}