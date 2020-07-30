package com.example.nextour;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilizada para referenciar os componentes e executar as ações da página que vai permitir
 * ao utilizador iniciar sessão na sua conta.
 */
public class PaginaLogin extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    EditText emailInput, passwordInput;
    Button botaoLogin;
    TextView registoString;
    private ProgressDialog progressDialog;
    DrawerLayout aDrawerLayout;
    ActionBarDrawerToggle aToggle;
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
        setContentView(R.layout.activity_pagina_login);

        registoString = findViewById(R.id.loginTextView);

        emailInput = findViewById(R.id.emailInput);

        passwordInput = findViewById(R.id.passwordInput);

        botaoLogin = findViewById(R.id.botaoLogin);
        botaoLogin.setOnClickListener(this);

        verificaLogado();

        criarDrawerLayout();

        criarStringsClicaveis();

        progressDialog = new ProgressDialog(this);

        //associating the listener to the navigation menu
        navView = findViewById(R.id.menu_cair);
        navView.setNavigationItemSelectedListener(this);
        botoesDisponiveis();
    }


    /**
     * Este método irá inicializar a string que permitirá a passagem para a página de registo.
     */
    private void criarStringsClicaveis() {//função para criar uma string underlined e mudar as cores da mesma e o que acontece quando é clicada

        ClickableSpan linkClick = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (view == registoString) {
                    startActivity(new Intent(PaginaLogin.this, PaginaRegistar.class));
                }
                view.invalidate();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                if (registoString.isPressed()) {
                    ds.setColor(getResources().getColor(R.color.azulClaro));
                    registoString.invalidate();
                } else {
                    ds.setColor(getResources().getColor(R.color.azulEscuro));
                }
            }
        };
        //inserção de uma string para o registo
        registoString.setHighlightColor(Color.TRANSPARENT);
        Spannable stringLogin = new SpannableString("Ainda não tens conta?\nFaz aqui o teu registo");
        stringLogin.setSpan(linkClick, 22, 44, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        registoString.setText(stringLogin, TextView.BufferType.SPANNABLE);
        registoString.setMovementMethod(LinkMovementMethod.getInstance());
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
     * Este méodo irá verificar se o utilizador já se encontra com sessão iniciada, caso sim, irá
     * enviá-lo para a página inicial.
     */
    private void verificaLogado() {//fazer verificação de login
        SharedPreferences sp = getSharedPreferences("nextour", MODE_PRIVATE);
        if (sp.getBoolean("logado", false)) {
            startActivity(new Intent(PaginaLogin.this, PaginaInicial.class));
        }

    }

    /**
     * Este método irá fazer com que o menu lateral seja clicável.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {//se o hamburger menu foi clicado
        if (aToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Este método irá verificar em que botão o utilizador carregou e realizar as ações correspondentes
     * ao mesmo.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {//verificar o que se faz no botão login
        if (v == botaoLogin) {
            autenticarUtilizador();
        }
    }

    /**
     * Este método irá finalizar a autenticação do utilizador.
     * Aqui serão feitas as verificações dos inputs e caso esteja tudo em ordem irá ser realizado o
     * pedido de início de sessão ao servidor e caso este responda com um resultado positivo, o
     * utilizador será autenticado, caso não, este receberá a mensagem correspondente ao erro que aconteceu.
     */
    private void autenticarUtilizador() {
        final String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        boolean verifica = true;

        if (email.length() <= 0) {
            emailInput.setError("Insira um email");
            verifica = false;
        } else if (email.equals("")) {
            emailInput.setError("Insira um email");
            verifica = false;
        }
        if (password.length() <= 0) {
            passwordInput.setError("Insira uma password");
            verifica = false;
        } else if (password.equals("")) {
            passwordInput.setError("Insira uma password");
            verifica = false;
        }
        if (!verifica) {
            return;
        }


        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(password.getBytes());
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


        progressDialog.setMessage("A iniciar sessão");
        progressDialog.show();

        String url = "http://" + getResources().getString(R.string.ip_e_porto) + "/resources/gereutilizadores/"
                + getResources().getString(R.string.rest_key) + "/autenticacao?email=" + email + "&password=" + passwordFinal;
        StringRequest strRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Gson gson = new Gson();
                String resposta = gson.fromJson(response, String.class);
                switch (resposta) {
                    case "1": {
                        SharedPreferences sp = getSharedPreferences("nextour", MODE_PRIVATE);
                        Toast.makeText(getApplicationContext(), "Autenticação feita com sucesso.", Toast.LENGTH_LONG).show();
                        sp.edit().putBoolean("logado", true).apply();
                        sp.edit().putString("email", email).apply();
                        startActivity(new Intent(PaginaLogin.this, PaginaInicial.class));
                        break;
                    }
                    case "2": {
                        Toast.makeText(getApplicationContext(), "Dados inválidos.", Toast.LENGTH_LONG).show();
                        break;
                    }
                    case "3": {
                        Toast.makeText(getApplicationContext(), "Insere o email e a password.", Toast.LENGTH_LONG).show();
                        break;
                    }
                    case "4": {
                        Toast.makeText(getApplicationContext(), "Erro na aplicação, tenta novamente mais tarde.", Toast.LENGTH_LONG).show();
                        break;
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.hide();
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", passwordFinal);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(strRequest);

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
                    startActivity(new Intent(PaginaLogin.this, PaginaPerfil.class));
                } else {
                    startActivity(new Intent(PaginaLogin.this, PaginaLogin.class));
                }
                break;
            }
            case R.id.paginaCriarGuia: {
                if (sp.getBoolean("logado", false)) {
                    startActivity(new Intent(PaginaLogin.this, PaginaCriarGuia.class));
                } else {
                    startActivity(new Intent(PaginaLogin.this, PaginaLogin.class));
                }
                break;
            }
            case R.id.paginaMeusGuias: {
                startActivity(new Intent(PaginaLogin.this, PaginaMeusGuias.class));
                break;
            }
            case R.id.paginaAjuda: {
                startActivity(new Intent(PaginaLogin.this, PaginaAjuda.class));
                break;
            }
            case R.id.paginaInicial: {
                startActivity(new Intent(PaginaLogin.this, PaginaInicial.class));
                break;
            }
            case R.id.paginaSair: {
                sp.edit().clear().commit();
                startActivity(new Intent(PaginaLogin.this, PaginaLogin.class));
                break;
            }
            case R.id.paginaLogin: {
                startActivity(new Intent(PaginaLogin.this, PaginaLogin.class));
                break;
            }
        }
        return true;
    }

}


