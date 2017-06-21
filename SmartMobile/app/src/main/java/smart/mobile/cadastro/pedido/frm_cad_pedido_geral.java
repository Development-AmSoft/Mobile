package smart.mobile.cadastro.pedido;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.R;
import smart.mobile.consulta.cliente.frm_cons_clientes;
import smart.mobile.consulta.titulos.frm_cons_titulos;
import smart.mobile.utils.error.ErroGeralController;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

public class frm_cad_pedido_geral extends Activity
{

	private DB_LocalHost banco;
	private long PedidoID;
	private boolean Sincronizado = false;
	private boolean loading = false;
	private boolean vencida = false;

	private final int MN_VOLTAR = 0;
	private final int MN_SALVAR = 1;

	Intent intentClientes;
	String ClienteID = "";

	// Spinner cmbClientes;
	EditText edtNome;
	EditText edtCPFCNPJ;

	Spinner cmbOperacoes;
	ImageButton btnConsCli;
	Spinner cmbFormasPgto;
	Spinner cmbListas;
	boolean ListaUnica = false;

	ArrayAdapter adpFormasPgto;
	ArrayAdapter adpListas;
	String ultimoCliFin = "";

	ArrayAdapter adpCli;

	String cpf_Cnpj;

	@Override
	public void onCreate(Bundle icicle)
	{

		// carrega dados do banco
		this.banco = new DB_LocalHost(this);
		ErroGeralController erro = new ErroGeralController(this, banco);
		
		// PARAMETRO DO PEDIDO
		Bundle b = getIntent().getExtras();
		PedidoID = b.getLong("pedidoid");

		// CARREGA O LAYOUT
		super.onCreate(icicle);
		setContentView(R.layout.lay_cad_pedido_geral);

		// CARREGANDO TIPOS DE OPERAÇÂO
		cmbOperacoes = (Spinner) this.findViewById(R.id.cmbOperacao);
		ArrayAdapter<String> adpOperacoes = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[]
		{
				"Venda", "Bonificação", "Devolução/Perda"
		});
		cmbOperacoes.setAdapter(adpOperacoes);

		Spinner cmbOrigem = (Spinner) this.findViewById(R.id.cmbOrigem);
		ArrayAdapter<String> adpOrigem = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[]
				{
				"Pessoalmente", "E-mail", "Telefone"
				});
		cmbOrigem.setAdapter(adpOrigem);
		cmbOrigem.setVisibility(View.GONE);

		// BOTAO DE SELECAO DE CLIENTE
		btnConsCli = (ImageButton) findViewById(R.id.btnConsCliente);
		btnConsCli.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Cliente_Seleciona();
			}
		});

		// CARREGA RAZAO CLIENTES
		// cmbClientes = (Spinner) findViewById(R.id.cmbCliente);
		// adpCli = new ArrayAdapter<String>(this,
		// android.R.layout.simple_spinner_item,
		// banco.Sql_Select2("SELECT NOME FROM CLIENTES WHERE SINCRONIZADO <= 1 ORDER BY NOME ASC"));
		// adpCli.setDropDownViewResource(android.R.layout.simple_spinner_item);
		// cmbClientes.setAdapter(adpCli);

		edtNome = (EditText) findViewById(R.id.edtNome);
		edtCPFCNPJ = (EditText) findViewById(R.id.edtCPFCNPJ);

		// CARREGA FORMAS DE PAGAMENTO
		cmbFormasPgto = (Spinner) findViewById(R.id.cmbFormaPgto);
		adpFormasPgto = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, banco.Sql_Select("FORMAS_PGTO", "DESCRICAO", "_id ASC"));
		adpFormasPgto.setDropDownViewResource(android.R.layout.simple_spinner_item);
		cmbFormasPgto.setAdapter(adpFormasPgto);

		// CARREGA LISTAS DE PREÇOS
		cmbListas = (Spinner) findViewById(R.id.cmbListaPreco);
		// cmbListas.setEnabled(false);
		adpListas = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, banco.Sql_Select("LISTAS_PRECOS", "DESCRICAO", "_id ASC"));
		adpListas.setDropDownViewResource(android.R.layout.simple_spinner_item);
		cmbListas.setAdapter(adpListas);

		// CARREGA OS DADOS DO PEDIDO
		if (PedidoID > 0)
		{

			carregaDadosPedido();
			banco.TempPed_ClearPRodutosID();
			// consulta de clientes
			intentClientes = new Intent(frm_cad_pedido_geral.this, frm_cons_clientes.class);
			Bundle c = new Bundle();
			c.putLong("pedidoid", PedidoID);
			intentClientes.putExtras(c);

			Cursor cProds = banco.Sql_Select("vendas", new String[]
			{
				"CPF_CNPJ"
			}, "_id = " + String.valueOf(PedidoID), "");
			if (cProds.moveToFirst())
			{

				if (!cProds.getString(0).isEmpty())
				{
					cpf_Cnpj = cProds.getString(0);
				}

			}

			if (Sincronizado)
			{

				ViewGroup group = (ViewGroup) findViewById(R.id.LinearLayout02);
				for (int i = 0, count = group.getChildCount(); i < count; ++i)
				{
					View view = group.getChildAt(i);
					if (view instanceof EditText)
					{
						((EditText) view).setEnabled(false);
					}
					if (view instanceof Spinner)
					{
						((Spinner) view).setEnabled(false);
					}
				}
			}

		}

		if (ClienteID.isEmpty())
		{
			Cliente_Seleciona();
		} else
		{
			verificaProdutos();

			// ADICIONA NA LISTA TEMPORARIA DE PRODUTOS DA VENDA

			Cursor cProds = banco.Sql_Select("VENDAS_ITENS", new String[]
			{
					"PRODUTOID", "LINHAID", "COLUNAID"
			}, "vendaid = " + String.valueOf(PedidoID), "");// ,
															// "SINCRONIZADO = 0",
															// "");
			if (cProds.moveToFirst())
			{
				do
				{
					banco.TempPed_AddProdutosID(cProds.getString(0), cProds.getString(1), cProds.getString(2));

				} while (cProds.moveToNext());
			}
		}

		// ADICIONA O EVENTO QUE SELECIONA A FANTASIA
		/*
		 * cmbClientes.setOnItemSelectedListener(new
		 * AdapterView.OnItemSelectedListener(){
		 * 
		 * public void onItemSelected(AdapterView adapter, View v, int i, long
		 * lng) { //do something here /*if (!loading) { //pega a posicao do nome
		 * e posiciona a fantasia if (cl){ int posNome =
		 * adapter.getSelectedItemPosition(); cmbFantasia.setSelection(posNome);
		 * 
		 * //configuracao do cliente CarregaConfigCliente(); } }
		 * 
		 * }
		 * 
		 * public void onNothingSelected(AdapterView arg0) { //do something else
		 * } });
		 */

		// demarca a flag
		loading = false;

	}

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		banco.closeHelper();
	}

	private void carregaDadosPedido()
	{

		loading = true;
		boolean trocoucliente = false;

		Cursor rs = banco.db.rawQuery("select vendas.operacao,clientes.nome,clientes.fantasia,vendas.listaid,vendas.forma_pgtoid,vendas.obs,vendas.sincronizado,clientes.cpf_cnpj,vendas.CPF_CNPJ,clientes.listaid, vendas.origem from vendas join clientes on vendas.CPF_CNPJ = clientes.CPF_CNPJ where vendas._id = " + String.valueOf(PedidoID), null);
		if (rs.moveToFirst())
		{

			// valida se trocou de cliente
			if ((!ClienteID.equals("")) && (ClienteID.equals(rs.getInt(7))))
			{
				trocoucliente = true;
			}

			// codigo do cliente
			ClienteID = rs.getString(7);

			// operacao
			((Spinner) findViewById(R.id.cmbOperacao)).setSelection(rs.getInt(0));
			((Spinner) findViewById(R.id.cmbOrigem)).setSelection(rs.getInt(10));

			// razao social
			// for(int x=0; x < adpCli.getCount(); x++){
			// if(rs.getString(1).equals(cmbClientes.getItemAtPosition(x).toString())){
			// cmbClientes.setSelection(x);
			// break;}
			// }

			// razao
			((EditText) findViewById(R.id.edtNome)).setText(rs.getString(1));

			// fantasia
			((EditText) findViewById(R.id.edtFantasia)).setText(rs.getString(2));

			// cpf/cnpj
			((EditText) findViewById(R.id.edtCPFCNPJ)).setText(rs.getString(7));

			// lista de preco
			if (trocoucliente)
			{
				SelecionaLista(rs.getString(9));
			} else
			{
				SelecionaLista(rs.getString(3));
			}

			// forma de pagamento
			Seleciona_FormaPedido(rs.getString(4));

			// observacao
			((EditText) findViewById(R.id.edtObs)).setText(rs.getString(5));

			// flag de pedido sincronizado
			Sincronizado = (rs.getLong(6) == 1);

			// carrega lista de preço/ formas de pagamento/ avisa se cliente
			// possui pendencias
			CarregaConfigCliente();

			// 20/09/2013 - Jakson Gava - validado para INCAS - em caso de
			// cliente ter lista '0 - indefinida'
			// permitir ao vendedor escolher a lista '0' ou a lista '14'
			// (acréscimo 25%)
			if (banco.Banco.toUpperCase().trim().equals("INCAS") && rs.getString(9).equalsIgnoreCase("0"))
			{

				// marcador para permitir alterar a lista
				ListaUnica = false;

				// recarrega apenas a lista 0 + lista 12 + lista 14
				adpListas = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, banco.Sql_SelectIncas("LISTAS_PRECOS", "DESCRICAO", "(LISTAID = 0) OR (TIPO_LISTA = 'A')", "_id ASC"));
				adpListas.setDropDownViewResource(android.R.layout.simple_spinner_item);
				cmbListas.setAdapter(adpListas);

				// lista de preco
				if (trocoucliente)
				{
					SelecionaLista(rs.getString(9));
				} else
				{
					SelecionaLista(rs.getString(3));
				}

			}

		}
		if (rs != null || !rs.isClosed())
		{
			rs.close();
		}

	}

	private void SelecionaLista(String xCodLista)
	{

		for (int x = 0; x < cmbListas.getCount(); x++)
		{
			String codLista = ((Spinner) findViewById(R.id.cmbListaPreco)).getItemAtPosition(x).toString().substring(0, ((Spinner) findViewById(R.id.cmbListaPreco)).getItemAtPosition(x).toString().indexOf("-")).trim();
			if ((xCodLista).equals(codLista))
			{
				((Spinner) findViewById(R.id.cmbListaPreco)).setSelection(x);
				break;
			}
		}

	}

	private void Seleciona_FormaPedido(String xCodForma)
	{

		for (int y = 0; y < cmbFormasPgto.getCount(); y++)
		{
			String codSpinner = ((Spinner) findViewById(R.id.cmbFormaPgto)).getItemAtPosition(y).toString().substring(0, ((Spinner) findViewById(R.id.cmbFormaPgto)).getItemAtPosition(y).toString().indexOf("-")).trim();
			if ((codSpinner).equals(xCodForma))
			{
				((Spinner) findViewById(R.id.cmbFormaPgto)).setSelection(y);
				break;
			}
		}

	}

	private void CarregaConfigCliente()
	{

		// quando nao estiver carregando o pedido

		// armazena a lista e forma de pagamento selecionadas
		String codLista = cmbListas.getItemAtPosition(cmbListas.getSelectedItemPosition()).toString().substring(0, ((Spinner) findViewById(R.id.cmbListaPreco)).getItemAtPosition(cmbListas.getSelectedItemPosition()).toString().indexOf("-")).trim();
		String codForma = cmbFormasPgto.getItemAtPosition(cmbFormasPgto.getSelectedItemPosition()).toString().substring(0, ((Spinner) findViewById(R.id.cmbFormaPgto)).getItemAtPosition(cmbFormasPgto.getSelectedItemPosition()).toString().indexOf("-")).trim();

		Log.i("LISTA SEL", codLista);
		Log.i("FORMA SEL", codForma);

		List LFormas;
		List LListas;

		// verifica se possui alguma especifica então carrega senao carrega
		// todas
		// so carrega a lista do cliente caso ainda não tiver produtos
		// cadastrados
		verificaProdutos();

		if (btnConsCli.isEnabled())
		{

			Cursor cli0 = banco.db.rawQuery("select _id, listaid, FORMA_PGTOID from clientes where CPF_CNPJ like '" + String.valueOf(ClienteID) + "'", null);
			if (cli0.moveToFirst())
			{

				// lista de preco
				if (cli0.getDouble(1) > 0)
				{

					ListaUnica = true;
					// cmbListas.setEnabled(false);
					if (codLista.equals("") || codLista.equals("0") || edtNome.isEnabled())
					{
						codLista = cli0.getString(1);
					}
				} else
				{

					ListaUnica = false;
					// cmbListas.setEnabled(true);

					if (edtNome.isEnabled())
					{
						// cmbListas.setEnabled(true);
					}
					codLista = "0";
				}

				// 20/09/2013 - Jakson Gava - validado para INCAS - em caso de
				// cliente ter lista '0 - indefinida'
				// permitir ao vendedor escolher a lista '0' ou a lista '14'
				// (acréscimo 25%)
				if (banco.Banco.toUpperCase().trim().equals("INCAS") && codLista.equalsIgnoreCase("0"))
				{

					// marcador para permitir alterar a lista
					ListaUnica = false;

					// recarrega apenas a lista 0 e lista 14
					adpListas = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, banco.Sql_SelectIncas("LISTAS_PRECOS", "DESCRICAO", "(LISTAID = 0) OR (TIPO_LISTA = 'A')", "_id ASC"));
					adpListas.setDropDownViewResource(android.R.layout.simple_spinner_item);
					cmbListas.setAdapter(adpListas);

				}
				
				codForma = cli0.getString(2);

			} else
			{

				ListaUnica = true;

			}

			// formas de pagamento
			//LFormas = banco.Sql_Select2("select  formas_pgto.descricao from clientes_formas_pgto join formas_pgto on formas_pgto.forma_pgtoid = clientes_formas_pgto.forma_pgtoid where clientes_formas_pgto.CPF_CNPJ like '" + String.valueOf(ClienteID) + "'");
			//if (!LFormas.isEmpty())
			//{
			//	ArrayAdapter adpFormasPgto2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, LFormas);
			//	adpFormasPgto2.setDropDownViewResource(android.R.layout.simple_spinner_item);
			//	cmbFormasPgto.setAdapter(adpFormasPgto2);
			//} else
			//{
			//	cmbFormasPgto.setAdapter(adpFormasPgto);
			//}
		}

		//ref. chamado 6002 - formas de pagamento recarregadas de acordo com configuracao do cliente mesmo que esteja editando o pedido
		LFormas = banco.Sql_Select2("select  formas_pgto.descricao from clientes_formas_pgto join formas_pgto on formas_pgto.forma_pgtoid = clientes_formas_pgto.forma_pgtoid where clientes_formas_pgto.CPF_CNPJ like '" + String.valueOf(ClienteID) + "'");
		if (!LFormas.isEmpty())
		{
			ArrayAdapter adpFormasPgto2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, LFormas);
			adpFormasPgto2.setDropDownViewResource(android.R.layout.simple_spinner_item);
			cmbFormasPgto.setAdapter(adpFormasPgto2);
		} else
		{
			cmbFormasPgto.setAdapter(adpFormasPgto);
		}

		// seleciona novamente lista e forma
		SelecionaLista(codLista);
		Seleciona_FormaPedido(codForma);

		// verifica se tem titulos pendentes
		CarregaTitulosPendentes();

	}

	private boolean isVencidoIncas(String DataVencimento, int DiasBloqueio)
	{

		Log.i("vencimento antes", DataVencimento);

		// valida se a conta esta vencida a mais de 10 dias para impedir o
		// pedido
		boolean retorno = false;

		// Step-1 Get Calendar instance from the specified string
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		try
		{
			c.setTime(sdf.parse(DataVencimento.substring(6, 10) + "-" + DataVencimento.substring(3, 5) + "-" + DataVencimento.substring(0, 2)));
		} catch (java.text.ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Step-2 use add() to add number of days to calendar
		c.add(Calendar.DATE, DiasBloqueio);

		// Step-3 Convert the dtae to the resultant date format
		sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date resultdate = new Date(c.getTimeInMillis());
		DataVencimento = sdf.format(resultdate);

		// acrescenta X dias a data de vencimento
		// Calendar c = new
		// GregorianCalendar(Integer.valueOf(DataVencimento.substring(6,10)),
		// Integer.valueOf(DataVencimento.substring(3,5)),
		// Integer.valueOf(DataVencimento.substring(0,2)));
		// c.add(Calendar.DAY_OF_MONTH, DiasBloqueio);

		// converte o calendario para data
		// SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy");
		// DataVencimento = sd.format(c.getTime());

		// busca a data de hoje
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
		String DataHoje = sdf2.format(new Date());

		// converte para numero inteiro as duas datas
		DataVencimento = DataVencimento.substring(6, 10) + DataVencimento.substring(3, 5) + DataVencimento.substring(0, 2);
		DataHoje = DataHoje.substring(6, 10) + DataHoje.substring(3, 5) + DataHoje.substring(0, 2);

		Log.i("vencimento", DataVencimento);
		Log.i("hoje", DataHoje);

		if (Long.valueOf(DataVencimento) <= Long.valueOf(DataHoje))
		{
			retorno = true;
		}

		return retorno;

	}

	private void CarregaTitulosPendentes()
	{

		final String CpfCnpj = edtCPFCNPJ.getText().toString();
		if (ultimoCliFin.equals("") || (!ultimoCliFin.equals(CpfCnpj)))
		{

			ultimoCliFin = CpfCnpj;
			Cursor crTitulos = banco.db.rawQuery("select VENCIMENTO from titulos where nome = '" + CpfCnpj + "'", null);
			if (crTitulos.moveToFirst())
			{

				String msg = "Cliente possui Pendências, deseja visualizar ?";
				vencida = false;

				if (banco.Banco.toUpperCase().trim().equals("INCASxxx") || banco.Banco.toUpperCase().trim().equals("PADARIAST") || banco.Banco.toUpperCase().trim().equals("PHLDISTRIBUIDORA") || banco.Banco.toUpperCase().trim().equals("MIG"))
				{

					// dias para bloqueio
					int dias = 0;
					if (banco.Banco.toUpperCase().trim().equals("INCASxxx"))
					{
						dias = 10;
					} else if (banco.Banco.toUpperCase().trim().equals("PADARIAST"))
					{
						dias = 5;
					} else if (banco.Banco.toUpperCase().trim().equals("PHLDISTRIBUIDORA"))
					{
						dias = 1;
					} else if (banco.Banco.toUpperCase().trim().equals("MIG"))
					{
						dias = 2;
					}

					// verifica se algum delas está vencida, apenas se estiver
					// vencida vai bloquear
					if (crTitulos.moveToFirst())
					{
						do
						{

							String vencimento = crTitulos.getString(0);
							if (isVencidoIncas(vencimento, dias))
							{

								msg = "Cliente possui Pendências Vencidas a mais de " + dias + " dias, deseja visualizar ?\n\nDevem ser efetuadas as Baixas destas Pendências no SmartTools[Sistema de Gestão] e sincronizar novamente o SmartMobile[Sistema de Vendas] !!!";
								vencida = true;

							}

						} while (crTitulos.moveToNext());
					}
					if (crTitulos != null || !crTitulos.isClosed())
					{
						crTitulos.close();
					}

				}

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(msg).setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{

						Intent intent = new Intent(frm_cad_pedido_geral.this, frm_cons_titulos.class);
						intent.putExtra("filtro", CpfCnpj);
						startActivity(intent);
						// break;

						// se for incas e estiver alguma vencida não permite
						// efetuar o pedido
						if (((banco.Banco.toUpperCase().trim().equals("INCASxxx") || banco.Banco.toUpperCase().trim().equals("PADARIAST") || banco.Banco.toUpperCase().trim().equals("PHLDISTRIBUIDORA") || banco.Banco.toUpperCase().trim().equals("MIG")) && vencida))
						{
							finish();
						}

					}
				}).setNegativeButton("Não", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{

						dialog.cancel();

						// se for incas não permite efetuar o pedido
						if (((banco.Banco.toUpperCase().trim().equals("INCAS") || banco.Banco.toUpperCase().trim().equals("PADARIAST") || banco.Banco.toUpperCase().trim().equals("PHLDISTRIBUIDORA") || banco.Banco.toUpperCase().trim().equals("MIG")) && vencida))
						{
							finish();
						}
					}
				})
				// Set your icon here
						.setTitle("SmartMobile - Títulos Pendentes").setIcon(R.drawable.ico_titulos);
				AlertDialog alert = builder.create();
				alert.show();

				/*
				 * DialogInterface.OnClickListener dialogClickListener = new
				 * DialogInterface.OnClickListener() { public void
				 * onClick(DialogInterface dialog, int which) { switch (which){
				 * 
				 * case DialogInterface.BUTTON_POSITIVE:
				 * 
				 * Intent intent = new Intent(frm_cad_pedido_geral.this,
				 * frm_cons_titulos.class); intent.putExtra("filtro", nome);
				 * startActivity(intent); break;
				 * 
				 * case DialogInterface.BUTTON_NEGATIVE: break; } } };
				 * 
				 * AlertDialog.Builder builder2 = new
				 * AlertDialog.Builder(frm_cad_pedido_geral.this);
				 * builder2.setIcon(R.drawable.ico_cliente);
				 * builder2.setMessage(
				 * "Cliente possui pendências, deseja visualizar ?"
				 * ).setPositiveButton("Sim",
				 * dialogClickListener).setNegativeButton("Não",
				 * dialogClickListener).show();
				 */

			}
			if (crTitulos != null || !crTitulos.isClosed())
			{
				crTitulos.close();
			}

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemAdd = menu.add(0, MN_VOLTAR, Menu.NONE, "Voltar");
		itemAdd.setIcon(R.drawable.ico_voltar);
		itemAdd.setShortcut('0', 'v');

		if (!Sincronizado)
		{
			MenuItem itemRem = menu.add(0, MN_SALVAR, Menu.NONE, "Salvar");
			itemRem.setIcon(R.drawable.ico_salvar);
			itemRem.setShortcut('1', 's');
		}

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

		case MN_SALVAR:

			// refresh = new Intent(this, frmConsClientes.class);
			// startActivity(refresh);

			finish();

			return true;
		}
		return false;
	}

	private void SALVAR()
	{

		// ??? caso nao possuir nenhum item nao salva o pedido/deleta
		/*
		 * Cursor c_itens =
		 * banco.db.rawQuery("select produtoid from vendas_itens where _id = " +
		 * String.valueOf(PedidoID), null); if(!c_itens.moveToFirst()) {
		 * banco.db.delete("VENDAS", "_id = " + String.valueOf(PedidoID),null);
		 * } else
		 */
		{
			if (!Sincronizado)
			{

				try
				{
					String codFormaPgto = ((Spinner) findViewById(R.id.cmbFormaPgto)).getSelectedItem().toString().substring(0, ((Spinner) findViewById(R.id.cmbFormaPgto)).getSelectedItem().toString().indexOf("-")).trim();
					String codLista = ((Spinner) findViewById(R.id.cmbListaPreco)).getSelectedItem().toString().substring(0, ((Spinner) findViewById(R.id.cmbListaPreco)).getSelectedItem().toString().indexOf("-")).trim();

					banco.TB_VENDAS_INSERIR(PedidoID, ((Spinner) findViewById(R.id.cmbOperacao)).getSelectedItemPosition(), ClienteID, Integer.valueOf(codFormaPgto), Integer.valueOf(codLista), 0.00, ((EditText) findViewById(R.id.edtObs)).getText().toString(),((Spinner) findViewById(R.id.cmbOrigem)).getSelectedItemPosition());
				} catch (NullPointerException e)
				{

				}

				// Toast.makeText(this,"Pedido salvo com sucesso !!!",
				// Toast.LENGTH_SHORT).show();
				// Log.i("SmartMobile", "Pedido salvo com sucesso !!!");

			}
		}
	}

	@Override
	public void finish()
	{

		if (!Sincronizado)
		{
			SALVAR();
		}

		super.finish();
		return;

	}

	@Override
	public void onPause()
	{

		super.onPause();
		SALVAR();
		Log.d("Cadastro", "Caiu no OnPause");

	}

	@Override
	public void onResume()
	{
		super.onResume();

		carregaDadosPedido();
		verificaProdutos();

	}

	private void verificaProdutos()
	{

		// CASO TENHA ALGUM PRODUTO ADICIONA BLOQUEIA O CAMPO DE TIPOOPERACAO + LISTA DE PRECO
		Cursor cPed = banco.db.rawQuery("select produtoid from vendas_itens where vendaid = " + String.valueOf(PedidoID), null);
		if (cPed.moveToFirst())
		{
			cmbOperacoes.setEnabled(false);
			edtNome.setEnabled(false);
			btnConsCli.setEnabled(false);

			cmbListas.setEnabled(false);
		} else
		{
			cmbOperacoes.setEnabled(true);
			if (!ListaUnica)
			{
				cmbListas.setEnabled(true);
			} else
			{
				cmbListas.setEnabled(false);
			}
		}
	}

	private void Cliente_Seleciona()
	{
		// intentClientes = new Intent(frm_cad_pedido_geral.this,
		// frm_cons_clientes.class);
		// Bundle c = new Bundle();
		// c.putLong("pedidoid", PedidoID);
		startActivity(intentClientes);

	}

}
