package smart.mobile.consulta.produtos;

import java.util.ArrayList;
import java.util.HashMap;

import com.uphyca.sqlite.lazyloading.LazyLoadingSQLiteQueryBuilder;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.download.DB_Sincroniza;
import smart.mobile.PrincipalClasse;
import smart.mobile.model.ProdutoComparacao;
import smart.mobile.R;
import smart.mobile.cadastro.pedido.frm_cad_pedido_item;
import smart.mobile.model.Estoque;
import smart.mobile.utils.ConfiguracaoInicialTela;
import smart.mobile.utils.error.ErroGeralController;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class frm_cons_produtos extends Activity {

	// variavel principal de acesso ao banco
	private DB_LocalHost banco;
	CharSequence[] statesTexto = { "Sincronizar", "Produtos com Estoque" };
	boolean[] statesOpcoes = { false, false };

	// definição de colunas da consulta
	int ultPosicao = 0;
	int col_order = 1;
	boolean col_desc = false;

	final CharSequence[] col_banco = { "produtos.produtoid", "produtos.descricao", "produtos.grupo", "produtos.valor", "produtos.estoque" };
	final CharSequence[] col_descricao = { "Código", "Descrição", "Grupo", "Valor", "Estoque" };

	private EditText filterText = null;
	SimpleCursorAdapter adapter;
	TextView lblFiltro = null;
	EditText txtFiltro = null;

	Cursor c;
	ListView listaCons;

	private final int MN_VOLTAR = 0;
	private final int MN_OPCOES = 1;
	private final int MN_FILTRAR = 2;
	private final int MN_ORDENAR = 3;

	long ItemId = 0;
	long PedidoID = 0;
	String UF = "";
	long ListaID = 0;
	String ListaID_Descricao = "";
	String ClienteSel = "";
	boolean pesquisaDesc = true;
	boolean listarDestaques = true;
	boolean listarAdd = true;
	boolean listarJaComprados = true;
	private ArrayList<ProdutoComparacao> produtosAdds = null;
	private int tipoListaTemp;
	private Context context;
	private HashMap<String, String> valoresWhere = new HashMap<String, String>();

	private String whereConsulta;
	private int valorEscolhido;
	private String[] choiceList;
	private int tipoListaEstoque;
	private String codigoBarras;
	private PrincipalClasse aplication;
	private Estoque estoque;
	private int antigoLayout = 0;
	private boolean fatorEstoque;

	// BLOCO 1 - EVENTOS DA ACTIVITY

	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		setContentView(R.layout.activity_ctela_base);

		// 22/09/2016 - ref. chamado 5998 - adicionado parametro da 'Lista de Preço' para exibir já o preço final na Consulta de Produtos
		try {
			Bundle b = getIntent().getExtras();
			ListaID = b.getLong("listaid");
			ListaID_Descricao = b.getString("listaid_descricao");
		} catch (Exception ex) {
			ListaID = 0;
			ListaID_Descricao = "";
		}
		Log.i("ref.5898--y--ListaID ", String.valueOf(ListaID) + "-" + ListaID_Descricao);

		//if(ListaID > 0){
		//	setTitle("Produtos [Lista:"+ListaID_Descricao+ "]");
		//}else {
			setTitle("SmartMobile - Consulta de Produtos");
		//}

		ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.layoutpadrao_consgeral, false);

		RelativeLayout footer = (RelativeLayout) findViewById(R.id.FooterEsconder);
		footer.setVisibility(View.GONE);


		try {
			Bundle b = getIntent().getExtras();
			PedidoID = b.getLong("pedidoid");
			UF = b.getString("uf");

		} catch (Exception ex) {
		}

		antigoLayout = banco.cons_prod_indexTIPO;
		this.fatorEstoque = false;
		try {
			Bundle b = getIntent().getExtras();

			this.tipoListaEstoque = b.getInt("tipoLista");
			if (tipoListaEstoque == 2) {
				antigoLayout = banco.cons_prod_indexTIPO;
				banco.cons_prod_indexTIPO = tipoListaEstoque;
				this.fatorEstoque = true;
			}

			aplication = (PrincipalClasse) getApplication();

			this.codigoBarras = b.getString("codigoBarra");

		} catch (Exception e) {

		}

		// carrega dados do banco
		this.banco = new DB_LocalHost(this);

		ErroGeralController erro = new ErroGeralController(this, banco);

		this.context = this;

		choiceList = context.getResources().getStringArray(R.array.ProdutosWhere);

		Cursor cursorWhere = banco.db.rawQuery("SELECT CONSULTA_PRODUTO_WHERE FROM CONFIG_DINAMICA", null);
		cursorWhere.moveToFirst();
		if (!cursorWhere.getString(0).isEmpty()) {
			whereConsulta = cursorWhere.getString(0);
		} else {
			whereConsulta = "Todos";
		}
		valoresWhere.put("Descrição", "DESCRICAO");
		valoresWhere.put("Grupo", "GRUPO");

		verificarValorFiltro();

		ImageView filtro = (ImageView) findViewById(R.id.btnFiltro);
		filtro.setVisibility(View.VISIBLE);

		filtro.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(frm_cons_produtos.this);
				builder.setTitle("Tipo de Lista");
				builder.setSingleChoiceItems(choiceList, valorEscolhido,

				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						whereConsulta = choiceList[which];
						updateWhereBanco(choiceList[which]);
						CarregaDados(false, 0);
						verificarValorFiltro();
						dialog.cancel();
					}

				});

				AlertDialog alert = builder.create();

				alert.show();

			}
		});

		// configura componente de filtro
		// filterText = (EditText) findViewById(R.id.edtFiltro);
		// filterText.addTextChangedListener(filterTextWatcher);
		lblFiltro = (TextView) findViewById(R.id.lblStatus);
		listaCons = (ListView) findViewById(R.id.vendaListView);

		LinearLayout linear = (LinearLayout) findViewById(R.id.linearProdutos);
		linear.setVisibility(View.VISIBLE);
		LinearLayout llDestaque = (LinearLayout) findViewById(R.id.produtosEmDestaque);

		llDestaque.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (listarDestaques) {
					banco.cons_prod_Destaque = true;
					CarregaDados(false, 0);
					listarDestaques = false;
					ImageView img = (ImageView) arg0.findViewById(R.id.checkDestaque);
					img.setImageResource(R.drawable.ico_check1);
				} else {
					banco.cons_prod_Destaque = false;
					CarregaDados(false, 0);
					listarDestaques = true;
					ImageView img = (ImageView) arg0.findViewById(R.id.checkDestaque);
					img.setImageResource(R.drawable.ico_check0);
				}

			}
		});

		if (PedidoID != 0) {

			Cursor cursorIds;
			cursorIds = banco.db.rawQuery("SELECT item.PRODUTOID, item.UNIDADEID, item.LINHAID, item.COLUNAID, vendas.DATA, item.QTDE FROM VENDAS_ITENS as item INNER JOIN VENDAS on item.vendaid = vendas._id WHERE vendaid in (select _id from vendas where CPF_CNPJ like (select CPF_CNPJ from vendas where _id = " + PedidoID + " )) ORDER BY item.VENDAID DESC", null);
			produtosAdds = new ArrayList<ProdutoComparacao>();
			while (cursorIds.moveToNext()) {
				ProdutoComparacao produto = new ProdutoComparacao();
				produto.setProdutoId(cursorIds.getInt(cursorIds.getColumnIndex("PRODUTOID")));
				produto.setUnidadeId(cursorIds.getInt(cursorIds.getColumnIndex("UNIDADEID")));
				produto.setLinhaId(cursorIds.getInt(cursorIds.getColumnIndex("LINHAID")));
				produto.setColunaId(cursorIds.getInt(cursorIds.getColumnIndex("COLUNAID")));
				produto.setData(cursorIds.getString(cursorIds.getColumnIndex("DATA")));
				produto.setQtde(cursorIds.getDouble(cursorIds.getColumnIndex("QTDE")));

				produtosAdds.add(produto);
			}

			//cursorIds = banco.db.rawQuery("select lista.listaid,lista.DESCRICAO from vendas inner join LISTAS_PRECOS lista on vendas.LISTAID = lista.LISTAID where vendas._id = " + PedidoID, null);
			//while (cursorIds.moveToNext()) {
			//	this.listaIdDescricao = cursorIds.getString(cursorIds.getColumnIndex("DESCRICAO"));
			//}

		} else {
			if (tipoListaEstoque != 2) {
				LinearLayout llComprados = (LinearLayout) findViewById(R.id.produtosComprados);
				LinearLayout llAdd = (LinearLayout) findViewById(R.id.produtosAdd);
				LinearLayout llDestaqueCenter = (LinearLayout) findViewById(R.id.produtosEmDestaque);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) llDestaqueCenter.getLayoutParams();
				params.weight = 100.0f;
				llDestaqueCenter.setLayoutParams(params);
				llDestaqueCenter.setGravity(Gravity.CENTER_HORIZONTAL);

				llComprados.setVisibility(View.GONE);
				llAdd.setVisibility(View.GONE);
			}
		}

		if (PedidoID != 0 || banco.cons_prod_indexTIPO == 2) {
			LinearLayout llComprados = (LinearLayout) findViewById(R.id.produtosComprados);

			llComprados.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (listarJaComprados) {
						banco.cons_prod_JaComprado = true;
						CarregaDados(false, 0);
						listarJaComprados = false;
						ImageView img = (ImageView) arg0.findViewById(R.id.checkComprado);
						img.setImageResource(R.drawable.ico_check1);
					} else {
						banco.cons_prod_JaComprado = false;
						CarregaDados(false, 0);
						listarJaComprados = true;
						ImageView img = (ImageView) arg0.findViewById(R.id.checkComprado);
						img.setImageResource(R.drawable.ico_check0);
					}
				}
			});

			LinearLayout llAdd = (LinearLayout) findViewById(R.id.produtosAdd);

			llAdd.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (listarAdd) {
						banco.cons_prod_Add = true;
						CarregaDados(false, 0);
						listarAdd = false;
						ImageView img = (ImageView) arg0.findViewById(R.id.checkAdd);
						img.setImageResource(R.drawable.ico_check1);
					} else {
						banco.cons_prod_Add = false;
						CarregaDados(false, 0);
						listarAdd = true;
						ImageView img = (ImageView) arg0.findViewById(R.id.checkAdd);
						img.setImageResource(R.drawable.ico_check0);
					}
				}
			});
		}

		ImageButton btnTipoLista = (ImageButton) findViewById(R.id.btnTipoLista);
		if (banco.cons_prod_indexTIPO == 2) {

		} else {
			btnTipoLista.setVisibility(View.VISIBLE);
		}
		btnTipoLista.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				AlertDialog.Builder builder = new AlertDialog.Builder(frm_cons_produtos.this);
				builder.setTitle("Tipo de Lista");
				final CharSequence[] choiceList =

				{ "Lista Simples", "Lista Completa" };

				builder.setSingleChoiceItems(choiceList, banco.cons_prod_indexTIPO,

				new DialogInterface.OnClickListener() {

					public void onClick(

					DialogInterface dialog,

					int which) {

						banco.cons_prod_indexTIPO = which;
						antigoLayout = which;
						dialog.cancel();
						CarregaDados(false, 0);

					}

				});

				AlertDialog alert = builder.create();

				alert.show();

			}
		});

		ImageButton btnCad = (ImageButton) findViewById(R.id.btnCadastro);
		// btnCad.setEnabled(false);
		btnCad.setVisibility(View.GONE);

		ImageButton btnSync = (ImageButton) findViewById(R.id.btnSincroniza);
		btnSync.setVisibility(View.GONE);
		btnSync.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Produto_Sincroniza();
			}
		});

		txtFiltro = (EditText) findViewById(R.id.edtFiltro);

		if (tipoListaEstoque == 2) {
			try{
				txtFiltro.setText(aplication.getTextFiltroProduto());
			}catch (Exception e){
				e.printStackTrace();
			}
			txtFiltro.setHint("Desc.,Cód.,Grupo, Marca");
		} else {
			txtFiltro.setHint("Desc.,Cód.,Grupo");
		}
		txtFiltro.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			public void afterTextChanged(Editable s) {

				CarregaDados(false, 0);
				if (s.toString().trim().equals("")) {
					DefineTeclado();
				}

			}
		});

		if (banco.cons_prod_indexTIPO == 2) {
			TextView incluso = (TextView) findViewById(R.id.lblStatusAnt);
			TextView naoIncluso = (TextView) findViewById(R.id.lblStatusAdd);

			incluso.setText(" Incluso na Lista ");
			naoIncluso.setText(" Não Incluso na Lista ");
			naoIncluso.setBackgroundColor(Color.WHITE);
		}

		// carrega consulta
		// CarregaDados(false, 0);

	}

	private void verificarValorFiltro() {
		for (int i = 0; i < choiceList.length; i++) {
			if (choiceList[i].equals(whereConsulta)) {
				valorEscolhido = i;
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		DefineTeclado();
		// Log.i("TENTANDO", "RESTAURAR POSIÇÃO");
		// c.moveToPosition(ultPosicao);
		if (listaCons != null) {
			listaCons.setSelection(ultPosicao);
			listaCons.invalidateViews();
		}

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
			AlertDialog.Builder builder = new AlertDialog.Builder(frm_cons_produtos.this);
			builder.setTitle("Ordenar por").setSingleChoiceItems(col_descricao, col_order, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					Toast.makeText(getApplicationContext(), "Ordenar por: " + col_descricao[item], Toast.LENGTH_SHORT).show();
					col_order = item;
					CarregaDados(true, 0);
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
	protected void onPause() {
		try {
			aplication.setTextFiltroProduto(txtFiltro.getText().toString());
		} catch (Exception e){
			e.printStackTrace();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {

		banco.cons_prod_indexTIPO = antigoLayout;
		banco.closeHelper();
		super.onDestroy();

	}

	// BLOCO 2 - EVENTOS DE CONSULTAS

	private void DefineTeclado() {

		if (!txtFiltro.getText().toString().equals("")) {

			try {
				// VERIFICA SE O FILTRO É UM NUMERO
				Integer.parseInt(txtFiltro.getText().toString());

				// MANTEM O TECLADO NO PADRAO NUMERICO CASO TENHA FILTRADO
				// CODIGO(INCAS)

				txtFiltro.setInputType(InputType.TYPE_CLASS_NUMBER);

			} catch (NumberFormatException nfe) {
				txtFiltro.setInputType(InputType.TYPE_CLASS_TEXT);
			}

			txtFiltro.selectAll();
			txtFiltro.requestFocus();

		} else {
			txtFiltro.setInputType(InputType.TYPE_CLASS_TEXT);
		}
	}

	private String convText(TextView v, String text) {

		if (v.getId() == R.id.txtValor) {
			if (c.getDouble(c.getColumnIndex("VALOR")) <= 0) {
				v.setTextColor(getResources().getColor(R.color.cor_vermelho));
			} else {
				v.setTextColor(getResources().getColor(R.color.all_black));
			}
			return banco.myCustDecFormatter.format(c.getDouble(c.getColumnIndex("VALOR")));
		} else if (v.getId() == R.id.txtEstoque) {
			if (c.getDouble(c.getColumnIndex("ESTOQUE")) <= 0) {
				v.setTextColor(getResources().getColor(R.color.cor_vermelho));
			} else {
				v.setTextColor(getResources().getColor(R.color.all_black));
			}
			return banco.myCustDecFormatter.format(c.getDouble(c.getColumnIndex("ESTOQUE")));
		}

		return text;
	}

	public void CarregaDados(boolean Ordernando, int yIDDetalhar) {
		boolean inserirFinal = false;
		// DEFININDO CAMPO DE ORDENAÇÂO -- COL 1 PADRAO = DESCRICAO
		String campoOrder = "produtos.destaque desc, " + (String) col_banco[1] + " asc";

		// CASO ESTIVER ORDENANDO NO MENU
		if (Ordernando) {
			campoOrder = "produtos.destaque desc, " + (String) col_banco[col_order];
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
		// DEFININDO CONSULTA
		String strWhere = "WHERE";

		if (codigoBarras == null) {
			// FILTRO DE ESTOQUE
			if (banco.cons_prod_indexFiltro == 0) {
				strWhere += " ESTOQUE > 0";
			} else if (banco.cons_prod_indexFiltro == 1) {
				strWhere += " ESTOQUE <= 0";
			}

			if (banco.cons_prod_Destaque) {
				if (strWhere.equals("WHERE")) {
					strWhere += " destaque > 0";
				} else {
					strWhere += " AND destaque > 0";
				}
			}

			if (banco.cons_prod_Add) {
				Cursor cursorIds;
				if (banco.cons_prod_indexTIPO == 2) {
					if (!strWhere.equals("WHERE")) {
						strWhere += " AND ( not exists (select 1 from estoque e where e.PRODUTOID = produtos.PRODUTOID and e.COLUNAID = produtos.COLUNAID and e.UNIDADEID = produtos.UNIDADEID and e.LINHAID = produtos.LINHAID))";
					} else {
						strWhere = "WHERE not exists (select 1 from estoque e where e.PRODUTOID = produtos.PRODUTOID and e.COLUNAID = produtos.COLUNAID and e.UNIDADEID = produtos.UNIDADEID and e.LINHAID = produtos.LINHAID)";
					}
				} else {
					cursorIds = banco.db.rawQuery("SELECT item.PRODUTOID, item.UNIDADEID, item.LINHAID, item.COLUNAID FROM VENDAS_ITENS as item WHERE item.vendaid = " + PedidoID, null);

					produtosAdds = new ArrayList<ProdutoComparacao>();
					while (cursorIds.moveToNext()) {
						ProdutoComparacao produto = new ProdutoComparacao();
						produto.setProdutoId(cursorIds.getInt(cursorIds.getColumnIndex("PRODUTOID")));
						produto.setUnidadeId(cursorIds.getInt(cursorIds.getColumnIndex("UNIDADEID")));
						produto.setLinhaId(cursorIds.getInt(cursorIds.getColumnIndex("LINHAID")));
						produto.setColunaId(cursorIds.getInt(cursorIds.getColumnIndex("COLUNAID")));

						produtosAdds.add(produto);
					}

					strWhere = monteSqlArray(strWhere);
				}
			}

			if (banco.cons_prod_JaComprado) {
				Cursor cursorIds;
				if (banco.cons_prod_indexTIPO == 2) {
					cursorIds = banco.db.rawQuery("SELECT PRODUTOID, UNIDADEID, LINHAID, COLUNAID FROM estoque", null);
				} else {
					cursorIds = banco.db.rawQuery("SELECT item.PRODUTOID, item.UNIDADEID, item.LINHAID, item.COLUNAID, vendas.DATA, item.QTDE FROM VENDAS_ITENS as item INNER JOIN VENDAS on item.vendaid = vendas._id WHERE vendaid in (select _id from vendas where CPF_CNPJ like (select CPF_CNPJ from vendas where _id = " + PedidoID + " ) AND _id <> " + PedidoID + ") ORDER BY item.VENDAID DESC ", null);
				}
				produtosAdds = new ArrayList<ProdutoComparacao>();
				while (cursorIds.moveToNext()) {
					ProdutoComparacao produto = new ProdutoComparacao();
					produto.setProdutoId(cursorIds.getInt(cursorIds.getColumnIndex("PRODUTOID")));
					produto.setUnidadeId(cursorIds.getInt(cursorIds.getColumnIndex("UNIDADEID")));
					produto.setLinhaId(cursorIds.getInt(cursorIds.getColumnIndex("LINHAID")));
					produto.setColunaId(cursorIds.getInt(cursorIds.getColumnIndex("COLUNAID")));
					if (banco.cons_prod_indexTIPO != 2) {
						produto.setData(cursorIds.getString(cursorIds.getColumnIndex("DATA")));
						produto.setQtde(cursorIds.getDouble(cursorIds.getColumnIndex("QTDE")));
					}

					produtosAdds.add(produto);
				}
				strWhere = monteSqlArray(strWhere);

			}

			// FILTRO DOS OUTROS CAMPOS
			if (!filtro.equals("")) {

				if (!strWhere.equals("WHERE")) {
					inserirFinal = true;
					strWhere = strWhere + " AND (";
				} else {
					strWhere = "";
					strWhere = "WHERE ";
				}

				if (whereConsulta.equalsIgnoreCase("todos")) {

					if (banco.Banco.equalsIgnoreCase("incas")) {
						try {
							// pega tambem pelo codigo inteiro
							Integer.parseInt(filtro);

							strWhere = strWhere + "PRODUTOS.PRODUTOID = " + filtro;
						} catch (NumberFormatException nfe) {
							if (pesquisaDesc) {

								strWhere = strWhere + "DESCRICAO LIKE " + "'%" + filtro + "%' OR GRUPO LIKE " + "'" + filtro + "%'";

							} else {
//								inserirFinal = true;
								strWhere = strWhere + "(DESCRICAO LIKE " + "'%" + filtro + "%') OR (GRUPO LIKE " + "'" + filtro + "')";
							}
						}
					} else {

						try {
							// pega tambem pelo codigo inteiro
							Integer.parseInt(filtro);

							strWhere = strWhere + "(PRODUTOS.PRODUTOID = " + filtro + ") OR CODIGO LIKE '" + filtro + "%'";
						} catch (NumberFormatException nfe) {
							if (pesquisaDesc) {

								strWhere = strWhere + "DESCRICAO LIKE " + "'%" + filtro + "%' OR CODIGO LIKE '" + filtro + "%' OR GRUPO LIKE " + "'" + filtro + "%' OR MARCA LIKE '" + filtro + "%'";

							} else {
//								inserirFinal = true;
								strWhere = strWhere + "((DESCRICAO LIKE " + "'%" + filtro + "%') OR (GRUPO LIKE " + "'" + filtro + "') OR (CODIGO LIKE '" + filtro + "')) OR MARCA LIKE '" + filtro + "%'";
							}
						}
					}

				} else {

					if (whereConsulta.equalsIgnoreCase("Código")) {
						if (banco.Banco.equalsIgnoreCase("incas")) {
							try {
								// pega tambem pelo codigo inteiro
								Integer.parseInt(filtro);

								strWhere = strWhere + "PRODUTOS.PRODUTOID = " + filtro;
							} catch (Exception e) {

							}
						} else {
							try {
								// pega tambem pelo codigo inteiro
								Integer.parseInt(filtro);

								strWhere = strWhere + "(PRODUTOS.PRODUTOID = " + filtro + ") OR CODIGO LIKE '" + filtro + "%'";
							} catch (NumberFormatException nfe) {

							}
						}
					} else {

						if (whereConsulta.equalsIgnoreCase("descrição")) {
							strWhere = strWhere + valoresWhere.get(whereConsulta) + " LIKE " + "'%" + filtro + "%'";
						} else {
							strWhere = strWhere + valoresWhere.get(whereConsulta) + " LIKE " + "'" + filtro + "%'";
						}
					}
				}
			} else {
				pesquisaDesc = true;
			}
		} else {
			strWhere = " CODIGO_BARRA LIKE '" + codigoBarras.trim() + "'";
		}

		if (fatorEstoque) {
			if (strWhere.trim().equals("WHERE")) {
				strWhere += " FATOR = 1 ";
				if (!filtro.trim().isEmpty()) {
					strWhere += "OR CODIGO_BARRA like '%" + filtro.trim() + "%'";
				}
			} else {
				strWhere += " AND  FATOR = 1 ";
				if (!filtro.trim().isEmpty()) {
					strWhere += "OR CODIGO_BARRA like '%" + filtro.trim() + "%'";
				}
			}
		}

		if (inserirFinal ) {
			strWhere += " )";
		} 

		
		if (strWhere.trim().equals("WHERE")) {
			strWhere = "";
		}

		c = null;
		/* ACHAR SOLUÇÃO */
		/* O PROBLEMA É AQUI DE LENTIDÃO */
		/* ESTA RECARREGANDO SEMPRE A LISTA DE PRODUTOS E NÃO PODE */
		String[] colunasZero = new String[] { "0  AS QTDE", "PRODUTOS._id as _id", "DESCRICAO", "PRODUTOID", "UND", "VALOR", "GRUPO", "ESTOQUE", "LINHAID", "COLUNAID", "LINHA", "COLUNA", "PRODUTOS.ALIQUOTA_IPI as ALIQUOTA_IPI", "PRODUTOS.IMPOSTOID as IMPOSTOID", "'" + UF + "' AS UF", "IMPOSTOS.ALIQUOTA_UF as ALIQUOTA_UF", "IMPOSTOS.SUBS_ALIQ as SUBS_ALIQ", "IMPOSTOS.SUBS_IVA as SUBS_IVA", "PRODUTOS.DESTAQUE as DESTAQUE", "PRODUTOS.UNIDADEID as UNIDADEID", "PRODUTOS.CODIGO as CODIGO", "PRODUTOS.CODIGO_BARRA as CODIGO_BARRA", "PRODUTOS.FATOR as FATOR", "MARCA" };
		String[] colunasUm = new String[] { "-1 AS QTDE", "PRODUTOS._id as _id", "DESCRICAO", "PRODUTOID", "UND", "VALOR", "GRUPO", "ESTOQUE", "LINHAID", "COLUNAID", "LINHA", "COLUNA", "PRODUTOS.ALIQUOTA_IPI as ALIQUOTA_IPI", "PRODUTOS.IMPOSTOID as IMPOSTOID", "UF", "IMPOSTOS.ALIQUOTA_UF as ALIQUOTA_UF", "IMPOSTOS.SUBS_ALIQ as SUBS_ALIQ", "IMPOSTOS.SUBS_IVA as SUBS_IVA", "PRODUTOS.DESTAQUE as DESTAQUE", "PRODUTOS.UNIDADEID as UNIDADEID", "PRODUTOS.CODIGO as CODIGO", "PRODUTOS.CODIGO_BARRA as CODIGO_BARRA", "PRODUTOS.FATOR as FATOR", "MARCA" };

		LazyLoadingSQLiteQueryBuilder builder = new LazyLoadingSQLiteQueryBuilder(30);

		if (PedidoID == 0) {

			if (adapter != null) {
				builder.setTables("PRODUTOS LEFT OUTER JOIN IMPOSTOS ON PRODUTOS.IMPOSTOID = IMPOSTOS.IMPOSTOID AND ((IMPOSTOS.UF = '" + UF + "') OR (IMPOSTOS.UF = '" + "ZZ" + "')) ");
				c = builder.query(banco.db, colunasUm, (strWhere.trim().isEmpty()) ? null : strWhere.replace("WHERE", "").trim(), new String[] {}, null, null, campoOrder);

				if (tipoListaTemp == banco.cons_prod_indexTIPO) {
					adapter.changeCursor(c);
				} else {
					adapter = new frm_cons_produtos_adapter(this, R.layout.lay_cons_produtos, c, banco, new String[] { "DESCRICAO", "PRODUTOID", "UND", "VALOR", "GRUPO", "ESTOQUE", "DESTAQUE" }, new int[] { R.id.txtDescricao, R.id.txtCodigo, R.id.txtUN, R.id.txtValor, R.id.txtGrupo, R.id.txtEstoque }, banco.cons_prod_indexTIPO, yIDDetalhar, PedidoID, produtosAdds, ListaID);
					ListAdapter lsadapter = adapter;
					listaCons.setAdapter(lsadapter);
					listaCons.setFastScrollEnabled(true);
					listaCons.setDrawSelectorOnTop(true);
					tipoListaTemp = banco.cons_prod_indexTIPO;
				}
			} else {
				// startManagingCursor(c);
				builder.setTables("PRODUTOS LEFT OUTER JOIN IMPOSTOS ON PRODUTOS.IMPOSTOID = IMPOSTOS.IMPOSTOID AND ((IMPOSTOS.UF = '" + UF + "') OR (IMPOSTOS.UF = '" + "ZZ" + "')) ");
				c = builder.query(banco.db, colunasUm, (strWhere.trim().isEmpty()) ? null : strWhere.replace("WHERE", "").trim(), new String[] {}, null, null, campoOrder);

				adapter = new frm_cons_produtos_adapter(this, R.layout.lay_cons_produtos, c, banco, new String[] { "DESCRICAO", "PRODUTOID", "UND", "VALOR", "GRUPO", "ESTOQUE", "DESTAQUE" }, new int[] { R.id.txtDescricao, R.id.txtCodigo, R.id.txtUN, R.id.txtValor, R.id.txtGrupo, R.id.txtEstoque }, banco.cons_prod_indexTIPO, yIDDetalhar, PedidoID, produtosAdds, ListaID);
				ListAdapter lsadapter = adapter;
				listaCons.setAdapter(lsadapter);
				listaCons.setFastScrollEnabled(true);
				listaCons.setDrawSelectorOnTop(true);
				tipoListaTemp = banco.cons_prod_indexTIPO;
			}
		} else {

			if (adapter != null) {

				builder.setTables("PRODUTOS LEFT OUTER JOIN IMPOSTOS ON PRODUTOS.IMPOSTOID = IMPOSTOS.IMPOSTOID AND ((IMPOSTOS.UF = '" + UF + "') OR (IMPOSTOS.UF = '" + "ZZ" + "')) ");
				c = builder.query(banco.db, colunasZero, (strWhere.trim().isEmpty()) ? null : strWhere.replace("WHERE", "").trim(), new String[] {}, null, null, campoOrder);

				if (tipoListaTemp == banco.cons_prod_indexTIPO) {
					adapter.changeCursor(c);
				} else {
					adapter = new frm_cons_produtos_adapter(this, R.layout.lay_cons_produtos, c, banco, new String[] {}, new int[] {}, banco.cons_prod_indexTIPO, yIDDetalhar, PedidoID, produtosAdds, ListaID);
					ListAdapter lsadapter = adapter;
					listaCons.setAdapter(lsadapter);
					listaCons.setFastScrollEnabled(true);
					listaCons.setDrawSelectorOnTop(true);
					tipoListaTemp = banco.cons_prod_indexTIPO;
				}

			} else {
				// startManagingCursor(c);
				builder.setTables("PRODUTOS LEFT OUTER JOIN IMPOSTOS ON PRODUTOS.IMPOSTOID = IMPOSTOS.IMPOSTOID AND ((IMPOSTOS.UF = '" + UF + "') OR (IMPOSTOS.UF = '" + "ZZ" + "')) ");
				c = builder.query(banco.db, colunasZero, (strWhere.trim().isEmpty()) ? null : strWhere.replace("WHERE", "").trim(), new String[] {}, null, null, campoOrder);
				// c.isFirst();
				adapter = new frm_cons_produtos_adapter(this, R.layout.lay_cons_produtos, c, banco, new String[] {}, new int[] {}, banco.cons_prod_indexTIPO, yIDDetalhar, PedidoID, produtosAdds, ListaID);
				ListAdapter lsadapter = adapter;
				listaCons.setAdapter(lsadapter);
				listaCons.setFastScrollEnabled(true);
				listaCons.setDrawSelectorOnTop(true);
				tipoListaTemp = banco.cons_prod_indexTIPO;
			}
		}

		// caso pesquisa pelo inicio da descriçao e nao achar busca denovo em
		// qualquer parte de 'descricao' ou 'grupo'
		if ((adapter.getCursor().getCount() == 0) && ((!filtro.equals("")))) {
			if (pesquisaDesc == true) {
				pesquisaDesc = false;
				CarregaDados(false, 0);
				return;
			}
		}

		lblFiltro.setText("Registros: " + adapter.getCount());

		listaCons.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// @todo
				// view.setBackgroundColor(R.color.all_cinza);
				ultPosicao = position;
				ItemId = id;

				if (tipoListaEstoque == 0) {
					if (PedidoID > 0) {
						AddProdutoVenda();
					} else {
						op_Opcoes();
					}
				} else {
					addProdutoEstoque();
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

	private void addProdutoEstoque() {


		final Cursor cProduto = banco.db.rawQuery("SELECT DESCRICAO, UND, PRODUTOID,LINHAID,COLUNAID,UNIDADEID,ESTOQUE, CODIGO_BARRA FROM PRODUTOS WHERE PRODUTOS._id = " + String.valueOf(ItemId), null);
		cProduto.moveToFirst();

		Cursor cursorEstoque =  banco.db.rawQuery("SELECT _id, PRODUTOID, LINHAID, COLUNAID, UNIDADEID FROM estoque WHERE estoque.UNIDADEID = "+cProduto.getString(cProduto.getColumnIndex("UNIDADEID"))+" AND estoque.COLUNAID = "+cProduto.getString(cProduto.getColumnIndex("COLUNAID"))+" AND estoque.LINHAID = "+cProduto.getString(cProduto.getColumnIndex("LINHAID"))+" AND estoque.PRODUTOID = "+cProduto.getString(cProduto.getColumnIndex("PRODUTOID")), null);
		cursorEstoque.moveToFirst();

		if(cursorEstoque.getCount() != 0){
			AlertDialog.Builder builder = new AlertDialog.Builder(frm_cons_produtos.this);
			builder.setTitle("Coletor de dados").setMessage("Produto já adicionado na lista, deseja edita-lo?!").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					mudarProdutoEstoque(cProduto);
					dialog.dismiss();
				}
			}).setNegativeButton("Não", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			mudarProdutoEstoque(cProduto);
		}


	}

	private void mudarProdutoEstoque(Cursor cProduto){
		this.estoque = new Estoque();
		estoque.setProdutoId(cProduto.getInt(cProduto.getColumnIndex("PRODUTOID")));
		estoque.setLinhaId(cProduto.getInt(cProduto.getColumnIndex("LINHAID")));
		estoque.setColunaId(cProduto.getInt(cProduto.getColumnIndex("COLUNAID")));
		estoque.setUnidadeId(cProduto.getInt(cProduto.getColumnIndex("UNIDADEID")));
		estoque.setDescricao(cProduto.getString(cProduto.getColumnIndex("DESCRICAO")));
		estoque.setUnd(cProduto.getString(cProduto.getColumnIndex("UND")));
		estoque.setEstoque(Double.parseDouble(cProduto.getString(cProduto.getColumnIndex("ESTOQUE"))));
		estoque.setCodigoBarra(cProduto.getString(cProduto.getColumnIndex("CODIGO_BARRA")));

		aplication.setEstoqueProduto(estoque);

		finish();
	}

	private String monteSqlArray(String strWhere) {

		StringBuilder builder = new StringBuilder();
		builder.append("PRODUTOS.PRODUTOID in (");

		for (int i = 0; i < produtosAdds.size(); i++) {
			builder.append(produtosAdds.get(i).getProdutoId());
			if (i + 1 != produtosAdds.size()) {
				builder.append(", ");
			}
		}

		builder.append(") AND PRODUTOS.UNIDADEID in (");
		for (int i = 0; i < produtosAdds.size(); i++) {
			builder.append(produtosAdds.get(i).getUnidadeId());
			if (i + 1 != produtosAdds.size()) {
				builder.append(", ");
			}
		}

		builder.append(") AND PRODUTOS.LINHAID in (");
		for (int i = 0; i < produtosAdds.size(); i++) {
			builder.append(produtosAdds.get(i).getLinhaId());
			if (i + 1 != produtosAdds.size()) {
				builder.append(", ");
			}
		}
		builder.append(") AND PRODUTOS.COLUNAID in (");
		for (int i = 0; i < produtosAdds.size(); i++) {
			builder.append(produtosAdds.get(i).getColunaId());
			if (i + 1 != produtosAdds.size()) {
				builder.append(", ");
			}
		}
		builder.append(")");

		if (strWhere.trim().equals("WHERE")) {
			if (!builder.toString().trim().isEmpty()) {
				strWhere += " " + builder.toString();
			}
		} else {
			if (!builder.toString().trim().isEmpty()) {
				strWhere += " AND " + builder.toString();
			}
		}

		return strWhere;
	}

	private String monteNotSqlArray(String strWhere) {

		StringBuilder builder = new StringBuilder();
		builder.append("(PRODUTOS.PRODUTOID not in (");

		for (int i = 0; i < produtosAdds.size(); i++) {
			builder.append(produtosAdds.get(i).getProdutoId());
			if (i + 1 != produtosAdds.size()) {
				builder.append(", ");
			}
		}

		builder.append(") AND PRODUTOS.UNIDADEID not in (");
		for (int i = 0; i < produtosAdds.size(); i++) {
			builder.append(produtosAdds.get(i).getUnidadeId());
			if (i + 1 != produtosAdds.size()) {
				builder.append(", ");
			}
		}

		builder.append(") AND PRODUTOS.LINHAID not in (");
		for (int i = 0; i < produtosAdds.size(); i++) {
			builder.append(produtosAdds.get(i).getLinhaId());
			if (i + 1 != produtosAdds.size()) {
				builder.append(", ");
			}
		}
		builder.append(") AND PRODUTOS.COLUNAID not in (");
		for (int i = 0; i < produtosAdds.size(); i++) {
			builder.append(produtosAdds.get(i).getColunaId());
			if (i + 1 != produtosAdds.size()) {
				builder.append(", ");
			}
		}
		builder.append("))");

		if (strWhere.trim().equals("WHERE")) {
			if (!builder.toString().trim().isEmpty()) {
				strWhere += " " + builder.toString();
			}
		} else {
			if (!builder.toString().trim().isEmpty()) {
				strWhere += " AND " + builder.toString();
			}
		}

		return strWhere;
	}

	private void Produto_Sincroniza() {

		// DB_Sincroniza dbSync = new DB_Sincroniza(frm_cons_produtos.this);

		AlertDialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(frm_cons_produtos.this);
		builder.setTitle("SmartMobile - Sincronizar Produtos").setSingleChoiceItems(new String[]{"Cadastros", "Cadastros + Imagens"}, -1, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				DB_Sincroniza dbSync = new DB_Sincroniza(frm_cons_produtos.this);
				if (item == 0) {
					dbSync.Syncroniza_Produtos();
				} else if (item == 1) {
					dbSync.Syncroniza_Imagens();
				}
				dialog.dismiss();
			}
		});

		dialog = builder.create();
		dialog.show();

	};

	private void op_Opcoes() {

		CharSequence[] items = new CharSequence[] { "Filtrar", "Ver Imagens" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Produtos - Opções");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				if (item == 0) {

					// CarregaDados(false,Integer.valueOf(String.valueOf(ItemId)));
					op_Filtrar();

				} else if (item == 1) {
					Intent intent = new Intent(frm_cons_produtos.this, frm_full_image.class);
					Bundle params = new Bundle();

					Cursor cProduto = banco.db.rawQuery("SELECT PRODUTOID from PRODUTOS WHERE PRODUTOS._id = " + String.valueOf(ItemId), null);

					if (cProduto.moveToFirst()) {
						params.putString("idProduto", cProduto.getLong(0) + "");
					}
					intent.putExtras(params);
					startActivity(intent);
				}

			}
		});
		AlertDialog alert = builder.create();
		alert.show();

		/*
		 * AlertDialog.Builder builder = new AlertDialog.Builder(this);
		 * builder.setMultiChoiceItems(statesTexto, statesOpcoes, new
		 * DialogInterface.OnMultiChoiceClickListener() { public void
		 * onClick(DialogInterface dialog, int item, boolean check) {
		 * 
		 * if(item==0){ dialog.cancel(); Produto_Sincroniza(); }else{
		 * 
		 * if(statesTexto[item].equals("All Categories")) { AlertDialog d =
		 * (AlertDialog) dialog; ListView v = d.getListView(); int i = 0;
		 * while(i < statesTexto.length) { v.setItemChecked(i, check);
		 * statesOpcoes[i] = check; i++; } } statesOpcoes[item] = check;
		 * dialog.cancel(); CarregaDados(false); }
		 * 
		 * statesOpcoes[0] = false; } }).show();
		 */

	}

	private void op_Filtrar() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Produtos - Filtrar");
		final CharSequence[] choiceList =

		{ "Com Estoque", "Sem Estoque", "Todos os Produtos", "Produtos em Destaque" };

		builder.setSingleChoiceItems(choiceList, banco.cons_prod_indexFiltro,

		new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				if (which != 3) {
					banco.cons_prod_indexFiltro = which;
					banco.cons_prod_Destaque = false;
				} else {
					banco.cons_prod_Destaque = true;
				}
				dialog.cancel();
				CarregaDados(false, 0);

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

	private void AddProdutoVenda() {

		// Cursor cProduto = banco.Sql_Select("PRODUTOS",new
		// String[]{"PRODUTOID","DESCRICAO","UND","VALOR","DESC_MAX","LINHAID","COLUNAID","LINHA","COLUNA","UNIDADEID","ESTOQUE"},
		// "_id = " + String.valueOf(ItemId), "");
		final Cursor cProduto = banco.db.rawQuery("SELECT PRODUTOID,DESCRICAO,UND,VALOR,DESC_MAX,LINHAID,COLUNAID,LINHA,COLUNA,UNIDADEID,ESTOQUE,PRODUTOS.ALIQUOTA_IPI,PRODUTOS.IMPOSTOID,'" + UF + "' AS UF,IMPOSTOS.ALIQUOTA_UF,IMPOSTOS.SUBS_ALIQ,IMPOSTOS.SUBS_IVA, PRODUTOS.PESO FROM PRODUTOS LEFT OUTER JOIN IMPOSTOS ON PRODUTOS.IMPOSTOID = IMPOSTOS.IMPOSTOID AND ((IMPOSTOS.UF = '" + UF + "') OR (IMPOSTOS.UF = '" + "ZZ" + "')) WHERE PRODUTOS._id = " + String.valueOf(ItemId), null);
		if (cProduto.moveToFirst()) {

			if (cProduto.getInt(cProduto.getColumnIndex("ESTOQUE")) <= 0) {

				Cursor cProdutoEstoque = banco.db.rawQuery("SELECT SMARTMOBILE_VALIDA_ESTOQUE FROM CONFIG_DINAMICA", null);
				cProdutoEstoque.moveToFirst();

				if (cProdutoEstoque.getInt(0) == 0) {

					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("Produto com estoque zerado!\nContinuar assim mesmo?").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							adicionarProduto(cProduto);
						}
					}).setNegativeButton("Não", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("Conf. Empresa: Não é permitido incluir produtos com estoque zerado!").setTitle("Configuração da Empresa").setCancelable(false).setNegativeButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
				}
			} else {

				adicionarProduto(cProduto);
			}
		}

	}

	private void adicionarProduto(Cursor cProduto) {
		// verifica se o produto já está incluso no pedido
		Cursor pedProd = banco.Sql_Select("VENDAS_ITENS", new String[] { "PRODUTOID" }, "VENDAID = " + String.valueOf(PedidoID) + " AND PRODUTOID = " + cProduto.getLong(0) + " AND LINHAID = " + cProduto.getLong(5) + " AND COLUNAID = " + cProduto.getLong(6), null);
		if (pedProd.moveToFirst()) {
			// if(c.getLong(c.getColumnIndex("QTDE_PED"))>0){
			banco.MostraMsg(this, "Produto já existe no pedido !!!");
		}
		// tocari não permite vender produtos 'sem estoque(estoque - vendas
		// em aberto)'
		else if (banco.Banco.toUpperCase().trim().equals("TOCARI") && (cProduto.getLong(10) <= 0.00)) {

			banco.MostraMsg(this, "Produto não possui Estoque Suficiente !!!");
		} else {

			// do {
			Bundle b = new Bundle();
			b.putBoolean("incluindo", true);
			b.putLong("pedidoid", PedidoID);
			b.putLong("itemid", 0);

			b.putLong("produtoid", cProduto.getLong(0));
			b.putString("descricao", cProduto.getString(1));
			b.putLong("linhaid", cProduto.getLong(5));
			b.putLong("colunaid", cProduto.getLong(6));
			b.putString("linha", cProduto.getString(7));
			b.putString("coluna", cProduto.getString(8));
			b.putLong("unidadeid", cProduto.getLong(9));
			b.putDouble("estoque", cProduto.getLong(10));

			b.putString("und", cProduto.getString(2));
			b.putDouble("qtde", 1.00);
			b.putDouble("valor", cProduto.getDouble(3));
			b.putDouble("valor_cad", cProduto.getDouble(3));
			b.putDouble("desc_max", cProduto.getDouble(4));
			b.putDouble("peso", cProduto.getDouble(cProduto.getColumnIndex("PESO")));

			// caso tenha imposto de IPI
			if (c.getString(c.getColumnIndex("ALIQUOTA_IPI")) != null) {
				b.putDouble("percentual_ipi", cProduto.getDouble(cProduto.getColumnIndex("ALIQUOTA_IPI")));
			} else {
				b.putDouble("percentual_ipi", 0.00);
			}

			// caso tenha imposto de SUBs TRIBUTARIA
			if (c.getString(c.getColumnIndex("ALIQUOTA_UF")) != null) {

				Log.i("JAKSON 1", "");

				Double ValorIPI = ((cProduto.getDouble(cProduto.getColumnIndex("VALOR")) * cProduto.getDouble(cProduto.getColumnIndex("ALIQUOTA_IPI"))) / 100);
				Double ValorICMS = ((cProduto.getDouble(cProduto.getColumnIndex("VALOR")) * cProduto.getDouble(cProduto.getColumnIndex("ALIQUOTA_UF"))) / 100);

				Double BaseIcms = cProduto.getDouble(cProduto.getColumnIndex("VALOR"));
				Double BaseIcmsSub = (BaseIcms + ValorIPI) + (((BaseIcms + ValorIPI) * cProduto.getDouble(cProduto.getColumnIndex("SUBS_IVA"))) / 100);
				Double ValorIcmsSub = ((BaseIcmsSub * cProduto.getDouble(cProduto.getColumnIndex("SUBS_ALIQ"))) / 100) - ValorICMS;

				// envia para a tela de adicionar produto ao pedido
				Log.i("VALOR ST", String.valueOf(cProduto.getDouble(cProduto.getColumnIndex("VALOR")) + ValorIcmsSub));
				Log.i("PERCENTUAL ST", String.valueOf((ValorIcmsSub * 100) / cProduto.getDouble(cProduto.getColumnIndex("VALOR"))));

				b.putDouble("percentual_st", (ValorIcmsSub * 100) / cProduto.getDouble(cProduto.getColumnIndex("VALOR")));

			} else {

				b.putDouble("percentual_st", 0.00);
			}



			Log.i("JAKSON 2", "");

			Intent intent = new Intent(frm_cons_produtos.this, frm_cad_pedido_item.class);
			intent.putExtras(b);
			startActivity(intent);

			// moveTaskToBack(true);
			// } while (cli0.moveToNext());
		}
	}

	private void updateWhereBanco(final String valor) {
		Handler handler = new Handler();
		handler.post(new Runnable() {

			@Override
			public void run() {
				SQLiteStatement insertStmt = banco.db.compileStatement("UPDATE CONFIG_DINAMICA SET CONSULTA_PRODUTO_WHERE = ? WHERE _id = 1");
				insertStmt.bindString(1, valor);
				insertStmt.executeInsert();
			}
		});

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("filtro", txtFiltro.getText().toString());
		super.onSaveInstanceState(outState);
	}
}