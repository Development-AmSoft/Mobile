package smart.mobile.consulta.titulos;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

public class frm_cons_titulos_adapter extends SimpleCursorAdapter {

        private Cursor c;
        private Context context;
    	private DecimalFormat myCustDecFormatter = new DecimalFormat("#,##0.00");

	public frm_cons_titulos_adapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.c = c;
		this.context = context;
	}
	
	
	private boolean isVencido(String DataVencimento){
		  
		 boolean retorno = false;
		 
	     SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
		 String DataHoje = sdf.format(new Date());
		 
		 //converte para numero inteiro as duas datas
		 DataVencimento = DataVencimento.substring(6,10) + DataVencimento.substring(3,5) + DataVencimento.substring(0,2);
		 DataHoje       = DataHoje.substring(6,10)       + DataHoje.substring(3,5)       + DataHoje.substring(0,2);
				 
		if(Long.valueOf(DataVencimento) <= Long.valueOf(DataHoje)){
			retorno = true;
		}
		 	  
		return retorno;
		  
	  }
	

	public View getView(int pos, View inView, ViewGroup parent) {
	       View v = inView;
	      
	       if (v == null) {
	            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = inflater.inflate(R.layout.lay_cons_titulos, null);
	       }
	       
	       this.c.moveToPosition(pos);		
	       //String bookmark = this.c.getString(this.c.getColumnIndex(Browser.BookmarkColumns.TITLE));
	       //byte[] favicon = this.c.getBlob(this.c.getColumnIndex(Browser.BookmarkColumns.FAVICON));
	       //if (favicon != null) {
		   
	       
	       TextView txtNome = (TextView) v.findViewById(R.id.txtNome);
           txtNome.setText(c.getString(c.getColumnIndex("NOME")));
           
           TextView txtCPFCNPJ = (TextView) v.findViewById(R.id.txtCPFCNPJ);
           txtCPFCNPJ.setText(c.getString(c.getColumnIndex("CPFCNPJ")));
           
	       ImageView iv = (ImageView) v.findViewById(R.id.x_situacao);
	       TextView txtTipo = (TextView) v.findViewById(R.id.txtTipo);
           	       
	       if(c.getLong(c.getColumnIndex("TIPO"))==0){
//	    	   iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_duplicata));
	       	   txtTipo.setText("Dup./Boleto");}
	    	   //txtSituacao.setText("Sincronizado"); txtSituacao.setTextColor(context.getResources().getColor(R.color.solid_green));}
	       else{
	    	   txtTipo.setText("Cheque");
//	    	   iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_cheque));
	    	   //txtSituacao.setText("Pendente"); txtSituacao.setTextColor(context.getResources().getColor(R.color.all_cinza));
	    	   }
	  
           TextView txtVencimento = (TextView) v.findViewById(R.id.txtVencimento);
           txtVencimento.setText(c.getString(c.getColumnIndex("VENCIMENTO")));

	       TextView txtDocumento = (TextView) v.findViewById(R.id.txtDocumento);
           txtDocumento.setText(c.getString(c.getColumnIndex("DOCUMENTO")));
           
           TextView txtValor = (TextView) v.findViewById(R.id.txtValor);
           txtValor.setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex("VALOR"))));
                   
   		   if(isVencido(txtVencimento.getText().toString())){
   			   //txtVencimento.setBackgroundColor(context.getResources().getColor(R.color.solid_red));
   			   txtValor.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
    		   txtVencimento.setTextColor(context.getResources().getColor(R.color.cor_vermelho));}
   		   else{
   			   //txtVencimento.setBackgroundColor(context.getResources().getColor(R.color.all_white));
   			   txtValor.setTextColor(context.getResources().getColor(R.color.all_black));
   			   txtVencimento.setTextColor(context.getResources().getColor(R.color.all_black));}
   			    
           
	       
	       //}
           //    TextView bTitle = (TextView) v.findViewById(R.id.btitle);
           //    bTitle.setText(bookmark);*/
	       return(v);
	}

}