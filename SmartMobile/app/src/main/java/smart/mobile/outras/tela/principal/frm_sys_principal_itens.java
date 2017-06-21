package smart.mobile.outras.tela.principal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import smart.mobile.R;

public class frm_sys_principal_itens extends BaseAdapter
{
    Context context;

    private int[] menuIcons = { R.drawable.ico_pedidos,
                               R.drawable.ico_cliente,
                               R.drawable.ico_produto,
                               R.drawable.ico_pendencias,
                               R.drawable.ico_relatorios,
                               R.drawable.ico_saldo_flex,
                               R.drawable.ico_metas,
                               R.drawable.ico_venda,
                               R.drawable.ico_sincronizar,
                               R.drawable.ico_smart_web,
                               R.drawable.ico_map,
                               R.drawable.ico_estoque, 
                               R.drawable.ico_sugestao,
                               R.drawable.ico_conexao,
                               R.drawable.ico_sobre,
                               R.drawable.ico_configurar};
    private String[] menuNames = {"Pedidos",
    		"Clientes",
    		"Produtos",
    		"Pendências",
    		"Relatórios",
    		"Saldo Flex",
    		"Rel. Metas",
    		"Rel. Vendas",
    		"Sincronizar",
    		"SmartWeb",
    		"Mapas",
    		"Coletor",
    		"Sugestão",
    		"Conexão",
    		"Sobre",
    		"Configurar"};


    public frm_sys_principal_itens(Context context)
    { 
        // TODO Auto-generated constructor stub
        this.context = context;
    }

    public int getCount()
    {
        // TODO Auto-generated method stub
        return menuIcons.length;
    }

    public Object getItem(int position)
    {
        // TODO Auto-generated method stub
        return menuIcons[position];
    }

    public long getItemId(int position)
    {
        // TODO Auto-generated method stub
        return position;
    }

    private class ViewHolder
    {
        public ImageView icon;
        public TextView label;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        // TODO Auto-generated method stub
        View vi = convertView;
        ViewHolder holder;

        if(convertView == null)
        {
            vi = LayoutInflater.from(context).inflate(R.layout.frmprincipal_layoutitens, null);
            holder = new ViewHolder();
            
            holder.icon = (ImageView) vi.findViewById(R.id.icone);
            holder.label = (TextView) vi.findViewById(R.id.icone_texto);

            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) vi.getTag();
        }
        holder.icon.setImageResource(menuIcons[position]);
        holder.label.setText(menuNames[position]);
        return vi;
    }

}
