package smart.mobile.outras.sincronismo;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class DB_ServerHost
{

	// ??? 1) IDENTIFICAR SE SERVIDOR APACHE ESTÁ RESPONDENDO
	// ??? 2) IDENTIFICAR sE SERVIDOR SQLSERVER ESTÁ RESPONDENDO

	public String MsgErro = "";

	private Context context = null;
	private String Server_DNS;
	private String Server_Banco;

	public DB_ServerHost(Context context, String server, String db)
	{

		this.context = context;
		this.Server_DNS = server;
		this.Server_Banco = db;

	}

	public void isConnected() throws Exception
	{
		NetworkInfo info = null;
		info = ((ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info == null || !info.isConnected())
		{
			throw new Exception("Verifique sua conexão com a Internet.");
		}
		if (info.isRoaming())
		{
			throw new Exception("Verifique sua conexão com a Internet.");
		}
	}

	public boolean ServidorOnline(String URL)
	{

		try
		{
			HttpPost httpPost = new HttpPost(URL);
			StringEntity se = new StringEntity("", HTTP.UTF_8);
			httpPost.setEntity(se);

			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is
			// established.
			int timeoutConnection = 10000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT)
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 10000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
			BasicHttpResponse httpResponse = (BasicHttpResponse) httpClient.execute(httpPost);

			HttpEntity entity = httpResponse.getEntity();

			return true;

		} catch (Exception e)
		{
			return false;
		}

	}

	private String Consome_WS(String operacao, String parametro)
	{

		// String ipservidor = "vargasmobile.no-ip.org";
		// String banco = "Vargas";
		String ipservidor = Server_DNS;
		String banco = Server_Banco;
		String retorno = "";

		// porta default do Apache Tomcat
		if (Server_DNS.indexOf(":") <= 0)
		{
			ipservidor = ipservidor + ":8080";
		}

		// Log.i("SERVIDOR",ipservidor);

		// if (!ServidorOnline("http://" + ipservidor +
		// ":8080/axis/SmartMobile.jws")) {
		if (!ServidorOnline("http://" + ipservidor + "/axis/SmartMobile.jws") && !ServidorOnline("http://" + ipservidor + "/axis/SmartMobile.jws"))
		{
			retorno = "java.apache.SQLException" + "Não foi possível conectar ao Servidor " + ipservidor + ".\n\n1) Verifique sua Conexão\n2) Sincronize Novamente";
		} else
		{

			String operation = operacao;
			String ServiceNamespace = "http://tempuri.org";
			String ServiceEndpointURL = "http://" + ipservidor + "/axis/SmartMobile.jws";
			String Soap_Action = ServiceNamespace + "/" + operation;

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			HttpTransportSE ht = new HttpTransportSE(ServiceEndpointURL);

			SoapObject soapBody = new SoapObject(ServiceNamespace, operation);
			soapBody.addProperty("banco", banco);
			soapBody.addProperty("xml", parametro);

			envelope.dotNet = false;
			envelope.bodyOut = soapBody;
			envelope.bodyIn = null;

			ht.debug = true;
			try
			{

				ht.call(Soap_Action, envelope);

			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				retorno = "java.apache.SQLException" + "Não foi possível conectar ao Servidor Web.\n\n1) Verifique sua Conexão\n2) Sincronize Novamente\n\nMsg1:" + e.getMessage();
				e.printStackTrace();
			} catch (XmlPullParserException e)
			{
				// TODO Auto-generated catch block
				retorno = "java.apache.SQLException" + "Não foi possível conectar ao Servidor Web.\n\n1) Verifique sua Conexão\n2) Sincronize Novamente\n\nMsg2:" + e.getMessage();
				e.printStackTrace();
			}

			if (retorno == "")
			{

				if (envelope.bodyIn instanceof SoapFault)
				{
					SoapFault soapFault = (SoapFault) envelope.bodyIn;
					retorno = soapFault.faultstring;
					// System.out.println(soapFault.faultstring);
				} else
				{
					SoapObject result = (SoapObject) envelope.bodyIn;
					retorno = result.toString();
					// System.out.println(result.toString());
				}

			}

		}

		return retorno;

	}

	private String Consome_WS_Banco(String operacao, String parametro)
	{

		// String ipservidor = "vargasmobile.no-ip.org";
		// String banco = "Vargas";
		String ipservidor = Server_DNS;
		String banco = Server_Banco;
		String retorno = "";

		// porta default do Apache Tomcat
		if (Server_DNS.indexOf(":") <= 0)
		{
			ipservidor = ipservidor + ":8080";
		}

		// Log.i("SERVIDOR",ipservidor);

		// if (!ServidorOnline("http://" + ipservidor +
		// ":8080/axis/SmartMobile.jws")) {
		if (!ServidorOnline("http://" + ipservidor + "/axis/SmartMobile.jws") && !ServidorOnline("http://" + ipservidor + "/axis/SmartMobile.jws"))
		{
			retorno = "java.apache.SQLException" + "Não foi possível conectar ao Servidor " + ipservidor + ".\n\n1) Verifique sua Conexão\n2) Sincronize Novamente";
		} else
		{

			String operation = operacao;
			String ServiceNamespace = "http://tempuri.org";
			String ServiceEndpointURL = "http://" + ipservidor + "/axis/SmartMobile.jws";
			String Soap_Action = ServiceNamespace + "/" + operation;

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			HttpTransportSE ht = new HttpTransportSE(ServiceEndpointURL);

			SoapObject soapBody = new SoapObject(ServiceNamespace, operation);
			soapBody.addProperty("xml", parametro);

			envelope.dotNet = false;
			envelope.bodyOut = soapBody;
			envelope.bodyIn = null;

			ht.debug = true;
			try
			{

				ht.call(Soap_Action, envelope);

			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				retorno = "java.apache.SQLException" + "Não foi possível conectar ao Servidor Web.\n\n1) Verifique sua Conexão\n2) Sincronize Novamente\n\nMsg1:" + e.getMessage();
				e.printStackTrace();
			} catch (XmlPullParserException e)
			{
				// TODO Auto-generated catch block
				retorno = "java.apache.SQLException" + "Não foi possível conectar ao Servidor Web.\n\n1) Verifique sua Conexão\n2) Sincronize Novamente\n\nMsg2:" + e.getMessage();
				e.printStackTrace();
			}

			if (retorno == "")
			{

				if (envelope.bodyIn instanceof SoapFault)
				{
					SoapFault soapFault = (SoapFault) envelope.bodyIn;
					retorno = soapFault.faultstring;
					// System.out.println(soapFault.faultstring);
				} else
				{
					SoapObject result = (SoapObject) envelope.bodyIn;
					retorno = result.toString();
					// System.out.println(result.toString());
				}

			}

		}

		return retorno;

	}

	private String Consome_WS_Image(String operacao, String empresaId)
	{

		// String ipservidor = "vargasmobile.no-ip.org";
		// String banco = "Vargas";
		String ipservidor = Server_DNS;
		String banco = Server_Banco;
		String retorno = "";

		// porta default do Apache Tomcat
		if (Server_DNS.indexOf(":") <= 0)
		{
			ipservidor = ipservidor + ":8080";
		}

		// Log.i("SERVIDOR",ipservidor);

		// if (!ServidorOnline("http://" + ipservidor +
		// ":8080/axis/SmartMobile.jws")) {
		if (!ServidorOnline("http://" + ipservidor + "/axis/SmartMobile.jws") && !ServidorOnline("http://" + ipservidor + "/axis/SmartMobile.jws"))
		{
			retorno = "java.apache.SQLException" + "Não foi possível conectar ao Servidor " + ipservidor + ".\n\n1) Verifique sua Conexão\n2) Sincronize Novamente";
		} else
		{

			String operation = operacao;
			String ServiceNamespace = "http://tempuri.org";
			String ServiceEndpointURL = "http://" + ipservidor + "/axis/SmartMobile.jws";
			String Soap_Action = ServiceNamespace + "/" + operation;

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			HttpTransportSE ht = new HttpTransportSE(ServiceEndpointURL);
			System.setProperty("http.keepAlive", "false");

			SoapObject soapBody = new SoapObject(ServiceNamespace, operation);
			soapBody.addProperty("nomeBanco", banco);
			soapBody.addProperty("idEmpresa", Integer.parseInt(empresaId));

			envelope.dotNet = false;
			envelope.bodyOut = soapBody;
			envelope.bodyIn = null;

			ht.debug = true;
			try
			{

				// ht.
				ht.call(Soap_Action, envelope);

				// SoapObject response = (SoapObject)envelope.getResponse();
				// String string = response.toString();

			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				retorno = "java.apache.SQLException" + "Não foi possível conectar ao Servidor Web.\n\n1) Verifique sua Conexão\n2) Sincronize Novamente\n\nMsg1:" + e.getMessage();
				e.printStackTrace();
			} catch (XmlPullParserException e)
			{
				// TODO Auto-generated catch block
				retorno = "java.apache.SQLException" + "Não foi possível conectar ao Servidor Web.\n\n1) Verifique sua Conexão\n2) Sincronize Novamente\n\nMsg2:" + e.getMessage();
				e.printStackTrace();
			}

			if (retorno == "")
			{

				if (envelope.bodyIn instanceof SoapFault)
				{
					SoapFault soapFault = (SoapFault) envelope.bodyIn;
					retorno = soapFault.faultstring;
					// System.out.println(soapFault.faultstring);
				} else
				{
					SoapObject result = (SoapObject) envelope.bodyIn;
					retorno = result.toString();
					// System.out.println(result.toString());
				}

			}

		}

		return retorno;
	}

	public String Sql_DownloadImage(String str)
	{

		// grava no log
		Log.i("DB_ServerHost-montarImagensZip", str);

		// parametro de retorno
		String retorno = "";

		// remove os campos padroes de retorno
		retorno = Consome_WS_Image("montarImagensZip", str).replace("montarImagensZipResponse{montarImagensZipReturn=", "").replace("; }", "");

		// remove campos 'pipe' >> | <<
		// retorno = retorno.replace("\"|","");

		// verifica se ocorreu alguma exceção
		if (retorno.indexOf("Exception") > -1)
		{
			MsgErro = retorno.replace("java.sql.SQLException", "SqlServer").replace("java.apache.SQLException", "Conexão: ");
		} // armazena o erro para mostra na tela

		return retorno;

	}

	public String Sql_sincronizarBanco(String str)
	{

		// grava no log
		Log.i("DB_ServerHost-sincronizarBanco", str);

		// parametro de retorno
		String retorno = "";

		// remove os campos padroes de retorno
		retorno = Consome_WS_Banco("sincronizarBanco", str).replace("sincronizarBancoResponse{sincronizarBancoReturn=", "").replace("; }", "");

		// remove campos 'pipe' >> | <<
		// retorno = retorno.replace("\"|","");

		// verifica se ocorreu alguma exceção
		if (retorno.indexOf("Exception") > -1)
		{
			MsgErro = retorno.replace("java.sql.SQLException", "SqlServer").replace("java.apache.SQLException", "Conexão: ");
		} // armazena o erro para mostra na tela

		return retorno;

	}
	
	public String Sql_sincronizarInsereBanco(String str)
	{
		
		// grava no log
		Log.i("DB_ServerHost-sincronizarInsereBanco", str);
		
		// parametro de retorno
		String retorno = "";
		
		// remove os campos padroes de retorno
		retorno = Consome_WS_Banco("sincronizarInsereBanco", str).replace("sincronizarInsereBancoResponse{sincronizarInsereBancoReturn=", "").replace("; }", "");
		
		// remove campos 'pipe' >> | <<
		// retorno = retorno.replace("\"|","");
		
		// verifica se ocorreu alguma exceção
		if (retorno.indexOf("Exception") > -1)
		{
			MsgErro = retorno.replace("java.sql.SQLException", "SqlServer").replace("java.apache.SQLException", "Conexão: ");
		} // armazena o erro para mostra na tela
		
		return retorno;
		
	}

	public String Sql_Select(String str)
	{

		// grava no log
		Log.i("DB_ServerHost-SqlSelect", str);

		// parametro de retorno
		String retorno = "";

		// remove os campos padroes de retorno
		retorno = Consome_WS("SqlConsulta", str).replace("SqlConsultaResponse{SqlConsultaReturn=", "").replace("; }", "");

		// remove campos 'pipe' >> | <<
		// retorno = retorno.replace("\"|","");

		// verifica se ocorreu alguma exceção
		if (retorno.indexOf("Exception") > -1)
		{
			MsgErro = retorno.replace("java.sql.SQLException", "SqlServer").replace("java.apache.SQLException", "Conexão: ");
		} // armazena o erro para mostra na tela

		return retorno;

	}

	public String Sql_Executa(String str)
	{

		// grava no log
		Log.i("DB_ServerHost-SqlExecuta", str);

		// parametro de retorno
		String retorno = "";

		// remove os campos padroes de retorno
		retorno = Consome_WS("SqlExecuta", str).replace("SqlExecutaResponse{SqlExecutaReturn=", "").replace("; }", "");

		// verifica se ocorreu alguma exceção
		if (retorno.indexOf("Exception") > -1)
		{
			MsgErro = retorno.replace("java.sql.SQLException", "SqlServer").replace("java.apache.SQLException", "Conexão: ");
		} // armazena o erro para mostra na tela

		return retorno;

	}

}