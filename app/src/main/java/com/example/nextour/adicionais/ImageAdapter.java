package com.example.nextour.adicionais;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nextour.R;
import com.example.nextour.classes.Imagem;

import java.util.ArrayList;

/**
 * Esta classe, que faz o extend da classe PagerAdapter, serve para carregar uma lista de urls de imagens para
 * os componentes apropriados para posterirormente ver as imagens.
 *
 */
public class ImageAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<Imagem> listaImagens;

    /**
     * Este método é o contrutor e vai receber como parâmtros o contexto da aplicação e
     * a lista de imagens que terão os urls das imagens a inserir para visualização.
     * @param context
     * @param listaImagens
     */
    public ImageAdapter(Context context, ArrayList<Imagem> listaImagens) {
        this.context = context;
        this.listaImagens = listaImagens;
    }


    /**
     * Este método serve para verificar o tamanho da lista de imagens.
     * @return
     */
    @Override
    public int getCount() {
        return listaImagens.size();
    }

    /**
     * Este método é utilizado para verificar de se uma View está associada a uma chave de um objeto específico e retornar essa View.
     *
     * @param view
     * @param object
     * @return
     */
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    /**
     * Este método vai criar a página para a posicão que o utilizador estará a ver.
     * Vai então ser criada uma ImageView que terá o source proveniente do url pedido pela imagem que o utilizador está a ver.
     * @param container
     * @param position
     * @return
     */
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Glide.with(context)
                .asBitmap()
                .load(context.getResources().getString(R.string.ip_servidor) + listaImagens.get(position).getUrl())
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView);
        container.addView(imageView, 0);
        return imageView;
    }

    /**
     * Este método serve para remover uma página associada ao item correspondente.
     * @param container
     * @param position
     * @param object
     */
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }
}
