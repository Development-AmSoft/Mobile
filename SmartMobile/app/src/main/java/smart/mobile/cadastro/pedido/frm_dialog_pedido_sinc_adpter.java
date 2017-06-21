package smart.mobile.cadastro.pedido;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import smart.mobile.R;
import smart.mobile.outras.sincronismo.DB_LocalHost;

public class frm_dialog_pedido_sinc_adpter extends SimpleCursorAdapter
{

	private int[] ids;
	private String[] valores;
	private Cursor c;
	private int layout;
	private DB_LocalHost banco;
	private int saldo;
	private Context context;

	public frm_dialog_pedido_sinc_adpter(Context context, int layout, Cursor c, String[] from, int[] to, DB_LocalHost banco, int i)
	{
		super(context, layout, c, from, to);

		this.ids = to;
		this.c = c;
		this.valores = from;
		this.layout = layout;
		this.banco = banco;
		this.saldo = i;
		this.context = context;

	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2)
	{
		View v = super.getView(arg0, arg1, arg2);
		this.c.moveToPosition(arg0);

		CheckBox check = (CheckBox) v.findViewById(R.id.checkSinc);

		final String numeroPedido = c.getString(c.getColumnIndex(valores[0]));
		final String nomeCliente = c.getString(c.getColumnIndex(valores[1]));
		final String valorTotal = c.getString(c.getColumnIndex(valores[2]));

		StringBuilder montar = new StringBuilder();
		montar.append("Pedido nº ");
		montar.append(numeroPedido);
		montar.append(" - ");
		montar.append(nomeCliente);
		montar.append("\nValor Total: R$ ");
		montar.append(valorTotal);

		check.setText(montar.toString());

		if (c.getInt(c.getColumnIndex(valores[3])) == 0)
		{
			check.setChecked(false);
		} else
		{
			check.setChecked(true);
		}

		check.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (saldo <= 0)
				{
					SQLiteStatement stm = banco.db.compileStatement("update vendas set SINCRONIZAR = ? where _id = " + numeroPedido);
					if (isChecked)
					{
						stm.bindLong(1, 1);
					} else
					{
						stm.bindLong(1, 0);
					}
					stm.executeInsert();
					stm.close();
				} else {
					buttonView.setChecked(true);
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage("Não é permitido enviar pedidos parciais pois a funcionalidade Saldo Flex está ativada.").setCancelable(true).setPositiveButton("Ok", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							dialog.dismiss();
						}
					}).setTitle("SaldoFlex").setIcon(R.drawable.ico_warning);

					AlertDialog alert = builder.create();
					alert.show();
				}

			}
		});

		return v;
	}
}
