package com.example.nextour;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.nextour.adicionais.ImageAdapter;
import com.example.nextour.classes.Imagem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Classe utilizada para referenciar os componentes e executar as ações da página que vai mostrar
 * uma lista de imagens ao utiliador pela página xml activity_pagina_ver_imagens
 */
public class PaginaVerImagens extends AppCompatActivity {

    private Context context;
    private ArrayList<Imagem> listaImagens;


    public PaginaVerImagens() {
    }

    /**
     * Método onCreate que será chamado quando esta atividade for iniciada pela primeira vez e serão
     * associados todos os componentes às variáveis em questão e ainda as variáveis necessárias para
     * iniciar esta página.
     * Aqui será inicializado o ViewPager existente na página xml e vai inserir nele o ImageAdapter
     * com a lista de imagens necessária.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagina_ver_imagens);

        context = getApplicationContext();
        listaImagens = new Gson().fromJson(getIntent().getStringExtra("listaImagens"), new TypeToken<ArrayList<Imagem>>() {
        }.getType());

        ViewPager viewPager = findViewById(R.id.viewPager);
        ImageAdapter imageAdapter = new ImageAdapter(context, listaImagens);
        viewPager.setAdapter(imageAdapter);
    }
}
