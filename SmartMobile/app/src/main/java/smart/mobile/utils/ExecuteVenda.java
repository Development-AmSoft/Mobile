package smart.mobile.utils;

import smart.mobile.consulta.vendas.frm_cons_vendas;

import android.content.Context;
import android.os.Handler;

public abstract class ExecuteVenda {
	
	private Handler h = new Handler();
	public abstract void execute(Context context);
	
	public ExecuteVenda(final Context context) {
		h.post(new Runnable() {

			@Override
			public void run() {
				((frm_cons_vendas)context).ativarPopUp();
				Handler hh = new Handler();
				hh.postDelayed(new Runnable() {

					@Override
					public void run() {
						execute(context);
						Handler handler = new Handler(); 
			            handler.postDelayed(new Runnable() { 
			            public void run() { 
			            	((frm_cons_vendas)context).desativarPopUp();;

			         } 
			    }, 400); 
							
						
					}
				}, 200);
			}
		});
	}
	
	
}
