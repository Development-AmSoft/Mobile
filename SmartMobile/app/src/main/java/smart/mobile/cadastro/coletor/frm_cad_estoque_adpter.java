package smart.mobile.cadastro.coletor;

import smart.mobile.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class frm_cad_estoque_adpter extends SimpleCursorAdapter {

	private Context context;

	public frm_cad_estoque_adpter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		this.getCursor().moveToPosition(position);
		ViewHolder holder;
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.lay_cons_produtos_estoque, parent, false);

			holder = new ViewHolder();

			holder.txtDescricao = (TextView) v.findViewById(R.id.txtDescricao);
			holder.txtCodigo = (TextView) v.findViewById(R.id.txtCodigo);
			holder.txtUN = (TextView) v.findViewById(R.id.txtUN);
			holder.txtEstoque = (TextView) v.findViewById(R.id.txtEstoque);
			holder.txtEstoqueAntigo = (TextView) v.findViewById(R.id.txtEstoqueAntigo);
			holder.txtAcrescimo = (TextView) v.findViewById(R.id.txtAcrescimo);
			holder.txtDecrescimo = (TextView) v.findViewById(R.id.txtDecrescimo);
			holder.txtObs = (TextView) v.findViewById(R.id.txtObs);
			v.setTag(holder);

		} else {
			holder = (ViewHolder) v.getTag();
		}
		
		holder.txtDescricao.setText(getCursor().getString(getCursor().getColumnIndex("DESCRICAO")));
		holder.txtCodigo.setText(getCursor().getString(getCursor().getColumnIndex("_id")));
		holder.txtUN.setText(getCursor().getString(getCursor().getColumnIndex("UND")));
		holder.txtEstoqueAntigo.setText(getCursor().getString(getCursor().getColumnIndex("ESTOQUE_ANTIGO")));
		holder.txtObs.setText(getCursor().getString(getCursor().getColumnIndex("OBS")));
		try {
			double acrescimo = getCursor().getDouble(getCursor().getColumnIndex("ASCRECIMO"));
			double descrescimo = getCursor().getDouble(getCursor().getColumnIndex("DECRESCIMO"));
			double estoque = getCursor().getDouble(getCursor().getColumnIndex("ESTOQUE"));
			holder.txtEstoque.setText(((estoque+acrescimo) - descrescimo)+"");
			holder.txtAcrescimo.setText(acrescimo+"");
			holder.txtDecrescimo.setText(descrescimo+"");
		} catch (Exception e) {
			holder.txtEstoque.setText(getCursor().getString(getCursor().getColumnIndex("ESTOQUE")));
		}

		return v;
	}

	static class ViewHolder {
		TextView txtDescricao, txtCodigo, txtUN, txtEstoque, txtEstoqueAntigo, txtAcrescimo, txtDecrescimo, txtObs;
	}

}
