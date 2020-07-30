package com.example.nextour;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Esta classe que faz extend de um AppCompatDialogFragment irá servir para criar dialog box por cima
 * de uma activity na qual o utilizador possa realizar/editar uma avaliação a um guia turístico.
 */
public class CaixaClassificacaoGuia extends AppCompatDialogFragment implements View.OnClickListener {

    private Context context;
    private long idGuia;
    private float classificacao = 0;
    private String email;
    private ImageView estrela1;
    private ImageView estrela2;
    private ImageView estrela3;
    private ImageView estrela4;
    private ImageView estrela5;
    private PaginaVerGuia paginaVerGuia;

    /**
     * Este é o construtor.
     *
     * @param aContext
     * @param idGuia
     * @param email
     * @param paginaVerGuia
     */
    public CaixaClassificacaoGuia(Context aContext, long idGuia, String email, PaginaVerGuia paginaVerGuia) {
        context = aContext;
        this.idGuia = idGuia;
        this.email = email;
        this.paginaVerGuia = paginaVerGuia;
    }

    /**
     * Este método serve para inicializar este dialog box.
     * Aqui haverão 5 estrelas com um valor associado a cada uma e um listener para quando o utilizador carregar nelas.
     *
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_caixa_classificacao_guia, null);

        builder.setView(view).setTitle("").setNegativeButton("Voltar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                paginaVerGuia.reload();
            }
        });

        estrela1 = view.findViewById(R.id.estrela_1);
        estrela1.setTag("nao");
        estrela1.setOnClickListener(this);
        estrela2 = view.findViewById(R.id.estrela_2);
        estrela2.setTag("nao");
        estrela2.setOnClickListener(this);
        estrela3 = view.findViewById(R.id.estrela_3);
        estrela3.setTag("nao");
        estrela3.setOnClickListener(this);
        estrela4 = view.findViewById(R.id.estrela_4);
        estrela4.setTag("nao");
        estrela4.setOnClickListener(this);
        estrela5 = view.findViewById(R.id.estrela_5);
        estrela5.setTag("nao");
        estrela5.setOnClickListener(this);

        obterClassificacao();
        builder.setCancelable(false);
        return builder.create();
    }

    /**
     * Método que vai gerir os listeners dos cliques nas estrelas.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (email != null) {
            if (email.length() > 0) {
                if (v == estrela1) {
                    enviarPedido(1);
                }
                if (v == estrela2) {
                    enviarPedido(2);
                }
                if (v == estrela3) {
                    enviarPedido(3);
                }
                if (v == estrela4) {
                    enviarPedido(4);
                }
                if (v == estrela5) {
                    enviarPedido(5);
                }
            } else {
                Toast.makeText(context, "É necessário inicir sessão para avaliar um guia", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "É necessário inicir sessão para avaliar um guia", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Método que irá verificar a classificação dada ao guia pelo utilizador e pintar o número
     * necessário de estrelas para corresponder à classificação.
     */
    public void pintarEstrelas() {
        reporEstrelas();
        if (classificacao >= 1) {
            if (classificacao < 2) {
                estrela1.setImageResource(R.drawable.estrela);
                estrela1.setTag("sim");
            } else if (classificacao >= 2 && classificacao < 3) {
                estrela1.setImageResource(R.drawable.estrela);
                estrela1.setTag("sim");
                estrela2.setImageResource(R.drawable.estrela);
                estrela2.setTag("sim");
            } else if (classificacao >= 3 && classificacao < 4) {
                estrela1.setImageResource(R.drawable.estrela);
                estrela1.setTag("sim");
                estrela2.setImageResource(R.drawable.estrela);
                estrela2.setTag("sim");
                estrela3.setImageResource(R.drawable.estrela);
                estrela3.setTag("sim");
            } else if (classificacao >= 4 && classificacao < 5) {
                estrela1.setImageResource(R.drawable.estrela);
                estrela1.setTag("sim");
                estrela2.setImageResource(R.drawable.estrela);
                estrela2.setTag("sim");
                estrela3.setImageResource(R.drawable.estrela);
                estrela3.setTag("sim");
                estrela4.setImageResource(R.drawable.estrela);
                estrela4.setTag("sim");
            } else if (classificacao == 5) {
                estrela1.setImageResource(R.drawable.estrela);
                estrela1.setTag("sim");
                estrela2.setImageResource(R.drawable.estrela);
                estrela2.setTag("sim");
                estrela3.setImageResource(R.drawable.estrela);
                estrela3.setTag("sim");
                estrela4.setImageResource(R.drawable.estrela);
                estrela4.setTag("sim");
                estrela5.setImageResource(R.drawable.estrela);
                estrela5.setTag("sim");
            }
        }
    }

    /**
     * Este método irá obter a classificação dada pelo utilizador ao guia em quetão.
     */
    public void obterClassificacao() {
        StringRequest strRequest = new StringRequest(Request.Method.GET, "http://"
                + getResources().getString(R.string.ip_e_porto) + "/resources/gereguias/"
                + getResources().getString(R.string.rest_key) + "/guia/classificacao?aEmail=" + email
                + "&idGuiaTuristico=" + idGuia, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (new Gson().fromJson(response, String.class).equals("0")) {
                    setClassificacao(0);
                    pintarEstrelas();
                } else {
                    setClassificacao(Integer.parseInt(new Gson().fromJson(response, String.class)));
                    pintarEstrelas();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("aEmail", email);
                params.put("idGuiaTuristico", String.valueOf(idGuia));
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(strRequest);
    }

    /**
     * Este método irá enviar um pedido ao servidor para atualizar a avaliação do utilizador quanto ao guia em questão.
     *
     * @param classificacao
     */
    public void enviarPedido(int classificacao) {
        StringRequest strRequest = new StringRequest(Request.Method.POST, "http://" + getResources().getString(R.string.ip_e_porto)
                + "/resources/gereguias/" + getResources().getString(R.string.rest_key) + "/guia/classificacao", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("aEmail", email);
                params.put("idGuiaTuristico", String.valueOf(idGuia));
                params.put("valor", String.valueOf(classificacao));
                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                switch (response.statusCode) {
                    case 200: {
                        setClassificacao(classificacao);
                        pintarEstrelas();
                        paginaVerGuia.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(context, "Guia avaliado", Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                    case 202: {
                        setClassificacao(0);
                        pintarEstrelas();
                        paginaVerGuia.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(context, "Avaliação removida", Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                    default: {
                        paginaVerGuia.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(context, "Erro ao avaliar o guia, tente novamente mais tarde", Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                }
                return super.parseNetworkResponse(response);
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(strRequest);
    }

    public void setClassificacao(int classificacao) {
        this.classificacao = classificacao;
    }

    /**
     * Este método irá repôr as estrelas para as cores originais.
     */
    public void reporEstrelas() {
        estrela1.setImageResource(R.drawable.estrela_cinzenta);
        estrela1.setTag("nao");
        estrela2.setImageResource(R.drawable.estrela_cinzenta);
        estrela2.setTag("nao");
        estrela3.setImageResource(R.drawable.estrela_cinzenta);
        estrela3.setTag("nao");
        estrela4.setImageResource(R.drawable.estrela_cinzenta);
        estrela4.setTag("nao");
        estrela5.setImageResource(R.drawable.estrela_cinzenta);
        estrela5.setTag("nao");
    }
}
