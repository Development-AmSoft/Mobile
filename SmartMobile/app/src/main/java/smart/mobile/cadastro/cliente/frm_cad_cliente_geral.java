package smart.mobile.cadastro.cliente;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.R;
import smart.mobile.utils.error.ErroGeralController;
import android.app.ActivityGroup;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

//MainActivity herda de ActivityGroup, porque vocÊ está lidando
//com várias atividades em um elemento só. Ou seja, o TabHost hospeda um grupo de atividades.
public class frm_cad_cliente_geral extends ActivityGroup
{

	private DB_LocalHost banco;
	static TabHost tabHost;
	static int tab = -1;

	private String ClienteID;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);

		setContentView(R.layout.lay_cad_pedido);
		setTitle("SmartMobile - Detalhes do Cliente");
		this.banco = new DB_LocalHost(this);
		
		ErroGeralController erro = new ErroGeralController(this, banco);

		Resources res = getResources();
		tabHost = (TabHost) findViewById(R.id.tabhost);
		tabHost.setup(this.getLocalActivityManager());
		TabHost.TabSpec spec;
		Intent intent;

		// Cada Tab adicionada tem sua própria Activity, que por sua vez , tem
		// seu próprio .xml(layout) e //também tem um elemento Drawable. Esse
		// elemento eu usei para mudar o ícone da Tab.
		// Quando ela não está ativa, o ícone é cinza. Quando ela está ativa, o
		// ícone fica colorido e indica //pro usuário que ele está naquela Tab.

		// Nesse meu exemplo, eu tenho 4 Tabs.4 Activities. 4 Layouts. 4
		// drawable que faz a mudança de ícone. // 1 para cada Tab.

		// PARAMETRO DO PEDIDO
		Bundle c = getIntent().getExtras();
		ClienteID = c.getString("clienteid");

		// parametro do pedido
		Bundle b = new Bundle();
		b.putLong("pedidoid", 0);
		b.putString("clienteid", ClienteID);

		// Adiciona Tab #1
		intent = new Intent().setClass(this, frm_cad_cliente.class);
		intent.putExtras(b);
		// spec = tabHost.newTabSpec("0").setIndicator(" ",
		// res.getDrawable(R.drawable.ico_pedidos)).setContent(intent);
		spec = tabHost.newTabSpec("0").setIndicator("Cadastro", getResources().getDrawable(R.drawable.ico_cliente)).setContent(intent);
		tabHost.addTab(spec);

//		// Adiciona Tab #2
//		intent = new Intent(this, frm_cad_pedido_itens_historico.class);
//		b.putLong("tipoid", 0);
//		intent.putExtras(b);
//		// spec = tabHost.newTabSpec("1").setIndicator(" ",
//		// res.getDrawable(R.drawable.ico_produtos)).setContent(intent);
//		spec = tabHost.newTabSpec("1").setIndicator("Mix - SmartMobile", getResources().getDrawable(R.drawable.ico_mix_smartmobile)).setContent(intent);
//		tabHost.addTab(spec);
//
//		// Adiciona Tab #3
//		intent = new Intent(this, frm_cad_pedido_itens_historico.class);
//		b.putLong("tipoid", 1);
//		intent.putExtras(b);
//		// spec = tabHost.newTabSpec("1").setIndicator(" ",
//		// res.getDrawable(R.drawable.ico_produtos)).setContent(intent);
//		spec = tabHost.newTabSpec("2").setIndicator("Mix - SmartTools", getResources().getDrawable(R.drawable.ico_mix_smarttools)).setContent(intent);
//		tabHost.addTab(spec);

		// intent = new Intent().setClass(this, OndeEstouActivity.class);
		// spec = tabHost.newTabSpec("2").setIndicator("Onde Estou?",
		// res.getDrawable(R.drawable.tab_where)).setContent(intent);
		// tabHost.addTab(spec);

		// Adiciona Tab #4
		// intent = new Intent().setClass(this, ListasActivity.class);
		// spec = tabHost.newTabSpec("3").setIndicator("Mapas",
		// res.getDrawable(R.drawable.tab_mapas)).setContent(intent);
		// tabHost.addTab(spec);

		// essa ultima linha indica qual tab será carregada ao iniciar essa
		// activity. No nosso caso, a Primeira!!!

		tabHost.setCurrentTab(0);

	}

}
