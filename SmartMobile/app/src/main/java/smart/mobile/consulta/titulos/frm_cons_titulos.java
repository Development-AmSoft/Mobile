package smart.mobile.consulta.titulos;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.download.DB_Sincroniza;
import smart.mobile.R;
import smart.mobile.cadastro.titulo.frm_cad_titulo;
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
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class frm_cons_titulos extends Activity
{

	// variavel principal de acesso ao banco
	private DB_LocalHost banco;

	// definição de colunas da consulta
	int col_order = 0;
	boolean col_desc = true;
	final CharSequence[] col_banco =
	{
			"titulos._id", "titulos.nome", "titulos.documento", "titulos.valor"
	};
	final CharSequence[] col_descricao =
	{
			"Vencimento", "Nome", "Documento", "Valor R$"
	};

	private EditText filterText = null;
	SimpleCursorAdapter adapter;
	TextView lblFiltro = null;
	EditText txtFiltro = null;

	private final int MN_VOLTAR = 0;
	private final int MN_OPCOES = 1;
	private final int MN_FILTRAR = 2;
	private final int MN_ORDENAR = 3;

	long ClienteID = 0;
	String ClienteSel = "";
	Cursor c;

	// BLOCO 1 - EVENTOS DA ACTIVITY

	public void onCreate(Bundle icicle)
	{

		super.onCreate(icicle);
		setContentView(R.layout.activity_ctela_base);
		setTitle("SmartMobile - Consulta de Pendências");
		
		ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.layoutpadrao_consgeral, false);
		
		RelativeLayout footer = (RelativeLayout) findViewById(R.id.FooterEsconder);
		footer.setVisibility(View.GONE);

		// carrega dados do banco
		this.banco = new DB_LocalHost(this);
		
		ErroGeralController erro = new ErroGeralController(this, banco);

		// configura componente de filtro
		// filterText = (EditText) findViewById(R.id.edtFiltro);
		// filterText.addTextChangedListener(filterTextWatcher);
		lblFiltro = (TextView) findViewById(R.id.lblStatus);

		ImageButton btnTipoLista = (ImageButton) findViewById(R.id.btnTipoLista);
		btnTipoLista.setVisibility(View.GONE);
		btnTipoLista.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{

				AlertDialog.Builder builder = new AlertDialog.Builder(frm_cons_titulos.this);
				builder.setTitle("Tipo de Lista");
				final CharSequence[] choiceList =

				{
						"Lista Simples", "Lista Completa"
				};

				builder.setSingleChoiceItems(choiceList, banco.cons_prod_indexTIPO,

				new DialogInterface.OnClickListener()
				{

					public void onClick(

					DialogInterface dialog,

					int which)
					{

						banco.cons_prod_indexTIPO = which;
						dialog.cancel();
						// CarregaDados(false,0);

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

		ImageButton btnCad = (ImageButton) findViewById(R.id.btnCadastro);
		btnCad.setVisibility(View.GONE); // View.INVISIBLE);

		ImageButton btnSync = (ImageButton) findViewById(R.id.btnSincroniza);
		btnSync.setVisibility(View.GONE);
		btnSync.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Titulo_Sincroniza();
			}
		});

		txtFiltro = (EditText) findViewById(R.id.edtFiltro);
		txtFiltro.setHint("Razão,Docto,Vencimento");

		if (getIntent().getExtras() != null)
		{
			txtFiltro.setText(getIntent().getExtras().getString("filtro"));
		}

		txtFiltro.addTextChangedListener(new TextWatcher()
		{

			public void onTextChanged(CharSequence s, int start, int before, int count)
			{

			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{

			}

			public void afterTextChanged(Editable s)
			{
				CarregaDados(true);

			}
		});

	}

	@Override
	public void onResume()
	{
		super.onResume();

		// Log.v("Szakbarbar", "Need the cursor from the Service");
		CarregaDados(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
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

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
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

			// final CharSequence[] items = {"High", "Normal", "Low"};

			AlertDialog.Builder builder = new AlertDialog.Builder(frm_cons_titulos.this);
			builder.setTitle("Ordenar por").setSingleChoiceItems(col_descricao, col_order, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item)
				{

					Toast.makeText(getApplicationContext(), "Ordenar por : " + col_descricao[item], Toast.LENGTH_SHORT).show();
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
	public void finish()
	{
		super.finish();
		return;

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

	}

	// BLOCO 2 - EVENTOS DE CONSULTAS

	private String convText(TextView v, String text)
	{

		if (v.getId() == R.id.txtValor)
		{
			if (c.getDouble(c.getColumnIndex("VALOR")) <= 0)
			{
				v.setTextColor(getResources().getColor(R.color.cor_vermelho));
			}
			return banco.myCustDecFormatter.format(c.getDouble(c.getColumnIndex("VALOR")));
		} else if (v.getId() == R.id.txtVencimento)
		{

			// if(isVencido(c.getString(c.getColumnIndex("VENCIMENTO")))){
			// v.setTextColor(getResources().getColor(R.color.solid_red));};
			String Venc = c.getString(c.getColumnIndex("VENCIMENTO")).trim();

			if (Venc.equalsIgnoreCase("21/06/2012"))
			{
				v.setTextColor(getResources().getColor(R.color.cor_vermelho));
			}
			;

			// else{
			// v.setTextColor(getResources().getColor(R.color.solid_green));}

			return Venc;

			// if (c.getDouble(c.getColumnIndex("ESTOQUE")) <=
			// 0){v.setTextColor(getResources().getColor(R.color.solid_red));}
			// return
			// myCustDecFormatter.format(c.getDouble(c.getColumnIndex("ESTOQUE")));
		}

		return text;
	}

	private boolean isVencido(String DataVencimento)
	{

		boolean retorno = false;

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
		String DataHoje = sdf.format(new Date());

		// converte para numero inteiro as duas datas
		DataVencimento = DataVencimento.substring(6, 10) + DataVencimento.substring(3, 5) + DataVencimento.substring(0, 2);
		DataHoje = DataHoje.substring(6, 10) + DataHoje.substring(3, 5) + DataHoje.substring(0, 2);

		if (Long.valueOf(DataVencimento) <= Long.valueOf(DataHoje))
		{
			retorno = true;
		}

		return retorno;

	}

	public void CarregaDados(boolean Ordernando)
	{

		// DEFININDO CAMPO DE ORDENAÇÂO -- COL 0 PADRAO
		String campoOrder = (String) col_banco[0] + " asc";

		// CASO ESTIVER ORDENANDO NO MENU
		if (Ordernando)
		{
			campoOrder = (String) col_banco[col_order];
			if (col_desc)
			{
				campoOrder = campoOrder + " desc";
				col_desc = false;
			} else
			{
				campoOrder = campoOrder + " asc";
				col_desc = true;
			}
		}
		;

		// DEFININDO VALOR DO FILTRO
		String filtro = txtFiltro.getText().toString();

		// DEFININDO FILTRO DE DOCUMENTOS
		String strDocs = "";
		if (banco.cons_titulos_indexFiltro == 0)
		{
			strDocs = "(TIPO = 1)";
		} else if (banco.cons_titulos_indexFiltro == 1)
		{
			strDocs = "(TIPO = 0)";
		}

		// DEFININDO CONSULTA
		if (!filtro.equals(""))
		{
			if (strDocs.equals(""))
			{
				filtro = " WHERE (TITULOS.NOME LIKE " + "'%" + filtro + "%') OR (CLIENTES.NOME LIKE " + "'%" + filtro + "%') OR (TITULOS.DOCUMENTO LIKE " + "'%" + filtro + "%') OR (TITULOS.VENCIMENTO LIKE " + "'%" + filtro + "%')";
			} else
			{
				filtro = " WHERE " + strDocs + " AND ((TITULOS.NOME LIKE " + "'%" + filtro + "%') OR (CLIENTES.NOME LIKE " + "'%" + filtro + "%') OR (TITULOS.DOCUMENTO LIKE " + "'%" + filtro + "%') OR (TITULOS.VENCIMENTO LIKE " + "'%" + filtro + "%'))";
			}
		} else
		{
			if (!strDocs.equals(""))
			{
				filtro = " WHERE " + strDocs;
			}
		}

		c = banco.db.rawQuery("SELECT TITULOS._id,CLIENTES.NOME,TITULOS.NOME AS CPFCNPJ,TITULOS.VENCIMENTO,TITULOS.DOCUMENTO,TITULOS.VALOR,TITULOS.TIPO FROM TITULOS " + "JOIN CLIENTES ON TITULOS.NOME = CLIENTES.CPF_CNPJ " + filtro + " ORDER BY " + campoOrder, null);
		// {c = banco.db.query("TITULOS", new String[]
		// {"_id","NOME","VENCIMENTO", "DOCUMENTO", "VALOR","TIPO"}, null, null,
		// null, null,campoOrder);}
		// else
		// {c = banco.db.query("TITULOS", new String[]
		// {"_id","NOME","VENCIMENTO", "DOCUMENTO", "VALOR","TIPO"},
		// "(NOME LIKE " + "'%" + filtro + "%') OR (DOCUMENTO LIKE " + "'%" +
		// filtro + "%') OR (VENCIMENTO LIKE " + "'%" + filtro + "%')", null,
		// null, null,campoOrder);}

		// INICIANDO CURSOR
		startManagingCursor(c);
		adapter = new frm_cons_titulos_adapter(this, R.layout.lay_cons_titulos, c, new String[]
		{
				"NOME", "CPFCNPJ", "VENCIMENTO", "VALOR"
		}, new int[]
		{
				R.id.txtNome, R.id.txtCPFCNPJ, R.id.txtVencimento, R.id.txtValor
		});

		/*
		 * adapter = new SimpleCursorAdapter(this, R.layout.lay_cons_titulos, c,
		 * new String[] { "NOME", "VENCIMENTO", "VALOR"}, new int[] {
		 * R.id.txtNome, R.id.txtVencimento, R.id.txtValor }) {
		 * 
		 * @Override public void setViewText(TextView v, String text) {
		 * super.setViewText(v, convText(v, text)); }
		 * 
		 * };
		 */

		// ListAdapter adapter = new SimpleCursorAdapter(this,
		// R.layout.layoutpadrao_consitens, c,
		// new String[] { "NOME", "FANTASIA" },
		// new int[] { R.id.text1, R.id.text2 });
		ListAdapter lsadapter = adapter;
		ListView listaCons = (ListView) findViewById(R.id.vendaListView);
		listaCons.setAdapter(lsadapter);
		listaCons.setFastScrollEnabled(true);

		Double totven = 0.00;
		Double totgeral = 0.00;
		if (c.moveToFirst())
		{
			do
			{
				totgeral = totgeral + c.getDouble(c.getColumnIndex("VALOR"));
				if (isVencido(c.getString(c.getColumnIndex("VENCIMENTO"))))
				{
					totven = totven + c.getDouble(c.getColumnIndex("VALOR"));
				}

			} while (c.moveToNext());
		}

		lblFiltro.setText("Registros: " + lsadapter.getCount() + "  Vencidos: " + banco.myCustDecFormatter.format(totven) + "\n  Total Geral: " + banco.myCustDecFormatter.format(totgeral));

		listaCons.setOnItemClickListener(new OnItemClickListener()
		{

			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				// @todo

				ClienteID = id;
				op_Opcoes();

				// Toast.makeText(frmConsClientes.this,"O id do item selecionado é: "+
				// String.valueOf(id), Toast.LENGTH_LONG).show();
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

	private void Titulo_Sincroniza()
	{
		DB_Sincroniza dbSync = new DB_Sincroniza(frm_cons_titulos.this);
		dbSync.Syncroniza_Titulos();
	}

	private void op_Opcoes()
	{

		final CharSequence[] items =
		{
				"Visualizar", "Sincronizar"
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pendências - Opções");
		builder.setItems(items, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int item)
			{

				// Toast.makeText(getApplicationContext(), items[item],
				// Toast.LENGTH_SHORT).show();

				if (item == 0)
				{

					Intent intent = new Intent(frm_cons_titulos.this, frm_cad_titulo.class);

					Bundle b = new Bundle();
					b.putLong("tituloid", ClienteID);
					intent.putExtras(b);

					startActivity(intent);
				} else if (item == 1)
				{
					Titulo_Sincroniza();

				}

			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	private void op_Filtrar()
	{

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pendências - Filtrar");
		final CharSequence[] choiceList =

		{
				"Cheques", "Duplicatas/Boletos", "Todos os Documentos"
		};

		builder.setSingleChoiceItems(choiceList, banco.cons_titulos_indexFiltro,

		new DialogInterface.OnClickListener()
		{

			public void onClick(

			DialogInterface dialog,

			int which)
			{

				banco.cons_titulos_indexFiltro = which;
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

}