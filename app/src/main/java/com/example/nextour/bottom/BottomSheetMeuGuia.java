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

import com.example.nextour.PaginaInicial;
import com.example.nextour.PaginaVerGuia;
import com.example.nextour.R;
import com.example.nextour.classes.GereGuias;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Esta classe serva para criar o bottom sheet quando é carregado um guia que seja do utilizador que o criou.
 * Aqui existirão 3 opções, eliminar, alterar ou visualizar.
 */
public class BottomSheetMeuGuia extends BottomSheetDialogFragment {

    long idGuiaEscolhido;
    Context context;

    /**
     * Este é o construtor.
     *
     * @param aIdGuiaEscolhido
     * @param aContext
     */
    public BottomSheetMeuGuia(long aIdGuiaEscolhido, Context aContext) {
        idGuiaEscolhido = aIdGuiaEscolhido;
        context = aContext;
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
        View v = inflater.inflate(R.layout.bottom_sheet_meu_guia_layout, container, false);

        Button editar = v.findViewById(R.id.botao_editar);
        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GereGuias().obterGuiaParaEdicao(String.valueOf(idGuiaEscolhido), context);
            }
        });
        Button eliminar = v.findViewById(R.id.botao_eliminar);
        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GereGuias().eliminarGuiaTuristico(String.valueOf(idGuiaEscolhido), context, getActivity());
                context.startActivity(new Intent(context, PaginaInicial.class));
            }
        });
        Button ver = v.findViewById(R.id.botao_ver);
        ver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, PaginaVerGuia.class).putExtra("idGuiaEscolhido", idGuiaEscolhido));
            }
        });

        return v;
    }

}
