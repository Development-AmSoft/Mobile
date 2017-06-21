package smart.mobile.consulta.vendas;

import java.util.ArrayList;
import java.util.List;

import smart.mobile.R;
import smart.mobile.consulta.vendas.frm_cons_vendas.VendasConsulta;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class frm_cons_vendas_adapter extends ArrayAdapter<VendasConsulta> {

	private Context context;
	private ArrayList<VendasConsulta> array;
	private int layout;

	public frm_cons_vendas_adapter(Context context, int layout, List<VendasConsulta> objects) {
		super(context, layout, objects);
		this.layout = layout;
		this.context = context;
		array = (ArrayList<VendasConsulta>) objects;
	}
	
	@Override
	public VendasConsulta getItem(int position) {
		// TODO Auto-generated method stub
		return array.get(position);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return array.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		VendasConsulta consulta = getItem(position);

		ViewHolder holder = new ViewHolder();
		
		if (layout != R.layout.lay_cons_vendas_simples_adp) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.lay_cons_vendas_adp, null);
			
			holder.cnpj = (TextView) convertView.findViewById(R.id.vendaCnpj);
			holder.nome = (TextView) convertView.findViewById(R.id.vendaNome);
			holder.dataPed = (TextView) convertView.findViewById(R.id.vendaDataPed);
			holder.dataFat = (TextView) convertView.findViewById(R.id.vendaDataFat);
			holder.total = (TextView) convertView.findViewById(R.id.vendaTotal);
			holder.situacao = (TextView) convertView.findViewById(R.id.vendaSit);
			holder.vendaId = (TextView) convertView.findViewById(R.id.vendaId);

			holder.cnpj.setText(consulta.getCnpj());
			holder.nome.setText(consulta.getNome());
			holder.dataPed.setText(consulta.getDataPed());
			holder.dataFat.setText(consulta.getDataFat());
			holder.total.setText(consulta.getTotal());
			holder.vendaId.setText(consulta.getVendaId());
			int situacaoInt = Integer.parseInt(consulta.getSituacao());

			if (situacaoInt == 9) {
				holder.situacao.setText("Faturado");
				holder.situacao.setTextColor(context.getResources().getColor(R.color.cor_verde));
				holder.nome.setTextColor(context.getResources().getColor(R.color.cor_verde));
				holder.vendaId.setTextColor(context.getResources().getColor(R.color.cor_verde));
			} else if (situacaoInt == 8) {
				holder.situacao.setText("Pendente");
				holder.situacao.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
				holder.nome.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
				holder.vendaId.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
			} else if (situacaoInt == 4) {
				holder.situacao.setText("Pendente");
				holder.situacao.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
				holder.nome.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
				holder.vendaId.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
			} else if (situacaoInt == 3) {
				holder.situacao.setText("Cancelado");
				holder.situacao.setTextColor(context.getResources().getColor(R.color.titulo));
				holder.nome.setTextColor(context.getResources().getColor(R.color.titulo));
				holder.vendaId.setTextColor(context.getResources().getColor(R.color.titulo));
			} else if (situacaoInt == 10) {
				holder.situacao.setText("Cancelado");
				holder.situacao.setTextColor(context.getResources().getColor(R.color.titulo));
				holder.nome.setTextColor(context.getResources().getColor(R.color.titulo));
				holder.vendaId.setTextColor(context.getResources().getColor(R.color.titulo));
			}
			convertView.setTag(holder);
		} else {

			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.lay_cons_vendas_simples_adp, null);

			holder.um = (TextView) convertView.findViewById(R.id.vendaValor);
			holder.dois = (TextView) convertView.findViewById(R.id.vendaSoma);

			holder.um.setText(consulta.getCnpj());
			holder.dois.setText(consulta.getTotal());
			convertView.setTag(holder);
		}

		return convertView;

	}
	
	static class ViewHolder{
		TextView cnpj ;
		TextView nome ;
		TextView dataPed ;
		TextView dataFat;
		TextView total;
		TextView situacao;
		TextView vendaId;
		TextView um;
		TextView dois;
	}

}
