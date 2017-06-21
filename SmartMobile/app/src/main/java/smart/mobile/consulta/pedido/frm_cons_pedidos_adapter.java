package smart.mobile.consulta.pedido;

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

public class frm_cons_pedidos_adapter extends SimpleCursorAdapter
{

	private int TipoLayout = 0;// 0 -- simples 1 -- completo
	private DB_LocalHost banco;
	private Cursor c;
	private Context context;
	private TextView txtTotalST;
	private TextView txtTotal;

	public frm_cons_pedidos_adapter(Context context, int layout, Cursor c, String[] from, int[] to, int xTipoLayout, DB_LocalHost banco)
	{
		super(context, layout, c, from, to);

		this.TipoLayout = xTipoLayout;
		this.c = c;
		this.context = context;

		// carrega dados do banco
		this.banco = banco;

	}

	public View getView(int pos, View inView, ViewGroup parent)
	{
		View v = inView;

		if (v == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if (TipoLayout == 0)
			{
				v = inflater.inflate(R.layout.lay_cons_pedidos_simples, null);
			} else
			{
				v = inflater.inflate(R.layout.lay_cons_pedidos, null);
			}

		}
		this.c.moveToPosition(pos);
		// String bookmark =
		// this.c.getString(this.c.getColumnIndex(Browser.BookmarkColumns.TITLE));
		// byte[] favicon =
		// this.c.getBlob(this.c.getColumnIndex(Browser.BookmarkColumns.FAVICON));
		// if (favicon != null) {

		ImageView iv = (ImageView) v.findViewById(R.id.x_situacao);
		TextView txtSituacao = (TextView) v.findViewById(R.id.txtSituacao);

		txtTotal = (TextView) v.findViewById(R.id.txtTotal);
		txtTotal.setText(banco.myCustDecFormatter.format(c.getDouble(c.getColumnIndex("TOTAL"))));

		// caso for bonificacao ou devolucao

		if (c.getLong(c.getColumnIndex("SINCRONIZADO")) == 1)
		{
			iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_sync1));
			txtSituacao.setText("Enviado"); // txtSituacao.setTextColor(context.getResources().getColor(R.color.solid_green));}
			txtSituacao.setTextColor(context.getResources().getColor(R.color.cor_verde));
			// txtTotal.setTextColor(context.getResources().getColor(R.color.cor_verde));
			// txtTotalST.setTextColor(context.getResources().getColor(R.color.cor_verde));
			// txtSituacao.setBackgroundColor(context.getResources().getColor(R.color.solid_green));

		} else
		{
			iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_sync01));
			txtSituacao.setText("Pendente"); // txtSituacao.setTextColor(context.getResources().getColor(R.color.solid_red));
			txtSituacao.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
			// txtTotal.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
			// txtTotalST.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
			// txtSituacao.setBackgroundColor(context.getResources().getColor(R.color.solid_red));
			// txtTotal.setTextColor(context.getResources().getColor(R.color.solid_red));
		}

		// iv.setImageBitmap(BitmapFactory.decodeByteArray(favicon, 0,
		// favicon.length));

		TextView txtNome = (TextView) v.findViewById(R.id.txtNome);
		txtNome.setText(c.getString(c.getColumnIndex("NOME")));

		TextView txtData = (TextView) v.findViewById(R.id.txtData);
		txtData.setText(c.getString(c.getColumnIndex("DATA")));

		if (TipoLayout == 1)
		{

			TextView txtST = (TextView) v.findViewById(R.id.txtST);
			txtST.setText(banco.myCustDecFormatter.format(c.getDouble(c.getColumnIndex("TOTAL_ST")) - c.getDouble(c.getColumnIndex("TOTAL"))));

			TextView txtTotalST = (TextView) v.findViewById(R.id.txtTotalST);
			txtTotalST.setText(banco.myCustDecFormatter.format(c.getDouble(c.getColumnIndex("TOTAL_ST"))));

			TextView txtPedido = (TextView) v.findViewById(R.id.txtPedido);
			txtPedido.setText(c.getString(c.getColumnIndex("_id")));

			TextView labelNumero = (TextView) v.findViewById(R.id.lblPedido);
			if (c.getLong(c.getColumnIndex("SINCRONIZADO")) == 1)
			{
				txtPedido.setTextColor(context.getResources().getColor(R.color.cor_verde));
				labelNumero.setTextColor(context.getResources().getColor(R.color.cor_verde));
			} else
			{
				txtPedido.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
				labelNumero.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
			}

			TextView txtFantasia = (TextView) v.findViewById(R.id.txtFantasia);
			txtFantasia.setText(c.getString(c.getColumnIndex("FANTASIA")));

			TextView txtCPFCNPJ = (TextView) v.findViewById(R.id.txtCPFCNPJ);
			txtCPFCNPJ.setText(c.getString(c.getColumnIndex("CPF_CNPJ")));

			TextView txtCidade = (TextView) v.findViewById(R.id.txtCidade);
			txtCidade.setText(c.getString(c.getColumnIndex("CIDADE")));

		}

		prepararOperacaoZero();

		// TextView txtFantasia = (TextView) v.findViewById(R.id.n_fantasia);
		// txtFantasia.setText(c.getString(c.getColumnIndex("FANTASIA")));

		// TextView txtCidade = (TextView) v.findViewById(R.id.n_cidade);
		// txtCidade.setText(c.getString(c.getColumnIndex("CIDADE")));

		// }
		// TextView bTitle = (TextView) v.findViewById(R.id.btitle);
		// bTitle.setText(bookmark);*/
		return (v);
	}

	private void prepararOperacaoZero()
	{
		if (c.getLong(c.getColumnIndex("OPERACAO")) != 0)
		{
			if (txtTotal != null)
			{
				txtTotal.setText("");
			}
			if (txtTotalST != null)
			{
				txtTotalST.setText("");
			}
			// if(c.getLong(c.getColumnIndex("OPERACAO"))==1){txtTotal.setText("Bonificação");}
			// if(c.getLong(c.getColumnIndex("OPERACAO"))==2){txtTotal.setText("Devolução");}
		}
	}

}