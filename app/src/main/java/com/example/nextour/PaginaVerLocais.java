package com.example.nextour;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nextour.classes.GuiaTuristico;
import com.example.nextour.classes.Imagem;
import com.example.nextour.classes.PontoParagem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilizada para referenciar os componentes e executar as ações da página na qual o utilizador
 * irá ver o mapa com o percurso criado pelas localizações dos pontos de paragem de um guia turístico.
 */
public class PaginaVerLocais extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "PaginaVerMeusLocais";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private Boolean permissoesLocalizacao = false;

    private GuiaTuristico guiaTuristico;

    //bottom sheet
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout contentorBottomSheetCima;
    private TextView tituloPontoParagemPequeno;
    private ImageView imagemPontoParagemPequena;
    private ImageView startPausePontoParagemPequeno;
    private ImageView stopPontoParagemPequeno;
    private TextView tituloPontoParagem;
    private ImageView imagemPontoParagem;
    private ImageView startPausePontoParagem;
    private ImageView stopPontoParagem;
    private TextView descricaoPontoParagem;

    private boolean emBaixo = false;


    //direções
    private GeoApiContext geoApiContext = null;

    private boolean isPlaying = false;
    private MediaPlayer mp;

    /**
     * Método onCreate que será chamado quando esta atividade for iniciada pela primeira vez e serão
     * associados todos os componentes às variáveis em questão e ainda as variáveis necessárias para
     * iniciar esta página.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagina_ver_locais);

        contentorBottomSheetCima = findViewById(R.id.contentor_cima_bottom_sheet);
        View bottomSheet = findViewById(R.id.bottom_sheet_ponto_paragem);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING: {
                        contentorBottomSheetCima.setVisibility(View.GONE);
                        break;
                    }
                    case BottomSheetBehavior.STATE_SETTLING: {
                        if (emBaixo) {
                            contentorBottomSheetCima.setVisibility(View.GONE);
                            emBaixo = false;
                        } else {
                            contentorBottomSheetCima.setVisibility(View.VISIBLE);
                            emBaixo = true;
                        }
                        break;
                    }

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });


        tituloPontoParagemPequeno = findViewById(R.id.titulo_ponto_paragem_pequeno);
        imagemPontoParagemPequena = findViewById(R.id.imagem_principal_pequena);
        startPausePontoParagemPequeno = findViewById(R.id.botao_start_pause_pequeno);
        stopPontoParagemPequeno = findViewById(R.id.botao_stop_pequeno);
        stopPontoParagemPequeno.setVisibility(View.INVISIBLE);

        tituloPontoParagem = findViewById(R.id.titulo_ponto_paragem);
        imagemPontoParagem = findViewById(R.id.imagem_principal);
        startPausePontoParagem = findViewById(R.id.botao_start_pause);
        stopPontoParagem = findViewById(R.id.botao_stop);
        stopPontoParagem.setVisibility(View.INVISIBLE);
        descricaoPontoParagem = findViewById(R.id.descricao_ponto_paragem);


        String json = getIntent().getStringExtra("guiaTuristico");
        Gson gson = new Gson();
        guiaTuristico = gson.fromJson(json, GuiaTuristico.class);


        if (servicosAtualizados()) {
            obterPermissaoLocalizacao();
        } else {
            finish();
        }

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Método que irá acontecer quando o mapa estiver completamente carregado.
     * Aqui será chamado o método para inserir os marcadores e criar o percurso.
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (permissoesLocalizacao) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }

        adicionarMarcadores();
        mMap.setOnMarkerClickListener(this);
    }


    /**
     * Este método irá mover a cãmera para uma localização através do parâmetro latLng.
     *
     * @param latLng
     * @param zoom
     */
    private void moverCamara(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        esconderKeyboard();
    }


    /**
     * Este método irá iniciar o mapa e chamar o método para calcular o percurso entre os pontos de
     * paragem e criar as polylines (linhas que passam pela estrada por entre os pontos).
     */
    private void iniciarMapa() {
        Log.e(TAG, "calculateDirections: mapa iniciado");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();
        }
        Log.e(TAG, "calculateDirections: entrada no calc");
        calculateDirections();
    }


    /**
     * Este método irá verificar as permissões de localização do dispositivo se não estiverem a
     * funcionar ele irá pedi-las. Caso estejam a funcionar, ele irá iniciar o mapa.
     */
    public void obterPermissaoLocalizacao() {
        String[] permissoes = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                permissoesLocalizacao = true;
                iniciarMapa();
            } else {
                ActivityCompat.requestPermissions(this, permissoes, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissoes, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Este método serve para verificar a resposta quanto às permissões feitas pelo utilizador.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissoesLocalizacao = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            permissoesLocalizacao = false;
                            return;
                        }
                    }
                    permissoesLocalizacao = true;

                    iniciarMapa();
                }
            }
        }
    }

    /**
     * Este método irá verificar se o utilizador tem os serviços dos mapas atualizados.
     *
     * @return
     */
    public boolean servicosAtualizados() {

        int ativos = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(PaginaVerLocais.this);

        if (ativos == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(ativos)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(PaginaVerLocais.this, ativos, ERROR_DIALOG_REQUEST);
            dialog.show();

        } else {
            Toast.makeText(getApplicationContext(), "Não é possível usar o mapa", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * Este método vai esconder o teclado do ecrã.
     */
    private void esconderKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Este método vai adicionr os marcadores de cada ponto de paragem ao mapa.
     * Cada marcador terá um click listener para subir uma bottomsheet que terá os seus dados.
     */
    private void adicionarMarcadores() {
        int contador = 0;
        for (PontoParagem pontoParagem : guiaTuristico.getListaPontosParagem()) {
            if (mMap != null) {
                MarkerOptions markerOptions = new MarkerOptions();
                int drawable = 0;
                switch (contador) {
                    case 0: {
                        drawable = R.drawable.marcador1final;
                        break;
                    }
                    case 1: {
                        drawable = R.drawable.marcador2final;
                        break;
                    }
                    case 2: {
                        drawable = R.drawable.marcador3final;
                        break;
                    }
                    case 3: {
                        drawable = R.drawable.marcador4final;
                        break;
                    }
                    case 4: {
                        drawable = R.drawable.marcador5final;
                        break;
                    }
                    case 5: {
                        drawable = R.drawable.marcador6final;
                        break;
                    }
                    case 6: {
                        drawable = R.drawable.marcador7final;
                        break;
                    }
                    case 7: {
                        drawable = R.drawable.marcador8final;
                        break;
                    }
                    case 8: {
                        drawable = R.drawable.marcador9final;
                        break;
                    }
                    case 9: {
                        drawable = R.drawable.marcador10final;
                        break;
                    }
                }
                Bitmap marcador = BitmapFactory.decodeResource(getResources(),
                        drawable);
                int height = 100;
                int width = 80;
                Bitmap smallMarker = Bitmap.createScaledBitmap(marcador, width, height, false);
                BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                markerOptions.icon(smallMarkerIcon);
                markerOptions.position(new LatLng(pontoParagem.getLocal().getLatitude(), pontoParagem.getLocal().getLongitude()));
                mMap.addMarker(markerOptions).setTag(contador);
                contador++;
            }
        }
        moverCamara(new LatLng(guiaTuristico.getLocal().getLatitude(), guiaTuristico.getLocal().getLongitude()), 15f);
    }

    /**
     * Este método é utilizado para verificar qual dos marcadores foi carregado e dependendo de qual
     * foi, aparecerá um bottomsheet com os dados do ponto de paragem.
     *
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick: " + marker.getTag().toString());
        ArrayList<PontoParagem> listaPontosParagem = guiaTuristico.getListaPontosParagem();
        Log.d(TAG, "onMarkerClick: " + guiaTuristico.toString());
        emBaixo = false;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        carregarPontoParagemBottomSheet(listaPontosParagem.get((Integer) marker.getTag()));

        return true;
    }

    public void cancelarMediaPlayer() {
        if (mp != null) {
            if (mp.isPlaying()) {
                mp.stop();
                mp.release();
                mp = null;
            }
        }
    }

    /**
     * Este método vai inserir os valores do ponto de paragem carregado para os componentes
     * correspondentes.
     *
     * @param pontoParagem
     */
    private void carregarPontoParagemBottomSheet(final PontoParagem pontoParagem) {

        //cancelarMediaPlayer();

        tituloPontoParagemPequeno.setText(pontoParagem.getNome());
        tituloPontoParagem.setText(pontoParagem.getNome());

        if (pontoParagem.getListaImagens() != null) {
            if (pontoParagem.getListaImagens().size() > 0) {
                if (pontoParagem.getListaImagens().get(0) != null) {
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(getResources().getString(R.string.ip_servidor) + pontoParagem
                                    .getListaImagens()
                                    .get(0)
                                    .getUrl())
                            .dontTransform()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(imagemPontoParagemPequena);
                    Log.e(TAG, "carregarPontoParagemBottomSheet: " + getResources().getString(R.string.ip_servidor) + pontoParagem
                            .getListaImagens()
                            .get(0)
                            .getUrl() );

                    imagemPontoParagemPequena.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(PaginaVerLocais.this, PaginaVerImagens.class);
                            intent.putExtra("listaImagens", new Gson().toJson(pontoParagem.getListaImagens(), new TypeToken<ArrayList<Imagem>>() {
                            }.getType()));
                            startActivity(intent);
                        }
                    });
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(getResources().getString(R.string.ip_servidor) + pontoParagem
                                    .getListaImagens()
                                    .get(0)
                                    .getUrl())
                            .dontTransform()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(imagemPontoParagem);

                    imagemPontoParagem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(PaginaVerLocais.this, PaginaVerImagens.class);
                            intent.putExtra("listaImagens", new Gson().toJson(pontoParagem.getListaImagens(), new TypeToken<ArrayList<Imagem>>() {
                            }.getType()));
                            startActivity(intent);
                        }
                    });
                } else {
                    inserirImagemDefault();
                }
            } else {
                inserirImagemDefault();
            }
        } else {
            inserirImagemDefault();
        }

        if (pontoParagem.getAudio() != null) {
            if (pontoParagem.getAudio().length() > 0) {
                startPausePontoParagemPequeno.setVisibility(View.VISIBLE);
                startPausePontoParagem.setVisibility(View.VISIBLE);
                startPausePontoParagemPequeno.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        funcoesAudio(getResources().getString(R.string.ip_servidor) + pontoParagem.getAudio(), false);
                    }
                });
                stopPontoParagemPequeno.setOnClickListener(new View.OnClickListener() {
                    /**
                     * Este método irá verificar em que botão o utilizador carregou e realizar as ações correspondentes
                     * ao mesmo.
                     *
                     * @param v
                     */
                    @Override
                    public void onClick(View v) {
                        funcoesAudio(getResources().getString(R.string.ip_servidor) + pontoParagem.getAudio(), true);
                    }
                });
                startPausePontoParagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        funcoesAudio(getResources().getString(R.string.ip_servidor) + pontoParagem.getAudio(), false);
                    }
                });
                stopPontoParagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        funcoesAudio(getResources().getString(R.string.ip_servidor) + pontoParagem.getAudio(), true);
                    }
                });
            } else {
                startPausePontoParagemPequeno.setVisibility(View.INVISIBLE);
                stopPontoParagemPequeno.setVisibility(View.INVISIBLE);
                startPausePontoParagem.setVisibility(View.INVISIBLE);
                stopPontoParagem.setVisibility(View.INVISIBLE);
            }
        } else {
            startPausePontoParagemPequeno.setVisibility(View.INVISIBLE);
            stopPontoParagemPequeno.setVisibility(View.INVISIBLE);
            startPausePontoParagem.setVisibility(View.INVISIBLE);
            stopPontoParagem.setVisibility(View.INVISIBLE);
        }
        descricaoPontoParagem.setText(pontoParagem.getDescricao());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /**
     * Este método serve para inserir uma imagem default nas imagens dos pontos de paragem que não
     * possuam imagens.
     */
    private void inserirImagemDefault() {
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(R.drawable.default_ponto_paragem)
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imagemPontoParagemPequena);

        Glide.with(getApplicationContext())
                .asBitmap()
                .load(R.drawable.default_ponto_paragem)
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imagemPontoParagem);
    }

    /**
     * Este método vai lidar com os pontos do media player.
     * Aqui serão efetuadas as ações para começar, parar e finalizar a reprodução do áudio do ponto
     * de paragem.
     *
     * @param audioUrl
     * @param finalizar
     */
    private void funcoesAudio(String audioUrl, boolean finalizar) {
        if (finalizar) {
            if (mp != null) {

                isPlaying = false;
                mp.release();
                mp = null;
                stopPontoParagem.setVisibility(View.INVISIBLE);
                stopPontoParagemPequeno.setVisibility(View.INVISIBLE);
                startPausePontoParagemPequeno.setImageResource(android.R.color.transparent);
                startPausePontoParagem.setImageResource(android.R.color.transparent);
                startPausePontoParagemPequeno.setImageResource(R.drawable.start);
                startPausePontoParagem.setImageResource(R.drawable.start);
            }
            return;
        }
        if (!isPlaying) {
            isPlaying = true;
            stopPontoParagem.setVisibility(View.VISIBLE);
            stopPontoParagemPequeno.setVisibility(View.VISIBLE);
            startPausePontoParagemPequeno.setImageResource(android.R.color.transparent);
            startPausePontoParagem.setImageResource(android.R.color.transparent);
            startPausePontoParagemPequeno.setImageResource(R.drawable.pause);
            startPausePontoParagem.setImageResource(R.drawable.pause);

            if (mp != null) {
                mp.start();
            } else {
                mp = new MediaPlayer();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        audioAcabou();
                    }
                });
                try {
                    mp.setDataSource(audioUrl);
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
                startPausePontoParagemPequeno.setImageResource(android.R.color.transparent);
                startPausePontoParagem.setImageResource(android.R.color.transparent);
                startPausePontoParagemPequeno.setImageResource(R.drawable.start);
                startPausePontoParagem.setImageResource(R.drawable.start);
            }
        }
    }

    /**
     * Este método será chamado quando áudio chegar ao fim e irá pôr os componentes como estavam no
     * início.
     */
    private void audioAcabou() {
        isPlaying = false;
        mp.release();
        mp = null;
        stopPontoParagem.setVisibility(View.INVISIBLE);
        stopPontoParagemPequeno.setVisibility(View.INVISIBLE);
        startPausePontoParagemPequeno.setImageResource(android.R.color.transparent);
        startPausePontoParagem.setImageResource(android.R.color.transparent);
        startPausePontoParagemPequeno.setImageResource(R.drawable.start);
        startPausePontoParagem.setImageResource(R.drawable.start);
    }

    /**
     * Este método vai fazer os cálculos da distância e das direções entre cada ponto de paragem e
     * no final vai criar as polilynes para cada direção.
     */
    private void calculateDirections() {
        if (guiaTuristico.getListaPontosParagem().size() > 1) {
            Log.e(TAG, "calculateDirections: entrou no if");
            for (int x = 1; x < guiaTuristico.getListaPontosParagem().size(); x++) {
                com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                        guiaTuristico.getListaPontosParagem().get(x - 1).getLocal().getLatitude(),
                        guiaTuristico.getListaPontosParagem().get(x - 1).getLocal().getLongitude()
                );
                DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);

                directions.origin(
                        new com.google.maps.model.LatLng(
                                guiaTuristico.getListaPontosParagem().get(x).getLocal().getLatitude(),
                                guiaTuristico.getListaPontosParagem().get(x).getLocal().getLongitude()
                        )
                );
                directions.mode(TravelMode.WALKING);
                Log.e(TAG, "calculateDirections: pedido enviado");
                directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
                    @Override
                    public void onResult(DirectionsResult result) {
                        Log.e(TAG, "calculateDirections: resposta recebida" + result);
                        addPolylinesToMap(result);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage());
                    }
                });
            }
        }
    }

    /**
     * Este método serve para adicionar as polylines ao mapa.
     *
     * @param result
     */
    private void addPolylinesToMap(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                for (DirectionsRoute route : result.routes) {
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for (com.google.maps.model.LatLng latLng : decodedPath) {
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(R.color.azulClaro);
                }
            }
        });
    }
}
