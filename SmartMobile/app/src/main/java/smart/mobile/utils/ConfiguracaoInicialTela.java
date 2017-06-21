package smart.mobile.utils;

import smart.mobile.PrincipalClasse;
import smart.mobile.R;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class ConfiguracaoInicialTela
{

	public ConfiguracaoInicialTela()
	{
		// TODO Auto-generated constructor stub
	}

	public static void carregarDadosIniciais(final Activity activity, int layout, boolean footerEspaco)
	{

		// Pega relative para insercao dinamica
		RelativeLayout r1 = (RelativeLayout) activity.findViewById(R.id.BaseConteudo);
		
		if(!footerEspaco){
			
			 /*<RelativeLayout
             android:id="@+id/BaseConteudo"
             android:layout_width="match_parent"
             android:layout_height="wrap_content"
             android:clickable="true"
             android:focusable="true"
             android:layout_marginBottom="45dp"
             android:focusableInTouchMode="true"
             android:padding="1dp" >
         </RelativeLayout>*/
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 0, 0, 0); //left,top,right,bottom
			r1.setLayoutParams(params);
		}
		
		r1.clearDisappearingChildren();
		View child = activity.getLayoutInflater().inflate(layout, r1, false);// carrega
																				// pagina
																				// a
																				// ser
																				// inseida
		r1.addView(child);// injeta na pagina a tela

		// mapeia componentes de config
		TextView lblEmpresa = (TextView) activity.findViewById(R.id.textView2);
		TextView lblVendedor = (TextView) activity.findViewById(R.id.textView3);

		PrincipalClasse aplication = (PrincipalClasse) activity.getApplication();

		TextView lblTitulo = (TextView) activity.findViewById(R.id.titulo);
		lblTitulo.setText(activity.getTitle());

		lblEmpresa.setText(aplication.getEmpresa());
		lblVendedor.setText(aplication.getVendedor());

		ImageView menu = (ImageView) activity.findViewById(R.id.menuimage);
		menu.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{

				activity.openOptionsMenu();

			}
		});
	}

	public static void removerFundoTela(final Activity activity, RelativeLayout relative)
	{

		relative.setBackground(null);

	}

	public static void removerFundoTelaPadraoConsulta(final Activity activity)
	{
		RelativeLayout relative = (RelativeLayout) activity.findViewById(R.id.conteudo_sombra);
		if (Build.VERSION.SDK_INT >= 16) {

			relative.setBackground(null);

		} else {

			relative.setBackgroundDrawable(null);
		}

	}

}
