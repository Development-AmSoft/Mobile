package smart.mobile.outras.tela.config;

import smart.mobile.R;
import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.download.DB_Sincroniza;
import smart.mobile.utils.ConfiguracaoInicialTela;
import smart.mobile.utils.error.ErroGeralController;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class frm_sys_config extends Activity
{

	private final int MN_VOLTAR = 0;
	private final int MN_SALVAR = 1;
	private final int MN_DEMO = 2;

	private DB_LocalHost banco;
	private EditText txtServerOnline;
	private EditText txtBanco;
	private Spinner cmbEmpresa;
	private Spinner cmbVendedor;

	@Override
	public void onCreate(Bundle icicle)
	{

		// layout da tela
		super.onCreate(icicle);
		setContentView(R.layout.activity_ctela_base);
		setTitle("SmartMobile - Configurar");

		ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.lay_sys_config, false);

		RelativeLayout footer = (RelativeLayout) findViewById(R.id.FooterEsconder);
		footer.setVisibility(View.GONE);

		Log.i("PASSO1", "ok");

		// banco de dados
		this.banco = new DB_LocalHost(this);
		ErroGeralController erro = new ErroGeralController(this, banco);

		Log.i("PASSO2", "ok");

		// mapeia componentes
		txtServerOnline = (EditText) findViewById(R.id.txtServerOnline);
		txtBanco = (EditText) findViewById(R.id.txtBanco);
		cmbEmpresa = (Spinner) findViewById(R.id.cmbEmpresa);
		cmbVendedor = (Spinner) findViewById(R.id.cmbVendedor);

		Log.i("PASSO3", "ok");

		// carrega componentes
		txtServerOnline.setText(banco.ServidorOnline);
		txtBanco.setText(banco.Banco);

		Log.i("PASSO4", "ok");

		// carrega empresas e vendedores
		LoadEmpresasVendedores();

		Log.i("PASSO5", "ok");

		// eventos dos botoes de empresas/vendedores
		ImageButton btnEmp = (ImageButton) findViewById(R.id.btnConsEmpresa);
		btnEmp.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				ZerarConfigurar();
			}
		});

		ImageButton btnSaveIP = (ImageButton) findViewById(R.id.btnSaveIP);
		btnSaveIP.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					// verifica se ele alterou a 'empresa' ou 'vendedor'
					String codEmp = cmbEmpresa.getSelectedItem().toString().substring(0, cmbEmpresa.getSelectedItem().toString().indexOf("-")).trim();
					String codVend = cmbVendedor.getSelectedItem().toString().substring(0, cmbVendedor.getSelectedItem().toString().indexOf("-")).trim();
					if ((!codEmp.trim().equalsIgnoreCase(banco.EmpresaID)) || (!codVend.trim().equalsIgnoreCase(banco.VendedorID)))
					{
						// caso mudou 'empresa' ou 'vendedor' não permite salvar
						banco.MostraMsg(frm_sys_config.this, "Para alterar Empresa ou Vendedor deve ser utilizada a opção 'Implantar Vendedor' !!!");
					} else
					{

						// solicita confirmação
						AlertDialog.Builder builder = new AlertDialog.Builder(frm_sys_config.this);
						builder.setMessage("Deseja alterar o servidor de '" + banco.ServidorOnline + "' para '" + txtServerOnline.getText().toString() + "' ?").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{
								// salva configuracoes
								salvaConfig();

								// finaliza tela
								finish();

							}
						}).setNegativeButton("Não", new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{
								dialog.cancel();
							}
						})
						// Set your icon here
								.setTitle("SmartMobile - Implantar").setIcon(R.drawable.ico_warning);

						AlertDialog alert = builder.create();
						alert.show();

					}
				} catch (Exception e)
				{

				}
			}

		});

		ImageButton btnVend = (ImageButton) findViewById(R.id.btnConsVendedor);
		btnVend.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				ZerarConfigurar();
			}
		});

		Button btnTestar = (Button) findViewById(R.id.btnTestar);
		btnTestar.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				VersaoTeste();
			}
		});

		Button btnImpl = (Button) findViewById(R.id.btnImplantar);
		btnImpl.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Implantar();
			}
		});

		// caso não tiver nada cadastrado pede se deseja configurar para teste
		if (txtServerOnline.getText().toString().trim().equals(""))
		{
			VersaoTeste();
		}

	}

	public void LoadEmpresasVendedores()
	{

		// empresas
		ArrayAdapter adp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, banco.Sql_Select("EMPRESAS", "EMPRESA", "_id ASC"));
		adp.setDropDownViewResource(android.R.layout.simple_spinner_item);
		cmbEmpresa.setAdapter(adp);
		for (int i = 0; i < adp.getCount(); i++)
		{
			if (((banco.EmpresaID + " - " + banco.NomeEmpresa).trim()).equals(cmbEmpresa.getItemAtPosition(i).toString().trim()))
			{
				cmbEmpresa.setSelection(i);
				break;
			}
		}

		// vendedores
		ArrayAdapter adp2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, banco.Sql_Select("VENDEDORES", "VENDEDOR", "_id ASC"));
		adp2.setDropDownViewResource(android.R.layout.simple_spinner_item);
		cmbVendedor.setAdapter(adp2);
		for (int x = 0; x < adp2.getCount(); x++)
		{
			if ((banco.VendedorID + " - " + banco.NomeVendedor).equals(adp2.getItem(x).toString()))
			{
				cmbVendedor.setSelection(x);
				break;
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemAdd = menu.add(0, MN_VOLTAR, Menu.NONE, "Voltar");
		MenuItem itemRem = menu.add(0, MN_SALVAR, Menu.NONE, "Opções");
		MenuItem itemDemo = menu.add(0, MN_DEMO, Menu.NONE, "Demo");

		// Assign icons
		itemAdd.setIcon(R.drawable.ico_voltar);
		itemRem.setIcon(R.drawable.ico_opcoes);
		itemDemo.setIcon(R.drawable.ico_refresh);

		// Allocate shortcuts to each of them.
		itemAdd.setShortcut('0', 'v');
		itemRem.setShortcut('1', 's');
		itemRem.setShortcut('2', 'd');
		return true;
	}

	public void salvaConfig()
	{

		if (cmbEmpresa.getCount() == 0)
		{
			Toast.makeText(frm_sys_config.this, "Selecione a Empresa !!!", Toast.LENGTH_LONG).show();
		} else if (cmbVendedor.getCount() == 0)
		{
			Toast.makeText(frm_sys_config.this, "Selecione o Vendedor !!!", Toast.LENGTH_LONG).show();
		} else
		{
			// SALVA AS CONFIGURAÇÔES
			String codEmp = cmbEmpresa.getSelectedItem().toString().substring(0, cmbEmpresa.getSelectedItem().toString().indexOf("-")).trim();
			String nomeEmp = cmbEmpresa.getSelectedItem().toString().substring(cmbEmpresa.getSelectedItem().toString().indexOf("-") + 2);

			String codVend = cmbVendedor.getSelectedItem().toString().substring(0, cmbVendedor.getSelectedItem().toString().indexOf("-")).trim();
			String nomeVend = cmbVendedor.getSelectedItem().toString().substring(cmbVendedor.getSelectedItem().toString().indexOf("-") + 2);

			banco.DB_ConfigSave1(txtServerOnline.getText().toString(), txtBanco.getText().toString(), codEmp, nomeEmp, codVend, nomeVend);
		}

	}

	public void Implantar()
	{

		AlertDialog.Builder builder = new AlertDialog.Builder(frm_sys_config.this);
		builder.setMessage("Esta opção irá apagar todos os dados, deseja continuar ?").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{

				// salva configuracoes
				salvaConfig();

				AlertDialog.Builder builder = new AlertDialog.Builder(frm_sys_config.this);
				//builder.setMessage("Todos os dados serão sincronizados, deseja continuar?").setCancelable(true).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
				builder.setMessage("Sincronizar também as 'Imagens dos Produtos' ?").setCancelable(true).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						// sincroniza tudo 'com imagens'
					    DB_Sincroniza dbSync = new DB_Sincroniza(frm_sys_config.this);
						dbSync.Syncroniza_Geral(true,true);

					}
				}).setNegativeButton("Não", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						// sincroniza tudo 'sem imagens'
						DB_Sincroniza dbSync = new DB_Sincroniza(frm_sys_config.this);
						dbSync.Syncroniza_Geral(true,false);
					}
				})
						// Set your icon here
						.setTitle("SmartMobile - Sincronizar").setIcon(R.drawable.ico_warning);

				AlertDialog alert = builder.create();
				alert.show();



			}
		}).setNegativeButton("Não", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
			}
		})
		// Set your icon here
				.setTitle("SmartMobile - Implantar").setIcon(R.drawable.ico_warning);

		AlertDialog alert = builder.create();
		alert.show();

	}

	public void VersaoTeste()
	{

		AlertDialog.Builder builder = new AlertDialog.Builder(frm_sys_config.this);
		builder.setMessage("Configurar aparelho para Versão de Demonstração ?").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{

				// SERVIDOR E BANCO DE DEMONSTRAÇÃO
				// banco.ServidorOnline = "10.0.0.10";
				// banco.Banco = "smart";
				// banco.ServidorOnline = "192.168.1.2:3390";

				banco.ServidorOnline = "demo.amsoft.com.br:3390";
				banco.Banco = "demo";

				txtServerOnline.setText(banco.ServidorOnline);
				txtBanco.setText(banco.Banco);

				// EMPRESA = 0 E VENDEDOR = 0
				banco.DB_ConfigSave1(banco.ServidorOnline, banco.Banco, "0", "Indefinida", "0", "Indefinido");

				// BUSCA EMPRESAS E VENDEDORES
				DB_Sincroniza dbSync = new DB_Sincroniza(frm_sys_config.this);
				dbSync.Syncroniza_EmpresasVendedores();

			}
		}).setNegativeButton("Não", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
			}
		})
		// Set your icon here
				.setTitle("SmartMobile - Versão de Teste").setIcon(R.drawable.ico_warning);

		AlertDialog alert = builder.create();
		alert.show();

	}

	private void ZerarConfigurar()
	{

		AlertDialog.Builder builder = new AlertDialog.Builder(frm_sys_config.this);
		builder.setMessage("Esta opção irá apagar todos os dados, deseja continuar ?").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{

				// ATUALIZA PARA EMPRESA 0 E VENDEDOR 0
				banco.DB_ConfigSave1(txtServerOnline.getText().toString(), txtBanco.getText().toString(), "0", "Indefinida", "0", "Indefinido");

				// BUSCA EMPRESAS E VENDEDORES DO SERVIDOR
				DB_Sincroniza dbSync = new DB_Sincroniza(frm_sys_config.this);
				dbSync.Syncroniza_EmpresasVendedores();

				// DB_ServerHost service = new
				// DB_ServerHost(frm_config.this,"vargasmobile.no-ip.org","vargas");
				// String retorno =
				// service.Sql_Select("select empresaid, razao from vw_mobile_empresas order by empresaid asc");
				// Toast.makeText(frm_config.this,"x:" + retorno
				// ,Toast.LENGTH_LONG).show();

			}
		}).setNegativeButton("Não", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
			}
		})
		// Set your icon here
				.setTitle("SmartMobile - Zerar/Configurar").setIcon(R.drawable.ico_warning);

		AlertDialog alert = builder.create();
		alert.show();

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

			final CharSequence[] items =
			{
					"Salvar", "Implantar Vendedor"
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Opções");
			builder.setItems(items, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item)
				{

					if (item == 0)
					{

						salvaConfig();
						Toast.makeText(frm_sys_config.this, "Configurações salvas com sucesso !!!", Toast.LENGTH_LONG).show();
						finish();

					} else if (item == 1)
					{
						Implantar();
					}
					;

				}
			});
			AlertDialog alert = builder.create();
			alert.show();

			return true;
		case MN_DEMO:
			VersaoTeste();
			return true;

		}
		return false;
	}

}
