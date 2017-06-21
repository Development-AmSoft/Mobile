package smart.mobile.consulta.pedido;

import java.text.DecimalFormat;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.download.DB_Sincroniza;
import smart.mobile.R;
import smart.mobile.cadastro.pedido.frm_cad_pedido;
import smart.mobile.outras.tela.principal.frm_sys_principal;
import smart.mobile.utils.ConfiguracaoInicialTela;
import smart.mobile.utils.error.ErroGeralController;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
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
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class frm_cons_pedidos extends Activity {

	// variavel principal de acesso ao banco
	private DB_LocalHost banco;

	// definição de colunas da consulta
	int col_order = 0;
	boolean col_desc = true;
	final CharSequence[] col_banco = { "vendas._id", "vendas.data", "clientes.nome", "clientes.cidade", "vendas.total", "vendas.sincronizado" };
	final CharSequence[] col_descricao = { "Nº Venda", "Data", "Nome", "Cidade", "Total R$", "Situação" };

	private EditText filterText = null;
	SimpleCursorAdapter adapter;
	TextView lblFiltro = null;
	EditText txtFiltro = null;

	private final int MN_VOLTAR = 0;
	private final int MN_OPCOES = 1;
	private final int MN_FILTRAR = 2;
	private final int MN_ORDENAR = 3;

	long PedidoID = 0;
	String ClienteSel = "";

	Cursor c;

	// BLOCO 1 - EVENTOS DA ACTIVITY

	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		setContentView(R.layout.activity_ctela_base);
		setTitle("SmartMobile - Consulta de Pedidos");

		ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.layoutpadrao_consgeral, false);

		// carrega dados do banco
		this.banco = new DB_LocalHost(this);

		ErroGeralController erro = new ErroGeralController(this, banco);

		RelativeLayout footer = (RelativeLayout) findViewById(R.id.FooterEsconder);
		footer.setVisibility(View.GONE);

		ImageButton btnTipoLista = (ImageButton) findViewById(R.id.btnTipoLista);
		btnTipoLista.setVisibility(View.VISIBLE);
		btnTipoLista.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				AlertDialog.Builder builder = new AlertDialog.Builder(frm_cons_pedidos.this);
				builder.setTitle("Tipo de Lista");
				final CharSequence[] choiceList =

				{ "Lista Simples", "Lista Completa" };

				builder.setSingleChoiceItems(choiceList, banco.cons_ped_indexTIPO,

				new DialogInterface.OnClickListener() {

					public void onClick(

					DialogInterface dialog,

					int which) {

						banco.cons_ped_indexTIPO = which;
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

		// botoes de cadastro e sincronizacao
		ImageButton btnCad = (ImageButton) findViewById(R.id.btnCadastro);
		btnCad.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Pedido_Novo();
			}
		});

		ImageButton btnSync = (ImageButton) findViewById(R.id.btnSincroniza);
		btnSync.setVisibility(View.GONE);
		btnSync.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Pedido_Sincronizar(true);
			}
		});

		// configura componente de filtro
		lblFiltro = (TextView) findViewById(R.id.lblStatus);
		txtFiltro = (EditText) findViewById(R.id.edtFiltro);
		txtFiltro.setHint("Razão,Fant.,Cid.,Data");
		txtFiltro.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void afterTextChanged(Editable s) {
				CarregaDados(true);
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();

		// Log.v("Szakbarbar", "Need the cursor from the Service");
		CarregaDados(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
		itemOpt.setShortcut('2', 'f');
		itemOrd.setShortcut('3', 'o');

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
			break;

		case MN_OPCOES:

			op_Opcoes();
			break;

		case MN_FILTRAR:

			op_Filtrar();
			break;

		case MN_ORDENAR:

			AlertDialog dialog;
			AlertDialog.Builder builder = new AlertDialog.Builder(frm_cons_pedidos.this);
			builder.setTitle("Ordenar por").setSingleChoiceItems(col_descricao, col_order, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

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
	public void finish() {
		super.finish();
		return;

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		banco.closeHelper();

	}

	// BLOCO 2 - EVENTOS DE CONSULTAS

	private String convText(TextView v, String text) {

		if (v.getId() == R.id.txtTotal) {
			Cursor cTot = banco.db.rawQuery("select sum(vendas_itens.QTDE * vendas_itens.VALOR) from vendas_itens where vendas_itens.vendaid = " + c.getString(c.getColumnIndex("_id")), null);
			if (cTot.moveToFirst()) {
				try {

					return banco.myCustDecFormatter.format(cTot.getDouble(0));
				} catch (Exception ex) {
				}
			}

		} else if (v.getId() == R.id.txtSituacao) {
			if (c.getDouble(4) <= 0) {
				v.setTextColor(getResources().getColor(R.color.all_cinza));
				return "Pendente";
			} else {
				v.setTextColor(getResources().getColor(R.color.cor_verde));
				return "Sincronizado";
			}
		}

		return text;

	}

	public void CarregaDados(boolean Ordernando) {

		// REMOVE TODOS OS PEDIDOS SEM VALORES
		banco.VERIFICA_DEL_PEDIDOZERO();

		// DEFININDO CAMPO DE ORDENAÇÂO -- COL 0 PADRAO
		String campoOrder = (String) col_banco[0] + " desc";

		// CASO ESTIVER ORDENANDO NO MENU
		if (Ordernando) {
			campoOrder = (String) col_banco[col_order];
			if (col_desc) {
				campoOrder = campoOrder + " desc";
				col_desc = false;
			} else {
				campoOrder = campoOrder + " asc";
				col_desc = true;
			}
		}
		;

		// DEFININDO VALOR DO FILTRO
		String filtro = txtFiltro.getText().toString();

		// FILTRO DE QTDE DA LISTA
		String filtroTop = "";
		if (banco.cons_ped_indexFiltro == 0) {
			filtroTop = "limit 50";
		} else if (banco.cons_ped_indexFiltro == 1) {
			filtroTop = "limit 100";
		}

		// DEFININDO CONSULTA
		try {
			if (filtro.equals("")) {
				c = banco.db.rawQuery("select vendas._id,clientes.nome,clientes.cidade,vendas.data,vendas.total,vendas.sincronizado,clientes.fantasia,clientes.cpf_cnpj,vendas.operacao,vendas.total_st from vendas join clientes on vendas.CPF_CNPJ = clientes.CPF_CNPJ order by " + campoOrder + " " + filtroTop, null);
			} else {
				c = banco.db.rawQuery("select vendas._id,clientes.nome,clientes.cidade,vendas.data,vendas.total,vendas.sincronizado,clientes.fantasia,clientes.cpf_cnpj,vendas.operacao,vendas.total_st from vendas join clientes on vendas.CPF_CNPJ = clientes.CPF_CNPJ where (clientes.nome LIKE " + "'%" + filtro + "%') or (vendas.data LIKE" + " " + "'%" + filtro + "%') or (clientes.fantasia like " + "'%" + filtro + "%') or (clientes.cidade like " + "'%" + filtro + "%')  order by " + campoOrder + " " + filtroTop, null);
			}
		} catch (SQLiteException e) {
			if (e.getMessage().contains("no such column: vendas.CPF_CNPJ")) {
				Intent intent = new Intent(this, frm_sys_principal.class);
				startActivity(intent);
			}
		}

		if (c != null) {
			// INICIANDO CURSOR
			startManagingCursor(c);
			adapter = new frm_cons_pedidos_adapter(this, R.layout.lay_cons_pedidos, c, new String[] { "NOME", "CIDADE", "_id", "DATA", "TOTAL", "SINCRONIZADO", "TOTAL_ST" }, new int[] { R.id.txtNome, R.id.txtCidade, R.id.txtPedido, R.id.txtData, R.id.txtTotal, R.id.txtSituacao, R.id.txtTotalST }, banco.cons_ped_indexTIPO, banco);

			/*
			 * adapter = new SimpleCursorAdapter(this,
			 * R.layout.lay_cons_pedidos, c, new String[] { "NOME","_id",
			 * "DATA", "TOTAL", "SINCRONIZADO"}, new int[] { R.id.txtNome,
			 * R.id.txtPedido, R.id.txtData, R.id.txtTotal, R.id.txtSituacao,})
			 * {
			 * 
			 * @Override public void setViewText(TextView v, String text) {
			 * super.setViewText(v, convText(v, text)); }
			 * 
			 * };
			 */

			ListAdapter lsadapter = adapter;
			ListView listaCons = (ListView) findViewById(R.id.vendaListView);
			listaCons.setAdapter(lsadapter);
			listaCons.setFastScrollEnabled(true);

			lblFiltro.setText("Registros: " + lsadapter.getCount());

			Double totgeral = 0.00;
			if (c.moveToFirst()) {
				do {
					// apenas vendas
					if (c.getLong(c.getColumnIndex("OPERACAO")) == 0) {
						totgeral = totgeral + c.getDouble(c.getColumnIndex("TOTAL"));
					}
				} while (c.moveToNext());
			}

			lblFiltro.setText(lblFiltro.getText().toString() + "  Total Geral: " + banco.myCustDecFormatter.format(totgeral));

			listaCons.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					// @todo

					PedidoID = id;
					op_Opcoes();

					// Toast.makeText(frmConsClientes.this,"O id do item selecionado é: "+
					// String.valueOf(id), Toast.LENGTH_LONG).show();
				}

			});
		}

		// adapter = new
		// ArrayAdapter<String>(this,R.layout.layoutpadrao_consitens,
		// R.id.text1, banco.Sql_Select("CLIENTES", "NOME", "_ID DESC"));
		// ListView listaCons = (ListView) findViewById(R.id.listView1);
		// listaCons.setAdapter(adapter);
		// lblFil.setText(adapter.getCount() + " registros encontrados !!!");

		/*
		 * String[] from = new String[]{"NOME","FANTASIA"}; int[] to = new
		 * int[]{R.id.text1,R.id.text2}; SimpleCursorAdapter adap = new
		 * SimpleCursorAdapter(this,R.layout.layoutpadrao_consitens,
		 * banco.Clientes_Busca(), from, to);
		 */

	}

	// BLOCO 3 - EVENTOS DE MENUS

	// BLOCO 3 - EVENTOS DOS MENUS

	private void op_Opcoes() {

		final CharSequence[] items;
		if (banco.getIsSincronizado("VENDAS", PedidoID)) {
			items = new CharSequence[] { "Cadastrar", "Visualizar", "Remover", "Enviar por Email", "Clonar Pedido" };
		} else {
			if (banco.temp_cSaldoFlex) { // caso use saldo flex não permite
											// sincronizar indivisual
				items = new CharSequence[] { "Cadastrar", "Visualizar", "Remover", "Enviar por Email", "Clonar Pedido" };
			} else {
				items = new CharSequence[] { "Cadastrar", "Visualizar", "Remover", "Enviar por Email", "Clonar Pedido" };
			}
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pedidos - Opções");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				// Toast.makeText(getApplicationContext(), items[item],
				// Toast.LENGTH_SHORT).show();
				if (item == 0) {
					Pedido_Novo();
				} else if (item == 1) {
					Intent intent = new Intent(frm_cons_pedidos.this, frm_cad_pedido.class);
					Bundle b = new Bundle();
					b.putLong("pedidoid", PedidoID);
					intent.putExtras(b);
					startActivity(intent);
				} else if (item == 2) {

					if (banco.getIsSincronizado("VENDAS", PedidoID)) {

						banco.MostraMsg(frm_cons_pedidos.this, "Pedido já está sincronizado !!!");

					} else {

						DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
									case DialogInterface.BUTTON_POSITIVE:

										double saldo = 0;
										double reducao = 0;
										Cursor cursorFlex = banco.db.rawQuery("SELECT SALDO FROM FLEX ORDER BY _id DESC LIMIT 1", null);
										Cursor cursorVenda = banco.db.rawQuery("SELECT ACRESCIMO FROM VENDAS_ITENS WHERE vendaid = " + PedidoID, null);
										cursorFlex.moveToFirst();
										while (cursorVenda.moveToNext()) {
											reducao += cursorVenda.getDouble(0);
										}
										if (cursorFlex.getCount() > 0) {
											saldo = cursorFlex.getDouble(0);
										}

										boolean verificacaoFlex = false;
										if ((saldo - reducao) >= 0 && banco.temp_cSaldoFlex) {
											verificacaoFlex = true;
										} else {
											verificacaoFlex = false;
										}
										if (!banco.temp_cSaldoFlex) {
											verificacaoFlex = true;
										}

										if(verificacaoFlex){
										// remove os produtos
										Cursor cItensDel = banco.db.rawQuery("select _id,produtoid,flex_acrescimo,flex_desconto from vendas_itens where vendaid = " + String.valueOf(PedidoID), null);
										if (cItensDel.moveToFirst()) {
											do {
												banco.TB_VENDAS_ITENS_DELETAR(PedidoID, cItensDel.getLong(0), cItensDel.getLong(1), cItensDel.getDouble(2), cItensDel.getDouble(3));
											} while (cItensDel.moveToNext());
										}
										if (cItensDel != null || !cItensDel.isClosed()) {
											cItensDel.close();
										}

										// remove o pedido
										banco.db.delete("VENDAS", "_ID=" + String.valueOf(PedidoID), null);

										CarregaDados(false);

								}else{
									AlertDialog ad = new AlertDialog.Builder(frm_cons_pedidos.this).create();
									ad.setCancelable(true); // This blocks
									// the 'BACK'
									// button
									DecimalFormat df = new DecimalFormat(",##0,00");
									ad.setTitle("SmartMobile - Saldo Flex");
									ad.setMessage("Saldo Flex não poder ficar negativo, favor entrar em contato com sua empresa.\nSaldo atual: " + df.format(saldo) + "\nEstorno[Acres. Ped.]: " + df.format(reducao) + "\nSaldo final: " + df.format((saldo - reducao)));
									ad.setButton("Ok", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();
										}
									});
									ad.show();
								}

								case DialogInterface.BUTTON_NEGATIVE:
								// No button clicked
								break;
							}
						}
					} ;

					AlertDialog.Builder builder2 = new AlertDialog.Builder(frm_cons_pedidos.this);
					builder2.setMessage("Deseja mesmo remover o Pedido nº " + String.valueOf(PedidoID) + " ?").setPositiveButton("Sim", dialogClickListener).setNegativeButton("Não", dialogClickListener).show();
				}

			}

			else if(item==3)

			{

				// busca dados do pedido
				String Data = "";
				String NomeCliente = "";
				String EmailCliente = "";
				Double TotalPedido = 0.00;

				Cursor cCliente = banco.db.rawQuery("select vendas.data,clientes.nome,clientes.email,vendas.total from vendas join clientes on vendas.CPF_CNPJ = clientes.CPF_CNPJ where vendas._id = " + String.valueOf(PedidoID), null);
				if (cCliente.moveToFirst()) {

					Data = cCliente.getString(0);
					NomeCliente = cCliente.getString(1);
					EmailCliente = cCliente.getString(2);
					TotalPedido = cCliente.getDouble(3);
				}

				if (cCliente != null || !cCliente.isClosed()) {
					cCliente.close();
				}

				// monta o corpo do pedido com os produtos
				String listaProdutos = "";
				Cursor cProdutos = banco.db.rawQuery("select vendas_itens.produtoid,produtos.descricao,vendas_itens.qtde,(vendas_itens.valor + (vendas_itens.acrescimo / vendas_itens.qtde) - (vendas_itens.desconto / vendas_itens.qtde)), produtos.LINHA, produtos.COLUNA, produtos.UND  from vendas_itens join produtos on vendas_itens.produtoid = produtos.produtoid and vendas_itens.unidadeid = produtos.unidadeid and vendas_itens.linhaid = produtos.linhaid and vendas_itens.colunaid = produtos.colunaid where vendas_itens.vendaid = " + String.valueOf(PedidoID), null);
				if (cProdutos.moveToFirst()) {
					do {

						// codigo + descricao
						listaProdutos = listaProdutos + cProdutos.getString(0) + " - " + cProdutos.getString(1);

						// caso exitir dados de grade
						if ((!cProdutos.getString(cProdutos.getColumnIndex("LINHA")).equals("")) || (!cProdutos.getString(cProdutos.getColumnIndex("COLUNA")).equals(""))) {
							listaProdutos = listaProdutos + " - " + cProdutos.getString(cProdutos.getColumnIndex("LINHA")) + ": " + cProdutos.getString(cProdutos.getColumnIndex("COLUNA"));
						}

						// und + qtde + unit + total
						listaProdutos = listaProdutos + " >> " + cProdutos.getString(cProdutos.getColumnIndex("UND")) + " >> Qtde: " + banco.myCustDecFormatter.format(cProdutos.getDouble(2)) + " >> Unit: " + banco.myCustDecFormatter.format(cProdutos.getDouble(3)) + " >> Total: " + banco.myCustDecFormatter.format(cProdutos.getDouble(2) * cProdutos.getDouble(3)) + "<br>";

					} while (cProdutos.moveToNext());
				}
				if (cProdutos != null || !cProdutos.isClosed()) {
					cProdutos.close();
				}

				String[] recipients = new String[]{EmailCliente, "",};
				StringBuilder body = new StringBuilder();
				body.append("-----------------------------------------------<br>");
				body.append("<b>Detalhes do Pedido: </b><br>");
				body.append("-----------------------------------------------<br>");
				body.append("Empresa: " + banco.NomeEmpresa + "<br>");
				body.append("Vendedor: " + banco.NomeVendedor + "<br>");
				body.append("Cliente: " + NomeCliente + "<br>");
				body.append("Data: " + Data + "<br>");
				body.append("-----------------------------------------------<br>");
				body.append("<b>Produtos: </b><br>");
				body.append("-----------------------------------------------<br>");
				body.append(listaProdutos + "<br>");
				body.append("-----------------------------------------------<br>");
				body.append("<b>Total R$: </b>" + banco.myCustDecFormatter.format(TotalPedido) + "<br>");
				body.append("-----------------------------------------------<br><br>");
				body.append("<a href='www.amsoft.com.br/solucoes/smartmobile'>SmartMobile - Força de Vendas</a><br>");
				body.append("<a href='www.amsoft.com.br'>AmSoft - Soluções em Softwares</a><br>");

				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				emailIntent.setType("text/html");
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, banco.NomeEmpresa + " - Pedido nº" + String.valueOf(PedidoID));
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(body.toString()));
				startActivity(Intent.createChooser(emailIntent, "Enviar email ..."));

			}

			else if(item==4)

			{
				Long idNovo = 0l;
				Cursor rs = banco.db.rawQuery("select OPERACAO, CPF_CNPJ, FORMA_PGTOID, LISTAID, TOTAL, OBS, ORIGEM from vendas where vendas._id = " + String.valueOf(PedidoID), null);
				while (rs.moveToNext()) {

					// Vendas: OPERACAO, CLIENTEID, DATA, FORMA_PGTOID,
					// LISTAID, OBS, TOTAL, TOTAL_ST, SINCRONIZADO

					idNovo = banco.TB_VENDAS_INSERIR(0, rs.getInt(0), rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getDouble(4), rs.getString(5), rs.getInt(6));

				}

				Cursor rs2 = banco.db.rawQuery("select item.PRODUTOID, item.UNIDADEID, item.LINHAID, item.COLUNAID, produtos.DESCRICAO as descricao, " + "item.QTDE, item.ACRESCIMO, item.DESCONTO, item.VALOR, item.FLEX_ACRESCIMO, item.FLEX_DESCONTO, item.VALOR_ST, item.peso_total " + "from VENDAS_ITENS as item join PRODUTOS on item.produtoid = produtos.produtoid and item.unidadeid = produtos.unidadeid and item.linhaid = produtos.linhaid and item.colunaid = produtos.colunaid " + "where item.VENDAID = " + String.valueOf(PedidoID), null);
				while (rs2.moveToNext()) {
					// itens: "VENDAID, PRODUTOID, UNIDADEID, LINHAID,
					// COLUNAID, QTDE, ACRESCIMO, DESCONTO, VALOR,
					// FLEX_ACRESCIMO, FLEX_DESCONTO, VALOR_ST

					double valorTotal = rs2.getDouble(rs2.getColumnIndex("VALOR"));
					double valorTotalSt = rs2.getDouble(rs2.getColumnIndex("VALOR_ST"));
					double acrescimo = rs2.getDouble(rs2.getColumnIndex("ACRESCIMO"));
					double desconto = rs2.getDouble(rs2.getColumnIndex("DESCONTO"));
					double quantidade = rs2.getDouble(rs2.getColumnIndex("QTDE"));
					valorTotal = ((valorTotal + acrescimo) - desconto) * quantidade;

					//aqui já tem ST + IPI somado no mesmo valor
					double percST = valorTotalSt - valorTotal;
					percST = (100 * percST) / valorTotal;
					double pesoTotal = rs2.getDouble(rs2.getColumnIndex("peso_total"));

					banco.TB_VENDAS_ITENS_INSERIR(idNovo, 0, rs2.getInt(rs2.getColumnIndex("PRODUTOID")), rs2.getInt(rs2.getColumnIndex("UNIDADEID")), rs2.getInt(rs2.getColumnIndex("LINHAID")), rs2.getInt(rs2.getColumnIndex("COLUNAID")), rs2.getString(rs2.getColumnIndex("descricao")), quantidade, acrescimo, desconto, rs2.getDouble(rs2.getColumnIndex("VALOR")), rs2.getDouble(rs2.getColumnIndex("FLEX_ACRESCIMO")), rs2.getDouble(rs2.getColumnIndex("FLEX_DESCONTO")), percST, 0.00, pesoTotal, null);
				}

				CarregaDados(false);

			}

		}
	});
		AlertDialog alert = builder.create();
		alert.show();

	}

	private void op_Filtrar() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pedidos - Filtrar");
		final CharSequence[] choiceList =

		{ "Últimos 50", "Últimos 100", "Todos os Pedidos" };

		builder.setSingleChoiceItems(choiceList, banco.cons_ped_indexFiltro,

		new DialogInterface.OnClickListener() {

			public void onClick(

			DialogInterface dialog,

			int which) {

				banco.cons_ped_indexFiltro = which;
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

	private void Pedido_Novo() {
		Intent intent = new Intent(frm_cons_pedidos.this, frm_cad_pedido.class);
		Bundle b = new Bundle();
		b.putLong("pedidoid", 0);
		intent.putExtras(b);
		startActivity(intent);
	}

	private void Pedido_Sincronizar(boolean todos) {
		DB_Sincroniza dbSync = new DB_Sincroniza(frm_cons_pedidos.this);
		if (todos) {
			dbSync.Syncroniza_Pedidos(0);
		} else {
			dbSync.Syncroniza_Pedidos(PedidoID);
		}
	}

}