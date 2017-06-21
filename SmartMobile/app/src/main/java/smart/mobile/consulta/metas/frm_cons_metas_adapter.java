package smart.mobile.consulta.metas;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.provider.Browser;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import smart.mobile.R;

public class frm_cons_metas_adapter extends SimpleCursorAdapter
{

	private Cursor c;
	private Context context;
	private DecimalFormat myCustDecFormatter = new DecimalFormat("#,##0.00");
	private Activity activity;

	public frm_cons_metas_adapter(Context context, int layout, Cursor c, String[] from, int[] to, Activity activity)
	{
		super(context, layout, c, from, to);
		this.c = c;
		this.context = context;
		this.activity = activity;
	}

	public View getView(int pos, View inView, ViewGroup parent)
	{
		View v = inView;

		if (v == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.lay_cons_metas_linha, null);
		}

		this.c.moveToPosition(pos);
		// String bookmark =
		// this.c.getString(this.c.getColumnIndex(Browser.BookmarkColumns.TITLE));
		// byte[] favicon =
		// this.c.getBlob(this.c.getColumnIndex(Browser.BookmarkColumns.FAVICON));
		// if (favicon != null) {

		TextView txtNome = (TextView) v.findViewById(R.id.txtMes);
		txtNome.setText(c.getString(c.getColumnIndex("MES")).replace("12", "Dez").replace("11", "Nov").replace("10", "Out").replace("9", "Set").replace("8", "Ago").replace("7", "Jul").replace("6", "Jun").replace("5", "Mai").replace("4", "Abr").replace("3", "Mar").replace("2", "Fev").replace("1", "Jan"));

		// ImageView iv = (ImageView) v.findViewById(R.id.x_situacao);
		// TextView txtTipo = (TextView) v.findViewById(R.id.txtTipo);

		// if(c.getLong(c.getColumnIndex("TIPO"))==0){
		// iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_duplicata));
		// txtTipo.setText("Duplicata");}
		// txtSituacao.setText("Sincronizado");
		// txtSituacao.setTextColor(context.getResources().getColor(R.color.solid_green));}
		// else{
		// txtTipo.setText("Cheque");
		// iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_cheque));
		// txtSituacao.setText("Pendente");
		// txtSituacao.setTextColor(context.getResources().getColor(R.color.all_cinza));
		// }

		TextView txtVencimento = (TextView) v.findViewById(R.id.txtMeta);
		txtVencimento.setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex("META"))));

		TextView txtDocumento = (TextView) v.findViewById(R.id.txtTotal);
		txtDocumento.setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex("TOTAL"))));

		/*
		 * TextView txtValor = (TextView) v.findViewById(R.id.txtValor);
		 * txtValor
		 * .setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex
		 * ("VALOR")))); /*
		 */

		if ((c.getDouble(c.getColumnIndex("TOTAL")) >= c.getDouble(c.getColumnIndex("META"))) && (c.getDouble(c.getColumnIndex("TOTAL")) > 0))
		{
			// txtDocumento.setBackgroundColor(context.getResources().getColor(R.color.cor_verde));
			txtDocumento.setTextColor(context.getResources().getColor(R.color.cor_verde));
		} else
		{
			// txtDocumento.setBackgroundColor(context.getResources().getColor(R.color.cor_vermelho));
			txtDocumento.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
		}

		// }
		// TextView bTitle = (TextView) v.findViewById(R.id.btitle);
		// bTitle.setText(bookmark);*/
		return (v);
	}

}