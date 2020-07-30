package com.example.nextour;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nextour.classes.GuiaTuristico;
import com.example.nextour.classes.Imagem;
import com.example.nextour.classes.PlaceAutoSuggestAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilizada para referenciar os componentes e executar as ações da página na qual o utilizador
 * irá ver o mapa com os vários guias turísticos na zona apresentada ao utilizador.
 */
public class PaginaMapa extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMarkerClickListener {

    private DrawerLayout aDrawerLayout;
    private ActionBarDrawerToggle aToggle;
    private NavigationView navView;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private GoogleMap mMap;

    private static final String TAG = "PaginaEscolherLocalidad";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private Boolean permissoesLocalizacao = false;

    //widgets
    private AutoCompleteTextView input_pesquisar;


    //bottom sheet
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout contentorBottomSheetCima;
    private TextView tituloGuiaTuristicoPequeno;
    private ImageView imagemGuiaTuristicoPequena;
    private ImageView startPauseGuiaTuristicoPequeno;
    private ImageView stopGuiaTuristicoPequeno;
    private TextView tituloGuiaTuristico;
    private ImageView imagemGuiaTuristico;
    private ImageView startPauseGuiaTuristico;
    private ImageView stopGuiaTuristico;
    private TextView descricaoGuiaTuristico;
    private boolean emBaixo = false;
    private Button botaoIrGuia;

    //variáveis
    private ArrayList<GuiaTuristico> listaGuiasTuristicos;

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
        setContentView(R.layout.activity_pagina_mapa);

        BottomNavigationView navigationView = (BottomNavigationView) findViewById(R.id.navigationView);
        navigationView.setOnNavigationItemSelectedListener(this);
        navigationView.setSelectedItemId(R.id.paginaMapa);

        //creating the dropdown menu
        criarDrawerLayout();

        //associating the listener to the navigation menu
        navView = findViewById(R.id.menu_cair);
        navView.setNavigationItemSelectedListener(this);
        botoesDisponiveis();

        //bottom sheet
        contentorBottomSheetCima = findViewById(R.id.contentor_cima_bottom_sheet);
        View bottomSheet = findViewById(R.id.bottom_sheet_guia_turistico);
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

        tituloGuiaTuristicoPequeno = findViewById(R.id.titulo_guia_turistico_pequeno);
        imagemGuiaTuristicoPequena = findViewById(R.id.imagem_principal_pequena);
        startPauseGuiaTuristicoPequeno = findViewById(R.id.botao_start_pause_pequeno);
        stopGuiaTuristicoPequeno = findViewById(R.id.botao_stop_pequeno);
        stopGuiaTuristicoPequeno.setVisibility(View.INVISIBLE);

        tituloGuiaTuristico = findViewById(R.id.titulo_guia_turistico);
        imagemGuiaTuristico = findViewById(R.id.imagem_principal);
        startPauseGuiaTuristico = findViewById(R.id.botao_start_pause);
        stopGuiaTuristico = findViewById(R.id.botao_stop);
        stopGuiaTuristico.setVisibility(View.INVISIBLE);
        descricaoGuiaTuristico = findViewById(R.id.descricao_guia_turistico);
        botaoIrGuia = findViewById(R.id.botao_visitar_guia);

        //area da pesquisa
        input_pesquisar = findViewById(R.id.input_pequisa);
        if (servicosAtualizados()) {
            obterPermissaoLocalizacao();
        } else {
            finish();
        }
        inicioDaAreaPesquisa();
    }

    /////////////////Area de carregamento dos guias para o mapa/////////////////

    /**
     * Este método vai carregar os guias turísticos através de um pedido ao servidor e no final do pedido
     * irá executar o método adicionarMarcadores com os guias turísticos já obtidos.
     */
    private void carregarGuias() {
        mMap.clear();
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        LatLng inicio = bounds.southwest;
        LatLng fim = bounds.northeast;

        final SharedPreferences sp = getSharedPreferences("nextour", MODE_PRIVATE);
        StringRequest strRequest = new StringRequest(Request.Method.GET, "http://"
                + getResources().getString(R.string.ip_e_porto) + "/resources/gereguias/"
                + getResources().getString(R.string.rest_key) + "/guias/mapa?minLat=" + inicio.latitude
                + "&minLong=" + inicio.longitude
                + "&maxLat=" + fim.latitude
                + "&maxLong=" + fim.longitude
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                listaGuiasTuristicos = gson.fromJson(response, new TypeToken<ArrayList<GuiaTuristico>>() {
                }.getType());
                if (listaGuiasTuristicos == null) {
                    Toast.makeText(getApplicationContext(), "Ainda não existem guias para mostrar", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "onResponse: " + listaGuiasTuristicos.size());
                    adicionarMarcadores();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(strRequest);
    }


    /**
     * Este método vai adicionr os marcadores de cada guia turístico ao mapa.
     * Cada marcador terá um click listener para subir uma bottomsheet que terá os seus dados.
     */
    private void adicionarMarcadores() {
        int contador = 0;
        for (GuiaTuristico guiaTuristico : listaGuiasTuristicos) {
            if (mMap != null) {
                MarkerOptions markerOptions = new MarkerOptions();
                Bitmap marcador = BitmapFactory.decodeResource(getResources(),
                        R.drawable.marcador_normal_final);
                int height = 100;
                int width = 80;
                Bitmap smallMarker = Bitmap.createScaledBitmap(marcador, width, height, false);
                BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                markerOptions.icon(smallMarkerIcon);
                Log.d(TAG, "adicionarMarcadores: " + guiaTuristico.getLocal().getLatitude() + " \n" + guiaTuristico.getLocal().getLongitude());
                markerOptions.position(new LatLng(guiaTuristico.getLocal().getLatitude(), guiaTuristico.getLocal().getLongitude()));
                mMap.addMarker(markerOptions).setTag(contador);
                contador++;
            }
        }
    }


    /**
     * Este método é utilizado para verificar qual dos marcadores foi carregado e dependendo de qual
     * foi, aparecerá um bottomsheet com os dados do guia turístico.
     *
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        emBaixo = false;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        carregarGuiaTuristicoBottomSheet(listaGuiasTuristicos.get((Integer) marker.getTag()));
        return true;
    }

    /**
     * Este método serve para inserir uma imagem default nas imagens dos guias que não
     * possuam imagens.
     */
    private void inserirImagemDefault() {
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(R.drawable.default_ponto_paragem)
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imagemGuiaTuristicoPequena);

        Glide.with(getApplicationContext())
                .asBitmap()
                .load(R.drawable.default_ponto_paragem)
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imagemGuiaTuristico);
    }

    /**
     * Este método vai inserir os valores do guia turístico carregado para os componentes
     * correspondentes.
     *
     * @param guiaTuristico
     */
    private void carregarGuiaTuristicoBottomSheet(final GuiaTuristico guiaTuristico) {
        tituloGuiaTuristicoPequeno.setText(guiaTuristico.getNome());
        tituloGuiaTuristico.setText(guiaTuristico.getNome());


        if (guiaTuristico.getListaImagens() != null) {
            if (guiaTuristico.getListaImagens().size() > 0) {
                if (guiaTuristico.getListaImagens().get(0) != null) {
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(getResources().getString(R.string.ip_servidor) + guiaTuristico
                                    .getListaImagens()
                                    .get(0)
                                    .getUrl())
                            .dontTransform()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)

                            .into(imagemGuiaTuristicoPequena);
                    imagemGuiaTuristicoPequena.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), PaginaVerImagens.class);
                            intent.putExtra("listaImagens", new Gson().toJson(guiaTuristico.getListaImagens(), new TypeToken<ArrayList<Imagem>>() {
                            }.getType()));
                            startActivity(intent);
                        }
                    });

                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(getResources().getString(R.string.ip_servidor) + guiaTuristico
                                    .getListaImagens()
                                    .get(0)
                                    .getUrl())
                            .dontTransform()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(imagemGuiaTuristico);
                    imagemGuiaTuristico.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), PaginaVerImagens.class);
                            intent.putExtra("listaImagens", new Gson().toJson(guiaTuristico.getListaImagens(), new TypeToken<ArrayList<Imagem>>() {
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

        if (guiaTuristico.getAudio() != null) {
            if (guiaTuristico.getAudio().length() > 0) {
                startPauseGuiaTuristicoPequeno.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        funcoesAudio(getResources().getString(R.string.ip_servidor) + guiaTuristico.getAudio(), false);
                    }
                });
                stopGuiaTuristicoPequeno.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        funcoesAudio(getResources().getString(R.string.ip_servidor) + guiaTuristico.getAudio(), true);
                    }
                });

                startPauseGuiaTuristico.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        funcoesAudio(getResources().getString(R.string.ip_servidor) + guiaTuristico.getAudio(), false);
                    }
                });

                stopGuiaTuristico.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        funcoesAudio(getResources().getString(R.string.ip_servidor) + guiaTuristico.getAudio(), true);
                    }
                });

                startPauseGuiaTuristicoPequeno.setVisibility(View.VISIBLE);
                startPauseGuiaTuristico.setVisibility(View.VISIBLE);

            } else {
                startPauseGuiaTuristicoPequeno.setVisibility(View.INVISIBLE);
                stopGuiaTuristicoPequeno.setVisibility(View.INVISIBLE);
                startPauseGuiaTuristico.setVisibility(View.INVISIBLE);
                stopGuiaTuristico.setVisibility(View.INVISIBLE);
            }
        } else {
            startPauseGuiaTuristicoPequeno.setVisibility(View.INVISIBLE);
            stopGuiaTuristicoPequeno.setVisibility(View.INVISIBLE);
            startPauseGuiaTuristico.setVisibility(View.INVISIBLE);
            stopGuiaTuristico.setVisibility(View.INVISIBLE);
        }

        botaoIrGuia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), PaginaVerGuia.class).putExtra("idGuiaEscolhido", guiaTuristico.getId()));
            }
        });
        descricaoGuiaTuristico.setText(guiaTuristico.getDescricao());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /**
     * Este método será chamado quando áudio chegar ao fim e irá pôr os componentes como estavam no
     * início.
     */
    private void audioAcabou() {
        isPlaying = false;
        mp.release();
        mp = null;
        stopGuiaTuristico.setVisibility(View.INVISIBLE);
        stopGuiaTuristicoPequeno.setVisibility(View.INVISIBLE);
        startPauseGuiaTuristicoPequeno.setImageResource(android.R.color.transparent);
        startPauseGuiaTuristico.setImageResource(android.R.color.transparent);
        startPauseGuiaTuristicoPequeno.setImageResource(R.drawable.start);
        startPauseGuiaTuristico.setImageResource(R.drawable.start);
    }

    /**
     * Este método vai lidar com os pontos do media player.
     * Aqui serão efetuadas as ações para começar, parar e finalizar a reprodução do áudio guia
     * turístico.
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
                stopGuiaTuristico.setVisibility(View.INVISIBLE);
                stopGuiaTuristicoPequeno.setVisibility(View.INVISIBLE);
                startPauseGuiaTuristicoPequeno.setImageResource(android.R.color.transparent);
                startPauseGuiaTuristico.setImageResource(android.R.color.transparent);
                startPauseGuiaTuristicoPequeno.setImageResource(R.drawable.start);
                startPauseGuiaTuristico.setImageResource(R.drawable.start);
            }
            return;
        }
        if (!isPlaying) {
            isPlaying = true;
            stopGuiaTuristico.setVisibility(View.VISIBLE);
            stopGuiaTuristicoPequeno.setVisibility(View.VISIBLE);
            startPauseGuiaTuristicoPequeno.setImageResource(android.R.color.transparent);
            startPauseGuiaTuristico.setImageResource(android.R.color.transparent);
            startPauseGuiaTuristicoPequeno.setImageResource(R.drawable.pause);
            startPauseGuiaTuristico.setImageResource(R.drawable.pause);

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
                startPauseGuiaTuristicoPequeno.setImageResource(android.R.color.transparent);
                startPauseGuiaTuristico.setImageResource(android.R.color.transparent);
                startPauseGuiaTuristicoPequeno.setImageResource(R.drawable.start);
                startPauseGuiaTuristico.setImageResource(R.drawable.start);
            }
        }
    }


    /////////////////Area de pesquisa///////////////
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    /**
     * Este método vai iniciar a barra de pesquisa em que será utilizada para pesquisar uma zona e
     * quando escolhida o utilizador será levado para essa zona no mapa.
     */
    private void inicioDaAreaPesquisa() {

        input_pesquisar.setAdapter(new PlaceAutoSuggestAdapter(this.getApplicationContext(), android.R.layout.simple_list_item_1));

        input_pesquisar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == KeyEvent.ACTION_DOWN
                        || actionId == KeyEvent.KEYCODE_ENTER) {
                    //execução do metodo de procura
                    geoLocate();
                }
                return false;
            }
        });

        esconderKeyboard();
    }

    /**
     * Método que irá localizar a zona que o utilizador pretendeu pesquisar.
     */
    private void geoLocate() {
        Log.d(TAG, "geoLocating ");
        String pesquisaString = input_pesquisar.getText().toString().trim();
        Geocoder geocoder = new Geocoder(getApplicationContext());
        List<Address> lista = new ArrayList<>();
        try {
            lista = geocoder.getFromLocationName(pesquisaString, 1);
        } catch (IOException e) {
            Log.d(TAG, "erro: " + e);
        }
        if (lista.size() > 0) {
            Address address = lista.get(0);
            Log.d(TAG, "address encontrado: " + address);
            mMap.clear();
            moverCamara(new LatLng(address.getLatitude(), address.getLongitude()), 15, address.getAddressLine(0));
        }
    }


    /**
     * Método que irá acontecer quando o mapa estiver completamente carregado.
     * Aqui será feito o onClick listener dos marcadores, a definição de o que acontecerá quando
     * o mapa mudar de posição.
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
            mMap.setMyLocationEnabled(false);
            mMap.setOnMarkerClickListener(this);
            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    carregarGuias();
                }
            });
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setPadding(0, 50, 50, 500);
        }
    }


    /**
     * Este método irá mover a cãmera para uma localização através do parâmetro latLng.
     *
     * @param latLng
     * @param zoom
     */
    private void moverCamara(LatLng latLng, float zoom, String titulo) {
        Log.d(TAG, "moverCamara: a mover camara para latlong: " + latLng + " com o zoom: " + zoom);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        esconderKeyboard();
    }


    /**
     * Este método irá iniciar o mapa.
     */
    private void iniciarMapa() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }


    /**
     * Este método irá verificar as permissões de localização do dispositivo se não estiverem a
     * funcionar ele irá pedi-las. Caso as permissões já estejam dadas, ele vai iniciar o mapa.
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
                    //init mapa
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

        int ativos = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());

        if (ativos == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(ativos)) {
            /*Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getApplicationContext(), ativos, ERROR_DIALOG_REQUEST);
            dialog.show();*/

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

    /////////////////Criação da página///////////////


    /**
     * Este método irá inserir o menu lateral na página xml em questão.
     */
    private void criarDrawerLayout() {//criar o hamburger menu
        aDrawerLayout = findViewById(R.id.dropdown_menu);
        aToggle = new ActionBarDrawerToggle(this, aDrawerLayout, R.string.abrir, R.string.fechar);
        aDrawerLayout.addDrawerListener(aToggle);
        aToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    /**
     * Este método irá verificar que botões no menu lateral poderão ser mostrados ao utilizador de
     * acordo com o seu estado de login.
     */
    private void botoesDisponiveis() {
        SharedPreferences sp = getSharedPreferences("nextour", MODE_PRIVATE);
        if (!sp.getBoolean("logado", false)) {
            MenuItem navigationMenuItemView = navView.getMenu().findItem(R.id.paginaSair);
            navigationMenuItemView.setVisible(false);
            navigationMenuItemView = navView.getMenu().findItem(R.id.paginaMeusGuias);
            navigationMenuItemView.setVisible(false);
            navigationMenuItemView = navView.getMenu().findItem(R.id.paginaLogin);
            navigationMenuItemView.setVisible(true);
        } else {
            MenuItem navigationMenuItemView = navView.getMenu().findItem(R.id.paginaLogin);
            navigationMenuItemView.setVisible(false);
        }
    }

    /**
     * Este método irá fazer com que o menu lateral seja clicável.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (aToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Este método irá verificar que botão foi carregado no menu lateral e o de baixo e executar as ações necessárias.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        SharedPreferences sp = getSharedPreferences("nextour", MODE_PRIVATE);
        switch (item.getItemId()) {
            case R.id.paginaPerfil: {
                if (sp.getBoolean("logado", false)) {
                    startActivity(new Intent(getApplicationContext(), PaginaPerfil.class));
                } else {
                    startActivity(new Intent(getApplicationContext(), PaginaLogin.class));
                }
                break;
            }
            case R.id.paginaCriarGuia: {
                if (sp.getBoolean("logado", false)) {
                    startActivity(new Intent(getApplicationContext(), PaginaCriarGuia.class));
                } else {
                    startActivity(new Intent(getApplicationContext(), PaginaLogin.class));
                }
                break;
            }
            case R.id.paginaMeusGuias: {
                startActivity(new Intent(getApplicationContext(), PaginaMeusGuias.class));
                break;
            }
            case R.id.paginaAjuda: {
                startActivity(new Intent(getApplicationContext(), PaginaAjuda.class));
                break;
            }
            case R.id.paginaInicial: {
                startActivity(new Intent(getApplicationContext(), PaginaInicial.class));
                break;
            }
            case R.id.paginaSair: {
                sp.edit().clear().commit();
                startActivity(new Intent(getApplicationContext(), PaginaLogin.class));
                break;
            }
            case R.id.paginaLogin: {
                startActivity(new Intent(getApplicationContext(), PaginaLogin.class));
                break;
            }
            case R.id.paginaNovos: {
                startActivity(new Intent(getApplicationContext(), PaginaInicial.class));
                overridePendingTransition(0, 0);
                return true;
            }
            case R.id.paginaPesquisar: {
                startActivity(new Intent(getApplicationContext(), PaginaPesquisar.class));
                overridePendingTransition(0, 0);
                return true;
            }
            case R.id.paginaMapa: {
                return true;
            }
            case R.id.paginaFavoritos: {
                startActivity(new Intent(getApplicationContext(), PaginaFavoritos.class));
                overridePendingTransition(0, 0);
                return true;
            }
        }
        return false;
    }
}
