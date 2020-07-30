package com.example.nextour;

import android.Manifest;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.nextour.classes.PlaceAutoSuggestAdapter;
import com.example.nextour.classes.PontoParagemPassagem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Classe utilizada para referenciar os componentes e executar as ações da página na qual o utilizador
 * irá escolher o ponto de referência de um guia/ponto de paragem.
 */
public class PaginaEscolherLocalidade extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, Button.OnClickListener {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final String TAG = "PaginaEscolherLocalidad";

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private Boolean permissoesLocalizacao = false;

    private Address enderecoFinal = null;


    //widgets
    private AutoCompleteTextView input_pesquisar;
    private Button confirmar;
    private Button cancelar;

    private String nomeSp;

    private PontoParagemPassagem pontoParagemTemporario = null;

    /**
     * Construtor.
     */
    public PaginaEscolherLocalidade() {
    }

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
        setContentView(R.layout.activity_pagina_escolher_localidade);

        if (getIntent().hasExtra("nomeSharedPref")) {
            nomeSp = getIntent().getStringExtra("nomeSharedPref");
        } else {
            Log.e("boooooooooas", "onCreate: entrou no carregarpontoparagemtemporario");
            SharedPreferences sharedPreferences = getSharedPreferences("nextour", MODE_PRIVATE);
            carregarPontoParagemTemporario(sharedPreferences);
        }


        input_pesquisar = findViewById(R.id.input_pequisa);

        confirmar = findViewById(R.id.continuar_botao);
        confirmar.setOnClickListener(this);

        cancelar = findViewById(R.id.cancelar_botao);
        cancelar.setOnClickListener(this);


        if (servicosAtualizados()) {
            obterPermissaoLocalizacao();
        } else {
            finish();
        }

        init();

    }


    /**
     * Este método vai carregar o ponto de paragem temporário das sharedpreferences que será editado.
     *
     * @param sharedPreferences
     * @return
     */
    private void carregarPontoParagemTemporario(SharedPreferences sharedPreferences) {
        String pontoParagemTemporarioString = sharedPreferences.getString("pontoParagemTemporario", "");
        if (!pontoParagemTemporarioString.equals("")) {
            Gson gson = new Gson();
            pontoParagemTemporario = gson.fromJson(pontoParagemTemporarioString, PontoParagemPassagem.class);
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Este método vai iniciar a barra de pesquisa em que será utilizada para pesquisar uma zona e
     * quando escolhida o utilizador será levado para essa zona no mapa.
     */
    private void init() {

        input_pesquisar.setAdapter(new PlaceAutoSuggestAdapter(PaginaEscolherLocalidade.this, android.R.layout.simple_list_item_1));

        input_pesquisar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == KeyEvent.ACTION_DOWN
                        || actionId == KeyEvent.KEYCODE_ENTER
                        || actionId == EditorInfo.IME_ACTION_GO) {
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
        Geocoder geocoder = new Geocoder(PaginaEscolherLocalidade.this);
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
            enderecoFinal = address;
            moverCamara(new LatLng(address.getLatitude(), address.getLongitude()), 15, address.getAddressLine(0));
        }
    }

    /**
     * Método que irá acontecer quando o mapa estiver completamente carregado.
     * Aqui será feito o onClick listener dele e ainda a localização do ponto se este já estiver
     * escolhido e estiver no caso de uma edição.
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (permissoesLocalizacao) {
            obterLocalizacaoDispositivo();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                enderecoFinal = addresses.get(0);
                moverCamara(latLng, mMap.getCameraPosition().zoom, addresses.get(0).getAddressLine(0));
            }
        });


        if (pontoParagemTemporario != null) {
            if (pontoParagemTemporario.getAddress() != null) {
                enderecoFinal = pontoParagemTemporario.getAddress();
                moverCamara(new LatLng(enderecoFinal.getLatitude(), enderecoFinal.getLongitude()), mMap.getCameraPosition().zoom, enderecoFinal.getAddressLine(0));
            }
        }
    }

    /**
     * Este método irá obter a localização do dispositivo.
     */
    private void obterLocalizacaoDispositivo() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (permissoesLocalizacao) {
                final Task localizacao = fusedLocationProviderClient.getLastLocation();
                localizacao.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: encontramos o dispositivo");
                            Location posicaoAtual = (Location) task.getResult();
                            if (posicaoAtual != null) {
                                moverCamara(new LatLng(posicaoAtual.getLatitude(), posicaoAtual.getLongitude()), 15f, "Minha localização");
                            }
                        } else {
                            Log.d(TAG, "onComplete: nao conseguimos encontramos o dispositivo");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "obterLocalizacaoDispositivo: erro :" + e);
        }
    }

    /**
     * Este método irá mover a cãmera para uma localização através do parâmetro latLng.
     *
     * @param latLng
     * @param zoom
     * @param titulo
     */
    private void moverCamara(LatLng latLng, float zoom, String titulo) {
        Log.d(TAG, "moverCamara: a mover camara para latlong: " + latLng + " com o zoom: " + zoom);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (!titulo.equals("Minha localização")) {
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(titulo);
            mMap.addMarker(markerOptions);
        }
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
     * funcionar ele irá pedi-las.
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
        Log.d(TAG, "servicosAtualizados: a verificar serviços");

        int ativos = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(PaginaEscolherLocalidade.this);

        if (ativos == ConnectionResult.SUCCESS) {
            Log.d(TAG, "servicosAtualizados: serviços atualizados");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(ativos)) {
            //ocorreu um erro que pode ser resolvido
            Log.d(TAG, "servicosAtualizados: erro resolvivel");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(PaginaEscolherLocalidade.this, ativos, ERROR_DIALOG_REQUEST);
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
     * Este método serve para inserir os dados nas sharedpreferences para
     * posteriormente buscar noutras páginas os dados necessários.
     */
    public void carregarParaSharedPrefs() {
        if (pontoParagemTemporario != null) {
            Log.e(TAG, "carregarParaSharedPrefs: ponto paragem" );
            pontoParagemTemporario.setAddress(enderecoFinal);
            Gson gson = new Gson();
            String json = gson.toJson(pontoParagemTemporario, PontoParagemPassagem.class);
            SharedPreferences sharedPreferences = getSharedPreferences("nextour", MODE_PRIVATE);
            sharedPreferences.edit().putString("pontoParagemTemporario", json).commit();
        } else {
            Log.e(TAG, "carregarParaSharedPrefs: guia" );
            SharedPreferences sharedPreferences = getSharedPreferences("nextour", MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(enderecoFinal);
            prefsEditor.putString(nomeSp, json);
            prefsEditor.commit();
            Log.d(TAG, "carregarParaSharedPrefs: acabou o carregamento");
        }
    }


    /**
     * Este método irá verificar em que botão o utilizador carregou e realizar as ações correspondentes
     * ao mesmo.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v == confirmar) {
            Log.d(TAG, "onClick: botao confirmado");
            if (enderecoFinal != null) {
                Log.d(TAG, "onClick: carregando");
                carregarParaSharedPrefs();
            }
            finish();
        } else if (v == cancelar) {
            Log.d(TAG, "onClick: botao cancelado");
            finish();
        }
    }
}
