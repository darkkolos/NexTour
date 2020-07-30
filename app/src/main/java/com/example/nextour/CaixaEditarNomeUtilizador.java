package com.example.nextour;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.nextour.classes.GereUtilizadores;

/**
 * Esta classe que faz extend de um AppCompatDialogFragment irá servir para criar dialog box por cima
 * de uma activity na qual o utilizador possa editar o seu nome de utilizador.
 */
public class CaixaEditarNomeUtilizador extends AppCompatDialogFragment implements View.OnClickListener {

    private Button botaoConfirmar;
    private EditText novoNome;
    private Context aContext;
    private String email;

    /**
     * Este é o construtor.
     *
     * @param context
     * @param email
     */
    public CaixaEditarNomeUtilizador(Context context, String email) {
        aContext = context;
        this.email = email;
    }


    /**
     * Este método serve para inicializar este dialog box.
     * Aqui haverá um input e dois botões para alterar ou voltar atrás.
     *
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_caixa_mudar_nome_utilizador, null);

        builder.setView(view).setTitle("Editar nome").setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        novoNome = view.findViewById(R.id.novo_nome);
        botaoConfirmar = view.findViewById(R.id.botao_confirmar);
        botaoConfirmar.setOnClickListener(this);

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
        if (v == botaoConfirmar) {
            alterarNome();
        }
    }

    /**
     * Este método irá fazer a alteração do nome do utilizador.
     */
    public void alterarNome() {

        String nome = novoNome.getText().toString().trim();
        if (nome.isEmpty()) {
            novoNome.setError("Insere um nome!");
        } else {
            novoNome.setError(null);
            new GereUtilizadores().alterarNomeUtilizador(nome, aContext, email, getActivity());
        }

    }

}
