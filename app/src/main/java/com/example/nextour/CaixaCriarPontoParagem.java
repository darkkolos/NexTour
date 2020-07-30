package com.example.nextour;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;

import com.example.nextour.bottom.BottomSheetCriarAudio;
import com.example.nextour.bottom.BottomSheetEscolherFotografia;
import com.example.nextour.classes.Imagem;
import com.example.nextour.classes.PontoParagemPassagem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Classe utilizada para criar a página onde vai ser criado/editado um ponto de paragem.
 */
public class CaixaCriarPontoParagem extends AppCompatActivity implements Button.OnClickListener, LifecycleObserver {


    private static final String TAG = "AAAAAAAAAAAAAAAA";

    private LinearLayout linearLayoutFotos;
    private ImageView botaoEscolherLocalidade;
    private ImageView botaoAdicionarAudio;
    private TextView adicionarFotoString;
    private Context context;
    private ImageView fotoCarregada = null;
    private int contador = 0;
    private Bitmap bitmap;
    private ArrayList<ImageView> listaImagens = new ArrayList<ImageView>();
    private ArrayList<Bitmap> listaBitMaps = new ArrayList<Bitmap>();
    private TextView local;

    private EditText inputTitulo;
    private EditText inputDescricao;

    private Button adicionar;
    private Button cancelar;

    private ArrayList<PontoParagemPassagem> listaPontosParagemPassagem = new ArrayList<>();

    private int pontoCarregado = 0;

    private boolean editar = false;

    private PontoParagemPassagem pontoParagemTemporario = null;

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
        setContentView(R.layout.activity_caixa_criar_ponto_paragem);
        context = getApplicationContext();

        pontoCarregado = getIntent().getIntExtra("pontoCarregado", 0);
        editar = getIntent().getBooleanExtra("editar", false);

        adicionar = findViewById(R.id.adicionar_botao);
        adicionar.setOnClickListener(this);

        cancelar = findViewById(R.id.cancelar_botao);
        cancelar.setOnClickListener(this);

        linearLayoutFotos = findViewById(R.id.containerFotos);

        botaoEscolherLocalidade = findViewById(R.id.localidadeBotao);
        botaoEscolherLocalidade.setOnClickListener(this);

        botaoAdicionarAudio = findViewById(R.id.audioBotao);
        botaoAdicionarAudio.setOnClickListener(this);

        adicionarFotoString = findViewById(R.id.adicionarFotoString);

        local = findViewById(R.id.localidadeInput);

        inputTitulo = findViewById(R.id.tituloInput);

        inputDescricao = findViewById(R.id.descricao_input);

        criarStringsClicaveis();

        SharedPreferences sharedPreferences = getSharedPreferences("nextour", MODE_PRIVATE);
        carregarPontosParagem(sharedPreferences);

        if (editar) {
            Log.e(TAG, "onCreate: entrou no editarrr");
            adicionar.setText("Guardar");
            for (PontoParagemPassagem pontoParagemPassagem : listaPontosParagemPassagem) {
                if (pontoParagemPassagem.getId() == pontoCarregado) {
                    pontoParagemTemporario = pontoParagemPassagem;
                }
            }
            carregarConteudo();
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
        carregarPontoParagemTemporario(sharedPreferences);
        if (pontoParagemTemporario != null) {
            Log.e(TAG, "onResume: atualizando");
            atualizarConteudo();
        }
        alterarCorBotoes();
    }

    /**
     * Este método serve para alterar as cores dos botões de escolher a localidade e criar/importar.
     */
    public void alterarCorBotoes() {
        Log.e(TAG, "onClick: alterar botoes do pp");
        if (!local.getText().toString().trim().equals(null)
                && !local.getText().toString().trim().equals("Escolher local ->")
                && !local.getText().toString().trim().equals("")) {
            botaoEscolherLocalidade.setBackground(getResources().getDrawable(R.drawable.botao_redondo_verde));
        } else {
            botaoEscolherLocalidade.setBackground(getResources().getDrawable(R.drawable.botao_redondo));
        }
        if (pontoParagemTemporario != null) {
            if (pontoParagemTemporario.getAudio() != null) {
                if (pontoParagemTemporario.getAudio().length() > 0) {
                    botaoAdicionarAudio.setBackground(getResources().getDrawable(R.drawable.botao_redondo_verde));
                } else {
                    botaoAdicionarAudio.setBackground(getResources().getDrawable(R.drawable.botao_redondo));
                }
            } else {
                botaoAdicionarAudio.setBackground(getResources().getDrawable(R.drawable.botao_redondo));
            }
        } else {
            botaoAdicionarAudio.setBackground(getResources().getDrawable(R.drawable.botao_redondo));
        }
    }

    /**
     * Este método vai carregar os pontos de paragem das sharedpreferences para uma variável local.
     *
     * @param sharedPreferences
     */
    private void carregarPontosParagem(SharedPreferences sharedPreferences) {
        String pontosParagemString = sharedPreferences.getString("arrayPontosParagem", "");
        if (!pontosParagemString.equals("")) {
            Gson gson = new Gson();
            listaPontosParagemPassagem = gson.fromJson(pontosParagemString, new TypeToken<ArrayList<PontoParagemPassagem>>() {
            }.getType());
        }
    }

    /**
     * Este método vai carregar o ponto de paragem temporário das sharedpreferences que será editado.
     *
     * @param sharedPreferences
     * @return
     */
    private boolean carregarPontoParagemTemporario(SharedPreferences sharedPreferences) {
        String pontoParagemTemporarioString = sharedPreferences.getString("pontoParagemTemporario", "");
        if (!pontoParagemTemporarioString.equals("")) {
            Gson gson = new Gson();
            pontoParagemTemporario = gson.fromJson(pontoParagemTemporarioString, PontoParagemPassagem.class);
            return true;
        }
        return false;
    }

    /**
     * Este método irá inicializar a string que permitirá a inserção de imagens no ponto de paragem.
     */
    private void criarStringsClicaveis() {//função para criar uma string underlined e mudar as cores da mesma e o que acontece quando é clicada
        ClickableSpan linkClick = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (view == adicionarFotoString) {
                    if (contador < 4) {
                        fotoCarregada = null;
                        escolherFotografia();
                    } else {
                        Toast.makeText(context, "Alcance máximo de fotos atingido", Toast.LENGTH_SHORT).show();
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
                    ds.setColor(getResources().getColor(R.color.azulEscuro));
                }
            }
        };
        //inserção de uma string para adicionar fotos
        adicionarFotoString.setHighlightColor(Color.TRANSPARENT);
        Spannable spannableFoto = new SpannableString("Adicionar fotografia");
        spannableFoto.setSpan(linkClick, 0, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        adicionarFotoString.setText(spannableFoto, TextView.BufferType.SPANNABLE);
        adicionarFotoString.setMovementMethod(LinkMovementMethod.getInstance());

    }

    /**
     * Este método éutilizado para carregar o conteúdo do ponto paragem a ser editado nos respetivos
     * componentes.
     */
    private void carregarConteudo() {
        if (pontoParagemTemporario != null) {
            linearLayoutFotos.removeAllViews();
            String endereco = null;
            if (pontoParagemTemporario.getAddress() != null) {
                endereco = pontoParagemTemporario.getAddress().getAddressLine(0);
            }
            local.setText(endereco);
            inputTitulo.setText(pontoParagemTemporario.getNome());
            inputDescricao.setText(pontoParagemTemporario.getDescricao());
            listaBitMaps = new ArrayList<>();
            listaImagens = new ArrayList<>();
            ArrayList<Imagem> listaUrls = pontoParagemTemporario.getListaImagens();
            if (listaUrls != null) {
                for (int x = 0; x < listaUrls.size(); x++) {
                    if (listaUrls.get(x) != null) {
                        ImageView fotos = new ImageView(context);
                        String imagemCodificada = listaUrls.get(x).getUrl();
                        Bitmap bitmap = conversaoStringImagem(imagemCodificada);
                        fotos.setImageBitmap(bitmap);
                        adicionarView(fotos, 146, 146);
                        listaBitMaps.add(bitmap);
                        listaImagens.add(fotos);
                        fotos.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    /**
     * Este método é utilizado após voltar da escolha da localidade do ponto de paragem para alterar
     * o valor do componente corresponde à mesma.
     */
    private void atualizarConteudo() {
        if (pontoParagemTemporario != null) {
            if (pontoParagemTemporario.getAddress() != null) {
                local.setText(pontoParagemTemporario.getAddress().getAddressLine(0));
            }
        }
    }

    /**
     * Este método vai converter uma String num bitmap.
     *
     * @param stringImagem
     * @return
     */
    public Bitmap conversaoStringImagem(String stringImagem) {
        byte[] decodedString = Base64.decode(stringImagem, Base64.NO_WRAP);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    /**
     * Este método vai adicionar as fotografias que existem no ponto de paragem ao componente que estas pertencem.
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
                BottomSheetEscolherFotografia bottomSheetEscolherFotografia = new BottomSheetEscolherFotografia(imageView, getApplicationContext(), false);
                bottomSheetEscolherFotografia.show(getSupportFragmentManager(), "Fotografia");
            }
        });
        linearLayoutFotos.addView(imageView);
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
            inserirPontoParagemTemporario();
            Intent intent = new Intent(getApplicationContext(), PaginaEscolherLocalidade.class);
            intent.removeExtra("nomeSharedPref");
            startActivity(intent);
        } else if (cancelar.equals(v)) {
            removerPontoParagemTemporario();
            finish();
        } else if (adicionar.equals(v)) {
            if (verificarInputs()) {
                finalizarAdicao();
                removerPontoParagemTemporario();
                finish();
            }
        } else if (botaoAdicionarAudio.equals(v)) {
            inserirPontoParagemTemporario();
            BottomSheetCriarAudio bottomSheetCriarAudio = new BottomSheetCriarAudio(null, pontoParagemTemporario, getApplicationContext(),
                    this, null, this);
            bottomSheetCriarAudio.show(getSupportFragmentManager(), "Audio");
        }
    }

    /**
     * Este método é utilizado para inserir o ponto de paragem temporário nas sharedpreferences.
     */
    private void inserirPontoParagemTemporario() {
        ArrayList<Imagem> conjuntoImagens = new ArrayList<Imagem>();
        for (Bitmap bitmap : listaBitMaps) {
            conjuntoImagens.add(new Imagem(0, conversaoImagemString(bitmap)));
        }

        String audio = null;
        Address address = null;
        if (pontoParagemTemporario != null) {
            audio = pontoParagemTemporario.getAudio();
            address = pontoParagemTemporario.getAddress();
        }

        PontoParagemPassagem pontoParagemPassagemInsercao = new PontoParagemPassagem(
                listaPontosParagemPassagem.size(),
                inputTitulo.getText().toString().trim(),
                audio,
                conjuntoImagens,
                inputDescricao.getText().toString().trim(),
                address);

        pontoParagemTemporario = pontoParagemPassagemInsercao;
        Gson gson = new Gson();
        String json = gson.toJson(pontoParagemPassagemInsercao, PontoParagemPassagem.class);
        SharedPreferences sharedPreferences = getSharedPreferences("nextour", MODE_PRIVATE);
        sharedPreferences.edit().putString("pontoParagemTemporario", json).commit();
    }

    /**
     * Este método é utilizado para remover o ponto de paragem temporário das sharedpreferences.
     */
    private void removerPontoParagemTemporario() {
        SharedPreferences sharedPreferences = getSharedPreferences("nextour", MODE_PRIVATE);
        sharedPreferences.edit().remove("pontoParagemTemporario").commit();
    }

    /**
     * Este método vai realizar a adição do ponto de paragem ao guia turístico a ser criado/editado.
     */
    private void finalizarAdicao() {
        ArrayList<Imagem> conjuntoImagens = new ArrayList<Imagem>();

        for (Bitmap bitmap : listaBitMaps) {
            conjuntoImagens.add(new Imagem(0, conversaoImagemString(bitmap)));
        }

        PontoParagemPassagem pontoParagemPassagemFinal = null;

        if (editar) {
            pontoParagemPassagemFinal = new PontoParagemPassagem(
                    pontoCarregado,
                    inputTitulo.getText().toString().trim(),
                    pontoParagemTemporario.getAudio(),
                    conjuntoImagens,
                    inputDescricao.getText().toString().trim(),
                    pontoParagemTemporario.getAddress()
            );
            for (int x = 0; x < listaPontosParagemPassagem.size(); x++) {
                if (listaPontosParagemPassagem.get(x).getId() == pontoCarregado) {
                    listaPontosParagemPassagem.set(x, pontoParagemPassagemFinal);
                }
            }
        } else {
            pontoParagemPassagemFinal = new PontoParagemPassagem(
                    listaPontosParagemPassagem.size(),
                    inputTitulo.getText().toString().trim(),
                    pontoParagemTemporario.getAudio(),
                    conjuntoImagens,
                    inputDescricao.getText().toString().trim(),
                    pontoParagemTemporario.getAddress()
            );
            listaPontosParagemPassagem.add(listaPontosParagemPassagem.size(), pontoParagemPassagemFinal);
        }

        Gson gson = new Gson();
        String json = gson.toJson(listaPontosParagemPassagem, new TypeToken<ArrayList<PontoParagemPassagem>>() {
        }.getType());

        SharedPreferences sharedPreferences = getSharedPreferences("nextour", MODE_PRIVATE);
        sharedPreferences.edit().putString("arrayPontosParagem", json).commit();

    }

    /**
     * Este método vai verificar os inputs necessários que o utilizador tem de inserir e vai retornar
     * true caso esteja tudo em ordem e false caso seja preciso algo.
     *
     * @return
     */
    private boolean verificarInputs() {
        boolean verifica = true;
        if (inputTitulo.getText().toString().trim().equals(null) || inputTitulo.getText().toString().trim().equals("")) {
            verifica = false;
            inputTitulo.setError("Insira um título");
        } else {
            inputTitulo.setError(null);
        }
        if (inputDescricao.getText().toString().trim().equals(null) || inputTitulo.getText().toString().trim().equals("")) {
            verifica = false;
            inputDescricao.setError("Insira uma descrição");
        } else {
            inputDescricao.setError(null);
        }
        if (pontoParagemTemporario != null) {
            if (pontoParagemTemporario.getAddress() == null) {
                verifica = false;
                local.setError("Escolha um local");
            } else {
                local.setError(null);
            }
        } else {
            verifica = false;
            local.setError("Escolha um local");
        }
        Log.e(TAG, "verificarInputs: " + verifica);
        return verifica;
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
}

