package smart.mobile.cadastro.pedido;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.download.DB_Sincroniza;
import smart.mobile.R;
import smart.mobile.consulta.produtos.frm_cons_produtos;
import smart.mobile.utils.ConfiguracaoInicialTela;
import smart.mobile.utils.error.ErroGeralController;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class frm_cad_pedido_itens_historico extends Activity
{

	private DB_LocalHost banco;
	private EditText filterText = null;

	SimpleCursorAdapter adapter;
	boolean Sincronizado = false;

	TextView lblFiltro = null;
	private final int MN_VOLTAR = 0;
	private final int MN_SYNC = 1;

	ListView listaCons;
	int ultPosicao = 0;

	long TipoID = 0;
	long PedidoID = 0;
	String ClienteID = "";
	long ItemID = 0;
	String ClienteSel = "";
	Intent intentProd;
	MenuItem itemOpt;

	Cursor c;

	public void onCreate(Bundle icicle)
	{

		super.onCreate(icicle);
		setContentView(R.layout.layoutpadrao_consgeral);
		
		ConfiguracaoInicialTela.removerFundoTelaPadraoConsulta(this);
		
//		RelativeLayout footer = (RelativeLayout) findViewById(R.id.FooterEsconder);
//		footer.setVisibility(View.GONE);

		// carrega dados do banco
		this.banco = new DB_LocalHost(this);
		
		ErroGeralController erro = new ErroGeralController(this, banco);

		// PARAMETRO DO PEDIDO
		Bundle b = getIntent().getExtras();
		TipoID = b.getLong("tipoid");
		PedidoID = b.getLong("pedidoid");

		// CARREGA O STATUS DO PEDIDO
		if (PedidoID > 0)
		{
			Cursor rs1 = banco.db.rawQuery("select sincronizado, CPF_CNPJ from vendas where vendas._id = " + String.valueOf(PedidoID), null);
			if (rs1.moveToFirst())
			{
				if (rs1.getLong(0) == 1)
				{
					Sincronizado = true;
				}
				ClienteID = rs1.getString(1);
			}
		} else
		{
			ClienteID = b.getString("clienteid");
		}
		;

		ImageButton btnCad = (ImageButton) findViewById(R.id.btnCadastro);
		btnCad.setVisibility(View.GONE);
		// btnCad.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		// Produto_Adiciona();
		// }
		// });}

		ImageButton btnSync = (ImageButton) findViewById(R.id.btnSincroniza);
		if (TipoID == 0)
		{
			btnSync.setVisibility(View.GONE);
		}
		btnSync.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				// TODO Auto-generated method stub

				opcoesSync();

			}
		});

		// btnSync.setVisibility(View.GONE);

		// configura componente de filtro
		filterText = (EditText) findViewById(R.id.edtFiltro);
		filterText.addTextChangedListener(new TextWatcher()
		{

			public void onTextChanged(CharSequence s, int start, int before, int count)
			{

			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{

			}

			public void afterTextChanged(Editable s)
			{
				CarregaDados();

			}
		});

		lblFiltro = (TextView) findViewById(R.id.lblStatus);

		// carrega consulta
		CarregaDados();

		//
		intentProd = new Intent(frm_cad_pedido_itens_historico.this, frm_cons_produtos.class);
		Bundle c = new Bundle();
		c.putLong("pedidoid", PedidoID);
		intentProd.putExtras(c);

	}

	private void opcoesSync()
	{

		DB_Sincroniza dbSync = new DB_Sincroniza(frm_cad_pedido_itens_historico.this);
		dbSync.Syncroniza_Historico(ClienteID);
		/*
		 * final CharSequence[] items =
		 * {"Últimos 50 Produtos","Últimos 100 Produtos"
		 * ,"Últimos 200 Produtos"};
		 * 
		 * AlertDialog.Builder builder = new AlertDialog.Builder(this);
		 * builder.setTitle("Opções"); builder.setItems(items, new
		 * DialogInterface.OnClickListener() { public void
		 * onClick(DialogInterface dialog, int item) {
		 * 
		 * 
		 * //Toast.makeText(getApplicationContext(), items[item],
		 * Toast.LENGTH_SHORT).show();
		 * 
		 * if (item == 0){
		 * 
		 * DB_Sincroniza dbSync = new
		 * DB_Sincroniza(frm_cad_pedido_historico.this);
		 * dbSync.Syncroniza_Historico(ClienteID, 50);
		 * 
		 * } if (item == 1){
		 * 
		 * DB_Sincroniza dbSync = new
		 * DB_Sincroniza(frm_cad_pedido_historico.this);
		 * dbSync.Syncroniza_Historico(ClienteID, 100);
		 * 
		 * } else if (item ==2){
		 * 
		 * DB_Sincroniza dbSync = new
		 * DB_Sincroniza(frm_cad_pedido_historico.this);
		 * dbSync.Syncroniza_Historico(ClienteID, 200);
		 * 
		 * }
		 * 
		 * } }); AlertDialog alert = builder.create(); alert.show();
		 */
	}

	@Override
	public void onResume()
	{
		super.onResume();

		// Log.v("Szakbarbar", "Need the cursor from the Service");
		// CarregaDados();
		listaCons.setSelection(ultPosicao);
		listaCons.invalidateViews();
	}

	@Override
	public void finish()
	{
		super.finish();
		return;

	}

	private String convText(TextView v, String text)
	{

		if (v.getId() == R.id.txtDescricao)
		{
			if (c.getDouble(c.getColumnIndex("QTDE_PED")) > 0)
			{
				v.setTextColor(getResources().getColor(R.color.cor_vermelho));
			}

			if (c.getDouble(c.getColumnIndex("VENDAID")) > 0)
			{
				return c.getString(c.getColumnIndex("DESCRICAO")) + " [Pedido nº " + c.getString(c.getColumnIndex("VENDAID")) + "]";
			} else
			{
				return c.getString(c.getColumnIndex("DESCRICAO"));
			}
		}

		else if (v.getId() == R.id.txtQtde)
		{
			/*
			 * EditText txtQtde = (EditText) v.findViewById(R.id.txtQtde);
			 * txtQtde.addTextChangedListener(new TextWatcher(){
			 * 
			 * public void afterTextChanged(Editable s) {
			 * Log.i("afterTextChangedQTDE",s.toString());
			 * if(!s.toString().equals("")) {
			 * Log.i("ITEMID",String.valueOf(ItemID)); //
			 * banco.TB_VENDAS_ITENS_INSERIR(PedidoID, ItemID, 0,
			 * Double.valueOf(s.toString()), 0.00); } } public void
			 * beforeTextChanged(CharSequence s, int start, int count, int
			 * after) {Log.i("beforeTextChangedQTDE",s.toString());} public void
			 * onTextChanged(CharSequence s, int start, int before, int count)
			 * {Log.i("onTextChangedQTDE",s.toString());} });
			 */

			// Log.i("QTDE", txtQtde.getText().toString());

			return banco.myCustDecFormatter.format(c.getDouble(c.getColumnIndex("QTDE")));
			// return
			// myCustDecFormatter.format(Double.valueOf(txtQtde.getText().toString()));

		} else if (v.getId() == R.id.txtValor)
		{

			return banco.myCustDecFormatter.format(c.getDouble(c.getColumnIndex("VALOR")));
		} else if (v.getId() == R.id.txtAcrescimo)
		{

			return banco.myCustDecFormatter.format(c.getDouble(c.getColumnIndex("ACRESCIMO")));
		} else if (v.getId() == R.id.txtDesconto)
		{

			return banco.myCustDecFormatter.format(c.getDouble(c.getColumnIndex("DESCONTO")));
		} else if (v.getId() == R.id.txtValorFinal)
		{
			return banco.myCustDecFormatter.format(((c.getDouble(c.getColumnIndex("QTDE")) * c.getDouble(c.getColumnIndex("VALOR"))) + c.getDouble(c.getColumnIndex("ACRESCIMO")) - c.getDouble(c.getColumnIndex("DESCONTO"))) / c.getDouble(c.getColumnIndex("QTDE")));
		} else if (v.getId() == R.id.txtTotal)
		{
			// v.setTextColor(getResources().getColor(R.color.solid_red));
			return banco.myCustDecFormatter.format((c.getDouble(c.getColumnIndex("QTDE")) * c.getDouble(c.getColumnIndex("VALOR"))) + c.getDouble(c.getColumnIndex("ACRESCIMO")) - c.getDouble(c.getColumnIndex("DESCONTO")));
		}

		return text;
	}

	public void CarregaDados()
	{

		String filtro = filterText.getText().toString();
		// definindo o filtro
		if (!filtro.equals(""))
		{

			try
			{
				// pega tambem pelo codigo inteiro
				Integer.parseInt(filtro);
				filtro = " and (produtos.produtoid = " + filtro + ") or (produtos.descricao LIKE " + "'%" + filtro + "%')";
			} catch (NumberFormatException nfe)
			{
				filtro = " and (produtos.descricao LIKE " + "'%" + filtro + "%')";
			}

		}

		// Use your own layout

		if (TipoID == 0)
		{ // mix de produtos

			if (PedidoID > 0)
			{
				/*
				 * c = banco.db.rawQuery(
				 * "select vendas_itens._id,  vendas_itens.produtoid, produtos.descricao, vendas_itens.qtde, vendas_itens.valor, vendas_itens.acrescimo, vendas_itens.desconto,vendas_itens.vendaid,V2.qtde as QTDE_PED from vendas_itens "
				 * +
				 * "join produtos on vendas_itens.produtoid = produtos.produtoid "
				 * + "join vendas on vendas_itens.vendaid = vendas._id " +
				 * "LEFT join VENDAS_ITENS V2 ON V2.VENDAID = " +
				 * String.valueOf(PedidoID) +
				 * " AND V2.PRODUTOID = PRODUTOS.PRODUTOID " +
				 * "where vendas._id <> " + String.valueOf(PedidoID) +
				 * " and vendas.clienteid = " + String.valueOf(ClienteID) +
				 * filtro + " order by vendas._id desc",null);
				 */
				c = banco.db.rawQuery("select vendas_itens._id,  vendas_itens.produtoid, produtos.descricao, vendas_itens.qtde, vendas_itens.VALOR_ST, vendas_itens.valor, vendas_itens.acrescimo, vendas_itens.desconto,vendas_itens.vendaid as vendaid,0 as QTDE_PED,produtos.linhaid,produtos.colunaid,produtos.linha,produtos.coluna,produtos.und from vendas_itens join produtos on vendas_itens.produtoid = produtos.produtoid and vendas_itens.linhaid = produtos.linhaid and vendas_itens.colunaid = produtos.colunaid and vendas_itens.unidadeid = produtos.unidadeid join vendas on vendas_itens.vendaid = vendas._id where vendas._id <> " + String.valueOf(PedidoID) + " and vendas.CPF_CNPJ like '" + String.valueOf(ClienteID)+"'" + filtro + " order by vendas._id desc", null);
			} else
			{
				c = banco.db.rawQuery("select vendas_itens._id,  vendas_itens.produtoid, produtos.descricao, vendas_itens.qtde, vendas_itens.VALOR_ST, vendas_itens.valor, vendas_itens.acrescimo, vendas_itens.desconto,vendas_itens.vendaid as vendaid,-1 as QTDE_PED,produtos.linhaid,produtos.colunaid,produtos.linha,produtos.coluna,produtos.und from vendas_itens join produtos on vendas_itens.produtoid = produtos.produtoid and vendas_itens.linhaid = produtos.linhaid and vendas_itens.colunaid = produtos.colunaid and vendas_itens.unidadeid = produtos.unidadeid join vendas on vendas_itens.vendaid = vendas._id where vendas._id <> " + String.valueOf(PedidoID) + " and vendas.CPF_CNPJ like '" + String.valueOf(ClienteID)+ "" + filtro + " order by vendas._id desc", null);
			}

		} else
		{ // ultimo pedido

			if (PedidoID > 0)
			{
				/*
				 * c = banco.db.rawQuery(
				 * "select clientes_historico._id,  clientes_historico.produtoid, produtos.descricao, clientes_historico.qtde, clientes_historico.valor, clientes_historico.acrescimo, clientes_historico.desconto,0 as VENDAID, V2.qtde as QTDE_PED  from clientes_historico "
				 * +
				 * "join produtos on clientes_historico.produtoid = produtos.produtoid "
				 * + "LEFT join VENDAS_ITENS V2 ON V2.VENDAID = " +
				 * String.valueOf(PedidoID) +
				 * " AND V2.PRODUTOID = PRODUTOS.PRODUTOID " +
				 * "where clientes_historico.clienteid = " +
				 * String.valueOf(ClienteID) + filtro,null);
				 */
				c = banco.db.rawQuery("select clientes_historico._id,  clientes_historico.produtoid, produtos.descricao, clientes_historico.qtde, clientes_historico.valor, clientes_historico.acrescimo, clientes_historico.desconto,0 as vendaid, 0 as QTDE_PED,produtos.linhaid,produtos.colunaid,produtos.linha,produtos.coluna,produtos.und  from clientes_historico join produtos on clientes_historico.produtoid = produtos.produtoid and clientes_historico.linhaid = produtos.linhaid and clientes_historico.colunaid = produtos.colunaid and clientes_historico.unidadeid = produtos.unidadeid where clientes_historico.CPF_CNPJ like '" + String.valueOf(ClienteID)+"'" + filtro, null);
			} else
			{
				c = banco.db.rawQuery("select clientes_historico._id,  clientes_historico.produtoid, produtos.descricao, clientes_historico.qtde, clientes_historico.valor, clientes_historico.acrescimo, clientes_historico.desconto,0 as vendaid, -1 as QTDE_PED,produtos.linhaid,produtos.colunaid,produtos.linha,produtos.coluna,produtos.und  from clientes_historico join produtos on clientes_historico.produtoid = produtos.produtoid and clientes_historico.linhaid = produtos.linhaid and clientes_historico.colunaid = produtos.colunaid and clientes_historico.unidadeid = produtos.unidadeid where clientes_historico.CPF_CNPJ like '" + String.valueOf(ClienteID)+"'" + filtro, null);
			}

		}

		// {c = banco.db.query("VENDAS_ITENS", new String[]
		// {"_id","PRODUTOID","QTDE","VALOR"}, null, null, null,
		// null,"_id DESC");}
		/*
		 * 
		 * startManagingCursor(c); adapter = new SimpleCursorAdapter(this,
		 * R.layout.lay_cad_pedido_itens_final, c, new String[] { "PRODUTOID",
		 * "DESCRICAO","QTDE","VALOR","ACRESCIMO", "DESCONTO", "VALOR",
		 * "VALOR"}, new int[] { R.id.txtCodigo, R.id.txtDescricao,
		 * R.id.txtQtde, R.id.txtValor, R.id.txtAcrescimo, R.id.txtDesconto,
		 * R.id.txtValorFinal, R.id.txtTotal}) {
		 * 
		 * @Override public void setViewText(TextView v, String text) {
		 * super.setViewText(v, convText(v, text)); }
		 * 
		 * };
		 */

		// INICIANDO CURSOR
		startManagingCursor(c);
		adapter = new frm_cad_pedido_itens_historico_adapter(this, R.layout.lay_cad_pedido_itens_final, c, banco, new String[]
		{
				"PRODUTOID", "DESCRICAO", "QTDE", "VALOR", "ACRESCIMO", "DESCONTO", "VALOR", "VALOR"
		}, new int[]
		{
				R.id.txtCodigo, R.id.txtDescricao, R.id.txtQtde, R.id.txtValor, R.id.txtAcrescimo, R.id.txtDesconto, R.id.txtValorFinal, R.id.txtTotal
		});

		// aqui configura o metodo que atualiza qtde e valor dos itens editados
		// aqui configura o metodo que atualiza qtde e valor dos itens editados
		// aqui configura o metodo que atualiza qtde e valor dos itens editados

		// txtQtde = (EditText) findViewById(R.id.txtQtde);

		// txtQtde.addTextChangedListener(new TextWatcher(){
		// public void afterTextChanged(Editable s) {
		// i++;
		// tv.setText(String.valueOf(i) + " / " + String.valueOf(charCounts));
		// Toast.makeText(frm_cad_pedido.this,"Editando qtde: ",
		// Toast.LENGTH_LONG).show();
		// Log.i("editando","ahaahah");
		// txtQtde.setText("00");

		// }
		// public void beforeTextChanged(CharSequence s, int start, int count,
		// int after){}
		// public void onTextChanged(CharSequence s, int start, int before, int
		// count){}
		// });

		ListAdapter lsadapter = adapter;
		listaCons = (ListView) findViewById(R.id.vendaListView);
		listaCons.setAdapter(lsadapter);
		listaCons.setFastScrollEnabled(true);

		// CALCULA O TOTAL DO PEDIDO
		lblFiltro.setText("Registros: " + lsadapter.getCount());
		// lblFiltro.setText("");

		if (TipoID == 1)
		{
			Cursor cTot = banco.db.rawQuery("select sum((QTDE * VALOR)+acrescimo-desconto), sum(acrescimo), sum(desconto) from clientes_historico where clientes_historico.CPF_CNPJ LIKE '" + String.valueOf(ClienteID)+"'", null);
			if (cTot.moveToFirst())
			{
				try
				{
					lblFiltro.setText(lblFiltro.getText().toString() + " Acres: " + banco.myCustDecFormatter.format(cTot.getDouble(1)) + " Desc: " + banco.myCustDecFormatter.format(cTot.getDouble(2)) + " Total: " + banco.myCustDecFormatter.format(cTot.getDouble(0)));

				} catch (Exception ex)
				{
				}
			}

		}
		/*
		 * Double cTot = 0.00; if( c.moveToFirst()) { do { cTot = cTot +
		 * (c.getDouble(5) * c.getDouble(6)); try{
		 * 
		 * } catch(Exception ex) {}
		 * 
		 * } while (c.moveToNext()); }
		 * 
		 * lblFiltro.setText(lblFiltro.getText().toString() + " Total: " +
		 * myCustDecFormatter.format(cTot));
		 */

		listaCons.setOnItemClickListener(new OnItemClickListener()
		{

			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				// @todo
				// Toast.makeText(frm_cad_pedido_itens.this,"O id do item selecionado é: "+
				// String.valueOf(id), Toast.LENGTH_LONG).show();

				ultPosicao = position;
				ItemID = id;
				if (!Sincronizado)
				{
					opcoesCli();
				}

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

	private void Produto_Adiciona()
	{

		startActivity(intentProd);
	}

	private void opcoesCli()
	{

		if (!Sincronizado)
		{

			final CharSequence[] items =
			{
				"Pedido(+)"
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Opções");
			builder.setItems(items, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item)
				{

					// Toast.makeText(getApplicationContext(), items[item],
					// Toast.LENGTH_SHORT).show();

					if (item == 0)
					{
						if (PedidoID > 0)
						{

							// busca o PRODUTOID e adiciona no PEDIDO
							Cursor cli0 = null;
							if (TipoID == 0)
							{
								cli0 = banco.db.rawQuery("select produtos.produtoid,produtos.descricao,vendas_itens.qtde,vendas_itens.valor,produtos.valor valor2, produtos.desc_max, produtos.und,produtos.linhaid,produtos.colunaid,produtos.linha,produtos.coluna,produtos.unidadeid,produtos.estoque from produtos join vendas_itens on vendas_itens.produtoid = produtos.produtoid and vendas_itens.linhaid = produtos.linhaid and vendas_itens.colunaid = produtos.colunaid where vendas_itens._id = " + String.valueOf(ItemID), null);
							} else
							{
								cli0 = banco.db.rawQuery("select produtos.produtoid,produtos.descricao,clientes_historico.qtde,clientes_historico.valor,produtos.valor valor2, produtos.desc_max, produtos.und,produtos.linhaid,produtos.colunaid,produtos.linha,produtos.coluna,produtos.unidadeid,produtos.estoque from produtos join clientes_historico on clientes_historico.produtoid = produtos.produtoid and clientes_historico.linhaid = produtos.linhaid and clientes_historico.colunaid = produtos.colunaid where clientes_historico._id = " + String.valueOf(ItemID), null);
							}

							if (cli0.moveToFirst())
							{
								// do {

								// verifica se o produto já está incluso no
								// pedido
								Cursor pedProd = banco.Sql_Select("VENDAS_ITENS", new String[]
								{
									"PRODUTOID"
								}, "VENDAID = " + String.valueOf(PedidoID) + " AND PRODUTOID = " + cli0.getLong(0) + " AND LINHAID = " + cli0.getLong(7) + " AND COLUNAID = " + cli0.getLong(8), null);
								if (pedProd.moveToFirst())
								{
									banco.MostraMsg(frm_cad_pedido_itens_historico.this, "Produto já existe no pedido !!!");
								} else
								{

									// ultima quantidade
									banco.QtdeProduto = cli0.getString(2);

									Bundle b = new Bundle();
									b.putBoolean("incluindo", true);
									b.putLong("pedidoid", PedidoID);
									b.putLong("itemid", 0);

									b.putLong("produtoid", cli0.getLong(0));
									b.putLong("linhaid", cli0.getLong(7));
									b.putLong("colunaid", cli0.getLong(8));
									b.putString("linha", cli0.getString(9));
									b.putString("coluna", cli0.getString(10));
									b.putLong("unidadeid", cli0.getLong(11));
									b.putDouble("estoque", cli0.getLong(12));

									b.putString("descricao", cli0.getString(1));
									b.putDouble("qtde", cli0.getDouble(2));
									b.putDouble("valor", cli0.getDouble(3));
									b.putDouble("valor_cad", cli0.getDouble(4));
									b.putDouble("desc_max", cli0.getDouble(5));
									b.putString("und", cli0.getString(6));

									Intent intent = new Intent(frm_cad_pedido_itens_historico.this, frm_cad_pedido_item.class);
									intent.putExtras(b);
									startActivity(intent);

								}

								// } while (cli0.moveToNext());
							}

						}

					}

				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemAdd = menu.add(0, MN_VOLTAR, Menu.NONE, "Voltar");
		// Assign icons
		itemAdd.setIcon(R.drawable.ico_voltar);

		if (PedidoID > 0)
		{
			itemOpt = menu.add(0, MN_SYNC, Menu.NONE, "Opções");
			if (Sincronizado)
			{
				itemOpt.setEnabled(false);
			}
			// itemOpt.setIcon(R.drawable.ico_opcoes);

			// Allocate shortcuts to each of them.
			itemAdd.setShortcut('0', 'v');
		}
		// itemOpt.setShortcut('1', 's');

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
			return true;

		case MN_SYNC:

			// DB_ServerHost service = new DB_ServerHost(frmConsClientes.this);
			// String retorno =
			// service.Sql_Select("select nome,fantasia,cpf_cnj,insc_est,endereco,numero,bairro,cidade,cep,telefone,celular,email,obs from vw_mobile_clientes where vendedorid = "
			// + banco.VendedorID);

			// Toast.makeText(frmConsClientes.this,retorno
			// ,Toast.LENGTH_LONG).show();

			// banco.Produto_Insere("123", "teste", "4.50");

			// banco.Clientes_DeleteAll();
			//
			// banco.Produto_Insere(456, "VASSOURA COM CABO", "LIMPEZA",10.0,
			// 45.6, 550.0);

			// banco.Produto_Insere(456, "VASSOURA3", "LIMPEZA",10.5, 7895.6,
			// 550.2);

			// CarregaDados("");

			opcoesCli();

			// Toast.makeText(frmConsClientes.this,"Terminou"
			// ,Toast.LENGTH_LONG).show();

		}
		return false;
	}

}