package smart.mobile.cadastro.pedido;

import java.text.DecimalFormat;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.R;

public class frm_cad_pedido_itens_historico_adapter extends SimpleCursorAdapter
{

	private Cursor c;
	private Context context;
	private DB_LocalHost banco;
	private DecimalFormat myCustDecFormatter = new DecimalFormat("#,##0.00");

	public frm_cad_pedido_itens_historico_adapter(Context context, int layout, Cursor c, DB_LocalHost bancoX, String[] from, int[] to)
	{
		super(context, layout, c, from, to);
		this.c = c;
		this.context = context;
		this.banco = bancoX;
	}

	public View getView(int pos, View inView, ViewGroup parent)
	{
		View v = inView;

		try
		{

			if (v == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.lay_cad_pedido_itens_final, null);
			}

			this.c.moveToPosition(pos);

			TextView txtDescricao = (TextView) v.findViewById(R.id.txtDescricao);
			txtDescricao.setText(c.getString(c.getColumnIndex("DESCRICAO")));

			// unidade
			txtDescricao.setText(c.getString(c.getColumnIndex("UND")) + " - " + txtDescricao.getText().toString());

			// caso exitir dados de grade
			if ((!c.getString(c.getColumnIndex("LINHA")).trim().equals("")) || (!c.getString(c.getColumnIndex("COLUNA")).trim().equals("")))
			{
				txtDescricao.setText(txtDescricao.getText().toString() + "\n" + c.getString(c.getColumnIndex("LINHA")) + " : " + c.getString(c.getColumnIndex("COLUNA")));
			}

			if (c.getDouble(c.getColumnIndex("vendaid")) > 0)
			{
				txtDescricao.setText(txtDescricao.getText().toString() + " [PED. Nº " + c.getString(c.getColumnIndex("vendaid")) + "]");
			}

			TextView txtCodigo = (TextView) v.findViewById(R.id.txtCodigo);
			txtCodigo.setText(c.getString(c.getColumnIndex("PRODUTOID")));

			TextView txtQtde = (TextView) v.findViewById(R.id.txtQtde);
			txtQtde.setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex("QTDE"))));

			TextView txtValor = (TextView) v.findViewById(R.id.txtValor);
			txtValor.setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex("VALOR"))));

			TextView txtAcres = (TextView) v.findViewById(R.id.txtAcrescimo);
			txtAcres.setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex("ACRESCIMO"))));

			TextView txtDesc = (TextView) v.findViewById(R.id.txtDesconto);
			txtDesc.setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex("DESCONTO"))));

			TextView txtValorFinal = (TextView) v.findViewById(R.id.txtValorFinal);
			txtValorFinal.setText(myCustDecFormatter.format(((c.getDouble(c.getColumnIndex("QTDE")) * c.getDouble(c.getColumnIndex("VALOR"))) + c.getDouble(c.getColumnIndex("ACRESCIMO")) - c.getDouble(c.getColumnIndex("DESCONTO"))) / c.getDouble(c.getColumnIndex("QTDE"))));

			TextView txtTotal = (TextView) v.findViewById(R.id.txtTotal);
			txtTotal.setText(myCustDecFormatter.format((c.getDouble(c.getColumnIndex("QTDE")) * c.getDouble(c.getColumnIndex("VALOR"))) + c.getDouble(c.getColumnIndex("ACRESCIMO")) - c.getDouble(c.getColumnIndex("DESCONTO"))));

			try
			{
				TextView txtST = (TextView) v.findViewById(R.id.txtST);
				txtST.setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex("VALOR_ST")) - ((c.getDouble(c.getColumnIndex("QTDE")) * c.getDouble(c.getColumnIndex("VALOR"))) + c.getDouble(c.getColumnIndex("ACRESCIMO")) - c.getDouble(c.getColumnIndex("DESCONTO")))));

				TextView txtValorST = (TextView) v.findViewById(R.id.txtValorST);
				txtValorST.setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex("VALOR_ST"))));
			} catch (Exception e)
			{

			}

			ImageView imgCheck = (ImageView) v.findViewById(R.id.imgCheck);
			if (c.getLong(c.getColumnIndex("QTDE_PED")) == -1)
			{
				imgCheck.setVisibility(View.GONE);
			}
			// else if(c.getLong(c.getColumnIndex("QTDE_PED"))>0){
			else if (banco.TempPed_ExisteProdutosID(c.getString(c.getColumnIndex("PRODUTOID")), c.getString(c.getColumnIndex("LINHAID")), c.getString(c.getColumnIndex("COLUNAID"))))
			{
				// imgCheck.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_check1));
				imgCheck.setVisibility(View.GONE);
				txtDescricao.setBackgroundColor(context.getResources().getColor(R.color.all_laranja));
				txtDescricao.setTextColor(context.getResources().getColor(R.color.all_white));
			} else
			{
				// imgCheck.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_check0));
				imgCheck.setVisibility(View.GONE);
				txtDescricao.setBackgroundColor(context.getResources().getColor(R.color.all_white));
				txtDescricao.setTextColor(context.getResources().getColor(R.color.all_black));
			}
		} catch (Exception e)
		{
		}
		return (v);
	}
}