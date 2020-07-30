package com.example.nextour;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.nextour.bottom.BottomSheetCriarAudio;
import com.example.nextour.bottom.BottomSheetCriarPontoParagem;
import com.example.nextour.bottom.BottomSheetEscolherFotografia;
import com.example.nextour.classes.GereGuias;
import com.example.nextour.classes.GuiaTuristico;
import com.example.nextour.classes.Imagem;
import com.example.nextour.classes.Local;
import com.example.nextour.classes.PontoParagem;
import com.example.nextour.classes.PontoParagemPassagem;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Classe utilizada para criar a página onde vai ser criado/editado um guia turístico.
 */
public class PaginaCriarGuia extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Button.OnClickListener {

    private DrawerLayout aDrawerLayout;
    private ActionBarDrawerToggle aToggle;
    private NavigationView navView;
    private Context context;

    private LinearLayout linearLayoutFotos;
    private LinearLayout linearLayoutPontosParagem;

    private Button continuar;
    private Button cancelar;

    private EditText titulo;
    private EditText descricao;
    private EditText duracao;

    private ImageView botaoGravarAudio;

    private ImageView botaoEscolherLocalidade;
    private TextView local;

    private TextView adicionarFotoString;
    private TextView adicionarLocalString;

    private PontoParagemPassagem pontoCarregado = null;
    private ImageView fotoCarregada = null;
    private int contador = 0;
    private int contadorPontosCarregados = 0;

    private Bitmap bitmap;
    private ArrayList<ImageView> listaImagens = new ArrayList<>();
    private ArrayList<Bitmap> listaBitMaps = new ArrayList<>();

    private String pathAudio;


    private ArrayList<PontoParagemPassagem> listaPontosParagemPassagem = new ArrayList<>();

    private GuiaTuristico guiaTuristico = null;


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
        setContentView(R.layout.activity_pagina_criar_guia);
        context = getApplicationContext();

        criarDrawerLayout();

        navView = findViewById(R.id.menu_cair);
        navView.setNavigationItemSelectedListener(this);
        botoesDisponiveis();

        linearLayoutFotos = findViewById(R.id.containerFotos);

        linearLayoutPontosParagem = findViewById(R.id.containerLocais);

        continuar = findViewById(R.id.continuar_botao);
        continuar.setOnClickListener(this);

        cancelar = findViewById(R.id.cancelar_botao);
        cancelar.setOnClickListener(this);

        titulo = findViewById(R.id.tituloInput);
        descricao = findViewById(R.id.descricao_input);
        duracao = findViewById(R.id.duracaoInput);

        botaoGravarAudio = findViewById(R.id.audioBotao);
        botaoGravarAudio.setOnClickListener(this);

        botaoEscolherLocalidade = findViewById(R.id.localidadeBotao);
        botaoEscolherLocalidade.setOnClickListener(this);

        adicionarFotoString = findViewById(R.id.adicionarFotoString);

        adicionarLocalString = findViewById(R.id.adicionarLocalString);

        local = findViewById(R.id.localidadeInput);

        criarStringsClicaveis();

        SharedPreferences sharedPreferences = getSharedPreferences("nextour", MODE_PRIVATE);

        if (getIntent().hasExtra("guiaTuristico")) {
            guiaTuristico = new Gson().fromJson(getIntent().getStringExtra("guiaTuristico"), GuiaTuristico.class);
            carregarDadosEdicao(sharedPreferences);
        } else {
            carregarEndereco(sharedPreferences);
            carregarPontosParagem(sharedPreferences);
        }

        alterarCorBotoes();

    }


    /**
     * Este método é chamado quando a atividade é iniciada.
     * Aqui vai atualizar alguns dados da mesma.
     */
    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("nextour", MODE_PRIVATE);
        carregarEndereco(sharedPreferences);
        carregarAudio(sharedPreferences);
        carregarPontosParagem(sharedPreferences);
        alterarCorBotoes();
    }

    /**
     * Este método é chamado quando a atividade é destruída.
     * Aqui vai remover os dados guardados nas sharedpreferences para realizar a criação do guia.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        limparSharedPrefs();
    }

    /**
     * Este método serve para alterar as cores dos botões de escolher a localidade e criar/importar.
     */
    public void alterarCorBotoes() {
        if (!local.getText().toString().trim().equals(null) && !local.getText().toString().trim().equals("Escolher local ->")) {
            botaoEscolherLocalidade.setBackground(getResources().getDrawable(R.drawable.botao_redondo_verde));
        } else {
            botaoEscolherLocalidade.setBackground(getResources().getDrawable(R.drawable.botao_redondo));
        }
        if (pathAudio != null) {
            if (pathAudio.length() > 0) {
                botaoGravarAudio.setBackground(getResources().getDrawable(R.drawable.botao_redondo_verde));
            } else {
                botaoGravarAudio.setBackground(getResources().getDrawable(R.drawable.botao_redondo));
            }
        } else {
            botaoGravarAudio.setBackground(getResources().getDrawable(R.drawable.botao_redondo));
        }
    }

    /**
     * Este método vai carregar todos os dados para os componentes correpondentes para cada valor do
     * guia turístico que o utilizador pretenda editar.
     *
     * @param sharedPreferences
     */
    @SuppressLint("ApplySharedPref")
    private void carregarDadosEdicao(SharedPreferences sharedPreferences) {
        titulo.setText(guiaTuristico.getNome());
        local.setText(guiaTuristico.getLocal().getMorada());
        descricao.setText(guiaTuristico.getDescricao());
        duracao.setText(String.valueOf(guiaTuristico.getDuracao()));
        sharedPreferences.edit().putString("enderecoFinal",
                new Gson().toJson(
                        criarEndereco(
                                new LatLng(
                                        guiaTuristico.getLocal().getLatitude(),
                                        guiaTuristico.getLocal().getLongitude()
                                )
                        ), Address.class
                )).apply();
        sharedPreferences.edit().commit();
        for (PontoParagem pontoParagem : guiaTuristico.getListaPontosParagem()) {
            Address enderecoTemp = criarEndereco(new LatLng(pontoParagem.getLocal().getLatitude(), pontoParagem.getLocal().getLongitude()));
            PontoParagemPassagem pontoParagemPassagem = new PontoParagemPassagem(
                    pontoParagem.getId(),
                    pontoParagem.getNome(),
                    null,
                    null,
                    pontoParagem.getDescricao(),
                    enderecoTemp
            );
            listaPontosParagemPassagem.add(pontoParagemPassagem);
        }
        linearLayoutPontosParagem.removeAllViews();
        adicionarLocalContainer();
    }

    /**
     * Este método serve para criar um endereço a partir do latLng.
     * Isto é necessário para posteriormente poder alterar os endereços do guia e dos pontos de
     * paragem.
     *
     * @param latLng
     * @return
     */
    private Address criarEndereco(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses.get(0);
    }

    /**
     * Este método vai carregar os pontos de paragem das sharedpreferences para uma variável local.
     *
     * @param sharedPreferences
     */
    private void carregarPontosParagem(SharedPreferences sharedPreferences) {
        String pontosParagemString = sharedPreferences.getString("arrayPontosParagem", "");
        if (!pontosParagemString.equals("")) {
            linearLayoutPontosParagem.removeAllViews();
            Gson gson = new Gson();
            listaPontosParagemPassagem = gson.fromJson(pontosParagemString, new TypeToken<ArrayList<PontoParagemPassagem>>() {
            }.getType());
            adicionarLocalContainer();
        }
    }

    /**
     * Este método vai carregar o áudio do guia das sharedpreferences para uma variável local.
     *
     * @param sharedPreferences
     */
    private void carregarAudio(SharedPreferences sharedPreferences) {
        String pathAudioLocal = sharedPreferences.getString("audioFinal", "");
        if (!pathAudioLocal.equals("")) {
            pathAudio = pathAudioLocal;
        } else {
            pathAudio = null;
        }
    }

    /**
     * Este método vai carregar o endereço do guia das sharedpreferences para uma variável local.
     *
     * @param sharedPreferences
     */
    private void carregarEndereco(SharedPreferences sharedPreferences) {
        String enderecoFinal = sharedPreferences.getString("enderecoFinal", "");
        if (!enderecoFinal.equals("")) {
            Gson gson = new Gson();
            Address address = gson.fromJson(enderecoFinal, Address.class);
            local.setText(address.getAddressLine(0));
        }
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
                    startActivity(new Intent(PaginaCriarGuia.this, PaginaPerfil.class));
                } else {
                    startActivity(new Intent(PaginaCriarGuia.this, PaginaLogin.class));
                }
                break;
            }
            case R.id.paginaCriarGuia: {
                if (sp.getBoolean("logado", false)) {
                    startActivity(new Intent(PaginaCriarGuia.this, PaginaCriarGuia.class));
                } else {
                    startActivity(new Intent(PaginaCriarGuia.this, PaginaLogin.class));
                }
                break;
            }
            case R.id.paginaMeusGuias: {
                startActivity(new Intent(PaginaCriarGuia.this, PaginaMeusGuias.class));
                break;
            }
            case R.id.paginaAjuda: {
                startActivity(new Intent(PaginaCriarGuia.this, PaginaAjuda.class));
                break;
            }
            case R.id.paginaInicial: {
                startActivity(new Intent(PaginaCriarGuia.this, PaginaInicial.class));
                break;
            }
            case R.id.paginaSair: {
                sp.edit().clear().commit();
                startActivity(new Intent(PaginaCriarGuia.this, PaginaLogin.class));
                break;
            }
            case R.id.paginaLogin: {
                startActivity(new Intent(PaginaCriarGuia.this, PaginaLogin.class));
                break;
            }
        }
        return true;
    }

    /**
     * Este método irá inicializar as strings que permitirão a adição de fotografias ao guia e a
     * criação de pontos de paragem associados a este guia turístico.
     */
    private void criarStringsClicaveis() {//função para criar uma string underlined e mudar as cores da mesma e o que acontece quando é clicada

        ClickableSpan linkClick = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (view == adicionarFotoString) {
                    if (contador < 4) {
                        escolherFotografia();
                    } else {
                        Toast.makeText(context, "Alcance de fotos atingido", Toast.LENGTH_SHORT).show();
                    }
                } else if (view == adicionarLocalString) {
                    if (listaPontosParagemPassagem.size() < 10) {
                        enviarPontosParagem();
                        startActivity(new Intent(PaginaCriarGuia.this, CaixaCriarPontoParagem.class));
                    }else{
                        Toast.makeText(context, "Alcance de pontos de paragem atingido", Toast.LENGTH_SHORT).show();
                    }
                }
                view.invalidate();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                if (adicionarFotoString.isPressed()) {
                    ds.setColor(getResources().getColor(R.color.azulClaro));
                    adicionarFotoString.invalidate();
                } else {
                    if (adicionarLocalString.isPressed()) {
                        ds.setColor(getResources().getColor(R.color.azulClaro));
                        adicionarLocalString.invalidate();
                    } else {
                        ds.setColor(getResources().getColor(R.color.azulEscuro));
                    }
                }
            }
        };
        //inserção de uma string para adicionar fotos
        adicionarFotoString.setHighlightColor(Color.TRANSPARENT);
        Spannable spannableFoto = new SpannableString("Adicionar fotografia");
        spannableFoto.setSpan(linkClick, 0, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        adicionarFotoString.setText(spannableFoto, TextView.BufferType.SPANNABLE);
        adicionarFotoString.setMovementMethod(LinkMovementMethod.getInstance());


        //inserção de uma string para adicionar locais
        adicionarLocalString.setHighlightColor(Color.TRANSPARENT);
        Spannable spannableLocal = new SpannableString("Adicionar ponto paragem");
        spannableLocal.setSpan(linkClick, 0, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        adicionarLocalString.setText(spannableLocal, TextView.BufferType.SPANNABLE);
        adicionarLocalString.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Este método irá carregar os pontos de paragem existentes na variável local, quando for para
     * editar, para as sharedpreferences para posteriormente poder utilizar noutras páginas.
     */
    private void enviarPontosParagem() {
        SharedPreferences sharedPreferences = getSharedPreferences("nextour", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(listaPontosParagemPassagem, new TypeToken<ArrayList<PontoParagemPassagem>>() {
        }.getType());
        prefsEditor.putString("arrayPontosParagem", json);
        prefsEditor.commit();
    }

    /**
     * Este método vai criar layouts clicáveis para cada ponto de paragem para que o utilizador possa editar
     * quando quiser um desses pontos.
     */
    private void adicionarLocalContainer() {
        if (listaPontosParagemPassagem.size() > 0) {
            for (int x = 0; x < listaPontosParagemPassagem.size(); x++) {
                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setGravity(Gravity.CENTER_VERTICAL);

                ImageView novoLocal = new ImageView(context);
                String imagemCodificada = null;
                if (listaPontosParagemPassagem.get(x).getListaImagens() != null) {
                    if (listaPontosParagemPassagem.get(x).getListaImagens().size() > 0) {
                        imagemCodificada = listaPontosParagemPassagem.get(x).getListaImagens().get(0).getUrl();
                        novoLocal.setImageBitmap(conversaoStringImagem(imagemCodificada));
                    } else {
                        novoLocal.setImageResource(R.drawable.default_ponto_paragem);
                    }
                } else {
                    novoLocal.setImageResource(R.drawable.default_ponto_paragem);
                }
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(146, 146);
                layoutParams.setMargins(3, 0, 0, 0);
                novoLocal.setLayoutParams(layoutParams);
                final int valorPassar = (int) listaPontosParagemPassagem.get(x).getId();

                linearLayout.addView(novoLocal);
                novoLocal.setVisibility(View.VISIBLE);

                TextView titulo = new TextView(context);
                titulo.setText(listaPontosParagemPassagem.get(x).getNome());

                linearLayout.addView(titulo);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(10, 0, 0, 0);

                titulo.setLayoutParams(params);
                titulo.setVisibility(View.VISIBLE);
                titulo.setTextSize(15);
                titulo.setTextColor(getResources().getColor(R.color.azulClaro));
                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enviarPontosParagem();
                        BottomSheetCriarPontoParagem bottomSheetCriarPontoParagem = new BottomSheetCriarPontoParagem(valorPassar, getApplicationContext());
                        bottomSheetCriarPontoParagem.show(getSupportFragmentManager(), "Ponto paragem");
                    }
                });
                linearLayoutPontosParagem.addView(linearLayout);
            }
        }
    }

    /**
     * Este método vai remover o ponto de paragem que esteja no lugar i do layout e da lista dos pontos de paragem.
     *
     * @param i
     */
    public void removerPontoParagem(int i) {
        for (int x = 0; x < listaPontosParagemPassagem.size(); x++) {
            if (listaPontosParagemPassagem.get(x).getId() == i) {
                linearLayoutPontosParagem.removeViewAt(x);
                listaPontosParagemPassagem.remove(x);
            }
        }
    }

    /**
     * Este método vai converter uma string num bitmap.
     *
     * @param stringImagem
     * @return
     */
    public Bitmap conversaoStringImagem(String stringImagem) {
        byte[] decodedString = Base64.decode(stringImagem, Base64.NO_WRAP);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    /**
     * Este método vai adicionar as fotografias que existem no guia turístico ao componente que
     * estas pertencem.
     *
     * @param imageView
     * @param width
     * @param height
     */
    private void adicionarView(ImageView imageView, int width, int height) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        layoutParams.setMargins(3, 0, 0, 0);
        imageView.setLayoutParams(layoutParams);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetEscolherFotografia bottomSheetEscolherFotografia = new BottomSheetEscolherFotografia(imageView, getApplicationContext(), true);
                bottomSheetEscolherFotografia.show(getSupportFragmentManager(), "Fotografia");
            }
        });
        linearLayoutFotos.addView(imageView);
    }

    /**
     * Este método vai indicar qual foi a fotografia carregada pelo utilizador para posteriormente
     * saber qual é necessário modificar.
     *
     * @param i
     */
    public void setFotoCarregada(ImageView i) {
        fotoCarregada = i;
        escolherFotografia();
    }

    /**
     * Este método vai remover uma fotografia que o utilizador tenha escolhido.
     *
     * @param i
     */
    public void removerFotografia(ImageView i) {
        for (int x = 0; x < linearLayoutFotos.getChildCount(); x++) {
            if (linearLayoutFotos.getChildAt(x) == i) {
                linearLayoutFotos.removeViewAt(x);
                listaBitMaps.remove(x);
                listaImagens.remove(x);
                contador--;
            }
        }
    }


    /**
     * Este método vai abrir uma janela para o utilizador escolher uma fotografia.
     */
    public void escolherFotografia() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    /**
     * Este método vai realizar as ações neessárias após o utilizador ter escolhido uma foto no método escolherFotografia().
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri path = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                if (fotoCarregada == null) {
                    listaBitMaps.add(bitmap);
                    ImageView novaFoto = new ImageView(context);
                    novaFoto.setImageBitmap(bitmap);
                    adicionarView(novaFoto, 146, 146);
                    listaImagens.add(novaFoto);
                    contador++;
                } else {
                    for (int x = 0; x < listaImagens.size(); x++) {
                        if (fotoCarregada == listaImagens.get(x)) {
                            listaImagens.get(x).setImageBitmap(bitmap);
                            listaImagens.get(x).setVisibility(View.VISIBLE);
                            listaBitMaps.set(x, bitmap);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Este método irá verificar em que botão o utilizador carregou e realizar as ações correspondentes
     * ao mesmo.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v == botaoEscolherLocalidade) {
            startActivity(new Intent(PaginaCriarGuia.this, PaginaEscolherLocalidade.class).putExtra("nomeSharedPref", "enderecoFinal"));
        }
        if (v == botaoGravarAudio) {
            BottomSheetCriarAudio bottomSheetCriarAudio = new BottomSheetCriarAudio(pathAudio, null, getApplicationContext(), this,
                    this, null);
            bottomSheetCriarAudio.show(getSupportFragmentManager(), "Audio");
        }
        if (v == cancelar) {
            limparSharedPrefs();
            startActivity(new Intent(PaginaCriarGuia.this, PaginaInicial.class));
        }
        if (v == continuar) {
            if (verificarInputs()) {
                enviarDados();
                limparSharedPrefs();
                startActivity(new Intent(PaginaCriarGuia.this, PaginaInicial.class));
            }
        }
    }

    /**
     * Este método serve para finalizar a criação do guia turístico.
     * Aqui serão associados os valores nos inputs aos dados do guia turístico e posteriormente
     * será efetuado um pedido ao servidor para inserir/editar o guia em questão.
     */
    private void enviarDados() {
        //obtencao do adress
        SharedPreferences sharedPreferences = getSharedPreferences("nextour", MODE_PRIVATE);
        String enderecoFinal = sharedPreferences.getString("enderecoFinal", "");
        Address address = null;
        if (!enderecoFinal.equals("")) {
            Gson gson = new Gson();
            address = gson.fromJson(enderecoFinal, Address.class);
        }

        //obtencao imagens
        ArrayList<Imagem> listaImagens = new ArrayList<Imagem>();
        for (int x = 0; x < listaBitMaps.size(); x++) {
            if (listaBitMaps.get(x) != null) {
                listaImagens.add(new Imagem(0, conversaoImagemString(listaBitMaps.get(x))));
            }
        }

        //obtencao pontos paragem e alteracao dos audios de cada ponto de paragem
        ArrayList<PontoParagem> listaPontosParagemFinal = new ArrayList<PontoParagem>();
        for (PontoParagemPassagem pontoParagemPassagem : listaPontosParagemPassagem) {
            if (pontoParagemPassagem != null) {
                Address enderecoPassagem = pontoParagemPassagem.getAddress();
                PontoParagem pontoParagemTemp = new PontoParagem(pontoParagemPassagem.getId(),
                        pontoParagemPassagem.getNome(),
                        null,
                        pontoParagemPassagem.getListaImagens(),
                        pontoParagemPassagem.getDescricao(),
                        new Local(0,
                                enderecoPassagem.getAddressLine(0),
                                enderecoPassagem.getAdminArea(),
                                enderecoPassagem.getLatitude(),
                                enderecoPassagem.getLongitude()
                        )
                );
                pontoParagemTemp.setAudio(encodeAudio(pontoParagemPassagem.getAudio()));
                listaPontosParagemFinal.add(pontoParagemTemp);
            }
        }

        long idGuia = 0;
        if (guiaTuristico != null) {
            idGuia = guiaTuristico.getId();
        }

        GuiaTuristico guiaTuristicoTemp = new GuiaTuristico(
                idGuia,
                0,
                new Local(
                        0,
                        address.getAddressLine(0),
                        address.getAdminArea(),
                        address.getLatitude(),
                        address.getLongitude()
                ),
                listaImagens,
                titulo.getText().toString(),
                descricao.getText().toString(),
                encodeAudio(pathAudio),
                Long.parseLong(duracao.getText().toString()),
                listaPontosParagemFinal
        );
        Gson gson = new Gson();
        String json = gson.toJson(guiaTuristicoTemp);
        if (guiaTuristico != null) {
            new GereGuias().editarGuiaTuristico(json, getApplicationContext(), this);
        } else {
            new GereGuias().inserirGuiaTuristico(json, getApplicationContext(), this);
        }
    }

    /**
     * Este método vai codificar um ficheiro de áudio através do Base64.
     *
     * @param selectedPath
     * @return
     */
    public String encodeAudio(String selectedPath) {
        byte[] audioBytes;
        try {

            // Just to check file size.. Its is correct i-e; Not Zero
            File audioFile = new File(selectedPath);
            long fileSize = audioFile.length();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(new File(selectedPath));
            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fis.read(buf)))
                baos.write(buf, 0, n);
            audioBytes = baos.toByteArray();

            // Here goes the Base64 string
            return Base64.encodeToString(audioBytes, Base64.NO_WRAP);

        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Este método vai converter um bitmap numa string encoded em Base64.
     *
     * @param bitmap
     * @return
     */
    public String conversaoImagemString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream);
        byte[] b = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(b, Base64.NO_WRAP);
    }

    /**
     * Este método vai remover os dados guardados nas sharedpreferences para realizar a criação do guia
     * ou saída do mesmo.
     */
    private void limparSharedPrefs() {
        SharedPreferences sp = getSharedPreferences("nextour", MODE_PRIVATE);
        String email = sp.getString("email", "");
        sp.edit().clear().commit();
        sp.edit().commit();
        sp = getSharedPreferences("nextour", MODE_PRIVATE);
        sp.edit().putBoolean("logado", true).apply();
        sp.edit().putString("email", email).apply();
        sp.edit().commit();
    }


    /**
     * Este método vai verificar os inputs necessários que o utilizador tem de inserir e vai retornar
     * true caso esteja tudo em ordem e false caso seja preciso algo.
     *
     * @return
     */
    private boolean verificarInputs() {
        boolean verifica = true;
        if (titulo.getText().toString().trim().equals(null) || titulo.getText().toString().trim().equals("")) {
            verifica = false;
            titulo.setError("Insira um título");
        } else {
            titulo.setError(null);
        }
        if (descricao.getText().toString().trim().equals(null) || descricao.getText().toString().trim().equals("")) {
            verifica = false;
            descricao.setError("Insira uma descrição");
        } else {
            descricao.setError(null);
        }
        if (duracao.getText().toString().trim().equals(null) || duracao.getText().toString().trim().equals("")) {
            verifica = false;
            duracao.setError("Insira uma duração");
        } else {
            duracao.setError(null);
        }
        if (local.getText().toString().trim().equals(null) || local.getText().toString().trim().equals("Escolher local ->")) {
            verifica = false;
            local.setError("Escolha um local");
        } else {
            local.setError(null);
        }
        return verifica;
    }
}

