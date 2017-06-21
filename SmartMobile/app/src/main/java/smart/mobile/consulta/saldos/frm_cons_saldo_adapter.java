package smart.mobile.consulta.saldos;

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

public class frm_cons_saldo_adapter extends SimpleCursorAdapter {

        private Cursor c;
        private Context context;
    	private DecimalFormat myCustDecFormatter = new DecimalFormat("#,##0.00");

	public frm_cons_saldo_adapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.c = c;
		this.context = context;
	}
	
	public View getView(int pos, View inView, ViewGroup parent) {
	       View v = inView;
	      
	       if (v == null) {
	            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = inflater.inflate(R.layout.lay_cons_saldo_linha, null);
	       }
	       
	       this.c.moveToPosition(pos);		
	       //String bookmark = this.c.getString(this.c.getColumnIndex(Browser.BookmarkColumns.TITLE));
	       //byte[] favicon = this.c.getBlob(this.c.getColumnIndex(Browser.BookmarkColumns.FAVICON));
	       //if (favicon != null) {
	       
	       
	       ImageView iv = (ImageView) v.findViewById(R.id.imgSituacao);
	       if(c.getLong(c.getColumnIndex("SINCRONIZADO"))==1){
	    	   iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_sync1));
	       }else{
	    	   iv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_sync01));
	       }
	       
	       TextView txtData = (TextView) v.findViewById(R.id.txtData);
	       txtData.setText(c.getString(c.getColumnIndex("DATA")));
	       
	       TextView txtReferencia = (TextView) v.findViewById(R.id.txtReferencia);
           txtReferencia.setText(c.getString(c.getColumnIndex("REFERENCIA")));
	       
           TextView txtAcrescimo = (TextView) v.findViewById(R.id.txtAcrescimo);
           txtAcrescimo.setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex("ACRESCIMO"))));
           
           TextView txtDesconto = (TextView) v.findViewById(R.id.txtDesconto);
           txtDesconto.setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex("DESCONTO"))));
           
           TextView txtSaldo = (TextView) v.findViewById(R.id.txtSaldo);
           txtSaldo.setText(myCustDecFormatter.format(c.getDouble(c.getColumnIndex("SALDO"))));
	       
   		   if((c.getDouble(c.getColumnIndex("SALDO"))>0)){
//   		      	txtSaldo.setBackgroundColor(context.getResources().getColor(R.color.cor_verde));
   			    txtSaldo.setTextColor(context.getResources().getColor(R.color.cor_verde));}
   		   else{
//   			    txtSaldo.setBackgroundColor(context.getResources().getColor(R.color.cor_vermelho));
   			    txtSaldo.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
   			    }
   			    
           
	       
	       //}
           //    TextView bTitle = (TextView) v.findViewById(R.id.btitle);
           //    bTitle.setText(bookmark);*/
	       return(v);
	}

}