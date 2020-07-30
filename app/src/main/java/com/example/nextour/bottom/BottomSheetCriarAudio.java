package com.example.nextour.bottom;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.nextour.CaixaCriarAudio;
import com.example.nextour.CaixaCriarPontoParagem;
import com.example.nextour.PaginaCriarGuia;
import com.example.nextour.R;
import com.example.nextour.classes.PontoParagemPassagem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * Esta classe serva para criar o bottom sheet quando é carregado botão de inserir áudio num guia/ponto de paragem.
 * Aqui existirão 3 opções, eliminar, editar/criar ou importar.
 */
public class BottomSheetCriarAudio extends BottomSheetDialogFragment {

    private static final String TAG = "BottomSheetCriarAudio";
    String pathEscolhido;
    PontoParagemPassagem pontoParagemPassagem;
    Context context;
    Activity activity;
    PaginaCriarGuia paginaCriarGuia;
    CaixaCriarPontoParagem caixaCriarPontoParagem;
    boolean tipo;

    /**
     * Este é o construtor.
     *
     * @param aPathEscolhido
     * @param aPontoParagemPassagem
     * @param aContext
     * @param aActivity
     * @param aPaginaCriarGuia
     * @param aCaixaCriarPontoParagem
     */
    public BottomSheetCriarAudio(String aPathEscolhido, PontoParagemPassagem aPontoParagemPassagem, Context aContext,
                                 Activity aActivity, PaginaCriarGuia aPaginaCriarGuia, CaixaCriarPontoParagem aCaixaCriarPontoParagem) {
        pathEscolhido = aPathEscolhido;
        pontoParagemPassagem = aPontoParagemPassagem;
        context = aContext;
        activity = aActivity;
        paginaCriarGuia = aPaginaCriarGuia;
        caixaCriarPontoParagem = aCaixaCriarPontoParagem;

        if (pontoParagemPassagem == null) {
            tipo = true;
        } else {
            tipo = false;
        }
    }


    /**
     * Este método vai ser utilizado para associar os botões aos seus listeners.
     * Tem aqui os listeners dos botões quando estes forem carregados.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_criar_audio_layout, container, false);

        Button importar = v.findViewById(R.id.botao_importar);
        importar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermissionFromDevice()) {
                    requestPermissionsFromDevice();
                }
                escolherAudio();
            }
        });
        Button eliminar = v.findViewById(R.id.botao_eliminar);
        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tipo) {
                    removerSharedPrefsPathAudio();
                    paginaCriarGuia.onResume();
                } else {
                    pontoParagemPassagem.setAudio(null);
                    inserirPontoParagemTemporario();
                    caixaCriarPontoParagem.alterarCorBotoes();
                }
                dismiss();
            }
        });

        Button editar = v.findViewById(R.id.botao_editar);
        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tipo) {
                    context.
                            startActivity(
                                    new Intent(context, CaixaCriarAudio.class).
                                            putExtra("nomeSharedPref", "audioFinal").
                                            putExtra("pathEscolhido", pathEscolhido));
                    dismiss();
                } else {
                    Intent intent = new Intent(context, CaixaCriarAudio.class);
                    intent.removeExtra("nomeSharedPref");
                    startActivity(intent);
                    dismiss();
                }
            }
        });
        return v;
    }

    /**
     * Este método é utilizado quando o utilizador pretender escolher um ficheiro de áudio da memória do seu dispositivo.
     * Vai criar uma página para selecionar o ficheiro.
     */
    public void escolherAudio() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    /**
     * Este método é chamado posteriormente à escolha do ficheiro pelo utilizador e vai associá-lo ao valor onde este deva corresponder.
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
            if (tipo) {
                inserirSharedPrefsPath(getRealPathFromURI(path));
            } else {
                pontoParagemPassagem.setAudio(getRealPathFromURI(path));
                inserirPontoParagemTemporario();
            }
            dismiss();
        }
    }

    /**
     * Este método serve para inserir o ponto de paragem temporário que o utilizador está a criar, nas shared preferences para posteriormente utilizar noutras páginas.
     */
    private void inserirPontoParagemTemporario() {
        Gson gson = new Gson();
        String json = gson.toJson(pontoParagemPassagem, PontoParagemPassagem.class);
        SharedPreferences sharedPreferences = context.getSharedPreferences("nextour", MODE_PRIVATE);
        sharedPreferences.edit().putString("pontoParagemTemporario", json).commit();
    }

    /**
     * Este método serve para inserir o path do áudio ecolhido pelo utilizador, nas shared preferences para posteriormente utilizar noutras páginas.
     */
    private void inserirSharedPrefsPath(String pathNovo) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("nextour", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString("audioFinal", pathNovo);
        prefsEditor.commit();
    }


    /**
     * Este método serve para remover o path do áudio ecolhido pelo utilizador, das shared preferences.
     */
    private void removerSharedPrefsPathAudio() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("nextour", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        if (sharedPreferences.contains("audioFinal")) {
            prefsEditor.remove("audioFinal").commit();
        }
        dismiss();
    }

    /**
     * Este método serve para obter o path de origem do ficheiro correspondente ao contentUri.
     * @param contentUri
     * @return
     */
    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Este método serve para verificar as permissões de leitura do dipositivo para com a aplicação.
     * @return
     */
    private boolean checkPermissionFromDevice() {
        int read_external_storage = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        return read_external_storage == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Este método serve para fazer a permissão ao utilizador de escrita e captura de áudio no dispoitivo paa com a aplicação.
     */
    private void requestPermissionsFromDevice() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1000);
    }

    /**
     * Este método serve para verificar a resposta quanto às permissões feitas pelo utilizador.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1000: {
                if (grantResults.length > 0) {
                    for(int i : grantResults){
                        if (i == PackageManager.PERMISSION_GRANTED) {
                            //tudo bem
                        }else{
                            dismiss();
                        }
                    }
                }else{
                    dismiss();
                }
                break;
            }
        }
    }
}
