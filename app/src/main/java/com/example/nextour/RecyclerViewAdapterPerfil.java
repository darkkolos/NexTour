package com.example.nextour;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Classe utilizada para criar uma visualização do perfil do utilizador.
 */
public class RecyclerViewAdapterPerfil extends RecyclerView.Adapter<RecyclerViewAdapterPerfil.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private String aFotografiaUtilizador;
    private String aNomeUtilizador;
    private String aEmailUtilizador;
    private String aPasswordUtilizador;
    private Context aContext;
    private FragmentManager aFragmentManager;
    private ContentResolver aContentResolver;
    private String aEmailPassagem;

    /**
     * Este método é o contrutor e serve para inicializar esta visualização do perfil do utilizador.
     * Tem como parâmetros principais os dados do utilizdor que serão utilizadas para inserir em cada um dos componentes desta página.
     *
     * @param fotografiaUtilizador
     * @param nomeUtilizador
     * @param emailUtilizador
     * @param passwordUtilizador
     * @param context
     * @param fragmentManager
     * @param contentResolver
     * @param email
     */
    public RecyclerViewAdapterPerfil(String fotografiaUtilizador, String nomeUtilizador, String emailUtilizador, String passwordUtilizador,
                                     Context context, FragmentManager fragmentManager, ContentResolver contentResolver, String email) {
        this.aFotografiaUtilizador = fotografiaUtilizador;
        this.aNomeUtilizador = nomeUtilizador;
        this.aEmailUtilizador = emailUtilizador;
        this.aPasswordUtilizador = passwordUtilizador;
        this.aContext = context;
        this.aFragmentManager = fragmentManager;
        this.aContentResolver = contentResolver;
        this.aEmailPassagem = email;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    /**
     * Método que vai criar cada a página de visualização através dos dados do utilizador que foram anteriormente disonibilizados.
     * Aqui serão indicados os valores de cada componente de cada elemento a ser apresentado ao utilizador.
     * Também aqui estarão os listeners para os componentes carregados.
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called");


        //Picasso.get().load(Uri.parse(aFotografiaUtilizador)).into(holder.fotografiaUtilizador);
        Glide.with(aContext).asBitmap().load(aContext.getResources().getString(R.string.ip_servidor) + aFotografiaUtilizador).dontTransform().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(holder.fotografiaUtilizador);
        holder.nomeUtilizador.setText(aNomeUtilizador);
        holder.emailUtizador.setText(aEmailUtilizador);
        holder.passwordUtilizador.setText(aPasswordUtilizador);
        holder.editarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirEditarNome();
            }
        });

        holder.editarPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirEditarPassword();
            }
        });


        holder.fotografiaUtilizador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCaixa();
            }
        });
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
     * Este método serve para indicar que tipo de componentes existem em cada um dos elementos a criar a partir da página do perfil a ver.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView fotografiaUtilizador;
        ImageView editarNome;
        ImageView editarPassword;
        TextView nomeUtilizador;
        TextView emailUtizador;
        TextView passwordUtilizador;
        RelativeLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fotografiaUtilizador = itemView.findViewById(R.id.fotografia_perfil);
            editarNome = itemView.findViewById(R.id.editar_nome);
            editarPassword = itemView.findViewById(R.id.editar_password);
            nomeUtilizador = itemView.findViewById(R.id.nome_utilizador);
            emailUtizador = itemView.findViewById(R.id.email_utilizador);
            passwordUtilizador = itemView.findViewById(R.id.password_utilizador);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

    /**
     * Este método irá abrir a caixa que irá permitir ao utilizador editar a foto de perfil.
     */
    private void abrirCaixa() {
        CaixaEditarFotografiaPerfil caixa = new CaixaEditarFotografiaPerfil(aFotografiaUtilizador, aContentResolver, aContext, aEmailPassagem);
        caixa.show(aFragmentManager, "Exemplo de caixa");
    }

    /**
     * Este método irá abrir a caixa que irá permitir ao utilizador editar a password.
     */
    private void abrirEditarPassword() {
        CaixaEditarPasswordUtilizador caixa = new CaixaEditarPasswordUtilizador(aContext, aEmailPassagem);
        caixa.show(aFragmentManager, "Exemplo de caixa");
    }

    /**
     * Este método irá abrir a caixa que irá permitir ao utilizador editar o nome.
     */
    private void abrirEditarNome() {
        CaixaEditarNomeUtilizador caixa = new CaixaEditarNomeUtilizador(aContext, aEmailPassagem);
        caixa.show(aFragmentManager, "Exemplo de caixa");
    }
}
