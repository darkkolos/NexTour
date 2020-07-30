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
 * de uma activity na qual o utilizador possa editar a sua password.
 */
public class CaixaEditarPasswordUtilizador extends AppCompatDialogFragment implements View.OnClickListener {

    private Button botaoConfirmar;
    private EditText novaPalavraPasse;
    private EditText novaPalavraPasseRepetida;
    private Context aContext;
    private String email;

    /**
     * Este é o construtor.
     *
     * @param context
     * @param email
     */
    public CaixaEditarPasswordUtilizador(Context context, String email) {
        aContext = context;
        this.email = email;
    }


    /**
     * Este método serve para inicializar este dialog box.
     * Aqui haverão dois inputs e dois botões para alterar ou voltar atrás.
     *
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_caixa_mudar_password_utilizador, null);

        builder.setView(view).setTitle("Editar password").setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        novaPalavraPasse = view.findViewById(R.id.nova_password);
        novaPalavraPasseRepetida = view.findViewById(R.id.nova_password_repetida);
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
            alterarPassword();
        }
    }

    /**
     * Este método irá fazer a alteração da password do utilizador.
     */
    public void alterarPassword() {
        boolean valorFinal = true;
        String password = (novaPalavraPasse.getText()).toString().trim();
        String passwordRepetida = (novaPalavraPasseRepetida.getText()).toString();
        if (password.isEmpty()) {
            novaPalavraPasse.setError("Insere uma password!");
            valorFinal = false;
        } else {
            if (passwordRepetida.isEmpty()) {
                novaPalavraPasseRepetida.setError("Insere uma password!");
                valorFinal = false;
            } else {
                if (!password.equals(passwordRepetida)) {
                    novaPalavraPasseRepetida.setError("Passwords não coincidem!");
                } else {
                    novaPalavraPasseRepetida.setError(null);
                    boolean letra = false;
                    boolean numero = false;
                    for (int i = 0; i < password.length(); i++) {
                        if (!letra) {
                            if (Character.isAlphabetic(password.charAt(i))) {
                                letra = true;
                            }
                        }
                        if (!numero) {
                            if (Character.isDigit(password.charAt(i))) {
                                numero = true;
                            }
                        }
                    }
                    if (!letra || !numero) {
                        novaPalavraPasse.setError("A password precisa de pelo menos uma letra e um número.");
                        valorFinal = false;
                    } else {
                        if (password.length() < 6) {
                            novaPalavraPasse.setError("A password precisa de pelo menos 6 caracteres.");
                            valorFinal = false;
                        } else {
                            novaPalavraPasse.setError(null);
                        }
                    }
                }
            }
        }

        if (valorFinal) {//password passou no teste
            new GereUtilizadores().alterarPasswordUtilizador(password, aContext, email, getActivity());
        }
    }

}
