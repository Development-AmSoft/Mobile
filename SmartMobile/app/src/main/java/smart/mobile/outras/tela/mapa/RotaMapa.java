package smart.mobile.outras.tela.mapa;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import smart.mobile.PrincipalClasse;
import smart.mobile.R;
import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.utils.error.ErroGeralController;
import smart.mobile.utils.gps.GPSConstantes;
import smart.mobile.utils.gps.GPSTracker;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class RotaMapa extends FragmentActivity
{

	private SupportMapFragment mapFrag;
	private GoogleMap map;
	private Marker marker;
	private Polyline polyline;
	private List<LatLng> list;
	private long distance;
	private GPSTracker gps;
	private LatLng posicaoAtual;
	private PrincipalClasse aplication;
	private DB_LocalHost banco;
	private CameraUpdate update = null;

	private LatLng posicaoBrasil = new LatLng(-14.2400732, -53.1805017);

	private String endereco = "";
	private String cidade = "";
	private String numero = "";
	private String bairro = "";
	private String cep = "";
	private ProgressDialog myProgressDialog;
	private Handler h = new Handler();
	private LatLng posicaoFinal;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rota_mapa);

		myProgressDialog = ProgressDialog.show(RotaMapa.this, "Carregando Informações", "Carregando Informações do Mapa");
		myProgressDialog.setCancelable(false);
		myProgressDialog.setCanceledOnTouchOutside(false);
		myProgressDialog.show();

		h.post(new Runnable()
		{

			@Override
			public void run()
			{
				aplication = (PrincipalClasse) getApplication();
				banco = new DB_LocalHost(RotaMapa.this);
				ErroGeralController erro = new ErroGeralController(RotaMapa.this, banco);

				MapsInitializer.initialize(RotaMapa.this);

				GoogleMapOptions options = new GoogleMapOptions();
				options.zOrderOnTop(true);

				mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment1);

				map = mapFrag.getMap();

				/*
				 * mapFrag = SupportMapFragment.newInstance(options);
				 * 
				 * FragmentTransaction ft =
				 * getSupportFragmentManager().beginTransaction();
				 * ft.replace(R.id.llContainer, mapFrag); ft.commit();
				 */
				h.post(new Runnable()
				{

					@Override
					public void run()
					{
						configMap();

						if (aplication.getTipoMapa().equals(GPSConstantes.MAPA_CLIENTE_SELECIONADO))
						{

							Bundle b = getIntent().getExtras();

							endereco = b.getString("endereco");
							cidade = b.getString("cidade");
							numero = b.getString("numero");
							bairro = b.getString("bairro");
							cep = b.getString("cep");

							verificarString();

							try
							{
								getRouteByGMAV2RotaClienteExistente();
							} catch (Exception e)
							{
								e.printStackTrace();
							}
							myProgressDialog.dismiss();
						} else if (aplication.getTipoMapa().equals(GPSConstantes.MAPA_PEDIDOS_MAIOR_30))
						{
							Calendar data = Calendar.getInstance();
							SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd");

							String atual = formate.format(data.getTime());
							data.add(Calendar.DAY_OF_MONTH, -30);
							final String anterior = formate.format(data.getTime());

							h.post(new Runnable()
							{

								@Override
								public void run()
								{

									final Cursor cCli = banco.Sql_Select("CLIENTES", new String[]
											{
													"ENDERECO", "CIDADE", "NUMERO", "BAIRRO", "CEP", "ULT_DATA"
											}, "DATE(ULT_DATA) BETWEEN DATE('1971-01-01') AND DATE('" + anterior + "') OR ULT_DATA LIKE '';", "");
									
									for (int i = 0; i < cCli.getCount(); i++)
									{
										cCli.moveToPosition(i);
										cCli.getString(cCli.getColumnIndex("ULT_DATA"));
										LatLng posicaoCliente = getLocationEndereco(cCli.getString(cCli.getColumnIndex("CIDADE")), cCli.getString(cCli.getColumnIndex("ENDERECO")), cCli.getString(cCli.getColumnIndex("NUMERO")));
										if (posicaoCliente != null)
										{
											customAddMarkerRedCliente(posicaoCliente, "Titulo", "Corpo");
										}
										// cCli.getString(cCli.getColumnIndex("ULT_DATA"));
									}

									myProgressDialog.dismiss();
								};
							});
						}
						aplication.setTipoMapa("");
					}
				});
			}
		});

	}

	private void verificarString()
	{
		if (endereco.equals("Indefinido"))
		{
			endereco = "";
		}

		if (numero.equals("SN"))
		{
			numero = "";
		}

		if (numero.equals("0"))
		{
			numero = "";
		}

		if (bairro.equals("Indefinido"))
		{
			bairro = "";
		}

		if (cep.equals("     -   "))
		{
			cep = "";
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		/*
		 * new Thread(){ public void run(){ while(mapFrag.getMap() == null){ try
		 * { Thread.sleep(1000); } catch (InterruptedException e) {
		 * e.printStackTrace(); } } runOnUiThread(new Runnable(){ public void
		 * run(){ configMap(); } }); } }.start();
		 */
	}

	public void configMap()
	{
		map = mapFrag.getMap();
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		list = new ArrayList<LatLng>();

		update = getLocationGPS();
		map.animateCamera(update, 3000, new CancelableCallback()
		{

			public void onCancel()
			{
				Log.i("Script", "CancelableCallback.onCancel()");
			}

			public void onFinish()
			{
				Log.i("Script", "CancelableCallback.onFinish()");
			}
		});
		// map.moveCamera(update);

		// MARKERS
		// customAddMarker(new LatLng(-23.564224, -46.653156), "Marcador 1",
		// "O Marcador 1 foi reposicionado");
		// customAddMarker(new LatLng(-23.564205, -46.653102), "Marcador 2",
		// "O Marcador 2 foi reposicionado");

		map.setInfoWindowAdapter(new InfoWindowAdapter()
		{

			public View getInfoWindow(Marker marker)
			{
				LinearLayout ll = new LinearLayout(RotaMapa.this);
				ll.setPadding(20, 20, 20, 20);
				ll.setBackgroundColor(Color.WHITE);

				TextView tv = new TextView(RotaMapa.this);
				tv.setText(Html.fromHtml("<b><font color=\"#000000\">" + marker.getTitle() + ":</b> " + marker.getSnippet() + "</font>"));
				ll.addView(tv);

				// Button bt = new Button(RotaMapa.this);
				// bt.setText("Botão");
				// bt.setBackgroundColor(Color.RED);
				// bt.setOnClickListener(new Button.OnClickListener()
				// {
				// public void onClick(View v)
				// {
				// Log.i("Script", "Botão clicado");
				// }
				//
				// });
				//
				// ll.addView(bt);

				return ll;
			}

			public View getInfoContents(Marker marker)
			{
				TextView tv = new TextView(RotaMapa.this);
				tv.setText(Html.fromHtml("<b><font color=\"#ff0000\">" + marker.getTitle() + ":</font></b> " + marker.getSnippet()));

				return tv;
			}
		});

		// EVENTS
		map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener()
		{

			public void onCameraChange(CameraPosition cameraPosition)
			{
				// Log.i("Script", "setOnCameraChangeListener()");
				//
				// if (marker != null)
				// {
				// marker.remove();
				// }
				// customAddMarker(new LatLng(cameraPosition.target.latitude,
				// cameraPosition.target.longitude), "1: Marcador Alterado",
				// "O Marcador foi reposicionado");

			}
		});

		// map.setOnMapClickListener(new GoogleMap.OnMapClickListener()
		// {
		// public void onMapClick(LatLng latLng)
		// {
		// Log.i("Script", "setOnMapClickListener()");
		//
		// if (marker != null)
		// {
		// marker.remove();
		// }
		// customAddMarker(new LatLng(latLng.latitude, latLng.longitude),
		// "2: Marcador Alterado", "O Marcador foi reposicionado");
		// list.add(latLng);
		// drawRoute();
		// }
		// });

		// map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
		// {
		// public boolean onMarkerClick(Marker marker)
		// {
		// Log.i("Script", "3: Marker: " + marker.getTitle());
		// return false;
		// }
		// });

		// map.setOnInfoWindowClickListener(new
		// GoogleMap.OnInfoWindowClickListener()
		// {
		// public void onInfoWindowClick(Marker marker)
		// {
		// Log.i("Script", "4: Marker: " + marker.getTitle());
		// }
		// });
	}

	public void customAddMarkerRed(LatLng latLng, String title, String snippet)
	{
		MarkerOptions options = new MarkerOptions();
		options.position(latLng).title(title).snippet(snippet).draggable(true);
		options.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));

		marker = map.addMarker(options);
	}

	public void customAddMarkerRedCliente(LatLng latLng, String title, String snippet)
	{
		MarkerOptions options = new MarkerOptions();
		options.position(latLng).title(title).snippet(snippet).draggable(true);
		options.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));

		marker = map.addMarker(options);
	}

	public void customAddMarkerBlue(LatLng latLng, String title, String snippet)
	{
		MarkerOptions options = new MarkerOptions();
		options.position(latLng).title(title).snippet(snippet).draggable(true);
		options.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_blue));

		marker = map.addMarker(options);
	}

	public void drawRoute()
	{
		PolylineOptions po;
		PolylineOptions poBorder;

		if (polyline == null)
		{
			po = new PolylineOptions();
			poBorder = new PolylineOptions();

			for (int i = 0, tam = list.size(); i < tam; i++)
			{
				po.add(list.get(i));
				poBorder.add(list.get(i));
			}

			po.color(R.color.cor_borda_mapa).width(10);
			poBorder.color(R.color.cor_linha_mapa).width(4);
			polyline = map.addPolyline(poBorder);
			polyline = map.addPolyline(po);
		} else
		{
			polyline.setPoints(list);
		}
	}

	public void getDistance(View view)
	{
		/*
		 * double distance = 0;
		 * 
		 * for(int i = 0, tam = list.size(); i < tam; i++){ if(i < tam - 1){
		 * distance += distance(list.get(i), list.get(i+1)); } }
		 */

		Toast.makeText(RotaMapa.this, "Distancia: " + distance + " metros", Toast.LENGTH_LONG).show();
	}

	public static double distance(LatLng StartP, LatLng EndP)
	{
		double lat1 = StartP.latitude;
		double lat2 = EndP.latitude;
		double lon1 = StartP.longitude;
		double lon2 = EndP.longitude;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return 6366000 * c;
	}

	public void getLocation(View view)
	{
		Geocoder gc = new Geocoder(RotaMapa.this);

		List<Address> addressList;
		try
		{
			// addressList = gc.getFromLocation(list.get(list.size() -
			// 1).latitude, list.get(list.size() - 1).longitude, 1);
			addressList = gc.getFromLocationName("Rua Vergueiro, São Paulo, São Paulo, Brasil", 1);

			String address = "Rua: " + addressList.get(0).getThoroughfare() + "\n";
			address += "Cidade: " + addressList.get(0).getSubAdminArea() + "\n";
			address += "Estado: " + addressList.get(0).getAdminArea() + "\n";
			address += "País: " + addressList.get(0).getCountryName();

			LatLng ll = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());

			// Toast.makeText(MainActivity.this, "Local: "+address,
			// Toast.LENGTH_LONG).show();
			// Toast.makeText(RotaMapa.this, "LatLng: " + ll,
			// Toast.LENGTH_LONG).show();

		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// gps = new GPSTracker(RotaMapa.this);
		//
		// // Check if GPS enabled
		// if(gps.canGetLocation()) {
		//
		// double latitude = gps.getLatitude();
		// double longitude = gps.getLongitude();
		//
		// // \n is for new line
		// Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
		// + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
		// } else {
		// // Can't get location.
		// // GPS or network is not enabled.
		// // Ask user to enable GPS/network in settings.
		// gps.showSettingsAlert();
		// }

	}

	public static JSONObject getLocationInfo(String address)
	{
		StringBuilder stringBuilder = new StringBuilder();
		try
		{

			address = address.replaceAll(" ", "%20");

			HttpPost httppost = new HttpPost("http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false");
			HttpClient client = new DefaultHttpClient();
			HttpResponse response;
			stringBuilder = new StringBuilder();

			response = client.execute(httppost);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1)
			{
				stringBuilder.append((char) b);
			}
		} catch (ClientProtocolException e)
		{
			e.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject = new JSONObject(stringBuilder.toString());
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonObject;
	}

	private static List<Address> getAddrByWeb(JSONObject jsonObject)
	{
		List<Address> res = new ArrayList<Address>();
		try
		{
			JSONArray array = (JSONArray) jsonObject.get("results");
			for (int i = 0; i < array.length(); i++)
			{
				Double lon = new Double(0);
				Double lat = new Double(0);
				String name = "";
				try
				{
					lon = array.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");

					lat = array.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
					name = array.getJSONObject(i).getString("formatted_address");
					Address addr = new Address(Locale.getDefault());
					addr.setLatitude(lat);
					addr.setLongitude(lon);
					addr.setAddressLine(0, name != null ? name : "");
					res.add(addr);
				} catch (JSONException e)
				{
					e.printStackTrace();

				}
			}
		} catch (JSONException e)
		{
			e.printStackTrace();

		}

		return res;
	}

	public LatLng getLocationEndereco(boolean gerarMarcaMapa)
	{
		LatLng ll = null;
		Geocoder gc = new Geocoder(RotaMapa.this);

		List<Address> addressList;
		try
		{
			// addressList = gc.getFromLocation(list.get(list.size() -
			// 1).latitude, list.get(list.size() - 1).longitude, 1);
			if (endereco.equals("") && numero.equals(""))
			{
				addressList = getAddrByWeb(getLocationInfo(cidade + ", Brasil"));
			} else
			{
				addressList = getAddrByWeb(getLocationInfo(endereco + " " + numero + ", " + cidade + ", Brasil"));
			}

			// String address = "Rua: " + addressList.get(0).getThoroughfare() +
			// "\n";
			// address += "Cidade: " + addressList.get(0).getSubAdminArea() +
			// "\n";
			// address += "Estado: " + addressList.get(0).getAdminArea() + "\n";
			// address += "País: " + addressList.get(0).getCountryName();

			ll = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());

			if (gerarMarcaMapa)
			{
				customAddMarkerRed(ll, "Final", "Final");
			}

			// Toast.makeText(MainActivity.this, "Local: "+address,
			// Toast.LENGTH_LONG).show();
			// Toast.makeText(RotaMapa.this, "LatLng: " + ll,
			// Toast.LENGTH_LONG).show();
		} catch (IndexOutOfBoundsException e)
		{
			Toast.makeText(RotaMapa.this, endereco + ", " + cidade + " não existe", Toast.LENGTH_LONG).show();
		}

		return ll;
	}

	public LatLng getLocationEndereco(String cidade, String endereco, String numero)
	{
		this.cidade = cidade;
		this.endereco = endereco;
		this.numero = numero;
		verificarString();
		return getLocationEndereco(false);
	}

	public CameraUpdate getLocationGPS()
	{
		gps = new GPSTracker(RotaMapa.this);
		CameraUpdate update = null;

		// Check if GPS enabled
		if (gps.canGetLocation())
		{

			double latitude = gps.getLatitude();
			double longitude = gps.getLongitude();
			posicaoAtual = new LatLng(latitude, longitude);

			if (latitude == 0.0)
			{
				posicaoAtual = posicaoBrasil;
			}

			CameraPosition cameraPosition = new CameraPosition.Builder().target(posicaoAtual).zoom(15).bearing(40).tilt(0).build();
			update = CameraUpdateFactory.newCameraPosition(cameraPosition);

			// \n is for new line
			Toast.makeText(getApplicationContext(), "Sua posição autal - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_SHORT).show();
		} else
		{
			// Can't get location.
			// GPS or network is not enabled.
			// Ask user to enable GPS/network in settings.
			gps.showSettingsAlert();

			posicaoAtual = posicaoBrasil;

			CameraPosition cameraPosition = new CameraPosition.Builder().target(posicaoAtual).zoom(0).bearing(0).tilt(0).build();
			update = CameraUpdateFactory.newCameraPosition(cameraPosition);
		}
		customAddMarkerBlue(posicaoAtual, "Posição Atual", "Atual");
		return update;
	}

	/*
	 * ***************************************** ROTA
	 * *****************************************
	 */

	public void getRouteByGMAV2(View view) throws UnsupportedEncodingException
	{
		// EditText etO = (EditText) findViewById(R.id.origin);
		// EditText etD = (EditText) findViewById(R.id.destination);
		// String origin = URLEncoder.encode(etO.getText().toString(), "UTF-8");
		// String destination = URLEncoder.encode(etD.getText().toString(),
		// "UTF-8");
		posicaoFinal = getLocationEndereco(true);
		getRoute(posicaoAtual, posicaoFinal);
	}

	public void getRouteByGMAV2RotaClienteExistente() throws UnsupportedEncodingException
	{
		// EditText etO = (EditText) findViewById(R.id.origin);
		// EditText etD = (EditText) findViewById(R.id.destination);
		// String origin = URLEncoder.encode(etO.getText().toString(), "UTF-8");
		// String destination = URLEncoder.encode(etD.getText().toString(),
		// "UTF-8");
		posicaoFinal = getLocationEndereco(true);
		getRoute(posicaoAtual, posicaoFinal);
	}

	// WEB CONNECTION
	// public void getRoute(final String origin, final String destination){
	public void getRoute(final LatLng origin, final LatLng destination)
	{
		new Thread()
		{
			public void run()
			{
				try
				{
					/*
					 * String url=
					 * "http://maps.googleapis.com/maps/api/directions/json?origin="
					 * + origin+"&destination=" + destination+"&sensor=false";
					 */
					try
					{
						String url = "http://maps.googleapis.com/maps/api/directions/json?origin=" + posicaoAtual.latitude + "," + posicaoAtual.longitude + "&destination=" + posicaoFinal.latitude + "," + posicaoFinal.longitude + "&sensor=false";

						HttpResponse response;
						HttpGet request;
						AndroidHttpClient client = AndroidHttpClient.newInstance("route");

						request = new HttpGet(url);

						response = client.execute(request);
						final String answer = EntityUtils.toString(response.getEntity());

						runOnUiThread(new Runnable()
						{
							public void run()
							{
								try
								{
									// Log.i("Script", answer);
									list = buildJSONRoute(answer);
									drawRoute();
								} catch (JSONException e)
								{
									e.printStackTrace();
								}
							}
						});

					} catch (IOException e)
					{
						e.printStackTrace();
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void gerarToast(String mensagem)
	{
		Toast.makeText(RotaMapa.this, mensagem, Toast.LENGTH_LONG);
	}

	// PARSER JSON
	public List<LatLng> buildJSONRoute(String json) throws JSONException
	{
		JSONObject result = new JSONObject(json);
		JSONArray routes = result.getJSONArray("routes");

		distance = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getInt("value");

		JSONArray steps = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
		List<LatLng> lines = new ArrayList<LatLng>();

		for (int i = 0; i < steps.length(); i++)
		{
			Log.i("Script", "STEP: LAT: " + steps.getJSONObject(i).getJSONObject("start_location").getDouble("lat") + " | LNG: " + steps.getJSONObject(i).getJSONObject("start_location").getDouble("lng"));

			String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");

			for (LatLng p : decodePolyline(polyline))
			{
				lines.add(p);
			}

			Log.i("Script", "STEP: LAT: " + steps.getJSONObject(i).getJSONObject("end_location").getDouble("lat") + " | LNG: " + steps.getJSONObject(i).getJSONObject("end_location").getDouble("lng"));
		}

		return (lines);
	}

	// DECODE POLYLINE
	private List<LatLng> decodePolyline(String encoded)
	{

		List<LatLng> listPoints = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len)
		{
			int b, shift = 0, result = 0;
			do
			{
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do
			{
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
			Log.i("Script", "POL: LAT: " + p.latitude + " | LNG: " + p.longitude);
			listPoints.add(p);
		}
		return listPoints;
	}
}
