package com.example.nextour.adicionais;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nextour.CaixaClassificacaoGuia;
import com.example.nextour.PaginaVerGuia;
import com.example.nextour.PaginaVerImagens;
import com.example.nextour.PaginaVerLocais;
import com.example.nextour.R;
import com.example.nextour.classes.GuiaTuristico;
import com.example.nextour.classes.Imagem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilizada para criar uma visualização do guia que o utilizador pretender ver.
 */
public class VerGuiaRecyclerViewAdapter extends RecyclerView.Adapter<VerGuiaRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private GuiaTuristico guiaTuristico;
    private String classificacao;
    private Context context;
    private boolean isFavorito;
    private String email;
    private FragmentManager fragmentManager;

    private MediaPlayer mp;
    private boolean isPlaying = false;
    PaginaVerGuia paginaVerGuia;

    /**
     * Este método é o contrutor e serve para inicializar esta visualização do guia turístico.
     * Tem como parâmetros principais o guia turístico com as suas informações que serão utilizadas para inserir em cada um dos componentes desta página.
     * Tem a classificação que será mostrada ao utilizador a partir da componente classificacao.
     * Um boolean para indicar se o cliente já tem este guia como favorito.
     *
     * @param guiaTuristico
     * @param classificacao
     * @param context
     * @param isFavorito
     * @param fragmentManager
     * @param paginaVerGuia
     */
    public VerGuiaRecyclerViewAdapter(GuiaTuristico guiaTuristico, String classificacao, Context context, boolean isFavorito,
                                      FragmentManager fragmentManager, PaginaVerGuia paginaVerGuia) {
        this.guiaTuristico = guiaTuristico;
        this.classificacao = classificacao;
        this.context = context;
        this.isFavorito = isFavorito;
        this.fragmentManager = fragmentManager;
        this.paginaVerGuia = paginaVerGuia;
        email = context.getSharedPreferences("nextour", context.MODE_PRIVATE).getString("email", "");
    }

    /**
     * Este método vai ser utilizado para quando esta página estiver totalmente carregada, seja mostrada ao utilizador.
     *
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_ver_guia, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    /**
     * Método que vai criar cada a página de visualização através do guia que foi anteriormente disonibilizado.
     * Aqui serão indicados os valores de cada componente de cada elemento a ser apresentado ao utilizador.
     * Também aqui estarão os listeners para os componentes carregados.
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        if (guiaTuristico.getListaImagens() != null) {
            if (guiaTuristico.getListaImagens().size() > 0) {
                Glide.with(context).asBitmap().load(context.getResources().getString(R.string.ip_servidor) + guiaTuristico.getListaImagens().get(0).getUrl()).dontTransform().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(holder.imagemPrincipal);
            } else {
                Glide.with(context).asBitmap().load(R.drawable.default_ponto_paragem).dontTransform().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(holder.imagemPrincipal);
            }
        } else {
            Glide.with(context).asBitmap().load(R.drawable.default_ponto_paragem).dontTransform().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(holder.imagemPrincipal);
        }
        holder.titulo.setText(guiaTuristico.getNome());
        holder.distrito.setText(guiaTuristico.getLocal().getDistrito());
        holder.duracao.setText(guiaTuristico.getDuracao() + " min");
        holder.classificacaoString.setText(classificacao + "/5");
        holder.descricao.setText(guiaTuristico.getDescricao());
        if (isFavorito) {
            holder.imagemFavorito.setImageResource(R.drawable.favorito_vermelho);
        } else {
            holder.imagemFavorito.setImageResource(R.drawable.favorito_cinzento);
        }
        holder.imagemFavorito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email != null) {
                    if (email.length() > 0) {
                        carregadoFavorito();
                        if (isFavorito) {
                            isFavorito = false;
                            holder.imagemFavorito.setImageResource(R.drawable.favorito_cinzento);
                        } else {
                            holder.imagemFavorito.setImageResource(R.drawable.favorito_vermelho);
                            isFavorito = true;
                        }
                    } else {
                        Toast.makeText(context, "É necessário iniciar sessão", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, "É necessário iniciar sessão", Toast.LENGTH_LONG).show();
                }
            }
        });

        holder.botaoIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gson gson = new Gson();
                String json = gson.toJson(guiaTuristico, GuiaTuristico.class);
                context.startActivity(new Intent(context.getApplicationContext(), PaginaVerLocais.class).putExtra("guiaTuristico", json));
            }
        });
        if (guiaTuristico.getAudio() == null) {
            holder.imagemStartPause.setVisibility(View.GONE);
            holder.linhaAudio.setVisibility(View.GONE);
        }
        holder.imagemStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    isPlaying = true;
                    holder.imagemStop.setVisibility(View.VISIBLE);
                    holder.imagemStartPause.setImageResource(R.drawable.pause);

                    if (mp != null) {
                        mp.start();
                    } else {
                        mp = new MediaPlayer();
                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {
                                audioAcabou();
                                holder.imagemStop.setVisibility(View.INVISIBLE);
                                holder.imagemStartPause.setImageResource(R.drawable.start);
                            }
                        });
                        try {
                            mp.setDataSource(context.getResources().getString(R.string.ip_servidor) + guiaTuristico.getAudio());
                            mp.prepare();
                            mp.start();
                        } catch (IOException e) {
                            Log.e("onBindViewHolder", e.getMessage());
                        }
                    }
                } else {
                    if (mp != null) {
                        isPlaying = false;
                        mp.pause();
                        holder.imagemStartPause.setImageResource(R.drawable.start);
                    }
                }
            }
        });
        holder.imagemStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null) {
                    isPlaying = false;
                    mp.release();
                    mp = null;
                    holder.imagemStop.setVisibility(View.INVISIBLE);
                    holder.imagemStartPause.setImageResource(R.drawable.start);
                }
            }
        });
        holder.imagemPrincipal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context.getApplicationContext(), PaginaVerImagens.class);
                intent.putExtra("listaImagens", new Gson().toJson(guiaTuristico.getListaImagens(), new TypeToken<ArrayList<Imagem>>() {
                }.getType()));
                context.startActivity(intent);
            }
        });
        holder.botaoClassificacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CaixaClassificacaoGuia caixa = new CaixaClassificacaoGuia(context, guiaTuristico.getId(), email, paginaVerGuia);
                caixa.show(fragmentManager, "Exemplo de caixa");
            }
        });
        holder.imagemStop.setVisibility(View.INVISIBLE);
    }

    /**
     * Este método serve apenas para indicar que existe apenas 1 único item a ser posto na página.
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return 1;
    }

    /**
     * Este método serve para indicar que tipo de componentes existem em cada um dos elementos a criar a partir da página do guia a ver.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imagemPrincipal;
        ImageView imagemFavorito;
        Button botaoIniciar;
        LinearLayout botaoClassificacao;
        ImageView imagemStartPause;
        ImageView imagemStop;
        TextView titulo;
        TextView distrito;
        TextView duracao;
        TextView classificacaoString;
        TextView descricao;
        RelativeLayout parentLayout;
        View linhaAudio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagemPrincipal = itemView.findViewById(R.id.imagem_principal);
            imagemFavorito = itemView.findViewById(R.id.favorito);
            botaoIniciar = itemView.findViewById(R.id.botao_iniciar);
            botaoClassificacao = itemView.findViewById(R.id.botao_classificacao_guia);
            imagemStartPause = itemView.findViewById(R.id.botao_start_pause);
            imagemStop = itemView.findViewById(R.id.botao_stop);
            titulo = itemView.findViewById(R.id.titulo_guia);
            distrito = itemView.findViewById(R.id.distrito_guia);
            duracao = itemView.findViewById(R.id.duracao_guia);
            classificacaoString = itemView.findViewById(R.id.classificacao_guia);
            descricao = itemView.findViewById(R.id.descricao_guia);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            linhaAudio = itemView.findViewById(R.id.linha_audio);
        }
    }

    /**
     * Este método é chamado quando o utilizador carrega no botão do 'favorito' o que irá realizar um pedido ao servidor.
     */
    public void carregadoFavorito() {
        String url = "http://" + context.getResources().getString(R.string.ip_e_porto) + "/resources/gereguias/"
                + context.getResources().getString(R.string.rest_key) + "/guia/favorito";
        StringRequest strRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("aEmail", email);
                params.put("idGuiaTuristico", String.valueOf(guiaTuristico.getId()));
                return params;
            }
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                switch (response.statusCode) {
                    case 202:
                    case 200: {
                        paginaVerGuia.runOnUiThread(new Runnable() {
                            public void run() {
                                if (!isFavorito) {
                                    Toast.makeText(context.getApplicationContext(), "Removido dos favoritos.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context.getApplicationContext(), "Adicionado aos favoritos.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        break;
                    }
                    default: {
                        paginaVerGuia.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(context.getApplicationContext(), "Houve um erro, tente novamente mais tarde.", Toast.LENGTH_LONG).show();
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


    /**
     * Este método serve para quando a reprodução do áudio terminar sejam feitas as medidas necessárias para que possa ser reproduzido outro vez.
     */
    private void audioAcabou() {
        isPlaying = false;
        mp.release();
        mp = null;
    }
}
