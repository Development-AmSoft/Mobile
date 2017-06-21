package smart.mobile.consulta.cliente;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.provider.Browser;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import smart.mobile.R;

public class frm_cons_clientes_adapter extends SimpleCursorAdapter
{

	private int TipoLayout = 0;// 0 -- simples 1 -- completo
	private Cursor c;
	private Context context;
	private static DecimalFormat myCustDecFormatter = new DecimalFormat("#,##0.00");

	public frm_cons_clientes_adapter(Context context, int layout, Cursor c, String[] from, int[] to, int xTipoLayout)
	{
		super(context, layout, c, from, to);

		this.TipoLayout = xTipoLayout;
		this.c = c;
		this.context = context;
	}

	public View getView(int pos, View inView, ViewGroup parent)
	{
		View v = inView;

		if (v == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (TipoLayout == 0)
			{
				v = inflater.inflate(R.layout.lay_cons_clientes_simples, null);
			} else
			{
				v = inflater.inflate(R.layout.lay_cons_clientes, null);
			}
		}

		this.c.moveToPosition(pos);
		// String bookmark =
		// this.c.getString(this.c.getColumnIndex(Browser.BookmarkColumns.TITLE));
		// byte[] favicon =
		// this.c.getBlob(this.c.getColumnIndex(Browser.BookmarkColumns.FAVICON));
		// if (favicon != null) {

		ImageView iv = (ImageView) v.findViewById(R.id.imgSituacao);
		TextView txtSituacao = (TextView) v.findViewById(R.id.txtStatus);
		if (c.getLong(c.getColumnIndex("SINCRONIZADO")) == 2)
		{
			iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_sync1));
		} else if (c.getLong(c.getColumnIndex("SINCRONIZADO")) == 1 || c.getLong(c.getColumnIndex("SINCRONIZADO")) == 3)
		{
			iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_sync1));
		} else
		{
			iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_sync01));
		}

		TextView txtNome = (TextView) v.findViewById(R.id.txtNome);
		txtNome.setText(c.getString(c.getColumnIndex("NOME")));

		if (TipoLayout == 1)
		{ // completo

			if (c.getLong(c.getColumnIndex("SINCRONIZADO")) == 2)
			{
				// iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_sync1));

				txtNome.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
				txtSituacao.setText("Inativo");
				txtSituacao.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
				// txtSituacao.setBackgroundColor(context.getResources().getColor(R.color.all_cinza));
			} else if (c.getLong(c.getColumnIndex("SINCRONIZADO")) == 1)
			{
				// iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_sync1));
				txtNome.setTextColor(context.getResources().getColor(R.color.all_black));
				txtSituacao.setText("Enviado");
				txtSituacao.setTextColor(context.getResources().getColor(R.color.cor_verde));
				// txtSituacao.setBackgroundColor(context.getResources().getColor(R.color.solid_green));
			}else if (c.getLong(c.getColumnIndex("SINCRONIZADO")) == 3)
			{
				// iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_sync1));
				txtNome.setTextColor(context.getResources().getColor(R.color.all_black));
				txtSituacao.setText("Enviado - Alterado");
				txtSituacao.setTextColor(context.getResources().getColor(R.color.cor_verde));
				// txtSituacao.setBackgroundColor(context.getResources().getColor(R.color.solid_green));
			} else
			{
				// iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_sync0));
				txtNome.setTextColor(context.getResources().getColor(R.color.all_black));
				txtSituacao.setText("Pendente"); // txtSituacao.setTextColor(context.getResources().getColor(R.color.solid_red));
				txtSituacao.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
				// txtSituacao.setBackgroundColor(context.getResources().getColor(R.color.solid_red));
			}

			// iv.setImageBitmap(BitmapFactory.decodeByteArray(favicon, 0,
			// favicon.length));

			TextView txtFantasia = (TextView) v.findViewById(R.id.txtFantasia);
			txtFantasia.setText(c.getString(c.getColumnIndex("FANTASIA")));

			TextView txtCPFCNPJ = (TextView) v.findViewById(R.id.txtCPFCNPJ);
			txtCPFCNPJ.setText(c.getString(c.getColumnIndex("CPF_CNPJ")));

			TextView txtCidade = (TextView) v.findViewById(R.id.txtCidade);
			txtCidade.setText(c.getString(c.getColumnIndex("CIDADE")));

			TextView txtUltData = (TextView) v.findViewById(R.id.txtUltData);

			try
			{
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date troca = format.parse(c.getString(c.getColumnIndex("ULT_DATA")));
				SimpleDateFormat formatFinal = new SimpleDateFormat("dd/MM/yyyy");
				txtUltData.setText(formatFinal.format(troca));
			} catch (Exception e)
			{
				txtUltData.setText(c.getString(c.getColumnIndex("ULT_DATA")));
			}

			TextView txtUltTotal = (TextView) v.findViewById(R.id.txtUltTotal);
			txtUltTotal.setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex("ULT_TOTAL"))));
			if (txtUltData.getText().toString().equalsIgnoreCase(""))
			{
				txtUltTotal.setText("");
			}

		}

		// }
		// TextView bTitle = (TextView) v.findViewById(R.id.btitle);
		// bTitle.setText(bookmark);*/
		return (v);
	}

}