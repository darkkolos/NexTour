package com.example.nextour.bottom;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nextour.CaixaCriarPontoParagem;
import com.example.nextour.PaginaCriarGuia;
import com.example.nextour.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Esta classe serva para criar o bottom sheet quando é carregado botão de editar um ponto de paragem durante a fase de criação/edição de guias.
 * Aqui existirão 2 opções, eliminar ou editar/criar.
 */
public class BottomSheetCriarPontoParagem extends BottomSheetDialogFragment {

    int pontoCarregado;
    Context contextCriarGuia;

    /**
     * Este é o construtor.
     * @param pontoCarregado
     * @param context
     */
    public BottomSheetCriarPontoParagem(int pontoCarregado, Context context) {
        this.pontoCarregado = pontoCarregado;
        this.contextCriarGuia = context;
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
        View v = inflater.inflate(R.layout.bottom_sheet_criar_ponto_paragem_layout, container, false);

        Button editar = v.findViewById(R.id.botao_editar);
        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(contextCriarGuia, CaixaCriarPontoParagem.class).putExtra("pontoCarregado", pontoCarregado).putExtra("editar", true));
                finalizar();
            }
        });
        Button eliminar = v.findViewById(R.id.botao_eliminar);
        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PaginaCriarGuia) getActivity()).removerPontoParagem(pontoCarregado);
                finalizar();
            }
        });

        return v;
    }

    /**
     * Este método é utilizado para finalizar esta bottomsheet.
     */
    public void finalizar(){
        try {
            this.dismiss();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}
