package smart.mobile.outras.tela.sobre;

import smart.mobile.PrincipalClasse;
import smart.mobile.R;
import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.utils.ConfiguracaoInicialTela;
import smart.mobile.utils.error.ErroGeralController;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class frm_sys_sobre extends Activity
{

	private DB_LocalHost bdh;
	private DB_LocalHost banco;
	private RelativeLayout r1;
	private PrincipalClasse aplication;
	private Context context;

	private final int MN_VOLTAR = 0;

	@Override
	public void onCreate(Bundle icicle)
	{

		// CARREGA O LAYOUT
		super.onCreate(icicle);
		setContentView(R.layout.activity_ctela_base);
		setTitle("SmartMobile - Sobre o Sistema");

		ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.lay_sys_sobre, false);
		context = this;
		
		banco = new DB_LocalHost(this);
		ErroGeralController erro = new ErroGeralController(this, banco);

		RelativeLayout footer = (RelativeLayout) findViewById(R.id.FooterEsconder);
		footer.setVisibility(View.GONE);

		TextView link = (TextView) findViewById(R.id.linkSobre);
		link.setMovementMethod(LinkMovementMethod.getInstance());

		Cursor cli0 = banco.Sql_Select("VENDAS", new String[]
		{
			"_id"
		}, "SINCRONIZADO = 0", "");

		if (cli0.getCount() == 0)
		{
			link.setText(Html.fromHtml(getResources().getString(R.string.link)));
		} else
		{
			link.setText("aqui");
			link.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage("Não é permitido atualizar o sistema com pedidos pendentes!").setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener()
					{

						public void onClick(DialogInterface arg0, int arg1)
						{
							arg0.dismiss();
						}

					});
					AlertDialog a = builder.create();
					a.show();
					
				}
			});
		}

		TextView www = (TextView) findViewById(R.id.www);
		www.setMovementMethod(LinkMovementMethod.getInstance());
		www.setText(Html.fromHtml(getResources().getString(R.string.www)));

		TextView amsoft = (TextView) findViewById(R.id.nome_link);
		amsoft.setMovementMethod(LinkMovementMethod.getInstance());
		amsoft.setText(Html.fromHtml(getResources().getString(R.string.link_nome)));

		TextView com = (TextView) findViewById(R.id.com);
		com.setMovementMethod(LinkMovementMethod.getInstance());
		com.setText(Html.fromHtml(getResources().getString(R.string.com)));

		// CARREGA A VERSAO
		PackageInfo manager;
		try
		{
			manager = getPackageManager().getPackageInfo(getPackageName(), 0);

			((TextView) findViewById(R.id.txtVersao)).setText(manager.versionName);
			// ((TextView)
			// findViewById(R.id.txtVersao2)).setText("Outubro/2014");
			// ( (TextView)
			// findViewById(R.id.txtVersao2)).setText("Descrição = " +
			// manager.versionName);
			((TextView) findViewById(R.id.txtVersao3)).setText("Android V. " + Build.VERSION.RELEASE);

		} catch (NameNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemAdd = menu.add(0, MN_VOLTAR, Menu.NONE, "Voltar");

		// Assign icons
		itemAdd.setIcon(R.drawable.ico_voltar);

		// Allocate shortcuts to each of them.
		itemAdd.setShortcut('0', 'v');
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

		}
		return false;
	}

}
