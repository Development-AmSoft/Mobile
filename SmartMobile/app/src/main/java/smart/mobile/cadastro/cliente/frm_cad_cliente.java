package smart.mobile.cadastro.cliente;

import java.util.ArrayList;
import java.util.InputMismatchException;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.R;
import smart.mobile.utils.ConfiguracaoInicialTela;
import smart.mobile.utils.error.ErroGeralController;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class frm_cad_cliente extends Activity
{

	private DB_LocalHost banco;
	private String ClienteID;
	private boolean Sincronizado = false;

	private EditText edtNome;
	private EditText edtCPFCNPJ;
	private EditText edtTelefone;
	private EditText edtCelular;
	private EditText edtEmail;
	private EditText edtResponsavel;
	private EditText edtUltTotal;
	private EditText edtObs;
	private EditText edtInscricaoEstatudal;
	private EditText edtFantasia;

	private boolean loading = false;
	private int statusPedido;

	private final int MN_VOLTAR = 0;
	private final int MN_SALVAR = 1;

	@Override
	public void onAttachedToWindow()
	{
		/*openOptionsMenu();*/
	};

	@Override
	public void onOptionsMenuClosed(Menu menu)
	{
		/*openOptionsMenu();*/
	}

	@Override
	public void onCreate(Bundle icicle)
	{

		// carrega dados do banco
		this.banco = new DB_LocalHost(this);
		ErroGeralController erro = new ErroGeralController(this, banco);
		
		
		// PARAMETRO DO CLIENTE
		Bundle b = getIntent().getExtras();
		ClienteID = b.getString("clienteid");

		// CARREGA O LAYOUT
		super.onCreate(icicle);
		setContentView(R.layout.activity_ctela_base);
		setTitle("SmartMobile - Detalhes do Cliente");
		
		ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.lay_cad_cliente, false);
		
		RelativeLayout footer = (RelativeLayout) findViewById(R.id.FooterEsconder);
		footer.setVisibility(View.GONE);
		
		
		// CARREGA CIDADES DO XML
		// String[] listCidades =
		// getResources().getStringArray(R.array.cidades_array);
		// ArrayAdapter<String> adp = new ArrayAdapter<String>(this,
		// android.R.layout.simple_dropdown_item_1line, listCidades);
		// AutoCompleteTextView compCidades = (AutoCompleteTextView)
		// findViewById(R.id.edtCidade);
		// compCidades.setAdapter(adp);

		// CARREGA LISTAS DE PRE�OS
		Spinner cmbListas = (Spinner) findViewById(R.id.cmbListaPreco);
		ArrayAdapter adpListas = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, banco.Sql_Select("LISTAS_PRECOS", "DESCRICAO", "_id ASC"));
		adpListas.setDropDownViewResource(android.R.layout.simple_spinner_item);
		cmbListas.setAdapter(adpListas);
		cmbListas.setEnabled(false);

		// CARREGA FORMAS DE PAGAMENTO
		Spinner cmbFormasPgto = (Spinner) findViewById(R.id.cmbFormaPgto);
		ArrayAdapter adpFormasPgto = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, banco.Sql_Select("FORMAS_PGTO", "DESCRICAO", "_id ASC"));
		adpFormasPgto.setDropDownViewResource(android.R.layout.simple_spinner_item);
		cmbFormasPgto.setAdapter(adpFormasPgto);
		cmbFormasPgto.setEnabled(false);

		// ESTADOS NO SPINER
		Spinner cmbUFs = (Spinner) findViewById(R.id.cmbUF);
		ArrayAdapter adpUFs = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listUFs);
		adpUFs.setDropDownViewResource(android.R.layout.simple_spinner_item);
		cmbUFs.setAdapter(adpUFs);
		// POSICIONA EM PR
		for (int x = 0; x < adpUFs.getCount(); x++)
		{
			if ((((Spinner) findViewById(R.id.cmbUF)).getItemAtPosition(x).toString()).equals("PR"))
			{
				((Spinner) findViewById(R.id.cmbUF)).setSelection(x);
				break;
			}
		}

		// CIDADES NO SPINER
		Spinner cmbCidades = (Spinner) findViewById(R.id.cmbCidade);
		ArrayAdapter adpCidades = new ArrayAdapter<String>(frm_cad_cliente.this, android.R.layout.simple_spinner_item, listCidades);
		adpCidades.setDropDownViewResource(android.R.layout.simple_spinner_item);
		cmbCidades.setAdapter(adpCidades);

		edtNome = ((EditText) findViewById(R.id.edtNome));
		edtCPFCNPJ = ((EditText) findViewById(R.id.edtCPFCNPJ));
		edtTelefone = ((EditText) findViewById(R.id.edtTelefone));
		edtCelular = ((EditText) findViewById(R.id.edtCelular));
		edtEmail = ((EditText) findViewById(R.id.edtEmail));
		edtResponsavel = ((EditText) findViewById(R.id.edtResponsavel));
		edtUltTotal = ((EditText) findViewById(R.id.edtUltTotal));
		edtInscricaoEstatudal = ((EditText) findViewById(R.id.edtInscricao));
		edtFantasia = ((EditText) findViewById(R.id.edtFantasia));

		// CARREGA OS DADOS DO CLIENTE
		if (!ClienteID.isEmpty())
		{

			loading = true;
			Cursor rs = banco.Sql_Select("CLIENTES", banco.TB_CLIENTES_CAMPOS, "CPF_CNPJ like '" + String.valueOf(ClienteID)+"'", null);
			if (rs.moveToFirst())
			{

				((EditText) findViewById(R.id.edtNome)).setText(rs.getString(0));
				((EditText) findViewById(R.id.edtFantasia)).setText(rs.getString(1));

				edtCPFCNPJ.setText(rs.getString(2));

				((EditText) findViewById(R.id.edtInscricao)).setText(rs.getString(3));

				// seleciona uf
				for (int x = 0; x < adpUFs.getCount(); x++)
				{
					String uf = ((Spinner) findViewById(R.id.cmbUF)).getItemAtPosition(x).toString();
					if ((rs.getString(4).substring(rs.getString(4).indexOf("-") + 1).trim()).equals(uf.trim()))
					{
						((Spinner) findViewById(R.id.cmbUF)).setSelection(x);
						break;
					}
				}

				// seleciona cidade
				for (int x = 0; x < adpCidades.getCount(); x++)
				{
					String cidade = ((Spinner) findViewById(R.id.cmbCidade)).getItemAtPosition(x).toString();
					if ((rs.getString(4)).equals(cidade))
					{
						((Spinner) findViewById(R.id.cmbCidade)).setSelection(x);
						break;
					}
				}

				((EditText) findViewById(R.id.edtEndereco)).setText(rs.getString(5));
				((EditText) findViewById(R.id.edtNumero)).setText(rs.getString(6));
				((EditText) findViewById(R.id.edtBairro)).setText(rs.getString(7));
				((EditText) findViewById(R.id.edtCEP)).setText(rs.getString(8));
				((EditText) findViewById(R.id.edtTelefone)).setText(rs.getString(9));
				((EditText) findViewById(R.id.edtCelular)).setText(rs.getString(10));
				((EditText) findViewById(R.id.edtEmail)).setText(rs.getString(11));

				edtObs = ((EditText) findViewById(R.id.edtObs));
				((EditText) findViewById(R.id.edtObs)).setText(rs.getString(12));

				((EditText) findViewById(R.id.edtLimite)).setText(banco.myCustDecFormatter.format(rs.getDouble(13)));

				for (int x = 0; x < adpListas.getCount(); x++)
				{
					String codLista = ((Spinner) findViewById(R.id.cmbListaPreco)).getItemAtPosition(x).toString().substring(0, ((Spinner) findViewById(R.id.cmbListaPreco)).getItemAtPosition(x).toString().indexOf("-")).trim();
					if ((rs.getString(14)).equals(codLista))
					{
						((Spinner) findViewById(R.id.cmbListaPreco)).setSelection(x);
						break;
					}
				}

				for (int y = 0; y < adpFormasPgto.getCount(); y++)
				{
					String codForma = ((Spinner) findViewById(R.id.cmbFormaPgto)).getItemAtPosition(y).toString().substring(0, ((Spinner) findViewById(R.id.cmbFormaPgto)).getItemAtPosition(y).toString().indexOf("-")).trim();
					if ((rs.getString(15)).equals(codForma))
					{
						((Spinner) findViewById(R.id.cmbFormaPgto)).setSelection(y);
						break;
					}
				}

				Sincronizado = (rs.getLong(16) >= 1);
				if(rs.getLong(16) == 1 || rs.getLong(16) == 3){
//					Raz�o social, Fantasia, CNPJ, IE, Cidade e Estado
					desabilitarEditText(edtCPFCNPJ);
					desabilitarEditText(edtFantasia);
					desabilitarEditText(edtNome);
					desabilitarEditText(edtInscricaoEstatudal);
					cmbCidades.setEnabled(false);
					cmbUFs.setEnabled(false);
					edtResponsavel.requestFocus();
				} else if(rs.getLong(16) == 0){
					desabilitarEditText(edtCPFCNPJ);
				}

				((EditText) findViewById(R.id.edtUltData)).setText(rs.getString(17));
				((EditText) findViewById(R.id.edtUltTotal)).setText(rs.getString(18));
				((EditText) findViewById(R.id.edtComplemento)).setText(rs.getString(19));
				((EditText) findViewById(R.id.edtResponsavel)).setText(rs.getString(20));

			}

			// if(rs != null || !rs.isClosed()) {
			// rs.close();
			// }
		}

		// ZERA PARA NAO DAR PAU
		if (((EditText) findViewById(R.id.edtUltTotal)).getText().toString().equals(""))
		{
			((EditText) findViewById(R.id.edtUltTotal)).setText("0");
		}

		// ADICIONA O EVENTO QUE FILTRA AS CIDADES CONFORME O UF
		cmbUFs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{

			public void onItemSelected(AdapterView adapter, View v, int i, long lng)
			{
				// do something here
				if (loading)
				{
					loading = false;
				} else
				// so filtra as cidades depois que tiver carregado o cliente
				{
					// Toast.makeText(frm_cad_cliente.this,"UF " +
					// adapter.getSelectedItem().toString(),Toast.LENGTH_LONG).show();

					// UF selecionado
					String UF = adapter.getSelectedItem().toString();

					// filtra apenas as cidades do UF
					ArrayList<String> filtro = new ArrayList<String>();
					for (int x = 0; x < listCidades.length - 1; x++)
					{
						if (listCidades[x].indexOf("- " + UF) > 0)
						{
							filtro.add(listCidades[x]);
						}
					}

					ArrayAdapter adpCid2 = new ArrayAdapter<String>(frm_cad_cliente.this, android.R.layout.simple_spinner_item, filtro);
					adpCid2.setDropDownViewResource(android.R.layout.simple_spinner_item);
					Spinner cmbCid2 = (Spinner) findViewById(R.id.cmbCidade);
					cmbCid2.setAdapter(adpCid2);

				}

			}

			public void onNothingSelected(AdapterView arg0)
			{
				// do something else
			}
		});



		// se estiver sincronizado desabilita todos os campos
//		if (Sincronizado || (!edtUltTotal.getText().toString().equals("0")))
//		{
//
//			ViewGroup group = (ViewGroup) findViewById(R.id.LinearLayout02);
//			for (int i = 0, count = group.getChildCount(); i < count; ++i)
//			{
//				View view = group.getChildAt(i);
//				if (view instanceof EditText)
//				{
//					((EditText) view).setEnabled(false);
//				}
//				if (view instanceof Spinner)
//				{
//					((Spinner) view).setEnabled(false);
//				}
//			}
//
//			// habilita apenas telefones e email 30/01/2013
//			edtTelefone.setEnabled(true);
//			edtCelular.setEnabled(true);
//			edtEmail.setEnabled(true);
//			edtResponsavel.setEnabled(true);
//			edtObs.setEnabled(true);
//
//		}

	}

	private void desabilitarEditText(EditText edit){
		edit.setEnabled(false);
		edit.setTextColor(R.drawable.color_disble);
	}

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		banco.closeHelper();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemAdd = menu.add(0, MN_VOLTAR, Menu.NONE, "Voltar");
		itemAdd.setIcon(R.drawable.ico_voltar);
		itemAdd.setShortcut('0', 'v');

		// if(!Sincronizado){
		MenuItem itemRem = menu.add(0, MN_SALVAR, Menu.NONE, "Salvar");
		itemRem.setIcon(R.drawable.ico_salvar);
		itemRem.setShortcut('1', 's');// }

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

			if (ValidaCPFCNPJ())
			{

				// if(Sincronizado){
				// AlertDialog.Builder msg = new
				// AlertDialog.Builder(frm_cad_cliente.this);
				// msg.setMessage("Cliente j� foi sincronizado.\nDever� ser sincronizado novamente para ser atualizado no SmartTools !!!").show();
				// }
				// else{

				String codFormaPgto = ((Spinner) findViewById(R.id.cmbFormaPgto)).getSelectedItem().toString().substring(0, ((Spinner) findViewById(R.id.cmbFormaPgto)).getSelectedItem().toString().indexOf("-")).trim();
				String codLista = ((Spinner) findViewById(R.id.cmbListaPreco)).getSelectedItem().toString().substring(0, ((Spinner) findViewById(R.id.cmbListaPreco)).getSelectedItem().toString().indexOf("-")).trim();
				String cidade = ((Spinner) findViewById(R.id.cmbCidade)).getSelectedItem().toString();

				banco.TB_CLIENTES_INSERIR(ClienteID, ((EditText) findViewById(R.id.edtNome)).getText().toString(), ((EditText) findViewById(R.id.edtFantasia)).getText().toString(), ((EditText) findViewById(R.id.edtCPFCNPJ)).getText().toString(), ((EditText) findViewById(R.id.edtInscricao)).getText().toString(), ((EditText) findViewById(R.id.edtResponsavel)).getText().toString(), cidade, ((EditText) findViewById(R.id.edtEndereco)).getText().toString(), ((EditText) findViewById(R.id.edtNumero)).getText().toString(), ((EditText) findViewById(R.id.edtBairro)).getText().toString(), ((EditText) findViewById(R.id.edtComplemento)).getText().toString(), ((EditText) findViewById(R.id.edtCEP)).getText().toString(), ((EditText) findViewById(R.id.edtTelefone)).getText().toString(), ((EditText) findViewById(R.id.edtCelular)).getText().toString(), ((EditText) findViewById(R.id.edtEmail)).getText().toString(), ((EditText) findViewById(R.id.edtObs)).getText().toString(),
						((EditText) findViewById(R.id.edtLimite)).getText().toString(), codFormaPgto, codLista, statusPedido, 1, ((EditText) findViewById(R.id.edtUltData)).getText().toString(), Double.valueOf(((EditText) findViewById(R.id.edtUltTotal)).getText().toString()));

				statusPedido = 0;

				Toast.makeText(this, "Cliente salvo com sucesso !!!", Toast.LENGTH_SHORT).show();
				Log.i("SmartMobile", "Cliente salvo com sucesso !!!");

				finish();
				return true;
				// }

				// refresh = new Intent(this, frmConsClientes.class);
				// startActivity(refresh);

			}

		}
		return false;
	}

	public static String SomenteNumeros(String str)
	{
		if (str != null)
		{
			return str.replaceAll("[^0123456789]", "");
		} else
		{
			return "";
		}
	}

	public boolean ValidaCPFCNPJ()
	{

		boolean retorno = true;
		if (!edtCPFCNPJ.getText().toString().trim().equals(""))
		{
			edtCPFCNPJ.setText(edtCPFCNPJ.getText().toString().replace(".", "").replace("-", "").replace("/", ""));
		}

		if (edtNome.getText().toString().trim().equals(""))
		{
			banco.MostraMsg(this, "Informe o Nome !!!");
			edtNome.requestFocus();
			retorno = false;
		} else if (edtCPFCNPJ.getText().toString().trim().equals(""))
		{
			banco.MostraMsg(this, "Informe o CPF/CNPJ !!!");
			edtCPFCNPJ.requestFocus();
			retorno = false;
		} else if (edtCPFCNPJ.getText().toString().length() <= 11)
		{
			retorno = Valida_CPF(edtCPFCNPJ.getText().toString());
			if (!retorno)
			{
				banco.MostraMsg(this, "CPF inv�lido !!!");
				edtCPFCNPJ.requestFocus();
			}
		} else if (edtCPFCNPJ.getText().toString().length() > 11)
		{
			if (!edtCPFCNPJ.getText().toString().trim().equals(""))
			{
				edtCPFCNPJ.setText(edtCPFCNPJ.getText().toString().replace(".", "").replace("-", "").replace("/", ""));
			}
			retorno = Valida_CNPJ(edtCPFCNPJ.getText().toString());
			if (!retorno)
			{
				banco.MostraMsg(this, "CNPJ inv�lido !!!");
				edtCPFCNPJ.requestFocus();
			}
		} else if ((!edtTelefone.getText().toString().equals("")) && (edtTelefone.getText().toString().length() < 10))
		{
			banco.MostraMsg(this, "Formato do Telefone >> (00)0000-0000 !!!");
			edtTelefone.requestFocus();
			retorno = false;
		} else if ((!edtCelular.getText().toString().equals("")) && (edtCelular.getText().toString().length() < 10))
		{
			banco.MostraMsg(this, "Formato do Celular >> (00)0000-0000 !!!");
			edtCelular.requestFocus();
			retorno = false;
		}

		// formata o cpf ou cnpj
		String xCpfCnpj = edtCPFCNPJ.getText().toString();
		if (retorno)
		{
			if (xCpfCnpj.length() <= 11)
			{
				edtCPFCNPJ.setText(xCpfCnpj.substring(0, 3) + "." + xCpfCnpj.substring(3, 6) + "." + xCpfCnpj.substring(6, 9) + "-" + xCpfCnpj.substring(9, 11));
			} else
			{
				edtCPFCNPJ.setText(xCpfCnpj.substring(0, 2) + "." + xCpfCnpj.substring(2, 5) + "." + xCpfCnpj.substring(5, 8) + "/" + xCpfCnpj.substring(8, 12) + "-" + xCpfCnpj.substring(12, 14));
			}

			// verifica se o cpf/cnpj ja esta cadastrado
			Cursor rsExiste = banco.Sql_Select("CLIENTES", new String[]
			{
					"_id", "NOME", "SINCRONIZADO"
			}, "cpf_cnpj like '" + edtCPFCNPJ.getText().toString() + "'", null);
			if (rsExiste.moveToFirst())
			{
				int status = 0;
				String situacao = "";

				if (rsExiste.getInt(2) == 0)
				{
					situacao = "Pendente";
				} else if (rsExiste.getInt(2) == 1)
				{
					status = 3;
					situacao = "Enviado";
				} else if (rsExiste.getInt(2) == 2)
				{
					situacao = "Inativo";
					status = 2;

					AlertDialog ad = new AlertDialog.Builder(frm_cad_cliente.this).create();
					ad.setCancelable(true); // This blocks the 'BACK' button
					ad.setTitle("SmartMobile - For�a de Vendas");
					ad.setMessage("CPF/CNPJ j� est� cadastrado !!!\n\nNome: " + rsExiste.getString(1) + "\nSitua��o: " + situacao);
					ad.setButton("Ok", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{

							dialog.dismiss();

						}
					});
					ad.show();
					retorno = false;
					edtCPFCNPJ.requestFocus();
				}
				statusPedido = status;
			}
		}

		return retorno;

	}

	public boolean Valida_CPF(String cpf)
	{
		int d1, d2;
		int digito1, digito2, resto;
		int digitoCPF;
		String nDigResult;

		d1 = d2 = 0;
		digito1 = digito2 = resto = 0;

		for (int nCount = 1; nCount < cpf.length() - 1; nCount++)
		{
			digitoCPF = Integer.valueOf(cpf.substring(nCount - 1, nCount)).intValue();

			// multiplique a ultima casa por 2 a seguinte por 3 a seguinte por 4
			// e assim por diante.
			d1 = d1 + (11 - nCount) * digitoCPF;

			// para o segundo digito repita o procedimento incluindo o primeiro
			// digito calculado no passo anterior.
			d2 = d2 + (12 - nCount) * digitoCPF;
		}
		;

		// Primeiro resto da divis�o por 11.
		resto = (d1 % 11);

		// Se o resultado for 0 ou 1 o digito � 0 caso contr�rio o digito � 11
		// menos o resultado anterior.
		if (resto < 2)
			digito1 = 0;
		else
			digito1 = 11 - resto;

		d2 += 2 * digito1;

		// Segundo resto da divis�o por 11.
		resto = (d2 % 11);

		// Se o resultado for 0 ou 1 o digito � 0 caso contr�rio o digito � 11
		// menos o resultado anterior.
		if (resto < 2)
			digito2 = 0;
		else
			digito2 = 11 - resto;

		// Digito verificador do CPF que est� sendo validado.
		String nDigVerific = cpf.substring(cpf.length() - 2, cpf.length());

		// Concatenando o primeiro resto com o segundo.
		nDigResult = String.valueOf(digito1) + String.valueOf(digito2);

		// comparar o digito verificador do cpf com o primeiro resto + o segundo
		// resto.
		return nDigVerific.equals(nDigResult);
	}

	public boolean Valida_CNPJ(String CNPJ)
	{
		// considera-se erro CNPJ's formados por uma sequencia de numeros iguais
		if (CNPJ.equals("00000000000000") || CNPJ.equals("11111111111111") || CNPJ.equals("22222222222222") || CNPJ.equals("33333333333333") || CNPJ.equals("44444444444444") || CNPJ.equals("55555555555555") || CNPJ.equals("66666666666666") || CNPJ.equals("77777777777777") || CNPJ.equals("88888888888888") || CNPJ.equals("99999999999999") || (CNPJ.length() != 14))
			return (false);

		char dig13, dig14;
		int sm, i, r, num, peso;

		// "try" - protege o c�digo para eventuais erros de conversao de tipo
		// (int)
		try
		{
			// Calculo do 1o. Digito Verificador
			sm = 0;
			peso = 2;
			for (i = 11; i >= 0; i--)
			{
				// converte o i-�simo caractere do CNPJ em um n�mero:
				// por exemplo, transforma o caractere '0' no inteiro 0
				// (48 eh a posi��o de '0' na tabela ASCII)
				num = (int) (CNPJ.charAt(i) - 48);
				sm = sm + (num * peso);
				peso = peso + 1;
				if (peso == 10)
					peso = 2;
			}

			r = sm % 11;
			if ((r == 0) || (r == 1))
				dig13 = '0';
			else
				dig13 = (char) ((11 - r) + 48);

			// Calculo do 2o. Digito Verificador
			sm = 0;
			peso = 2;
			for (i = 12; i >= 0; i--)
			{
				num = (int) (CNPJ.charAt(i) - 48);
				sm = sm + (num * peso);
				peso = peso + 1;
				if (peso == 10)
					peso = 2;
			}

			r = sm % 11;
			if ((r == 0) || (r == 1))
				dig14 = '0';
			else
				dig14 = (char) ((11 - r) + 48);

			// Verifica se os d�gitos calculados conferem com os d�gitos
			// informados.
			if ((dig13 == CNPJ.charAt(12)) && (dig14 == CNPJ.charAt(13)))
				return (true);
			else
				return (false);
		} catch (InputMismatchException erro)
		{
			return (false);
		}
	}

	private String[] listUFs = new String[]
	{
			"AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO", "MA", "MG", "MS", "MT", "PA", "PB", "PE", "PI", "PR", "RJ", "RN", "RO", "RR", "RS", "SC", "SE", "SP", "TO"
	};

	private String[] listCidades = new String[]
	{
			"Indefinida - 0 ", "Abadia de Goias - GO", "Abadia dos Dourados - MG", "Abadiania - GO", "Abaete - MG", "Abaetetuba - PA", "Abaiara - CE", "Abaira - BA", "Abare - BA", "Abatia - PR", "Abdon Batista - SC", "Abel Figueiredo - PA", "Abelardo Luz - SC", "Abre Campo - MG", "Abreu e Lima - PE", "Abreulandia - TO", "Acaiaca - MG", "Acailandia - MA", "Acajutiba - BA", "Acara - PA", "Acarape - CE", "Acarau - CE", "Acari - RN", "Acaua - PI", "Acegua - RS", "Acopiara - CE", "Acorizal - MT", "Acrelandia - AC", "Acreuna - GO", "Acu - RN", "Acucena - MG", "Adamantina - SP", "Adelandia - GO", "Adolfo - SP", "Adrianopolis - PR", "Adustina - BA", "Afogados da Ingazeira - PE", "Afonso Bezerra - RN", "Afonso Claudio - ES", "Afonso Cunha - MA", "Afranio - PE", "Afua - PA", "Agrestina - PE", "Agricolandia - PI", "Agrolandia - SC", "Agronomica - SC", "agua Azul do Norte - PA", "agua Boa - MT", "agua Boa - MG", "agua Branca - AL", "agua Branca - PB", "agua Branca - PI", "agua Clara - MS",
			"agua Comprida - MG", "agua Doce - SC", "agua Doce do Maranhao - MA", "agua Doce do Norte - ES", "agua Fria - BA", "agua Fria de Goias - GO", "agua Limpa - GO", "agua Nova - RN", "agua Preta - PE", "agua Santa - RS", "Aguai - SP", "Aguanil - MG", "aguas Belas - PE", "aguas da Prata - SP", "aguas de Chapeco - SC", "aguas de Lindoia - SP", "aguas de Santa Barbara - SP", "aguas de Sao Pedro - SP", "aguas Formosas - MG", "aguas Frias - SC", "aguas Lindas de Goias - GO", "aguas Mornas - SC", "aguas Vermelhas - MG", "Agudo - RS", "Agudos - SP", "Agudos do Sul - PR", "aguia Branca - ES", "Aguiar - PB", "Aguiarnopolis - TO", "Aimores - MG", "Aiquara - BA", "Aiuaba - CE", "Aiuruoca - MG", "Ajuricaba - RS", "Alagoa - MG", "Alagoa Grande - PB", "Alagoa Nova - PB", "Alagoinha - PB", "Alagoinha - PE", "Alagoinha do Piaui - PI", "Alagoinhas - BA", "Alambari - SP", "Albertina - MG", "Alcantara - MA", "Alcantaras - CE", "Alcantil - PB", "Alcinopolis - MS", "Alcobaca - BA",
			"Aldeias Altas - MA", "Alecrim - RS", "Alegre - ES", "Alegrete - RS", "Alegrete do Piaui - PI", "Alegria - RS", "Alem Paraiba - MG", "Alenquer - PA", "Alexandria - RN", "Alexania - GO", "Alfenas - MG", "Alfredo Chaves - ES", "Alfredo Marcondes - SP", "Alfredo Vasconcelos - MG", "Alfredo Wagner - SC", "Algodao de Jandaira - PB", "Alhandra - PB", "Alianca - PE", "Alianca do Tocantins - TO", "Almadina - BA", "Almas - TO", "Almeirim - PA", "Almenara - MG", "Almino Afonso - RN", "Almirante Tamandare - PR", "Almirante Tamandare do Sul - RS", "Aloandia - GO", "Alpercata - MG", "Alpestre - RS", "Alpinopolis - MG", "Alta Floresta - MT", "Alta Floresta DOeste - RO", "Altair - SP", "Altamira - PA", "Altamira do Maranhao - MA", "Altamira do Parana - PR", "Altaneira - CE", "Alterosa - MG", "Altinho - PE", "Altinopolis - SP", "Alto Alegre - SP", "Alto Alegre - RS", "Alto Alegre - RR", "Alto Alegre do Maranhao - MA", "Alto Alegre do Pindare - MA", "Alto Alegre dos Parecis - RO",
			"Alto Araguaia - MT", "Alto Bela Vista - SC", "Alto Boa Vista - MT", "Alto Caparao - MG", "Alto do Rodrigues - RN", "Alto Feliz - RS", "Alto Garcas - MT", "Alto Horizonte - GO", "Alto Jequitiba - MG", "Alto Longa - PI", "Alto Paraguai - MT", "Alto Paraiso - RO", "Alto Paraiso - PR", "Alto Paraiso de Goias - GO", "Alto Parana - PR", "Alto Parnaiba - MA", "Alto Piquiri - PR", "Alto Rio Doce - MG", "Alto Rio Novo - ES", "Alto Santo - CE", "Alto Taquari - MT", "Altonia - PR", "Altos - PI", "Aluminio - SP", "Alvaraes - AM", "Alvarenga - MG", "alvares Florence - SP", "alvares Machado - SP", "alvaro de Carvalho - SP", "Alvinlandia - SP", "Alvinopolis - MG", "Alvorada - TO", "Alvorada - RS", "Alvorada de Minas - MG", "Alvorada do Gurgueia - PI", "Alvorada do Norte - GO", "Alvorada do Sul - PR", "Alvorada DOeste - RO", "Amajari - RR", "Amambai - MS", "Amapa - AP", "Amapa do Maranhao - MA", "Amapora - PR", "Amaraji - PE", "Amaral Ferrador - RS", "Amaralina - GO", "Amarante - PI",
			"Amarante do Maranhao - MA", "Amargosa - BA", "Amatura - AM", "Amelia Rodrigues - BA", "America Dourada - BA", "Americana - SP", "Americano do Brasil - GO", "Americo Brasiliense - SP", "Americo de Campos - SP", "Ametista do Sul - RS", "Amontada - CE", "Amorinopolis - GO", "Amparo - SP", "Amparo - PB", "Amparo de Sao Francisco - SE", "Amparo do Serra - MG", "Ampere - PR", "Anadia - AL", "Anage - BA", "Anahy - PR", "Anajas - PA", "Anajatuba - MA", "Analandia - SP", "Anama - AM", "Ananas - TO", "Ananindeua - PA", "Anapolis - GO", "Anapu - PA", "Anapurus - MA", "Anastacio - MS", "Anaurilandia - MS", "Anchieta - ES", "Anchieta - SC", "Andarai - BA", "Andira - PR", "Andorinha - BA", "Andradas - MG", "Andradina - SP", "Andre da Rocha - RS", "Andrelandia - MG", "Angatuba - SP", "Angelandia - MG", "Angelica - MS", "Angelim - PE", "Angelina - SC", "Angical - BA", "Angical do Piaui - PI", "Angico - TO", "Angicos - RN", "Angra dos Reis - RJ", "Anguera - BA", "angulo - PR",
			"Anhanguera - GO", "Anhembi - SP", "Anhumas - SP", "Anicuns - GO", "Anisio de Abreu - PI", "Anita Garibaldi - SC", "Anitapolis - SC", "Anori - AM", "Anta Gorda - RS", "Antas - BA", "Antonina - PR", "Antonina do Norte - CE", "Antonio Almeida - PI", "Antonio Cardoso - BA", "Antonio Carlos - MG", "Antonio Carlos - SC", "Antonio Dias - MG", "Antonio Goncalves - BA", "Antonio Joao - MS", "Antonio Martins - RN", "Antonio Olinto - PR", "Antonio Prado - RS", "Antonio Prado de Minas - MG", "Aparecida - PB", "Aparecida - SP", "Aparecida de Goiania - GO", "Aparecida do Rio Doce - GO", "Aparecida do Rio Negro - TO", "Aparecida do Taboado - MS", "Aparecida dOeste - SP", "Aperibe - RJ", "Apiaca - ES", "Apiacas - MT", "Apiai - SP", "Apicum-Acu - MA", "Apiuna - SC", "Apodi - RN", "Apora - BA", "Apore - GO", "Apuarema - BA", "Apucarana - PR", "Apui - AM", "Apuiares - CE", "Aquidaba - SE", "Aquidauana - MS", "Aquiraz - CE", "Arabuta - SC", "Aracagi - PB", "Aracai - MG", "Aracaju - SE",
			"Aracariguama - SP", "Aracas - BA", "Aracati - CE", "Aracatu - BA", "Aracatuba - SP", "Araci - BA", "Aracitaba - MG", "Aracoiaba - CE", "Aracoiaba - PE", "Aracoiaba da Serra - SP", "Aracruz - ES", "Aracu - GO", "Aracuai - MG", "Aragarcas - GO", "Aragoiania - GO", "Aragominas - TO", "Araguacema - TO", "Araguacu - TO", "Araguaiana - MT", "Araguaina - TO", "Araguainha - MT", "Araguana - MA", "Araguana - TO", "Araguapaz - GO", "Araguari - MG", "Araguatins - TO", "Araioses - MA", "Aral Moreira - MS", "Aramari - BA", "Arambare - RS", "Arame - MA", "Aramina - SP", "Arandu - SP", "Arantina - MG", "Arapei - SP", "Arapiraca - AL", "Arapoema - TO", "Araponga - MG", "Arapongas - PR", "Arapora - MG", "Arapoti - PR", "Arapua - MG", "Arapua - PR", "Araputanga - MT", "Araquari - SC", "Arara - PB", "Ararangua - SC", "Araraquara - SP", "Araras - SP", "Ararenda - CE", "Arari - MA", "Ararica - RS", "Araripe - CE", "Araripina - PE", "Araruama - RJ", "Araruna - PR", "Araruna - PB",
			"Arataca - BA", "Aratiba - RS", "Aratuba - CE", "Aratuipe - BA", "Araua - SE", "Araucaria - PR", "Araujos - MG", "Araxa - MG", "Arceburgo - MG", "Arco-iris - SP", "Arcos - MG", "Arcoverde - PE", "Areado - MG", "Areal - RJ", "Arealva - SP", "Areia - PB", "Areia Branca - RN", "Areia Branca - SE", "Areia de Baraunas - PB", "Areial - PB", "Areias - SP", "Areiopolis - SP", "Arenapolis - MT", "Arenopolis - GO", "Ares - RN", "Argirita - MG", "Aricanduva - MG", "Arinos - MG", "Aripuana - MT", "Ariquemes - RO", "Ariranha - SP", "Ariranha do Ivai - PR", "Armacao dos Buzios - RJ", "Armazem - SC", "Arneiroz - CE", "Aroazes - PI", "Aroeiras - PB", "Aroeiras do Itaim - PI", "Arraial - PI", "Arraial do Cabo - RJ", "Arraias - TO", "Arroio do Meio - RS", "Arroio do Padre - RS", "Arroio do Sal - RS", "Arroio do Tigre - RS", "Arroio dos Ratos - RS", "Arroio Grande - RS", "Arroio Trinta - SC", "Artur Nogueira - SP", "Aruana - GO", "Aruja - SP", "Arvoredo - SC", "Arvorezinha - RS",
			"Ascurra - SC", "Aspasia - SP", "Assai - PR", "Assare - CE", "Assis - SP", "Assis Brasil - AC", "Assis Chateaubriand - PR", "Assuncao - PB", "Assuncao do Piaui - PI", "Astolfo Dutra - MG", "Astorga - PR", "Atalaia - AL", "Atalaia - PR", "Atalaia do Norte - AM", "Atalanta - SC", "Ataleia - MG", "Atibaia - SP", "Atilio Vivacqua - ES", "Augustinopolis - TO", "Augusto Correa - PA", "Augusto de Lima - MG", "Augusto Pestana - RS", "Augusto Severo - RN", "aurea - RS", "Aurelino Leal - BA", "Auriflama - SP", "Aurilandia - GO", "Aurora - SC", "Aurora - CE", "Aurora do Para - PA", "Aurora do Tocantins - TO", "Autazes - AM", "Avai - SP", "Avanhandava - SP", "Avare - SP", "Aveiro - PA", "Avelino Lopes - PI", "Avelinopolis - GO", "Axixa - MA", "Axixa do Tocantins - TO", "Babaculandia - TO", "Bacabal - MA", "Bacabeira - MA", "Bacuri - MA", "Bacurituba - MA", "Bady Bassitt - SP", "Baependi - MG", "Bage - RS", "Bagre - PA", "Baia da Traicao - PB", "Baia Formosa - RN", "Baianopolis - BA",
			"Baiao - PA", "Baixa Grande - BA", "Baixa Grande do Ribeiro - PI", "Baixio - CE", "Baixo Guandu - ES", "Balbinos - SP", "Baldim - MG", "Baliza - GO", "Balneario Arroio do Silva - SC", "Balneario Barra do Sul - SC", "Balneario Camboriu - SC", "Balneario Gaivota - SC", "Balneario Picarras - SC", "Balneario Pinhal - RS", "Balsa Nova - PR", "Balsamo - SP", "Balsas - MA", "Bambui - MG", "Banabuiu - CE", "Bananal - SP", "Bananeiras - PB", "Bandeira - MG", "Bandeira do Sul - MG", "Bandeirante - SC", "Bandeirantes - MS", "Bandeirantes - PR", "Bandeirantes do Tocantins - TO", "Bannach - PA", "Banzae - BA", "Barao - RS", "Barao de Antonina - SP", "Barao de Cocais - MG", "Barao de Cotegipe - RS", "Barao de Grajau - MA", "Barao de Melgaco - MT", "Barao de Monte Alto - MG", "Barao do Triunfo - RS", "Barauna - RN", "Barauna - PB", "Barbacena - MG", "Barbalha - CE", "Barbosa - SP", "Barbosa Ferraz - PR", "Barcarena - PA", "Barcelona - RN", "Barcelos - AM", "Bariri - SP", "Barra - BA",
			"Barra Bonita - SP", "Barra Bonita - SC", "Barra da Estiva - BA", "Barra DAlcantara - PI", "Barra de Guabiraba - PE", "Barra de Santa Rosa - PB", "Barra de Santana - PB", "Barra de Santo Antonio - AL", "Barra de Sao Francisco - ES", "Barra de Sao Miguel - AL", "Barra de Sao Miguel - PB", "Barra do Bugres - MT", "Barra do Chapeu - SP", "Barra do Choca - BA", "Barra do Corda - MA", "Barra do Garcas - MT", "Barra do Guarita - RS", "Barra do Jacare - PR", "Barra do Mendes - BA", "Barra do Ouro - TO", "Barra do Pirai - RJ", "Barra do Quarai - RS", "Barra do Ribeiro - RS", "Barra do Rio Azul - RS", "Barra do Rocha - BA", "Barra do Turvo - SP", "Barra dos Coqueiros - SE", "Barra Funda - RS", "Barra Longa - MG", "Barra Mansa - RJ", "Barra Velha - SC", "Barracao - RS", "Barracao - PR", "Barras - PI", "Barreira - CE", "Barreiras - BA", "Barreiras do Piaui - PI", "Barreirinha - AM", "Barreirinhas - MA", "Barreiros - PE", "Barretos - SP", "Barrinha - SP", "Barro - CE",
			"Barro Alto - GO", "Barro Alto - BA", "Barro Duro - PI", "Barro Preto - BA", "Barrocas - BA", "Barrolandia - TO", "Barroquinha - CE", "Barros Cassal - RS", "Barroso - MG", "Barueri - SP", "Bastos - SP", "Bataguassu - MS", "Batalha - PI", "Batalha - AL", "Batatais - SP", "Bataypora - MS", "Baturite - CE", "Bauru - SP", "Bayeux - PB", "Bebedouro - SP", "Beberibe - CE", "Bela Cruz - CE", "Bela Vista - MS", "Bela Vista da Caroba - PR", "Bela Vista de Goias - GO", "Bela Vista de Minas - MG", "Bela Vista do Maranhao - MA", "Bela Vista do Paraiso - PR", "Bela Vista do Piaui - PI", "Bela Vista do Toldo - SC", "Belagua - MA", "Belem - PB", "Belem - PA", "Belem - AL", "Belem de Maria - PE", "Belem de Sao Francisco - PE", "Belem do Brejo do Cruz - PB", "Belem do Piaui - PI", "Belford Roxo - RJ", "Belmiro Braga - MG", "Belmonte - BA", "Belmonte - SC", "Belo Campo - BA", "Belo Horizonte - MG", "Belo Jardim - PE", "Belo Monte - AL", "Belo Oriente - MG", "Belo Vale - MG",
			"Belterra - PA", "Beneditinos - PI", "Benedito Leite - MA", "Benedito Novo - SC", "Benevides - PA", "Benjamin Constant - AM", "Benjamin Constant do Sul - RS", "Bento de Abreu - SP", "Bento Fernandes - RN", "Bento Goncalves - RS", "Bequimao - MA", "Berilo - MG", "Berizal - MG", "Bernardino Batista - PB", "Bernardino de Campos - SP", "Bernardo do Mearim - MA", "Bernardo Sayao - TO", "Bertioga - SP", "Bertolinia - PI", "Bertopolis - MG", "Beruri - AM", "Betania - PE", "Betania do Piaui - PI", "Betim - MG", "Bezerros - PE", "Bias Fortes - MG", "Bicas - MG", "Biguacu - SC", "Bilac - SP", "Biquinhas - MG", "Birigui - SP", "Biritiba-Mirim - SP", "Biritinga - BA", "Bituruna - PR", "Blumenau - SC", "Boa Esperanca - MG", "Boa Esperanca - ES", "Boa Esperanca - PR", "Boa Esperanca do Iguacu - PR", "Boa Esperanca do Sul - SP", "Boa Hora - PI", "Boa Nova - BA", "Boa Ventura - PB", "Boa Ventura de Sao Roque - PR", "Boa Viagem - CE", "Boa Vista - PB", "Boa Vista - RR",
			"Boa Vista da Aparecida - PR", "Boa Vista das Missoes - RS", "Boa Vista do Burica - RS", "Boa Vista do Cadeado - RS", "Boa Vista do Gurupi - MA", "Boa Vista do Incra - RS", "Boa Vista do Ramos - AM", "Boa Vista do Sul - RS", "Boa Vista do Tupim - BA", "Boca da Mata - AL", "Boca do Acre - AM", "Bocaina - PI", "Bocaina - SP", "Bocaina de Minas - MG", "Bocaina do Sul - SC", "Bocaiuva - MG", "Bocaiuva do Sul - PR", "Bodo - RN", "Bodoco - PE", "Bodoquena - MS", "Bofete - SP", "Boituva - SP", "Bom Conselho - PE", "Bom Despacho - MG", "Bom Jardim - RJ", "Bom Jardim - MA", "Bom Jardim - PE", "Bom Jardim da Serra - SC", "Bom Jardim de Goias - GO", "Bom Jardim de Minas - MG", "Bom Jesus - SC", "Bom Jesus - RN", "Bom Jesus - RS", "Bom Jesus - PI", "Bom Jesus - PB", "Bom Jesus da Lapa - BA", "Bom Jesus da Penha - MG", "Bom Jesus da Serra - BA", "Bom Jesus das Selvas - MA", "Bom Jesus de Goias - GO", "Bom Jesus do Amparo - MG", "Bom Jesus do Araguaia - MT", "Bom Jesus do Galho - MG",
			"Bom Jesus do Itabapoana - RJ", "Bom Jesus do Norte - ES", "Bom Jesus do Oeste - SC", "Bom Jesus do Sul - PR", "Bom Jesus do Tocantins - TO", "Bom Jesus do Tocantins - PA", "Bom Jesus dos Perdoes - SP", "Bom Lugar - MA", "Bom Principio - RS", "Bom Principio do Piaui - PI", "Bom Progresso - RS", "Bom Repouso - MG", "Bom Retiro - SC", "Bom Retiro do Sul - RS", "Bom Sucesso - PB", "Bom Sucesso - MG", "Bom Sucesso - PR", "Bom Sucesso de Itarare - SP", "Bom Sucesso do Sul - PR", "Bombinhas - SC", "Bonfim - MG", "Bonfim - RR", "Bonfim do Piaui - PI", "Bonfinopolis - GO", "Bonfinopolis de Minas - MG", "Boninal - BA", "Bonito - MS", "Bonito - BA", "Bonito - PA", "Bonito - PE", "Bonito de Minas - MG", "Bonito de Santa Fe - PB", "Bonopolis - GO", "Boqueirao - PB", "Boqueirao do Leao - RS", "Boqueirao do Piaui - PI", "Boquim - SE", "Boquira - BA", "Bora - SP", "Boraceia - SP", "Borba - AM", "Borborema - PB", "Borborema - SP", "Borda da Mata - MG", "Borebi - SP", "Borrazopolis - PR",
			"Bossoroca - RS", "Botelhos - MG", "Botucatu - SP", "Botumirim - MG", "Botupora - BA", "Botuvera - SC", "Bozano - RS", "Braco do Norte - SC", "Braco do Trombudo - SC", "Braga - RS", "Braganca - PA", "Braganca Paulista - SP", "Braganey - PR", "Branquinha - AL", "Bras Pires - MG", "Brasil Novo - PA", "Brasilandia - MS", "Brasilandia de Minas - MG", "Brasilandia do Sul - PR", "Brasilandia do Tocantins - TO", "Brasileia - AC", "Brasileira - PI", "Brasilia - DF", "Brasilia de Minas - MG", "Brasnorte - MT", "Brasopolis - MG", "Brauna - SP", "Braunas - MG", "Brazabrantes - GO", "Brejao - PE", "Brejetuba - ES", "Brejinho - RN", "Brejinho - PE", "Brejinho de Nazare - TO", "Brejo - MA", "Brejo Alegre - SP", "Brejo da Madre de Deus - PE", "Brejo de Areia - MA", "Brejo do Cruz - PB", "Brejo do Piaui - PI", "Brejo dos Santos - PB", "Brejo Grande - SE", "Brejo Grande do Araguaia - PA", "Brejo Santo - CE", "Brejoes - BA", "Brejolandia - BA", "Breu Branco - PA", "Breves - PA",
			"Britania - GO", "Brochier - RS", "Brodowski - SP", "Brotas - SP", "Brotas de Macaubas - BA", "Brumadinho - MG", "Brumado - BA", "Brunopolis - SC", "Brusque - SC", "Bueno Brandao - MG", "Buenopolis - MG", "Buenos Aires - PE", "Buerarema - BA", "Bugre - MG", "Buique - PE", "Bujari - AC", "Bujaru - PA", "Buri - SP", "Buritama - SP", "Buriti - MA", "Buriti Alegre - GO", "Buriti Bravo - MA", "Buriti de Goias - GO", "Buriti do Tocantins - TO", "Buriti dos Lopes - PI", "Buriti dos Montes - PI", "Buriticupu - MA", "Buritinopolis - GO", "Buritirama - BA", "Buritirana - MA", "Buritis - RO", "Buritis - MG", "Buritizal - SP", "Buritizeiro - MG", "Butia - RS", "Caapiranga - AM", "Caapora - PB", "Caarapo - MS", "Caatiba - BA", "Cabaceiras - PB", "Cabaceiras do Paraguacu - BA", "Cabeceira Grande - MG", "Cabeceiras - GO", "Cabeceiras do Piaui - PI", "Cabedelo - PB", "Cabixi - RO", "Cabo de Santo Agostinho - PE", "Cabo Frio - RJ", "Cabo Verde - MG", "Cabralia Paulista - SP",
			"Cabreuva - SP", "Cabrobo - PE", "Cacador - SC", "Cacapava - SP", "Cacapava do Sul - RS", "Cacaulandia - RO", "Cacequi - RS", "Caceres - MT", "Cachoeira - BA", "Cachoeira Alta - GO", "Cachoeira da Prata - MG", "Cachoeira de Goias - GO", "Cachoeira de Minas - MG", "Cachoeira de Pajeu - MG", "Cachoeira do Arari - PA", "Cachoeira do Piria - PA", "Cachoeira do Sul - RS", "Cachoeira dos indios - PB", "Cachoeira Dourada - GO", "Cachoeira Dourada - MG", "Cachoeira Grande - MA", "Cachoeira Paulista - SP", "Cachoeiras de Macacu - RJ", "Cachoeirinha - TO", "Cachoeirinha - RS", "Cachoeirinha - PE", "Cachoeiro de Itapemirim - ES", "Cacimba de Areia - PB", "Cacimba de Dentro - PB", "Cacimbas - PB", "Cacimbinhas - AL", "Cacique Doble - RS", "Cacoal - RO", "Caconde - SP", "Cacu - GO", "Cacule - BA", "Caem - BA", "Caetanopolis - MG", "Caetanos - BA", "Caete - MG", "Caetes - PE", "Caetite - BA", "Cafarnaum - BA", "Cafeara - PR", "Cafelandia - SP", "Cafelandia - PR", "Cafezal do Sul - PR",
			"Caiabu - SP", "Caiana - MG", "Caiaponia - GO", "Caibate - RS", "Caibi - SC", "Caicara - PB", "Caicara - RS", "Caicara do Norte - RN", "Caicara do Rio do Vento - RN", "Caico - RN", "Caieiras - SP", "Cairu - BA", "Caiua - SP", "Cajamar - SP", "Cajapio - MA", "Cajari - MA", "Cajati - SP", "Cajazeiras - PB", "Cajazeiras do Piaui - PI", "Cajazeirinhas - PB", "Cajobi - SP", "Cajueiro - AL", "Cajueiro da Praia - PI", "Cajuri - MG", "Cajuru - SP", "Calcado - PE", "Calcoene - AP", "Caldas - MG", "Caldas Brandao - PB", "Caldas Novas - GO", "Caldazinha - GO", "Caldeirao Grande - BA", "Caldeirao Grande do Piaui - PI", "California - PR", "Calmon - SC", "Calumbi - PE", "Camacan - BA", "Camacari - BA", "Camacho - MG", "Camalau - PB", "Camamu - BA", "Camanducaia - MG", "Camapua - MS", "Camaqua - RS", "Camaragibe - PE", "Camargo - RS", "Cambara - PR", "Cambara do Sul - RS", "Cambe - PR", "Cambira - PR", "Camboriu - SC", "Cambuci - RJ", "Cambui - MG", "Cambuquira - MG", "Cameta - PA",
			"Camocim - CE", "Camocim de Sao Felix - PE", "Campanario - MG", "Campanha - MG", "Campestre - MG", "Campestre - AL", "Campestre da Serra - RS", "Campestre de Goias - GO", "Campestre do Maranhao - MA", "Campina da Lagoa - PR", "Campina das Missoes - RS", "Campina do Monte Alegre - SP", "Campina do Simao - PR", "Campina Grande - PB", "Campina Grande do Sul - PR", "Campina Verde - MG", "Campinacu - GO", "Campinapolis - MT", "Campinas - SP", "Campinas do Piaui - PI", "Campinas do Sul - RS", "Campinorte - GO", "Campo Alegre - SC", "Campo Alegre - AL", "Campo Alegre de Goias - GO", "Campo Alegre de Lourdes - BA", "Campo Alegre do Fidalgo - PI", "Campo Azul - MG", "Campo Belo - MG", "Campo Belo do Sul - SC", "Campo Bom - RS", "Campo Bonito - PR", "Campo de Santana - PB", "Campo do Brito - SE", "Campo do Meio - MG", "Campo do Tenente - PR", "Campo Ere - SC", "Campo Florido - MG", "Campo Formoso - BA", "Campo Grande - MS", "Campo Grande - AL", "Campo Grande do Piaui - PI",
			"Campo Largo - PR", "Campo Largo do Piaui - PI", "Campo Limpo de Goias - GO", "Campo Limpo Paulista - SP", "Campo Magro - PR", "Campo Maior - PI", "Campo Mourao - PR", "Campo Novo - RS", "Campo Novo de Rondonia - RO", "Campo Novo do Parecis - MT", "Campo Redondo - RN", "Campo Verde - MT", "Campos Altos - MG", "Campos Belos - GO", "Campos Borges - RS", "Campos de Julio - MT", "Campos do Jordao - SP", "Campos dos Goytacazes - RJ", "Campos Gerais - MG", "Campos Lindos - TO", "Campos Novos - SC", "Campos Novos Paulista - SP", "Campos Sales - CE", "Campos Verdes - GO", "Camutanga - PE", "Cana Verde - MG", "Canaa - MG", "Canaa dos Carajas - PA", "Canabrava do Norte - MT", "Cananeia - SP", "Canapi - AL", "Canapolis - BA", "Canapolis - MG", "Canarana - MT", "Canarana - BA", "Canas - SP", "Canavieira - PI", "Canavieiras - BA", "Candeal - BA", "Candeias - BA", "Candeias - MG", "Candeias do Jamari - RO", "Candelaria - RS", "Candiba - BA", "Candido de Abreu - PR",
			"Candido Godoi - RS", "Candido Mendes - MA", "Candido Mota - SP", "Candido Rodrigues - SP", "Candido Sales - BA", "Candiota - RS", "Candoi - PR", "Canela - RS", "Canelinha - SC", "Canguaretama - RN", "Cangucu - RS", "Canhoba - SE", "Canhotinho - PE", "Caninde - CE", "Caninde de Sao Francisco - SE", "Canitar - SP", "Canoas - RS", "Canoinhas - SC", "Cansancao - BA", "Canta - RR", "Cantagalo - MG", "Cantagalo - PR", "Cantagalo - RJ", "Cantanhede - MA", "Canto do Buriti - PI", "Canudos - BA", "Canudos do Vale - RS", "Canutama - AM", "Capanema - PA", "Capanema - PR", "Capao Alto - SC", "Capao Bonito - SP", "Capao Bonito do Sul - RS", "Capao da Canoa - RS", "Capao do Cipo - RS", "Capao do Leao - RS", "Caparao - MG", "Capela - AL", "Capela - SE", "Capela de Santana - RS", "Capela do Alto - SP", "Capela do Alto Alegre - BA", "Capela Nova - MG", "Capelinha - MG", "Capetinga - MG", "Capim - PB", "Capim Branco - MG", "Capim Grosso - BA", "Capinopolis - MG", "Capinzal - SC",
			"Capinzal do Norte - MA", "Capistrano - CE", "Capitao - RS", "Capitao Andrade - MG", "Capitao de Campos - PI", "Capitao Eneas - MG", "Capitao Gervasio Oliveira - PI", "Capitao Leonidas Marques - PR", "Capitao Poco - PA", "Capitolio - MG", "Capivari - SP", "Capivari de Baixo - SC", "Capivari do Sul - RS", "Capixaba - AC", "Capoeiras - PE", "Caputira - MG", "Caraa - RS", "Caracarai - RR", "Caracol - MS", "Caracol - PI", "Caraguatatuba - SP", "Carai - MG", "Caraibas - BA", "Carambei - PR", "Caranaiba - MG", "Carandai - MG", "Carangola - MG", "Carapebus - RJ", "Carapicuiba - SP", "Caratinga - MG", "Carauari - AM", "Caraubas - PB", "Caraubas - RN", "Caraubas do Piaui - PI", "Caravelas - BA", "Carazinho - RS", "Carbonita - MG", "Cardeal da Silva - BA", "Cardoso - SP", "Cardoso Moreira - RJ", "Careacu - MG", "Careiro - AM", "Careiro da Varzea - AM", "Cariacica - ES", "Caridade - CE", "Caridade do Piaui - PI", "Carinhanha - BA", "Carira - SE", "Carire - CE",
			"Cariri do Tocantins - TO", "Caririacu - CE", "Carius - CE", "Carlinda - MT", "Carlopolis - PR", "Carlos Barbosa - RS", "Carlos Chagas - MG", "Carlos Gomes - RS", "Carmesia - MG", "Carmo - RJ", "Carmo da Cachoeira - MG", "Carmo da Mata - MG", "Carmo de Minas - MG", "Carmo do Cajuru - MG", "Carmo do Paranaiba - MG", "Carmo do Rio Claro - MG", "Carmo do Rio Verde - GO", "Carmolandia - TO", "Carmopolis - SE", "Carmopolis de Minas - MG", "Carnaiba - PE", "Carnauba dos Dantas - RN", "Carnaubais - RN", "Carnaubal - CE", "Carnaubeira da Penha - PE", "Carneirinho - MG", "Carneiros - AL", "Caroebe - RR", "Carolina - MA", "Carpina - PE", "Carrancas - MG", "Carrapateira - PB", "Carrasco Bonito - TO", "Caruaru - PE", "Carutapera - MA", "Carvalhopolis - MG", "Carvalhos - MG", "Casa Branca - SP", "Casa Grande - MG", "Casa Nova - BA", "Casca - RS", "Cascalho Rico - MG", "Cascavel - CE", "Cascavel - PR", "Caseara - TO", "Caseiros - RS", "Casimiro de Abreu - RJ", "Casinhas - PE",
			"Casserengue - PB", "Cassia - MG", "Cassia dos Coqueiros - SP", "Cassilandia - MS", "Castanhal - PA", "Castanheira - MT", "Castanheiras - RO", "Castelandia - GO", "Castelo - ES", "Castelo do Piaui - PI", "Castilho - SP", "Castro - PR", "Castro Alves - BA", "Cataguases - MG", "Catalao - GO", "Catanduva - SP", "Catanduvas - PR", "Catanduvas - SC", "Catarina - CE", "Catas Altas - MG", "Catas Altas da Noruega - MG", "Catende - PE", "Catigua - SP", "Catingueira - PB", "Catolandia - BA", "Catole do Rocha - PB", "Catu - BA", "Catuipe - RS", "Catuji - MG", "Catunda - CE", "Caturai - GO", "Caturama - BA", "Caturite - PB", "Catuti - MG", "Caucaia - CE", "Cavalcante - GO", "Caxambu - MG", "Caxambu do Sul - SC", "Caxias - MA", "Caxias do Sul - RS", "Caxingo - PI", "Ceara-Mirim - RN", "Cedral - SP", "Cedral - MA", "Cedro - CE", "Cedro - PE", "Cedro de Sao Joao - SE", "Cedro do Abaete - MG", "Celso Ramos - SC", "Centenario - TO", "Centenario - RS", "Centenario do Sul - PR",
			"Central - BA", "Central de Minas - MG", "Central do Maranhao - MA", "Centralina - MG", "Centro do Guilherme - MA", "Centro Novo do Maranhao - MA", "Cerejeiras - RO", "Ceres - GO", "Cerqueira Cesar - SP", "Cerquilho - SP", "Cerrito - RS", "Cerro Azul - PR", "Cerro Branco - RS", "Cerro Cora - RN", "Cerro Grande - RS", "Cerro Grande do Sul - RS", "Cerro Largo - RS", "Cerro Negro - SC", "Cesario Lange - SP", "Ceu Azul - PR", "Cezarina - GO", "Cha de Alegria - PE", "Cha Grande - PE", "Cha Preta - AL", "Chacara - MG", "Chale - MG", "Chapada - RS", "Chapada da Natividade - TO", "Chapada de Areia - TO", "Chapada do Norte - MG", "Chapada dos Guimaraes - MT", "Chapada Gaucha - MG", "Chapadao do Ceu - GO", "Chapadao do Lageado - SC", "Chapadao do Sul - MS", "Chapadinha - MA", "Chapeco - SC", "Charqueada - SP", "Charqueadas - RS", "Charrua - RS", "Chaval - CE", "Chavantes - SP", "Chaves - PA", "Chiador - MG", "Chiapetta - RS", "Chopinzinho - PR", "Choro - CE", "Chorozinho - CE",
			"Chorrocho - BA", "Chui - RS", "Chupinguaia - RO", "Chuvisca - RS", "Cianorte - PR", "Cicero Dantas - BA", "Cidade Gaucha - PR", "Cidade Ocidental - GO", "Cidelandia - MA", "Cidreira - RS", "Cipo - BA", "Cipotanea - MG", "Ciriaco - RS", "Claraval - MG", "Claro dos Pocoes - MG", "Claudia - MT", "Claudio - MG", "Clementina - SP", "Clevelandia - PR", "Coaraci - BA", "Coari - AM", "Cocal - PI", "Cocal de Telha - PI", "Cocal do Sul - SC", "Cocal dos Alves - PI", "Cocalinho - MT", "Cocalzinho de Goias - GO", "Cocos - BA", "Codajas - AM", "Codo - MA", "Coelho Neto - MA", "Coimbra - MG", "Coite do Noia - AL", "Coivaras - PI", "Colares - PA", "Colatina - ES", "Colider - MT", "Colina - SP", "Colinas - RS", "Colinas - MA", "Colinas do Sul - GO", "Colinas do Tocantins - TO", "Colmeia - TO", "Colniza - MT", "Colombia - SP", "Colombo - PR", "Colonia do Gurgueia - PI", "Colonia do Piaui - PI", "Colonia Leopoldina - AL", "Colorado - RS", "Colorado - PR", "Colorado do Oeste - RO",
			"Coluna - MG", "Combinado - TO", "Comendador Gomes - MG", "Comendador Levy Gasparian - RJ", "Comercinho - MG", "Comodoro - MT", "Conceicao - PB", "Conceicao da Aparecida - MG", "Conceicao da Barra - ES", "Conceicao da Barra de Minas - MG", "Conceicao da Feira - BA", "Conceicao das Alagoas - MG", "Conceicao das Pedras - MG", "Conceicao de Ipanema - MG", "Conceicao de Macabu - RJ", "Conceicao do Almeida - BA", "Conceicao do Araguaia - PA", "Conceicao do Caninde - PI", "Conceicao do Castelo - ES", "Conceicao do Coite - BA", "Conceicao do Jacuipe - BA", "Conceicao do Lago-Acu - MA", "Conceicao do Mato Dentro - MG", "Conceicao do Para - MG", "Conceicao do Rio Verde - MG", "Conceicao do Tocantins - TO", "Conceicao dos Ouros - MG", "Conchal - SP", "Conchas - SP", "Concordia - SC", "Concordia do Para - PA", "Condado - PB", "Condado - PE", "Conde - BA", "Conde - PB", "Condeuba - BA", "Condor - RS", "Conego Marinho - MG", "Confins - MG", "Confresa - MT", "Congo - PB",
			"Congonhal - MG", "Congonhas - MG", "Congonhas do Norte - MG", "Congonhinhas - PR", "Conquista - MG", "Conquista DOeste - MT", "Conselheiro Lafaiete - MG", "Conselheiro Mairinck - PR", "Conselheiro Pena - MG", "Consolacao - MG", "Constantina - RS", "Contagem - MG", "Contenda - PR", "Contendas do Sincora - BA", "Coqueiral - MG", "Coqueiro Baixo - RS", "Coqueiro Seco - AL", "Coqueiros do Sul - RS", "Coracao de Jesus - MG", "Coracao de Maria - BA", "Corbelia - PR", "Cordeiro - RJ", "Cordeiropolis - SP", "Cordeiros - BA", "Cordilheira Alta - SC", "Cordisburgo - MG", "Cordislandia - MG", "Coreau - CE", "Coremas - PB", "Corguinho - MS", "Coribe - BA", "Corinto - MG", "Cornelio Procopio - PR", "Coroaci - MG", "Coroados - SP", "Coroata - MA", "Coromandel - MG", "Coronel Barros - RS", "Coronel Bicaco - RS", "Coronel Domingos Soares - PR", "Coronel Ezequiel - RN", "Coronel Fabriciano - MG", "Coronel Freitas - SC", "Coronel Joao Pessoa - RN", "Coronel Joao Sa - BA",
			"Coronel Jose Dias - PI", "Coronel Macedo - SP", "Coronel Martins - SC", "Coronel Murta - MG", "Coronel Pacheco - MG", "Coronel Pilar - RS", "Coronel Sapucaia - MS", "Coronel Vivida - PR", "Coronel Xavier Chaves - MG", "Corrego Danta - MG", "Corrego do Bom Jesus - MG", "Corrego do Ouro - GO", "Corrego Fundo - MG", "Corrego Novo - MG", "Correia Pinto - SC", "Corrente - PI", "Correntes - PE", "Correntina - BA", "Cortes - PE", "Corumba - MS", "Corumba de Goias - GO", "Corumbaiba - GO", "Corumbatai - SP", "Corumbatai do Sul - PR", "Corumbiara - RO", "Corupa - SC", "Coruripe - AL", "Cosmopolis - SP", "Cosmorama - SP", "Costa Marques - RO", "Costa Rica - MS", "Cotegipe - BA", "Cotia - SP", "Cotipora - RS", "Cotriguacu - MT", "Couto de Magalhaes - TO", "Couto de Magalhaes de Minas - MG", "Coxilha - RS", "Coxim - MS", "Coxixola - PB", "Craibas - AL", "Crateus - CE", "Crato - CE", "Cravinhos - SP", "Cravolandia - BA", "Criciuma - SC", "Crisolita - MG", "Crisopolis - BA",
			"Crissiumal - RS", "Cristais - MG", "Cristais Paulista - SP", "Cristal - RS", "Cristal do Sul - RS", "Cristalandia - TO", "Cristalandia do Piaui - PI", "Cristalia - MG", "Cristalina - GO", "Cristiano Otoni - MG", "Cristianopolis - GO", "Cristina - MG", "Cristinapolis - SE", "Cristino Castro - PI", "Cristopolis - BA", "Crixas - GO", "Crixas do Tocantins - TO", "Croata - CE", "Crominia - GO", "Crucilandia - MG", "Cruz - CE", "Cruz Alta - RS", "Cruz das Almas - BA", "Cruz do Espirito Santo - PB", "Cruz Machado - PR", "Cruzalia - SP", "Cruzaltense - RS", "Cruzeiro - SP", "Cruzeiro da Fortaleza - MG", "Cruzeiro do Iguacu - PR", "Cruzeiro do Oeste - PR", "Cruzeiro do Sul - PR", "Cruzeiro do Sul - RS", "Cruzeiro do Sul - AC", "Cruzeta - RN", "Cruzilia - MG", "Cruzmaltina - PR", "Cubatao - SP", "Cubati - PB", "Cuiaba - MT", "Cuite - PB", "Cuite de Mamanguape - PB", "Cuitegi - PB", "Cujubim - RO", "Cumari - GO", "Cumaru - PE", "Cumaru do Norte - PA", "Cumbe - SE", "Cunha - SP",
			"Cunha Pora - SC", "Cunhatai - SC", "Cuparaque - MG", "Cupira - PE", "Curaca - BA", "Curimata - PI", "Curionopolis - PA", "Curitiba - PR", "Curitibanos - SC", "Curiuva - PR", "Currais - PI", "Currais Novos - RN", "Curral de Cima - PB", "Curral de Dentro - MG", "Curral Novo do Piaui - PI", "Curral Velho - PB", "Curralinho - PA", "Curralinhos - PI", "Curua - PA", "Curuca - PA", "Cururupu - MA", "Curvelandia - MT", "Curvelo - MG", "Custodia - PE", "Cutias - AP", "Damianopolis - GO", "Damiao - PB", "Damolandia - GO", "Darcinopolis - TO", "Dario Meira - BA", "Datas - MG", "David Canabarro - RS", "Davinopolis - GO", "Davinopolis - MA", "Delfim Moreira - MG", "Delfinopolis - MG", "Delmiro Gouveia - AL", "Delta - MG", "Demerval Lobao - PI", "Denise - MT", "Deodapolis - MS", "Deputado Irapuan Pinheiro - CE", "Derrubadas - RS", "Descalvado - SP", "Descanso - SC", "Descoberto - MG", "Desterro - PB", "Desterro de Entre Rios - MG", "Desterro do Melo - MG", "Dezesseis de Novembro - RS",
			"Diadema - SP", "Diamante - PB", "Diamante do Norte - PR", "Diamante do Sul - PR", "Diamante DOeste - PR", "Diamantina - MG", "Diamantino - MT", "Dianopolis - TO", "Dias davila - BA", "Dilermando de Aguiar - RS", "Diogo de Vasconcelos - MG", "Dionisio - MG", "Dionisio Cerqueira - SC", "Diorama - GO", "Dirce Reis - SP", "Dirceu Arcoverde - PI", "Divina Pastora - SE", "Divinesia - MG", "Divino - MG", "Divino das Laranjeiras - MG", "Divino de Sao Lourenco - ES", "Divinolandia - SP", "Divinolandia de Minas - MG", "Divinopolis - MG", "Divinopolis de Goias - GO", "Divinopolis do Tocantins - TO", "Divisa Alegre - MG", "Divisa Nova - MG", "Divisopolis - MG", "Dobrada - SP", "Dois Corregos - SP", "Dois Irmaos - RS", "Dois Irmaos das Missoes - RS", "Dois Irmaos do Buriti - MS", "Dois Irmaos do Tocantins - TO", "Dois Lajeados - RS", "Dois Riachos - AL", "Dois Vizinhos - PR", "Dolcinopolis - SP", "Dom Aquino - MT", "Dom Basilio - BA", "Dom Bosco - MG", "Dom Cavati - MG",
			"Dom Eliseu - PA", "Dom Expedito Lopes - PI", "Dom Feliciano - RS", "Dom Inocencio - PI", "Dom Joaquim - MG", "Dom Macedo Costa - BA", "Dom Pedrito - RS", "Dom Pedro - MA", "Dom Pedro de Alcantara - RS", "Dom Silverio - MG", "Dom Vicoso - MG", "Domingos Martins - ES", "Domingos Mourao - PI", "Dona Emma - SC", "Dona Eusebia - MG", "Dona Francisca - RS", "Dona Ines - PB", "Dores de Campos - MG", "Dores de Guanhaes - MG", "Dores do Indaia - MG", "Dores do Rio Preto - ES", "Dores do Turvo - MG", "Doresopolis - MG", "Dormentes - PE", "Douradina - PR", "Douradina - MS", "Dourado - SP", "Douradoquara - MG", "Dourados - MS", "Doutor Camargo - PR", "Doutor Mauricio Cardoso - RS", "Doutor Pedrinho - SC", "Doutor Ricardo - RS", "Doutor Severiano - RN", "Doutor Ulysses - PR", "Doverlandia - GO", "Dracena - SP", "Duartina - SP", "Duas Barras - RJ", "Duas Estradas - PB", "Duere - TO", "Dumont - SP", "Duque Bacelar - MA", "Duque de Caxias - RJ", "Durande - MG", "Echapora - SP",
			"Ecoporanga - ES", "Edealina - GO", "Edeia - GO", "Eirunepe - AM", "Eldorado - SP", "Eldorado - MS", "Eldorado do Sul - RS", "Eldorado dos Carajas - PA", "Elesbao Veloso - PI", "Elias Fausto - SP", "Eliseu Martins - PI", "Elisiario - SP", "Elisio Medrado - BA", "Eloi Mendes - MG", "Emas - PB", "Embauba - SP", "Embu - SP", "Embu-Guacu - SP", "Emilianopolis - SP", "Encantado - RS", "Encanto - RN", "Encruzilhada - BA", "Encruzilhada do Sul - RS", "Eneas Marques - PR", "Engenheiro Beltrao - PR", "Engenheiro Caldas - MG", "Engenheiro Coelho - SP", "Engenheiro Navarro - MG", "Engenheiro Paulo de Frontin - RJ", "Engenho Velho - RS", "Entre Folhas - MG", "Entre Rios - SC", "Entre Rios - BA", "Entre Rios de Minas - MG", "Entre Rios do Oeste - PR", "Entre Rios do Sul - RS", "Entre-Ijuis - RS", "Envira - AM", "Epitaciolandia - AC", "Equador - RN", "Erebango - RS", "Erechim - RS", "Erere - CE", "erico Cardoso - BA", "Ermo - SC", "Ernestina - RS", "Erval Grande - RS",
			"Erval Seco - RS", "Erval Velho - SC", "Ervalia - MG", "Escada - PE", "Esmeralda - RS", "Esmeraldas - MG", "Espera Feliz - MG", "Esperanca - PB", "Esperanca do Sul - RS", "Esperanca Nova - PR", "Esperantina - TO", "Esperantina - PI", "Esperantinopolis - MA", "Espigao Alto do Iguacu - PR", "Espigao DOeste - RO", "Espinosa - MG", "Espirito Santo - RN", "Espirito Santo do Dourado - MG", "Espirito Santo do Pinhal - SP", "Espirito Santo do Turvo - SP", "Esplanada - BA", "Espumoso - RS", "Estacao - RS", "Estancia - SE", "Estancia Velha - RS", "Esteio - RS", "Estiva - MG", "Estiva Gerbi - SP", "Estreito - MA", "Estrela - RS", "Estrela Dalva - MG", "Estrela de Alagoas - AL", "Estrela do Indaia - MG", "Estrela do Norte - GO", "Estrela do Norte - SP", "Estrela do Sul - MG", "Estrela dOeste - SP", "Estrela Velha - RS", "Euclides da Cunha - BA", "Euclides da Cunha Paulista - SP", "Eugenio de Castro - RS", "Eugenopolis - MG", "Eunapolis - BA", "Eusebio - CE", "Ewbank da Camara - MG",
			"Extrema - MG", "Extremoz - RN", "Exu - PE", "Fagundes - PB", "Fagundes Varela - RS", "Faina - GO", "Fama - MG", "Faria Lemos - MG", "Farias Brito - CE", "Faro - PA", "Farol - PR", "Farroupilha - RS", "Fartura - SP", "Fartura do Piaui - PI", "Fatima - BA", "Fatima - TO", "Fatima do Sul - MS", "Faxinal - PR", "Faxinal do Soturno - RS", "Faxinal dos Guedes - SC", "Faxinalzinho - RS", "Fazenda Nova - GO", "Fazenda Rio Grande - PR", "Fazenda Vilanova - RS", "Feijo - AC", "Feira da Mata - BA", "Feira de Santana - BA", "Feira Grande - AL", "Feira Nova - PE", "Feira Nova - SE", "Feira Nova do Maranhao - MA", "Felicio dos Santos - MG", "Felipe Guerra - RN", "Felisburgo - MG", "Felixlandia - MG", "Feliz - RS", "Feliz Deserto - AL", "Feliz Natal - MT", "Fenix - PR", "Fernandes Pinheiro - PR", "Fernandes Tourinho - MG", "Fernando de Noronha - PE", "Fernando Falcao - MA", "Fernando Pedroza - RN", "Fernando Prestes - SP", "Fernandopolis - SP", "Fernao - SP",
			"Ferraz de Vasconcelos - SP", "Ferreira Gomes - AP", "Ferreiros - PE", "Ferros - MG", "Fervedouro - MG", "Figueira - PR", "Figueirao - MS", "Figueiropolis - TO", "Figueiropolis DOeste - MT", "Filadelfia - TO", "Filadelfia - BA", "Firmino Alves - BA", "Firminopolis - GO", "Flexeiras - AL", "Flor da Serra do Sul - PR", "Flor do Sertao - SC", "Flora Rica - SP", "Florai - PR", "Florania - RN", "Floreal - SP", "Flores - PE", "Flores da Cunha - RS", "Flores de Goias - GO", "Flores do Piaui - PI", "Floresta - PE", "Floresta - PR", "Floresta Azul - BA", "Floresta do Araguaia - PA", "Floresta do Piaui - PI", "Florestal - MG", "Florestopolis - PR", "Floriano - PI", "Floriano Peixoto - RS", "Florianopolis - SC", "Florida - PR", "Florida Paulista - SP", "Florinia - SP", "Fonte Boa - AM", "Fontoura Xavier - RS", "Formiga - MG", "Formigueiro - RS", "Formosa - GO", "Formosa da Serra Negra - MA", "Formosa do Oeste - PR", "Formosa do Rio Preto - BA", "Formosa do Sul - SC", "Formoso - GO",
			"Formoso - MG", "Formoso do Araguaia - TO", "Forquetinha - RS", "Forquilha - CE", "Forquilhinha - SC", "Fortaleza - CE", "Fortaleza de Minas - MG", "Fortaleza do Tabocao - TO", "Fortaleza dos Nogueiras - MA", "Fortaleza dos Valos - RS", "Fortim - CE", "Fortuna - MA", "Fortuna de Minas - MG", "Foz do Iguacu - PR", "Foz do Jordao - PR", "Fraiburgo - SC", "Franca - SP", "Francinopolis - PI", "Francisco Alves - PR", "Francisco Ayres - PI", "Francisco Badaro - MG", "Francisco Beltrao - PR", "Francisco Dantas - RN", "Francisco Dumont - MG", "Francisco Macedo - PI", "Francisco Morato - SP", "Francisco Sa - MG", "Francisco Santos - PI", "Franciscopolis - MG", "Franco da Rocha - SP", "Frecheirinha - CE", "Frederico Westphalen - RS", "Frei Gaspar - MG", "Frei Inocencio - MG", "Frei Lagonegro - MG", "Frei Martinho - PB", "Frei Miguelinho - PE", "Frei Paulo - SE", "Frei Rogerio - SC", "Fronteira - MG", "Fronteira dos Vales - MG", "Fronteiras - PI", "Fruta de Leite - MG",
			"Frutal - MG", "Frutuoso Gomes - RN", "Fundao - ES", "Funilandia - MG", "Gabriel Monteiro - SP", "Gado Bravo - PB", "Galia - SP", "Galileia - MG", "Galinhos - RN", "Galvao - SC", "Gameleira - PE", "Gameleira de Goias - GO", "Gameleiras - MG", "Gandu - BA", "Garanhuns - PE", "Gararu - SE", "Garca - SP", "Garibaldi - RS", "Garopaba - SC", "Garrafao do Norte - PA", "Garruchos - RS", "Garuva - SC", "Gaspar - SC", "Gastao Vidigal - SP", "Gaucha do Norte - MT", "Gaurama - RS", "Gaviao - BA", "Gaviao Peixoto - SP", "Geminiano - PI", "General Camara - RS", "General Carneiro - PR", "General Carneiro - MT", "General Maynard - SE", "General Salgado - SP", "General Sampaio - CE", "Gentil - RS", "Gentio do Ouro - BA", "Getulina - SP", "Getulio Vargas - RS", "Gilbues - PI", "Girau do Ponciano - AL", "Girua - RS", "Glaucilandia - MG", "Glicerio - SP", "Gloria - BA", "Gloria de Dourados - MS", "Gloria do Goita - PE", "Gloria DOeste - MT", "Glorinha - RS", "Godofredo Viana - MA",
			"Godoy Moreira - PR", "Goiabeira - MG", "Goiana - PE", "Goiana - MG", "Goianapolis - GO", "Goiandira - GO", "Goianesia - GO", "Goianesia do Para - PA", "Goiania - GO", "Goianinha - RN", "Goianira - GO", "Goianorte - TO", "Goias - GO", "Goiatins - TO", "Goiatuba - GO", "Goioere - PR", "Goioxim - PR", "Goncalves - MG", "Goncalves Dias - MA", "Gongogi - BA", "Gonzaga - MG", "Gouveia - MG", "Gouvelandia - GO", "Governador Archer - MA", "Governador Celso Ramos - SC", "Governador Dix-Sept Rosado - RN", "Governador Edison Lobao - MA", "Governador Eugenio Barros - MA", "Governador Jorge Teixeira - RO", "Governador Lindenberg - ES", "Governador Luiz Rocha - MA", "Governador Mangabeira - BA", "Governador Newton Bello - MA", "Governador Nunes Freire - MA", "Governador Valadares - MG", "Graca - CE", "Graca Aranha - MA", "Gracho Cardoso - SE", "Grajau - MA", "Gramado - RS", "Gramado dos Loureiros - RS", "Gramado Xavier - RS", "Grandes Rios - PR", "Granito - PE", "Granja - CE",
			"Granjeiro - CE", "Grao Mogol - MG", "Grao Para - SC", "Gravata - PE", "Gravatai - RS", "Gravatal - SC", "Groairas - CE", "Grossos - RN", "Grupiara - MG", "Guabiju - RS", "Guabiruba - SC", "Guacui - ES", "Guadalupe - PI", "Guaiba - RS", "Guaicara - SP", "Guaimbe - SP", "Guaira - PR", "Guaira - SP", "Guairaca - PR", "Guaiuba - CE", "Guajara - AM", "Guajara-Mirim - RO", "Guajeru - BA", "Guamare - RN", "Guamiranga - PR", "Guanambi - BA", "Guanhaes - MG", "Guape - MG", "Guapiacu - SP", "Guapiara - SP", "Guapimirim - RJ", "Guapirama - PR", "Guapo - GO", "Guapore - RS", "Guaporema - PR", "Guara - SP", "Guarabira - PB", "Guaracai - SP", "Guaraci - PR", "Guaraci - SP", "Guaraciaba - MG", "Guaraciaba - SC", "Guaraciaba do Norte - CE", "Guaraciama - MG", "Guarai - TO", "Guaraita - GO", "Guaramiranga - CE", "Guaramirim - SC", "Guaranesia - MG", "Guarani - MG", "Guarani das Missoes - RS", "Guarani de Goias - GO", "Guarani dOeste - SP", "Guaraniacu - PR", "Guaranta - SP",
			"Guaranta do Norte - MT", "Guarapari - ES", "Guarapuava - PR", "Guaraquecaba - PR", "Guarara - MG", "Guararapes - SP", "Guararema - SP", "Guaratinga - BA", "Guaratingueta - SP", "Guaratuba - PR", "Guarda-Mor - MG", "Guarei - SP", "Guariba - SP", "Guaribas - PI", "Guarinos - GO", "Guaruja - SP", "Guaruja do Sul - SC", "Guarulhos - SP", "Guatambu - SC", "Guatapara - SP", "Guaxupe - MG", "Guia Lopes da Laguna - MS", "Guidoval - MG", "Guimaraes - MA", "Guimarania - MG", "Guiratinga - MT", "Guiricema - MG", "Gurinhata - MG", "Gurinhem - PB", "Gurjao - PB", "Gurupa - PA", "Gurupi - TO", "Guzolandia - SP", "Harmonia - RS", "Heitorai - GO", "Heliodora - MG", "Heliopolis - BA", "Herculandia - SP", "Herval - RS", "Herval dOeste - SC", "Herveiras - RS", "Hidrolandia - GO", "Hidrolandia - CE", "Hidrolina - GO", "Holambra - SP", "Honorio Serpa - PR", "Horizonte - CE", "Horizontina - RS", "Hortolandia - SP", "Hugo Napoleao - PI", "Hulha Negra - RS", "Humaita - RS", "Humaita - AM",
			"Humberto de Campos - MA", "Iacanga - SP", "Iaciara - GO", "Iacri - SP", "Iacu - BA", "Iapu - MG", "Iaras - SP", "Iati - PE", "Ibaiti - PR", "Ibarama - RS", "Ibaretama - CE", "Ibate - SP", "Ibateguara - AL", "Ibatiba - ES", "Ibema - PR", "Ibertioga - MG", "Ibia - MG", "Ibiaca - RS", "Ibiai - MG", "Ibiam - SC", "Ibiapina - CE", "Ibiara - PB", "Ibiassuce - BA", "Ibicarai - BA", "Ibicare - SC", "Ibicoara - BA", "Ibicui - BA", "Ibicuitinga - CE", "Ibimirim - PE", "Ibipeba - BA", "Ibipitanga - BA", "Ibipora - PR", "Ibiquera - BA", "Ibira - SP", "Ibiracatu - MG", "Ibiraci - MG", "Ibiracu - ES", "Ibiraiaras - RS", "Ibirajuba - PE", "Ibirama - SC", "Ibirapitanga - BA", "Ibirapua - BA", "Ibirapuita - RS", "Ibirarema - SP", "Ibirataia - BA", "Ibirite - MG", "Ibiruba - RS", "Ibitiara - BA", "Ibitinga - SP", "Ibitirama - ES", "Ibitita - BA", "Ibitiura de Minas - MG", "Ibituruna - MG", "Ibiuna - SP", "Ibotirama - BA", "Icapui - CE", "Icara - SC", "Icarai de Minas - MG",
			"Icaraima - PR", "Icatu - MA", "Icem - SP", "Ichu - BA", "Ico - CE", "Iconha - ES", "Ielmo Marinho - RN", "Iepe - SP", "Igaci - AL", "Igapora - BA", "Igaracu do Tiete - SP", "Igaracy - PB", "Igarapava - SP", "Igarape - MG", "Igarape do Meio - MA", "Igarape Grande - MA", "Igarape-Acu - PA", "Igarape-Miri - PA", "Igarassu - PE", "Igarata - SP", "Igaratinga - MG", "Igrapiuna - BA", "Igreja Nova - AL", "Igrejinha - RS", "Iguaba Grande - RJ", "Iguai - BA", "Iguape - SP", "Iguaraci - PE", "Iguaracu - PR", "Iguatama - MG", "Iguatemi - MS", "Iguatu - CE", "Iguatu - PR", "Ijaci - MG", "Ijui - RS", "Ilha Comprida - SP", "Ilha das Flores - SE", "Ilha de Itamaraca - PE", "Ilha Grande - PI", "Ilha Solteira - SP", "Ilhabela - SP", "Ilheus - BA", "Ilhota - SC", "Ilicinea - MG", "Ilopolis - RS", "Imaculada - PB", "Imarui - SC", "Imbau - PR", "Imbe - RS", "Imbe de Minas - MG", "Imbituba - SC", "Imbituva - PR", "Imbuia - SC", "Imigrante - RS", "Imperatriz - MA", "Inacio Martins - PR",
			"Inaciolandia - GO", "Inaja - PE", "Inaja - PR", "Inconfidentes - MG", "Indaiabira - MG", "Indaial - SC", "Indaiatuba - SP", "Independencia - RS", "Independencia - CE", "Indiana - SP", "Indianopolis - PR", "Indianopolis - MG", "Indiapora - SP", "Indiara - GO", "Indiaroba - SE", "Indiavai - MT", "Inga - PB", "Ingai - MG", "Ingazeira - PE", "Inhacora - RS", "Inhambupe - BA", "Inhangapi - PA", "Inhapi - AL", "Inhapim - MG", "Inhauma - MG", "Inhuma - PI", "Inhumas - GO", "Inimutaba - MG", "Inocencia - MS", "Inubia Paulista - SP", "Iomere - SC", "Ipaba - MG", "Ipameri - GO", "Ipanema - MG", "Ipanguacu - RN", "Ipaporanga - CE", "Ipatinga - MG", "Ipaumirim - CE", "Ipaussu - SP", "Ipe - RS", "Ipecaeta - BA", "Ipero - SP", "Ipeuna - SP", "Ipiacu - MG", "Ipiau - BA", "Ipigua - SP", "Ipira - SC", "Ipira - BA", "Ipiranga - PR", "Ipiranga de Goias - GO", "Ipiranga do Norte - MT", "Ipiranga do Piaui - PI", "Ipiranga do Sul - RS", "Ipixuna - AM", "Ipixuna do Para - PA", "Ipojuca - PE",
			"Ipora - GO", "Ipora - PR", "Ipora do Oeste - SC", "Iporanga - SP", "Ipu - CE", "Ipua - SP", "Ipuacu - SC", "Ipubi - PE", "Ipueira - RN", "Ipueiras - TO", "Ipueiras - CE", "Ipuiuna - MG", "Ipumirim - SC", "Ipupiara - BA", "Iracema - CE", "Iracema - RR", "Iracema do Oeste - PR", "Iracemapolis - SP", "Iraceminha - SC", "Irai - RS", "Irai de Minas - MG", "Irajuba - BA", "Iramaia - BA", "Iranduba - AM", "Irani - SC", "Irapua - SP", "Irapuru - SP", "Iraquara - BA", "Irara - BA", "Irati - SC", "Irati - PR", "Iraucuba - CE", "Irece - BA", "Iretama - PR", "Irineopolis - SC", "Irituia - PA", "Irupi - ES", "Isaias Coelho - PI", "Israelandia - GO", "Ita - SC", "Itaara - RS", "Itabaiana - PB", "Itabaiana - SE", "Itabaianinha - SE", "Itabela - BA", "Itabera - SP", "Itaberaba - BA", "Itaberai - GO", "Itabi - SE", "Itabira - MG", "Itabirinha - MG", "Itabirito - MG", "Itaborai - RJ", "Itabuna - BA", "Itacaja - TO", "Itacambira - MG", "Itacarambi - MG", "Itacare - BA", "Itacoatiara - AM",
			"Itacuruba - PE", "Itacurubi - RS", "Itaete - BA", "Itagi - BA", "Itagiba - BA", "Itagimirim - BA", "Itaguacu - ES", "Itaguacu da Bahia - BA", "Itaguai - RJ", "Itaguaje - PR", "Itaguara - MG", "Itaguari - GO", "Itaguaru - GO", "Itaguatins - TO", "Itai - SP", "Itaiba - PE", "Itaicaba - CE", "Itainopolis - PI", "Itaiopolis - SC", "Itaipava do Grajau - MA", "Itaipe - MG", "Itaipulandia - PR", "Itaitinga - CE", "Itaituba - PA", "Itaja - RN", "Itaja - GO", "Itajai - SC", "Itajobi - SP", "Itaju - SP", "Itaju do Colonia - BA", "Itajuba - MG", "Itajuipe - BA", "Italva - RJ", "Itamaraju - BA", "Itamarandiba - MG", "Itamarati - AM", "Itamarati de Minas - MG", "Itamari - BA", "Itambacuri - MG", "Itambaraca - PR", "Itambe - BA", "Itambe - PE", "Itambe - PR", "Itambe do Mato Dentro - MG", "Itamogi - MG", "Itamonte - MG", "Itanagra - BA", "Itanhaem - SP", "Itanhandu - MG", "Itanhanga - MT", "Itanhem - BA", "Itanhomi - MG", "Itaobim - MG", "Itaoca - SP", "Itaocara - RJ", "Itapaci - GO",
			"Itapage - CE", "Itapagipe - MG", "Itaparica - BA", "Itape - BA", "Itapebi - BA", "Itapecerica - MG", "Itapecerica da Serra - SP", "Itapecuru Mirim - MA", "Itapejara dOeste - PR", "Itapema - SC", "Itapemirim - ES", "Itaperucu - PR", "Itaperuna - RJ", "Itapetim - PE", "Itapetinga - BA", "Itapetininga - SP", "Itapeva - SP", "Itapeva - MG", "Itapevi - SP", "Itapicuru - BA", "Itapipoca - CE", "Itapira - SP", "Itapiranga - AM", "Itapiranga - SC", "Itapirapua - GO", "Itapirapua Paulista - SP", "Itapiratins - TO", "Itapissuma - PE", "Itapitanga - BA", "Itapiuna - CE", "Itapoa - SC", "Itapolis - SP", "Itapora - MS", "Itapora do Tocantins - TO", "Itaporanga - PB", "Itaporanga - SP", "Itaporanga dAjuda - SE", "Itapororoca - PB", "Itapua do Oeste - RO", "Itapuca - RS", "Itapui - SP", "Itapura - SP", "Itapuranga - GO", "Itaquaquecetuba - SP", "Itaquara - BA", "Itaqui - RS", "Itaquirai - MS", "Itaquitinga - PE", "Itarana - ES", "Itarantim - BA", "Itarare - SP", "Itarema - CE",
			"Itariri - SP", "Itaruma - GO", "Itati - RS", "Itatiaia - RJ", "Itatiaiucu - MG", "Itatiba - SP", "Itatiba do Sul - RS", "Itatim - BA", "Itatinga - SP", "Itatira - CE", "Itatuba - PB", "Itau - RN", "Itau de Minas - MG", "Itauba - MT", "Itaubal - AP", "Itaucu - GO", "Itaueira - PI", "Itauna - MG", "Itauna do Sul - PR", "Itaverava - MG", "Itinga - MG", "Itinga do Maranhao - MA", "Itiquira - MT", "Itirapina - SP", "Itirapua - SP", "Itirucu - BA", "Itiuba - BA", "Itobi - SP", "Itororo - BA", "Itu - SP", "Ituacu - BA", "Itubera - BA", "Itueta - MG", "Ituiutaba - MG", "Itumbiara - GO", "Itumirim - MG", "Itupeva - SP", "Itupiranga - PA", "Ituporanga - SC", "Iturama - MG", "Itutinga - MG", "Ituverava - SP", "Iuiu - BA", "Iuna - ES", "Ivai - PR", "Ivaipora - PR", "Ivate - PR", "Ivatuba - PR", "Ivinhema - MS", "Ivolandia - GO", "Ivora - RS", "Ivoti - RS", "Jaboatao dos Guararapes - PE", "Jabora - SC", "Jaborandi - SP", "Jaborandi - BA", "Jaboti - PR", "Jaboticaba - RS",
			"Jaboticabal - SP", "Jaboticatubas - MG", "Jacana - RN", "Jacaraci - BA", "Jacarau - PB", "Jacare dos Homens - AL", "Jacareacanga - PA", "Jacarei - SP", "Jacarezinho - PR", "Jaci - SP", "Jaciara - MT", "Jacinto - MG", "Jacinto Machado - SC", "Jacobina - BA", "Jacobina do Piaui - PI", "Jacui - MG", "Jacuipe - AL", "Jacuizinho - RS", "Jacunda - PA", "Jacupiranga - SP", "Jacutinga - RS", "Jacutinga - MG", "Jaguapita - PR", "Jaguaquara - BA", "Jaguaracu - MG", "Jaguarao - RS", "Jaguarari - BA", "Jaguare - ES", "Jaguaretama - CE", "Jaguari - RS", "Jaguariaiva - PR", "Jaguaribara - CE", "Jaguaribe - CE", "Jaguaripe - BA", "Jaguariuna - SP", "Jaguaruana - CE", "Jaguaruna - SC", "Jaiba - MG", "Jaicos - PI", "Jales - SP", "Jambeiro - SP", "Jampruca - MG", "Janauba - MG", "Jandaia - GO", "Jandaia do Sul - PR", "Jandaira - RN", "Jandaira - BA", "Jandira - SP", "Janduis - RN", "Jangada - MT", "Janiopolis - PR", "Januaria - MG", "Januario Cicco - RN", "Japaraiba - MG",
			"Japaratinga - AL", "Japaratuba - SE", "Japeri - RJ", "Japi - RN", "Japira - PR", "Japoata - SE", "Japonvar - MG", "Japora - MS", "Japura - PR", "Japura - AM", "Jaqueira - PE", "Jaquirana - RS", "Jaragua - GO", "Jaragua do Sul - SC", "Jaraguari - MS", "Jaramataia - AL", "Jardim - CE", "Jardim - MS", "Jardim Alegre - PR", "Jardim de Angicos - RN", "Jardim de Piranhas - RN", "Jardim do Mulato - PI", "Jardim do Serido - RN", "Jardim Olinda - PR", "Jardinopolis - SP", "Jardinopolis - SC", "Jari - RS", "Jarinu - SP", "Jaru - RO", "Jatai - GO", "Jataizinho - PR", "Jatauba - PE", "Jatei - MS", "Jati - CE", "Jatoba - MA", "Jatoba - PE", "Jatoba do Piaui - PI", "Jau - SP", "Jau do Tocantins - TO", "Jaupaci - GO", "Jauru - MT", "Jeceaba - MG", "Jenipapo de Minas - MG", "Jenipapo dos Vieiras - MA", "Jequeri - MG", "Jequia da Praia - AL", "Jequie - BA", "Jequitai - MG", "Jequitiba - MG", "Jequitinhonha - MG", "Jeremoabo - BA", "Jerico - PB", "Jeriquara - SP", "Jeronimo Monteiro - ES",
			"Jerumenha - PI", "Jesuania - MG", "Jesuitas - PR", "Jesupolis - GO", "Jijoca de Jericoacoara - CE", "Ji-Parana - RO", "Jiquirica - BA", "Jitauna - BA", "Joacaba - SC", "Joaima - MG", "Joanesia - MG", "Joanopolis - SP", "Joao Alfredo - PE", "Joao Camara - RN", "Joao Costa - PI", "Joao Dias - RN", "Joao Dourado - BA", "Joao Lisboa - MA", "Joao Monlevade - MG", "Joao Neiva - ES", "Joao Pessoa - PB", "Joao Pinheiro - MG", "Joao Ramalho - SP", "Joaquim Felicio - MG", "Joaquim Gomes - AL", "Joaquim Nabuco - PE", "Joaquim Pires - PI", "Joaquim Tavora - PR", "Joca Marques - PI", "Joia - RS", "Joinville - SC", "Jordania - MG", "Jordao - AC", "Jose Boiteux - SC", "Jose Bonifacio - SP", "Jose da Penha - RN", "Jose de Freitas - PI", "Jose Goncalves de Minas - MG", "Jose Raydan - MG", "Joselandia - MA", "Josenopolis - MG", "Joviania - GO", "Juara - MT", "Juarez Tavora - PB", "Juarina - TO", "Juatuba - MG", "Juazeirinho - PB", "Juazeiro - BA", "Juazeiro do Norte - CE",
			"Juazeiro do Piaui - PI", "Jucas - CE", "Jucati - PE", "Jucurucu - BA", "Jucurutu - RN", "Juina - MT", "Juiz de Fora - MG", "Julio Borges - PI", "Julio de Castilhos - RS", "Julio Mesquita - SP", "Jumirim - SP", "Junco do Maranhao - MA", "Junco do Serido - PB", "Jundia - AL", "Jundia - RN", "Jundiai - SP", "Jundiai do Sul - PR", "Junqueiro - AL", "Junqueiropolis - SP", "Jupi - PE", "Jupia - SC", "Juquia - SP", "Juquitiba - SP", "Juramento - MG", "Juranda - PR", "Jurema - PE", "Jurema - PI", "Juripiranga - PB", "Juru - PB", "Jurua - AM", "Juruaia - MG", "Juruena - MT", "Juruti - PA", "Juscimeira - MT", "Jussara - GO", "Jussara - PR", "Jussara - BA", "Jussari - BA", "Jussiape - BA", "Jutai - AM", "Juti - MS", "Juvenilia - MG", "Kalore - PR", "Labrea - AM", "Lacerdopolis - SC", "Ladainha - MG", "Ladario - MS", "Lafaiete Coutinho - BA", "Lagamar - MG", "Lagarto - SE", "Lages - SC", "Lago da Pedra - MA", "Lago do Junco - MA", "Lago dos Rodrigues - MA", "Lago Verde - MA",
			"Lagoa - PB", "Lagoa Alegre - PI", "Lagoa Bonita do Sul - RS", "Lagoa da Canoa - AL", "Lagoa da Confusao - TO", "Lagoa da Prata - MG", "Lagoa dAnta - RN", "Lagoa de Dentro - PB", "Lagoa de Pedras - RN", "Lagoa de Sao Francisco - PI", "Lagoa de Velhos - RN", "Lagoa do Barro do Piaui - PI", "Lagoa do Carro - PE", "Lagoa do Itaenga - PE", "Lagoa do Mato - MA", "Lagoa do Ouro - PE", "Lagoa do Piaui - PI", "Lagoa do Sitio - PI", "Lagoa do Tocantins - TO", "Lagoa dos Gatos - PE", "Lagoa dos Patos - MG", "Lagoa dos Tres Cantos - RS", "Lagoa Dourada - MG", "Lagoa Formosa - MG", "Lagoa Grande - MG", "Lagoa Grande - PE", "Lagoa Grande do Maranhao - MA", "Lagoa Nova - RN", "Lagoa Real - BA", "Lagoa Salgada - RN", "Lagoa Santa - MG", "Lagoa Santa - GO", "Lagoa Seca - PB", "Lagoa Vermelha - RS", "Lagoao - RS", "Lagoinha - SP", "Lagoinha do Piaui - PI", "Laguna - SC", "Laguna Carapa - MS", "Laje - BA", "Laje do Muriae - RJ", "Lajeado - RS", "Lajeado - TO", "Lajeado do Bugre - RS",
			"Lajeado Grande - SC", "Lajeado Novo - MA", "Lajedao - BA", "Lajedinho - BA", "Lajedo - PE", "Lajedo do Tabocal - BA", "Lajes - RN", "Lajes Pintadas - RN", "Lajinha - MG", "Lamarao - BA", "Lambari - MG", "Lambari DOeste - MT", "Lamim - MG", "Landri Sales - PI", "Lapa - PR", "Lapao - BA", "Laranja da Terra - ES", "Laranjal - PR", "Laranjal - MG", "Laranjal do Jari - AP", "Laranjal Paulista - SP", "Laranjeiras - SE", "Laranjeiras do Sul - PR", "Lassance - MG", "Lastro - PB", "Laurentino - SC", "Lauro de Freitas - BA", "Lauro Muller - SC", "Lavandeira - TO", "Lavinia - SP", "Lavras - MG", "Lavras da Mangabeira - CE", "Lavras do Sul - RS", "Lavrinhas - SP", "Leandro Ferreira - MG", "Lebon Regis - SC", "Leme - SP", "Leme do Prado - MG", "Lencois - BA", "Lencois Paulista - SP", "Leoberto Leal - SC", "Leopoldina - MG", "Leopoldo de Bulhoes - GO", "Leopolis - PR", "Liberato Salzano - RS", "Liberdade - MG", "Licinio de Almeida - BA", "Lidianopolis - PR", "Lima Campos - MA",
			"Lima Duarte - MG", "Limeira - SP", "Limeira do Oeste - MG", "Limoeiro - PE", "Limoeiro de Anadia - AL", "Limoeiro do Ajuru - PA", "Limoeiro do Norte - CE", "Lindoeste - PR", "Lindoia - SP", "Lindoia do Sul - SC", "Lindolfo Collor - RS", "Linha Nova - RS", "Linhares - ES", "Lins - SP", "Livramento - PB", "Livramento de Nossa Senhora - BA", "Lizarda - TO", "Loanda - PR", "Lobato - PR", "Logradouro - PB", "Londrina - PR", "Lontra - MG", "Lontras - SC", "Lorena - SP", "Loreto - MA", "Lourdes - SP", "Louveira - SP", "Lucas do Rio Verde - MT", "Lucelia - SP", "Lucena - PB", "Lucianopolis - SP", "Luciara - MT", "Lucrecia - RN", "Luis Antonio - SP", "Luis Correia - PI", "Luis Domingues - MA", "Luis Eduardo Magalhaes - BA", "Luis Gomes - RN", "Luisburgo - MG", "Luislandia - MG", "Luiz Alves - SC", "Luiziana - PR", "Luiziania - SP", "Luminarias - MG", "Lunardelli - PR", "Lupercio - SP", "Lupionopolis - PR", "Lutecia - SP", "Luz - MG", "Luzerna - SC", "Luziania - GO",
			"Luzilandia - PI", "Luzinopolis - TO", "Macae - RJ", "Macaiba - RN", "Macajuba - BA", "Macambara - RS", "Macambira - SE", "Macapa - AP", "Macaparana - PE", "Macarani - BA", "Macatuba - SP", "Macau - RN", "Macaubal - SP", "Macaubas - BA", "Macedonia - SP", "Maceio - AL", "Machacalis - MG", "Machadinho - RS", "Machadinho DOeste - RO", "Machado - MG", "Machados - PE", "Macieira - SC", "Macuco - RJ", "Macurure - BA", "Madalena - CE", "Madeiro - PI", "Madre de Deus - BA", "Madre de Deus de Minas - MG", "Mae dagua - PB", "Mae do Rio - PA", "Maetinga - BA", "Mafra - SC", "Magalhaes Barata - PA", "Magalhaes de Almeida - MA", "Magda - SP", "Mage - RJ", "Maiquinique - BA", "Mairi - BA", "Mairinque - SP", "Mairipora - SP", "Mairipotaba - GO", "Major Gercino - SC", "Major Isidoro - AL", "Major Sales - RN", "Major Vieira - SC", "Malacacheta - MG", "Malhada - BA", "Malhada de Pedras - BA", "Malhada dos Bois - SE", "Malhador - SE", "Mallet - PR", "Malta - PB", "Mamanguape - PB",
			"Mambai - GO", "Mambore - PR", "Mamonas - MG", "Mampituba - RS", "Manacapuru - AM", "Manaira - PB", "Manaquiri - AM", "Manari - PE", "Manaus - AM", "Mancio Lima - AC", "Mandaguacu - PR", "Mandaguari - PR", "Mandirituba - PR", "Manduri - SP", "Manfrinopolis - PR", "Manga - MG", "Mangaratiba - RJ", "Mangueirinha - PR", "Manhuacu - MG", "Manhumirim - MG", "Manicore - AM", "Manoel Emidio - PI", "Manoel Ribas - PR", "Manoel Urbano - AC", "Manoel Viana - RS", "Manoel Vitorino - BA", "Mansidao - BA", "Mantena - MG", "Mantenopolis - ES", "Maquine - RS", "Mar de Espanha - MG", "Mar Vermelho - AL", "Mara Rosa - GO", "Maraa - AM", "Maraba - PA", "Maraba Paulista - SP", "Maracacume - MA", "Maracai - SP", "Maracaja - SC", "Maracaju - MS", "Maracana - PA", "Maracanau - CE", "Maracas - BA", "Maragogi - AL", "Maragogipe - BA", "Maraial - PE", "Maraja do Sena - MA", "Maranguape - CE", "Maranhaozinho - MA", "Marapanim - PA", "Marapoama - SP", "Marata - RS", "Marataizes - ES", "Marau - RS",
			"Marau - BA", "Maravilha - AL", "Maravilha - SC", "Maravilhas - MG", "Marcacao - PB", "Marcelandia - MT", "Marcelino Ramos - RS", "Marcelino Vieira - RN", "Marcionilio Souza - BA", "Marco - CE", "Marcolandia - PI", "Marcos Parente - PI", "Marechal Candido Rondon - PR", "Marechal Deodoro - AL", "Marechal Floriano - ES", "Marechal Thaumaturgo - AC", "Marema - SC", "Mari - PB", "Maria da Fe - MG", "Maria Helena - PR", "Marialva - PR", "Mariana - MG", "Mariana Pimentel - RS", "Mariano Moro - RS", "Marianopolis do Tocantins - TO", "Mariapolis - SP", "Maribondo - AL", "Marica - RJ", "Marilac - MG", "Marilandia - ES", "Marilandia do Sul - PR", "Marilena - PR", "Marilia - SP", "Mariluz - PR", "Maringa - PR", "Marinopolis - SP", "Mario Campos - MG", "Mariopolis - PR", "Maripa - PR", "Maripa de Minas - MG", "Marituba - PA", "Marizopolis - PB", "Marlieria - MG", "Marmeleiro - PR", "Marmelopolis - MG", "Marques de Souza - RS", "Marquinho - PR", "Martinho Campos - MG",
			"Martinopole - CE", "Martinopolis - SP", "Martins - RN", "Martins Soares - MG", "Maruim - SE", "Marumbi - PR", "Marzagao - GO", "Mascote - BA", "Massape - CE", "Massape do Piaui - PI", "Massaranduba - PB", "Massaranduba - SC", "Mata - RS", "Mata de Sao Joao - BA", "Mata Grande - AL", "Mata Roma - MA", "Mata Verde - MG", "Matao - SP", "Mataraca - PB", "Mateiros - TO", "Matelandia - PR", "Materlandia - MG", "Mateus Leme - MG", "Mathias Lobato - MG", "Matias Barbosa - MG", "Matias Cardoso - MG", "Matias Olimpio - PI", "Matina - BA", "Matinha - MA", "Matinhas - PB", "Matinhos - PR", "Matipo - MG", "Mato Castelhano - RS", "Mato Grosso - PB", "Mato Leitao - RS", "Mato Queimado - RS", "Mato Rico - PR", "Mato Verde - MG", "Matoes - MA", "Matoes do Norte - MA", "Matos Costa - SC", "Matozinhos - MG", "Matrincha - GO", "Matriz de Camaragibe - AL", "Matupa - MT", "Matureia - PB", "Matutina - MG", "Maua - SP", "Maua da Serra - PR", "Maues - AM", "Maurilandia - GO",
			"Maurilandia do Tocantins - TO", "Mauriti - CE", "Maxaranguape - RN", "Maximiliano de Almeida - RS", "Mazagao - AP", "Medeiros - MG", "Medeiros Neto - BA", "Medianeira - PR", "Medicilandia - PA", "Medina - MG", "Meleiro - SC", "Melgaco - PA", "Mendes - RJ", "Mendes Pimentel - MG", "Mendonca - SP", "Mercedes - PR", "Merces - MG", "Meridiano - SP", "Meruoca - CE", "Mesopolis - SP", "Mesquita - MG", "Mesquita - RJ", "Messias - AL", "Messias Targino - RN", "Miguel Alves - PI", "Miguel Calmon - BA", "Miguel Leao - PI", "Miguel Pereira - RJ", "Miguelopolis - SP", "Milagres - BA", "Milagres - CE", "Milagres do Maranhao - MA", "Milha - CE", "Milton Brandao - PI", "Mimoso de Goias - GO", "Mimoso do Sul - ES", "Minacu - GO", "Minador do Negrao - AL", "Minas do Leao - RS", "Minas Novas - MG", "Minduri - MG", "Mineiros - GO", "Mineiros do Tiete - SP", "Ministro Andreazza - RO", "Mira Estrela - SP", "Mirabela - MG", "Miracatu - SP", "Miracema - RJ", "Miracema do Tocantins - TO",
			"Mirador - PR", "Mirador - MA", "Miradouro - MG", "Miraguai - RS", "Mirai - MG", "Miraima - CE", "Miranda - MS", "Miranda do Norte - MA", "Mirandiba - PE", "Mirandopolis - SP", "Mirangaba - BA", "Miranorte - TO", "Mirante - BA", "Mirante da Serra - RO", "Mirante do Paranapanema - SP", "Miraselva - PR", "Mirassol - SP", "Mirassol dOeste - MT", "Mirassolandia - SP", "Miravania - MG", "Mirim Doce - SC", "Mirinzal - MA", "Missal - PR", "Missao Velha - CE", "Mocajuba - PA", "Mococa - SP", "Modelo - SC", "Moeda - MG", "Moema - MG", "Mogeiro - PB", "Mogi das Cruzes - SP", "Mogi Guacu - SP", "Moipora - GO", "Moita Bonita - SE", "Moji Mirim - SP", "Moju - PA", "Mombaca - CE", "Mombuca - SP", "Moncao - MA", "Moncoes - SP", "Mondai - SC", "Mongagua - SP", "Monjolos - MG", "Monsenhor Gil - PI", "Monsenhor Hipolito - PI", "Monsenhor Paulo - MG", "Monsenhor Tabosa - CE", "Montadas - PB", "Montalvania - MG", "Montanha - ES", "Montanhas - RN", "Montauri - RS", "Monte Alegre - RN",
			"Monte Alegre - PA", "Monte Alegre de Goias - GO", "Monte Alegre de Minas - MG", "Monte Alegre de Sergipe - SE", "Monte Alegre do Piaui - PI", "Monte Alegre do Sul - SP", "Monte Alegre dos Campos - RS", "Monte Alto - SP", "Monte Aprazivel - SP", "Monte Azul - MG", "Monte Azul Paulista - SP", "Monte Belo - MG", "Monte Belo do Sul - RS", "Monte Carlo - SC", "Monte Carmelo - MG", "Monte Castelo - SP", "Monte Castelo - SC", "Monte das Gameleiras - RN", "Monte do Carmo - TO", "Monte Formoso - MG", "Monte Horebe - PB", "Monte Mor - SP", "Monte Negro - RO", "Monte Santo - BA", "Monte Santo de Minas - MG", "Monte Santo do Tocantins - TO", "Monte Siao - MG", "Monteiro - PB", "Monteiro Lobato - SP", "Monteiropolis - AL", "Montenegro - RS", "Montes Altos - MA", "Montes Claros - MG", "Montes Claros de Goias - GO", "Montezuma - MG", "Montividiu - GO", "Montividiu do Norte - GO", "Morada Nova - CE", "Morada Nova de Minas - MG", "Moraujo - CE", "Moreilandia - PE", "Moreira Sales - PR",
			"Moreno - PE", "Mormaco - RS", "Morpara - BA", "Morretes - PR", "Morrinhos - GO", "Morrinhos - CE", "Morrinhos do Sul - RS", "Morro Agudo - SP", "Morro Agudo de Goias - GO", "Morro Cabeca no Tempo - PI", "Morro da Fumaca - SC", "Morro da Garca - MG", "Morro do Chapeu - BA", "Morro do Chapeu do Piaui - PI", "Morro do Pilar - MG", "Morro Grande - SC", "Morro Redondo - RS", "Morro Reuter - RS", "Morros - MA", "Mortugaba - BA", "Morungaba - SP", "Mossamedes - GO", "Mossoro - RN", "Mostardas - RS", "Motuca - SP", "Mozarlandia - GO", "Muana - PA", "Mucajai - RR", "Mucambo - CE", "Mucuge - BA", "Mucum - RS", "Mucuri - BA", "Mucurici - ES", "Muitos Capoes - RS", "Muliterno - RS", "Mulungu - CE", "Mulungu - PB", "Mulungu do Morro - BA", "Mundo Novo - BA", "Mundo Novo - MS", "Mundo Novo - GO", "Munhoz - MG", "Munhoz de Melo - PR", "Muniz Ferreira - BA", "Muniz Freire - ES", "Muquem de Sao Francisco - BA", "Muqui - ES", "Muriae - MG", "Muribeca - SE", "Murici - AL",
			"Murici dos Portelas - PI", "Muricilandia - TO", "Muritiba - BA", "Murutinga do Sul - SP", "Mutuipe - BA", "Mutum - MG", "Mutunopolis - GO", "Muzambinho - MG", "Nacip Raydan - MG", "Nantes - SP", "Nanuque - MG", "Nao-Me-Toque - RS", "Naque - MG", "Narandiba - SP", "Natal - RN", "Natalandia - MG", "Natercia - MG", "Natividade - RJ", "Natividade - TO", "Natividade da Serra - SP", "Natuba - PB", "Navegantes - SC", "Navirai - MS", "Nazare - BA", "Nazare - TO", "Nazare da Mata - PE", "Nazare do Piaui - PI", "Nazare Paulista - SP", "Nazareno - MG", "Nazarezinho - PB", "Nazario - GO", "Neopolis - SE", "Nepomuceno - MG", "Neropolis - GO", "Neves Paulista - SP", "Nhamunda - AM", "Nhandeara - SP", "Nicolau Vergueiro - RS", "Nilo Pecanha - BA", "Nilopolis - RJ", "Nina Rodrigues - MA", "Ninheira - MG", "Nioaque - MS", "Nipoa - SP", "Niquelandia - GO", "Nisia Floresta - RN", "Niteroi - RJ", "Nobres - MT", "Nonoai - RS", "Nordestina - BA", "Normandia - RR", "Nortelandia - MT",
			"Nossa Senhora Aparecida - SE", "Nossa Senhora da Gloria - SE", "Nossa Senhora das Dores - SE", "Nossa Senhora das Gracas - PR", "Nossa Senhora de Lourdes - SE", "Nossa Senhora de Nazare - PI", "Nossa Senhora do Livramento - MT", "Nossa Senhora do Socorro - SE", "Nossa Senhora dos Remedios - PI", "Nova Alianca - SP", "Nova Alianca do Ivai - PR", "Nova Alvorada - RS", "Nova Alvorada do Sul - MS", "Nova America - GO", "Nova America da Colina - PR", "Nova Andradina - MS", "Nova Araca - RS", "Nova Aurora - GO", "Nova Aurora - PR", "Nova Bandeirantes - MT", "Nova Bassano - RS", "Nova Belem - MG", "Nova Boa Vista - RS", "Nova Brasilandia - MT", "Nova Brasilandia DOeste - RO", "Nova Brescia - RS", "Nova Campina - SP", "Nova Canaa - BA", "Nova Canaa do Norte - MT", "Nova Canaa Paulista - SP", "Nova Candelaria - RS", "Nova Cantu - PR", "Nova Castilho - SP", "Nova Colinas - MA", "Nova Crixas - GO", "Nova Cruz - RN", "Nova Era - MG", "Nova Erechim - SC", "Nova Esperanca - PR",
			"Nova Esperanca do Piria - PA", "Nova Esperanca do Sudoeste - PR", "Nova Esperanca do Sul - RS", "Nova Europa - SP", "Nova Fatima - PR", "Nova Fatima - BA", "Nova Floresta - PB", "Nova Friburgo - RJ", "Nova Gloria - GO", "Nova Granada - SP", "Nova Guarita - MT", "Nova Guataporanga - SP", "Nova Hartz - RS", "Nova Ibia - BA", "Nova Iguacu - RJ", "Nova Iguacu de Goias - GO", "Nova Independencia - SP", "Nova Iorque - MA", "Nova Ipixuna - PA", "Nova Itaberaba - SC", "Nova Itarana - BA", "Nova Lacerda - MT", "Nova Laranjeiras - PR", "Nova Lima - MG", "Nova Londrina - PR", "Nova Luzitania - SP", "Nova Mamore - RO", "Nova Marilandia - MT", "Nova Maringa - MT", "Nova Modica - MG", "Nova Monte Verde - MT", "Nova Mutum - MT", "Nova Nazare - MT", "Nova Odessa - SP", "Nova Olimpia - MT", "Nova Olimpia - PR", "Nova Olinda - CE", "Nova Olinda - TO", "Nova Olinda - PB", "Nova Olinda do Maranhao - MA", "Nova Olinda do Norte - AM", "Nova Padua - RS", "Nova Palma - RS", "Nova Palmeira - PB",
			"Nova Petropolis - RS", "Nova Ponte - MG", "Nova Porteirinha - MG", "Nova Prata - RS", "Nova Prata do Iguacu - PR", "Nova Ramada - RS", "Nova Redencao - BA", "Nova Resende - MG", "Nova Roma - GO", "Nova Roma do Sul - RS", "Nova Rosalandia - TO", "Nova Russas - CE", "Nova Santa Barbara - PR", "Nova Santa Helena - MT", "Nova Santa Rita - RS", "Nova Santa Rita - PI", "Nova Santa Rosa - PR", "Nova Serrana - MG", "Nova Soure - BA", "Nova Tebas - PR", "Nova Timboteua - PA", "Nova Trento - SC", "Nova Ubirata - MT", "Nova Uniao - MG", "Nova Uniao - RO", "Nova Venecia - ES", "Nova Veneza - SC", "Nova Veneza - GO", "Nova Vicosa - BA", "Nova Xavantina - MT", "Novais - SP", "Novo Acordo - TO", "Novo Airao - AM", "Novo Alegre - TO", "Novo Aripuana - AM", "Novo Barreiro - RS", "Novo Brasil - GO", "Novo Cabrais - RS", "Novo Cruzeiro - MG", "Novo Gama - GO", "Novo Hamburgo - RS", "Novo Horizonte - SC", "Novo Horizonte - SP", "Novo Horizonte - BA", "Novo Horizonte do Norte - MT",
			"Novo Horizonte do Oeste - RO", "Novo Horizonte do Sul - MS", "Novo Itacolomi - PR", "Novo Jardim - TO", "Novo Lino - AL", "Novo Machado - RS", "Novo Mundo - MT", "Novo Oriente - CE", "Novo Oriente de Minas - MG", "Novo Oriente do Piaui - PI", "Novo Planalto - GO", "Novo Progresso - PA", "Novo Repartimento - PA", "Novo Santo Antonio - MT", "Novo Santo Antonio - PI", "Novo Sao Joaquim - MT", "Novo Tiradentes - RS", "Novo Triunfo - BA", "Novo Xingu - RS", "Novorizonte - MG", "Nuporanga - SP", "obidos - PA", "Ocara - CE", "Ocaucu - SP", "Oeiras - PI", "Oeiras do Para - PA", "Oiapoque - AP", "Olaria - MG", "oleo - SP", "Olho dagua - PB", "Olho dagua das Cunhas - MA", "Olho dagua das Flores - AL", "Olho dagua do Casado - AL", "Olho Dagua do Piaui - PI", "Olho dagua Grande - AL", "Olho-dagua do Borges - RN", "Olhos-dagua - MG", "Olimpia - SP", "Olimpio Noronha - MG", "Olinda - PE", "Olinda Nova do Maranhao - MA", "Olindina - BA", "Olivedos - PB", "Oliveira - MG",
			"Oliveira de Fatima - TO", "Oliveira dos Brejinhos - BA", "Oliveira Fortes - MG", "Olivenca - AL", "Onca de Pitangui - MG", "Onda Verde - SP", "Oratorios - MG", "Oriente - SP", "Orindiuva - SP", "Oriximina - PA", "Orizania - MG", "Orizona - GO", "Orlandia - SP", "Orleans - SC", "Orobo - PE", "Oroco - PE", "Oros - CE", "Ortigueira - PR", "Osasco - SP", "Oscar Bressane - SP", "Osorio - RS", "Osvaldo Cruz - SP", "Otacilio Costa - SC", "Ourem - PA", "Ouricangas - BA", "Ouricuri - PE", "Ourilandia do Norte - PA", "Ourinhos - SP", "Ourizona - PR", "Ouro - SC", "Ouro Branco - MG", "Ouro Branco - AL", "Ouro Branco - RN", "Ouro Fino - MG", "Ouro Preto - MG", "Ouro Preto do Oeste - RO", "Ouro Velho - PB", "Ouro Verde - SC", "Ouro Verde - SP", "Ouro Verde de Goias - GO", "Ouro Verde de Minas - MG", "Ouro Verde do Oeste - PR", "Ouroeste - SP", "Ourolandia - BA", "Ouvidor - GO", "Pacaembu - SP", "Pacaja - PA", "Pacajus - CE", "Pacaraima - RR", "Pacatuba - SE", "Pacatuba - CE",
			"Paco do Lumiar - MA", "Pacoti - CE", "Pacuja - CE", "Padre Bernardo - GO", "Padre Carvalho - MG", "Padre Marcos - PI", "Padre Paraiso - MG", "Paes Landim - PI", "Pai Pedro - MG", "Paial - SC", "Paicandu - PR", "Paim Filho - RS", "Paineiras - MG", "Painel - SC", "Pains - MG", "Paiva - MG", "Pajeu do Piaui - PI", "Palestina - SP", "Palestina - AL", "Palestina de Goias - GO", "Palestina do Para - PA", "Palhano - CE", "Palhoca - SC", "Palma - MG", "Palma Sola - SC", "Palmacia - CE", "Palmares - PE", "Palmares do Sul - RS", "Palmares Paulista - SP", "Palmas - TO", "Palmas - PR", "Palmas de Monte Alto - BA", "Palmeira - PR", "Palmeira - SC", "Palmeira das Missoes - RS", "Palmeira do Piaui - PI", "Palmeira dOeste - SP", "Palmeira dos indios - AL", "Palmeirais - PI", "Palmeirandia - MA", "Palmeirante - TO", "Palmeiras - BA", "Palmeiras de Goias - GO", "Palmeiras do Tocantins - TO", "Palmeirina - PE", "Palmeiropolis - TO", "Palmelo - GO", "Palminopolis - GO", "Palmital - SP",
			"Palmital - PR", "Palmitinho - RS", "Palmitos - SC", "Palmopolis - MG", "Palotina - PR", "Panama - GO", "Panambi - RS", "Pancas - ES", "Panelas - PE", "Panorama - SP", "Pantano Grande - RS", "Pao de Acucar - AL", "Papagaios - MG", "Papanduva - SC", "Paqueta - PI", "Para de Minas - MG", "Paracambi - RJ", "Paracatu - MG", "Paracuru - CE", "Paragominas - PA", "Paraguacu - MG", "Paraguacu Paulista - SP", "Parai - RS", "Paraiba do Sul - RJ", "Paraibano - MA", "Paraibuna - SP", "Paraipaba - CE", "Paraiso - SC", "Paraiso - SP", "Paraiso do Norte - PR", "Paraiso do Sul - RS", "Paraiso do Tocantins - TO", "Paraisopolis - MG", "Parambu - CE", "Paramirim - BA", "Paramoti - CE", "Parana - RN", "Parana - TO", "Paranacity - PR", "Paranagua - PR", "Paranaiba - MS", "Paranaiguara - GO", "Paranaita - MT", "Paranapanema - SP", "Paranapoema - PR", "Paranapua - SP", "Paranatama - PE", "Paranatinga - MT", "Paranavai - PR", "Paranhos - MS", "Paraopeba - MG", "Parapua - SP", "Parari - PB",
			"Parati - RJ", "Paratinga - BA", "Parau - RN", "Parauapebas - PA", "Parauna - GO", "Parazinho - RN", "Pardinho - SP", "Pareci Novo - RS", "Parecis - RO", "Parelhas - RN", "Pariconha - AL", "Parintins - AM", "Paripiranga - BA", "Paripueira - AL", "Pariquera-Acu - SP", "Parisi - SP", "Parnagua - PI", "Parnaiba - PI", "Parnamirim - PE", "Parnamirim - RN", "Parnarama - MA", "Parobe - RS", "Passa e Fica - RN", "Passa Quatro - MG", "Passa Sete - RS", "Passa Tempo - MG", "Passabem - MG", "Passagem - PB", "Passagem - RN", "Passagem Franca - MA", "Passagem Franca do Piaui - PI", "Passa-Vinte - MG", "Passira - PE", "Passo de Camaragibe - AL", "Passo de Torres - SC", "Passo do Sobrado - RS", "Passo Fundo - RS", "Passos - MG", "Passos Maia - SC", "Pastos Bons - MA", "Patis - MG", "Pato Bragado - PR", "Pato Branco - PR", "Patos - PB", "Patos de Minas - MG", "Patos do Piaui - PI", "Patrocinio - MG", "Patrocinio do Muriae - MG", "Patrocinio Paulista - SP", "Patu - RN",
			"Paty do Alferes - RJ", "Pau Brasil - BA", "Pau DArco - PA", "Pau DArco - TO", "Pau DArco do Piaui - PI", "Pau dos Ferros - RN", "Paudalho - PE", "Pauini - AM", "Paula Candido - MG", "Paula Freitas - PR", "Pauliceia - SP", "Paulinia - SP", "Paulino Neves - MA", "Paulista - PB", "Paulista - PE", "Paulistana - PI", "Paulistania - SP", "Paulistas - MG", "Paulo Afonso - BA", "Paulo Bento - RS", "Paulo de Faria - SP", "Paulo Frontin - PR", "Paulo Jacinto - AL", "Paulo Lopes - SC", "Paulo Ramos - MA", "Pavao - MG", "Paverama - RS", "Pavussu - PI", "Pe de Serra - BA", "Peabiru - PR", "Pecanha - MG", "Pederneiras - SP", "Pedra - PE", "Pedra Azul - MG", "Pedra Bela - SP", "Pedra Bonita - MG", "Pedra Branca - PB", "Pedra Branca - CE", "Pedra Branca do Amapari - AP", "Pedra do Anta - MG", "Pedra do Indaia - MG", "Pedra Dourada - MG", "Pedra Grande - RN", "Pedra Lavrada - PB", "Pedra Mole - SE", "Pedra Preta - MT", "Pedra Preta - RN", "Pedralva - MG", "Pedranopolis - SP",
			"Pedrao - BA", "Pedras Altas - RS", "Pedras de Fogo - PB", "Pedras de Maria da Cruz - MG", "Pedras Grandes - SC", "Pedregulho - SP", "Pedreira - SP", "Pedreiras - MA", "Pedrinhas - SE", "Pedrinhas Paulista - SP", "Pedrinopolis - MG", "Pedro Afonso - TO", "Pedro Alexandre - BA", "Pedro Avelino - RN", "Pedro Canario - ES", "Pedro de Toledo - SP", "Pedro do Rosario - MA", "Pedro Gomes - MS", "Pedro II - PI", "Pedro Laurentino - PI", "Pedro Leopoldo - MG", "Pedro Osorio - RS", "Pedro Regis - PB", "Pedro Teixeira - MG", "Pedro Velho - RN", "Peixe - TO", "Peixe-Boi - PA", "Peixoto de Azevedo - MT", "Pejucara - RS", "Pelotas - RS", "Penaforte - CE", "Penalva - MA", "Penapolis - SP", "Pendencias - RN", "Penedo - AL", "Penha - SC", "Pentecoste - CE", "Pequeri - MG", "Pequi - MG", "Pequizeiro - TO", "Perdigao - MG", "Perdizes - MG", "Perdoes - MG", "Pereira Barreto - SP", "Pereiras - SP", "Pereiro - CE", "Peri Mirim - MA", "Periquito - MG", "Peritiba - SC", "Peritoro - MA",
			"Perobal - PR", "Perola - PR", "Perola dOeste - PR", "Perolandia - GO", "Peruibe - SP", "Pescador - MG", "Pesqueira - PE", "Petrolandia - SC", "Petrolandia - PE", "Petrolina - PE", "Petrolina de Goias - GO", "Petropolis - RJ", "Piacabucu - AL", "Piacatu - SP", "Pianco - PB", "Piata - BA", "Piau - MG", "Picada Cafe - RS", "Picarra - PA", "Picos - PI", "Picui - PB", "Piedade - SP", "Piedade de Caratinga - MG", "Piedade de Ponte Nova - MG", "Piedade do Rio Grande - MG", "Piedade dos Gerais - MG", "Pien - PR", "Pilao Arcado - BA", "Pilar - AL", "Pilar - PB", "Pilar de Goias - GO", "Pilar do Sul - SP", "Piloes - PB", "Piloes - RN", "Piloezinhos - PB", "Pimenta - MG", "Pimenta Bueno - RO", "Pimenteiras - PI", "Pimenteiras do Oeste - RO", "Pindai - BA", "Pindamonhangaba - SP", "Pindare-Mirim - MA", "Pindoba - AL", "Pindobacu - BA", "Pindorama - SP", "Pindorama do Tocantins - TO", "Pindoretama - CE", "Pingo-dagua - MG", "Pinhais - PR", "Pinhal - RS", "Pinhal da Serra - RS",
			"Pinhal de Sao Bento - PR", "Pinhal Grande - RS", "Pinhalao - PR", "Pinhalzinho - SC", "Pinhalzinho - SP", "Pinhao - SE", "Pinhao - PR", "Pinheiral - RJ", "Pinheirinho do Vale - RS", "Pinheiro - MA", "Pinheiro Machado - RS", "Pinheiro Preto - SC", "Pinheiros - ES", "Pintadas - BA", "Pintopolis - MG", "Pio IX - PI", "Pio XII - MA", "Piquerobi - SP", "Piquet Carneiro - CE", "Piquete - SP", "Piracaia - SP", "Piracanjuba - GO", "Piracema - MG", "Piracicaba - SP", "Piracuruca - PI", "Pirai - RJ", "Pirai do Norte - BA", "Pirai do Sul - PR", "Piraju - SP", "Pirajuba - MG", "Pirajui - SP", "Pirambu - SE", "Piranga - MG", "Pirangi - SP", "Pirangucu - MG", "Piranguinho - MG", "Piranhas - GO", "Piranhas - AL", "Pirapemas - MA", "Pirapetinga - MG", "Pirapo - RS", "Pirapora - MG", "Pirapora do Bom Jesus - SP", "Pirapozinho - SP", "Piraquara - PR", "Piraque - TO", "Pirassununga - SP", "Piratini - RS", "Piratininga - SP", "Piratuba - SC", "Pirauba - MG", "Pirenopolis - GO",
			"Pires do Rio - GO", "Pires Ferreira - CE", "Piripa - BA", "Piripiri - PI", "Piritiba - BA", "Pirpirituba - PB", "Pitanga - PR", "Pitangueiras - PR", "Pitangueiras - SP", "Pitangui - MG", "Pitimbu - PB", "Pium - TO", "Piuma - ES", "Piumhi - MG", "Placas - PA", "Placido de Castro - AC", "Planaltina - GO", "Planaltina do Parana - PR", "Planaltino - BA", "Planalto - SP", "Planalto - BA", "Planalto - RS", "Planalto - PR", "Planalto Alegre - SC", "Planalto da Serra - MT", "Planura - MG", "Platina - SP", "Poa - SP", "Pocao - PE", "Pocao de Pedras - MA", "Pocinhos - PB", "Poco Branco - RN", "Poco Dantas - PB", "Poco das Antas - RS", "Poco das Trincheiras - AL", "Poco de Jose de Moura - PB", "Poco Fundo - MG", "Poco Redondo - SE", "Poco Verde - SE", "Pocoes - BA", "Pocone - MT", "Pocos de Caldas - MG", "Pocrane - MG", "Pojuca - BA", "Poloni - SP", "Pombal - PB", "Pombos - PE", "Pomerode - SC", "Pompeia - SP", "Pompeu - MG", "Pongai - SP", "Ponta de Pedras - PA",
			"Ponta Grossa - PR", "Ponta Pora - MS", "Pontal - SP", "Pontal do Araguaia - MT", "Pontal do Parana - PR", "Pontalina - GO", "Pontalinda - SP", "Pontao - RS", "Ponte Alta - SC", "Ponte Alta do Bom Jesus - TO", "Ponte Alta do Norte - SC", "Ponte Alta do Tocantins - TO", "Ponte Branca - MT", "Ponte Nova - MG", "Ponte Preta - RS", "Ponte Serrada - SC", "Pontes e Lacerda - MT", "Pontes Gestal - SP", "Ponto Belo - ES", "Ponto Chique - MG", "Ponto dos Volantes - MG", "Ponto Novo - BA", "Populina - SP", "Poranga - CE", "Porangaba - SP", "Porangatu - GO", "Porciuncula - RJ", "Porecatu - PR", "Portalegre - RN", "Portao - RS", "Porteirao - GO", "Porteiras - CE", "Porteirinha - MG", "Portel - PA", "Portelandia - GO", "Porto - PI", "Porto Acre - AC", "Porto Alegre - RS", "Porto Alegre do Norte - MT", "Porto Alegre do Piaui - PI", "Porto Alegre do Tocantins - TO", "Porto Amazonas - PR", "Porto Barreiro - PR", "Porto Belo - SC", "Porto Calvo - AL", "Porto da Folha - SE",
			"Porto de Moz - PA", "Porto de Pedras - AL", "Porto do Mangue - RN", "Porto dos Gauchos - MT", "Porto Esperidiao - MT", "Porto Estrela - MT", "Porto Feliz - SP", "Porto Ferreira - SP", "Porto Firme - MG", "Porto Franco - MA", "Porto Grande - AP", "Porto Lucena - RS", "Porto Maua - RS", "Porto Murtinho - MS", "Porto Nacional - TO", "Porto Real - RJ", "Porto Real do Colegio - AL", "Porto Rico - PR", "Porto Rico do Maranhao - MA", "Porto Seguro - BA", "Porto Uniao - SC", "Porto Velho - RO", "Porto Vera Cruz - RS", "Porto Vitoria - PR", "Porto Walter - AC", "Porto Xavier - RS", "Posse - GO", "Pote - MG", "Potengi - CE", "Potim - SP", "Potiragua - BA", "Potirendaba - SP", "Potiretama - CE", "Pouso Alegre - MG", "Pouso Alto - MG", "Pouso Novo - RS", "Pouso Redondo - SC", "Poxoreo - MT", "Pracinha - SP", "Pracuuba - AP", "Prado - BA", "Prado Ferreira - PR", "Pradopolis - SP", "Prados - MG", "Praia Grande - SC", "Praia Grande - SP", "Praia Norte - TO", "Prainha - PA",
			"Pranchita - PR", "Prata - MG", "Prata - PB", "Prata do Piaui - PI", "Pratania - SP", "Pratapolis - MG", "Pratinha - MG", "Presidente Alves - SP", "Presidente Bernardes - SP", "Presidente Bernardes - MG", "Presidente Castello Branco - SC", "Presidente Castelo Branco - PR", "Presidente Dutra - BA", "Presidente Dutra - MA", "Presidente Epitacio - SP", "Presidente Figueiredo - AM", "Presidente Getulio - SC", "Presidente Janio Quadros - BA", "Presidente Juscelino - MA", "Presidente Juscelino - MG", "Presidente Juscelino - RN", "Presidente Kennedy - ES", "Presidente Kennedy - TO", "Presidente Kubitschek - MG", "Presidente Lucena - RS", "Presidente Medici - MA", "Presidente Medici - RO", "Presidente Nereu - SC", "Presidente Olegario - MG", "Presidente Prudente - SP", "Presidente Sarney - MA", "Presidente Tancredo Neves - BA", "Presidente Vargas - MA", "Presidente Venceslau - SP", "Primavera - PE", "Primavera - PA", "Primavera de Rondonia - RO", "Primavera do Leste - MT",
			"Primeira Cruz - MA", "Primeiro de Maio - PR", "Princesa - SC", "Princesa Isabel - PB", "Professor Jamil - GO", "Progresso - RS", "Promissao - SP", "Propria - SE", "Protasio Alves - RS", "Prudente de Morais - MG", "Prudentopolis - PR", "Pugmil - TO", "Pureza - RN", "Putinga - RS", "Puxinana - PB", "Quadra - SP", "Quarai - RS", "Quartel Geral - MG", "Quarto Centenario - PR", "Quata - SP", "Quatigua - PR", "Quatipuru - PA", "Quatis - RJ", "Quatro Barras - PR", "Quatro Irmaos - RS", "Quatro Pontes - PR", "Quebrangulo - AL", "Quedas do Iguacu - PR", "Queimada Nova - PI", "Queimadas - PB", "Queimadas - BA", "Queimados - RJ", "Queiroz - SP", "Queluz - SP", "Queluzito - MG", "Querencia - MT", "Querencia do Norte - PR", "Quevedos - RS", "Quijingue - BA", "Quilombo - SC", "Quinta do Sol - PR", "Quintana - SP", "Quinze de Novembro - RS", "Quipapa - PE", "Quirinopolis - GO", "Quissama - RJ", "Quitandinha - PR", "Quiterianopolis - CE", "Quixaba - PE", "Quixaba - PB",
			"Quixabeira - BA", "Quixada - CE", "Quixelo - CE", "Quixeramobim - CE", "Quixere - CE", "Rafael Fernandes - RN", "Rafael Godeiro - RN", "Rafael Jambeiro - BA", "Rafard - SP", "Ramilandia - PR", "Rancharia - SP", "Rancho Alegre - PR", "Rancho Alegre DOeste - PR", "Rancho Queimado - SC", "Raposa - MA", "Raposos - MG", "Raul Soares - MG", "Realeza - PR", "Reboucas - PR", "Recife - PE", "Recreio - MG", "Recursolandia - TO", "Redencao - PA", "Redencao - CE", "Redencao da Serra - SP", "Redencao do Gurgueia - PI", "Redentora - RS", "Reduto - MG", "Regeneracao - PI", "Regente Feijo - SP", "Reginopolis - SP", "Registro - SP", "Relvado - RS", "Remanso - BA", "Remigio - PB", "Renascenca - PR", "Reriutaba - CE", "Resende - RJ", "Resende Costa - MG", "Reserva - PR", "Reserva do Cabacal - MT", "Reserva do Iguacu - PR", "Resplendor - MG", "Ressaquinha - MG", "Restinga - SP", "Restinga Seca - RS", "Retirolandia - BA", "Riachao - PB", "Riachao - MA", "Riachao das Neves - BA",
			"Riachao do Bacamarte - PB", "Riachao do Dantas - SE", "Riachao do Jacuipe - BA", "Riachao do Poco - PB", "Riachinho - MG", "Riachinho - TO", "Riacho da Cruz - RN", "Riacho das Almas - PE", "Riacho de Santana - RN", "Riacho de Santana - BA", "Riacho de Santo Antonio - PB", "Riacho dos Cavalos - PB", "Riacho dos Machados - MG", "Riacho Frio - PI", "Riachuelo - RN", "Riachuelo - SE", "Rialma - GO", "Rianapolis - GO", "Ribamar Fiquene - MA", "Ribas do Rio Pardo - MS", "Ribeira - SP", "Ribeira do Amparo - BA", "Ribeira do Piaui - PI", "Ribeira do Pombal - BA", "Ribeirao - PE", "Ribeirao Bonito - SP", "Ribeirao Branco - SP", "Ribeirao Cascalheira - MT", "Ribeirao Claro - PR", "Ribeirao Corrente - SP", "Ribeirao das Neves - MG", "Ribeirao do Largo - BA", "Ribeirao do Pinhal - PR", "Ribeirao do Sul - SP", "Ribeirao dos indios - SP", "Ribeirao Grande - SP", "Ribeirao Pires - SP", "Ribeirao Preto - SP", "Ribeirao Vermelho - MG", "Ribeiraozinho - MT", "Ribeiro Goncalves - PI",
			"Ribeiropolis - SE", "Rifaina - SP", "Rincao - SP", "Rinopolis - SP", "Rio Acima - MG", "Rio Azul - PR", "Rio Bananal - ES", "Rio Bom - PR", "Rio Bonito - RJ", "Rio Bonito do Iguacu - PR", "Rio Branco - AC", "Rio Branco - MT", "Rio Branco do Ivai - PR", "Rio Branco do Sul - PR", "Rio Brilhante - MS", "Rio Casca - MG", "Rio Claro - RJ", "Rio Claro - SP", "Rio Crespo - RO", "Rio da Conceicao - TO", "Rio das Antas - SC", "Rio das Flores - RJ", "Rio das Ostras - RJ", "Rio das Pedras - SP", "Rio de Contas - BA", "Rio de Janeiro - RJ", "Rio do Antonio - BA", "Rio do Campo - SC", "Rio do Fogo - RN", "Rio do Oeste - SC", "Rio do Pires - BA", "Rio do Prado - MG", "Rio do Sul - SC", "Rio Doce - MG", "Rio dos Bois - TO", "Rio dos Cedros - SC", "Rio dos indios - RS", "Rio Espera - MG", "Rio Formoso - PE", "Rio Fortuna - SC", "Rio Grande - RS", "Rio Grande da Serra - SP", "Rio Grande do Piaui - PI", "Rio Largo - AL", "Rio Manso - MG", "Rio Maria - PA", "Rio Negrinho - SC",
			"Rio Negro - PR", "Rio Negro - MS", "Rio Novo - MG", "Rio Novo do Sul - ES", "Rio Paranaiba - MG", "Rio Pardo - RS", "Rio Pardo de Minas - MG", "Rio Piracicaba - MG", "Rio Pomba - MG", "Rio Preto - MG", "Rio Preto da Eva - AM", "Rio Quente - GO", "Rio Real - BA", "Rio Rufino - SC", "Rio Sono - TO", "Rio Tinto - PB", "Rio Verde - GO", "Rio Verde de Mato Grosso - MS", "Rio Vermelho - MG", "Riolandia - SP", "Riozinho - RS", "Riqueza - SC", "Ritapolis - MG", "Riversul - SP", "Roca Sales - RS", "Rochedo - MS", "Rochedo de Minas - MG", "Rodeio - SC", "Rodeio Bonito - RS", "Rodeiro - MG", "Rodelas - BA", "Rodolfo Fernandes - RN", "Rodrigues Alves - AC", "Rolador - RS", "Rolandia - PR", "Rolante - RS", "Rolim de Moura - RO", "Romaria - MG", "Romelandia - SC", "Roncador - PR", "Ronda Alta - RS", "Rondinha - RS", "Rondolandia - MT", "Rondon - PR", "Rondon do Para - PA", "Rondonopolis - MT", "Roque Gonzales - RS", "Rorainopolis - RR", "Rosana - SP", "Rosario - MA",
			"Rosario da Limeira - MG", "Rosario do Catete - SE", "Rosario do Ivai - PR", "Rosario do Sul - RS", "Rosario Oeste - MT", "Roseira - SP", "Roteiro - AL", "Rubelita - MG", "Rubiacea - SP", "Rubiataba - GO", "Rubim - MG", "Rubineia - SP", "Ruropolis - PA", "Russas - CE", "Ruy Barbosa - RN", "Ruy Barbosa - BA", "Sabara - MG", "Sabaudia - PR", "Sabino - SP", "Sabinopolis - MG", "Saboeiro - CE", "Sacramento - MG", "Sagrada Familia - RS", "Sagres - SP", "Saire - PE", "Saldanha Marinho - RS", "Sales - SP", "Sales Oliveira - SP", "Salesopolis - SP", "Salete - SC", "Salgadinho - PB", "Salgadinho - PE", "Salgado - SE", "Salgado de Sao Felix - PB", "Salgado Filho - PR", "Salgueiro - PE", "Salinas - MG", "Salinas da Margarida - BA", "Salinopolis - PA", "Salitre - CE", "Salmourao - SP", "Saloa - PE", "Saltinho - SP", "Saltinho - SC", "Salto - SP", "Salto da Divisa - MG", "Salto de Pirapora - SP", "Salto do Ceu - MT", "Salto do Itarare - PR", "Salto do Jacui - RS",
			"Salto do Lontra - PR", "Salto Grande - SP", "Salto Veloso - SC", "Salvador - BA", "Salvador das Missoes - RS", "Salvador do Sul - RS", "Salvaterra - PA", "Sambaiba - MA", "Sampaio - TO", "Sananduva - RS", "Sanclerlandia - GO", "Sandolandia - TO", "Sandovalina - SP", "Sangao - SC", "Sanharo - PE", "Santa Adelia - SP", "Santa Albertina - SP", "Santa Amelia - PR", "Santa Barbara - MG", "Santa Barbara - BA", "Santa Barbara de Goias - GO", "Santa Barbara do Leste - MG", "Santa Barbara do Monte Verde - MG", "Santa Barbara do Para - PA", "Santa Barbara do Sul - RS", "Santa Barbara do Tugurio - MG", "Santa Barbara dOeste - SP", "Santa Branca - SP", "Santa Brigida - BA", "Santa Carmem - MT", "Santa Cecilia - SC", "Santa Cecilia - PB", "Santa Cecilia do Pavao - PR", "Santa Cecilia do Sul - RS", "Santa Clara do Sul - RS", "Santa Clara dOeste - SP", "Santa Cruz - RN", "Santa Cruz - PE", "Santa Cruz - PB", "Santa Cruz Cabralia - BA", "Santa Cruz da Baixa Verde - PE",
			"Santa Cruz da Conceicao - SP", "Santa Cruz da Esperanca - SP", "Santa Cruz da Vitoria - BA", "Santa Cruz das Palmeiras - SP", "Santa Cruz de Goias - GO", "Santa Cruz de Minas - MG", "Santa Cruz de Monte Castelo - PR", "Santa Cruz de Salinas - MG", "Santa Cruz do Arari - PA", "Santa Cruz do Capibaribe - PE", "Santa Cruz do Escalvado - MG", "Santa Cruz do Piaui - PI", "Santa Cruz do Rio Pardo - SP", "Santa Cruz do Sul - RS", "Santa Cruz do Xingu - MT", "Santa Cruz dos Milagres - PI", "Santa Efigenia de Minas - MG", "Santa Ernestina - SP", "Santa Fe - PR", "Santa Fe de Goias - GO", "Santa Fe de Minas - MG", "Santa Fe do Araguaia - TO", "Santa Fe do Sul - SP", "Santa Filomena - PI", "Santa Filomena - PE", "Santa Filomena do Maranhao - MA", "Santa Gertrudes - SP", "Santa Helena - MA", "Santa Helena - PR", "Santa Helena - PB", "Santa Helena - SC", "Santa Helena de Goias - GO", "Santa Helena de Minas - MG", "Santa Ines - BA", "Santa Ines - PR", "Santa Ines - PB",
			"Santa Ines - MA", "Santa Isabel - SP", "Santa Isabel - GO", "Santa Isabel do Ivai - PR", "Santa Isabel do Para - PA", "Santa Isabel do Rio Negro - AM", "Santa Izabel do Oeste - PR", "Santa Juliana - MG", "Santa Leopoldina - ES", "Santa Lucia - SP", "Santa Lucia - PR", "Santa Luz - PI", "Santa Luzia - PB", "Santa Luzia - MG", "Santa Luzia - MA", "Santa Luzia - BA", "Santa Luzia do Itanhy - SE", "Santa Luzia do Norte - AL", "Santa Luzia do Para - PA", "Santa Luzia do Parua - MA", "Santa Luzia DOeste - RO", "Santa Margarida - MG", "Santa Margarida do Sul - RS", "Santa Maria - RS", "Santa Maria - RN", "Santa Maria da Boa Vista - PE", "Santa Maria da Serra - SP", "Santa Maria da Vitoria - BA", "Santa Maria das Barreiras - PA", "Santa Maria de Itabira - MG", "Santa Maria de Jetiba - ES", "Santa Maria do Cambuca - PE", "Santa Maria do Herval - RS", "Santa Maria do Oeste - PR", "Santa Maria do Para - PA", "Santa Maria do Salto - MG", "Santa Maria do Suacui - MG",
			"Santa Maria do Tocantins - TO", "Santa Maria Madalena - RJ", "Santa Mariana - PR", "Santa Mercedes - SP", "Santa Monica - PR", "Santa Quiteria - CE", "Santa Quiteria do Maranhao - MA", "Santa Rita - MA", "Santa Rita - PB", "Santa Rita de Caldas - MG", "Santa Rita de Cassia - BA", "Santa Rita de Ibitipoca - MG", "Santa Rita de Jacutinga - MG", "Santa Rita de Minas - MG", "Santa Rita do Araguaia - GO", "Santa Rita do Itueto - MG", "Santa Rita do Novo Destino - GO", "Santa Rita do Pardo - MS", "Santa Rita do Passa Quatro - SP", "Santa Rita do Sapucai - MG", "Santa Rita do Tocantins - TO", "Santa Rita do Trivelato - MT", "Santa Rita dOeste - SP", "Santa Rosa - RS", "Santa Rosa da Serra - MG", "Santa Rosa de Goias - GO", "Santa Rosa de Lima - SE", "Santa Rosa de Lima - SC", "Santa Rosa de Viterbo - SP", "Santa Rosa do Piaui - PI", "Santa Rosa do Purus - AC", "Santa Rosa do Sul - SC", "Santa Rosa do Tocantins - TO", "Santa Salete - SP", "Santa Teresa - ES",
			"Santa Teresinha - BA", "Santa Teresinha - PB", "Santa Tereza - RS", "Santa Tereza de Goias - GO", "Santa Tereza do Oeste - PR", "Santa Tereza do Tocantins - TO", "Santa Terezinha - PE", "Santa Terezinha - SC", "Santa Terezinha - MT", "Santa Terezinha de Goias - GO", "Santa Terezinha de Itaipu - PR", "Santa Terezinha do Progresso - SC", "Santa Terezinha do Tocantins - TO", "Santa Vitoria - MG", "Santa Vitoria do Palmar - RS", "Santaluz - BA", "Santana - AP", "Santana - BA", "Santana da Boa Vista - RS", "Santana da Ponte Pensa - SP", "Santana da Vargem - MG", "Santana de Cataguases - MG", "Santana de Mangueira - PB", "Santana de Parnaiba - SP", "Santana de Pirapama - MG", "Santana do Acarau - CE", "Santana do Araguaia - PA", "Santana do Cariri - CE", "Santana do Deserto - MG", "Santana do Garambeu - MG", "Santana do Ipanema - AL", "Santana do Itarare - PR", "Santana do Jacare - MG", "Santana do Livramento - RS", "Santana do Manhuacu - MG", "Santana do Maranhao - MA",
			"Santana do Matos - RN", "Santana do Mundau - AL", "Santana do Paraiso - MG", "Santana do Piaui - PI", "Santana do Riacho - MG", "Santana do Sao Francisco - SE", "Santana do Serido - RN", "Santana dos Garrotes - PB", "Santana dos Montes - MG", "Santanopolis - BA", "Santarem - PB", "Santarem - PA", "Santarem Novo - PA", "Santiago - RS", "Santiago do Sul - SC", "Santo Afonso - MT", "Santo Amaro - BA", "Santo Amaro da Imperatriz - SC", "Santo Amaro das Brotas - SE", "Santo Amaro do Maranhao - MA", "Santo Anastacio - SP", "Santo Andre - SP", "Santo Andre - PB", "Santo angelo - RS", "Santo Antonio - RN", "Santo Antonio da Alegria - SP", "Santo Antonio da Barra - GO", "Santo Antonio da Patrulha - RS", "Santo Antonio da Platina - PR", "Santo Antonio das Missoes - RS", "Santo Antonio de Goias - GO", "Santo Antonio de Jesus - BA", "Santo Antonio de Lisboa - PI", "Santo Antonio de Padua - RJ", "Santo Antonio de Posse - SP", "Santo Antonio do Amparo - MG",
			"Santo Antonio do Aracangua - SP", "Santo Antonio do Aventureiro - MG", "Santo Antonio do Caiua - PR", "Santo Antonio do Descoberto - GO", "Santo Antonio do Grama - MG", "Santo Antonio do Ica - AM", "Santo Antonio do Itambe - MG", "Santo Antonio do Jacinto - MG", "Santo Antonio do Jardim - SP", "Santo Antonio do Leste - MT", "Santo Antonio do Leverger - MT", "Santo Antonio do Monte - MG", "Santo Antonio do Palma - RS", "Santo Antonio do Paraiso - PR", "Santo Antonio do Pinhal - SP", "Santo Antonio do Planalto - RS", "Santo Antonio do Retiro - MG", "Santo Antonio do Rio Abaixo - MG", "Santo Antonio do Sudoeste - PR", "Santo Antonio do Taua - PA", "Santo Antonio dos Lopes - MA", "Santo Antonio dos Milagres - PI", "Santo Augusto - RS", "Santo Cristo - RS", "Santo Estevao - BA", "Santo Expedito - SP", "Santo Expedito do Sul - RS", "Santo Hipolito - MG", "Santo Inacio - PR", "Santo Inacio do Piaui - PI", "Santopolis do Aguapei - SP", "Santos - SP", "Santos Dumont - MG",
			"Sao Benedito - CE", "Sao Benedito do Rio Preto - MA", "Sao Benedito do Sul - PE", "Sao Bentinho - PB", "Sao Bento - MA", "Sao Bento - PB", "Sao Bento Abade - MG", "Sao Bento do Norte - RN", "Sao Bento do Sapucai - SP", "Sao Bento do Sul - SC", "Sao Bento do Tocantins - TO", "Sao Bento do Trairi - RN", "Sao Bento do Una - PE", "Sao Bernardino - SC", "Sao Bernardo - MA", "Sao Bernardo do Campo - SP", "Sao Bonifacio - SC", "Sao Borja - RS", "Sao Bras - AL", "Sao Bras do Suacui - MG", "Sao Braz do Piaui - PI", "Sao Caetano de Odivelas - PA", "Sao Caetano do Sul - SP", "Sao Caitano - PE", "Sao Carlos - SP", "Sao Carlos - SC", "Sao Carlos do Ivai - PR", "Sao Cristovao - SE", "Sao Cristovao do Sul - SC", "Sao Desiderio - BA", "Sao Domingos - BA", "Sao Domingos - SC", "Sao Domingos - SE", "Sao Domingos - GO", "Sao Domingos das Dores - MG", "Sao Domingos de Pombal - PB", "Sao Domingos do Araguaia - PA", "Sao Domingos do Azeitao - MA", "Sao Domingos do Capim - PA",
			"Sao Domingos do Cariri - PB", "Sao Domingos do Maranhao - MA", "Sao Domingos do Norte - ES", "Sao Domingos do Prata - MG", "Sao Domingos do Sul - RS", "Sao Felipe - BA", "Sao Felipe DOeste - RO", "Sao Felix - BA", "Sao Felix de Balsas - MA", "Sao Felix de Minas - MG", "Sao Felix do Araguaia - MT", "Sao Felix do Coribe - BA", "Sao Felix do Piaui - PI", "Sao Felix do Tocantins - TO", "Sao Felix do Xingu - PA", "Sao Fernando - RN", "Sao Fidelis - RJ", "Sao Francisco - MG", "Sao Francisco - SP", "Sao Francisco - SE", "Sao Francisco - PB", "Sao Francisco de Assis - RS", "Sao Francisco de Assis do Piaui - PI", "Sao Francisco de Goias - GO", "Sao Francisco de Itabapoana - RJ", "Sao Francisco de Paula - RS", "Sao Francisco de Paula - MG", "Sao Francisco de Sales - MG", "Sao Francisco do Brejao - MA", "Sao Francisco do Conde - BA", "Sao Francisco do Gloria - MG", "Sao Francisco do Guapore - RO", "Sao Francisco do Maranhao - MA", "Sao Francisco do Oeste - RN",
			"Sao Francisco do Para - PA", "Sao Francisco do Piaui - PI", "Sao Francisco do Sul - SC", "Sao Gabriel - RS", "Sao Gabriel - BA", "Sao Gabriel da Cachoeira - AM", "Sao Gabriel da Palha - ES", "Sao Gabriel do Oeste - MS", "Sao Geraldo - MG", "Sao Geraldo da Piedade - MG", "Sao Geraldo do Araguaia - PA", "Sao Geraldo do Baixio - MG", "Sao Goncalo - RJ", "Sao Goncalo do Abaete - MG", "Sao Goncalo do Amarante - CE", "Sao Goncalo do Amarante - RN", "Sao Goncalo do Gurgueia - PI", "Sao Goncalo do Para - MG", "Sao Goncalo do Piaui - PI", "Sao Goncalo do Rio Abaixo - MG", "Sao Goncalo do Rio Preto - MG", "Sao Goncalo do Sapucai - MG", "Sao Goncalo dos Campos - BA", "Sao Gotardo - MG", "Sao Jeronimo - RS", "Sao Jeronimo da Serra - PR", "Sao Joao - PE", "Sao Joao - PR", "Sao Joao Batista - MA", "Sao Joao Batista - SC", "Sao Joao Batista do Gloria - MG", "Sao Joao da Baliza - RR", "Sao Joao da Barra - RJ", "Sao Joao da Boa Vista - SP", "Sao Joao da Canabrava - PI",
			"Sao Joao da Fronteira - PI", "Sao Joao da Lagoa - MG", "Sao Joao da Mata - MG", "Sao Joao da Parauna - GO", "Sao Joao da Ponta - PA", "Sao Joao da Ponte - MG", "Sao Joao da Serra - PI", "Sao Joao da Urtiga - RS", "Sao Joao da Varjota - PI", "Sao Joao dAlianca - GO", "Sao Joao das Duas Pontes - SP", "Sao Joao das Missoes - MG", "Sao Joao de Iracema - SP", "Sao Joao de Meriti - RJ", "Sao Joao de Pirabas - PA", "Sao Joao del Rei - MG", "Sao Joao do Araguaia - PA", "Sao Joao do Arraial - PI", "Sao Joao do Caiua - PR", "Sao Joao do Cariri - PB", "Sao Joao do Caru - MA", "Sao Joao do Itaperiu - SC", "Sao Joao do Ivai - PR", "Sao Joao do Jaguaribe - CE", "Sao Joao do Manhuacu - MG", "Sao Joao do Manteninha - MG", "Sao Joao do Oeste - SC", "Sao Joao do Oriente - MG", "Sao Joao do Pacui - MG", "Sao Joao do Paraiso - MG", "Sao Joao do Paraiso - MA", "Sao Joao do Pau dAlho - SP", "Sao Joao do Piaui - PI", "Sao Joao do Polesine - RS", "Sao Joao do Rio do Peixe - PB",
			"Sao Joao do Sabugi - RN", "Sao Joao do Soter - MA", "Sao Joao do Sul - SC", "Sao Joao do Tigre - PB", "Sao Joao do Triunfo - PR", "Sao Joao dos Patos - MA", "Sao Joao Evangelista - MG", "Sao Joao Nepomuceno - MG", "Sao Joaquim - SC", "Sao Joaquim da Barra - SP", "Sao Joaquim de Bicas - MG", "Sao Joaquim do Monte - PE", "Sao Jorge - RS", "Sao Jorge do Ivai - PR", "Sao Jorge do Patrocinio - PR", "Sao Jorge dOeste - PR", "Sao Jose - SC", "Sao Jose da Barra - MG", "Sao Jose da Bela Vista - SP", "Sao Jose da Boa Vista - PR", "Sao Jose da Coroa Grande - PE", "Sao Jose da Lagoa Tapada - PB", "Sao Jose da Laje - AL", "Sao Jose da Lapa - MG", "Sao Jose da Safira - MG", "Sao Jose da Tapera - AL", "Sao Jose da Varginha - MG", "Sao Jose da Vitoria - BA", "Sao Jose das Missoes - RS", "Sao Jose das Palmeiras - PR", "Sao Jose de Caiana - PB", "Sao Jose de Espinharas - PB", "Sao Jose de Mipibu - RN", "Sao Jose de Piranhas - PB", "Sao Jose de Princesa - PB", "Sao Jose de Ribamar - MA",
			"Sao Jose de Uba - RJ", "Sao Jose do Alegre - MG", "Sao Jose do Barreiro - SP", "Sao Jose do Belmonte - PE", "Sao Jose do Bonfim - PB", "Sao Jose do Brejo do Cruz - PB", "Sao Jose do Calcado - ES", "Sao Jose do Campestre - RN", "Sao Jose do Cedro - SC", "Sao Jose do Cerrito - SC", "Sao Jose do Divino - MG", "Sao Jose do Divino - PI", "Sao Jose do Egito - PE", "Sao Jose do Goiabal - MG", "Sao Jose do Herval - RS", "Sao Jose do Hortencio - RS", "Sao Jose do Inhacora - RS", "Sao Jose do Jacuipe - BA", "Sao Jose do Jacuri - MG", "Sao Jose do Mantimento - MG", "Sao Jose do Norte - RS", "Sao Jose do Ouro - RS", "Sao Jose do Peixe - PI", "Sao Jose do Piaui - PI", "Sao Jose do Povo - MT", "Sao Jose do Rio Claro - MT", "Sao Jose do Rio Pardo - SP", "Sao Jose do Rio Preto - SP", "Sao Jose do Sabugi - PB", "Sao Jose do Serido - RN", "Sao Jose do Sul - RS", "Sao Jose do Vale do Rio Preto - RJ", "Sao Jose do Xingu - MT", "Sao Jose dos Ausentes - RS", "Sao Jose dos Basilios - MA",
			"Sao Jose dos Campos - SP", "Sao Jose dos Cordeiros - PB", "Sao Jose dos Pinhais - PR", "Sao Jose dos Quatro Marcos - MT", "Sao Jose dos Ramos - PB", "Sao Juliao - PI", "Sao Leopoldo - RS", "Sao Lourenco - MG", "Sao Lourenco da Mata - PE", "Sao Lourenco da Serra - SP", "Sao Lourenco do Oeste - SC", "Sao Lourenco do Piaui - PI", "Sao Lourenco do Sul - RS", "Sao Ludgero - SC", "Sao Luis - MA", "Sao Luis de Montes Belos - GO", "Sao Luis do Curu - CE", "Sao Luis do Paraitinga - SP", "Sao Luis do Piaui - PI", "Sao Luis do Quitunde - AL", "Sao Luis Gonzaga do Maranhao - MA", "Sao Luiz - RR", "Sao Luiz do Norte - GO", "Sao Luiz Gonzaga - RS", "Sao Mamede - PB", "Sao Manoel do Parana - PR", "Sao Manuel - SP", "Sao Marcos - RS", "Sao Martinho - RS", "Sao Martinho - SC", "Sao Martinho da Serra - RS", "Sao Mateus - ES", "Sao Mateus do Maranhao - MA", "Sao Mateus do Sul - PR", "Sao Miguel - RN", "Sao Miguel Arcanjo - SP", "Sao Miguel da Baixa Grande - PI",
			"Sao Miguel da Boa Vista - SC", "Sao Miguel das Matas - BA", "Sao Miguel das Missoes - RS", "Sao Miguel de Taipu - PB", "Sao Miguel do Aleixo - SE", "Sao Miguel do Anta - MG", "Sao Miguel do Araguaia - GO", "Sao Miguel do Fidalgo - PI", "Sao Miguel do Gostoso - RN", "Sao Miguel do Guama - PA", "Sao Miguel do Guapore - RO", "Sao Miguel do Iguacu - PR", "Sao Miguel do Oeste - SC", "Sao Miguel do Passa Quatro - GO", "Sao Miguel do Tapuio - PI", "Sao Miguel do Tocantins - TO", "Sao Miguel dos Campos - AL", "Sao Miguel dos Milagres - AL", "Sao Nicolau - RS", "Sao Patricio - GO", "Sao Paulo - SP", "Sao Paulo das Missoes - RS", "Sao Paulo de Olivenca - AM", "Sao Paulo do Potengi - RN", "Sao Pedro - SP", "Sao Pedro - RN", "Sao Pedro da agua Branca - MA", "Sao Pedro da Aldeia - RJ", "Sao Pedro da Cipa - MT", "Sao Pedro da Serra - RS", "Sao Pedro da Uniao - MG", "Sao Pedro das Missoes - RS", "Sao Pedro de Alcantara - SC", "Sao Pedro do Butia - RS", "Sao Pedro do Iguacu - PR",
			"Sao Pedro do Ivai - PR", "Sao Pedro do Parana - PR", "Sao Pedro do Piaui - PI", "Sao Pedro do Suacui - MG", "Sao Pedro do Sul - RS", "Sao Pedro do Turvo - SP", "Sao Pedro dos Crentes - MA", "Sao Pedro dos Ferros - MG", "Sao Rafael - RN", "Sao Raimundo das Mangabeiras - MA", "Sao Raimundo do Doca Bezerra - MA", "Sao Raimundo Nonato - PI", "Sao Roberto - MA", "Sao Romao - MG", "Sao Roque - SP", "Sao Roque de Minas - MG", "Sao Roque do Canaa - ES", "Sao Salvador do Tocantins - TO", "Sao Sebastiao - AL", "Sao Sebastiao - SP", "Sao Sebastiao da Amoreira - PR", "Sao Sebastiao da Bela Vista - MG", "Sao Sebastiao da Boa Vista - PA", "Sao Sebastiao da Grama - SP", "Sao Sebastiao da Vargem Alegre - MG", "Sao Sebastiao de Lagoa de Roca - PB", "Sao Sebastiao do Alto - RJ", "Sao Sebastiao do Anta - MG", "Sao Sebastiao do Cai - RS", "Sao Sebastiao do Maranhao - MG", "Sao Sebastiao do Oeste - MG", "Sao Sebastiao do Paraiso - MG", "Sao Sebastiao do Passe - BA",
			"Sao Sebastiao do Rio Preto - MG", "Sao Sebastiao do Rio Verde - MG", "Sao Sebastiao do Tocantins - TO", "Sao Sebastiao do Uatuma - AM", "Sao Sebastiao do Umbuzeiro - PB", "Sao Sepe - RS", "Sao Simao - GO", "Sao Simao - SP", "Sao Thome das Letras - MG", "Sao Tiago - MG", "Sao Tomas de Aquino - MG", "Sao Tome - PR", "Sao Tome - RN", "Sao Valentim - RS", "Sao Valentim do Sul - RS", "Sao Valerio da Natividade - TO", "Sao Valerio do Sul - RS", "Sao Vendelino - RS", "Sao Vicente - SP", "Sao Vicente - RN", "Sao Vicente de Minas - MG", "Sao Vicente do Sul - RS", "Sao Vicente Ferrer - PE", "Sao Vicente Ferrer - MA", "Sape - PB", "Sapeacu - BA", "Sapezal - MT", "Sapiranga - RS", "Sapopema - PR", "Sapucaia - RJ", "Sapucaia - PA", "Sapucaia do Sul - RS", "Sapucai-Mirim - MG", "Saquarema - RJ", "Sarandi - RS", "Sarandi - PR", "Sarapui - SP", "Sardoa - MG", "Sarutaia - SP", "Sarzedo - MG", "Satiro Dias - BA", "Satuba - AL", "Satubinha - MA", "Saubara - BA", "Saudade do Iguacu - PR",
			"Saudades - SC", "Saude - BA", "Schroeder - SC", "Seabra - BA", "Seara - SC", "Sebastianopolis do Sul - SP", "Sebastiao Barros - PI", "Sebastiao Laranjeiras - BA", "Sebastiao Leal - PI", "Seberi - RS", "Sede Nova - RS", "Segredo - RS", "Selbach - RS", "Selviria - MS", "Sem-Peixe - MG", "Sena Madureira - AC", "Senador Alexandre Costa - MA", "Senador Amaral - MG", "Senador Canedo - GO", "Senador Cortes - MG", "Senador Eloi de Souza - RN", "Senador Firmino - MG", "Senador Georgino Avelino - RN", "Senador Guiomard - AC", "Senador Jose Bento - MG", "Senador Jose Porfirio - PA", "Senador La Rocque - MA", "Senador Modestino Goncalves - MG", "Senador Pompeu - CE", "Senador Rui Palmeira - AL", "Senador Sa - CE", "Senador Salgado Filho - RS", "Senges - PR", "Senhor do Bonfim - BA", "Senhora de Oliveira - MG", "Senhora do Porto - MG", "Senhora dos Remedios - MG", "Sentinela do Sul - RS", "Sento Se - BA", "Serafina Correa - RS", "Sericita - MG", "Serido - PB", "Seringueiras - RO",
			"Serio - RS", "Seritinga - MG", "Seropedica - RJ", "Serra - ES", "Serra Alta - SC", "Serra Azul - SP", "Serra Azul de Minas - MG", "Serra Branca - PB", "Serra da Raiz - PB", "Serra da Saudade - MG", "Serra de Sao Bento - RN", "Serra do Mel - RN", "Serra do Navio - AP", "Serra do Ramalho - BA", "Serra do Salitre - MG", "Serra dos Aimores - MG", "Serra Dourada - BA", "Serra Grande - PB", "Serra Negra - SP", "Serra Negra do Norte - RN", "Serra Nova Dourada - MT", "Serra Preta - BA", "Serra Redonda - PB", "Serra Talhada - PE", "Serrana - SP", "Serrania - MG", "Serrano do Maranhao - MA", "Serranopolis - GO", "Serranopolis de Minas - MG", "Serranopolis do Iguacu - PR", "Serranos - MG", "Serraria - PB", "Serrinha - RN", "Serrinha - BA", "Serrinha dos Pintos - RN", "Serrita - PE", "Serro - MG", "Serrolandia - BA", "Sertaneja - PR", "Sertania - PE", "Sertanopolis - PR", "Sertao - RS", "Sertao Santana - RS", "Sertaozinho - SP", "Sertaozinho - PB", "Sete Barras - SP",
			"Sete de Setembro - RS", "Sete Lagoas - MG", "Sete Quedas - MS", "Setubinha - MG", "Severiano de Almeida - RS", "Severiano Melo - RN", "Severinia - SP", "Sideropolis - SC", "Sidrolandia - MS", "Sigefredo Pacheco - PI", "Silva Jardim - RJ", "Silvania - GO", "Silvanopolis - TO", "Silveira Martins - RS", "Silveirania - MG", "Silveiras - SP", "Silves - AM", "Silvianopolis - MG", "Simao Dias - SE", "Simao Pereira - MG", "Simoes - PI", "Simoes Filho - BA", "Simolandia - GO", "Simonesia - MG", "Simplicio Mendes - PI", "Sinimbu - RS", "Sinop - MT", "Siqueira Campos - PR", "Sirinhaem - PE", "Siriri - SE", "Sitio dAbadia - GO", "Sitio do Mato - BA", "Sitio do Quinto - BA", "Sitio Novo - RN", "Sitio Novo - MA", "Sitio Novo do Tocantins - TO", "Sobradinho - RS", "Sobradinho - BA", "Sobrado - PB", "Sobral - CE", "Sobralia - MG", "Socorro - SP", "Socorro do Piaui - PI", "Solanea - PB", "Soledade - PB", "Soledade - RS", "Soledade de Minas - MG", "Solidao - PE", "Solonopole - CE",
			"Sombrio - SC", "Sonora - MS", "Sooretama - ES", "Sorocaba - SP", "Sorriso - MT", "Sossego - PB", "Soure - PA", "Sousa - PB", "Souto Soares - BA", "Sucupira - TO", "Sucupira do Norte - MA", "Sucupira do Riachao - MA", "Sud Mennucci - SP", "Sul Brasil - SC", "Sulina - PR", "Sumare - SP", "Sume - PB", "Sumidouro - RJ", "Surubim - PE", "Sussuapara - PI", "Suzanapolis - SP", "Suzano - SP", "Tabai - RS", "Tabapora - MT", "Tabapua - SP", "Tabatinga - AM", "Tabatinga - SP", "Tabira - PE", "Taboao da Serra - SP", "Tabocas do Brejo Velho - BA", "Taboleiro Grande - RN", "Tabuleiro - MG", "Tabuleiro do Norte - CE", "Tacaimbo - PE", "Tacaratu - PE", "Taciba - SP", "Tacuru - MS", "Taguai - SP", "Taguatinga - TO", "Taiacu - SP", "Tailandia - PA", "Taio - SC", "Taiobeiras - MG", "Taipas do Tocantins - TO", "Taipu - RN", "Taiuva - SP", "Talisma - TO", "Tamandare - PE", "Tamarana - PR", "Tambau - SP", "Tamboara - PR", "Tamboril - CE", "Tamboril do Piaui - PI", "Tanabi - SP",
			"Tangara - RN", "Tangara - SC", "Tangara da Serra - MT", "Tangua - RJ", "Tanhacu - BA", "Tanque dArca - AL", "Tanque do Piaui - PI", "Tanque Novo - BA", "Tanquinho - BA", "Taparuba - MG", "Tapaua - AM", "Tapejara - PR", "Tapejara - RS", "Tapera - RS", "Taperoa - BA", "Taperoa - PB", "Tapes - RS", "Tapira - MG", "Tapira - PR", "Tapirai - MG", "Tapirai - SP", "Tapiramuta - BA", "Tapiratiba - SP", "Tapurah - MT", "Taquara - RS", "Taquaracu de Minas - MG", "Taquaral - SP", "Taquaral de Goias - GO", "Taquarana - AL", "Taquari - RS", "Taquaritinga - SP", "Taquaritinga do Norte - PE", "Taquarituba - SP", "Taquarivai - SP", "Taquarucu do Sul - RS", "Taquarussu - MS", "Tarabai - SP", "Tarauaca - AC", "Tarrafas - CE", "Tartarugalzinho - AP", "Taruma - SP", "Tarumirim - MG", "Tasso Fragoso - MA", "Tatui - SP", "Taua - CE", "Taubate - SP", "Tavares - PB", "Tavares - RS", "Tefe - AM", "Teixeira - PB", "Teixeira de Freitas - BA", "Teixeira Soares - PR", "Teixeiras - MG",
			"Teixeiropolis - RO", "Tejucuoca - CE", "Tejupa - SP", "Telemaco Borba - PR", "Telha - SE", "Tenente Ananias - RN", "Tenente Laurentino Cruz - RN", "Tenente Portela - RS", "Tenorio - PB", "Teodoro Sampaio - BA", "Teodoro Sampaio - SP", "Teofilandia - BA", "Teofilo Otoni - MG", "Teolandia - BA", "Teotonio Vilela - AL", "Terenos - MS", "Teresina - PI", "Teresina de Goias - GO", "Teresopolis - RJ", "Terezinha - PE", "Terezopolis de Goias - GO", "Terra Alta - PA", "Terra Boa - PR", "Terra de Areia - RS", "Terra Nova - PE", "Terra Nova - BA", "Terra Nova do Norte - MT", "Terra Rica - PR", "Terra Roxa - PR", "Terra Roxa - SP", "Terra Santa - PA", "Tesouro - MT", "Teutonia - RS", "Theobroma - RO", "Tiangua - CE", "Tibagi - PR", "Tibau - RN", "Tibau do Sul - RN", "Tiete - SP", "Tigrinhos - SC", "Tijucas - SC", "Tijucas do Sul - PR", "Timbauba - PE", "Timbauba dos Batistas - RN", "Timbe do Sul - SC", "Timbiras - MA", "Timbo - SC", "Timbo Grande - SC", "Timburi - SP", "Timon - MA",
			"Timoteo - MG", "Tio Hugo - RS", "Tiradentes - MG", "Tiradentes do Sul - RS", "Tiros - MG", "Tobias Barreto - SE", "Tocantinia - TO", "Tocantinopolis - TO", "Tocantins - MG", "Tocos do Moji - MG", "Toledo - MG", "Toledo - PR", "Tomar do Geru - SE", "Tomazina - PR", "Tombos - MG", "Tome-Acu - PA", "Tonantins - AM", "Toritama - PE", "Torixoreu - MT", "Toropi - RS", "Torre de Pedra - SP", "Torres - RS", "Torrinha - SP", "Touros - RN", "Trabiju - SP", "Tracuateua - PA", "Tracunhaem - PE", "Traipu - AL", "Trairao - PA", "Trairi - CE", "Trajano de Morais - RJ", "Tramandai - RS", "Travesseiro - RS", "Tremedal - BA", "Tremembe - SP", "Tres Arroios - RS", "Tres Barras - SC", "Tres Barras do Parana - PR", "Tres Cachoeiras - RS", "Tres Coracoes - MG", "Tres Coroas - RS", "Tres de Maio - RS", "Tres Forquilhas - RS", "Tres Fronteiras - SP", "Tres Lagoas - MS", "Tres Marias - MG", "Tres Palmeiras - RS", "Tres Passos - RS", "Tres Pontas - MG", "Tres Ranchos - GO", "Tres Rios - RJ",
			"Treviso - SC", "Treze de Maio - SC", "Treze Tilias - SC", "Trindade - PE", "Trindade - GO", "Trindade do Sul - RS", "Triunfo - PE", "Triunfo - PB", "Triunfo - RS", "Triunfo Potiguar - RN", "Trizidela do Vale - MA", "Trombas - GO", "Trombudo Central - SC", "Tubarao - SC", "Tucano - BA", "Tucuma - PA", "Tucunduva - RS", "Tucurui - PA", "Tufilandia - MA", "Tuiuti - SP", "Tumiritinga - MG", "Tunapolis - SC", "Tunas - RS", "Tunas do Parana - PR", "Tuneiras do Oeste - PR", "Tuntum - MA", "Tupa - SP", "Tupaciguara - MG", "Tupanatinga - PE", "Tupanci do Sul - RS", "Tupancireta - RS", "Tupandi - RS", "Tuparendi - RS", "Tuparetama - PE", "Tupassi - PR", "Tupi Paulista - SP", "Tupirama - TO", "Tupiratins - TO", "Turiacu - MA", "Turilandia - MA", "Turiuba - SP", "Turmalina - MG", "Turmalina - SP", "Turucu - RS", "Tururu - CE", "Turvania - GO", "Turvelandia - GO", "Turvo - PR", "Turvo - SC", "Turvolandia - MG", "Tutoia - MA", "Uarini - AM", "Uaua - BA", "Uba - MG", "Ubai - MG",
			"Ubaira - BA", "Ubaitaba - BA", "Ubajara - CE", "Ubaporanga - MG", "Ubarana - SP", "Ubata - BA", "Ubatuba - SP", "Uberaba - MG", "Uberlandia - MG", "Ubirajara - SP", "Ubirata - PR", "Ubiretama - RS", "Uchoa - SP", "Uibai - BA", "Uiramuta - RR", "Uirapuru - GO", "Uirauna - PB", "Ulianopolis - PA", "Umari - CE", "Umarizal - RN", "Umbauba - SE", "Umburanas - BA", "Umburatiba - MG", "Umbuzeiro - PB", "Umirim - CE", "Umuarama - PR", "Una - BA", "Unai - MG", "Uniao - PI", "Uniao da Serra - RS", "Uniao da Vitoria - PR", "Uniao de Minas - MG", "Uniao do Oeste - SC", "Uniao do Sul - MT", "Uniao dos Palmares - AL", "Uniao Paulista - SP", "Uniflor - PR", "Unistalda - RS", "Upanema - RN", "Urai - PR", "Urandi - BA", "Urania - SP", "Urbano Santos - MA", "Uru - SP", "Uruacu - GO", "Uruana - GO", "Uruana de Minas - MG", "Uruara - PA", "Urubici - SC", "Uruburetama - CE", "Urucania - MG", "Urucara - AM", "Urucuca - BA", "Urucui - PI", "Urucuia - MG", "Urucurituba - AM", "Uruguaiana - RS",
			"Uruoca - CE", "Urupa - RO", "Urupema - SC", "Urupes - SP", "Urussanga - SC", "Urutai - GO", "Utinga - BA", "Vacaria - RS", "Vale de Sao Domingos - MT", "Vale do Anari - RO", "Vale do Paraiso - RO", "Vale do Sol - RS", "Vale Real - RS", "Vale Verde - RS", "Valenca - BA", "Valenca - RJ", "Valenca do Piaui - PI", "Valente - BA", "Valentim Gentil - SP", "Valinhos - SP", "Valparaiso - SP", "Valparaiso de Goias - GO", "Vanini - RS", "Vargeao - SC", "Vargem - SP", "Vargem - SC", "Vargem Alegre - MG", "Vargem Alta - ES", "Vargem Bonita - MG", "Vargem Bonita - SC", "Vargem Grande - MA", "Vargem Grande do Rio Pardo - MG", "Vargem Grande do Sul - SP", "Vargem Grande Paulista - SP", "Varginha - MG", "Varjao - GO", "Varjao de Minas - MG", "Varjota - CE", "Varre-Sai - RJ", "Varzea - PB", "Varzea - RN", "Varzea Alegre - CE", "Varzea Branca - PI", "Varzea da Palma - MG", "Varzea da Roca - BA", "Varzea do Poco - BA", "Varzea Grande - MT", "Varzea Grande - PI", "Varzea Nova - BA",
			"Varzea Paulista - SP", "Varzedo - BA", "Varzelandia - MG", "Vassouras - RJ", "Vazante - MG", "Venancio Aires - RS", "Venda Nova do Imigrante - ES", "Venha-Ver - RN", "Ventania - PR", "Venturosa - PE", "Vera - MT", "Vera Cruz - RS", "Vera Cruz - BA", "Vera Cruz - SP", "Vera Cruz - RN", "Vera Cruz do Oeste - PR", "Vera Mendes - PI", "Veranopolis - RS", "Verdejante - PE", "Verdelandia - MG", "Vere - PR", "Vereda - BA", "Veredinha - MG", "Verissimo - MG", "Vermelho Novo - MG", "Vertente do Lerio - PE", "Vertentes - PE", "Vespasiano - MG", "Vespasiano Correa - RS", "Viadutos - RS", "Viamao - RS", "Viana - ES", "Viana - MA", "Vianopolis - GO", "Vicencia - PE", "Vicente Dutra - RS", "Vicentina - MS", "Vicentinopolis - GO", "Vicosa - AL", "Vicosa - MG", "Vicosa - RN", "Vicosa do Ceara - CE", "Victor Graeff - RS", "Vidal Ramos - SC", "Videira - SC", "Vieiras - MG", "Vieiropolis - PB", "Vigia - PA", "Vila Bela da Santissima Trindade - MT", "Vila Boa - GO", "Vila Flor - RN",
			"Vila Flores - RS", "Vila Langaro - RS", "Vila Maria - RS", "Vila Nova do Piaui - PI", "Vila Nova do Sul - RS", "Vila Nova dos Martirios - MA", "Vila Pavao - ES", "Vila Propicio - GO", "Vila Rica - MT", "Vila Valerio - ES", "Vila Velha - ES", "Vilhena - RO", "Vinhedo - SP", "Viradouro - SP", "Virgem da Lapa - MG", "Virginia - MG", "Virginopolis - MG", "Virgolandia - MG", "Virmond - PR", "Visconde do Rio Branco - MG", "Viseu - PA", "Vista Alegre - RS", "Vista Alegre do Alto - SP", "Vista Alegre do Prata - RS", "Vista Gaucha - RS", "Vista Serrana - PB", "Vitor Meireles - SC", "Vitoria - ES", "Vitoria Brasil - SP", "Vitoria da Conquista - BA", "Vitoria das Missoes - RS", "Vitoria de Santo Antao - PE", "Vitoria do Jari - AP", "Vitoria do Mearim - MA", "Vitoria do Xingu - PA", "Vitorino - PR", "Vitorino Freire - MA", "Volta Grande - MG", "Volta Redonda - RJ", "Votorantim - SP", "Votuporanga - SP", "Wagner - BA", "Wall Ferraz - PI", "Wanderlandia - TO", "Wanderley - BA",
			"Wenceslau Braz - PR", "Wenceslau Braz - MG", "Wenceslau Guimaraes - BA", "Westfalia - RS", "Witmarsum - SC", "Xambioa - TO", "Xambre - PR", "Xangri-la - RS", "Xanxere - SC", "Xapuri - AC", "Xavantina - SC", "Xaxim - SC", "Xexeu - PE", "Xinguara - PA", "Xique-Xique - BA", "Zabele - PB", "Zacarias - SP", "Ze Doca - MA", "Zortea - SC"
	};

}
