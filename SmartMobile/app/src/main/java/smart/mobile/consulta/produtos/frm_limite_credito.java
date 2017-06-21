package smart.mobile.consulta.produtos;

import java.text.DecimalFormat;

import smart.mobile.R;
import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.utils.error.ErroGeralController;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

public class frm_limite_credito extends Activity
{

	private DB_LocalHost banco;

	private TextView limiteCredito;
	private TextView saldoCredito;
	private TextView valorVenda;
	private TextView saldoDisponivel;
	private long pedidoId;
	
	private DecimalFormat myCustDecFormatter = new DecimalFormat("#,##0.00");

	@Override
	public void onCreate(Bundle icicle)
	{

		super.onCreate(icicle);
		setContentView(R.layout.lay_limite_credito);

		banco = new DB_LocalHost(this);
		ErroGeralController erro = new ErroGeralController(this, banco);

		Bundle b = getIntent().getExtras();
		pedidoId = b.getLong("pedidoid");

		limiteCredito = (TextView) findViewById(R.id.textLimi);
		saldoCredito = (TextView) findViewById(R.id.tSaldoCred);
		valorVenda = (TextView) findViewById(R.id.tValVend);
		saldoDisponivel = (TextView) findViewById(R.id.tSalDisp);

	}
	
	@Override
	protected void onResume()
	{
		
		super.onResume();
		atualizarInformacoes();
	}
	
	private void atualizarInformacoes(){
		Cursor cPesquisaInteira = banco.db.rawQuery("select sum(titulos.valor) as total, " + "(select total from vendas where _id = " + pedidoId + ") as total_item, " + "(select clientes.limite from vendas join clientes on vendas.CPF_CNPJ = clientes.CPF_CNPJ where vendas._id = " + pedidoId + ") as limite_cliente " + "from titulos where nome like (select clientes.CPF_CNPJ " + "from vendas join clientes on vendas.CPF_CNPJ = clientes.CPF_CNPJ " + "where vendas._id = " + pedidoId + ")", null);

		if (cPesquisaInteira.moveToFirst())
		{
			double valorPendencias = cPesquisaInteira.getDouble(0);
			double totalDaVenda = cPesquisaInteira.getDouble(1);
			double limite = cPesquisaInteira.getDouble(2);

			double umLimiteCredito = limite;
			double doisSaldoCredito = limite - valorPendencias;
			double tresValorVenda = totalDaVenda;
			double quatroSaldoDisponivel = doisSaldoCredito - tresValorVenda;

			limiteCredito.setText("Limite de Crédito - R$ " + myCustDecFormatter.format(umLimiteCredito));
			saldoCredito.setText("R$ " + myCustDecFormatter.format(doisSaldoCredito));
			valorVenda.setText("R$ " + myCustDecFormatter.format(tresValorVenda));
			saldoDisponivel.setText("R$ " + myCustDecFormatter.format(quatroSaldoDisponivel));
			
			if(quatroSaldoDisponivel > 0){
				saldoDisponivel.setTextColor(Color.GREEN);
			} else {
				saldoDisponivel.setTextColor(Color.RED);
			}

		}
	}
}
