package com.example.nextour.adicionais;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.nextour.PaginaVerGuia;
import com.example.nextour.R;
import com.example.nextour.bottom.BottomSheetMeuGuia;
import com.example.nextour.classes.GuiaTuristico;

import java.util.ArrayList;

/**
 * Classe utilizada para criar uma lista de visualização dos guias que um utilizador pretende ver.
 */
public class ListaGuiasListAdapter extends ArrayAdapter<GuiaTuristico> {

    private Context mContext;
    private FragmentManager mFragmentManager;
    private int mResource;
    private int lastPosition = -1;
    private boolean meu;

    /**
     * Este método serve para indicar que tipo de componentes existem em cada um dos elementos a criar a partir da lista dos guias a ver.
     */
    static class ViewHolder {
        TextView hTitulo;
        TextView hDistrito;
        TextView hDuracao;
        TextView hClassificacao;
        ImageView hImagemPrincipal;
        long idGuia;
    }

    /**
     * Este método é o contrutor e serve para inicializar esta lista de visualização.
     * Tem ainda um boolean 'meu' que serve para verificar se o guia que está a ser carregado vai ser do utilizador que o está a ver ou não.
     * Caso sim, ele poderá eliminar ou editar o mesmo. Caso não, poderá apenas ver.
     *
     * @param context
     * @param resource
     * @param objects
     * @param fragmentManager
     * @param meu
     */
    public ListaGuiasListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<GuiaTuristico> objects, FragmentManager fragmentManager, boolean meu) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mFragmentManager = fragmentManager;
        this.meu = meu;
    }


    /**
     * Método que vai criar cada um dos elementos de visualização através do guia na posição 'position'
     * da lista de guias pelo qual a lista é carregada.
     * Aqui vamos indicar os valores de cada componente de cada elemento a ser apresentado ao utilizador.
     * Também aqui estarão os listeners para os componentes carregados.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final long id = getItem(position).getId();
        String titulo = getItem(position).getNome();
        String distrito = getItem(position).getLocal().getDistrito();
        long duracao = getItem(position).getDuracao();
        String urlImagem = null;
        if (getItem(position).getAudio() != null) {
            urlImagem = mContext.getResources().getString(R.string.ip_servidor) + getItem(position).getAudio();
        }
        String classificacao = getItem(position).getDescricao();
        final View result;
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.hTitulo = (TextView) convertView.findViewById(R.id.titulo_guia);
            holder.hDistrito = (TextView) convertView.findViewById(R.id.distrito);
            holder.hDuracao = (TextView) convertView.findViewById(R.id.duracao_guia);
            holder.hClassificacao = (TextView) convertView.findViewById(R.id.classificacao_guia);
            holder.hImagemPrincipal = (ImageView) convertView.findViewById(R.id.imagem_principal_guia);
            holder.idGuia = getItem(position).getId();

            result = convertView;

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.carregar_conteudo_meus_guias_baixo : R.anim.carregar_conteudo_meus_guias_cima);
        result.startAnimation(animation);
        lastPosition = position;

        holder.hTitulo.setText(titulo);
        holder.hDistrito.setText(distrito);
        holder.hDuracao.setText(duracao + " min");
        holder.hClassificacao.setText(classificacao + "/5");
        if (urlImagem != null) {
            Glide.with(convertView).load(urlImagem).into(holder.hImagemPrincipal);
        } else {
            Glide.with(convertView).load(R.drawable.default_ponto_paragem).into(holder.hImagemPrincipal);
        }


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (meu) {
                    BottomSheetMeuGuia bottomSheetMeuGuia = new BottomSheetMeuGuia(id, mContext);
                    bottomSheetMeuGuia.show(mFragmentManager, "Meu guia");
                } else {
                    mContext.startActivity(new Intent(mContext, PaginaVerGuia.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("idGuiaEscolhido", id));
                }
            }
        });

        return convertView;
    }
}
