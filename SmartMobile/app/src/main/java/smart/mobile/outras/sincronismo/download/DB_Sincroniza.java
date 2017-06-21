package smart.mobile.outras.sincronismo.download;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import smart.mobile.R;
import smart.mobile.cadastro.pedido.frm_cad_pedido_itens_historico;
import smart.mobile.consulta.metas.frm_cons_metas;
import smart.mobile.consulta.pedido.frm_cons_pedidos;
import smart.mobile.consulta.produtos.frm_cons_produtos;
import smart.mobile.consulta.saldos.frm_cons_saldo;
import smart.mobile.consulta.titulos.frm_cons_titulos;
import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.DB_ServerHost;
import smart.mobile.outras.tela.config.frm_sys_config;
import smart.mobile.utils.DeviceUuidFactory;
import smart.mobile.utils.ZipDownload.DownloadHelper;
import smart.mobile.utils.ZipDownload.UnzipFile;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DB_Sincroniza {

	private static String c_linhas = "#l#"; // SEPARADOR DE REGISTROS = "#l#"
	private static String c_colunas = "#c#"; // SEPARADOR DE COLUNAS = "#c#"
	private static String c_select = "#separadorConsulta#"; // SEPARADOR DE
															// COLUNAS = "#c#"

	public static DB_LocalHost banco;
	private Context context;
	private boolean COnfig_Inicial = false;

	ProgressDialog myProgressDialog = null;
	private Handler handler;
	DB_ServerHost service;

	private String senhaSinc;
	private EditText senha;
	private Dialog login;

	private Calendar tempoInicio;

	private Handler config_handler;
	private Handler principal_handler;
	private Handler pedidos_handler;
	private Handler historico_handler;
	private Handler produtos_handler;
	private Handler titulos_handler;
	private Handler metas_handler;
	private Handler saldos_handler;
	private Handler imagens_handler;

	public DB_Sincroniza(Context context) {

		this.context = context;
		banco = new DB_LocalHost(context);
		service = new DB_ServerHost(context, banco.ServidorOnline, banco.Banco);

		createConfigHandler();
	}

	public DB_Sincroniza(Context context, DB_LocalHost banco) {

		this.context = context;
		this.banco = banco;
		service = new DB_ServerHost(context, banco.ServidorOnline, banco.Banco);

	}

	private void createConfigHandler() {
		config_handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				if (service.MsgErro != "") {
					MostraErro(service.MsgErro);
				} else {
					((frm_sys_config) context).LoadEmpresasVendedores();

					((frm_sys_config) context).salvaConfig();

					if (banco.Banco.toUpperCase().equals("DEMO")) {
						// ja sincroniza o primeiro vendedor automaticamente
						((frm_sys_config) context).Implantar();
						// Toast.makeText(((frm_sys_config)context),"Sincronização concluída, clique em Implantar Vendedor !!!"
						// ,Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(((frm_sys_config) context), "Sincronização concluída !!!", Toast.LENGTH_LONG).show();
					}

					// banco.MostraErro("Sincronização concluída !!!");
					// ((frm_sys_config)context).salvaConfig();
					if (!COnfig_Inicial) {
						((frm_sys_config) context).finish();
					}
				}

			}
		};

		principal_handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				if (service.MsgErro != "") {
					MostraErro(service.MsgErro);
				} else {
					// ((frm_sys_config)context).LoadEmpresasVendedores();

					// ((frm_sys_config)context).salvaConfig();

					// if(banco.Banco.toUpperCase().equals("DEMO")){
					// ja sincroniza o primeiro vendedor automaticamente
					// ((frm_sys_config)context).Implantar();
					// Toast.makeText(((frm_sys_config)context),"Sincronização concluída, clique em Implantar Vendedor !!!"
					// ,Toast.LENGTH_LONG).show();
					// }else{

					if (((Activity) context) instanceof frm_sys_config) {
						((Activity) context).finish();
					}
					Toast.makeText(context, "Sincronização concluída !!!", Toast.LENGTH_LONG).show();
				}

				// banco.MostraErro("Sincronização concluída !!!");
				// ((frm_sys_config)context).salvaConfig();
				// if (!COnfig_Inicial){((frm_sys_config)context).finish();}
				// }

			}
		};

		principal_handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				if (service.MsgErro != "") {
					MostraErro(service.MsgErro);
				} else {
					// ((frm_sys_config)context).LoadEmpresasVendedores();

					// ((frm_sys_config)context).salvaConfig();

					// if(banco.Banco.toUpperCase().equals("DEMO")){
					// ja sincroniza o primeiro vendedor automaticamente
					// ((frm_sys_config)context).Implantar();
					// Toast.makeText(((frm_sys_config)context),"Sincronização concluída, clique em Implantar Vendedor !!!"
					// ,Toast.LENGTH_LONG).show();
					// }else{

					if (((Activity) context) instanceof frm_sys_config) {
						((Activity) context).finish();
					}
					Toast.makeText(context, "Sincronização concluída !!!", Toast.LENGTH_LONG).show();
				}

				// banco.MostraErro("Sincronização concluída !!!");
				// ((frm_sys_config)context).salvaConfig();
				// if (!COnfig_Inicial){((frm_sys_config)context).finish();}
				// }

			}
		};

		historico_handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				if (service.MsgErro != "") {
					MostraErro(service.MsgErro);
				} else
					((frm_cad_pedido_itens_historico) context).CarregaDados();

			}
		};

		produtos_handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				if (service.MsgErro != "") {
					MostraErro(service.MsgErro);
				} else
					((frm_cons_produtos) context).CarregaDados(false, 0);

			}
		};

		titulos_handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				if (service.MsgErro != "") {
					MostraErro(service.MsgErro);
				} else
					((frm_cons_titulos) context).CarregaDados(false);

			}
		};

		metas_handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				if (service.MsgErro != "") {
					MostraErro(service.MsgErro);
				} else
					((frm_cons_metas) context).LoadRelatorio(0);

			}
		};

		saldos_handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				if (service.MsgErro != "") {
					MostraErro(service.MsgErro);
				} else
					((frm_cons_saldo) context).LoadRelatorio(0);

			}
		};

		imagens_handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				if (service.MsgErro != "") {
					MostraErro(service.MsgErro);
				} else
					((frm_cons_produtos) context).CarregaDados(false, 0);

			}
		};

		pedidos_handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				Log.i("XXXXAQUI", service.MsgErro);

				if (service.MsgErro != "") {
					MostraErro(service.MsgErro);
				} else
					((frm_cons_pedidos) context).CarregaDados(false);

			}
		};
	}

	public void MostraErro(String Mensagem) {

		AlertDialog ad = new AlertDialog.Builder(context).create();
		// ad.setCancelable(false); // This blocks the 'BACK' button
		ad.setTitle("SmartMobile - Sincronizando Dados");
		if (Mensagem.indexOf("SqlServer") > -1) {
			ad.setIcon(R.drawable.ic_conn_erro);
		} else {
			ad.setIcon(R.drawable.ico_conexao);
		}
		ad.setMessage(Mensagem);

		// ad.setButton("Ok", new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// dialog.dismiss();
		// }
		// });

		ad.setButton("Testar Conexão", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// dialog.dismiss();
				banco.TestarConexao(context);
			}
		});

		ad.show();

	}

	public static String RemoveEspeciais(String str) {

		/** Troca os caracteres especiais da string por "" **/
		// String[] caracteresEspeciais = {"\\'","\\.", ",", "-", ":", "\\(",
		// "\\)", "ª", "\\|", "\\\\", "°"};
		String[] caracteresEspeciais = { "\\'", "ª", "\\|", "\\\\", "°" };

		for (int i = 0; i < caracteresEspeciais.length; i++) {
			str = str.replaceAll(caracteresEspeciais[i], "");
		}

		/** Troca os espaços no início por "" **/
		str = str.replaceAll("^\\s+", "");

		/** Troca os espaços no início por "" **/
		str = str.replaceAll("\\s+$", "");

		/** Troca os espaços duplicados, tabulações e etc por " " **/
		str = str.replaceAll("\\s+", " ");

		return str;

	}

	// ** procedimentos que conectam ao webservice e inserem os registro no
	// mobile
	// ** procedimentos que conectam ao webservice e inserem os registro no
	// mobile

	private void Sync_Pedidos(long xIDPedido) {
		boolean erro = false;
		// ENVIA OS PEDIDOS PENDENTES
		Cursor cPed;
		if (xIDPedido == 0) { // todos
			try {
				cPed = banco.Sql_Select("VENDAS", new String[] { "_id", "DATA", "CPF_CNPJ", "FORMA_PGTOID", "LISTAID", "OBS", "OPERACAO", "ORIGEM" }, "SINCRONIZADO = 0 AND CPF_CNPJ NOT LIKE '000.000.000-00' AND CPF_CNPJ NOT LIKE '00.000.000/0000-00'", "");
			} catch (Exception e) {
				erro = true;
				cPed = banco.Sql_Select("VENDAS", new String[] { "_id", "DATA", "CPF_CNPJ", "FORMA_PGTOID", "LISTAID", "OBS", "OPERACAO", "ORIGEM" }, "SINCRONIZADO = 0 ", "");
			}
		} else { // somente o selecionado
			try {
				cPed = banco.Sql_Select("VENDAS", new String[] { "_id", "DATA", "CPF_CNPJ", "FORMA_PGTOID", "LISTAID", "OBS", "OPERACAO", "ORIGEM" }, "SINCRONIZADO = 0 AND _id = " + Long.valueOf(xIDPedido), "");

			} catch (SQLiteException e) {
				erro = true;
				cPed = banco.Sql_Select("VENDAS", new String[] { "_id", "DATA", "CPF_CNPJ", "FORMA_PGTOID", "LISTAID", "OBS", "OPERACAO", "ORIGEM" }, "SINCRONIZADO = 0 AND _id = " + Long.valueOf(xIDPedido), "");
			}

		}

		Progresso_Max(cPed.getCount());
		int contador = 0;
		if (cPed.moveToFirst()) {
			do {

				Progresso_Posicao(contador++);
				String voltou = "";

				// getColumnIndex(String)
				String pedidoid = cPed.getString(0);
				String data = cPed.getString(1);
				String cpf = cPed.getString(2);
				String formaid = cPed.getString(3);
				String listaid = cPed.getString(4);
				String obs = cPed.getString(5);
				String operacao = cPed.getString(6);
				String origem = cPed.getString(7);

				if (erro) {
					Cursor cPedE = banco.Sql_Select("CLIENTES", new String[] { "CPF_CNPJ" }, "_id = " + cpf, "");

					if (cPedE.moveToFirst()) {
						cpf = cPedE.getString(0);
					}
				}

				// 1) -- INSERE A VENDA COMO CANCELADA POIS CASO DER ERRO E NAO
				// ENVIAR O PEDIDO INTEIRO NAO FICA DISPONIVELS NO SMARTTOOLS
				// PARA EDITAR

				String exec_venda = "SP_MOBILE_VENDA @retornoid = ?, @empresaid = " + banco.EmpresaID + ", @representanteid = " + banco.VendedorID + ", @data = '" + data + "', @cpf_cnpj = '" + cpf + "', @forma_pgtoid = " + formaid + ",  @listaid = " + listaid + ",  @obs = '" + RemoveEspeciais(obs) + "', @origem = " + origem;
				Log.i("WebService-SP", exec_venda);
				voltou = service.Sql_Executa(exec_venda);
				Log.i("WebService-SP-ret", voltou);

				// 2) -- SEM ERROS ??? >> INSERE OS PRODUTOS DA VENDA CASO NAO
				// TIVER ERROS EM TER INSERIDO O PEDIDO
				if ((service.MsgErro.equals("") && (!voltou.equals("")))) {

					// Toast.makeText(getApplicationContext(),"Nº Venda:" +
					// voltou, Toast.LENGTH_LONG).show();
					String VendaidERP = voltou.trim();

					// ************** ANTIGO MANDAVA UM REQUISICAO POR ITEM
					// ********************************/
					/*
					 * Cursor cProd = banco.Sql_Select("VENDAS_ITENS",new
					 * String[
					 * ]{"PRODUTOID","QTDE","VALOR","ACRESCIMO","DESCONTO"},
					 * "VENDAID = " + pedidoid, "");//, "SINCRONIZADO = 0", "");
					 * if( cProd.moveToFirst()) { do { String exec_item =
					 * "SP_MOBILE_VENDA_PRODUTO2 @retornoid = ?, @empresaid = "
					 * + banco.EmpresaID + ", @vendaid = " + Vendaid +
					 * ", @produtoid = " + cProd.getString(0) + ", @qtde = " +
					 * cProd.getString(1).replace(",",".") + ", @valor = " +
					 * cProd.getString(2).replace(",",".") + ", @acrescimo = " +
					 * cProd.getString(3).replace(",",".") + ",@desconto = " +
					 * cProd.getString(4).replace(",",".") + ",  @operacao = " +
					 * operacao; Log.i("WebService-SP",exec_item);
					 * voltou=service.Sql_Executa(exec_item);
					 * Log.i("WebService-SP-ret",voltou);
					 * 
					 * } while (cProd.moveToNext()); }
					 */

					/* 2013 MANDA TODOS OS ITENS EM UMA REQUISIÇÂO APENAS */
					String exec_item = "";
					// Cursor cProd = banco.Sql_Select("VENDAS_ITENS",new
					// String[]{"PRODUTOID","LINHAID","COLUNAID","QTDE","VALOR","ACRESCIMO","DESCONTO"},
					// "VENDAID = " + pedidoid, "");//, "SINCRONIZADO = 0", "");
					// 27/06/2013 - Jakson Gava - inclusão de grades
					Cursor cProd = banco.db.rawQuery("select vendas_itens.produtoid,vendas_itens.unidadeid,vendas_itens.linhaid,vendas_itens.colunaid,vendas_itens.qtde,vendas_itens.valor,vendas_itens.acrescimo,vendas_itens.desconto from vendas_itens where vendas_itens.vendaid = " + String.valueOf(pedidoid), null);
					if (cProd.moveToFirst()) {
						do {
							exec_item = exec_item + "SP_MOBILE_VENDA_PRODUTO3 @retornoid = ?, @empresaid = " + banco.EmpresaID + ", @vendaid = " + VendaidERP + ", @produtoid = " + cProd.getString(0) + ", @unidadeid = " + cProd.getString(1) + ", @linhaid = " + cProd.getString(2) + ",@colunaid = " + cProd.getString(3) + ", @qtde = " + cProd.getString(4).replace(",", ".") + ", @valor = " + cProd.getString(5).replace(",", ".") + ", @acrescimo = " + cProd.getString(6).replace(",", ".") + ",@desconto = " + cProd.getString(7).replace(",", ".") + ",  @operacao = " + operacao + c_linhas;

						} while (cProd.moveToNext());
					}

					/* >>>> 2013 MANDA TODOS OS ITENS EM UMA REQUISIÇÂO APENAS */
					/* >>>> 2013 MANDA TODOS OS ITENS EM UMA REQUISIÇÂO APENAS */
					Log.i("WebService-SP", exec_item);
					voltou = service.Sql_Executa(exec_item);
					Log.i("WebService-SP-ret", voltou);

					// 3) -- SEM ERROS ??? >> MARCA O PEDIDO COMO 'ABERTO' NO
					// SMARTTOOLS PARA PODER SER VISUALIZADO E FATURADO PELOS
					// USUÁRIOS
					if ((service.MsgErro.equals("") && (!voltou.equals("")))) {

						String exec_vendafim = "SP_MOBILE_VENDA_FINALIZA @retornoid = ?, @empresaid = " + banco.EmpresaID + ", @vendaid = " + VendaidERP;
						Log.i("WebService-SP", exec_vendafim);
						voltou = service.Sql_Executa(exec_vendafim);

						// MARCA O PEDIDO COMO SINCRONIZADO NO SMARTMOBILE
						if ((service.MsgErro.equals("") && (!voltou.equals("")))) {
							ContentValues values = new ContentValues();
							values.put("SINCRONIZADO", 1);
							banco.db.update("VENDAS", values, "_id = " + pedidoid, null);
						}
					}

				}

			} while (cPed.moveToNext() && service.MsgErro.equals(""));
		}

	}

	private void Sync_Clientes_Novos() {

		// ENVIA TODOS OS CLIENTES PENDENTES DO BANCO DE DADOS
		Cursor cli0 = banco.Sql_Select("CLIENTES", new String[] { "_id", "NOME", "FANTASIA", "CPF_CNPJ", "INSC_EST", "RESPONSAVEL", "ENDERECO", "NUMERO", "BAIRRO", "CIDADE", "TELEFONE", "CELULAR", "CEP", "EMAIL", "OBS", "COMPLEMENTO" }, "SINCRONIZADO = 0", "");
		if (cli0.moveToFirst()) {
			do {
				// getColumnIndex(String)
				String clienteid = cli0.getString(0);
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

				service.Sql_Executa("SP_MOBILE_CLIENTE_2 @retornoid = ?, @empresaid = " + banco.EmpresaID + ", @representanteid = " + banco.VendedorID + ", @nome = '" + RemoveEspeciais(nome) + "', @fantasia = '" + RemoveEspeciais(fantasia) + "',  @CPF_CNPJ = '" + RemoveEspeciais(cpfcnpj) + "',  @INSC_EST = '" + RemoveEspeciais(inscest) + "',  @RESPONSAVEL = '" + RemoveEspeciais(responsavel) + "',  @ENDERECO = '" + RemoveEspeciais(endereco) + "', @NUMERO = '" + RemoveEspeciais(numero) + "', @BAIRRO = '" + RemoveEspeciais(bairro) + "', @CIDADE = '" + RemoveEspeciais(cidade) + "', @TELEFONE = '" + RemoveEspeciais(telefone) + "', @CELULAR = '" + RemoveEspeciais(celular) + "', @CEP = '" + RemoveEspeciais(cep) + "', @EMAIL = '" + RemoveEspeciais(email) + "', @OBS = '" + RemoveEspeciais(obs) + "', @COMPLEMENTO = '" + RemoveEspeciais(complemento) + "'");

				// MARCA O CLIENTE COMO SINCRONIZADO
				if (service.MsgErro.equals("")) {
					ContentValues values = new ContentValues();
					values.put("SINCRONIZADO", 1);
					banco.db.update("CLIENTES", values, "_id = " + clienteid, null);
				}

			} while (cli0.moveToNext() && ((service.MsgErro.equals(""))));
		}

	}

	private void Sync_Clientes_NovosString() {
		String retorno = "";
		ArrayList<String> ids = new ArrayList<String>();

		// ENVIA TODOS OS CLIENTES PENDENTES DO BANCO DE DADOS
		Cursor cli0 = banco.Sql_Select("CLIENTES", new String[] { "_id", "NOME", "FANTASIA", "CPF_CNPJ", "INSC_EST", "RESPONSAVEL", "ENDERECO", "NUMERO", "BAIRRO", "CIDADE", "TELEFONE", "CELULAR", "CEP", "EMAIL", "OBS", "COMPLEMENTO" }, "SINCRONIZADO = 0", "");

		Progresso_Max(cli0.getCount());
		int contador = 0;
		if (cli0.moveToFirst()) {
			do {
				Progresso_Posicao(contador++);
				// getColumnIndex(String)
				String clienteid = cli0.getString(3);

				ids.add(clienteid);

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

				retorno += "SP_MOBILE_CLIENTE_2 @retornoid = ?, @empresaid = " + banco.EmpresaID + ", @representanteid = " + banco.VendedorID + ", @nome = '" + RemoveEspeciais(nome) + "', @fantasia = '" + RemoveEspeciais(fantasia) + "',  @CPF_CNPJ = '" + RemoveEspeciais(cpfcnpj) + "',  @INSC_EST = '" + RemoveEspeciais(inscest) + "',  @RESPONSAVEL = '" + RemoveEspeciais(responsavel) + "',  @ENDERECO = '" + RemoveEspeciais(endereco) + "', @NUMERO = '" + RemoveEspeciais(numero) + "', @BAIRRO = '" + RemoveEspeciais(bairro) + "', @CIDADE = '" + RemoveEspeciais(cidade) + "', @TELEFONE = '" + RemoveEspeciais(telefone) + "', @CELULAR = '" + RemoveEspeciais(celular) + "', @CEP = '" + RemoveEspeciais(cep) + "', @EMAIL = '" + RemoveEspeciais(email) + "', @OBS = '" + RemoveEspeciais(obs) + "', @COMPLEMENTO = '" + RemoveEspeciais(complemento) + "'";
				retorno += c_linhas;

			} while (cli0.moveToNext());

			Log.i("", retorno);

			service.Sql_Executa(retorno);

			Progresso_Posicao(0);
			contador = 0;

			// MARCA O CLIENTE COMO SINCRONIZADO
			if (service.MsgErro.equals("")) {
				for (String id : ids) {
					Progresso_Posicao(contador++);
					ContentValues values = new ContentValues();
					values.put("SINCRONIZADO", 1);
					banco.db.update("CLIENTES", values, "CPF_CNPJ like '" + id + "'", null);
				}

			}

		}

	}

	private void Sync_Clientes(Boolean isImplantacao, Boolean SyncHistorico, String sqlCliente, String sqlFormaPagamento, String sqlListaPreco, String sqlListaPrecoProduto, String sqlFormaPagamentoCliente) {

		// RECEBE CADASTRO DE CLIENTES
		String retorno = "";
		Log.i("teste", "clientes - antes do select");

		// INCAS-VENDEDOR : ROBSON
		// if(banco.Banco.toUpperCase().trim().equals("INCAS") &&
		// (banco.VendedorID.equals("50"))){
		// retorno=
		// service.Sql_Select("SELECT NOME,FANTASIA,CPF_CNPJ,INSC_EST,RESPONSAVEL,CIDADE,LOGRADOURO,NUMERO,BAIRRO,COMPLEMENTO,CEP,TELEFONE,CELULAR,EMAIL,OBS,LIMITE_CREDITO,FORMA_PGTOID,LISTA_PRECOID,ATIVO,ULT_DATA,ULT_TOTAL from vw_mobile_clientes where ((vendedorid = "
		// + "17" + ") or (vendedorid = " + "19" + ") or (vendedorid = " +
		// banco.VendedorID + ")) and empresaid = " + banco.EmpresaID +
		// " order by nome desc");
		// }else{
		if (sqlCliente == null) {
			retorno = service.Sql_Select(montarSQLCLiente());
		} else {
			retorno = sqlCliente;
		}

		// }

		Log.i("teste", "clientes - depois do select");

		if (service.MsgErro.equals("")) {
			if (retorno != "" && !retorno.isEmpty()) {

				// REGISTRA O LOG DE RETORNO
				Log.i("WebService-Vw_mobile_clientes", retorno);

				// REMOVE TODOS
				if (isImplantacao)
					banco.TB_CLIENTES_REMOVETODOS();

				// SETA PARA A PROGRESS BAR A QUANTIDADE DE REGISTROS
				String registros[] = retorno.split(c_linhas);

				// PROGRESSO - CLIENTES
				Progresso_Max(registros.length);

				// INSERE TODOS
				for (int i = 0; i < registros.length; i += 1) {

					// PROGRESSO - CLIENTES
					Log.i("WebService-Cliente", registros[i]);
					Progresso_Posicao(i);

					String colunas[] = registros[i].split(c_colunas);

					// VERIFICA SE LOCALIZA O CLIENTE NO SISTEMA PELO CPF_CNPJ,
					// SE NAO FOR 'IMPLANTACAO'
					String xClienteID = "";
					if (!isImplantacao) {

						xClienteID = colunas[2];

					}

					// INSERE OU ATUALIZA NO BANCO DE DADOS
					banco.TB_CLIENTES_INSERIR(xClienteID, colunas[0], colunas[1], colunas[2], colunas[3], colunas[4], colunas[5], colunas[6], colunas[7], colunas[8], colunas[9], colunas[10], colunas[11], colunas[12], colunas[13], colunas[14], colunas[15], colunas[16], colunas[17], 1, Integer.valueOf(colunas[18]), colunas[19], Double.valueOf(colunas[20]));

					// CASO FOR PARA SINCRONIZAR O HISTORICO
					if (SyncHistorico) {
						Sync_HistoricoCliente("", colunas[2]);
					}

				}

			}
		}

		// FORMAS DE PAGAMENTO
		if (service.MsgErro.equals("")) {

			handler.post(new Runnable() {

				public void run() {
					myProgressDialog.dismiss();
					Progresso_Inicia("Recebendo Forma de Pagamento");

				}
			});

			if (sqlFormaPagamento == null) {
				retorno = service.Sql_Select(montarSQLFormasDePagamento());
			} else {
				retorno = sqlFormaPagamento;
			}

			if (retorno != "" && !retorno.isEmpty()) {
				// REGISTRA O LOG DE RETORNO
				Log.i("WebService-vw_mobile_formas_pgto", retorno);

				// REMOVE TODOS
				banco.TB_FORMASPGTO_DELLALL();

				// INSERE TODOS
				String registros[] = retorno.split(c_linhas);
				Progresso_Max(registros.length);

				for (int i = 0; i < registros.length; i += 1) {
					Progresso_Posicao(i);
					String colunas[] = registros[i].split(c_colunas);
					banco.TB_FORMASPGTO_INSERT(colunas[0], colunas[1]);
				}
			}

		}

		// LISTAS DE PREÇOS
		if (service.MsgErro.equals("")) {
			handler.post(new Runnable() {

				public void run() {
					myProgressDialog.dismiss();
					Progresso_Inicia("Recebendo Lista Preco");

				}
			});

			if (sqlListaPreco == null) {
				retorno = service.Sql_Select(montarSQLListaPreco());
			} else {
				retorno = sqlListaPreco;
			}
			if (retorno != "" && !retorno.isEmpty()) {

				// REGISTRA O LOG DE RETORNO
				Log.i("WebService-vw_mobile_listas_precos", retorno);

				// REMOVE TODOS
				banco.TB_LISTASPRECOS_DELLALL();

				// INSERE TODOS
				String registros[] = retorno.split(c_linhas);
				Progresso_Max(registros.length);
				for (int i = 0; i < registros.length; i += 1) {
					Progresso_Posicao(i);
					String colunas[] = registros[i].split(c_colunas);
					banco.TB_LISTASPRECOS_INSERT(colunas[0], colunas[1], colunas[2], colunas[3]);
				}
			}

		}

		// LISTA DE PRECOS ESPECIAIS POR PRODUTO
		if (service.MsgErro.equals("")) {
			handler.post(new Runnable() {

				public void run() {
					myProgressDialog.dismiss();
					Progresso_Inicia("Recebendo Lista Preço por Produto");

				}
			});
			if (sqlListaPrecoProduto == null) {
				// valida clientes que utilizam codigo interno
				retorno = service.Sql_Select(montarSQLListaPrecoProduto());
			} else {
				retorno = sqlListaPrecoProduto;
			}

			if (retorno != "" && !retorno.isEmpty()) {

				// REGISTRA O LOG DE RETORNO
				Log.i("WebService-vw_mobile_listas_precos_produtos", retorno);

				// REMOVE TODOS
				banco.TB_LISTASPRECOSPRODUTOS_DELLALL();

				// INSERE TODOS
				String registros[] = retorno.split(c_linhas);
				Progresso_Max(registros.length);
				for (int i = 0; i < registros.length; i += 1) {
					Progresso_Posicao(i);
					String colunas[] = registros[i].split(c_colunas);
					banco.TB_LISTASPRECOSPRODUTOS_INSERT(colunas[0], colunas[1], colunas[2], colunas[3]);
				}
			}

		}

		// FORMAS DE PAGAMENTOS DO CLIENTES
		if (service.MsgErro.equals("")) {
			handler.post(new Runnable() {

				public void run() {
					myProgressDialog.dismiss();
					Progresso_Inicia("Recebendo Forma de Pagamento por Cliente");

				}
			});

			if (sqlFormaPagamentoCliente == null) {
				retorno = service.Sql_Select(montarSQLFormaPagamentoCliente());
			} else {
				retorno = sqlFormaPagamentoCliente;
			}

			if (retorno != "" && !retorno.isEmpty()) {

				// REGISTRA O LOG DE RETORNO
				Log.i("WebService-vw_mobile_clientes_formas_pgto", retorno);

				// REMOVE TODOS
				banco.TB_CLIENTES_FORMAS_PGTO_DELLALL();

				// INSERE TODOS
				String registros[] = retorno.split(c_linhas);
				Progresso_Max(registros.length);
				for (int i = 0; i < registros.length; i += 1) {
					Progresso_Posicao(i);
					String colunas[] = registros[i].split(c_colunas);
					banco.TB_CLIENTES_FORMAS_PGTO_INSERT(colunas[0], colunas[1], colunas[2]);
				}
			}
		}

	}

	private void Sync_HistoricoCliente(String clienteID, String cpfcnpj) {

		// busca o cpf/cnpj do cliente
		Cursor cCli = banco.Sql_Select("CLIENTES", new String[] { "_id", "CPF_CNPJ" }, "cpf_cnpj = '" + cpfcnpj + "'", "");
		if (cCli.moveToFirst()) {
			do {
				// armazena o cpf/cnpj
				clienteID = cCli.getString(1);
				String CpfCnpj = cCli.getString(1);

				// busca os ultimo produtos comprados pelo cliente
				String retorno = "";

				String campocodigo = "itens_venda.produtoid";
				if (banco.Banco.toUpperCase().trim().equals("INCAS") || banco.Banco.toUpperCase().trim().equals("SIAL")) {
					campocodigo = "produtos.codigo";
				}

				retorno = service.Sql_Select("select " + campocodigo + ",itens_venda.qtde,itens_venda.valor,itens_venda.acrescimo,itens_venda.desconto,itens_venda.linhaid,itens_venda.colunaid,itens_venda.unidadeid from itens_venda " + "inner join produtos on itens_venda.produtoid = produtos.produtoid " + "where " + "vendaid in (select top 1 vendaid from vendas v inner join clientes c on v.CPF_CNPJ = c.CPF_CNPJ and v.empresaid = itens_venda.empresaid and v.situacao = 9 and c.cpf_cnpj = '" + CpfCnpj + "' order by data_ped desc) " + "and empresaid = " + banco.EmpresaID);
				if (retorno != "" && !retorno.isEmpty()) {

					if (service.MsgErro.equals("")) {

						// remove todos
						Log.i("problema", "retornou");
						banco.db.delete("CLIENTES_HISTORICO", "CPF_CNPJ like '" + String.valueOf(clienteID) + "'", null);

						// insere todos
						Log.i("problema", "retornou=" + retorno);

						String registros[] = retorno.split(c_linhas);
						for (int i = 0; i < registros.length; i += 1) {

							String colunas[] = registros[i].split(c_colunas);
							banco.TB_CLIENTE_HISTORICO_INSERE(clienteID, Long.valueOf(colunas[0]), colunas[1], colunas[2], colunas[3], colunas[4], Long.valueOf(colunas[5]), Long.valueOf(colunas[6]), Long.valueOf(colunas[7]));
						}

					}

				}

			} while (cCli.moveToNext() && ((service.MsgErro.equals(""))));
		}

	}

	private void Sync_Produtos(String sqlProduto, String sqlImposto) {
		String retorno = "";
		if (sqlProduto == null) {
			// produtos ativos e marcados para exportar mobile
			retorno = service.Sql_Select(montarSQLProduto());
		} else {
			retorno = sqlProduto;
		}

		// if(CodInterno){
		// retorno =
		// service.Sql_Select("select codigo,descricao,grupo,un,desconto_max,valor,estoque from vw_mobile_produtos where empresaid = "
		// + banco.EmpresaID);}
		// else{
		// retorno =
		// service.Sql_Select("select produtoid,descricao,grupo,un,desconto_max,valor,estoque from vw_mobile_produtos where empresaid = "
		// + banco.EmpresaID);
		// }

		if (retorno != "" && !retorno.isEmpty()) {

			if (service.MsgErro.equals("")) {

				// remove todos
				banco.db.delete("PRODUTOS", null, null);

				// insere todos
				String registros[] = retorno.split(c_linhas);

				// PROGRESSO - PRODUTOS
				Progresso_Max(registros.length);

				for (int i = 0; i < registros.length; i += 1) {

					// PROGRESSO - PRODUTOS
					Log.i("WebService-Produto", registros[i]);
					Progresso_Posicao(i);

					// Log.i("WebService-Produto","antes do split");

					String colunas[] = registros[i].split(c_colunas);

					// Log.i("WebService-Produto","depois do split");
					/*
					 * Log.i("WebService-Produto",colunas[0]);
					 * Log.i("WebService-Produto",colunas[1]);
					 * Log.i("WebService-Produto",colunas[2]);
					 * Log.i("WebService-Produto",colunas[3]);
					 * Log.i("WebService-Produto",colunas[4]);
					 * Log.i("WebService-Produto",colunas[5]);
					 * Log.i("WebService-Produto",colunas[6]);
					 * Log.i("WebService-Produto",colunas[7]);
					 * Log.i("WebService-Produto",colunas[8]);
					 * Log.i("WebService-Produto",colunas[9].toString());
					 * Log.i("WebService-Produto",colunas[10].toString());
					 */

					try {

						banco.Produto_Insere(Integer.valueOf(colunas[0]), colunas[1], colunas[2], colunas[3], colunas[4], Double.valueOf(colunas[5]), Double.valueOf(colunas[6]), Double.valueOf(colunas[7]), Integer.valueOf(colunas[8]), Integer.valueOf(colunas[9]), colunas[10], colunas[11], Integer.valueOf(colunas[12]), Double.valueOf(colunas[13]));

					} catch (Exception e) {
						Log.i("WebService-Produto_Insere", e.getMessage());
					}
				}

			}
		}

		// IMPOSTOS
		if (service.MsgErro.equals("")) {

			if (sqlImposto == null) {
				retorno = service.Sql_Select(montarSQLImposto());
			} else {
				retorno = sqlImposto;
			}

			if (retorno != "" && !retorno.isEmpty()) {
				// REGISTRA O LOG DE RETORNO
				Log.i("WebService-vw_mobile_impostos", retorno);

				// REMOVE TODOS
				banco.TB_IMPOSTOS_DELLALL();

				// INSERE TODOS
				String registros[] = retorno.split(c_linhas);
				for (int i = 0; i < registros.length; i += 1) {
					String colunas[] = registros[i].split(c_colunas);
					banco.TB_IMPOSTOS_INSERT(Integer.valueOf(colunas[0]), colunas[1], Double.valueOf(colunas[2]), Double.valueOf(colunas[3]), Double.valueOf(colunas[4]));
				}
			}

		}

	}

	private void Sync_Titulos(String sqlTitulo) {

		// apenas titulos pendentes
		String retorno = "";
		if (sqlTitulo == null) {
			retorno = service.Sql_Select(montarSQLTitulo());
		} else {
			retorno = sqlTitulo;
		}

		if (retorno != "" && !retorno.isEmpty()) {
			if (service.MsgErro.equals("")) {
				// remove todos
				banco.db.delete("TITULOS", null, null);

				// insere todos
				String registros[] = retorno.split(c_linhas);

				// PROGRESSO - PENDENCIAS
				Progresso_Max(registros.length);

				for (int i = 0; i < registros.length; i += 1) {

					// PROGRESS0 - PENDENCIAS
					Progresso_Posicao(i);

					String colunas[] = registros[i].split(c_colunas);
					banco.TB_TITULOS_INSERE(colunas[0], colunas[1], colunas[2], colunas[3], colunas[4], colunas[5], colunas[6], colunas[7]);
				}
			}
		}
	}

	private void Sync_Imagens() {
		try {
			// apenas titulos pendentes
			String retorno = "";
			retorno = service.Sql_DownloadImage(banco.EmpresaID);

			File folder = new File("/data/data/smart.mobile/imagens");

			DeleteRecursive(folder);

			// if (folder != null)
			// {
			// String[] entries = folder.list();
			// if (entries != null)
			// {
			// if (entries.length > 0)
			// {
			// for (String s : entries)
			// {
			// File currentFile = new File(folder.getPath(), s);
			// currentFile.delete();
			// }
			// }
			// }
			// }

			if (retorno.equals(banco.EmpresaID + "")) {
				DownloadHelper dow = new DownloadHelper();
				dow.setDB(this);
				if (banco.ServidorOnline.contains(":")) {
					dow.DownloadFromUrl("http://" + banco.ServidorOnline + "/axis/teste.zip", "teste.zip");
				} else {
					dow.DownloadFromUrl("http://" + banco.ServidorOnline + ":8080/axis/teste.zip", "teste.zip");
				}
				UnzipFile fil = new UnzipFile();
				fil.unZipIt("teste.zip", "");

			}
		} catch (Exception e) {
			e.printStackTrace();
			service.MsgErro = e.getMessage();
		}

	}

	public void DeleteRecursive(File fileOrDirectory) {

		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				DeleteRecursive(child);

		fileOrDirectory.delete();

	}

	private void Sync_Metas(String sqlMetas) {
		String retorno = "";
		if (sqlMetas == null) {
			// apenas metas por mes e do ano corrente
			retorno = service.Sql_Select(montarSQLMetas());
		} else {
			retorno = sqlMetas;
		}

		if (retorno != "" && !retorno.isEmpty()) {

			if (service.MsgErro.equals("")) {

				// remove todos
				banco.db.delete("METAS", null, null);

				// insere todos
				String registros[] = retorno.split(c_linhas);
				for (int i = 0; i < registros.length; i += 1) {

					Log.i("WebService-Meta", registros[i]);

					String colunas[] = registros[i].split(c_colunas);
					banco.TB_META_INSERE(Long.valueOf(colunas[0]), Double.valueOf(colunas[1]), Double.valueOf(colunas[2]));
				}

			}

		}

	}

	private void Sync_Saldos(String sqlSaldo) {
		try {
			Log.i("xx", "aqui1");

			// insere no servidor os saldos pendentes do aparelho
			String exec_item = "";
			Cursor cSaldos = banco.Sql_Select("FLEX", new String[] { "_id", "REFERENCIA", "ACRESCIMO", "DESCONTO" }, "SINCRONIZADO = 0", "");
			Progresso_Max(cSaldos.getCount() + 20);
			int contador = 0;
			if (cSaldos.moveToFirst()) {
				do {
					Progresso_Posicao(contador++);
					exec_item = exec_item + "SP_MOBILE_SALDO @retornoid = ?, @empresaid = " + banco.EmpresaID + ", @vendedorid = " + banco.VendedorID + ", @referencia = '" + cSaldos.getString(1) + "', @acrescimo = " + cSaldos.getString(2).replace(",", ".") + ", @desconto = " + cSaldos.getString(3).replace(",", ".") + c_linhas;
				} while (cSaldos.moveToNext() && ((service.MsgErro.equals(""))));
			}

			Log.i("xx", "aqui2");

			if (cSaldos != null || !cSaldos.isClosed()) {
				cSaldos.close();
			}

			Log.i("xx", "aqui3");

			// 2013 MANDA TODOS OS ITENS EM UMA REQUISIÇÂO APENAS
			String voltou = "";
			if (!exec_item.equals("")) {
				Log.i("WebService-SP", exec_item);
				contador += 10;
				Progresso_Posicao(contador);
				voltou = service.Sql_Executa(exec_item);
				Log.i("WebService-SP-ret", voltou);
				contador += 10;
				Progresso_Posicao(contador);
			}

			Log.i("xx", "aqui4");

			// 3) -- SEM ERROS ??? >> MARCA O PEDIDO COMO 'ABERTO' NO SMARTTOOLS
			// PARA PODER SER VISUALIZADO E FATURADO PELOS USUÁRIOS
			if ((service.MsgErro.equals(""))) {

				Log.i("xx", "aqui5");

				// atualiza todos para sincronizados
				SQLiteStatement insertStmtx = banco.db.compileStatement("update FLEX set SINCRONIZADO = 1 where SINCRONIZADO = 0");
				insertStmtx.executeInsert();

				Log.i("xx", "aqui6");

				String retorno = "";
				if (sqlSaldo == null) {
					// historico de saldos que consta no smarttools
					retorno = service.Sql_Select(montarSQLSaldo());
				} else {
					retorno = sqlSaldo;
				}
				if (retorno != "" && !retorno.isEmpty()) {

					if (service.MsgErro.equals("")) {

						// remove todos
						banco.db.delete("FLEX", null, null);

						Log.i("xx", "aqui7");

						// insere todos
						String registros[] = retorno.split(c_linhas);
						Progresso_Max(registros.length);
						for (int i = 0; i < registros.length; i += 1) {
							Progresso_Posicao(i);
							Log.i("WebService-Saldo", registros[i]);

							// MARCA QUE CONTROLA SALDO
							banco.temp_cSaldoFlex = true;

							String colunas[] = registros[i].split(c_colunas);
							banco.TB_SALDO_INSERE(colunas[0], colunas[1], Double.valueOf(colunas[2]), Double.valueOf(colunas[3]), Double.valueOf(colunas[4]), 1, null);
						}

					}

				} else {
					// remove todos
					banco.temp_cSaldoFlex = false;
					banco.temp_SaldoFlex = 0.00;
					banco.db.delete("FLEX", null, null);
				}

				// atualiza o saldo atual carregado
				banco.DB_SaldoFlexLoad();
				// Progresso_Posicao(contador + 20);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ** procedimentos para controlar o progresso da sincronização de registros
	// ** procedimentos para controlar o progresso da sincronização de registros

	private void Progresso_Inicia(String mensagem) {

		try {
			myProgressDialog = new ProgressDialog(context);
			myProgressDialog.setIcon(R.drawable.ico_refresh);
			myProgressDialog.setTitle("Sincronizando");
			myProgressDialog.setMessage(mensagem);
			myProgressDialog.setIndeterminate(false);
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			myProgressDialog.setProgress(0);
			myProgressDialog.setCancelable(false);
			myProgressDialog.setCanceledOnTouchOutside(false);
			myProgressDialog.show();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void Progresso_Max(int registros) {

		try {
			myProgressDialog.setMax(registros);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void Progresso_Posicao(int index) {

		try {
			myProgressDialog.setProgress(index + 1);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void Progresso_Fim() {

		try {
			myProgressDialog.dismiss();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	// ** procedimentos em threads para controle do progresso da sincronização
	// ** procedimentos em threads para controle do progresso da sincronização

	// implantação
	public boolean Syncroniza_EmpresasVendedores() {

		// handler = new Handler();
		// Thread thread = new Sync_Clientes();
		// thread.start();

		COnfig_Inicial = true;
		myProgressDialog = ProgressDialog.show(context, "Sincronizando", "Empresas/Vendedores, aguarde ...");

		new Thread() {

			public void run() {
				try {
					// REMOVE TUDO
					banco.DB_CLEAR(false);

					// EMPRESAS
					String retorno = service.Sql_Select("select empresaid, razao from vw_mobile_empresas order by empresaid asc");

					if (retorno != "" && !retorno.isEmpty()) {
						String registros[] = retorno.split(c_linhas);
						for (int i = 0; i < registros.length; i += 1) {

							String colunas[] = registros[i].split(c_colunas);
							banco.DB_EMPRESA_INSERE(colunas[0], colunas[1]);

						}
					}

					// VENDEDORES
					if (service.MsgErro.equals("")) {

						retorno = service.Sql_Select("select vendedorid, nome, gerente, coletor from vw_mobile_vendedores order by vendedorid asc");
						if (retorno != "" && !retorno.isEmpty()) {
							String registros[] = retorno.split(c_linhas);
							for (int i = 0; i < registros.length; i += 1) {

								String colunas[] = registros[i].split(c_colunas);
								banco.DB_VENDEDOR_INSERE(colunas[0], colunas[1], colunas[2], colunas[3]);

							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				config_handler.sendEmptyMessage(0);
				myProgressDialog.dismiss();
			}

		}.start();
		return true;

	}//

	public boolean Syncroniza_Implantacao() {

		myProgressDialog = ProgressDialog.show(context, "Clientes/Produtos", "Sincronizando, aguarde ...");

		new Thread() {

			public void run() {

				try {

					// REMOVE TUDO
					banco.DB_CLEAR(true);

					if (banco.ServidorOnline.equals("demo.amsoft.com.br:3390") && banco.Banco.equals("demo")) {
						Sync_Imagens();
					}

					Sync_Clientes(true, false, null, null, null, null, null);
					if (service.MsgErro.equals("")) {

						Sync_Produtos(null, null);
						if (service.MsgErro.equals("")) {

							Sync_Titulos(null);
							if (service.MsgErro.equals("")) {

								Sync_Saldos(null);
							}
						}

					}

				} catch (Exception e) {
				}
				config_handler.sendEmptyMessage(0);
				myProgressDialog.dismiss();
			}

		}.start();

		return true;

	}

	// 20/08/2014 - JAKSON GAVA - sincronização unificada

	public boolean Syncroniza_Geral(final boolean isImplantacao, final boolean syncImagens) {

		login = new Dialog(context);
		login.setContentView(R.layout.frm_login_dialog);
		login.setTitle("Digite sua Senha:");
		login.setCancelable(true);
		login.setCanceledOnTouchOutside(false);

		Button sincronizarComSenha = (Button) login.findViewById(R.id.btnLogin);
		Button cancelar = (Button) login.findViewById(R.id.btnCancel);
		Button sincronizarSemSenha = (Button) login.findViewById(R.id.btnSincSemSenha);

		senha = (EditText) login.findViewById(R.id.edtPass);

		senha.setInputType(InputType.TYPE_CLASS_TEXT);
		senha.setTransformationMethod(PasswordTransformationMethod.getInstance());

		myProgressDialog = ProgressDialog.show(context, "Sincronização Geral", "Verificando Senha!");

		sincronizarSemSenha.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				funcaoSincGeral(isImplantacao,syncImagens);

			}
		});

		cancelar.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				login.dismiss();
				myProgressDialog.dismiss();
			}
		});

		sincronizarComSenha.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				handler = new Handler();
				Runnable run = new Runnable() {

					public void run() {
						senhaSinc = senha.getText().toString();

						if (verificarLoginSenha(true))// verifica login, true -
														// ok / false - erro
						{
							handler.post(new Runnable() {

								public void run() {
									login.dismiss();

								}
							});

							tempoInicio = Calendar.getInstance();

							metodoSinc(isImplantacao,syncImagens, null);// executa todas as
															// sinc

							handler.post(new Runnable() {

								public void run() {
									principal_handler.sendEmptyMessage(0);
									myProgressDialog.dismiss();
									if (login.isShowing()) {
										login.dismiss();
									}
								}
							});

						} else {
							handler.post(new Runnable() {

								public void run() {
									Toast.makeText(context, "Senha Inválida!", Toast.LENGTH_LONG).show();
									myProgressDialog.dismiss();
								}
							});
						}

					}

				};

				new Thread(run).start();

			}
		});

//		if (!banco.Banco.equalsIgnoreCase("demo")) {
//			login.show();
//		} else {
			funcaoSincGeral(isImplantacao,syncImagens);
//
//		}
		return true;

	}

	public void funcaoSincGeral(final boolean isImplantacao, final boolean xSincImagens) {
		handler = new Handler();
		Runnable run = new Runnable() {

			public void run() {
				senhaSinc = senha.getText().toString();

				if (verificarLoginSenha(false))// verifica senha - true
												// ok/ false - erro
				{

					handler.post(new Runnable() {

						public void run() {
							login.dismiss();

						}
					});
					tempoInicio = Calendar.getInstance();

					metodoSinc(isImplantacao,xSincImagens,  null);// executa todas as sinc

					handler.post(new Runnable() {

						public void run() {
							principal_handler.sendEmptyMessage(0);
							myProgressDialog.dismiss();
						}
					});

				} else {
					handler.post(new Runnable() {

						public void run() {
							Toast.makeText(context, "Usuário possui senha cadastrada!", Toast.LENGTH_LONG).show();
							myProgressDialog.dismiss();
						}
					});
				}

			}

		};

		new Thread(run).start();
	}

	// 27/06/2014 - JAKSON GAVA - sincronização para smartgroup - status dos
	// aparelhos

	// pedidos
	public boolean Syncroniza_Pedidos(final long IDPedido) {

		// handler = new Handler();
		// Thread thread = new Sync_Clientes();
		// thread.start();

		if (IDPedido == 0) {
			myProgressDialog = ProgressDialog.show(context, "Pedidos", "Sincronizando, aguarde ...");
		} else {
			myProgressDialog = ProgressDialog.show(context, "Pedido Nº" + Long.valueOf(IDPedido), "Sincronizando, aguarde ...");
		}

		myProgressDialog.setCancelable(false);
		myProgressDialog.setCanceledOnTouchOutside(false);

		new Thread() {

			public void run() {
				try {

					Log.i("ANTES", service.MsgErro);

					// ENVIA TODOS OS CLIENTES PENDENTES DO BANCO DE DADOS
					Sync_Clientes_Novos();

					if (service.MsgErro.equals("")) {
						Sync_Pedidos(IDPedido);

						if (service.MsgErro.equals("")) {

							if (banco.temp_cSaldoFlex) {
								Sync_Saldos(null);
							}

						}

					}

					Log.i("DEpois", service.MsgErro);

				} catch (Exception e) {

				}
				pedidos_handler.sendEmptyMessage(0);
				myProgressDialog.dismiss();

			}

		}.start();
		return true;

	}//

	// clientes
	public boolean Syncroniza_Clientes(Boolean xIsImplantacao, final Boolean xSyncHistoricos) {

		Progresso_Inicia("Clientes, aguarde ...");

		new Thread() {

			public void run() {

				try {

					// ENVIA TODOS OS CLIENTES PENDENTES DO BANCO DE DADOS
					Sync_Clientes_Novos();

					// RECEBE TODOS NOVAMENTE DO SMARTTOOLS
					if (service.MsgErro.equals("")) {
						Sync_Clientes(false, xSyncHistoricos, null, null, null, null, null);
					}

				} catch (Exception e) {
				}

				// clientes_handler.sendEmptyMessage(0);
				Progresso_Fim();

			}

		}.start();
		return true;

	}//

	// historico
	public boolean Syncroniza_Historico(final String clienteID) {

		// handler = new Handler();
		// Thread thread = new Sync_Clientes();
		// thread.start();

		myProgressDialog = ProgressDialog.show(context, "Histórico do Cliente", "Sincronizando, aguarde ...");
		myProgressDialog.setCancelable(false);
		myProgressDialog.setCanceledOnTouchOutside(false);

		new Thread() {

			public void run() {
				try {

					Sync_HistoricoCliente(clienteID, "");

				} catch (Exception e) {
				}
				historico_handler.sendEmptyMessage(0);
				myProgressDialog.dismiss();
			}

		}.start();
		return true;

	}//

	// produtos
	public boolean Syncroniza_Produtos() {

		Progresso_Inicia("Produtos, aguarde ...");

		new Thread() {

			public void run() {
				try {

					Sync_Produtos(null, null);

				} catch (Exception e) {
				}
				produtos_handler.sendEmptyMessage(0);
				Progresso_Fim();
			}

		}.start();
		return true;

	}//

	// IMAGES
	public boolean Syncroniza_Imagens() {

		Progresso_Inicia("Cadastros + Imagens, aguarde ...");

		new Thread() {

			public void run() {
				try {
					Sync_Produtos(null, null);
					Sync_Imagens();

				} catch (Exception e) {
				}
				imagens_handler.sendEmptyMessage(0);
				Progresso_Fim();
			}

		}.start();
		return true;

	}//

	// pendencias
	public boolean Syncroniza_Titulos() {

		Progresso_Inicia("Pendências, aguarde ...");

		new Thread() {

			public void run() {
				try {

					Sync_Titulos(null);

				} catch (Exception e) {
				}
				titulos_handler.sendEmptyMessage(0);
				Progresso_Fim();
			}

		}.start();
		return true;

	}//

	// metas
	public boolean Syncroniza_Metas() {

		// handler = new Handler();
		// Thread thread = new Sync_Clientes();
		// thread.start();

		myProgressDialog = ProgressDialog.show(context, "Metas de Vendas", "Sincronizando, aguarde ...");
		myProgressDialog.setCancelable(false);
		myProgressDialog.setCanceledOnTouchOutside(false);

		new Thread() {

			public void run() {
				try {

					Sync_Metas(null);

				} catch (Exception e) {
				}
				metas_handler.sendEmptyMessage(0);
				myProgressDialog.dismiss();
			}

		}.start();
		return true;

	}//

	// saldos
	public boolean Syncroniza_Saldos() {

		// handler = new Handler();
		// Thread thread = new Sync_Clientes();
		// thread.start();

		myProgressDialog = ProgressDialog.show(context, "Saldo Flex", "Sincronizando, aguarde ...");
		myProgressDialog.setCancelable(false);
		myProgressDialog.setCanceledOnTouchOutside(false);

		new Thread() {

			public void run() {
				try {

					Sync_Saldos(null);

				} catch (Exception e) {
				}
				saldos_handler.sendEmptyMessage(0);
				myProgressDialog.dismiss();
			}

		}.start();
		return true;

	}//

	public String montarSQLCLiente() {
		String retorno = "";

		if (banco.VendedorGerente) { // BUSCA TODOS OS CLIENTES
			retorno = "SELECT NOME, FANTASIA,CPF_CNPJ,INSC_EST,RESPONSAVEL,CIDADE,LOGRADOURO as ENDERECO,NUMERO,BAIRRO,COMPLEMENTO,CEP,TELEFONE,CELULAR,EMAIL,OBS,LIMITE_CREDITO as LIMITE,FORMA_PGTOID,LISTA_PRECOID as LISTAID,SINCRONIZADO,ULT_DATA,ULT_TOTAL from vw_mobile_clientes where empresaid = " + banco.EmpresaID /*
																																																																																		 * +
																																																																																		 * " order by nome desc"
																																																																																		 */;
		} else { // BUSCA CLIENTES DO VENDEDOR
			retorno = "SELECT NOME, FANTASIA,CPF_CNPJ,INSC_EST,RESPONSAVEL,CIDADE,LOGRADOURO as ENDERECO,NUMERO,BAIRRO,COMPLEMENTO,CEP,TELEFONE,CELULAR,EMAIL,OBS,LIMITE_CREDITO as LIMITE,FORMA_PGTOID,LISTA_PRECOID as LISTAID,SINCRONIZADO,ULT_DATA,ULT_TOTAL from vw_mobile_clientes where vendedorid = " + banco.VendedorID + " and empresaid = " + banco.EmpresaID /*
																																																																																										 * +
																																																																																										 * " order by nome desc"
																																																																																										 */;
		}

		return retorno;
	}

	public String montarSQLFormasDePagamento() {
		return "select forma_pgtoid, ((cast(forma_pgtoid as nvarchar(10))) + ' - '+ descricao) as descricao from vw_mobile_formas_pgto";
	}

	public String montarSQLListaPreco() {
		return "select LISTAID, ((cast(LISTAID as nvarchar(10))) + ' - '+ descricao) as DESCRICAO, TIPO_LISTA, PERCENTUAL from vw_mobile_listas_precos";
	}

	public String montarSQLListaPrecoProduto() {
		String retorno = "";

		Boolean CodInterno = false;
		if (banco.Banco.toUpperCase().trim().equals("INCAS") || banco.Banco.toUpperCase().trim().equals("SIAL")) {
			CodInterno = true;
		}

		if (CodInterno) {
			retorno = "select LISTAID, CODIGO, TIPO, PERCENTUAL from vw_mobile_listas_precos_produtos where empresaid = " + banco.EmpresaID;
		} else {
			retorno = "select LISTAID, PRODUTOID, TIPO, PERCENTUAL from vw_mobile_listas_precos_produtos where empresaid = " + banco.EmpresaID;
		}

		return retorno;
	}

	public String montarSQLFormaPagamentoCliente() {
		String retorno = "";

		if (banco.VendedorGerente) { // BUSCA TODOS OS CLIENTES
			retorno = "select cpf_cnpj, padrao, forma_pgtoid from vw_mobile_clientes_formas_pgto where empresaid = " + banco.EmpresaID + " and cpf_cnpj NOT LIKE '000.000.000-00' and cpf_cnpj NOT LIKE '00.000.000/0000-00'";
		} else {// BUSCA CLIENTES DO VENDEDOR
			retorno = "select cpf_cnpj, padrao, forma_pgtoid from vw_mobile_clientes_formas_pgto where vendedorid = " + banco.VendedorID + " and empresaid = " + banco.EmpresaID + " and cpf_cnpj NOT LIKE '000.000.000-00' and cpf_cnpj NOT LIKE '00.000.000/0000-00'";
		}

		return retorno;
	}

	public String montarSQLProduto() {
		String retorno = "";
		Boolean CodInterno = false;

		//view para consulta de produtos
		String Vw_Consulta = "vw_mobile_produtos2";

		//view para consulta de produtos, onde ESTOQUE = 'ESTOQUE_REAL' [exige mais processamento]
		if (banco.Banco.toUpperCase().trim().equals("TOCARI")) {
			Vw_Consulta = "vw_mobile_produtos2_tocari";
		} else if (banco.Banco.toUpperCase().trim().equals("GRAFFITATACADO")) {
			Vw_Consulta = "vw_mobile_produtos2_tocari";
		}

		// valida clientes que utilizam codigo interno
		if (banco.Banco.toUpperCase().trim().equals("INCAS") || banco.Banco.toUpperCase().trim().equals("SIAL")) {
			CodInterno = true;
		}

		String campoCodigo = "produtoid";
		if (CodInterno) {
			campoCodigo = "codigo";
		}

		// incas não busca UNIDADES SECUNDÁRIAS
		// if (banco.Banco.toUpperCase().trim().equals("INCAS")) {
		// retorno = "select " + campoCodigo +
		// ",descricao,grupo,unidadeid,un as UND,desconto_max as DESC_MAX,valor,estoque,linhaid,colunaid,linha,coluna,impostoid,aliquota_ipi,destaque, peso, codigo from "
		// + Vw_Consulta + " where empresaid = " + banco.EmpresaID +
		// " and fator = 1";
		// } else {
		retorno = "select " + campoCodigo + ",descricao,grupo,unidadeid,un as UND,desconto_max as DESC_MAX,valor,estoque,linhaid,colunaid,linha,coluna,impostoid,aliquota_ipi,destaque, peso, codigo, codigo_barra, fator, marca from " + Vw_Consulta + " where empresaid = " + banco.EmpresaID;
		// }

		String[] retornoSecundaria = service.Sql_Select("SELECT SMARTMOBILE_UNIDADES_SECUNDARIAS from VW_MOBILE_EMPRESAS where empresaid = " + banco.EmpresaID).split("#l#");
		retornoSecundaria = retornoSecundaria[0].split("#c#");
		if (Integer.parseInt(retornoSecundaria[0]) == 0) {
			retorno += " AND fator = 1";
		}
		
		return retorno;
	}

	public String montarSQLImposto() {
		return "select impostoid,uf,aliquota_uf,subs_aliq,subs_iva from vw_mobile_impostos";
	}

	public String montarSQLTitulo() {
		String retorno = "";

		if (banco.VendedorGerente) { // BUSCA TODOS OS CLIENTES
			retorno = "SELECT TIPO,CPF_CNPJ as NOME,CODIGO,DOCUMENTO,EMISSAO,VENCIMENTO,VALOR,HISTORICO from vw_mobile_titulos where empresaid = " + banco.EmpresaID + " order by VENCIMENTO_ORDER asc";
		} else {
			retorno = "SELECT TIPO,CPF_CNPJ as NOME,CODIGO,DOCUMENTO,EMISSAO,VENCIMENTO,VALOR,HISTORICO from vw_mobile_titulos where vendedorid = " + banco.VendedorID + " and empresaid = " + banco.EmpresaID + " order by VENCIMENTO_ORDER asc";
		}

		return retorno;
	}

	private String montarSQLMetas() {
		return "SELECT MES,META,TOTAL from vw_mobile_metas where vendedorid = " + banco.VendedorID + " and empresaid = " + banco.EmpresaID + " order by mes asc";
	}

	public String montarSQLConfDinamic() {
		return "SELECT SMARTMOBILE_UNIDADES_SECUNDARIAS, SMARTMOBILE_VALIDA_ESTOQUE FROM VW_MOBILE_EMPRESAS where empresaid = " + banco.EmpresaID;
	}

	public String montarSQLSaldo() {
		return "SELECT DATA,REFERENCIA,ACRESCIMO,DESCONTO,SALDO FROM VW_MOBILE_SALDOS where vendedorid = " + banco.VendedorID + " and empresaid = " + banco.EmpresaID;
	}

	private String montarSQLLogin() {
		if (senhaSinc.equals("NULL")) {
			return "SELECT vendedorid from VW_MOBILE_VENDEDORES where vendedorid = " + banco.VendedorID + " and senha is null";
		} else {
			return "SELECT vendedorid from VW_MOBILE_VENDEDORES where vendedorid = " + banco.VendedorID + " and (pwdCompare('" + senhaSinc + "', senha, 0) = 1)";
		}
	}

	private String montarSQLFinal() {
		StringBuilder retorno = new StringBuilder();

		retorno.append(montarSQLCLiente());
		retorno.append(c_select);
		retorno.append(montarSQLFormasDePagamento());
		retorno.append(c_select);
		retorno.append(montarSQLListaPreco());
		retorno.append(c_select);
		retorno.append(montarSQLListaPrecoProduto());
		retorno.append(c_select);
		retorno.append(montarSQLFormaPagamentoCliente());
		retorno.append(c_select);
		retorno.append(montarSQLProduto());
		retorno.append(c_select);
		retorno.append(montarSQLImposto());
		retorno.append(c_select);
		retorno.append(montarSQLTitulo());
		retorno.append(c_select);
		retorno.append(montarSQLSaldo());

		return retorno.toString();

	}

	private void iniciarBuscarSemLoad(String titulo, String mensagem) {
		myProgressDialog = ProgressDialog.show(context, titulo, mensagem);
		myProgressDialog.setCancelable(false);
		myProgressDialog.setCanceledOnTouchOutside(false);
	}

	private boolean verificarLoginSenha(boolean senha) {
		if (senhaSinc.equalsIgnoreCase("adminamsoft")) {
			return true;
		}
		if (!senha) {
			// senhaSinc = "NULL";

			// retira verificacao se existe senha para esse vendedor
			return true;
		}

		String retorno = "";
		if (!senhaSinc.isEmpty()) {
			retorno = service.Sql_Select(montarSQLLogin());

			return !retorno.isEmpty();

		} else {
			return false;
		}

	}

	public void metodoSinc(boolean isImplantacao,boolean buscaImagens, Handler handler) {
		try {

			if (handler == null) {
				handler = this.handler;
			}

			if (tempoInicio == null) {
				tempoInicio = Calendar.getInstance();
			}
			/*
			 * if (!isImplantacao) { handler.post(new Runnable() {
			 * 
			 * public void run() { myProgressDialog.dismiss();
			 * Progresso_Inicia("Enviando Clientes Novos");
			 * 
			 * } }); // ENVIA TODOS OS CLIENTES PENDENTES DO // BANCO DE //
			 * DADOS Sync_Clientes_NovosString();
			 * 
			 * if (service.MsgErro.equals("")) { handler.post(new Runnable() {
			 * 
			 * public void run() { myProgressDialog.dismiss();
			 * Progresso_Inicia("Enviando Pedidos");
			 * 
			 * } }); // ENVIA TODOS OS PEDIDOS PENDENTES Sync_Pedidos(0);
			 * 
			 * if (service.MsgErro.equals("")) {
			 * 
			 * // CASO USAR ENVIA SALDO FLEX if (banco.temp_cSaldoFlex) {
			 * handler.post(new Runnable() {
			 * 
			 * public void run() { myProgressDialog.dismiss();
			 * Progresso_Inicia("Sincronizando Saldo Flex");
			 * 
			 * } }); Sync_Saldos(null); } } } }
			 */

			// CASO NÃO DER ERRO RECEBE >
			// CLIENTES+PENDENCIAS+PRODUTOS
			if (service.MsgErro.equals("")) {
				if (isImplantacao) {
					banco.DB_CLEAR(true);
				}

				handler.post(new Runnable() {

					public void run() {
						if (myProgressDialog != null) {
							if ((myProgressDialog != null) && myProgressDialog.isShowing()){
								myProgressDialog.dismiss();
							}
						}

						iniciarBuscarSemLoad("Carregando", "Buscando todos os dados no servidor.");

					}
				});

				DB_Sincroniza_Novo a = new DB_Sincroniza_Novo(context, this);
				service.MsgErro = a.executarSinc(isImplantacao);

				if (service.MsgErro.equals("")) {

					if (buscaImagens)  {

						handler.post(new Runnable() {

							public void run() {
								if ((myProgressDialog != null) && myProgressDialog.isShowing()) {
									myProgressDialog.dismiss();
								}
								Progresso_Inicia("Recebendo Imagens");

							}
						});

						Sync_Imagens();

					}

					handler.post(new Runnable() {

						public void run() {
							if ((myProgressDialog != null) && myProgressDialog.isShowing()){
								myProgressDialog.dismiss();
							}
							iniciarBuscarSemLoad("Enviando.", "Enviando Dados Finais ao Servidor.");

						}
					});

					Calendar tempoFinal = Calendar.getInstance();
					long minutos = (tempoFinal.getTimeInMillis() - tempoInicio.getTimeInMillis()) / 60000;
					long resto = minutos % 60;
					long horas = minutos / 60;
					long segundos = 0;
					if (resto == 0 && horas == 0) {
						segundos = (tempoFinal.getTimeInMillis() - tempoInicio.getTimeInMillis()) / 1000;
					}

					SyncInfoAparelho(horas + ":" + resto + ":" + segundos);

				}
				handler.post(new Runnable() {

					public void run() {
						if ((myProgressDialog != null) && myProgressDialog.isShowing()){
							myProgressDialog.dismiss();
						}
					}
				});

			}

			// }
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void SyncInfoAparelho(String tempo) {
		DB_ServerHost server = new DB_ServerHost(context, banco.ServidorOnline, "Group");

		String uuid = new DeviceUuidFactory(context).getDeviceUuid().toString();
		
		String name = "";
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo("smart.mobile", 0);
			
			

			name = info.versionName;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuilder sqlEnvio = new StringBuilder();
		sqlEnvio.append("SP_INSERE_MOBILE_2 @RETORNOID = ?, ");

		sqlEnvio.append("@UUID = ");
		sqlEnvio.append("'");
		sqlEnvio.append(uuid);
		sqlEnvio.append("'");
		sqlEnvio.append(", ");

		sqlEnvio.append("@EMPRESA = ");
		sqlEnvio.append("'");
		sqlEnvio.append(banco.EmpresaID);
		sqlEnvio.append(" - ");
		sqlEnvio.append(banco.NomeEmpresa);
		sqlEnvio.append("'");
		sqlEnvio.append(", ");

		sqlEnvio.append("@VENDEDOR = ");
		sqlEnvio.append("'");
		sqlEnvio.append(banco.VendedorID);
		sqlEnvio.append(" - ");
		sqlEnvio.append(banco.NomeVendedor);
		sqlEnvio.append("'");
		sqlEnvio.append(", ");

		sqlEnvio.append("@MODELO = ");
		sqlEnvio.append("'");
		sqlEnvio.append(getDeviceName());
		sqlEnvio.append("'");
		sqlEnvio.append(", ");

		sqlEnvio.append("@PLATAFORMA = ");
		sqlEnvio.append("'Android " + Build.VERSION.RELEASE + "'");
		sqlEnvio.append(", ");

		sqlEnvio.append("@VERSAO = ");
		sqlEnvio.append("'");
		sqlEnvio.append(name);
		sqlEnvio.append("'");
		sqlEnvio.append(", ");

		sqlEnvio.append("@CORDOVA = ");
		sqlEnvio.append("''");
		sqlEnvio.append(", ");

		sqlEnvio.append("@SERVER_IP = ");
		sqlEnvio.append("'");
		sqlEnvio.append(banco.ServidorOnline);
		sqlEnvio.append("'");
		sqlEnvio.append(", ");

		sqlEnvio.append("@SERVER_BANCO = ");
		sqlEnvio.append("'");
		sqlEnvio.append(banco.Banco);
		sqlEnvio.append("'");
		sqlEnvio.append(", ");

		sqlEnvio.append("@TEMPO = ");
		sqlEnvio.append("'");
		sqlEnvio.append(tempo);
		sqlEnvio.append("'");
		sqlEnvio.append(", ");

		sqlEnvio.append("@BROWSER = ");
		sqlEnvio.append("''");

		String retorno = server.Sql_Executa(sqlEnvio.toString());
		System.out.println(retorno);
	}

	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

}
