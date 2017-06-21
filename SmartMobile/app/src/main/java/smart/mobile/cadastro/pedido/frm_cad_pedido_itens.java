package smart.mobile.cadastro.pedido;

import smart.mobile.consulta.produtos.frm_full_image;
import smart.mobile.outras.sincronismo.DB_LocalHost;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class frm_cad_pedido_itens extends Activity {

	private DB_LocalHost banco;
	private EditText filterText = null;

	SimpleCursorAdapter adapter;
	boolean Sincronizado = false;

	TextView lblFiltro = null;
	private final int MN_VOLTAR = 0;
	private final int MN_SYNC = 1;

	long PedidoID = 0;
	String UF = "";
	long ListaID = 0;
	String ListaID_Descricao = "";
	long ItemID = 0;
	String ClienteSel = "";
	Intent intentProd;
	MenuItem itemOpt;

	Cursor c;

	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		setContentView(R.layout.layoutpadrao_consgeral);

		ConfiguracaoInicialTela.removerFundoTelaPadraoConsulta(this);

		// RelativeLayout footer = (RelativeLayout)
		// findViewById(R.id.FooterEsconder);
		// footer.setVisibility(View.GONE);

		// carrega dados do banco
		this.banco = new DB_LocalHost(this);

		ErroGeralController erro = new ErroGeralController(this, banco);

		// PARAMETRO DO PEDIDO
		Bundle b = getIntent().getExtras();
		PedidoID = b.getLong("pedidoid");

		// CARREGA O STATUS DO PEDIDO + UF DO CLIENTE
		Cursor rs1 = banco.db.rawQuery("select vendas.sincronizado,clientes.cidade,vendas.listaid,LISTAS_PRECOS.descricao from vendas " +
										" join clientes on vendas.CPF_CNPJ = clientes.CPF_CNPJ " +
										" join LISTAS_PRECOS on vendas.LISTAID = LISTAS_PRECOS.LISTAID " +
										" where vendas._id = " + String.valueOf(PedidoID), null);
		if (rs1.moveToFirst()) {

			UF = (rs1.getString(1).substring(rs1.getString(1).indexOf("-") + 1).trim());
			Log.i("UF do Cliente", UF);

			ListaID = rs1.getInt(2);
			ListaID_Descricao =  rs1.getString(3);
			Log.i("ref.5898--x--ListaID ", String.valueOf(ListaID) + "-" + ListaID_Descricao);

			if (rs1.getLong(0) == 1) {
				Sincronizado = true;
			}
		}

		ImageButton btnCad = (ImageButton) findViewById(R.id.btnCadastro);
		if (Sincronizado) {
			btnCad.setEnabled(false);
		} else {
			btnCad.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Produto_Adiciona();
				}
			});
		}

		// oculta o botao de sincronizar
		ImageButton btnSync = (ImageButton) findViewById(R.id.btnSincroniza);
		btnSync.setVisibility(View.GONE);

		// configura componente de filtro
		filterText = (EditText) findViewById(R.id.edtFiltro);
		filterText.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			public void afterTextChanged(Editable s) {
				CarregaDados(s.toString());

			}
		});

		lblFiltro = (TextView) findViewById(R.id.lblStatus);

		// carrega consulta
		CarregaDados("");

		// deixa a tela de consulta predefinida com parametros padroes
		intentProd = new Intent(frm_cad_pedido_itens.this, frm_cons_produtos.class);

		Bundle c = new Bundle();
		c.putLong("pedidoid", PedidoID);
		c.putString("uf", UF);
		// 22/09/2016 - ref. chamado 5998 - adicionado parametro da 'Lista de Preço' para exibir já o preço final na Consulta de Produtos
		c.putLong("listaid", ListaID);
		c.putString("listaid_descricao", ListaID_Descricao);
		intentProd.putExtras(c);

	}

	@Override
	public void onResume() {
		super.onResume();

		// Log.v("Szakbarbar", "Need the cursor from the Service");
		CarregaDados("");
	}

	@Override
	public void finish() {
		super.finish();
		return;

	}

	private String convText(TextView v, String text) {

		// oculta a imagem de check
		if (v.getId() == R.id.imgCheck) {
			((ImageView) v.findViewById(R.id.imgCheck)).setVisibility(View.GONE);
		} else if (v.getId() == R.id.txtQtde) {
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

		} else if (v.getId() == R.id.txtValor) {
			return banco.myCustDecFormatter.format(c.getDouble(c.getColumnIndex("VALOR")));
		} else if (v.getId() == R.id.txtAcrescimo) {
			return banco.myCustDecFormatter.format(c.getDouble(c.getColumnIndex("ACRESCIMO")));
		} else if (v.getId() == R.id.txtDesconto) {
			return banco.myCustDecFormatter.format(c.getDouble(c.getColumnIndex("DESCONTO")));
		} else if (v.getId() == R.id.txtValorFinal) {
			return banco.myCustDecFormatter.format(((c.getDouble(c.getColumnIndex("QTDE")) * c.getDouble(c.getColumnIndex("VALOR"))) + c.getDouble(c.getColumnIndex("ACRESCIMO")) - c.getDouble(c.getColumnIndex("DESCONTO"))) / c.getDouble(c.getColumnIndex("QTDE")));
		} else if (v.getId() == R.id.txtTotal) {
			return banco.myCustDecFormatter.format((c.getDouble(c.getColumnIndex("QTDE")) * c.getDouble(c.getColumnIndex("VALOR"))) + c.getDouble(c.getColumnIndex("ACRESCIMO")) - c.getDouble(c.getColumnIndex("DESCONTO")));
		}

		return text;
	}

	public void CarregaDados(String filtro) {
		// Use your own layout

		// definindo o filtro
		if (!filtro.equals("")) {

			try {
				// pega tambem pelo codigo inteiro
				Integer.parseInt(filtro);
				filtro = " and (produtos.produtoid = " + filtro + ") or (produtos.descricao LIKE " + "'%" + filtro + "%')";
			} catch (NumberFormatException nfe) {
				filtro = " and (produtos.descricao LIKE " + "'%" + filtro + "%')";
			}
		}

		c = banco.db.rawQuery("select vendas_itens._id,vendas_itens.produtoid,produtos.descricao,vendas_itens.qtde,vendas_itens.valor,vendas_itens.acrescimo,vendas_itens.desconto,0 as vendaid, -1 as QTDE_PED,produtos.linha,produtos.coluna,produtos.und,vendas_itens.valor_st from vendas_itens join produtos on vendas_itens.produtoid = produtos.produtoid and vendas_itens.linhaid = produtos.linhaid and vendas_itens.colunaid = produtos.colunaid and vendas_itens.unidadeid = produtos.unidadeid where vendas_itens.vendaid = " + String.valueOf(PedidoID) + filtro + " order by vendas_itens._id desc", null);

		// {c = banco.db.query("VENDAS_ITENS", new String[]
		// {"_id","PRODUTOID","QTDE","VALOR"}, null, null, null,
		// null,"_id DESC");}

		/*
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
		adapter = new frm_cad_pedido_itens_historico_adapter(this, R.layout.lay_cad_pedido_itens_final, c, banco, new String[] { "PRODUTOID", "DESCRICAO", "QTDE", "VALOR", "ACRESCIMO", "DESCONTO", "VALOR", "VALOR" }, new int[] { R.id.txtCodigo, R.id.txtDescricao, R.id.txtQtde, R.id.txtValor, R.id.txtAcrescimo, R.id.txtDesconto, R.id.txtValorFinal, R.id.txtTotal });

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
		ListView listaCons = (ListView) findViewById(R.id.vendaListView);
		listaCons.setAdapter(lsadapter);
		listaCons.setFastScrollEnabled(true);

		// CALCULA O TOTAL DO PEDIDO
		lblFiltro.setText("Registros: " + lsadapter.getCount());
		// lblFiltro.setText("");
		Cursor cTot = banco.db.rawQuery("select sum((vendas_itens.QTDE * vendas_itens.VALOR)+vendas_itens.acrescimo-vendas_itens.desconto), sum(acrescimo), sum(desconto), sum(valor_st), sum(peso_total) from vendas_itens where vendas_itens.vendaid = " + String.valueOf(PedidoID), null);
		if (cTot.moveToFirst()) {
			try {
				lblFiltro.setText(lblFiltro.getText().toString() + " Acres: " + banco.myCustDecFormatter.format(cTot.getDouble(1)) + " Desc: " + banco.myCustDecFormatter.format(cTot.getDouble(2)) + "\n" + " Total R$: " + banco.myCustDecFormatter.format(cTot.getDouble(0)) + " ST+IPI: " + banco.myCustDecFormatter.format(cTot.getDouble(3) - cTot.getDouble(0)) + " Total+ST+IPI: " + banco.myCustDecFormatter.format(cTot.getDouble(3)) + " Peso Total: " + banco.myCustDecFormatter.format(cTot.getDouble(4)));

			} catch (Exception ex) {
			}
		}

		listaCons.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// @todo
				// Toast.makeText(frm_cad_pedido_itens.this,"O id do item selecionado é: "+
				// String.valueOf(id), Toast.LENGTH_LONG).show();

				ItemID = id;
				if (!Sincronizado) {
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

	private void Produto_Adiciona() {

		startActivity(intentProd);
	}

	private void opcoesCli() {

		if (!Sincronizado) {

			final CharSequence[] items = { "Incluir", "Alterar", "Remover", "Ver Imagens" };

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Opções");
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					// Toast.makeText(getApplicationContext(), items[item],
					// Toast.LENGTH_SHORT).show();

					if (item == 0) {

						Produto_Adiciona();

					}
					if (item == 1) {

						Cursor cli0 = banco.db.rawQuery("select produtos.produtoid,produtos.descricao,produtos.und,vendas_itens.qtde,vendas_itens.valor,vendas_itens.acrescimo,vendas_itens.desconto,produtos.valor valor2, produtos.desc_max, produtos.linhaid, produtos.colunaid, produtos.linha, produtos.coluna, vendas_itens.unidadeid, vendas_itens.valor_st, produtos.peso from produtos join vendas_itens on vendas_itens.produtoid = produtos.produtoid and vendas_itens.linhaid = produtos.linhaid and vendas_itens.colunaid = produtos.colunaid and vendas_itens.unidadeid = produtos.unidadeid where vendas_itens._id = " + String.valueOf(ItemID), null);
						if (cli0.moveToFirst()) {
							// do {
							Bundle b = new Bundle();
							b.putBoolean("incluindo", false);
							b.putLong("pedidoid", PedidoID);
							b.putLong("itemid", ItemID);
							b.putLong("produtoid", cli0.getLong(0));
							b.putString("descricao", cli0.getString(1));
							b.putString("und", cli0.getString(2));
							b.putDouble("qtde", cli0.getDouble(3));
							b.putDouble("valor", ((c.getDouble(c.getColumnIndex("QTDE")) * c.getDouble(c.getColumnIndex("VALOR"))) + c.getDouble(c.getColumnIndex("ACRESCIMO")) - c.getDouble(c.getColumnIndex("DESCONTO"))) / c.getDouble(c.getColumnIndex("QTDE")));
							b.putDouble("valor_cad", cli0.getDouble(7));
							b.putDouble("desc_max", cli0.getDouble(8));
							b.putLong("linhaid", cli0.getLong(9));
							b.putLong("colunaid", cli0.getLong(10));
							b.putString("linha", cli0.getString(11));
							b.putString("coluna", cli0.getString(12));
							b.putLong("unidadeid", cli0.getLong(13));
							b.putDouble("percentual_st", ((cli0.getDouble(cli0.getColumnIndex("VALOR_ST")) * 100) / ((cli0.getDouble(cli0.getColumnIndex("QTDE")) * cli0.getDouble(cli0.getColumnIndex("VALOR"))) - cli0.getDouble(cli0.getColumnIndex("DESCONTO")) + cli0.getDouble(cli0.getColumnIndex("ACRESCIMO")))) - 100);
							b.putDouble("peso", cli0.getDouble(cli0.getColumnIndex("PESO")));

							// Toast.makeText(frm_cad_pedido_itens.this,"Valor: "
							// + cli0.getString(3) + " incluso no pedido !!!",
							// Toast.LENGTH_SHORT).show();

							Intent intent = new Intent(frm_cad_pedido_itens.this, frm_cad_pedido_item.class);
							intent.putExtras(b);
							startActivity(intent);
							// } while (cli0.moveToNext());
						}

					} else if (item == 2) {

						DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
								case DialogInterface.BUTTON_POSITIVE:

									Cursor cItem = banco.db.rawQuery("select produtoid,flex_acrescimo,flex_desconto,linhaid,colunaid from vendas_itens where _id = " + String.valueOf(ItemID), null);
									if (cItem.moveToFirst()) {
											banco.TempPed_DelPRodutosID(cItem.getString(0), cItem.getString(3), cItem.getString(4));
											banco.TB_VENDAS_ITENS_DELETAR(PedidoID, ItemID, cItem.getLong(0), cItem.getDouble(1), cItem.getDouble(2));
											CarregaDados("");
									}

								case DialogInterface.BUTTON_NEGATIVE:
									// No button clicked
									break;
								}
							}
						};

						AlertDialog.Builder builder2 = new AlertDialog.Builder(frm_cad_pedido_itens.this);
						builder2.setMessage("Deseja mesmo remover ?").setPositiveButton("Sim", dialogClickListener).setNegativeButton("Não", dialogClickListener).show();

					} else if (item == 3) {

						Intent intent = new Intent(frm_cad_pedido_itens.this, frm_full_image.class);
						Bundle params = new Bundle();

						Cursor cItem = banco.db.rawQuery("select produtoid from vendas_itens where _id = " + String.valueOf(ItemID), null);
						if (cItem.moveToFirst()) {
							params.putString("idProduto", cItem.getLong(0) + "");
						}
						intent.putExtras(params);
						startActivity(intent);

					}

				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}

	}

	@Override
	protected void onDestroy() {
		this.banco.closeHelper();
		super.onDestroy();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemAdd = menu.add(0, MN_VOLTAR, Menu.NONE, "Voltar");
		itemOpt = menu.add(0, MN_SYNC, Menu.NONE, "Opções");
		if (Sincronizado) {
			itemOpt.setEnabled(false);
		}

		// Assign icons
		itemAdd.setIcon(R.drawable.ico_voltar);
		itemOpt.setIcon(R.drawable.ico_opcoes);

		// Allocate shortcuts to each of them.
		itemAdd.setShortcut('0', 'v');
		itemOpt.setShortcut('1', 's');

		// submenus de opções
		// itemOpt.add(MN_OPCOES, MN_PEDIDO, 2, "Pedido");
		// itemOpt.add(MN_OPCOES, MN_TITULOS, 3, "Títulos");
		// itemOpt.add(MN_OPCOES, MN_CADASTRAR, 4, "Incluir Cliente");
		// itemOpt.add(MN_OPCOES, MN_ALTERAR, 5, "Alterar Cliente");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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