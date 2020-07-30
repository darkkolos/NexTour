package com.example.nextour;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.coremedia.iso.boxes.Container;
import com.example.nextour.classes.PontoParagemPassagem;
import com.google.gson.Gson;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Classe utilizada para referenciar os componentes e executar as ações da página que vai criar/editar um áudio.
 */
public class CaixaCriarAudio extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "CaixaCriarAudio";
    private Chronometer chronometer;

    private ImageView botaoGravar;

    private ImageView comecarReproduzir;
    private ImageView pararReproduzir;
    private Button recomecar;

    private Button cancelar;
    private Button confirmar;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    private boolean running;
    private long pauseOffSet;
    private String pathNovo = "";
    private String pathAntigo = "";
    private String pathPassagem = "";

    private String nomeSp;

    private boolean gravando = false;

    private boolean isPlaying = false;

    private PontoParagemPassagem pontoParagemTemporario = null;

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
        setContentView(R.layout.activity_caixa_criar_audio);

        if (getIntent().hasExtra("nomeSharedPref")) {
            nomeSp = getIntent().getStringExtra("nomeSharedPref");
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("nextour", MODE_PRIVATE);
            carregarPontoParagemTemporario(sharedPreferences);
        }

        chronometer = findViewById(R.id.chronometer);

        botaoGravar = findViewById(R.id.botaoGravar);
        botaoGravar.setOnClickListener(this);

        comecarReproduzir = findViewById(R.id.ouvir_reproducao);
        comecarReproduzir.setOnClickListener(this);
        comecarReproduzir.setVisibility(View.INVISIBLE);

        pararReproduzir = findViewById(R.id.parar_reproducao);
        pararReproduzir.setOnClickListener(this);
        pararReproduzir.setVisibility(View.INVISIBLE);

        recomecar = findViewById(R.id.recomecar);
        recomecar.setOnClickListener(this);
        recomecar.setVisibility(View.INVISIBLE);


        confirmar = findViewById(R.id.botao_confirmar);
        confirmar.setOnClickListener(this);
        confirmar.setEnabled(false);

        cancelar = findViewById(R.id.cancelar_botao);
        cancelar.setOnClickListener(this);

        if (!checkPermissionFromDevice()) {
            requestPermissionsFromDevice();
        }
        obterPathAudioEscolhido();

    }

    /**
     * Este método serve para ir buscar o ponto de paragem, às shared preferences, do qual o áudio será editado.
     *
     * @param sharedPreferences
     */
    private void carregarPontoParagemTemporario(SharedPreferences sharedPreferences) {
        if (sharedPreferences.contains("pontoParagemTemporario")) {
            String pontoParagemTemporarioString = sharedPreferences.getString("pontoParagemTemporario", "");
            Gson gson = new Gson();
            pontoParagemTemporario = gson.fromJson(pontoParagemTemporarioString, PontoParagemPassagem.class);
        }
    }

    /**
     * Este método irá buscar o path do áudio que será editado.
     */
    private void obterPathAudioEscolhido() {
        if (getIntent().getStringExtra("pathEscolhido") != null) {
            pathPassagem = getIntent().getStringExtra("pathEscolhido");
            if (pathPassagem.contains("_nextour.mp3")) {
                pathNovo = pathPassagem;
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(pathPassagem);
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                int millSecond = Integer.parseInt(durationStr);
                chronometer.setBase(SystemClock.elapsedRealtime() - (millSecond));
                pauseOffSet = SystemClock.elapsedRealtime() - chronometer.getBase();
                comecarReproduzir.setVisibility(View.VISIBLE);
                recomecar.setVisibility(View.VISIBLE);
            }
        } else {
            if (pontoParagemTemporario != null) {
                if (pontoParagemTemporario.getAudio() != null) {
                    if (pontoParagemTemporario.getAudio().contains("_nextour.mp3")) {
                        pathPassagem = pontoParagemTemporario.getAudio();
                        pathNovo = pathPassagem;
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(pathPassagem);
                        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        int millSecond = Integer.parseInt(durationStr);
                        chronometer.setBase(SystemClock.elapsedRealtime() - (millSecond));
                        pauseOffSet = SystemClock.elapsedRealtime() - chronometer.getBase();
                        comecarReproduzir.setVisibility(View.VISIBLE);
                        recomecar.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    /**
     * Este método irá realizar as permissões de escrita e captura de áudio ao dispositivo.
     */
    private void requestPermissionsFromDevice() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1000);
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
        switch (requestCode) {
            case 1000: {
                if (grantResults.length > 0) {
                    for (int i : grantResults) {
                        if (i == PackageManager.PERMISSION_GRANTED) {
                            //tudo bem
                        } else {
                            finish();
                        }
                    }
                } else {
                    finish();
                }
            }
            break;
        }

    }

    /**
     * Este método serve para verificar as permissões de leitura do dipositivo para com a aplicação.
     *
     * @return
     */
    private boolean checkPermissionFromDevice() {
        int record_audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int write_external_storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return record_audio == PackageManager.PERMISSION_GRANTED && write_external_storage == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Este método irá verificar em que botão o utilizador carregou e realizar as ações correspondentes ao mesmo.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v == botaoGravar) {
            mediaPlayer = null;
            if (!gravando) {
                starChronometer(v);
                if (!pathNovo.equals("")) {
                    pathAntigo = pathNovo;
                }
                pathNovo = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + UUID.randomUUID().toString() + "_nextour.mp3";
                setupMediaRecorder();
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                comecarReproduzir.setVisibility(View.INVISIBLE);
                pararReproduzir.setVisibility(View.INVISIBLE);
                recomecar.setVisibility(View.INVISIBLE);
                confirmar.setEnabled(false);
                gravando = true;
                botaoGravar.setImageResource(R.drawable.pause);
                cancelar.setEnabled(false);
            } else {
                stopChronometer(v);
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                if (!pathAntigo.equals("")) {
                    try {
                        juntarAudios();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                comecarReproduzir.setVisibility(View.VISIBLE);
                pararReproduzir.setVisibility(View.INVISIBLE);
                recomecar.setVisibility(View.VISIBLE);
                confirmar.setEnabled(true);
                gravando = false;
                botaoGravar.setImageResource(R.drawable.comecar_gravar);
            }
        }
        if (v == comecarReproduzir) {
            if (isPlaying) {
                isPlaying = false;
                botaoGravar.setEnabled(true);
                comecarReproduzir.setBackgroundColor(getResources().getColor(R.color.cinzentoMuitoClaro));
                comecarReproduzir.setImageResource(R.drawable.start);
                pararReproduzir.setVisibility(View.INVISIBLE);
                recomecar.setVisibility(View.VISIBLE);
                confirmar.setEnabled(true);
                mediaPlayer.pause();
            } else {
                isPlaying = true;
                botaoGravar.setEnabled(false);
                comecarReproduzir.setBackgroundColor(getResources().getColor(R.color.cinzentoMuitoClaro));
                comecarReproduzir.setImageResource(R.drawable.pause);
                pararReproduzir.setVisibility(View.VISIBLE);
                recomecar.setVisibility(View.INVISIBLE);
                confirmar.setEnabled(false);

                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            isPlaying = false;
                            botaoGravar.setEnabled(true);
                            comecarReproduzir.setBackgroundColor(getResources().getColor(R.color.cinzentoMuitoClaro));
                            comecarReproduzir.setImageResource(R.drawable.start);
                            pararReproduzir.setVisibility(View.INVISIBLE);
                            recomecar.setVisibility(View.VISIBLE);
                            confirmar.setEnabled(true);
                            mediaPlayer = null;
                        }
                    });

                    try {
                        mediaPlayer.setDataSource(pathNovo);
                        mediaPlayer.prepare();
                    } catch (IllegalStateException | IOException e) {
                        e.printStackTrace();
                    }
                }
                mediaPlayer.start();
            }
        }
        if (v == pararReproduzir) {
            isPlaying = false;
            botaoGravar.setEnabled(true);
            comecarReproduzir.setBackgroundColor(getResources().getColor(R.color.cinzentoMuitoClaro));
            comecarReproduzir.setImageResource(R.drawable.start);
            pararReproduzir.setVisibility(View.INVISIBLE);
            recomecar.setVisibility(View.VISIBLE);
            confirmar.setEnabled(true);

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                setupMediaRecorder();
            }
        }
        if (v == recomecar) {
            mediaPlayer = null;
            resetChronometer(v);
            mediaRecorder = null;
            //File file = new File(pathNovo);
            //file.delete();
            pathNovo = "";
            pathAntigo = "";

            botaoGravar.setEnabled(true);
            comecarReproduzir.setVisibility(View.INVISIBLE);
            pararReproduzir.setVisibility(View.INVISIBLE);
            recomecar.setVisibility(View.INVISIBLE);
            confirmar.setEnabled(false);
        }
        if (v == confirmar) {
            enviarDadosSharedPrefs();
            finish();
        }
        if (v == cancelar) {
            finish();
        }
    }

    /**
     * Este método serve para inserir os dados nas sharedpreferences para
     * posteriormente buscar noutras páginas os dados necessários.
     */
    private void enviarDadosSharedPrefs() {
        if (pontoParagemTemporario != null) {
            pontoParagemTemporario.setAudio(pathNovo);
            Gson gson = new Gson();
            String json = gson.toJson(pontoParagemTemporario, PontoParagemPassagem.class);
            SharedPreferences sharedPreferences = getSharedPreferences("nextour", MODE_PRIVATE);
            sharedPreferences.edit().putString("pontoParagemTemporario", json).commit();
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("nextour", MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
            prefsEditor.putString(nomeSp, pathNovo);
            prefsEditor.commit();
        }
    }

    /**
     * Este método serve para quando um utilizador decidir parar de gravar e posteriormente voltar a gravar.
     * Isto é necessário para juntar os 2 ficheiros de áudio num só para se poder ouvir só 1 quando ele tentar reproduzir.
     *
     * @throws IOException
     */
    private void juntarAudios() throws IOException {
        String pathFinal = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + UUID.randomUUID().toString() + "_nextour.mp3";
        FileOutputStream fostream = new FileOutputStream(pathFinal);//destinationfile
        fostream.write(0);
        mergeMediaFiles(true, new String[]{pathAntigo, pathNovo}, pathFinal);
        File file = new File(pathAntigo);
        file.delete();
        file = new File(pathNovo);
        file.delete();
        pathNovo = pathFinal;
        Log.e(TAG, "juntarAudios: " + pathNovo);
    }

    /**
     * Este método vai juntar os ficheiros de áudio.
     *
     * @param isAudio
     * @param sourceFiles
     * @param targetFile
     */
    public void mergeMediaFiles(boolean isAudio, String sourceFiles[], String targetFile) {
        try {
            String mediaKey = isAudio ? "soun" : "vide";
            List<Movie> listMovies = new ArrayList<>();
            for (String filename : sourceFiles) {
                listMovies.add(MovieCreator.build(filename));
            }
            List<Track> listTracks = new LinkedList<>();
            for (Movie movie : listMovies) {
                for (Track track : movie.getTracks()) {
                    if (track.getHandler().equals(mediaKey)) {
                        listTracks.add(track);
                    }
                }
            }
            Movie outputMovie = new Movie();
            if (!listTracks.isEmpty()) {
                outputMovie.addTrack(new AppendTrack(listTracks.toArray(new Track[listTracks.size()])));
            }
            Container container = new DefaultMp4Builder().build(outputMovie);
            FileChannel fileChannel = new RandomAccessFile(String.format(targetFile), "rw").getChannel();
            container.writeContainer(fileChannel);
            fileChannel.close();
        } catch (IOException e) {
        }
    }

    /**
     * Este método vai criar o media recorder necessário para capturar o som que o utilizador reproduzir.
     */
    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(pathNovo);
    }

    /**
     * Este método vai iniciar/resumir o cronómetro.
     *
     * @param v
     */
    public void starChronometer(View v) {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffSet);
            chronometer.start();
            running = true;
        }
    }

    /**
     * Este método vai parar o cronómetro.
     *
     * @param v
     */
    public void stopChronometer(View v) {
        if (running) {
            chronometer.stop();
            pauseOffSet = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
    }

    /**
     * Este método vai reiniciar o cronómetro.
     *
     * @param v
     */
    public void resetChronometer(View v) {
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffSet = 0;
    }
}
