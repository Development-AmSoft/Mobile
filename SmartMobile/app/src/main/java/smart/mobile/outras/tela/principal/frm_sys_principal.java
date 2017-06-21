package smart.mobile.outras.tela.principal;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import smart.mobile.PrincipalClasse;
import smart.mobile.R;
import smart.mobile.outras.tela.mapa.RotaMapa;
import smart.mobile.cadastro.coletor.frm_cad_estoque;
import smart.mobile.cadastro.sugestao.frm_cad_sugestao;
import smart.mobile.consulta.cliente.frm_cons_clientes;
import smart.mobile.consulta.metas.frm_cons_metas;
import smart.mobile.consulta.pedido.frm_cons_pedidos;
import smart.mobile.consulta.produtos.frm_cons_produtos;
import smart.mobile.consulta.relatorio.frm_sys_relatorios;
import smart.mobile.consulta.saldos.frm_cons_saldo;
import smart.mobile.consulta.titulos.frm_cons_titulos;
import smart.mobile.consulta.vendas.frm_cons_vendas;
import smart.mobile.cadastro.pedido.frm_dialog_pedido_sinc_adpter;
import smart.mobile.outras.tela.sobre.frm_sys_sobre;
import smart.mobile.outras.tela.config.frm_sys_config;
import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.DB_ServerHost;
import smart.mobile.outras.sincronismo.download.DB_Sincroniza;
import smart.mobile.utils.ConfiguracaoInicialTela;
import smart.mobile.utils.DeviceUuidFactory;
import smart.mobile.utils.error.ErroGeralController;
import smart.mobile.utils.GridViewHeigth.ExpandableHeightGridView;
import smart.mobile.utils.gps.GPSConstantes;
import smart.mobile.utils.update.UpdateApp;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class frm_sys_principal extends Activity implements OnItemClickListener {

	private RelativeLayout r1;
	private DB_LocalHost banco;
	private Boolean sair;
	private GridView menuList;
	private Menu principal;

	ProgressDialog myProgressDialog = null;
	ProgressDialog myProgressDialogSimples = null;
	private Handler handler = new Handler();

	private final int MN_SOBRE = 0;
	private final int MN_OPCOEs = 1;

	private TextView lblEmpresa;
	private TextView lblVendedor;
	private Context context;
	private Handler handlerNovo;
	private TextView lblTitulo;

	private ImageView menu;

	private PrincipalClasse aplication;
	private AlertDialog.Builder alertDialog;
	private DB_ServerHost server;
	private ExpandableHeightGridView mAppsGrid;

	private final static int PEDIDO = 0;
	private final static int CONS_CLIENTES = 1;
	private final static int CONS_PRODUTOS = 2;
	private final static int PENDENCIAS = 3;
	private final static int RELATORIOS = 4;
	private final static int SALDO_FLEX = 5;
	private final static int METAS = 6;
	private final static int VENDAS = 7;
	private final static int SINCRONIZAR = 8;
	private final static int SMARTWEB = 9;
	private final static int MAPAS = 10;
	private final static int COLETOR = 11;
	private final static int SUGESTAO = 12;
	private final static int CONEXAO = 13;
	private final static int SOBRE = 14;
	private final static int CONFIGURAR = 15;

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	@Override
	public void onCreate(Bundle icicle) {
		context = this;

		// carrega layout da tela

		sair = false;
		super.onCreate(icicle);
		setContentView(R.layout.activity_ctela_base);
		setTitle("SmartMobile - Força de Vendas");

		ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.frmprincipal_layout, true);

		// carrega empresa e vendedor
		banco = new DB_LocalHost(this);
		banco.DB_ConfigLoad();
		banco.DB_SaldoFlexLoad();

		ErroGeralController erro = new ErroGeralController(this, banco);

		mAppsGrid = (ExpandableHeightGridView) findViewById(R.id.menu_list);
		mAppsGrid.setExpanded(true);

		NetworkInfo info = null;
		info = ((ConnectivityManager) this.getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

		if (info != null) {
			if (info.isConnected()) {
				verificarVersaoApp();
				verificarMensagemResposta();
			} else if (info.isRoaming()) {
				verificarVersaoApp();
				verificarMensagemResposta();
			}
		}

		menuList = (ExpandableHeightGridView) findViewById(R.id.menu_list);
		((ExpandableHeightGridView) menuList).setExpanded(true);
		menuList.setAdapter(new frm_sys_principal_itens(this));
		menuList.setOnItemClickListener(this);
		menuList.setDrawSelectorOnTop(true);

		// mapeia componentes de config
		lblEmpresa = (TextView) findViewById(R.id.textView2);
		lblVendedor = (TextView) findViewById(R.id.textView3);

		aplication = (PrincipalClasse) getApplication();

		lblTitulo = (TextView) findViewById(R.id.titulo);
		lblTitulo.setText(getTitle());

	}

	private void verificarMensagemResposta() {
		DB_ServerHost serverGroup = new DB_ServerHost(context, banco.ServidorOnline, "Group");

		DeviceUuidFactory uuidController = new DeviceUuidFactory(context);
		String uuid = uuidController.getDeviceUuid().toString();

		String select = "SELECT convert(varchar,DATA,103), MENSAGEM, RETORNO, MENSAGEMID FROM VW_VERSOES_MOBILES_MENSAGENS WHERE (UUID = '" + uuid + "') AND (LIDA = 0) and  (ltrim(rtrim(isnull(retorno,''))) <> '');";

		String retorno = serverGroup.Sql_Select(select);

		final String[] retornoArray = retorno.split("#l#");

		if (retornoArray.length > 0 && !retornoArray[0].isEmpty()  && !retornoArray[0].equalsIgnoreCase("")) {
			final String[] colunas = retornoArray[0].split("#c#");// DATA, // MENSAGEM, // RETORNO
			if (colunas[0].indexOf("SQLException") == -1) {

				AlertDialog ad = new AlertDialog.Builder(frm_sys_principal.this).create();
				// ad.setCancelable(false); // This blocks the 'BACK' button
				ad.setTitle("SmartMobile");
				ad.setIcon(R.drawable.ico_info);
				ad.setMessage("Você recebeu uma Mensagem da AmSoft [Equipe de Projetos], deseja visualizá-la ?");

				ad.setButton("Sim", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(context, frm_cad_sugestao.class);
						intent.putExtra("resposta", retornoArray[0]);
						context.startActivity(intent);
					}
				});
				ad.setButton2("Não", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

					}
				});

				ad.show();
			}
		}

	}

	private void verificarSincronizar() {
		try {
			Cursor rs5 = banco.db.rawQuery("select VERSAO, SERVIDOR, BANCO, EMPRESAID, EMPRESA, VENDEDORID from CONFIG", null);
			rs5.moveToFirst();

			try {
				PackageManager manager = context.getPackageManager();
				PackageInfo info = manager.getPackageInfo("smart.mobile", 0);
				double name = Double.parseDouble(info.versionName);

				if (name > rs5.getDouble(0)) {
					if (verificaExisteConf(rs5)) {
						executarSinc();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (SQLiteException e) {
			Cursor rs5 = banco.db.rawQuery("select SERVIDOR, BANCO, EMPRESAID, EMPRESA, VENDEDORID from CONFIG", null);
			rs5.moveToFirst();

			if (verificaExisteConf(rs5)) {
				executarSinc();
			}

		}

	}

	private void executarSinc() {

		DB_Sincroniza dbSync = new DB_Sincroniza(context, banco);
		dbSync.metodoSinc(false,false, handler);
		// carrega empresa e vendedor
		banco.closeHelper();
		banco = new DB_LocalHost(this);
		banco.DB_ConfigLoad();
		banco.DB_SaldoFlexLoad();

	}

	private boolean verificaExisteConf(Cursor cursor) {
		boolean retorno = true;

		if (cursor.getString(cursor.getColumnIndex("SERVIDOR")).isEmpty()) {
			retorno = false;
		}

		if (cursor.getString(cursor.getColumnIndex("BANCO")).isEmpty()) {
			retorno = false;
		}

		if (cursor.getInt(cursor.getColumnIndex("EMPRESAID")) == 0) {
			retorno = false;
		}

		if (cursor.getString(cursor.getColumnIndex("EMPRESA")).equalsIgnoreCase("Indefinida")) {
			retorno = false;
		}

		if (cursor.getInt(cursor.getColumnIndex("VENDEDORID")) == 0) {
			retorno = false;
		}

		return retorno;
	}

	@Override
	public void onResume() {
		super.onResume();

		lblEmpresa.setText(banco.NomeEmpresa);
		lblVendedor.setText(banco.NomeVendedor);

		aplication.setEmpresa(banco.NomeEmpresa);
		aplication.setVendedor(banco.NomeVendedor);

		banco.closeHelper();
		banco = new DB_LocalHost(this);
		banco.DB_ConfigLoad();
		banco.DB_SaldoFlexLoad();
		super.onStart();
		Thread t = new Thread() {
			public void run() {

				NetworkInfo info = null;
				info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

				if (info != null) {
					if (info.isConnected()) {
						verificarSincronizar();
					} else if (info.isRoaming()) {
						verificarSincronizar();
					}
				}
			}
		};
		t.start();

		// Log.v("Szakbarbar", "Need the cursor from the Service");
		// CarregaDados();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		banco.closeHelper();
		super.onDestroy();
	}

	@Override
	public void finish() {

		if (sair) {
			super.finish();
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Deseja realmente finalizar ?").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				sair = true;
				moveTaskToBack(true);
				// FrmMenuPrincipalActivity.this.finish();
			}
		}).setNegativeButton("Não", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemSobre = menu.add(0, MN_SOBRE, Menu.NONE, "Sobre");
		MenuItem itemOpcoes = menu.add(0, MN_OPCOEs, Menu.NONE, "Opções");

		// Assign icons.
		// Assign icons
		// itemSobre.setIcon(R.drawable.ico_info);
		// itemOpcoes.setIcon(R.drawable.ico_opcoes);

		// Allocate shortcuts to each of them.
		itemSobre.setShortcut('0', 's');
		itemOpcoes.setShortcut('1', 'o');

		principal = menu;

		return true;
	}

	private void op_Opcoes() {

		CharSequence[] items = new CharSequence[] { "Criar Atalho ...", "Finalizar Sistema" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("SmartMobile - Opções");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				if (item == 0) {

					dialog.dismiss();

					AlertDialog ad = new AlertDialog.Builder(frm_sys_principal.this).create();
					ad.setCancelable(true); // This blocks the 'BACK' button
					ad.setTitle("SmartMobile - Força de Vendas");
					ad.setMessage("Será criado um atalho para o sistema na sua tela inicial !!!");
					ad.setButton("Confirmar", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

							CriarAtalho();
							dialog.dismiss();
						}
					});
					ad.show();

				} else if (item == 1) {

					dialog.dismiss();
					finish();

				}

			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {

		case MN_OPCOEs:
			op_Opcoes();
			break;

		case MN_SOBRE:

			intent = new Intent(frm_sys_principal.this, frm_sys_sobre.class);
			startActivity(intent);
			break;

		// Toast.makeText(this,ClienteSel + " selecionado",
		// Toast.LENGTH_LONG).show();

		// Intent intent = new Intent(frmPrincipal.this, frmCadCliente.class);
		// intent.putExtra("registro", registro);
		// intent.putExtra("algumaString", algumaString);
		// startActivity(intent);
		default:
			break;
		}
		return false;
	}

	public void onItemClick(AdapterView parent, View view, int position, long id) {
		// TODO Auto-generated method stub

		// Toast.makeText(this, position + " selected",
		// Toast.LENGTH_LONG).show();
		if (position == PEDIDO) {
			Intent intent = new Intent(frm_sys_principal.this, frm_cons_pedidos.class);
			startActivity(intent);
		} else if (position == CONS_CLIENTES) {
			Intent intent = new Intent(frm_sys_principal.this, frm_cons_clientes.class);
			startActivity(intent);
		} else if (position == CONS_PRODUTOS) {
			Intent intent = new Intent(frm_sys_principal.this, frm_cons_produtos.class);
			startActivity(intent);
		} else if (position == PENDENCIAS) {
			Intent intent = new Intent(frm_sys_principal.this, frm_cons_titulos.class);
			startActivity(intent);
		} else if (position == RELATORIOS) {
			Intent intent = new Intent(frm_sys_principal.this, frm_sys_relatorios.class);
			startActivity(intent);
		} else if (position == SALDO_FLEX) {
			Intent intent = new Intent(frm_sys_principal.this, frm_cons_saldo.class);
			startActivity(intent);
		} else if (position == METAS) {
			Intent intent = new Intent(frm_sys_principal.this, frm_cons_metas.class);
			startActivity(intent);
		} else if (position == CONFIGURAR) {

			Intent intent = new Intent(frm_sys_principal.this, frm_sys_config.class);
			startActivity(intent);

		} else if (position == SINCRONIZAR) {

			this.alertDialog = new AlertDialog.Builder(frm_sys_principal.this);
			LayoutInflater inflater = getLayoutInflater();
			View convertView = (View) inflater.inflate(R.layout.lay_dialog_pedidos_sincronizar, null);
			alertDialog.setView(convertView);
			alertDialog.setTitle("Pedidos para sincronizar");
			ListView lv = (ListView) convertView.findViewById(R.id.listViewPedidos);

			Cursor cursorPedidos = banco.db.rawQuery("SELECT ve._id, cli.NOME as NOME, ve.TOTAL, ve.SINCRONIZAR from vendas ve INNER JOIN clientes cli on cli.CPF_CNPJ = ve.CPF_CNPJ WHERE ve.SINCRONIZADO = 0", null);

			Cursor cursorSaldo = banco.db.rawQuery("select _id from flex", null);
			cursorSaldo.moveToFirst();

			startManagingCursor(cursorPedidos);

			ListAdapter ad = new frm_dialog_pedido_sinc_adpter(context, R.layout.lay_pedido_dialog_adpter, cursorPedidos, new String[] { "_id", "NOME", "TOTAL", "SINCRONIZAR" }, new int[] { R.id.checkSinc }, banco, cursorSaldo.getCount());

			lv.setAdapter(ad);

			alertDialog.setCancelable(true);
			alertDialog.setPositiveButton("Sincronizar", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					montarDialogConfirmarSinc();

				}
			});

			alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});

			cursorPedidos.moveToFirst();

			if (cursorPedidos.getCount() > 0) {
				alertDialog.show();
			} else {
				montarDialogConfirmarSinc();
			}

		} else if (position == SMARTWEB) {

			String ipserv;
			if (banco.ServidorOnline.indexOf(":") <= 0) {
				ipserv = "http://" + banco.ServidorOnline + ":8080/smarttools";
			} else {
				ipserv = "http://" + banco.ServidorOnline + "/smarttools";
			}

			AlertDialog ad = new AlertDialog.Builder(frm_sys_principal.this).create();
			// ad.setCancelable(false); // This blocks the 'BACK' button
			ad.setTitle("SmartMobile");
			ad.setIcon(R.drawable.conexao_pop);
			ad.setMessage("A plataforma online já está disponível! Entre em contato para configuração de acesso."/*
																												 * +
																												 * "\n\nPágina de Teste : "
																												 * +
																												 * ipserv
																												 */);
			ad.setButton("Visitar Página", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					String url = "";
					Intent i = new Intent(Intent.ACTION_VIEW);

					if (banco.ServidorOnline.indexOf(":") <= 0) {
						url = "http://" + banco.ServidorOnline + ":8080/smarttools";
					} else {
						url = "http://" + banco.ServidorOnline + "/smarttools";
					}

					i.setData(Uri.parse(url));
					startActivity(i);

				}
			});
			ad.show();

		} else if (position == CONEXAO) {

			banco.TestarConexao(frm_sys_principal.this);

		} else if (position == SOBRE) {

			Intent intent = new Intent(frm_sys_principal.this, frm_sys_sobre.class);
			startActivity(intent);

		} else if (position == MAPAS) {
			if (Build.VERSION.SDK_INT >= 14) {

				AlertDialog.Builder builder = new AlertDialog.Builder(frm_sys_principal.this);
				builder.setTitle("Tipo de Lista");
				final CharSequence[] choiceList =

				{ "Clientes sem pedidos a mais de 30 dias" };

				builder.setSingleChoiceItems(choiceList, banco.cons_cli_indexTIPO,

				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						if (which == 0) {

							aplication.setTipoMapa(GPSConstantes.MAPA_PEDIDOS_MAIOR_30);
							Intent intent = new Intent(frm_sys_principal.this, RotaMapa.class);
							startActivity(intent);
						}
						dialog.cancel();
					}

				});

				AlertDialog alert = builder.create();

				alert.show();
			} else {
				AlertDialog ad = new AlertDialog.Builder(frm_sys_principal.this).create();
				// ad.setCancelable(false); // This blocks the 'BACK' button
				ad.setTitle("SmartMobile");
				ad.setIcon(R.drawable.ico_info);
				ad.setMessage("Recurso funcina apenas com Android 4.0 ou superior.");

				ad.setButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				ad.show();
			}
		} else if (position == VENDAS) {

			NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

			if (info != null) {
				if (info.isConnected()) {
					Intent intent = new Intent(frm_sys_principal.this, frm_cons_vendas.class);
					startActivity(intent);
				} else if (info.isRoaming()) {
					Intent intent = new Intent(frm_sys_principal.this, frm_cons_vendas.class);
					startActivity(intent);
				} else {
					mensagemSemNet();
				}
			} else {
				mensagemSemNet();
			}

		} else if (position == SUGESTAO) {
			Intent intent = new Intent(frm_sys_principal.this, frm_cad_sugestao.class);
			startActivity(intent);
		} else if (position == COLETOR) {
			try {
				banco = new DB_LocalHost(this);
				banco.DB_ConfigLoad();
				banco.DB_SaldoFlexLoad();
			} catch (Exception e) {

			}

			Cursor cli0 = banco.db.rawQuery("SELECT coletor from VENDEDORES where VENDEDORID = " + banco.VendedorID, null);
			if (cli0.moveToFirst()) {
				if (cli0.getInt(0) == 1) {
					Intent intent = new Intent(frm_sys_principal.this, frm_cad_estoque.class);
					startActivity(intent);
				} else {
					if (!((Activity) context).isFinishing()) {
						// show dialog

						AlertDialog ad = new AlertDialog.Builder(frm_sys_principal.this).create();
						// ad.setCancelable(false); // This blocks the 'BACK'
						// button
						ad.setTitle("SmartMobile - Coletor");
						ad.setIcon(R.drawable.ico_info);
						ad.setMessage("Vendedor sem permissão de 'Coletor de Estoque'");

						ad.setButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});

						ad.show();
					}
				}
			}

		}

	}

	private void montarDialogConfirmarSinc() {


		AlertDialog.Builder builder = new AlertDialog.Builder(frm_sys_principal.this);
		//builder.setMessage("Todos os dados serão sincronizados, deseja continuar?").setCancelable(true).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
		builder.setMessage("Sincronizar também as 'Imagens dos Produtos' ?").setCancelable(true).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				// salva configuracoes
				// salvaConfig();

				// sincroniza tudo
				DB_Sincroniza dbSync = new DB_Sincroniza(frm_sys_principal.this);
				dbSync.Syncroniza_Geral(false,true);

			}
		}).setNegativeButton("Não", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				//dialog.cancel();
				DB_Sincroniza dbSync = new DB_Sincroniza(frm_sys_principal.this);
				dbSync.Syncroniza_Geral(false,false);
			}
		})
		// Set your icon here
				.setTitle("SmartMobile - Sincronizar").setIcon(R.drawable.ico_warning);

		AlertDialog alert = builder.create();
		alert.show();

	}

	public void mensagemSemNet() {
		AlertDialog ad = new AlertDialog.Builder(context).create();
		// ad.setCancelable(false); // This blocks the 'BACK' button
		ad.setTitle("SmartMobile - Internet");
		ad.setMessage("Para abrir essa consulta é preciso conexão com internet. Verifique se está conectado!");
		ad.setButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}
		});
		ad.show();
	}

	public void CriarAtalho() {

		try {
			Intent shortcutIntent;
			shortcutIntent = new Intent();
			shortcutIntent.setComponent(new ComponentName(frm_sys_principal.this.getPackageName(), ".frm_sys_principal"));

			shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			final Intent putShortCutIntent = new Intent();
			putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

			// Sets the custom shortcut's title
			putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "SmartMobile");
			putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(frm_sys_principal.this, R.drawable.ic_launcher));
			putShortCutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
			sendBroadcast(putShortCutIntent);
		} catch (Exception e) {
		}

	}

	private void Menu_Sincronizar() {

		final CharSequence[] items = { "USB", "Internet" };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Conexão");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				if (banco.VendedorID == "0") {

					Toast.makeText(frm_sys_principal.this, "Configure Empresa e Vendedor !!!", Toast.LENGTH_SHORT).show();

				}

			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	private void verificarVersaoApp() {

		handlerNovo = new Handler();
		Runnable run = new Runnable() {

			public void run() {
				URL url;
				try {
					url = new URL("https://play.google.com/store/apps/details?id=smart.mobile");

					URLConnection ucon = url.openConnection();
					ucon.setReadTimeout(5000);
					InputStream is = ucon.getInputStream();
					BufferedInputStream bis = new BufferedInputStream(is);

					byte[] contents = new byte[1024];
					int bytesRead = 0;
					String json = "";
					while ((bytesRead = bis.read(contents)) != -1) {
						json += new String(contents, 0, bytesRead);
					}
					
					String array[] = json.split("softwareVersion\">");
					array = array[1].split("</div>");
					
					final String versao = array[0].trim();
					
					
					PackageManager manager = context.getPackageManager();
					PackageInfo info = manager.getPackageInfo("smart.mobile", 0);
					int code = info.versionCode;
					final String name = info.versionName;
					Log.i("Versao APP Local", name);

					double servidor = 0;
					double app = 0;

					try {
						servidor = Double.parseDouble(versao);
						app = Double.parseDouble(name);

						if (servidor > app) {
							handlerNovo.post(new Runnable() {

								public void run() {
									AlertDialog.Builder builder = new AlertDialog.Builder(context);
									builder.setMessage("Sua versão do sistema [" + name + "] está desatualizada, Deseja atualizar automaticamente [" + versao + "] ?").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											try {
												myProgressDialogSimples = ProgressDialog.show(context, "Atualizando", "Baixando arquivos ...");
											} catch (Exception e) {
												// TODO: handle exception
											}

											Cursor cli0 = banco.Sql_Select("VENDAS", new String[] { "_id" }, "SINCRONIZADO = 0", "");

											if (cli0.getCount() == 0) {
												String url = "http://amsoft.com.br/download/SmartMobile.apk";

												UpdateApp atualizaApp = new UpdateApp(myProgressDialogSimples);
												atualizaApp.setContext(getApplicationContext());
												atualizaApp.execute(url);

											} else {
												AlertDialog.Builder builder = new AlertDialog.Builder(context);
												builder.setMessage("Não é permitido atualizar o sistema com pedidos pendentes!").setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {

													public void onClick(DialogInterface arg0, int arg1) {
														arg0.dismiss();
													}

												});
												AlertDialog a = builder.create();
												myProgressDialogSimples.dismiss();
												a.show();

											}
										}
									}).setNegativeButton("Não", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
									AlertDialog alert = builder.create();
									alert.setIcon(R.drawable.ico_info);
									alert.show();

								}
							});

						}
					} catch (Exception e) {
						throw e;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		};

		new Thread(run).start();

	}
}