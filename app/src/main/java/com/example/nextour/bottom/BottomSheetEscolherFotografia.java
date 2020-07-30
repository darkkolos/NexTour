package com.example.nextour.bottom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nextour.CaixaCriarPontoParagem;
import com.example.nextour.PaginaCriarGuia;
import com.example.nextour.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Esta classe serva para criar o bottom sheet quando é carregada uma imagem durante a fase de criação/edição de guias/pontos de paragem.
 * Aqui existirão 2 opções, eliminar ou alterar.
 */
public class BottomSheetEscolherFotografia extends BottomSheetDialogFragment {

    ImageView imagemCarregada;
    Context contextCriarGuia;
    boolean tipo;

    /**
     * Este é o construtor.
     *
     * @param imagemCarregada
     * @param contextCriarGuia
     * @param tipo
     */
    public BottomSheetEscolherFotografia(ImageView imagemCarregada, Context contextCriarGuia, boolean tipo) {
        this.imagemCarregada = imagemCarregada;
        this.contextCriarGuia = contextCriarGuia;
        this.tipo = tipo;
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
        View v = inflater.inflate(R.layout.bottom_sheet_escolher_fotografia_layout, container, false);

        Button editar = v.findViewById(R.id.botao_editar);
        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tipo) {
                    ((PaginaCriarGuia) getActivity()).setFotoCarregada(imagemCarregada);
                } else {
                    ((CaixaCriarPontoParagem) getActivity()).setFotoCarregada(imagemCarregada);
                }
                finalizar();
            }
        });
        Button eliminar = v.findViewById(R.id.botao_eliminar);
        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tipo) {
                    ((PaginaCriarGuia) getActivity()).removerFotografia(imagemCarregada);
                } else {
                    ((CaixaCriarPontoParagem) getActivity()).removerFotografia(imagemCarregada);
                }
                finalizar();
            }
        });

        return v;
    }

    /**
     * Este método é utilizado para finalizar esta bottomsheet.
     */
    public void finalizar() {
        try {
            this.dismiss();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}
