package smart.mobile.outras.sincronismo.envio;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.DB_ServerHost;
import smart.mobile.outras.sincronismo.download.DB_Sincroniza;

public class DB_Sincroniza_Novo_Envio {

	private Context context;
	private DB_Sincroniza dbSincroniza;
	private DB_LocalHost banco;
	private DB_ServerHost service;

	public DB_Sincroniza_Novo_Envio(Context context, DB_LocalHost banco, DB_Sincroniza dbSincroniza, DB_ServerHost service) {
		this.context = context;
		this.banco = banco;
		this.dbSincroniza = dbSincroniza;
		this.service = service;
	}

	public String executarSinc(boolean implantacao) {
		String retorno = "";
		if (!implantacao) {
			try {
				String xml = new XMLOutputter().outputString(xmlFinal());
				retorno = service.Sql_sincronizarInsereBanco(xml);
				Log.v("resposta banco", retorno);

				retorno = executarRetorno(retorno);
			} catch (SQLiteException e) {
				if (e.getMessage().contains("SELECT")) {
					if (e.getMessage().contains("WHERE")) {
						retorno = e.getMessage().split("SELECT")[0] + e.getMessage().split("FROM")[1].split("WHERE")[0];
					} else {
						retorno = e.getMessage().split("SELECT")[0] + e.getMessage().split("FROM")[1];
					}
				} else {
					retorno = e.getMessage();
				}

				retorno = "Mobile: " + retorno;
			} catch (Exception e) {
				retorno = "Conexão: " + e.getMessage();
			}
		}

		return retorno;
	}

	private String executarRetorno(String retorno) {
		String retornoFinal = "";
		boolean error = false;

		SAXBuilder sb = new SAXBuilder();
		Document doc = null;
		try {
			doc = sb.build(new StringReader(retorno));
		} catch (Exception e) {
			error = true;
			retornoFinal = retorno;
		}

		if (!error) {

			try {

				Element eRetorno = doc.getRootElement();

				if (eRetorno.getChild("error") != null) {
					Element erro = eRetorno.getChild("error");
					throw new Exception("Servidor: " + erro.getAttributeValue("mensagem"));
				}

				Element eClientes = eRetorno.getChild("clientes");

				List ListClientes = eClientes.getChildren();
				Iterator iClientes = ListClientes.iterator();
				String ret = "";
				ArrayList<String> idClientes = new ArrayList<String>();
				while (iClientes.hasNext()) {
					Element eCliente = (Element) iClientes.next();
					idClientes.add(eCliente.getAttributeValue("id"));
				}

				if (idClientes.size() > 0) {
					for (String id : idClientes) {
						ContentValues values = new ContentValues();
						values.put("SINCRONIZADO", 1);
						banco.db.update("CLIENTES", values, "CPF_CNPJ like '" + id + "'", null);
					}
				}

				Element eVendas = eRetorno.getChild("pedidos");

				List ListVendas = eVendas.getChildren();
				Iterator iVendas = ListVendas.iterator();

				ArrayList<String> idVendas = new ArrayList<String>();
				while (iVendas.hasNext()) {
					Element eVenda = (Element) iVendas.next();
					idVendas.add(eVenda.getAttributeValue("id"));
				}

				if (idVendas.size() > 0) {
					for (String id : idVendas) {
						ContentValues values = new ContentValues();
						values.put("SINCRONIZADO", 1);
						banco.db.update("VENDAS", values, "_id = " + id, null);
					}
				}

				Element eSaldos = eRetorno.getChild("saldos");

				List ListSaldos = eSaldos.getChildren();
				Iterator iSaldo = ListSaldos.iterator();

				ArrayList<String> idSaldos = new ArrayList<String>();
				while (iSaldo.hasNext()) {
					Element eSaldo = (Element) iSaldo.next();
					idSaldos.add(eSaldo.getAttributeValue("id"));
				}

				if (idSaldos.size() > 0) {
					for (String id : idSaldos) {
						SQLiteStatement insertStmtx = banco.db.compileStatement("update FLEX set SINCRONIZADO = 1 where _id = " + id);
						insertStmtx.executeInsert();
					}
				}

				Log.v("", ret);

			} catch (Exception e) {
				e.printStackTrace();
				retornoFinal = e.getMessage();
			}
		}

		return retornoFinal;
	}

	private Document xmlFinal() throws Exception {
		Document documento = new Document();
		Element principal = new Element("ins");
		principal.setAttribute("banco", banco.Banco);

		principal.addContent(criarXMLClientes());
		principal.addContent(criarXMLPedidos());
		principal.addContent(criarXMLSaldo());
		principal.addContent(criarXMLEstoque());

		documento.setRootElement(principal);

		return documento;
	}

	private Element criarXMLClientes() throws Exception, SQLiteException {
		Element eClientes = new Element("clientes");

		Cursor cli0 = banco.Sql_SelectErro("CLIENTES", new String[] { "_id", "NOME", "FANTASIA", "CPF_CNPJ", "INSC_EST", "RESPONSAVEL", "ENDERECO", "NUMERO", "BAIRRO", "CIDADE", "TELEFONE", "CELULAR", "CEP", "EMAIL", "OBS", "COMPLEMENTO" }, "SINCRONIZADO = 0 OR SINCRONIZADO = 3", "");

		if (cli0.moveToFirst()) {
			do {
				Element eCliente = new Element("cliente");
				Attribute exec = null;

				// getColumnIndex(String)
				String clienteid = cli0.getString(3);

				Attribute aIdCliente = new Attribute("id", clienteid);
				eCliente.setAttribute(aIdCliente);

				String nome = cli0.getString(1);
				String fantasia = cli0.getString(2);
				String cpfcnpj = cli0.getString(3);
				String inscest = cli0.getString(4);
				String responsavel = cli0.getString(5);
				String endereco = cli0.getString(6);
				String numero = cli0.getString(7);
				String bairro = cli0.getString(8);
				String cidade = cli0.getString(9);
				String telefone = cli0.getString(10);
				String celular = cli0.getString(11);
				String cep = cli0.getString(12);
				String email = cli0.getString(13);
				String obs = cli0.getString(14);
				String complemento = cli0.getString(15);

				String sql = "SP_MOBILE_CLIENTE_2 @retornoid = ?, @empresaid = " + banco.EmpresaID + ", @representanteid = " + banco.VendedorID + ", @nome = '" + DB_Sincroniza.RemoveEspeciais(nome) + "', @fantasia = '" + DB_Sincroniza.RemoveEspeciais(fantasia) + "',  @CPF_CNPJ = '" + DB_Sincroniza.RemoveEspeciais(cpfcnpj) + "',  @INSC_EST = '" + DB_Sincroniza.RemoveEspeciais(inscest) + "',  @RESPONSAVEL = '" + DB_Sincroniza.RemoveEspeciais(responsavel) + "',  @ENDERECO = '" + DB_Sincroniza.RemoveEspeciais(endereco) + "', @NUMERO = '" + DB_Sincroniza.RemoveEspeciais(numero) + "', @BAIRRO = '" + DB_Sincroniza.RemoveEspeciais(bairro) + "', @CIDADE = '" + DB_Sincroniza.RemoveEspeciais(cidade) + "', @TELEFONE = '" + DB_Sincroniza.RemoveEspeciais(telefone) + "', @CELULAR = '" + DB_Sincroniza.RemoveEspeciais(celular) + "', @CEP = '" + DB_Sincroniza.RemoveEspeciais(cep) + "', @EMAIL = '" + DB_Sincroniza.RemoveEspeciais(email) + "', @OBS = '" + DB_Sincroniza.RemoveEspeciais(obs) + "', @COMPLEMENTO = '" + DB_Sincroniza.RemoveEspeciais(complemento) + "'";

				exec = new Attribute("exec", sql);
				eCliente.setAttribute(exec);

				eClientes.addContent(eCliente);

			} while (cli0.moveToNext());
		}

		return eClientes;
	}

	private Element criarXMLEstoque() throws Exception, SQLiteException {
		Element eEstoques = new Element("estoques");
		Cursor estoqueCursor = null;
		try {
			estoqueCursor = banco.db.rawQuery("SELECT _id, PRODUTOID, LINHAID, COLUNAID, UNIDADEID, ESTOQUE, OBS, ACRESCIMO, DECRESCIMO FROM ESTOQUE",null);
		} catch (Exception e) {
			estoqueCursor = banco.Sql_SelectErro("ESTOQUE", new String[] { "_id", "PRODUTOID", "LINHAID", "COLUNAID", "UNIDADEID", "ESTOQUE", "OBS" }, "", "");
		}

		while (estoqueCursor.moveToNext()) {

			Element eEstoque = new Element("estoque");
			Attribute exec = null;

			// getColumnIndex(String)
			String clienteid = estoqueCursor.getString(3);

			Attribute aIdCliente = new Attribute("id", estoqueCursor.getString(estoqueCursor.getColumnIndex("_id")));
			eEstoque.setAttribute(aIdCliente);

			String produtoId = estoqueCursor.getString(estoqueCursor.getColumnIndex("PRODUTOID"));
			String linhaId = estoqueCursor.getString(estoqueCursor.getColumnIndex("LINHAID"));
			String colunaId = estoqueCursor.getString(estoqueCursor.getColumnIndex("COLUNAID"));
			String unidadeId = estoqueCursor.getString(estoqueCursor.getColumnIndex("UNIDADEID"));
			double estoque = estoqueCursor.getDouble(estoqueCursor.getColumnIndex("ESTOQUE"));
			String obs = estoqueCursor.getString(estoqueCursor.getColumnIndex("OBS"));
			double acrescimo = estoqueCursor.getDouble(estoqueCursor.getColumnIndex("ACRESCIMO"));
			double decrescimo = estoqueCursor.getDouble(estoqueCursor.getColumnIndex("DECRESCIMO"));

			double valorFinal = (estoque + acrescimo) - decrescimo;

			String sql = "SP_MOBILE_REAJUSTA_ESTOQUE @retornoid = ?, @TIPO_AJUSTE = 'X', @empresaid = " + banco.EmpresaID + ", @OBS = '" + DB_Sincroniza.RemoveEspeciais(obs) + "', @PRODUTOID = " + produtoId + ", @COLUNAID = " + colunaId + ", @LINHAID = " + linhaId + ", @UNIDADEID = " + unidadeId + ", @QTDE = " + valorFinal;

			exec = new Attribute("exec", sql);
			eEstoque.setAttribute(exec);

			eEstoques.addContent(eEstoque);

		}

		return eEstoques;
	}

	private Element criarXMLPedidos() throws Exception, SQLiteException {
		Element ePedidos = new Element("pedidos");

		boolean erro = false;
		// ENVIA OS PEDIDOS PENDENTES
		Cursor cPed;
		try {
			cPed = banco.Sql_SelectErro("VENDAS", new String[] { "_id", "DATA", "CPF_CNPJ", "FORMA_PGTOID", "LISTAID", "OBS", "OPERACAO" }, "SINCRONIZADO = 0 AND CPF_CNPJ NOT LIKE '000.000.000-00' AND CPF_CNPJ NOT LIKE '00.000.000/0000-00' AND SINCRONIZAR = 1", "");
		} catch (Exception e) {
			erro = true;
			cPed = banco.Sql_SelectErro("VENDAS", new String[] { "_id", "DATA", "CPF_CNPJ", "FORMA_PGTOID", "LISTAID", "OBS", "OPERACAO" }, "SINCRONIZADO = 0", "");
		}

		int contador = 0;
		if (cPed.moveToFirst()) {
			do {
				Element eVenda = new Element("venda");

				String pedidoid = cPed.getString(0);
				Attribute aIdPedido = new Attribute("id", pedidoid);
				eVenda.setAttribute(aIdPedido);

				String data = cPed.getString(1);
				String cpf = cPed.getString(2);
				String formaid = cPed.getString(3);
				String listaid = cPed.getString(4);
				String obs = cPed.getString(5);
				String operacao = cPed.getString(6);
				// String origem = cPed.getString(7);

				if (erro) {
					Cursor cPedE = banco.Sql_Select("CLIENTES", new String[] { "CPF_CNPJ" }, "_id = " + cpf, "");

					if (cPedE.moveToFirst()) {
						cpf = cPedE.getString(0);
					}
				}

				// 1) -- INSERE A VENDA COMO CANCELADA POIS CASO DER ERRO E NAO
				// ENVIAR O PEDIDO INTEIRO NAO FICA DISPONIVELS NO SMARTTOOLS
				// PARA EDITAR

				String exec_venda = "SP_MOBILE_VENDA @retornoid = ?, @empresaid = " + banco.EmpresaID + ", @representanteid = " + banco.VendedorID + ", @data = '" + data + "', @cpf_cnpj = '" + cpf + "', @forma_pgtoid = " + formaid + ",  @listaid = " + listaid + ",  @obs = '" + DB_Sincroniza.RemoveEspeciais(obs) + "'" /*
																																																																																 * +
																																																																																 * "', @origem = "
																																																																																 * +
																																																																																 * origem
																																																																																 */;

				Attribute aSqlVenda = new Attribute("exec", exec_venda);
				eVenda.setAttribute(aSqlVenda);

				Element eProdutos = new Element("produtos");

				String exec_item = "";
				Cursor cProd = banco.db.rawQuery("select vendas_itens.produtoid,vendas_itens.unidadeid,vendas_itens.linhaid,vendas_itens.colunaid,vendas_itens.qtde,vendas_itens.valor,vendas_itens.acrescimo,vendas_itens.desconto from vendas_itens where vendas_itens.vendaid = " + String.valueOf(pedidoid), null);
				if (cProd.moveToFirst()) {
					do {
						Element eProduto = new Element("produto");
						exec_item = "SP_MOBILE_VENDA_PRODUTO3 @retornoid = ?, @empresaid = " + banco.EmpresaID + ", @produtoid = " + cProd.getString(0) + ", @unidadeid = " + cProd.getString(1) + ", @linhaid = " + cProd.getString(2) + ",@colunaid = " + cProd.getString(3) + ", @qtde = " + cProd.getString(4).replace(",", ".") + ", @valor = " + cProd.getString(5).replace(",", ".") + ", @acrescimo = " + cProd.getString(6).replace(",", ".") + ",@desconto = " + cProd.getString(7).replace(",", ".") + ",  @operacao = " + operacao + ", @vendaid = ";
						Attribute eSqlProduto = new Attribute("exec", exec_item);
						eProduto.setAttribute(eSqlProduto);

						eProdutos.addContent(eProduto);
					} while (cProd.moveToNext());
				}

				eVenda.addContent(eProdutos);

				Element eFinaliza = new Element("finaliza");

				String exec_vendafim = "SP_MOBILE_VENDA_FINALIZA @retornoid = ?, @empresaid = " + banco.EmpresaID + ", @vendaid = ";
				Attribute eSqlFinaliza = new Attribute("exec", exec_vendafim);
				eFinaliza.setAttribute(eSqlFinaliza);

				eVenda.addContent(eFinaliza);

				ePedidos.addContent(eVenda);

			} while (cPed.moveToNext());
		}

		return ePedidos;

	}

	private Element criarXMLSaldo() throws Exception, SQLiteException {
		Element eSaldos = new Element("saldos");
		// insere no servidor os saldos pendentes do aparelho
		String exec_item = "";
		Cursor cSaldos = banco.Sql_SelectErro("FLEX", new String[] { "_id", "REFERENCIA", "ACRESCIMO", "DESCONTO" }, "SINCRONIZADO = 0", "");
		int contador = 0;
		if (cSaldos.moveToFirst()) {
			do {
				Element eSaldo = new Element("saldo");
				exec_item = "SP_MOBILE_SALDO @retornoid = ?, @empresaid = " + banco.EmpresaID + ", @vendedorid = " + banco.VendedorID + ", @referencia = '" + cSaldos.getString(1) + "', @acrescimo = " + cSaldos.getString(2).replace(",", ".") + ", @desconto = " + cSaldos.getString(3).replace(",", ".");
				Attribute aSqlSaldo = new Attribute("exec", exec_item);
				eSaldo.setAttribute(aSqlSaldo);
				Attribute aSqlId = new Attribute("id", cSaldos.getString(0));
				eSaldo.setAttribute(aSqlId);

				eSaldos.addContent(eSaldo);
			} while (cSaldos.moveToNext() && ((service.MsgErro.equals(""))));
		}

		if (cSaldos != null || !cSaldos.isClosed()) {
			cSaldos.close();
		}

		return eSaldos;
	}

}
