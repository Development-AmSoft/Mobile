package smart.mobile.consulta.cliente;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.download.DB_Sincroniza;
import smart.mobile.PrincipalClasse;
import smart.mobile.R;
import smart.mobile.outras.tela.mapa.RotaMapa;
import smart.mobile.cadastro.cliente.frm_cad_cliente;
import smart.mobile.cadastro.pedido.frm_cad_pedido;
import smart.mobile.utils.ConfiguracaoInicialTela;
import smart.mobile.utils.error.ErroGeralController;
import smart.mobile.utils.gps.GPSConstantes;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class frm_cons_clientes extends Activity
{

	// variavel principal de acesso ao banco
	private DB_LocalHost banco;

	// definição de colunas da consulta
	int col_order = 0;
	boolean col_desc = true;
	final CharSequence[] col_banco =
	{
			"clientes.sincronizado", "clientes.nome", "clientes.fantasia", "clientes.cidade", "clientes.ult_data", "clientes.ult_total"
	};
	final CharSequence[] col_descricao =
	{
			"Situação", "Nome", "Fantasia", "Cidade", "Últ. Pedido - Data", "Últ. Pedido - Total R$"
	};

	// CharSequence[] statesTexto = {"Ativos", "Inativos", "Todos os Clientes"};
	// boolean[] statesOpcoes = {true, false, false};

	private EditText filterText = null;

	SimpleCursorAdapter adapter;
	TextView lblFiltro = null;
	EditText txtFiltro = null;

	private final int MN_VOLTAR = 0;
	private final int MN_OPCOES = 1;
	private final int MN_FILTRAR = 2;
	private final int MN_ORDENAR = 3;

	String ClienteID = "";
	String ClienteSel = "";
	long PedidoID = 0;
	Cursor c;

	private PrincipalClasse aplication;

	// BLOCO 1 - EVENTOS DA ACTIVITY

	public void onCreate(Bundle icicle)
	{

		super.onCreate(icicle);
		setContentView(R.layout.activity_ctela_base);
		// setContentView(R.layout.layoutpadrao_consgeral);
		setTitle("SmartMobile - Consulta de Clientes");

		ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.layoutpadrao_consgeral, false);

		RelativeLayout footer = (RelativeLayout) findViewById(R.id.FooterEsconder);
		footer.setVisibility(View.GONE);

		aplication = (PrincipalClasse) getApplication();

		// parametro de pedido
		try
		{
			Bundle b = getIntent().getExtras();
			PedidoID = b.getLong("pedidoid");
		} catch (Exception ex)
		{
		}

		// carrega dados do banco
		this.banco = new DB_LocalHost(this);
		
		ErroGeralController erro = new ErroGeralController(this, banco);

		// configura componente de filtro
		// filterText = (EditText) findViewById(R.id.edtFiltro);
		// filterText.addTextChangedListener(filterTextWatcher);
		lblFiltro = (TextView) findViewById(R.id.lblStatus);

		ImageButton btnTipoLista = (ImageButton) findViewById(R.id.btnTipoLista);
		btnTipoLista.setVisibility(View.VISIBLE);
		btnTipoLista.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{

				AlertDialog.Builder builder = new AlertDialog.Builder(frm_cons_clientes.this);
				builder.setTitle("Tipo de Lista");
				final CharSequence[] choiceList =

				{
						"Lista Simples", "Lista Completa"
				};

				builder.setSingleChoiceItems(choiceList, banco.cons_cli_indexTIPO,

				new DialogInterface.OnClickListener()
				{

					public void onClick(

					DialogInterface dialog,

					int which)
					{

						banco.cons_cli_indexTIPO = which;
						dialog.cancel();
						CarregaDados(false);

						/*
						 * Toast.makeText(
						 * 
						 * frm_cons_clientes.this,
						 * 
						 * "Select "+choiceList[which],
						 * 
						 * Toast.LENGTH_SHORT
						 * 
						 * )
						 * 
						 * .show();
						 */

					}

				});

				AlertDialog alert = builder.create();

				alert.show();

			}
		});

		ImageButton btnCad = (ImageButton) findViewById(R.id.btnCadastro);
		btnCad.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Cliente_Novo();
			}
		});

		ImageButton btnSync = (ImageButton) findViewById(R.id.btnSincroniza);
		btnSync.setVisibility(View.GONE);
		btnSync.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Cliente_Sincroniza(false);
			}
		});

		txtFiltro = (EditText) findViewById(R.id.edtFiltro);
		txtFiltro.setHint("Razão,Fant.,Cid.");
		txtFiltro.addTextChangedListener(new TextWatcher()
		{

			public void onTextChanged(CharSequence s, int start, int before, int count)
			{

			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{

			}

			public void afterTextChanged(Editable s)
			{
				CarregaDados(true);

			}
		});

	}

	@Override
	public void onResume()
	{
		super.onResume();

		// Log.v("Szakbarbar", "Need the cursor from the Service");
		CarregaDados(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemAdd = menu.add(0, MN_VOLTAR, Menu.NONE, "Voltar");
		MenuItem itemOpt = menu.add(0, MN_OPCOES, Menu.NONE, "Opções");
		MenuItem itemFil = menu.add(0, MN_FILTRAR, Menu.NONE, "Filtrar");
		MenuItem itemOrd = menu.add(0, MN_ORDENAR, Menu.NONE, "Ordenar");

		// Assign icons
		itemAdd.setIcon(R.drawable.ico_voltar);
		itemOpt.setIcon(R.drawable.ico_opcoes);
		itemFil.setIcon(R.drawable.ico_filtrar);
		itemOrd.setIcon(R.drawable.ico_order);

		// Allocate shortcuts to each of them.
		itemAdd.setShortcut('0', 'v');
		itemOpt.setShortcut('1', 's');
		itemFil.setShortcut('2', 'f');
		itemOrd.setShortcut('3', 'o');

		// submenus de opções
		// itemOpt.add(MN_OPCOES, MN_PEDIDO, 2, "Pedido");
		// itemOpt.add(MN_OPCOES, MN_TITULOS, 3, "Títulos");
		// itemOpt.add(MN_OPCOES, MN_CADASTRAR, 4, "Incluir Cliente");
		// itemOpt.add(MN_OPCOES, MN_ALTERAR, 5, "Alterar Cliente");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case MN_VOLTAR:

			finish();
			break;

		case MN_OPCOES:
			op_Opcoes();
			break;

		case MN_FILTRAR:
			op_Filtrar();
			break;

		case MN_ORDENAR:

			AlertDialog dialog;

			// final CharSequence[] items = {"High", "Normal", "Low"};

			AlertDialog.Builder builder = new AlertDialog.Builder(frm_cons_clientes.this);
			builder.setTitle("Ordenar por").setSingleChoiceItems(col_descricao, col_order, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item)
				{

					Toast.makeText(getApplicationContext(), "Ordenar por: " + col_descricao[item], Toast.LENGTH_SHORT).show();
					col_order = item;
					CarregaDados(true);
					dialog.dismiss();
				}
			});

			dialog = builder.create();
			dialog.show();

			break;

		}
		return false;
	}

	@Override
	public void finish()
	{
		super.finish();
		return;

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		banco.closeHelper();

	}

	// BLOCO 2 - EVENTOS DE CONSULTAS

	private String convText(TextView v, String text)
	{

		/*
		 * if (v.getId() == R.id.x_situacao){
		 * 
		 * 
		 * ((ImageView)
		 * findViewById(R.id.x_situacao)).setBackgroundDrawable(getResources
		 * ().getDrawable
		 * (R.drawable.ico_sync1));//v.setTextColor(getResources().
		 * getColor(R.color.solid_red)); Log.i("JAKSON","MUDA IMAGEM");
		 * 
		 * if(c.getDouble(4) <= 0) { ((ImageView)
		 * findViewById(R.id.imgStatus)).setBackgroundDrawable
		 * (getResources().getDrawable
		 * (R.drawable.ico_sync));//v.setTextColor(getResources
		 * ().getColor(R.color.solid_red)); //return "Pendente"; } else {
		 * ((ImageView)
		 * findViewById(R.id.imgStatus)).setBackgroundDrawable(getResources
		 * ().getDrawable(R.drawable.ico_sync1)); //return "Sincronizado"; }
		 * 
		 * } else
		 */if (v.getId() == R.id.txtStatus)
		{
			if (c.getDouble(4) <= 0)
			{
				v.setTextColor(getResources().getColor(R.color.all_cinza));
				return "Pendente";
			} else
			{
				v.setTextColor(getResources().getColor(R.color.cor_verde));
				return "Sincronizado";
			}
		}

		return text;

	}

	public void CarregaDados(boolean Ordernando)
	{

		// DEFININDO CAMPO DE ORDENAÇÂO -- COL 0 PADRAO
		String campoOrder = (String) col_banco[0] + " asc, ULT_DATA desc";

		// CASO ESTIVER ORDENANDO NO MENU
		if (Ordernando)
		{
			campoOrder = (String) col_banco[col_order];
			if (col_desc)
			{
				campoOrder = campoOrder + " desc";
				col_desc = false;
			} else
			{
				campoOrder = campoOrder + " asc";
				col_desc = true;
			}
		}
		;

		// DEFININDO VALOR DO FILTRO
		String filtro = txtFiltro.getText().toString();

		// DEFININDO FILTRO DE ATIVOS
		String strAtivo = "";
		if (banco.cons_cli_indexFiltro == 0)
		{
			strAtivo = "((SINCRONIZADO = 0) OR (SINCRONIZADO = 1) OR (SINCRONIZADO = 3))";
		} else if (banco.cons_cli_indexFiltro == 1)
		{
			strAtivo = "(SINCRONIZADO = 2)";
		} else
		{
			strAtivo = "(SINCRONIZADO > -1)";
		}

		// DEFININDO CONSULTA
		if (filtro.equals(""))
		{
			c = banco.db.query("CLIENTES", new String[]
			{
					"_id", "NOME", "FANTASIA", "CPF_CNPJ", "CIDADE", "ULT_DATA", "ULT_TOTAL", "SINCRONIZADO"
			}, strAtivo, null, null, null, campoOrder);
		} else
		{
			c = banco.db.query("CLIENTES", new String[]
			{
					"_id", "NOME", "FANTASIA", "CPF_CNPJ", "CIDADE", "ULT_DATA", "ULT_TOTAL", "SINCRONIZADO"
			}, "((NOME LIKE " + "'%" + filtro + "%') OR (FANTASIA LIKE " + "'%" + filtro + "%') OR (CIDADE LIKE " + "'%" + filtro + "%')) AND " + strAtivo, null, null, null, campoOrder);
		}

		// INICIANDO CURSOR
		startManagingCursor(c);

		adapter = new frm_cons_clientes_adapter(this, R.layout.lay_cons_clientes, c, new String[]
		{
				"NOME", "FANTASIA", "CIDADE", "SINCRONIZADO"
		}, new int[]
		{
				R.id.txtNome, R.id.txtFantasia, R.id.txtCidade, R.id.txtSituacao
		}, banco.cons_cli_indexTIPO);
		/*
		 * 
		 * <ImageView android:id="@+id/x_situacao" android:layout_gravity="left"
		 * android:background="@drawable/ico_sync" android:layout_width="48px"
		 * android:layout_height="48px" android:visibility="invisible"/>
		 */

		/*
		 * adapter = new SimpleCursorAdapter(this, R.layout.lay_cons_clientes,
		 * c, new String[] { "NOME", "FANTASIA", "CIDADE", "SINCRONIZADO"}, new
		 * int[] { R.id.n_nome, R.id.n_fantasia, R.id.n_cidade,
		 * R.id.n_situacao}) {
		 * 
		 * @Override public void setViewText(TextView v, String text) {
		 * super.setViewText(v, convText(v, text)); }
		 * 
		 * };
		 */

		// ListAdapter adapter = new SimpleCursorAdapter(this,
		// R.layout.layoutpadrao_consitens, c,
		// new String[] { "NOME", "FANTASIA" },
		// new int[] { R.id.text1, R.id.text2 });
		ListAdapter lsadapter = adapter;
		ListView listaCons = (ListView) findViewById(R.id.vendaListView);
		LayoutParams list = (LayoutParams) listaCons.getLayoutParams();
		list.height = 200;
		listaCons.setLayoutParams(list);

		listaCons.setAdapter(lsadapter);
		listaCons.setFastScrollEnabled(true);

		lblFiltro.setText("Registros: " + lsadapter.getCount());

		listaCons.setOnItemClickListener(new OnItemClickListener()
		{

			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				// @todo

				ClienteID = id + "";
				op_Opcoes();

				// Toast.makeText(frmConsClientes.this,"O id do item selecionado é: "+
				// String.valueOf(id), Toast.LENGTH_LONG).show();
			}

		});

		// adapter = new
		// ArrayAdapter<String>(this,R.layout.layoutpadrao_consitens,
		// R.id.text1, banco.Sql_Select("CLIENTES", "NOME", "_ID DESC"));
		// ListView listaCons = (ListView) findViewById(R.id.listView1);
		// listaCons.setAdapter(adapter);
		// lblFil.setText(adapter.getCount() + " registros encontrados !!!");

		/*
		 * String[] from = new String[]{"NOME","FANTASIA"}; int[] to = new
		 * int[]{R.id.text1,R.id.text2};
		 * 
		 * SimpleCursorAdapter adap = new
		 * SimpleCursorAdapter(this,R.layout.layoutpadrao_consitens,
		 * banco.Clientes_Busca(), from, to);
		 */

	}

	// BLOCO 3 - EVENTOS DE MENUS

	private void SelecionaCliente()
	{
		Cursor cPed = banco.Sql_Select("CLIENTES", new String[]
		{
				"NOME", "SINCRONIZADO", "LIMITE", "CPF_CNPJ"
		}, "_id = " + String.valueOf(ClienteID), "");// , "SINCRONIZADO = 0",
														// "");
		if (cPed.moveToFirst())
		{
			ClienteID = cPed.getString(3);
			if (cPed.getInt(1) == 2)
			{

				AlertDialog.Builder msg = new AlertDialog.Builder(frm_cons_clientes.this);
				msg.setMessage("Cliente está Inativo !!!").show();

			} else if (banco.Banco.equalsIgnoreCase("DNA") && (cPed.getDouble(2) <= 0.00))
			{

				// 16/09/2013 - Jakson Gava - validação - apenas DNA
				// 16/09/2013 - Jakson Gava - validação - apenas DNA

				AlertDialog ad = new AlertDialog.Builder(frm_cons_clientes.this).create();
				ad.setCancelable(true); // This blocks the 'BACK' button
				ad.setTitle("SmartMobile - Força de Vendas");
				ad.setIcon(R.drawable.ico_warning);
				ad.setMessage("Cliente com Limite de Crédito indisponível. Favor entrar em contato com a empresa.");
				ad.setButton("Ok", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});
				ad.show();
			} else
			{

				if (PedidoID > 0)
				{

					// AlertDialog.Builder msg = new
					// AlertDialog.Builder(frm_cons_clientes.this);
					// msg.setMessage("troca Cliente !!!").show();

					SQLiteStatement updatePedido = banco.db.compileStatement("update vendas set CPF_CNPJ = ? where _id = " + String.valueOf(PedidoID));
					updatePedido.bindString(1, ClienteID);
					updatePedido.executeInsert();
					finish();

				} else
				{

					banco.VERIFICA_DEL_PEDIDOZERO();
					banco.TB_VENDAS_INSERIR(0, 0, ClienteID, 0, 0, 0.00, "",0);

					Intent intent = new Intent(frm_cons_clientes.this, frm_cad_pedido.class);
					Bundle b = new Bundle();
					b.putLong("pedidoid", banco.getMaxId("VENDAS"));
					intent.putExtras(b);
					startActivity(intent);

				}
			}

		}
	}

	private void op_Opcoes()
	{

		if (PedidoID > 0)
		{

			SelecionaCliente();

		} else
		{

			final CharSequence[] items =
			{
					"(+) Pedido", "Cadastrar", "Editar", "Remover", "Telefonar", "Ver Rota - Mapa"
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Cliente - Opções");
			builder.setItems(items, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item)
				{

					// Toast.makeText(getApplicationContext(), items[item],
					// Toast.LENGTH_SHORT).show();
					if (item == 0)
					{

						SelecionaCliente();

					}
					if (item == 1)
					{
						Cliente_Novo();
					} else if (item == 2)
					{

						Cursor cPed = banco.Sql_Select("CLIENTES", new String[]
						{
							"CPF_CNPJ"
						}, "_id = " + String.valueOf(ClienteID), "");// ,
																		// "SINCRONIZADO = 0",
																		// "");
						if (cPed.moveToFirst())
						{
							ClienteID = cPed.getString(0);
						}
						Intent intent = new Intent(frm_cons_clientes.this, frm_cad_cliente.class);
						Bundle b = new Bundle();
						b.putString("clienteid", ClienteID);
						intent.putExtras(b);
						startActivity(intent);
					} else if (item == 3)
					{

						if (banco.getIsSincronizadoCpf("CLIENTES", ClienteID))
						{

							banco.MostraMsg(frm_cons_clientes.this, "Cliente já está sincronizado !!!");

						} else
						{

							DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int which)
								{
									switch (which)
									{
									case DialogInterface.BUTTON_POSITIVE:

										banco.db.delete("CLIENTES", "_id = " + Integer.parseInt(ClienteID), null);
										CarregaDados(false);

									case DialogInterface.BUTTON_NEGATIVE:
										// No button clicked
										break;
									}
								}
							};

							AlertDialog.Builder builder2 = new AlertDialog.Builder(frm_cons_clientes.this);
							builder2.setMessage("Deseja mesmo remover ?").setPositiveButton("Sim", dialogClickListener).setNegativeButton("Não", dialogClickListener).show();
						}

					} else if (item == 4)
					{

						Cursor cCli = banco.Sql_Select("CLIENTES", new String[]
						{
								"TELEFONE", "CELULAR"
						}, "_id = " + String.valueOf(ClienteID) , "");// ,
						// "SINCRONIZADO = 0",
						// "");
						if (cCli.moveToFirst())
						{
							EfetuaLigacao(cCli.getString(cCli.getColumnIndex("TELEFONE")), cCli.getString(cCli.getColumnIndex("CELULAR")));
						}

					} else if (item == 5)
					{

						if (Build.VERSION.SDK_INT >= 14)
						{

							aplication.setTipoMapa(GPSConstantes.MAPA_CLIENTE_SELECIONADO);
							Cursor cCli = banco.Sql_Select("CLIENTES", new String[]
							{
									"ENDERECO", "CIDADE", "NUMERO", "BAIRRO", "CEP"
							}, "_id = " + String.valueOf(ClienteID), "");

							Intent intent = new Intent(frm_cons_clientes.this, RotaMapa.class);
							if (cCli.moveToFirst())
							{
								Bundle b = new Bundle();
								b.putString("endereco", cCli.getString(cCli.getColumnIndex("ENDERECO")));
								b.putString("cidade", cCli.getString(cCli.getColumnIndex("CIDADE")));
								b.putString("numero", cCli.getString(cCli.getColumnIndex("NUMERO")));
								b.putString("bairro", cCli.getString(cCli.getColumnIndex("BAIRRO")));
								b.putString("cep", cCli.getString(cCli.getColumnIndex("CEP")));
								intent.putExtras(b);
								startActivity(intent);
							}

						} else
						{
							montarDialogRota();
						}
					}

				}
			});
			AlertDialog alert = builder.create();
			alert.show();

		}
	}

	private void montarDialogRota()
	{
		AlertDialog ad = new AlertDialog.Builder(frm_cons_clientes.this).create();
		// ad.setCancelable(false); // This blocks the 'BACK' button
		ad.setTitle("SmartMobile");
		ad.setIcon(R.drawable.ico_info);
		ad.setMessage("Recurso funcina apenas com Android 4.0 ou superior.");

		ad.setButton("Ok", new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface arg0, int arg1)
			{
				arg0.dismiss();

			}
		});

		ad.show();
	}

	private void op_Filtrar()
	{

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Clientes - Filtrar");
		final CharSequence[] choiceList =

		{
				"Ativos", "Inativos", "Todos os Clientes"
		};

		builder.setSingleChoiceItems(choiceList, banco.cons_cli_indexFiltro,

		new DialogInterface.OnClickListener()
		{

			public void onClick(

			DialogInterface dialog,

			int which)
			{

				banco.cons_cli_indexFiltro = which;
				dialog.cancel();
				CarregaDados(false);

				/*
				 * Toast.makeText(
				 * 
				 * frm_cons_clientes.this,
				 * 
				 * "Select "+choiceList[which],
				 * 
				 * Toast.LENGTH_SHORT
				 * 
				 * )
				 * 
				 * .show();
				 */

			}

		});

		AlertDialog alert = builder.create();

		alert.show();

	}

	private void Cliente_Novo()
	{

		// validação para DNA
		if (banco.Banco.equalsIgnoreCase("DNA"))
		{

			AlertDialog ad = new AlertDialog.Builder(frm_cons_clientes.this).create();
			ad.setCancelable(true); // This blocks the 'BACK' button
			ad.setTitle("SmartMobile - Força de Vendas");
			ad.setIcon(R.drawable.ico_warning);
			ad.setMessage("Cadastro de Cliente será apenas internamente. Favor enviar os dados por e-mail, telefone ou fax.");
			ad.setButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
			ad.show();

		} else
		{

			Intent intent = new Intent(frm_cons_clientes.this, frm_cad_cliente.class);
			Bundle b = new Bundle();
			b.putString("clienteid", "");
			intent.putExtras(b);
			startActivity(intent);

		}

	}

	private void Cliente_Sincroniza(Boolean IsImplantacao)
	{

		AlertDialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(frm_cons_clientes.this);
		builder.setTitle("SmartMobile - Sincronizar Clientes").setSingleChoiceItems(new String[]
		{
				"Cadastros", "Cadastros + Últ. Pedido"
		}, -1, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int item)
			{

				if (item == 0)
				{// apenas cadastros

					DB_Sincroniza dbSync = new DB_Sincroniza(frm_cons_clientes.this);
					dbSync.Syncroniza_Clientes(false, false);

				} else
				{// cadastros + historicos

					AlertDialog.Builder builder = new AlertDialog.Builder(frm_cons_clientes.this);
					builder.setMessage("Este procedimento exige uma conexão rápida e estável de internet, deseja continuar ?").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{

							DB_Sincroniza dbSync = new DB_Sincroniza(frm_cons_clientes.this);
							dbSync.Syncroniza_Clientes(false, true);

						}
					}).setNegativeButton("Não", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							dialog.cancel();
						}
					})
					// Set your icon here
							.setTitle("SmartMobile - Sincronizar Clientes").setIcon(R.drawable.ico_conexao);
					AlertDialog alert = builder.create();
					alert.show();

				}
				dialog.dismiss();
			}
		});

		dialog = builder.create();
		dialog.show();

	}

	private void EfetuaLigacao(final String xTelefone, final String xCelular)
	{

		try
		{

			// Intent callIntent = new Intent(Intent.ACTION_CALL);
			// callIntent.setData(Uri.parse("tel:"+cCli.getString(cCli.getColumnIndex("TELEFONE")).replace("(","").replace(")","").replace("-","")));
			// startActivity(callIntent);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// builder.setMessage("SmartMobile - Ligação ?")
			builder.setCancelable(true).setPositiveButton("Telefone: " + xTelefone, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{

					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent.setData(Uri.parse("tel:" + xTelefone.replace("(", "").replace(")", "").replace("-", "")));
					startActivity(callIntent);

				}
			}).setNegativeButton("Celular: \n" + xCelular, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{

					// dialog.cancel();
					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent.setData(Uri.parse("tel:" + xCelular.replace("(", "").replace(")", "").replace("-", "")));
					startActivity(callIntent);

				}
			})
			// Set your icon here
					.setTitle("SmartMobile - Efetuar Ligação").setIcon(R.drawable.ico_fone);
			AlertDialog alert = builder.create();
			alert.show();

		} catch (ActivityNotFoundException e)
		{
			Log.e("helloandroid dialing example", "Call failed", e);
		}

	}

}