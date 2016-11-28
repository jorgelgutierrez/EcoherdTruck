package nextu.android.certificacion.ecoherdtruck;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class Metodos {


    // constructor
    public Metodos() {

    }

    //Ejecuta la URL estableciendo la conexion y recibe el json en InputStream y posteriormente ser convertido a String...
    public String getJSONfromUrl(String myurl) throws IOException {
        StringBuilder result = new StringBuilder();
        String urlservidor = "http://basura.esy.es/";
        myurl = urlservidor + myurl;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int respuesta = conn.getResponseCode();


            if (respuesta == HttpURLConnection.HTTP_OK) {

                InputStream inputStream = new BufferedInputStream(conn.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String linea;
                while((linea = reader.readLine()) != null){
                    result.append(linea);
                }

            }

        }catch (Exception e) {
            return "Sin respuesta del servidor"+e;
        }

        return result.toString();

    }


}
