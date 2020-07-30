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
import android.util.Patterns;
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
import com.android.volley.NetworkResponse;
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
 * ao utilizador registar uma conta.
 */
public class PaginaRegistar extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    EditText nomeInput, emailInput, passwordInput, passwordRepetidaInput;
    Button botaoRegistar;
    TextView termosString, loginString;
    private ProgressDialog progressDialog;
    DrawerLayout aDrawerLayout;
    ActionBarDrawerToggle aToggle;
    boolean valorFinal = true;
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
        setContentView(R.layout.activity_main);

        termosString = findViewById(R.id.termosTextView);

        loginString = findViewById(R.id.loginTextView);

        nomeInput = findViewById(R.id.nomeInput);

        emailInput = findViewById(R.id.emailInput);

        passwordInput = findViewById(R.id.passwordInput);

        passwordRepetidaInput = findViewById(R.id.passwordRepetidaInput);

        criarDrawerLayout();

        //função para criar uma string underlined e mudar as cores da mesma e o que acontece quando é clicada
        criarStringsClicaveis();

        progressDialog = new ProgressDialog(this);

        botaoRegistar = findViewById(R.id.botaoRegistar);
        botaoRegistar.setOnClickListener(this);

        //associating the listener to the navigation menu
        navView = findViewById(R.id.menu_cair);
        navView.setNavigationItemSelectedListener(this);
        botoesDisponiveis();
    }

    /**
     * Este método irá inicializar as strings que permitirão a passagem para a página de login ou às
     * políticas de privacidade.
     */
    private void criarStringsClicaveis() {
        ClickableSpan linkClick = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (view == termosString) {
                    startActivity(new Intent(PaginaRegistar.this, PaginaAjuda.class));
                }
                if (view == loginString) {
                    startActivity(new Intent(PaginaRegistar.this, PaginaLogin.class));
                }
                view.invalidate();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                if (termosString.isPressed()) {
                    ds.setColor(getResources().getColor(R.color.azulClaro));
                    termosString.invalidate();
                } else {
                    if (loginString.isPressed()) {
                        ds.setColor(getResources().getColor(R.color.azulClaro));
                        loginString.invalidate();
                    } else {
                        ds.setColor(getResources().getColor(R.color.azulEscuro));
                    }
                }
            }
        };

        //inserção de uma string para os termos
        termosString.setHighlightColor(Color.TRANSPARENT);
        Spannable stringTermos = new SpannableString("Ao registares uma conta, concordas com os nossos Termos de uso e Políticas de privacidade.");
        stringTermos.setSpan(linkClick, 49, 89, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        termosString.setText(stringTermos, TextView.BufferType.SPANNABLE);
        termosString.setMovementMethod(LinkMovementMethod.getInstance());

        //inserção de uma string para o login
        loginString.setHighlightColor(Color.TRANSPARENT);
        Spannable stringLogin = new SpannableString("Já tens conta?\nFaz aqui o teu login");
        stringLogin.setSpan(linkClick, 15, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        loginString.setText(stringLogin, TextView.BufferType.SPANNABLE);
        loginString.setMovementMethod(LinkMovementMethod.getInstance());
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
     * Este método irá verificar em que botão o utilizador carregou e realizar as ações correspondentes
     * ao mesmo.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v == botaoRegistar) {
            registarUtilizador();
        }
    }

    /**
     * Este método irá finalizar o registo do utilizador.
     * Aqui serão feitas as verificações dos inputs e caso esteja tudo em ordem irá ser realizado o
     * pedido de registo de uma conta ao servidor e caso este responda com um resultado positivo,
     * significa que o utilizador foi registado e será enviado para a página de login.
     * Caso não, este receberá a mensagem correspondente ao erro que aconteceu.
     */
    private void registarUtilizador() {

        progressDialog.setMessage("A verificar dados");
        progressDialog.show();

        valorFinal = true;

        //verificação do nome
        String nome = nomeInput.getText().toString().trim();
        if (nome.isEmpty()) {
            nomeInput.setError("Insere um nome!");
            valorFinal = false;
        } else {
            nomeInput.setError(null);
        }

        //verificação da password
        String password = passwordInput.getText().toString().trim();
        String password2 = passwordRepetidaInput.getText().toString().trim();

        if (password2.isEmpty()) {
            passwordRepetidaInput.setError("Repete a password!");
            valorFinal = false;
        } else {
            if (!password.equals(password2)) {
                passwordRepetidaInput.setError("Passwords não combinam!");
                valorFinal = false;
            } else {
                passwordRepetidaInput.setError(null);
            }
        }

        if (password.isEmpty()) {
            passwordInput.setError("Insere uma password!");
            valorFinal = false;
        } else {
            boolean letra = false;
            boolean numero = false;
            for (int i = 0; i < password.length(); i++) {
                if (!letra) {
                    if (Character.isAlphabetic(password.charAt(i))) {
                        letra = true;
                    }
                    ;
                }
                if (!numero) {
                    if (Character.isDigit(password.charAt(i))) {
                        numero = true;
                    }
                    ;
                }
            }
            if (!letra || !numero) {
                passwordInput.setError("A password precisa de pelo menos uma letra e um número.");
                valorFinal = false;
            } else {
                if (password.length() < 6) {
                    passwordInput.setError("A password precisa de pelo menos 6 caracteres.");
                    valorFinal = false;
                } else {
                    passwordInput.setError(null);
                }
            }
        }


        //verificações de email
        final String email = emailInput.getText().toString().trim();
        if (email.isEmpty()) {
            emailInput.setError("Insere um email!");
            valorFinal = false;
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Insere um email correto!");
                valorFinal = false;
            } else {//verificação de email único

                String url = "http://" + getResources().getString(R.string.ip_e_porto) + "/resources/gereutilizadores/"
                        + getResources().getString(R.string.rest_key) + "/email/unique?email=" + email;
                StringRequest strRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Gson gson = new Gson();
                        String resposta = gson.fromJson(response, String.class);
                        switch (resposta) {
                            case "1": {
                                emailInput.setError("Email existente, insere outro.");
                                valorFinal = false;
                                break;
                            }
                            case "2": {
                                emailInput.setError(null);
                                break;
                            }
                            case "3": {
                                emailInput.setError("Erro da aplicação, tenta novamente mais tarde.");
                                valorFinal = false;
                                break;
                            }
                        }
                        if (valorFinal) {

                            final String nome = nomeInput.getText().toString().trim();
                            final String email = emailInput.getText().toString().trim();
                            String password = passwordInput.getText().toString().trim();

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

                            progressDialog.setMessage("A registar a conta");
                            progressDialog.show();

                            String url = "http://" + getResources().getString(R.string.ip_e_porto) + "/resources/gereutilizadores/" + getResources().getString(R.string.rest_key) + "/utilizador";
                            StringRequest strRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    progressDialog.dismiss();
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
                                    params.put("nome", nome);
                                    params.put("email", email);
                                    params.put("password", passwordFinal);
                                    return params;
                                }
                                @Override
                                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                                    switch (response.statusCode) {
                                        case 200: {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(), "Conta registada com sucesso", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                            startActivity(new Intent(getApplicationContext(), PaginaLogin.class));
                                            break;
                                        }
                                        default: {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(), "Ocorreu um erro, tente novamente mais tarde", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                            break;
                                        }
                                    }
                                    return super.parseNetworkResponse(response);
                                }
                            };

                            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                            requestQueue.add(strRequest);

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
                        return params;
                    }
                };

                RequestQueue requestQueue = Volley.newRequestQueue(this);
                requestQueue.add(strRequest);
            }
        }
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
                    startActivity(new Intent(PaginaRegistar.this, PaginaPerfil.class));
                } else {
                    startActivity(new Intent(PaginaRegistar.this, PaginaLogin.class));
                }
                break;
            }
            case R.id.paginaCriarGuia: {
                if (sp.getBoolean("logado", false)) {
                    startActivity(new Intent(PaginaRegistar.this, PaginaCriarGuia.class));
                } else {
                    startActivity(new Intent(PaginaRegistar.this, PaginaLogin.class));
                }
                break;
            }
            case R.id.paginaMeusGuias: {
                startActivity(new Intent(PaginaRegistar.this, PaginaMeusGuias.class));
                break;
            }
            case R.id.paginaAjuda: {
                startActivity(new Intent(PaginaRegistar.this, PaginaAjuda.class));
                break;
            }
            case R.id.paginaInicial: {
                startActivity(new Intent(PaginaRegistar.this, PaginaInicial.class));
                break;
            }
            case R.id.paginaSair: {
                sp.edit().clear().commit();
                startActivity(new Intent(PaginaRegistar.this, PaginaLogin.class));
                break;
            }
            case R.id.paginaLogin: {
                startActivity(new Intent(PaginaRegistar.this, PaginaLogin.class));
                break;
            }
        }
        return true;
    }
}


