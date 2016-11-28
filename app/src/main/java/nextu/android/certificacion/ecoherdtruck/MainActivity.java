package nextu.android.certificacion.ecoherdtruck;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;

import static java.net.Proxy.Type.HTTP;

public class MainActivity extends AppCompatActivity {

    String latitud = "0", longitud = "0", id_camion = "0";
    Boolean status = false;
    ToggleButton tb_activar;
    TextView id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tb_activar = (ToggleButton) findViewById(R.id.tb_iniciar);
        id = (TextView) findViewById(R.id.txt_id);
    }

    //Clase listener para escuchar los cambios de localizacion
    public class Localizacion implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            longitud = String.valueOf(location.getLongitude());
            latitud = String.valueOf(location.getLatitude());
            new EnviarLocation().execute();


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    //Ejecucion de operacion enviar posicion camion en un hilo separado de la interfaz del usuario....
    private class EnviarLocation extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String url_servidor = "http://basura.esy.es/Actualizar_Localizacion_Camion.php";
            HttpClient cliente = new DefaultHttpClient();
            HttpPost post = new HttpPost(url_servidor);
            List<NameValuePair> postParameters = new ArrayList<NameValuePair>(1);
            postParameters.add(new BasicNameValuePair("id",id_camion));
            postParameters.add(new BasicNameValuePair("lat",latitud));
            postParameters.add(new BasicNameValuePair("long",longitud));
            try {
                post.setEntity(new UrlEncodedFormEntity(postParameters, cz.msebera.android.httpclient.protocol.HTTP.UTF_8));
                cliente.execute(post);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    //Ejecucion de operacion Consultar Id camion en un hilo separado de la interfaz del usuario....
    private class ConsultarIdCamion extends AsyncTask<String, Void, String> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                Metodos obtenerjson = new Metodos();
                return obtenerjson.getJSONfromUrl((urls[0]));
            } catch (Exception e) {
                return "Problemas con la conexion a internet";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
                JSONArray arrayjson = null;
                try {

                    arrayjson = new JSONArray(result);
                    if(status == true) {
                        IniciarRecorrido();
                        Toast.makeText(getBaseContext(), "Recorrido Iniciado correctamente Id Camion "+id_camion, Toast.LENGTH_SHORT).show();
                        tb_activar.setBackgroundColor(getResources().getColor(R.color.colortoggleiniciado));
                    }else{
                        Toast.makeText(getBaseContext(),"Recorrido Terminado correctamente Id Camion "+id_camion,Toast.LENGTH_SHORT).show();
                        id_camion = "";
                        id.setText("");
                        tb_activar.setBackgroundColor(getResources().getColor(R.color.colortoggleterminado));
                    }


            } catch (JSONException e) {
                e.printStackTrace();
                    Toast.makeText(getBaseContext(),"El Id ingresado no existe en la base de datos favor de ingresar uno valido...",Toast.LENGTH_SHORT).show();
                    tb_activar.setChecked(false);
                    tb_activar.setBackgroundColor(getResources().getColor(R.color.colortoggleterminado));
                }

        }
    }

    public void IniciarRecorrido(){

            //Obteniendo la ubicacion por network para posteriormente cuando se desee poder obtener la localizacion...
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Localizacion local = new Localizacion();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 10, (LocationListener) local);
    }

    public void ConsultarCamionParaIniciar(View view){

        if(tb_activar.isChecked()){
            Toast.makeText(getBaseContext(),"Iniciando",Toast.LENGTH_SHORT).show();
            id_camion = id.getText().toString();
            status = true;
            new ConsultarIdCamion().execute("Gestion_Camion.php?id_camion="+id_camion+"&status=true");
        }else{
            Toast.makeText(getBaseContext(),"Terminando",Toast.LENGTH_SHORT).show();
            status = false;
            new ConsultarIdCamion().execute("Gestion_Camion.php?id_camion="+id_camion+"&status=false");
        }
    }
}

