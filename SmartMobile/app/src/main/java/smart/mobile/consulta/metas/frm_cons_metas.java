package smart.mobile.consulta.metas;

import java.text.DecimalFormat;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.download.DB_Sincroniza;
import smart.mobile.R;
import smart.mobile.utils.ConfiguracaoInicialTela;
import smart.mobile.utils.error.ErroGeralController;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class frm_cons_metas extends Activity
{

	private DB_LocalHost banco;
	SimpleCursorAdapter adapter;
	private final int MN_VOLTAR = 0;
	private DecimalFormat myCustDecFormatter = new DecimalFormat("#,##0.00");
	Cursor c;

	@Override
	public void onCreate(Bundle icicle)
	{

		// CARREGA O LAYOUT
		super.onCreate(icicle);
		setContentView(R.layout.activity_ctela_base);
		setTitle("SmartMobile - Relatório de Metas [Online]");

		ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.lay_cons_metas, false);

		RelativeLayout footer = (RelativeLayout) findViewById(R.id.FooterEsconder);
		footer.setVisibility(View.GONE);

		// carrega dados do banco
		this.banco = new DB_LocalHost(this);
		
		ErroGeralController erro = new ErroGeralController(this, banco);

		// evento do botao atualizar
		// Button btnSync =(Button)findViewById(R.id.btnSincroniza);
		// btnSync.setVisibility(View.GONE);
		// btnSync.setOnClickListener(new OnClickListener()
		// {
		// public void onClick(View v)
		// {
		//
		// DB_Sincroniza dbSync = new DB_Sincroniza(frm_cons_metas.this);
		// dbSync.Syncroniza_Metas();
		//
		// }
		// });

		DB_Sincroniza dbSync = new DB_Sincroniza(frm_cons_metas.this);
		dbSync.Syncroniza_Metas();

		// LoadRelatorio(0);

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
			break;
		default:
			break;
		}
		return false;
	}

	public void LoadRelatorio(int Tipo)
	{

		c = banco.db.query("METAS", new String[]
		{
				"_id", "MES", "META", "TOTAL"
		}, null, null, null, null, null);
		startManagingCursor(c);
		adapter = new frm_cons_metas_adapter(this, R.layout.lay_cons_metas_linha, c, new String[]
		{
				"MES", "META", "TOTAL"
		}, new int[]
		{
				R.id.txtMes, R.id.txtMeta, R.id.txtTotal
		}, this);

		
		// calculando total!
		double valorTotal = 0;
		while (c.moveToNext())
		{
			valorTotal += c.getDouble(c.getColumnIndex("TOTAL"));
		}

		TextView total = (TextView) findViewById(R.id.lblStatusValor);

		total.setText(myCustDecFormatter.format(valorTotal));

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
		ListView listaCons = (ListView) findViewById(R.id.SCHEDULE);
		listaCons.setAdapter(lsadapter);
		listaCons.setFastScrollEnabled(true);

	}

	private String convText(TextView v, String text)
	{

		if (v.getId() == R.id.txtSaldo)
		{
			if ((!(text == "Saldo R$")) && (!text.equalsIgnoreCase("0,00")))
			{
				if (text.indexOf("-") > -1)
				{
					v.setTextColor(getResources().getColor(R.color.cor_vermelho));
					return text;
				} else
				{
					v.setTextColor(getResources().getColor(R.color.cor_verde));
					return text;
				}
			}
		}

		return text;

	}

}