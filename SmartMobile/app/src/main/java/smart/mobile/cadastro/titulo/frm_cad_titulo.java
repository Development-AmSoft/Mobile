package smart.mobile.cadastro.titulo;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.R;
import smart.mobile.utils.error.ErroGeralController;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class frm_cad_titulo extends Activity
{

	private DB_LocalHost banco;
	private long TituloID;

	private final int MN_VOLTAR = 0;
	private final int MN_SALVAR = 1;

	@Override
	public void onCreate(Bundle icicle)
	{

		// CARREGA O LAYOUT
		super.onCreate(icicle);
		setContentView(R.layout.lay_cad_titulo);
		setTitle("SmartMobile - Detalhes do Título");

		// carrega dados do banco
		this.banco = new DB_LocalHost(this);
		ErroGeralController erro = new ErroGeralController(this, banco);

		// PARAMETRO DO TITULO
		Bundle b = getIntent().getExtras();
		TituloID = b.getLong("tituloid");

		// CARREGA CIDADES DO XML
		// String[] listCidades =
		// getResources().getStringArray(R.array.cidades_array);
		// ArrayAdapter<String> adp = new ArrayAdapter<String>(this,
		// android.R.layout.simple_dropdown_item_1line, listCidades);
		// AutoCompleteTextView compCidades = (AutoCompleteTextView)
		// findViewById(R.id.edtCidade);
		// compCidades.setAdapter(adp);

		// CARREGA LISTAS DE PREÇOS
		// Spinner cmbListas = (Spinner) findViewById(R.id.cmbListaPreco);
		// ArrayAdapter adp3 = new ArrayAdapter<String>(this,
		// android.R.layout.simple_spinner_item,
		// banco.Sql_Select("LISTAS_PRECOS","DESCRICAO","_id ASC"));
		// adp3.setDropDownViewResource(android.R.layout.simple_spinner_item);
		// cmbListas.setAdapter(adp3);

		// CARREGA FORMAS DE PAGAMENTO
		// Spinner cmbFormasPgto = (Spinner) findViewById(R.id.cmbFormaPgto);
		// ArrayAdapter adp2 = new ArrayAdapter<String>(this,
		// android.R.layout.simple_spinner_item,
		// banco.Sql_Select("FORMAS_PGTO","DESCRICAO","_id ASC"));
		// adp2.setDropDownViewResource(android.R.layout.simple_spinner_item);
		// cmbFormasPgto.setAdapter(adp2);

		// CIDADES NO SPINER
		// Spinner combo = (Spinner) findViewById(R.id.spCidade);
		// ArrayAdapter adp = new ArrayAdapter<String>(this,
		// android.R.layout.simple_spinner_item, listCidades);
		// adp.setDropDownViewResource(android.R.layout.simple_spinner_item);
		// combo.setAdapter(adp);

		// CARREGA OS DADOS DO CLIENTE

		// TIPO DE DOCUMENTO
		Spinner cmbTipos = ((Spinner) findViewById(R.id.cmbTipo));
		ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[]
		{
				"Duplicata", "Cheque"
		});
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cmbTipos.setAdapter(adapter);

		if (TituloID > 0)
		{

			Log.i("TiTLULO CARREGANdo", "");

			Cursor rs = banco.db.rawQuery("select TIPO,NOME,CODIGO,DOCUMENTO,EMISSAO,VENCIMENTO,VALOR,HISTORICO from TITULOS where _id = " + String.valueOf(TituloID), null);
			if (rs.moveToFirst())
			{

				cmbTipos.setSelection(rs.getInt(0));
				((EditText) findViewById(R.id.edtCliente)).setText(rs.getString(1));
				((EditText) findViewById(R.id.edtCodigo)).setText(rs.getString(2));
				((EditText) findViewById(R.id.edtDocumento)).setText(rs.getString(3));
				((EditText) findViewById(R.id.edtEmissao)).setText(rs.getString(4));
				((EditText) findViewById(R.id.edtVencimento)).setText(rs.getString(5));
				((EditText) findViewById(R.id.edtValor)).setText(rs.getString(6));
				((EditText) findViewById(R.id.edtHistorico)).setText(rs.getString(7));

			}

			if (rs != null || !rs.isClosed())
			{
				rs.close();
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemAdd = menu.add(0, MN_VOLTAR, Menu.NONE, "Voltar");
		// MenuItem itemRem = menu.add(0, MN_SALVAR, Menu.NONE, "Salvar");

		// Assign icons
		itemAdd.setIcon(R.drawable.ico_voltar);
		// itemRem.setIcon(R.drawable.ico_salvar);

		// Allocate shortcuts to each of them.
		itemAdd.setShortcut('0', 'v');
		// itemRem.setShortcut('1', 's');
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
			/*
			 * this.bdh = new DB_LocalHost(this); bdh.Clientes_Insert(
			 * ((EditText) findViewById(R.id.edtNome)).getText().toString(),
			 * ((EditText) findViewById(R.id.edtFantasia)).getText().toString(),
			 * ((EditText) findViewById(R.id.edtCPFCNPJ)).getText().toString(),
			 * ((EditText)
			 * findViewById(R.id.edtInscricao)).getText().toString(),
			 * ((EditText) findViewById(R.id.edtEndereco)).getText().toString(),
			 * ((EditText) findViewById(R.id.edtNumero)).getText().toString(),
			 * ((EditText) findViewById(R.id.edtBairro)).getText().toString(),
			 * ((EditText) findViewById(R.id.edtCidade)).getText().toString(),
			 * ((EditText) findViewById(R.id.edtCEP)).getText().toString(),
			 * ((EditText) findViewById(R.id.edtTelefone)).getText().toString(),
			 * ((EditText) findViewById(R.id.edtCelular)).getText().toString(),
			 * ((EditText) findViewById(R.id.edtEmail)).getText().toString(),
			 * ((EditText) findViewById(R.id.edtObs)).getText().toString());
			 */

			Toast.makeText(this, "Cliente salvo com sucesso !!!", Toast.LENGTH_SHORT).show();
			Log.i("SmartMobile", "Cliente salvo com sucesso !!!");

			// refresh = new Intent(this, frmConsClientes.class);
			// startActivity(refresh);

			finish();

			return true;
		}
		return false;
	}

}
