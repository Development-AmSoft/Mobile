package smart.mobile.outras.sincronismo.download;

import java.io.File;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.DB_ServerHost;
import smart.mobile.outras.sincronismo.envio.DB_Sincroniza_Novo_Envio;
import smart.mobile.utils.ZipDownload.DownloadHelper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DB_Sincroniza_Novo {

	private Context context;
	private DB_LocalHost banco;
	private DB_Sincroniza dbSincroniza;
	private DB_ServerHost service;
	private SQLiteDatabase dbNovo;
	private SQLiteStatement insertStmt;

	private String FILE_BANCO = "SmartMobile.db";
	private final String PATH = "/data/data/smart.mobile/databases/";

	public static String config = "CREATE TABLE CONFIG  (_id INTEGER PRIMARY KEY, " + "SERVIDOR     TEXT," + "BANCO        TEXT," + "EMPRESAID    INTEGER," + "EMPRESA      TEXT," + "VENDEDORID   INTEGER," + "VENDEDOR     TEXT, VERSAO DECIMAL)";
	public static String configDinamica = "CREATE TABLE CONFIG_DINAMICA (_id INTEGER PRIMARY KEY, SMARTMOBILE_UNIDADES_SECUNDARIAS INTEGER,  SMARTMOBILE_VALIDA_ESTOQUE INTEGER, CONSULTA_PRODUTO_WHERE TEXT)";
	public static String empresas = "CREATE TABLE EMPRESAS  (_id INTEGER PRIMARY KEY, " + "EMPRESA      TEXT)";
	public static String vendedores = "CREATE TABLE VENDEDORES(_id INTEGER PRIMARY KEY, " + "VENDEDORID   INTEGER," + "VENDEDOR     TEXT," + "GERENTE      INTEGER, COLETOR INTEGER)";
	public static String flex = "CREATE TABLE FLEX(_id INTEGER PRIMARY KEY, " + "DATA       TEXT," + "REFERENCIA TEXT," + "ACRESCIMO  DECIMAL," + "DESCONTO   DECIMAL," + "SALDO      DECIMAL," + "SINCRONIZADO INTEGER)";
	public static String metas = "CREATE TABLE METAS(_id INTEGER PRIMARY KEY, " + "MES   INTEGER," + "META  DECIMAL," + "TOTAL DECIMAL)";
	public static String formasPagto = "CREATE TABLE FORMAS_PGTO(_id INTEGER PRIMARY KEY, " + "FORMA_PGTOID  INTEGER," + "DESCRICAO     TEXT)";
	public static String listaPreco = "CREATE TABLE LISTAS_PRECOS(_id INTEGER PRIMARY KEY, " + "LISTAID       INTEGER," + "DESCRICAO     TEXT," + "TIPO_LISTA    TEXT," + "PERCENTUAL    DECIMAL)";
	public static String listPrecoProdutos = "CREATE TABLE LISTAS_PRECOS_PRODUTOS(_id INTEGER PRIMARY KEY, " + "LISTAID       INTEGER," + "PRODUTOID     INTEGER," + "TIPO          TEXT," + "PERCENTUAL    DECIMAL)";
	public static String clientes = "CREATE TABLE CLIENTES (_id INTEGER PRIMARY KEY, NOME     TEXT," + "FANTASIA TEXT," + "CPF_CNPJ TEXT," + "INSC_EST TEXT," + "RESPONSAVEL TEXT," + "CIDADE   TEXT," + "ENDERECO TEXT," + "NUMERO   TEXT," + "BAIRRO   TEXT," + "COMPLEMENTO TEXT," + "CEP      TEXT," + "TELEFONE TEXT," + "CELULAR  TEXT," + "EMAIL    TEXT," + "OBS      TEXT," + "LIMITE   TEXT," + "ULT_DATA DATE," + "ULT_TOTAL DECIMAL," + " LISTAID   INTEGER," + "FORMA_PGTOID INTEGER," + "SINCRONIZADO INTEGER)";
	public static String clientesHistorico = "CREATE TABLE CLIENTES_HISTORICO (_id INTEGER PRIMARY KEY, " + "CPF_CNPJ  text," + "PRODUTOID  INTEGER," + "UNIDADEID  INTEGER," + "LINHAID    INTEGER," + "COLUNAID   INTEGER," + "QTDE       DECIMAL," + "ACRESCIMO  DECIMAL," + "DESCONTO   DECIMAL," + "VALOR      DECIMAL)";
	public static String clientesFormaPagamento = "CREATE TABLE CLIENTES_FORMAS_PGTO (_id INTEGER PRIMARY KEY, " + "CPF_CNPJ  TEXT," + "FORMA_PGTOID INTEGER)";
	public static String titulos = "CREATE TABLE TITULOS (_id INTEGER PRIMARY KEY, " + "TIPO       INTEGER," + "NOME       TEXT," + "CODIGO     INTEGER," + "DOCUMENTO  TEXT," + "EMISSAO    TEXT," + "VENCIMENTO TEXT," + "VALOR      REAL," + "HISTORICO  TEXT)";
	public static String produtos = "CREATE TABLE PRODUTOS (_id INTEGER PRIMARY KEY, PRODUTOID INTEGER, UNIDADEID INTEGER, UND TEXT, LINHAID INTEGER, COLUNAID INTEGER, DESCRICAO TEXT, LINHA TEXT, COLUNA TEXT, GRUPO TEXT, DESC_MAX DECIMAL, VALOR DECIMAL, ESTOQUE DECIMAL, IMPOSTOID INTEGER, ALIQUOTA_IPI  DECIMAL, DESTAQUE INTEGER, PESO DECIMAL, CODIGO TEXT, CODIGO_BARRA TEXT, FATOR DECIMAL, MARCA TEXT)";
	public static String impostos = "CREATE TABLE IMPOSTOS (_id INTEGER PRIMARY KEY, " + "IMPOSTOID    INTEGER," + "UF           TEXT," + "ALIQUOTA_UF  DECIMAL," + "SUBS_ALIQ    DECIMAL," + "SUBS_IVA     DECIMAL)";
	public static String vendas = "CREATE TABLE VENDAS	(_id INTEGER PRIMARY KEY, " + "OPERACAO      INTEGER," + "CPF_CNPJ TEXT," + "DATA          TEXT," + "FORMA_PGTOID  INTEGER," + "LISTAID       INTEGER," + "OBS           TEXT," + "TOTAL         DECIMAL," + "TOTAL_ST      DECIMAL," + "SINCRONIZADO  INTEGER, ORIGEM INTEGER, SINCRONIZAR INTEGER)";
	public static String vendasItens = "CREATE TABLE VENDAS_ITENS	(_id INTEGER PRIMARY KEY, " + "vendaid INTEGER," + "PRODUTOID    INTEGER," + "UNIDADEID    INTEGER," + "LINHAID      INTEGER," + "COLUNAID     INTEGER," + "QTDE         DECIMAL," + "ACRESCIMO    DECIMAL," + "DESCONTO     DECIMAL," + "VALOR        DECIMAL," + "FLEX_ACRESCIMO DECIMAL," + "FLEX_DESCONTO  DECIMAL," + "VALOR_ST       DECIMAL, peso_total decimal)";
	public static String estoque = "CREATE TABLE ESTOQUE (_id INTEGER PRIMARY KEY, PRODUTOID INTEGER, LINHAID INTEGER, COLUNAID INTEGER, UNIDADEID INTEGER, ESTOQUE DECIMAL, OBS TEXT, ACRESCIMO REAL, DECRESCIMO REAL, COLETADO REAL)";

	public DB_Sincroniza_Novo(Context context, DB_Sincroniza dbSincroniza) {
		this.context = context;
		banco = new DB_LocalHost(context);
		// banco.DB_ConfigLoad();
		// banco.DB_SaldoFlexLoad();
		if (dbSincroniza == null) {
			this.dbSincroniza = new DB_Sincroniza(context);
		} else {
			this.dbSincroniza = dbSincroniza;
		}
		service = new DB_ServerHost(context, banco.ServidorOnline, banco.Banco);
	}

	private Document xmlFinal() {
		Document documento = new Document();
		Element principal = new Element("sinc");
		principal.setAttribute("banco", banco.Banco);
		principal.setAttribute("empresa", banco.EmpresaID);
		principal.setAttribute("vendedor", banco.VendedorID);

		principal.addContent(gerarCliente());
		principal.addContent(gerarFormaDePagamento());
		principal.addContent(gerarListaPreco());
		principal.addContent(gerarListaPrecoProduto());
		principal.addContent(gerarFormaPagamentoCliente());
		principal.addContent(gerarProduto());
		principal.addContent(gerarImposto());
		principal.addContent(gerarTitulo());
		principal.addContent(gerarSaldo());
		principal.addContent(gerarConfDinamica());

		principal.addContent(gerarConf());
		principal.addContent(gerarVendedores());
		principal.addContent(gerarEmpresas());
		principal.addContent(gerarMetas());
		principal.addContent(gerarClientesHistorico()); // Descontinuado
		principal.addContent(gerarVendas());
		principal.addContent(gerarVendasItem());
		principal.addContent(gerarEstoque());

		documento.setRootElement(principal);
		return documento;
	}

	public String executarSinc(boolean implantacao) {

		String error = "";
		String retorno = "";
		try {
			DB_Sincroniza_Novo_Envio envio = new DB_Sincroniza_Novo_Envio(context, banco, dbSincroniza, service);
			String volta = envio.executarSinc(implantacao);
			if (!volta.isEmpty()) {
				throw new Exception(volta);
			} else {

				String a = new XMLOutputter().outputString(xmlFinal());
				retorno = service.Sql_sincronizarBanco(a);
				Log.v("resposta banco", retorno);

				if (retorno.contains(".db")) {

					DownloadHelper dow = new DownloadHelper();
					if (banco.ServidorOnline.contains(":")) {
						dow.DownloadFromUrl("http://" + banco.ServidorOnline + "/axis/" + retorno, "databases/" + retorno);
					} else {
						dow.DownloadFromUrl("http://" + banco.ServidorOnline + ":8080/axis/" + retorno, "databases/" + retorno);
					}

					OpenHelperNovo dbHelper = new OpenHelperNovo(this.context, retorno, 24);
					this.dbNovo = dbHelper.getWritableDatabase();

					inserirBancoBack();
					dbHelper.close();
					trocaBanco(retorno);
				} else if (retorno.equalsIgnoreCase("No such operation 'sincronizarBanco'")) {

					throw new Exception("WebService desatualizado.\nInforme a equipe AmSoft.");

				} else {
					throw new Exception(retorno);
				}
			}
		} catch (Exception e) {
			error = e.getMessage();
		}

		if (error.isEmpty()) {
			return "";
		} else {
			return error;
		}

	}

	private void trocaBanco(String nomeBanco) throws Exception {
		File antigo = new File(PATH + FILE_BANCO);
		File novo = new File(PATH + nomeBanco);

		if (novo.exists()) {
			if (antigo.exists()) {
				antigo.delete();
				novo.renameTo(antigo);
				novo.setWritable(true);
				novo.setExecutable(true);
				novo.setReadable(true);
			}
		}
	}

	private void inserirBancoBack() throws Exception {
		try {
			try {
				Long idNovo = 0l;
				Cursor rs = null;
				boolean erro = false;
				try {
					rs = banco.db.rawQuery("select OPERACAO, CPF_CNPJ, DATA, FORMA_PGTOID, LISTAID, TOTAL, OBS, SINCRONIZADO, _ID, SINCRONIZAR from vendas", null);
				} catch (Exception e) {
					erro = true;
					rs = banco.db.rawQuery("select vendas.OPERACAO, clientes.CPF_CNPJ, vendas.DATA, vendas.FORMA_PGTOID, vendas.LISTAID, vendas.TOTAL, vendas.OBS, vendas.SINCRONIZADO, vendas._ID from vendas inner join clientes on vendas.CPF_CNPJ = clientes.CPF_CNPJ", null);
				}
				while (rs.moveToNext()) {

					// Vendas: OPERACAO, CLIENTEID, DATA, FORMA_PGTOID,
					// LISTAID, OBS, TOTAL, TOTAL_ST, SINCRONIZADO

					this.insertStmt = this.dbNovo.compileStatement("insert into vendas (operacao,CPF_CNPJ,data,forma_pgtoid,listaid,total,obs,sincronizado, _ID, sincronizar) " + " values (?,?,?,?,?,?,?,?,?,?)");
					this.insertStmt.bindLong(1, rs.getInt(0));
					this.insertStmt.bindString(2, rs.getString(1));
					this.insertStmt.bindString(3, rs.getString(2));
					this.insertStmt.bindLong(4, rs.getInt(3));
					this.insertStmt.bindLong(5, rs.getInt(4));
					this.insertStmt.bindDouble(6, rs.getDouble(5));
					this.insertStmt.bindString(7, rs.getString(6));
					this.insertStmt.bindLong(8, rs.getInt(7));
					this.insertStmt.bindLong(9, rs.getInt(8));
					if (!erro) {
						this.insertStmt.bindLong(10, rs.getInt(9));
					} else {
						this.insertStmt.bindLong(10, 1);
					}

					this.insertStmt.executeInsert();

					String idVenda = String.valueOf(rs.getLong(8));
					Cursor rs2 = banco.db.rawQuery("select item.PRODUTOID, item.UNIDADEID, item.LINHAID, item.COLUNAID, produtos.DESCRICAO as descricao, " + "item.QTDE, item.ACRESCIMO, item.DESCONTO, item.VALOR, item.FLEX_ACRESCIMO, item.FLEX_DESCONTO, item.VALOR_ST, item._id, item.peso_total " + "from VENDAS_ITENS as item join PRODUTOS on item.produtoid = produtos.produtoid and item.unidadeid = produtos.unidadeid and item.linhaid = produtos.linhaid and item.colunaid = produtos.colunaid " + "where item.VENDAID = " + idVenda, null);
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
						if (percST > 0) {
							percST = (100 * percST) / valorTotal;
						}

						double valortot = 0.00;
						long retorno = 0;
						// TB_VENDAS_ITENS_INSERIR(long PedidoID, long ItemID,
						// int
						// ProdutoID, int UnidadeID, int LinhaID, int ColunaID,
						// String
						// Produto_Descricao, Double Qtde, Double Acrescimo,
						// Double
						// Desconto, Double Valor, Double FlexAcrescimo, Double
						// FlexDesconto, Double PercST)
						// insere item na venda
						banco.TB_VENDAS_ITENS_INSERIR(Long.parseLong(idVenda), 0, rs2.getInt(rs2.getColumnIndex("PRODUTOID")), rs2.getInt(rs2.getColumnIndex("UNIDADEID")), rs2.getInt(rs2.getColumnIndex("LINHAID")), rs2.getInt(rs2.getColumnIndex("COLUNAID")), rs2.getString(rs2.getColumnIndex("descricao")), quantidade, acrescimo, desconto, rs2.getDouble(rs2.getColumnIndex("VALOR")), rs2.getDouble(rs2.getColumnIndex("FLEX_ACRESCIMO")), rs2.getDouble(rs2.getColumnIndex("FLEX_DESCONTO")), percST, 0.00, rs2.getDouble(rs2.getColumnIndex("peso_total")), this.dbNovo);

					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Cursor rs3 = null;
			try {
				rs3 = banco.db.rawQuery("select _id, VENDEDORID, VENDEDOR, GERENTE, COLETOR from VENDEDORES", null);
			} catch (Exception e) {
				rs3 = banco.db.rawQuery("select _id, VENDEDORID, VENDEDOR, GERENTE from VENDEDORES", null);
			}
			while (rs3.moveToNext()) {
				try {
					this.insertStmt = this.dbNovo.compileStatement("insert into vendedores (_id, VENDEDORID, VENDEDOR, GERENTE, COLETOR) " + " values (?,?,?,?,?)");
					this.insertStmt.bindLong(1, rs3.getInt(0));
					this.insertStmt.bindLong(2, rs3.getInt(1));
					this.insertStmt.bindString(3, rs3.getString(2));
					this.insertStmt.bindLong(4, rs3.getInt(3));
					
					//COLETOR ------------
					String retorno = service.Sql_Select("select coletor from vw_mobile_vendedores where vendedorid = "+  rs3.getInt(1));
					retorno = retorno.replace("#l#", "");
					this.insertStmt.bindLong(5, Long.parseLong(retorno));
					//--------------------
				} catch (Exception e) {
					this.insertStmt = this.dbNovo.compileStatement("insert into vendedores (_id, VENDEDORID, VENDEDOR, GERENTE) " + " values (?,?,?,?)");
					this.insertStmt.bindLong(1, rs3.getInt(0));
					this.insertStmt.bindLong(2, rs3.getInt(1));
					this.insertStmt.bindString(3, rs3.getString(2));
					this.insertStmt.bindLong(4, rs3.getInt(3));
				}
				this.insertStmt.executeInsert();
			}

			Cursor rs4 = banco.db.rawQuery("select _id, EMPRESA from EMPRESAS", null);
			while (rs4.moveToNext()) {
				this.insertStmt = this.dbNovo.compileStatement("insert into EMPRESAS (_id, EMPRESA) " + " values (?,?)");
				this.insertStmt.bindLong(1, rs4.getInt(0));
				this.insertStmt.bindString(2, rs4.getString(1));
				this.insertStmt.executeInsert();
			}

			Cursor rs5 = null;

			try {
				rs5 = banco.db.rawQuery("select _id, SERVIDOR, BANCO, EMPRESAID, EMPRESA, VENDEDORID, VENDEDOR, VERSAO from CONFIG", null);
			} catch (Exception e) {
				rs5 = banco.db.rawQuery("select _id, SERVIDOR, BANCO, EMPRESAID, EMPRESA, VENDEDORID, VENDEDOR from CONFIG", null);
			}
			while (rs5.moveToNext()) {
				// _id INTEGER PRIMARY KEY, " + "SERVIDOR TEXT," + "BANCO
				// TEXT," + "EMPRESAID INTEGER," + "EMPRESA TEXT," + "VENDEDORID
				// INTEGER," + "VENDEDOR TEXT)"
				try {
					this.insertStmt = this.dbNovo.compileStatement("insert into CONFIG (_id, SERVIDOR, BANCO, EMPRESAID, EMPRESA, VENDEDORID, VENDEDOR, VERSAO) " + " values (?,?,?,?,?,?,?,?)");

					PackageManager manager = context.getPackageManager();
					PackageInfo info = manager.getPackageInfo("smart.mobile", 0);
					String name = info.versionName;

					this.insertStmt.bindDouble(8, Double.parseDouble(name));
				} catch (Exception e) {
					this.insertStmt = this.dbNovo.compileStatement("insert into CONFIG (_id, SERVIDOR, BANCO, EMPRESAID, EMPRESA, VENDEDORID, VENDEDOR) " + " values (?,?,?,?,?,?,?)");
				}
				this.insertStmt.bindLong(1, rs5.getInt(0));
				this.insertStmt.bindString(2, rs5.getString(1));
				this.insertStmt.bindString(3, rs5.getString(2));
				this.insertStmt.bindLong(4, rs5.getInt(3));
				this.insertStmt.bindString(5, rs5.getString(4));
				this.insertStmt.bindLong(6, rs5.getInt(5));
				this.insertStmt.bindString(7, rs5.getString(6));
				this.insertStmt.executeInsert();
			}

			Cursor rs6 = null;

			try {
				rs6 = banco.db.rawQuery("select CONSULTA_PRODUTO_WHERE from CONFIG_DINAMICA", null);

				while (rs6.moveToNext()) {
					try {
						this.insertStmt = this.dbNovo.compileStatement("UPDATE CONFIG_DINAMICA SET CONSULTA_PRODUTO_WHERE = ? WHERE _id = 1");
						this.insertStmt.bindString(1, rs6.getString(0));
						this.insertStmt.executeInsert();
					} catch (Exception e) {

					}
				}
			} catch (Exception e) {
			}

			Cursor rs7 = null;
			
			try {
				rs7 = banco.db.rawQuery("select _id, PRODUTOID, LINHAID, COLUNAID, UNIDADEID, ESTOQUE, OBS, ACRESCIMO, DECRESCIMO, COLETADO from ESTOQUE where ACRESCIMO = 0.0 and DECRESCIMO = 0.0;", null);
				
				while (rs7.moveToNext()) {
					try {
						this.insertStmt = this.dbNovo.compileStatement("insert into ESTOQUE (_id, PRODUTOID, LINHAID, COLUNAID, UNIDADEID, ESTOQUE, OBS, ACRESCIMO, DECRESCIMO, COLETADO) values (?,?,?,?,?,?,?,?,?,?);");
						this.insertStmt.bindLong(1, rs7.getInt(0));
						this.insertStmt.bindLong(2, rs7.getInt(1));
						this.insertStmt.bindLong(3, rs7.getInt(2));
						this.insertStmt.bindLong(4, rs7.getInt(3));
						this.insertStmt.bindLong(5, rs7.getInt(4));
						this.insertStmt.bindDouble(6, rs7.getDouble(5));
						this.insertStmt.bindString(7, rs7.getString(5));
						this.insertStmt.bindDouble(8, rs7.getDouble(7));
						this.insertStmt.bindDouble(9, rs7.getDouble(8));
						this.insertStmt.bindDouble(10, rs7.getDouble(9));
						this.insertStmt.executeInsert();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			banco.DB_SaldoFlexLoad();

		} catch (Exception e) {
			throw e;
		}

	}

	private Element gerarMetas() {
		Element create = gerarCreate(this.metas);
		return create;
	}

	private Element gerarClientesHistorico() {
		Element create = gerarCreate(this.clientesHistorico);
		return create;
	}

	private Element gerarEstoque() {
		Element create = gerarCreate(this.estoque);
		return create;
	}

	private Element gerarVendas() {
		Element create = gerarCreate(this.vendas);
		return create;
	}

	private Element gerarVendasItem() {
		Element create = gerarCreate(this.vendasItens);
		return create;
	}

	private Element gerarVendedores() {
		Element create = gerarCreate(this.vendedores);
		return create;
	}

	private Element gerarEmpresas() {
		Element create = gerarCreate(this.empresas);
		return create;
	}

	private Element gerarConf() {
		Element create = gerarCreate(this.config);
		return create;
	}

	private Element gerarConfDinamica() {
		Element create = gerarCreate(this.configDinamica);
		Element select = gerarSelect(dbSincroniza.montarSQLConfDinamic());

		Element campo = new Element("campo");
		campo.setAttribute("tabela", "CONFIG_DINAMICA");

		// _id INTEGER PRIMARY KEY, SMARTMOBILE_UNIDADES_SECUNDARIAS INTEGER,
		// SMARTMOBILE_VALIDA_ESTOQUE INTEGER
		campo.addContent(gerarCampo("SMARTMOBILE_UNIDADES_SECUNDARIAS", "DECIMAL"));
		campo.addContent(gerarCampo("SMARTMOBILE_VALIDA_ESTOQUE", "DECIMAL"));
		campo.addContent(gerarCampo("CONSULTA_PRODUTO_WHERE", ""));

		select.addContent(campo);
		create.addContent(select);
		return create;
	}

	private Element gerarSaldo() {
		Element create = gerarCreate(this.flex);
		Element select = gerarSelect(dbSincroniza.montarSQLSaldo());

		Element campo = new Element("campo");
		campo.setAttribute("tabela", "FLEX");

		// _id INTEGER PRIMARY KEY, " + "DATA TEXT," + "REFERENCIA
		// TEXT," + "ACRESCIMO DECIMAL," + "DESCONTO DECIMAL," +
		// "SALDO      DECIMAL," + "SINCRONIZADO INTEGER
		campo.addContent(gerarCampo("DATA", "text"));
		campo.addContent(gerarCampo("REFERENCIA", "text"));
		campo.addContent(gerarCampo("ACRESCIMO", "DECIMAL"));
		campo.addContent(gerarCampo("DESCONTO", "DECIMAL"));
		campo.addContent(gerarCampo("SALDO", "DECIMAL"));
		campo.addContent(gerarCampo("SINCRONIZADO", "1"));

		select.addContent(campo);
		create.addContent(select);
		return create;
	}

	private Element gerarTitulo() {
		Element create = gerarCreate(this.titulos);
		Element select = gerarSelect(dbSincroniza.montarSQLTitulo());

		Element campo = new Element("campo");
		campo.setAttribute("tabela", "TITULOS");

		// _id INTEGER PRIMARY KEY, " + "TIPO INTEGER," + "NOME TEXT," + "CODIGO
		// INTEGER," + "DOCUMENTO TEXT," +
		// "EMISSAO    TEXT," + "VENCIMENTO TEXT," + "VALOR      REAL," +
		// "HISTORICO TEXT
		campo.addContent(gerarCampo("TIPO", "INTEGER"));
		campo.addContent(gerarCampo("NOME", "text"));
		campo.addContent(gerarCampo("CODIGO", "INTEGER"));
		campo.addContent(gerarCampo("DOCUMENTO", "text"));
		campo.addContent(gerarCampo("EMISSAO", "text"));
		campo.addContent(gerarCampo("VENCIMENTO", "text"));
		campo.addContent(gerarCampo("VALOR", "REAL"));
		campo.addContent(gerarCampo("HISTORICO", "text"));

		select.addContent(campo);
		create.addContent(select);
		return create;
	}

	private Element gerarImposto() {
		Element create = gerarCreate(this.impostos);
		Element select = gerarSelect(dbSincroniza.montarSQLImposto());

		Element campo = new Element("campo");
		campo.setAttribute("tabela", "IMPOSTOS");

		// _id INTEGER PRIMARY KEY, " + "IMPOSTOID INTEGER," + "UF
		// TEXT," + "ALIQUOTA_UF DECIMAL," + "SUBS_ALIQ DECIMAL," + "SUBS_IVA
		// DECIMAL
		campo.addContent(gerarCampo("IMPOSTOID", "INTEGER"));
		campo.addContent(gerarCampo("UF", "text"));
		campo.addContent(gerarCampo("ALIQUOTA_UF", "DECIMAL"));
		campo.addContent(gerarCampo("SUBS_ALIQ", "DECIMAL"));
		campo.addContent(gerarCampo("SUBS_IVA", "DECIMAL"));

		select.addContent(campo);
		create.addContent(select);
		return create;
	}

	private Element gerarProduto() {
		Element create = gerarCreate(this.produtos);
		Element select = gerarSelect(dbSincroniza.montarSQLProduto());

		Element campo = new Element("campo");
		campo.setAttribute("tabela", "PRODUTOS");

		// _id INTEGER PRIMARY KEY, " + "PRODUTOID INTEGER," + "UNIDADEID
		// INTEGER," + "UND TEXT," + "LINHAID INTEGER," + "COLUNAID INTEGER," +
		// "DESCRICAO TEXT," + "LINHA TEXT," + "COLUNA TEXT," + "GRUPO TEXT," +
		// "DESC_MAX DECIMAL," + "VALOR DECIMAL," + "ESTOQUE DECIMAL," +
		// "IMPOSTOID INTEGER," + "ALIQUOTA_IPI DECIMAL
		if (banco.Banco.toUpperCase().trim().equals("INCAS") || banco.Banco.toUpperCase().trim().equals("SIAL")) {
			campo.addContent(gerarCampo("PRODUTOID", "CODIGO"));
		} else {
			campo.addContent(gerarCampo("PRODUTOID", "INTEGER"));
		}
		campo.addContent(gerarCampo("UNIDADEID", "INTEGER"));
		campo.addContent(gerarCampo("UND", "text"));
		campo.addContent(gerarCampo("LINHAID", "INTEGER"));
		campo.addContent(gerarCampo("COLUNAID", "INTEGER"));
		campo.addContent(gerarCampo("DESCRICAO", "text"));
		campo.addContent(gerarCampo("LINHA", "text"));
		campo.addContent(gerarCampo("COLUNA", "text"));
		campo.addContent(gerarCampo("GRUPO", "text"));
		campo.addContent(gerarCampo("DESC_MAX", "DECIMAL"));
		campo.addContent(gerarCampo("VALOR", "DECIMAL"));
		campo.addContent(gerarCampo("ESTOQUE", "DECIMAL"));
		campo.addContent(gerarCampo("IMPOSTOID", "INTEGER"));
		campo.addContent(gerarCampo("ALIQUOTA_IPI", "DECIMAL"));
		campo.addContent(gerarCampo("DESTAQUE", "INTEGER"));
		campo.addContent(gerarCampo("PESO", "DECIMAL"));
		campo.addContent(gerarCampo("CODIGO", "text"));
		campo.addContent(gerarCampo("CODIGO_BARRA", "text"));
		campo.addContent(gerarCampo("FATOR", "DECIMAL"));
		campo.addContent(gerarCampo("MARCA", "text"));

		select.addContent(campo);
		create.addContent(select);
		return create;
	}

	private Element gerarFormaPagamentoCliente() {
		Element create = gerarCreate(this.clientesFormaPagamento);
		Element select = gerarSelect(dbSincroniza.montarSQLFormaPagamentoCliente());

		Element campo = new Element("campo");
		campo.setAttribute("tabela", "CLIENTES_FORMAS_PGTO");

		// _id INTEGER PRIMARY KEY, " + "CLIENTEID INTEGER," + "FORMA_PGTOID
		// INTEGER
		campo.addContent(gerarCampo("CPF_CNPJ", "text"));
		campo.addContent(gerarCampo("FORMA_PGTOID", "INTEGER"));

		select.addContent(campo);
		create.addContent(select);
		return create;
	}

	private Element gerarListaPrecoProduto() {
		Element create = gerarCreate(this.listPrecoProdutos);
		Element select = gerarSelect(dbSincroniza.montarSQLListaPrecoProduto());

		Element campo = new Element("campo");
		campo.setAttribute("tabela", "LISTAS_PRECOS_PRODUTOS");

		// _id INTEGER PRIMARY KEY, " + "LISTAID INTEGER," + "PRODUTOID
		// INTEGER," + "TIPO TEXT," + "PERCENTUAL DECIMAL
		campo.addContent(gerarCampo("LISTAID", "INTEGER"));
		if (banco.Banco.toUpperCase().trim().equals("INCAS") || banco.Banco.toUpperCase().trim().equals("SIAL")) {
			campo.addContent(gerarCampo("PRODUTOID", "CODIGO"));
		} else {
			campo.addContent(gerarCampo("PRODUTOID", "INTEGER"));
		}
		campo.addContent(gerarCampo("TIPO", "text"));
		campo.addContent(gerarCampo("PERCENTUAL", "DECIMAL"));

		select.addContent(campo);
		create.addContent(select);
		return create;
	}

	private Element gerarListaPreco() {
		Element create = gerarCreate(this.listaPreco);
		Element select = gerarSelect(dbSincroniza.montarSQLListaPreco());

		Element campo = new Element("campo");
		campo.setAttribute("tabela", "LISTAS_PRECOS");

		// _id INTEGER PRIMARY KEY, " + "LISTAID INTEGER," + "DESCRICAO
		// TEXT," + "TIPO_LISTA TEXT," + "PERCENTUAL DECIMAL
		campo.addContent(gerarCampo("LISTAID", "INTEGER"));
		campo.addContent(gerarCampo("DESCRICAO", "text"));
		campo.addContent(gerarCampo("TIPO_LISTA", "text"));
		campo.addContent(gerarCampo("PERCENTUAL", "DECIMAL"));

		select.addContent(campo);
		create.addContent(select);
		return create;
	}

	private Element gerarFormaDePagamento() {
		Element create = gerarCreate(this.formasPagto);
		Element select = gerarSelect(dbSincroniza.montarSQLFormasDePagamento());

		Element campo = new Element("campo");
		campo.setAttribute("tabela", "FORMAS_PGTO");

		// _id INTEGER PRIMARY KEY, " + "FORMA_PGTOID INTEGER," + "DESCRICAO
		// TEXT
		campo.addContent(gerarCampo("FORMA_PGTOID", "INTEGER"));
		campo.addContent(gerarCampo("DESCRICAO", "text"));

		select.addContent(campo);
		create.addContent(select);
		return create;
	}

	private Element gerarCliente() {
		Element create = gerarCreate(this.clientes);
		Element select = gerarSelect(dbSincroniza.montarSQLCLiente());
		Element campo = new Element("campo");
		campo.setAttribute("tabela", "CLIENTES");

		/*
		 * id INTEGER PRIMARY KEY NOME TEXT FANTASIA TEXT CPF_CNPJ TEXT INSC_EST
		 * TEXT RESPONSAVEL TEXT CIDADE TEXT ENDERECO TEXT NUMERO TEXT BAIRRO
		 * TEXT COMPLEMENTO TEXT CEP TEXT TELEFONE TEXT CELULAR TEXT EMAIL TEXT
		 * OBS TEXT LIMITE TEXT ULT_DATA TEXT ULT_TOTAL DECIMAL ULT_DATAX
		 * INTEGER LISTAID INTEGER FORMA_PGTOID INTEGER SINCRONIZADO INTEGER
		 */

		campo.addContent(gerarCampo("NOME", "text"));
		campo.addContent(gerarCampo("FANTASIA", "text"));
		campo.addContent(gerarCampo("CPF_CNPJ", "text"));
		campo.addContent(gerarCampo("INSC_EST", "text"));
		campo.addContent(gerarCampo("RESPONSAVEL", "text"));
		campo.addContent(gerarCampo("CIDADE", "text"));
		campo.addContent(gerarCampo("ENDERECO", "text"));
		campo.addContent(gerarCampo("NUMERO", "text"));
		campo.addContent(gerarCampo("BAIRRO", "text"));
		campo.addContent(gerarCampo("COMPLEMENTO", "text"));
		campo.addContent(gerarCampo("CEP", "text"));
		campo.addContent(gerarCampo("TELEFONE", "text"));
		campo.addContent(gerarCampo("CELULAR", "text"));
		campo.addContent(gerarCampo("EMAIL", "text"));
		campo.addContent(gerarCampo("OBS", "text"));
		campo.addContent(gerarCampo("LIMITE", "text"));
		campo.addContent(gerarCampo("ULT_DATA", "DATE"));
		campo.addContent(gerarCampo("ULT_TOTAL", "DECIMAL"));
		campo.addContent(gerarCampo("LISTAID", "INTEGER"));
		campo.addContent(gerarCampo("FORMA_PGTOID", "INTEGER"));
		campo.addContent(gerarCampo("SINCRONIZADO", "INTEGER"));

		select.addContent(campo);
		create.addContent(select);
		return create;
	}

	private Element gerarCampo(String value, String type) {
		Element item = new Element("item");
		Attribute aType = new Attribute("type", type);
		Attribute aValue = new Attribute("value", value);

		item.setAttribute(aValue);
		item.setAttribute(aType);

		return item;
	}

	private Element gerarCreate(String sql) {
		Element eSql = new Element("create");
		Attribute exec = new Attribute("exec", sql);
		eSql.setAttribute(exec);

		return eSql;
	}

	private Element gerarSelect(String sql) {
		Element eSql = new Element("select");
		Attribute exec = new Attribute("exec", sql);
		eSql.setAttribute(exec);

		return eSql;
	}

	private static class OpenHelperNovo extends SQLiteOpenHelper {

		OpenHelperNovo(Context context, String nomeBanco, int versaoBanco) {
			super(context, nomeBanco, null, versaoBanco);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}

}
