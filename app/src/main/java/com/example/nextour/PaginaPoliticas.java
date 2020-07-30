package com.example.nextour;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

/**
 * Classe utilizada para referenciar os componentes e executar as ações da página que vai mostrar os
 * políticas de privacidade da aplicação.
 */
public class PaginaPoliticas extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private DrawerLayout aDrawerLayout;
    private ActionBarDrawerToggle aToggle;
    private NavigationView navView;

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
        setContentView(R.layout.activity_pagina_politicas);

        criarDrawerLayout();

        navView = findViewById(R.id.menu_cair);
        navView.setNavigationItemSelectedListener(this);
        botoesDisponiveis();
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
                    startActivity(new Intent(PaginaPoliticas.this, PaginaPerfil.class));
                } else {
                    startActivity(new Intent(PaginaPoliticas.this, PaginaLogin.class));
                }
                break;
            }
            case R.id.paginaCriarGuia: {
                if (sp.getBoolean("logado", false)) {
                    startActivity(new Intent(PaginaPoliticas.this, PaginaCriarGuia.class));
                } else {
                    startActivity(new Intent(PaginaPoliticas.this, PaginaLogin.class));
                }
                break;
            }
            case R.id.paginaMeusGuias: {
                startActivity(new Intent(PaginaPoliticas.this, PaginaMeusGuias.class));
                break;
            }
            case R.id.paginaAjuda: {
                startActivity(new Intent(PaginaPoliticas.this, PaginaAjuda.class));
                break;
            }
            case R.id.paginaInicial: {
                startActivity(new Intent(PaginaPoliticas.this, PaginaInicial.class));
                break;
            }
            case R.id.paginaSair: {
                sp.edit().clear().commit();
                startActivity(new Intent(PaginaPoliticas.this, PaginaLogin.class));
                break;
            }
            case R.id.paginaLogin: {
                startActivity(new Intent(PaginaPoliticas.this, PaginaLogin.class));
                break;
            }
        }
        return true;
    }
}
