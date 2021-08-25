package helps;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class urlConnection {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String cpf = "39430025864";
		String entrada =postJson("https://user-info.herokuapp.com/users/"+cpf);
		System.out.println(entrada);

	}
	
	public static String postJson(String targetURL) {
	    URL url;
	    HttpURLConnection connection = null;
	    try {
	        // Criando Conexão
	        url = new URL(targetURL);
        	connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestMethod("GET");

	        // Recebendo Resposta
	        InputStream is = null;
	        StringBuffer response = new StringBuffer();
	        try {
	        	is = connection.getInputStream();
	        	BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		        String line;
		        while ((line = rd.readLine()) != null) {
		            response.append(line);
		            response.append('\r');
		        }
		        rd.close();
	        }catch (Exception e) {
	        	is = connection.getErrorStream();
			}
	        
	        return response.toString();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    } finally {
	        if (connection != null) {
	            connection.disconnect();
	        }
	    }
	}

}
