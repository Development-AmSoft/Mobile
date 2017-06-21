package smart.mobile.outras.sincronismo;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import smart.mobile.R;
import smart.mobile.consulta.produtos.frm_cons_produtos;
import smart.mobile.outras.sincronismo.download.DB_Sincroniza_Novo;
import smart.mobile.outras.tela.config.frm_sys_config;
import smart.mobile.outras.tela.principal.frm_sys_principal;

public class DB_LocalHost {

	// INFORMAÇÔES IMPORTANTES //
	/*
	 * 1) SEMPRE RETORNAR O _id NOS CURSORES PARA O ANDROID MAPEAR 2)
	 */
	public static DecimalFormat myCustDecFormatter = new DecimalFormat("#,##0.00");

	public static String ServidorOnline = "";
	public static String Banco = "";
	public static String EmpresaID = "";
	public static String NomeEmpresa = "";
	public static String VendedorID = "";
	public static String NomeVendedor = "";
	public static boolean VendedorGerente = false;

	public static String QtdeProduto = "1";
	public static String IDsProdutosPed = "";

	public static boolean temp_cSaldoFlex = false; // controla saldo flex ?
	public static Double temp_SaldoFlex = 0.00; // saldo disponivel

	public static int cons_ped_indexTIPO = 1;
	public static int cons_cli_indexTIPO = 1;
	public static int cons_prod_indexTIPO = 1;

	public static int cons_ped_indexFiltro = 0;
	public static int cons_cli_indexFiltro = 2;
	public static int cons_prod_indexFiltro = 2;
	public static int cons_titulos_indexFiltro = 2;
	public boolean cons_prod_Destaque = false;
	public boolean cons_prod_Add = false;
	public boolean cons_prod_JaComprado = false;

	private static final String DATABASE_NAME = "SmartMobile.db";
	private static final int DATABASE_VERSION = 24;

	public static final String[] TB_CLIENTES_CAMPOS = new String[] { "NOME", "FANTASIA", "CPF_CNPJ", "INSC_EST", "CIDADE", "ENDERECO", "NUMERO", "BAIRRO", "CEP", "TELEFONE", "CELULAR", "EMAIL", "OBS", "LIMITE", "LISTAID", "FORMA_PGTOID", "SINCRONIZADO", "ULT_DATA", "ULT_TOTAL", "COMPLEMENTO", "RESPONSAVEL" };

	// 1 - inicialização da classe

	public DB_LocalHost(Context context) {

		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();

		// this.insertStmt = this.db.compileStatement(INSERT);

	}

	public void closeHelper() {
		if (db != null) {
			db.close();
		}
	}

	// 2 - procidimento de criação e atualização do banco de dados

	private static class OpenHelper extends SQLiteOpenHelper {

		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/*
		 * O “id” tem um “_” (undescore) antes, assim o Android automaticamente
		 * amarra o ID do banco com o ID utilizado quando se seleciona um
		 * elemento da lista. Suponha que a turma “1a.A” tenha ID 10 no banco de
		 * dados, se alguém clicar neste item, ou executar um Long Press,
		 * eventos específicos de ListActivity serão executados, no caso de um
		 * click, será o evento associado ao ClickListener setado para a lista
		 * da tela.
		 * 
		 * /** O código abaixo recupera o ListView e seta o ClickListener
		 * 
		 * this.getListView().setOnItemClickListener( new OnItemClickListener()
		 * { public void onItemClick(AdapterView<?> context, View view,int
		 * position, long id) { //Mostra uma mensagem com o valor de ID
		 * Toast.makeText(TurmaTela.this,"O id do item selecionado é: "+
		 * String.valueOf(id), Toast.LENGTH_LONG).show(); } } );
		 */

		@Override
		public void onCreate(SQLiteDatabase db) {

			try {
				// db.execSQL("CREATE TABLE " + TABLE_NAME +
				// "(id INTEGER PRIMARY KEY, name TEXT, count INTEGER)");
				// ((frm_sys_principal)context).CriarAtalho();

				Log.i("CREATE_TABLE", "CONFIG");

				db.execSQL(DB_Sincroniza_Novo.config);

				Log.i("CREATE_TABLE", "CONFIG_DINAMICA");

				db.execSQL(DB_Sincroniza_Novo.configDinamica);

				Log.i("CREATE_TABLE", "EMPRESAS");

				db.execSQL(DB_Sincroniza_Novo.empresas);

				Log.i("CREATE_TABLE", "VENDEDORES");

				db.execSQL(DB_Sincroniza_Novo.vendedores);

				Log.i("CREATE_TABLE", "FLEX");

				db.execSQL(DB_Sincroniza_Novo.flex);

				Log.i("CREATE_TABLE", "METAS");

				db.execSQL(DB_Sincroniza_Novo.metas);

				Log.i("CREATE_TABLE", "FORMAS_PGTO");

				db.execSQL(DB_Sincroniza_Novo.formasPagto);

				Log.i("CREATE_TABLE", "LISTAS_PRECOS");

				db.execSQL(DB_Sincroniza_Novo.listaPreco);

				Log.i("CREATE_TABLE", "LISTAS_PRECOS_PRODUTOS");

				db.execSQL(DB_Sincroniza_Novo.listPrecoProdutos);

				Log.i("CREATE_TABLE", "CLIENTES");

				db.execSQL(DB_Sincroniza_Novo.clientes);

				Log.i("CREATE_TABLE", "CLIENTES_HISTORICO");

				db.execSQL(DB_Sincroniza_Novo.clientesHistorico);

				Log.i("CREATE_TABLE", "CLIENTES_FORMAS_PGTO");

				db.execSQL(DB_Sincroniza_Novo.clientesFormaPagamento);

				Log.i("CREATE_TABLE", "TITULOS");

				db.execSQL(DB_Sincroniza_Novo.titulos);

				Log.i("CREATE_TABLE", "PRODUTOS");

				db.execSQL(DB_Sincroniza_Novo.produtos);

				Log.i("CREATE_TABLE", "IMPOSTOS");

				db.execSQL(DB_Sincroniza_Novo.impostos);

				Log.i("CREATE_TABLE", "VENDAS");

				db.execSQL(DB_Sincroniza_Novo.vendas);

				Log.i("CREATE_TABLE", "VENDAS_ITENS");

				db.execSQL(DB_Sincroniza_Novo.vendasItens);

				// CRIA ATALHO PARA O SISTEMA
				((frm_sys_principal) context).CriarAtalho();

				// PEDE SE USUARIO DESEJA CONFIGURAR VERSAO DE DEMONTRACAO
				Intent intent = new Intent(((frm_sys_principal) context), frm_sys_config.class);
				((frm_sys_principal) context).startActivity(intent);

			} catch (Exception e) {
				Log.i("SQLException CreateDataBase :", e.getMessage());
			}

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

			Log.w("banco Database", "Upgrading database, this will destroy the database and recreate.");

			// db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS CONFIG");
			db.execSQL("DROP TABLE IF EXISTS EMPRESAS");
			db.execSQL("DROP TABLE IF EXISTS VENDEDORES");

			db.execSQL("DROP TABLE IF EXISTS FLEX");
			db.execSQL("DROP TABLE IF EXISTS METAS");
			db.execSQL("DROP TABLE IF EXISTS FORMAS_PGTO");
			db.execSQL("DROP TABLE IF EXISTS LISTAS_PRECOS");
			db.execSQL("DROP TABLE IF EXISTS LISTAS_PRECOS_PRODUTOS");

			db.execSQL("DROP TABLE IF EXISTS CLIENTES");
			db.execSQL("DROP TABLE IF EXISTS CLIENTES_HISTORICO");
			db.execSQL("DROP TABLE IF EXISTS CLIENTES_FORMAS_PGTO");

			db.execSQL("DROP TABLE IF EXISTS TITULOS");
			db.execSQL("DROP TABLE IF EXISTS PRODUTOS");
			db.execSQL("DROP TABLE IF EXISTS IMPOSTOS");

			db.execSQL("DROP TABLE IF EXISTS VENDAS");
			db.execSQL("DROP TABLE IF EXISTS VENDAS_ITENS");

			onCreate(db);

		}
	}

	// 3 - procedimentos para armazenar ultima qtde inclusa e também produtos
	// inclusos no pedido

	public void TempPed_SetQtdePRodVenda(String qtde) {
		QtdeProduto = qtde;
	}

	public boolean TempPed_ExisteProdutosID(String ProdutoID, String LinhaID, String ColunaID) {

		if (IDsProdutosPed.indexOf("<p>" + ProdutoID + ":" + LinhaID + ":" + ColunaID + "</p>") != -1) {
			return true;
		} else {
			return false;
		}

	}

	public void TempPed_AddProdutosID(String ProdutoID, String LinhaID, String ColunaID) {

		IDsProdutosPed = IDsProdutosPed + "<p>" + ProdutoID + ":" + LinhaID + ":" + ColunaID + "</p>";
	}

	public void TempPed_DelPRodutosID(String ProdutoID, String LinhaID, String ColunaID) {

		IDsProdutosPed = IDsProdutosPed.replace("<p>" + ProdutoID + ":" + LinhaID + ":" + ColunaID + "</p>", "");

	}

	public void TempPed_ClearPRodutosID() {

		IDsProdutosPed = "";

	}

	private void InsereDadosTeste() {

	}

	public void MostraErro(String Mensagem) {

		AlertDialog ad = new AlertDialog.Builder(context).create();
		ad.setCancelable(false); // This blocks the 'BACK' button
		ad.setTitle("Mensagem do Aparelho");
		ad.setMessage(Mensagem);
		ad.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		ad.show();

	}

	// carrega o saldo flex disponivel
	public void DB_SaldoFlexLoad() {

		Cursor cAtual = db.query("FLEX", new String[] { "SALDO" }, null, null, null, null, "_id desc");
		if (cAtual.moveToFirst()) {
			// do {
			temp_cSaldoFlex = true;
			temp_SaldoFlex = cAtual.getDouble(cAtual.getColumnIndex("SALDO"));// +
																				// Acrescimo
																				// -
																				// Desconto;
			// } while (cAtual.moveToNext());
		} else {
			temp_cSaldoFlex = false;
			temp_SaldoFlex = 0.00;
		}

		if (cAtual != null || !cAtual.isClosed()) {
			cAtual.close();
		}

	}

	// carega as configurações
	public void DB_ConfigLoad() {

		// carrega do banco de dados
		Cursor cur = Sql_Select("CONFIG", new String[] { "SERVIDOR", "BANCO", "EMPRESAID", "EMPRESA", "VENDEDORID", "VENDEDOR" }, null, null);
		// Cursor cur =
		// db.rawQuery("select config.SERVIDOR, config.BANCO, config.EMPRESAID , config.EMPRESA, config.VENDEDORID, config.VENDEDOR , vendedores.GERENTE from config join vendedores on config.vendedorid = vendedores.vendedorid",
		// null);
		if (cur.moveToFirst()) {
			do {
				ServidorOnline = cur.getString(0);
				Banco = cur.getString(1);
				EmpresaID = cur.getString(2);
				NomeEmpresa = cur.getString(3);
				VendedorID = cur.getString(4);
				NomeVendedor = cur.getString(5);
				VendedorGerente = false;// (cur.getInt(6)==1);

				// caso ja tenha sido configurado o vendedor verificamos se ele
				// é gerente
				if ((!VendedorID.equals("")) && (!VendedorID.equals("0"))) {

					Cursor curVendedor = Sql_Select("VENDEDORES", new String[] { "GERENTE" }, "VENDEDORID = " + VendedorID, null);
					if (curVendedor.moveToFirst()) {
						VendedorGerente = (curVendedor.getInt(0) == 1);
					}
					if (curVendedor != null || !curVendedor.isClosed()) {
						curVendedor.close();
					}
				}

			} while (cur.moveToNext());
		}
		if (cur != null || !cur.isClosed()) {
			cur.close();
		}

		// caso não tiver cadastrado nada configura o default
		if (ServidorOnline == "") {

			this.insertStmt = this.db.compileStatement("insert into config (servidor,banco,empresaid,empresa,vendedorid,vendedor) values (?,?,?,?,?,?)");
			this.insertStmt.bindString(1, "");
			this.insertStmt.bindString(2, "");
			this.insertStmt.bindString(3, "0");
			this.insertStmt.bindString(4, "Indefinida");
			this.insertStmt.bindString(5, "0");
			this.insertStmt.bindString(6, "Indefinido");
			this.insertStmt.executeInsert();

			this.insertStmt = this.db.compileStatement("insert into empresas (empresa) values (?)");
			this.insertStmt.bindString(1, "0 - Indefinida");
			this.insertStmt.executeInsert();
			try {
				this.insertStmt = this.db.compileStatement("insert into vendedores (vendedorid,vendedor,gerente, coletor) values (?,?,?,?)");
				this.insertStmt.bindString(1, "0");
				this.insertStmt.bindString(2, "0 - Indefinido");
				this.insertStmt.bindString(3, "0");
				this.insertStmt.bindString(4, "0");
				this.insertStmt.executeInsert();
			} catch (Exception e) {
				this.insertStmt = this.db.compileStatement("insert into vendedores (vendedorid,vendedor,gerente) values (?,?,?)");
				this.insertStmt.bindString(1, "0");
				this.insertStmt.bindString(2, "0 - Indefinido");
				this.insertStmt.bindString(3, "0");
				this.insertStmt.executeInsert();
			}

			// carrega novamente
			DB_ConfigLoad();

		}

	}

	// salva as configurações
	public void DB_ConfigSave1(String ServerOnline, String Banco, String CodEmpresa, String NomeEmpresa, String CodVendedor, String NomeVendedor) {

		ContentValues dataToInsert = new ContentValues();
		dataToInsert.put("SERVIDOR", ServerOnline.replaceAll(" ", "").replaceAll("\n", ""));
		dataToInsert.put("BANCO", Banco.replaceAll(" ", "").replaceAll("\n", ""));
		dataToInsert.put("EMPRESAID", CodEmpresa);
		dataToInsert.put("EMPRESA", NomeEmpresa);
		dataToInsert.put("VENDEDORID", CodVendedor);
		dataToInsert.put("VENDEDOR", NomeVendedor);

		db.update("CONFIG", dataToInsert, null, null);

		// carrega novamente
		DB_ConfigLoad();

	}

	// retorna um cursor de dados
	public Cursor Sql_Select(String TABELA, String[] CAMPOS, String WHERE, String ORDER) {

		Cursor retorno = null;

		try {

			retorno = this.db.query(TABELA, CAMPOS, WHERE, null, null, null, ORDER);

		} catch (Exception e) {
			MostraErro(e.getMessage());
		}

		return retorno;

	}

	// retorna um cursor de dados
	public Cursor Sql_SelectErro(String TABELA, String[] CAMPOS, String WHERE, String ORDER) throws Exception {

		Cursor retorno = null;

		retorno = this.db.query(TABELA, CAMPOS, WHERE, null, null, null, ORDER);

		return retorno;

	}

	// retorna uma lista simples de um campo apenas
	public List Sql_Select(String TABELA, String CAMPO, String ORDER) {
		List list = new ArrayList();
		Cursor cursor = this.db.query(TABELA, new String[] { CAMPO }, null, null, null, null, ORDER);
		if (cursor.moveToFirst()) {
			do {
				list.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}

	public List Sql_SelectIncas(String TABELA, String CAMPO, String WHERE, String ORDER) {
		List list = new ArrayList();
		Cursor cursor = this.db.query(TABELA, new String[] { CAMPO }, WHERE, null, null, null, ORDER);
		if (cursor.moveToFirst()) {
			do {
				list.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}

	public List Sql_Select2(String selectX) {

		List list = new ArrayList();
		Cursor cursor = this.db.rawQuery(selectX, null);
		if (cursor.moveToFirst()) {
			do {
				list.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}

	// Metodo que insere um registro no banco. Se voce reparar voce nao usa sql
	// no codigo. Mas em qualquer caso, voce ainda pode usar.
	/*
	 * public void insertDB(){ SQLiteDatabase db = cD.getWritableDatabase();
	 * ContentValues values = new ContentValues();
	 * //values.put(DBEstrutura.ID,1); values.put(DBEstrutura.PREFIX,"CSCI");
	 * values.put(DBEstrutura.NUMBER,153); db.insert(DBEstrutura.TABLE_NAME,
	 * null, values); db.close(); }
	 * 
	 * 
	 * //metodo que atualiza um registro no banco public void updateDB(){
	 * SQLiteDatabase db = cD.getWritableDatabase(); String where =
	 * "\"NUMBER\"=\"153\""; ContentValues values = new ContentValues();
	 * values.put(DBEstrutura.NUMBER,151); db.update(DBEstrutura.TABLE_NAME,
	 * values, where, null); db.close();
	 * 
	 * }
	 */

	private void ShowURL() {

	}

	public void TestarConexao(final Context tela) {

		AlertDialog ad = new AlertDialog.Builder(tela).create();
		// ad.setCancelable(false); // This blocks the 'BACK' button
		ad.setTitle("SmartMobile");
		ad.setIcon(R.drawable.conexao_pop);
		ad.setMessage("O sistema irá testar a conexão com o seu servidor do SmartTools!\n\nCaso a página não esteja carregando, você deverá verificar as configurações da internet de seu aparelho.");
		ad.setButton("Iniciar Teste", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				String url = "";
				Intent i = new Intent(Intent.ACTION_VIEW);

				if (ServidorOnline.indexOf(":") <= 0) {
					url = "http://" + ServidorOnline + ":8080";
				} else {
					url = "http://" + ServidorOnline;
				}

				i.setData(Uri.parse(url));
				tela.startActivity(i);

			}
		});
		ad.show();

	}

	public void MostraMsg(Context tela, String Mensagem) {

		AlertDialog ad = new AlertDialog.Builder(tela).create();
		ad.setCancelable(false); // This blocks the 'BACK' button
		ad.setTitle("SmartMobile");
		ad.setIcon(R.drawable.ico_info);
		ad.setMessage(Mensagem);
		ad.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		ad.show();

	}

	public void DB_CLEAR(Boolean Implantar) {

		if (!Implantar) {
			this.db.delete("EMPRESAS", null, null);
			this.db.delete("VENDEDORES", null, null);
		}

		this.db.delete("FLEX", null, null);
		this.db.delete("METAS", null, null);
		this.db.delete("FORMAS_PGTO", null, null);
		this.db.delete("LISTAS_PRECOS", null, null);
		this.db.delete("LISTAS_PRECOS_PRODUTOS", null, null);

		this.db.delete("CLIENTES", null, null);
		this.db.delete("CLIENTES_HISTORICO", null, null);
		this.db.delete("CLIENTES_FORMAS_PGTO", null, null);

		this.db.delete("TITULOS", null, null);
		this.db.delete("PRODUTOS", null, null);
		this.db.delete("VENDAS", null, null);
		this.db.delete("VENDAS_ITENS", null, null);

	}

	public long DB_EMPRESA_INSERE(String cod, String name) {
		this.insertStmt = this.db.compileStatement("insert into empresas (empresa) values (?)");
		this.insertStmt.bindString(1, cod + " - " + name);
		return this.insertStmt.executeInsert();
	}

	public long DB_VENDEDOR_INSERE(String cod, String name, String gerente, String coletor) {
		try {
			this.insertStmt = this.db.compileStatement("insert into vendedores (vendedorid,vendedor,gerente, coletor) values (?,?,?,?)");
			this.insertStmt.bindString(1, cod);
			this.insertStmt.bindString(2, cod + " - " + name);
			this.insertStmt.bindString(3, gerente);
			this.insertStmt.bindLong(4, Long.parseLong(coletor));
		} catch (Exception e) {
			this.insertStmt = this.db.compileStatement("insert into vendedores (vendedorid,vendedor,gerente) values (?,?,?)");
			this.insertStmt.bindString(1, cod);
			this.insertStmt.bindString(2, cod + " - " + name);
			this.insertStmt.bindString(3, gerente);
		}
		return this.insertStmt.executeInsert();
	}

	public long TB_META_INSERE(Long Mes, Double Meta, Double Total) {
		this.insertStmt = this.db.compileStatement("insert into metas (mes,meta,total) values (?,?,?)");
		this.insertStmt.bindLong(1, Mes);
		this.insertStmt.bindDouble(2, Meta);
		this.insertStmt.bindDouble(3, Total);
		return this.insertStmt.executeInsert();
	}

	public long TB_SALDO_INSERE(String Data, String Referencia, Double FlexAcrescimo, Double FlexDesconto, Double Saldo, int Sincronizado, SQLiteDatabase db) {

		try {
			// so vai gerando caso utilize mesmo o controle
			if (temp_cSaldoFlex) {

				// caso o saldo for zerado(inserindo do aparelho) busca o ultimo
				// e
				// recalcula
				if (Saldo <= 0.00) {

					Cursor cAtual = db.query("FLEX", new String[] { "SALDO" }, null, null, null, null, "_id desc");
					if (cAtual.moveToFirst()) {
						// do {
						Saldo = cAtual.getDouble(cAtual.getColumnIndex("SALDO")) + FlexAcrescimo - FlexDesconto;
						temp_SaldoFlex = Saldo;
						// } while (cAtual.moveToNext());
					}
					if (cAtual != null || !cAtual.isClosed()) {
						cAtual.close();
					}
				}

				// insere o registro de saldo
				this.insertStmt = db.compileStatement("insert into flex (data,referencia,acrescimo,desconto,saldo,sincronizado) values (?,?,?,?,?,?)");
				this.insertStmt.bindString(1, Data);
				this.insertStmt.bindString(2, Referencia);
				this.insertStmt.bindDouble(3, FlexAcrescimo);
				this.insertStmt.bindDouble(4, FlexDesconto);
				this.insertStmt.bindDouble(5, Saldo);
				this.insertStmt.bindLong(6, Long.valueOf(String.valueOf(Sincronizado)));

				return this.insertStmt.executeInsert();
			} else {
				return 0;
			}
		} catch (NullPointerException e) {
			// so vai gerando caso utilize mesmo o controle
			if (temp_cSaldoFlex) {

				// caso o saldo for zerado(inserindo do aparelho) busca o ultimo
				// e
				// recalcula
				if (Saldo <= 0.00) {

					Cursor cAtual = this.db.query("FLEX", new String[] { "SALDO" }, null, null, null, null, "_id desc");
					if (cAtual.moveToFirst()) {
						// do {
						Saldo = cAtual.getDouble(cAtual.getColumnIndex("SALDO")) + FlexAcrescimo - FlexDesconto;
						temp_SaldoFlex = Saldo;
						// } while (cAtual.moveToNext());
					}
					if (cAtual != null || !cAtual.isClosed()) {
						cAtual.close();
					}
				}

				// insere o registro de saldo
				this.insertStmt = this.db.compileStatement("insert into flex (data,referencia,acrescimo,desconto,saldo,sincronizado) values (?,?,?,?,?,?)");
				this.insertStmt.bindString(1, Data);
				this.insertStmt.bindString(2, Referencia);
				this.insertStmt.bindDouble(3, FlexAcrescimo);
				this.insertStmt.bindDouble(4, FlexDesconto);
				this.insertStmt.bindDouble(5, Saldo);
				this.insertStmt.bindLong(6, Long.valueOf(String.valueOf(Sincronizado)));

				return this.insertStmt.executeInsert();
			} else {
				return 0;
			}
		}

	}

	public long TB_TITULOS_INSERE(String TIPO, String NOME, String ID, String DOCUMENTO, String EMISSAO, String VENCIMENTO, String VALOR, String HISTORICO) {
		this.insertStmt = this.db.compileStatement("insert into titulos (tipo,nome,codigo,documento,emissao,vencimento,valor,historico) values (?,?,?,?,?,?,?,?)");
		this.insertStmt.bindString(1, TIPO);
		this.insertStmt.bindString(2, NOME);
		this.insertStmt.bindString(3, ID);
		this.insertStmt.bindString(4, DOCUMENTO);
		this.insertStmt.bindString(5, EMISSAO);
		this.insertStmt.bindString(6, VENCIMENTO);
		this.insertStmt.bindString(7, VALOR);
		this.insertStmt.bindString(8, HISTORICO);
		return this.insertStmt.executeInsert();
	}

	public long TB_CLIENTE_HISTORICO_INSERE(String ClienteID, Long ProdutoID, String Qtde, String Valor, String Acrescimo, String Desconto, Long LinhaID, Long ColunaID, Long UnidadeID) {
		this.insertStmt = this.db.compileStatement("insert into CLIENTES_HISTORICO (CPF_CNPJ,PRODUTOID,QTDE,VALOR,ACRESCIMO,DESCONTO,LINHAID,COLUNAID,UNIDADEID) values (?,?,?,?,?,?,?,?,?)");
		this.insertStmt.bindString(1, ClienteID);
		this.insertStmt.bindLong(2, ProdutoID);
		this.insertStmt.bindString(3, Qtde);
		this.insertStmt.bindString(4, Valor);
		this.insertStmt.bindString(5, Acrescimo);
		this.insertStmt.bindString(6, Desconto);
		this.insertStmt.bindLong(7, LinhaID);
		this.insertStmt.bindLong(8, ColunaID);
		this.insertStmt.bindLong(9, UnidadeID);
		return this.insertStmt.executeInsert();
	}

	public void VERIFICA_DEL_PEDIDOZERO() {

		// verifica se o ultimo pedido esta sem itens e deleta o mesmo
		Cursor cPed = db.rawQuery("select max(_id) from vendas", null);
		if (cPed.moveToFirst()) {

			Cursor cItens = db.rawQuery("select produtoid from vendas_itens where vendaid = " + cPed.getString(0), null);
			if (!cItens.moveToFirst()) {

				db.execSQL("delete from vendas where _id = " + cPed.getString(0));
			}
		}
		if (cPed != null || !cPed.isClosed()) {
			cPed.close();
		}

	}

	public long Produto_Insere(int ProdutoID, String Descricao, String Grupo, String UnidadeID, String UN, double DescMax, double Valor, double Estoque, int LinhaID, int ColunaID, String Linha, String Coluna, int ImpostoID, double AliquotaIPI) {

		long retorno = 0;
		try {
			this.insertStmt = this.db.compileStatement("insert into produtos (produtoid,descricao,grupo,unidadeid,und,desc_max,valor,estoque,linhaid,colunaid,linha,coluna,impostoid,aliquota_ipi) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			this.insertStmt.bindLong(1, ProdutoID);
			this.insertStmt.bindString(2, Descricao);
			this.insertStmt.bindString(3, Grupo);
			this.insertStmt.bindString(4, UnidadeID);
			this.insertStmt.bindString(5, UN);
			this.insertStmt.bindDouble(6, DescMax);
			this.insertStmt.bindDouble(7, Valor);
			this.insertStmt.bindDouble(8, Estoque);
			this.insertStmt.bindLong(9, LinhaID);
			this.insertStmt.bindLong(10, ColunaID);
			this.insertStmt.bindString(11, Linha.trim());
			this.insertStmt.bindString(12, Coluna.trim());
			this.insertStmt.bindLong(13, ImpostoID);
			this.insertStmt.bindDouble(14, AliquotaIPI);
			retorno = this.insertStmt.executeInsert();
		} catch (Exception e) {
			Log.e("DB_LocalHost-Produto_Insere", e.getMessage());
			Toast.makeText((frm_cons_produtos) context, e.toString(), Toast.LENGTH_LONG).show();

		}
		return retorno;
	}

	// CLIENTES

	// PRODUTO

	// IMPOSTOS
	public void TB_IMPOSTOS_DELLALL() {
		this.db.delete("IMPOSTOS", null, null);
	}

	public void TB_IMPOSTOS_INSERT(int ImpostoID, String UF, double AliquotaUF, double SubsAliq, double SubsIVA) {

		try {
			ContentValues values = new ContentValues();
			values.put("IMPOSTOID", ImpostoID);
			values.put("UF", UF);
			values.put("ALIQUOTA_UF", AliquotaUF);
			values.put("SUBS_ALIQ", SubsAliq);
			values.put("SUBS_IVA", SubsIVA);
			db.insert("IMPOSTOS", "", values);

		} catch (Exception e) {
			throw new RuntimeException("SQL exception in openDatabase", e);

		}

	}

	// FORMAS DE PAGAMENTO
	public void TB_FORMASPGTO_DELLALL() {
		this.db.delete("FORMAS_PGTO", null, null);
	}

	public void TB_FORMASPGTO_INSERT(String Forma_PgtoID, String Descricao) {

		try {
			ContentValues values = new ContentValues();
			values.put("FORMA_PGTOID", Forma_PgtoID);
			values.put("DESCRICAO", Forma_PgtoID + " - " + Descricao);
			db.insert("FORMAS_PGTO", "", values);

		} catch (Exception e) {
			throw new RuntimeException("SQL exception in openDatabase", e);

		}

	}

	// LISTA DE PREÇOS
	public void TB_LISTASPRECOS_DELLALL() {
		this.db.delete("LISTAS_PRECOS", null, null);
	}

	public void TB_LISTASPRECOS_INSERT(String ListaID, String Descricao, String Tipo, String Percentual) {

		try {
			ContentValues values = new ContentValues();
			values.put("LISTAID", ListaID);
			values.put("DESCRICAO", ListaID + " - " + Descricao);
			values.put("TIPO_LISTA", Tipo);
			values.put("PERCENTUAL", Percentual);
			db.insert("LISTAS_PRECOS", "", values);

		} catch (Exception e) {
			throw new RuntimeException("SQL exception in openDatabase", e);

		}
	}

	// LISTA DE PREÇOS PRODUTOS
	public void TB_LISTASPRECOSPRODUTOS_DELLALL() {
		this.db.delete("LISTAS_PRECOS_PRODUTOS", null, null);
	}

	public void TB_LISTASPRECOSPRODUTOS_INSERT(String ListaID, String ProdutoiD, String Tipo, String Percentual) {

		try {
			ContentValues values = new ContentValues();
			values.put("LISTAID", ListaID);
			values.put("PRODUTOID", ProdutoiD);
			values.put("TIPO", Tipo);
			values.put("PERCENTUAL", Percentual);
			db.insert("LISTAS_PRECOS_PRODUTOS", "", values);

		} catch (Exception e) {
			throw new RuntimeException("SQL exception in openDatabase", e);

		}
	}

	public void TB_CLIENTES_FORMAS_PGTO_DELLALL() {
		this.db.delete("CLIENTES_FORMAS_PGTO", null, null);
	}

	public void TB_CLIENTES_FORMAS_PGTO_INSERT(String CpfCnpj, String FormaPadrao, String FormaPgtoID) {

		try {
			// busca o id do cliente pelo cpf/cnpj
			Cursor cursor = db.rawQuery("select _id from clientes where cpf_cnpj = '" + CpfCnpj + "'", null);
			if (cursor.moveToFirst()) {
				ContentValues values = new ContentValues();
				values.put("CLIENTEID", cursor.getDouble(0));
				values.put("FORMA_PGTOID", FormaPgtoID);
				db.insert("CLIENTES_FORMAS_PGTO", "", values);
			}

		} catch (Exception e) {
			throw new RuntimeException("SQL exception in openDatabase", e);

		}
	}

	private static Context context;
	public SQLiteDatabase db;

	private SQLiteStatement insertStmt;

	// private static final String INSERT = "insert into " + TABLE_NAME +
	// "(name) values (?)";

	public long insert(String name) {
		this.insertStmt.bindString(1, name);
		return this.insertStmt.executeInsert();
	}

	public void deleteAll() {
		// this.db.delete(TABLE_NAME, null, null);
	}

	public String select(String wrd) {
		/*
		 * String list = new String(); Cursor cursor = this.db.query(TABLE_NAME,
		 * new String[] {"name"},null, null, null, null, null); if
		 * (cursor.moveToFirst()) { do { list = cursor.getString(0); } while
		 * (cursor.moveToNext()); } if (cursor != null || !cursor.isClosed()) {
		 * cursor.close(); } return list;
		 */
		return "";
	}

	public Cursor SqlSelect() {

		Cursor cursorx = this.db.query("CLIENTES", new String[] { "nome" }, null, null, null, null, "id desc");
		return cursorx;
	}

	public void TB_CLIENTES_REMOVETODOS() {
		this.db.delete("CLIENTES", null, null);
	}

	public long TB_CLIENTES_INSERIR(String ClienteID, String Nome, String Fantasia, String CpfCNPJ, String InscEst, String Responsavel, String Cidade, String Endereco, String Numero, String Bairro, String Complemento, String CEP, String Telefone, String Celular, String Email, String Obs, String Limite, String FormaPgtoId, String ListaId, long Sync, int Ativo, String UltimoPedido, Double UltimoTotal) {
		long retorno = 0;
		try {
			if (ClienteID.isEmpty()) {
				this.insertStmt = this.db.compileStatement("insert into clientes (nome,fantasia,cpf_cnpj,insc_est,responsavel,cidade,endereco,numero,bairro,complemento,cep,telefone,celular,email,obs,limite,forma_pgtoid,listaid,sincronizado,ult_data,ult_total) " + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				Log.i("cliente-insert", Nome + "-" + CpfCNPJ);

			} else {
				this.insertStmt = this.db.compileStatement("update clientes set nome = ?,fantasia = ?,cpf_cnpj = ?,insc_est = ?,responsavel = ?,cidade = ?,endereco = ?,numero = ?,bairro = ?,complemento = ?,cep = ?,telefone = ?,celular = ?,email = ?, obs = ?,limite = ?,forma_pgtoid = ?,listaid = ?,sincronizado = ?,ult_data = ?, ult_total = ? where CPF_CNPJ like '" + String.valueOf(ClienteID) + "'");
				Log.i("cliente-update", Nome + "-" + CpfCNPJ);
			}

			this.insertStmt.bindString(1, Nome);
			this.insertStmt.bindString(2, Fantasia);
			this.insertStmt.bindString(3, CpfCNPJ);
			this.insertStmt.bindString(4, InscEst);
			this.insertStmt.bindString(5, Responsavel);
			this.insertStmt.bindString(6, Cidade);
			this.insertStmt.bindString(7, Endereco);
			this.insertStmt.bindString(8, Numero);
			this.insertStmt.bindString(9, Bairro);
			this.insertStmt.bindString(10, Complemento);
			this.insertStmt.bindString(11, CEP);
			this.insertStmt.bindString(12, Telefone);
			this.insertStmt.bindString(13, Celular);
			this.insertStmt.bindString(14, Email);
			this.insertStmt.bindString(15, Obs);
			this.insertStmt.bindString(16, Limite);
			this.insertStmt.bindString(17, FormaPgtoId);
			this.insertStmt.bindString(18, ListaId);

			if (Ativo == 0) {
				this.insertStmt.bindLong(19, 2); // cliente inativo
			} else {
				this.insertStmt.bindLong(19, Sync); // pendente ou enviado
			}

			this.insertStmt.bindString(20, UltimoPedido);
			this.insertStmt.bindDouble(21, UltimoTotal);

			// // coluna para ordenar a data
			// if (!UltimoPedido.equals(""))
			// {
			// this.insertStmt.bindLong(22,
			// Long.valueOf(UltimoPedido.substring(6, 10) +
			// UltimoPedido.substring(3, 5) + UltimoPedido.substring(0, 2)));
			// } else
			// {
			// this.insertStmt.bindLong(22, 0);
			// }

			retorno = this.insertStmt.executeInsert();

		} catch (Exception e) {
			Log.e("Insert-Cliente", Nome + " Erro : " + e.toString());
			Toast.makeText(context, "ERRO : " + e.toString(), Toast.LENGTH_LONG).show();
		}
		return retorno;
	}

	public long TB_VENDAS_INSERIR(long PedidoID, int Operacao, String ClienteCpfCnpj, int FormaPgtoId, int ListaId, Double Total, String Obs, int origem)

	{
		long retorno = 0;
		try {

			if (PedidoID == 0) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
				this.insertStmt = this.db.compileStatement("insert into vendas (operacao,CPF_CNPJ,data,forma_pgtoid,listaid,obs,sincronizado, sincronizar) " + " values (?,?,?,?,?,?,0,1)");
				this.insertStmt.bindLong(1, Operacao);
				this.insertStmt.bindString(2, ClienteCpfCnpj);
				this.insertStmt.bindString(3, sdf.format(new Date()));
				this.insertStmt.bindLong(4, FormaPgtoId);
				this.insertStmt.bindLong(5, ListaId);
				this.insertStmt.bindString(6, Obs);
				// this.insertStmt.bindLong(7, origem);
				// this.insertStmt.bindDouble(7, Total);
			} else {
				this.insertStmt = this.db.compileStatement("update vendas set operacao = ?,CPF_CNPJ = ?,forma_pgtoid = ?,listaid = ?,obs = ?,sincronizado = 0 where _id = " + String.valueOf(PedidoID));
				this.insertStmt.bindLong(1, Operacao);
				this.insertStmt.bindString(2, ClienteCpfCnpj);
				this.insertStmt.bindLong(3, FormaPgtoId);
				this.insertStmt.bindLong(4, ListaId);
				this.insertStmt.bindString(5, Obs);
				// this.insertStmt.bindLong(6, origem);
				// this.insertStmt.bindDouble(6, Total);
			}

			retorno = this.insertStmt.executeInsert();

		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "ERRO : " + e.toString(), Toast.LENGTH_LONG).show();

		}
		return retorno;
	}

	public long TB_VENDAS_ITENS_INSERIR(long PedidoID, long ItemID, int ProdutoID, int UnidadeID, int LinhaID, int ColunaID, String Produto_Descricao, Double Qtde, Double Acrescimo, Double Desconto, Double Valor, Double FlexAcrescimo, Double FlexDesconto, Double PercST, Double PercIPI, Double peso, SQLiteDatabase dbb) {
		double valortot = 0.00;
		long retorno = 0;
		SQLiteDatabase db;
		if (dbb != null) {
			db = dbb;
		} else {
			db = this.db;
		}
		try {

			if (ItemID > 0) {
				// remove item da venda
				Cursor cItem = db.rawQuery("select produtoid,flex_acrescimo,flex_desconto from vendas_itens where _id = " + String.valueOf(ItemID), null);
				if (cItem.moveToFirst()) {
					TB_VENDAS_ITENS_DELETAR(PedidoID, ItemID, cItem.getLong(0), cItem.getDouble(1), cItem.getDouble(2));
				}
				if (cItem != null || !cItem.isClosed()) {
					cItem.close();
				}
			}

			// sempre insere um novo item
			ItemID = 0;

			if (ItemID == 0) {
				// insere item na venda
				this.insertStmt = db.compileStatement("insert into vendas_itens (vendaid,produtoid,unidadeid,linhaid,colunaid,qtde,acrescimo,desconto,valor,flex_acrescimo,flex_desconto,valor_st, peso_total) " + " values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
				this.insertStmt.bindLong(1, PedidoID);
				this.insertStmt.bindLong(2, ProdutoID);
				this.insertStmt.bindLong(3, UnidadeID);
				this.insertStmt.bindLong(4, LinhaID);
				this.insertStmt.bindLong(5, ColunaID);
				this.insertStmt.bindDouble(6, Qtde);
				this.insertStmt.bindDouble(7, Acrescimo);
				this.insertStmt.bindDouble(8, Desconto);
				this.insertStmt.bindDouble(9, Valor);
				this.insertStmt.bindDouble(10, FlexAcrescimo);
				this.insertStmt.bindDouble(11, FlexDesconto);

				valortot = (Qtde * Valor) + Acrescimo - Desconto;

				if ((PercST > 0.00) || (PercIPI > 0.00)) {
					this.insertStmt.bindDouble(12, valortot + ((valortot * PercST) / 100) + ((valortot * PercIPI) / 100));
				} else {
					this.insertStmt.bindDouble(12, valortot);
				}

				this.insertStmt.bindDouble(13, peso);

				retorno = this.insertStmt.executeInsert();
				TB_VENDA_UPDATETOTAl(PedidoID, db);

				// insere no saldo flex
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);

				if (dbb == null) {
					if (FlexAcrescimo != 0 || FlexDesconto != 0) {
						TB_SALDO_INSERE(sdf.format(new Date()), "Pedido Nº" + String.valueOf(PedidoID) + ", [ ADD ] Produto " + String.valueOf(Produto_Descricao), FlexAcrescimo, FlexDesconto, 0.00, 00, null);
					}
				}
			}

		} catch (Exception e) {
			Toast.makeText(context, "ERRO: " + e.toString(), Toast.LENGTH_LONG).show();

		}
		return retorno;
	}

	public void TB_VENDAS_ITENS_DELETAR(long xPedidoID, long xItemID, long ProdutoID, Double FlexAcrescimo, Double FlexDesconto) {

		// remove produto do pedido
		db.delete("VENDAS_ITENS", "_ID=" + String.valueOf(xItemID), null);
		TB_VENDA_UPDATETOTAl(xPedidoID, null);

		// insere no saldo flex
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
		TB_SALDO_INSERE(sdf.format(new Date()), "Pedido Nº" + String.valueOf(xPedidoID) + ", [ DEL ]Produto " + String.valueOf(ProdutoID), FlexDesconto, FlexAcrescimo, 0.00, 0, null);

	}

	public void TB_VENDA_UPDATETOTAl(long yPedidoID, SQLiteDatabase db) {

		// ATUALIZA O TOTAL DA VENDA
		if (db == null) {
			db = this.db;
		}
		try {
			Cursor cTot = db.rawQuery("select sum((vendas_itens.QTDE * vendas_itens.VALOR) + vendas_itens.acrescimo - vendas_itens.desconto), sum(vendas_itens.VALOR_ST) from vendas_itens where vendas_itens.vendaid = " + String.valueOf(yPedidoID), null);
			if (cTot.moveToFirst()) {
				this.insertStmt = db.compileStatement("update vendas set total = ?,total_st = ? where _id = " + String.valueOf(yPedidoID));
				this.insertStmt.bindDouble(1, cTot.getDouble(0));
				this.insertStmt.bindDouble(2, cTot.getDouble(1));
				this.insertStmt.executeInsert();
			}
		} catch (NullPointerException e) {
			Cursor cTot = db.rawQuery("select sum((vendas_itens.QTDE * vendas_itens.VALOR) + vendas_itens.acrescimo - vendas_itens.desconto), sum(vendas_itens.VALOR_ST) from vendas_itens where vendas_itens.vendaid = " + String.valueOf(yPedidoID), null);
			if (cTot.moveToFirst()) {
				this.insertStmt = db.compileStatement("update vendas set total = ?,total_st = ? where _id = " + String.valueOf(yPedidoID));
				this.insertStmt.bindDouble(1, cTot.getDouble(0));
				this.insertStmt.bindDouble(2, cTot.getDouble(1));
				this.insertStmt.executeInsert();
			}
		}

	}

	public long getMaxId(String tabela) {
		String query = "SELECT MAX(_id) AS max_id FROM " + tabela;
		Cursor cursor = db.rawQuery(query, null);

		long id = 0;
		if (cursor.moveToFirst()) {
			do {
				try {
					id = cursor.getLong(0);
				} catch (Exception e) {
					id = 1; // / TODO: handle exception
				}

			} while (cursor.moveToNext());
		}
		return id;
	}

	public boolean getIsSincronizado(String tabela, long id) {

		boolean retorno = false;

		Cursor cursor = db.rawQuery("select sincronizado from " + tabela + " where _id = " + String.valueOf(id), null);

		if (cursor.moveToFirst()) {
			retorno = (cursor.getLong(0) == 1);
		}

		if (cursor != null || !cursor.isClosed()) {
			cursor.close();
		}

		return retorno;
	}

	public boolean getIsSincronizadoCpf(String tabela, String id) {

		boolean retorno = false;

		Cursor cursor = db.rawQuery("select sincronizado from " + tabela + " where CPF_CNPJ like '" + String.valueOf(id) + "'", null);

		if (cursor.moveToFirst()) {
			retorno = (cursor.getLong(0) == 1);
		}

		if (cursor != null || !cursor.isClosed()) {
			cursor.close();
		}

		return retorno;
	}

	/**
	 * Método para comparar as das e retornar o numero de dias de diferença
	 * entre elas
	 * 
	 * Compare two date and return the difference between them in days.
	 * 
	 * @param dataLow
	 *            The lowest date
	 * @param dataHigh
	 *            The highest date
	 * 
	 * @return int
	 */
	public static int dataDiff(java.util.Date dataLow, java.util.Date dataHigh) {

		GregorianCalendar startTime = new GregorianCalendar();
		GregorianCalendar endTime = new GregorianCalendar();

		GregorianCalendar curTime = new GregorianCalendar();
		GregorianCalendar baseTime = new GregorianCalendar();

		startTime.setTime(dataLow);
		endTime.setTime(dataHigh);

		int dif_multiplier = 1;

		// Verifica a ordem de inicio das datas
		if (dataLow.compareTo(dataHigh) < 0) {
			baseTime.setTime(dataHigh);
			curTime.setTime(dataLow);
			dif_multiplier = 1;
		} else {
			baseTime.setTime(dataLow);
			curTime.setTime(dataHigh);
			dif_multiplier = -1;
		}

		int result_years = 0;
		int result_months = 0;
		int result_days = 0;

		// Para cada mes e ano, vai de mes em mes pegar o ultimo dia para import
		// acumulando
		// no total de dias. Ja leva em consideracao ano bissesto
		while (curTime.get(GregorianCalendar.YEAR) < baseTime.get(GregorianCalendar.YEAR) || curTime.get(GregorianCalendar.MONTH) < baseTime.get(GregorianCalendar.MONTH)) {

			int max_day = curTime.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
			result_months += max_day;
			curTime.add(GregorianCalendar.MONTH, 1);

		}

		// Marca que é um saldo negativo ou positivo
		result_months = result_months * dif_multiplier;

		// Retirna a diferenca de dias do total dos meses
		result_days += (endTime.get(GregorianCalendar.DAY_OF_MONTH) - startTime.get(GregorianCalendar.DAY_OF_MONTH));

		return result_years + result_months + result_days;
	}

}
