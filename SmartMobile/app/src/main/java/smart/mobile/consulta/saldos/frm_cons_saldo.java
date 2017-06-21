package smart.mobile.consulta.saldos;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

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
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class frm_cons_saldo extends Activity
{

	private DB_LocalHost banco;
	SimpleCursorAdapter adapter;
	private final int MN_VOLTAR = 0;
	Cursor c;

	private DecimalFormat myCustDecFormatter = new DecimalFormat("#,##0.00");

	Spinner cmbOperacoes;
	EditText txtFiltro = null;
	TextView lblFiltro = null;

	ImageButton btnSync = null;
	ListView listaCons = null;

	TextView lblStatus = null;
	TextView lblSaldo = null;

	@Override
	public void onCreate(Bundle icicle)
	{

		// CARREGA O LAYOUT
		super.onCreate(icicle);
		setContentView(R.layout.activity_ctela_base);
		setTitle("SmartMobile - Controle do Saldo Flex");

		ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.lay_cons_saldo, false);
		
		RelativeLayout footer = (RelativeLayout) findViewById(R.id.FooterEsconder);
		footer.setVisibility(View.GONE);

		// carrega dados do banco
		this.banco = new DB_LocalHost(this);
		ErroGeralController erro = new ErroGeralController(this, banco);

		// evento do botao atualizar
		ImageButton btnSync = (ImageButton) findViewById(R.id.btnSincroniza);
		btnSync.setVisibility(View.GONE);
		btnSync.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{

				DB_Sincroniza dbSync = new DB_Sincroniza(frm_cons_saldo.this);
				dbSync.Syncroniza_Saldos();

			}
		});

		LoadRelatorio(0);
		// DB_Sincroniza dbSync = new DB_Sincroniza(frm_cons_saldo.this);
		// dbSync.Syncroniza_Saldos();

	}

	public void LoadRelatorio(int Tipo)
	{

		c = banco.db.query("FLEX", new String[]
		{
				"_id", "DATA", "REFERENCIA", "ACRESCIMO", "DESCONTO", "SALDO", "SINCRONIZADO"
		}, null, null, null, null, "_id desc");
		startManagingCursor(c);
		adapter = new frm_cons_saldo_adapter(this, R.layout.lay_cons_saldo_linha, c, new String[]
		{
				"DATA", "REFERENCIA", "ACRESCIMO", "DESCONTO", "SALDO"
		}, new int[]
		{
				R.id.txtData, R.id.txtReferencia, R.id.txtAcrescimo, R.id.txtDesconto, R.id.txtSaldo
		});

		// legenda do saldo
		lblStatus = ((TextView) findViewById(R.id.lblStatus));
		lblSaldo = ((TextView) findViewById(R.id.lblSaldoDisp));

		// atualiza o saldo disponivel atual
		if (c.getCount() > 0)
		{
			c.move(1);
			lblSaldo.setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex("SALDO"))));
			if (c.getDouble(c.getColumnIndex("SALDO")) > 0)
			{
				// lblSaldo.setBackgroundColor(frm_cons_saldo.this.getResources().getColor(R.color.cor_verde));
				 lblSaldo.setTextColor(frm_cons_saldo.this.getResources().getColor(R.color.cor_verde));
			} else
			{
				// lblSaldo.setBackgroundColor(frm_cons_saldo.this.getResources().getColor(R.color.cor_vermelho));
				 lblSaldo.setTextColor(frm_cons_saldo.this.getResources().getColor(R.color.cor_vermelho));
			}

		} else
		{
			lblStatus.setText("Status do Controle: ");
			lblSaldo.setText("Inativo");
			lblSaldo.setBackgroundColor(frm_cons_saldo.this.getResources().getColor(R.color.all_laranja));
			lblSaldo.setTextColor(frm_cons_saldo.this.getResources().getColor(R.color.all_white));
		}

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
					v.setTextColor(getResources().getColor(R.color.cor_verde));
					return text;
				} else
				{
					v.setTextColor(getResources().getColor(R.color.cor_vermelho));
					return text;
				}
			}
		}

		return text;

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

	private void Lista_Cabecalho(ArrayList lista)
	{

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("col1", cmbOperacoes.getSelectedItem().toString());
		map.put("col2", "Total R$");
		map.put("col3", "Acrésc.");
		map.put("col4", "Desc.");
		map.put("col5", "Saldo R$");
		lista.add(map);

	}

	private void Lista_AdicionaLinha(ArrayList lista, String Descricao, Double Total, Double Credito, Double Debito)
	{

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("col1", Descricao);
		map.put("col2", myCustDecFormatter.format(Total));
		map.put("col3", myCustDecFormatter.format(Credito));
		map.put("col4", myCustDecFormatter.format(Debito));
		map.put("col5", myCustDecFormatter.format(Credito - Debito));
		lista.add(map);

	}

}