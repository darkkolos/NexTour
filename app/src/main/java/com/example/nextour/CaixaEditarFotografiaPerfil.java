package com.example.nextour;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nextour.classes.GereUtilizadores;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Classe utilizada para criar a página onde vai ser inserida/editada a fotografia de perfil de um
 * utilizador.
 */
public class CaixaEditarFotografiaPerfil extends AppCompatDialogFragment implements View.OnClickListener {

    private Button botaoEscolher;
    private Button botaoAlterar;
    private ImageView fotografiaUtilizador;
    private String urlImagem;
    private String email;
    private Context aContext;
    Bitmap bitmap = null;
    private ContentResolver aContentResolver;

    /**
     * Este é o construtor.
     *
     * @param aUrl
     * @param contentResolver
     * @param context
     * @param email
     */
    public CaixaEditarFotografiaPerfil(String aUrl, ContentResolver contentResolver, Context context, String email) {
        urlImagem = aUrl;
        aContentResolver = contentResolver;
        aContext = context;
        this.email = email;
    }


    /**
     * Este método serve para inicializar este dialog box.
     * Aqui haverá 1 imagem que é a que o utilizador tem como inicial e ele poderá alterá-la ou voltar atrás.
     *
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_caixa_mudar_imagem_perfil, null);

        builder.setView(view).setTitle("Fotografia").setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        botaoEscolher = view.findViewById(R.id.botao_escolher_perfil);
        botaoEscolher.setOnClickListener(this);
        botaoAlterar = view.findViewById(R.id.botao_alterar_perfil);
        botaoAlterar.setOnClickListener(this);
        fotografiaUtilizador = view.findViewById(R.id.fotografia_perfil_editar);
        Glide.with(aContext).asBitmap().load(aContext.getResources().getString(R.string.ip_servidor) + urlImagem).dontTransform().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(fotografiaUtilizador);

        return builder.create();
    }

    /**
     * Este método irá verificar em que botão o utilizador carregou e realizar as ações correspondentes
     * ao mesmo.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v == botaoEscolher) {
            escolherFotografia();
        }
        if (v == botaoAlterar) {
            if (bitmap != null) {
                new GereUtilizadores().uploadFotografiaPerfil(conversaoImagemString(bitmap), aContext, email, getActivity());
            } else {
                Toast.makeText(getContext(), "Insere primeiro uma imagem", Toast.LENGTH_SHORT).show();
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
        if (requestCode == 1 && resultCode == -1 && data != null) {
            Uri path = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(aContentResolver, path);
                fotografiaUtilizador.setImageBitmap(bitmap);
                fotografiaUtilizador.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
}
