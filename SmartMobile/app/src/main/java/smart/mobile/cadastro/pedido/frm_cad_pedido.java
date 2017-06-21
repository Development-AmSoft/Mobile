package smart.mobile.cadastro.pedido;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.R;
import smart.mobile.consulta.produtos.frm_limite_credito;
import smart.mobile.utils.ConfiguracaoInicialTela;
import smart.mobile.utils.error.ErroGeralController;
import android.app.ActivityGroup;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TabHost;

//MainActivity herda de ActivityGroup, porque vocÊ está lidando
//com várias atividades em um elemento só. Ou seja, o TabHost hospeda um grupo de atividades.
public class frm_cad_pedido extends ActivityGroup
{

	private DB_LocalHost banco;
	static TabHost tabHost;
	static int tab = -1;

	private long Ped_param;
	private long PedidoID;
	
	private final int MN_VOLTAR = 0;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_ctela_base);
		setTitle("SmartMobile - Detalhes do Pedido");
		
		ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.lay_cad_pedido, false);
		
		RelativeLayout footer = (RelativeLayout) findViewById(R.id.FooterEsconder);
		footer.setVisibility(View.GONE);
		
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
		Ped_param = c.getLong("pedidoid");

		Log.i("19", "Aqui " + String.valueOf(Ped_param));

		// CASO FOR UM NOVO PEDIDO ADICIONA NO BANCO DE DADOS E PEGA O ID
		if (Ped_param <= 0)
		{

			banco.TB_VENDAS_INSERIR(0, 0, "", 0, 0, 0.00, "",0);

			PedidoID = banco.getMaxId("VENDAS");
		} else
		{
			PedidoID = Ped_param;
		}
		;

		Log.i("Teste20", "Numero do Pedido " + String.valueOf(PedidoID));

		// parametro do pedido
		Bundle b = new Bundle();
		b.putLong("pedidoid", PedidoID);

		// Adiciona Tab #1
		intent = new Intent().setClass(this, frm_cad_pedido_geral.class);
		intent.putExtras(b);
		spec = tabHost.newTabSpec("0").setIndicator("Pedido nº" + String.valueOf(PedidoID), getResources().getDrawable(R.drawable.ico_pedidos_24)).setContent(intent);
		// spec = tabHost.newTabSpec("0").setIndicator("Pedido nº" +
		// String.valueOf(PedidoID)).setContent(intent);
		tabHost.addTab(spec);

		// Adiciona Tab #2
		intent = new Intent(this, frm_cad_pedido_itens.class);
		intent.putExtras(b);
		spec = tabHost.newTabSpec("1").setIndicator("Produtos", getResources().getDrawable(R.drawable.ico_produtos_24)).setContent(intent);
		// spec =
		// tabHost.newTabSpec("1").setIndicator("Produtos").setContent(intent);
		tabHost.addTab(spec);

//		// Adiciona Tab #3
//		intent = new Intent(this, frm_cad_pedido_itens_historico.class);
//		b.putLong("tipoid", 0);
//		intent.putExtras(b);
//		// spec = tabHost.newTabSpec("1").setIndicator(" ",
//		// res.getDrawable(R.drawable.ico_produtos)).setContent(intent);
//		spec = tabHost.newTabSpec("2").setIndicator("Mix Mobile", getResources().getDrawable(R.drawable.ico_mix_smartmobile_24)).setContent(intent);
//		tabHost.addTab(spec);
//
//		// Adiciona Tab #4
//		intent = new Intent(this, frm_cad_pedido_itens_historico.class);
//		b.putLong("tipoid", 1);
//		intent.putExtras(b);
//		// spec = tabHost.newTabSpec("1").setIndicator(" ",
//		// res.getDrawable(R.drawable.ico_produtos)).setContent(intent);
//		spec = tabHost.newTabSpec("3").setIndicator("Mix Fatura", getResources().getDrawable(R.drawable.ico_mix_smarttools_24)).setContent(intent);
//		tabHost.addTab(spec);

		// Adiciona Tab #5
		intent = new Intent(this, frm_limite_credito.class);
		intent.putExtras(b);
		// spec = tabHost.newTabSpec("1").setIndicator(" ",
		// res.getDrawable(R.drawable.ico_produtos)).setContent(intent);
		spec = tabHost.newTabSpec("4").setIndicator("Limite de Crédito", getResources().getDrawable(R.drawable.ico_mix_smarttools_24)).setContent(intent);
		tabHost.addTab(spec);

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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemAdd = menu.add(0, MN_VOLTAR, Menu.NONE, "Voltar");

		// Allocate shortcuts to each of them.
		itemAdd.setShortcut('0', 'v');

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case MN_VOLTAR:

			finish();
			break;
		}
		return false;
	}

}
