package smart.mobile.consulta.relatorio;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import smart.mobile.R;
import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.utils.ConfiguracaoInicialTela;
import smart.mobile.utils.error.ErroGeralController;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class frm_sys_relatorios extends Activity {
	
	private DB_LocalHost banco;
	Cursor cVendas;
	private DecimalFormat myCustDecFormatter = new DecimalFormat("#,##0.00");
	private final int MN_VOLTAR = 0;
	
	Spinner cmbOperacoes;
	EditText txtFiltro = null;
	TextView lblFiltro = null;
	ImageButton btnSync = null;
	ListView listaCons = null;


    @Override 
    public void onCreate(Bundle icicle) {
                            
        // CARREGA O LAYOUT
    	super.onCreate(icicle);
        setContentView(R.layout.activity_ctela_base);
        setTitle("SmartMobile - Relatórios de Pedidos");
        
        ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.lay_sys_relatorios, false);
        
        RelativeLayout footer = (RelativeLayout) findViewById(R.id.FooterEsconder);
		footer.setVisibility(View.GONE);
        
        //carrega dados do banco
      	this.banco = new DB_LocalHost(this);
      	ErroGeralController erro = new ErroGeralController(this, banco);
      	
      	 //tipos de relatorios
        cmbOperacoes = (Spinner)this.findViewById(R.id.cmbTipo);
        ArrayAdapter<String> adpOperacoes = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,new String[]
        	                            	{"Resumo dos Pedidos", "Por Dia/Mês/Ano","Por Mês/Ano","Por Cliente","Por Cidade", "Por Produto", "Por Grupo"});
        cmbOperacoes.setAdapter(adpOperacoes);
      
        cmbOperacoes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
          	 
        	public void onItemSelected(AdapterView adapter, View v, int i, long lng) {
        	
        		LoadRelatorio(i);
        		 
        	}
        	 
        	public void onNothingSelected(AdapterView arg0) {
        	//do something else
        	}
        	});
        
      
        //campo de filtro
        txtFiltro = (EditText)findViewById(R.id.edtFiltro);
        txtFiltro.setHint("Filtrar no Relatório");
        
        //lista de dados
        listaCons = (ListView) findViewById(R.id.SCHEDULE);
        
        //campo de status
    	lblFiltro = (TextView) findViewById(R.id.lblStatus);
        
		//evento do botao atualizar
    	btnSync =(ImageButton)findViewById(R.id.btnSincroniza);
		btnSync.setOnClickListener(new OnClickListener() {
	          public void onClick(View v) {
	        	LoadRelatorio(cmbOperacoes.getSelectedItemPosition()); 
	             }
	           });
        
	}
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemAdd = menu.add(0, MN_VOLTAR, Menu.NONE, "Voltar");

		// Assign icons
		itemAdd.setIcon(R.drawable.ico_voltar);

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
		default:
			break;
		}
		return false;
	}
    
    private void LoadRelatorio(int Tipo){
    	
    	ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
    	
	    Double t_total   = 0.00;
	    Double t_credito = 0.00;
	    Double t_debito  = 0.00;
    	
    	if(Tipo==0){ // RESUMO GERAL
    		
    		txtFiltro.setVisibility(View.GONE);
    		btnSync.setVisibility(View.GONE);
    		//listaCons.setVisibility(View.GONE);
    		
    		 //CABEÇALHO
//            Lista_Cabecalho(mylist);
            
            //VARIAVEIS PARA SOMAR
            String a  = "Hoje";
    	    String b  = "Ontem";
    	    String c  = "Este Mês/Ano";
    	    String d  = "Todos os Pedidos";
    	    
    	    Double a_total = 0.00,a_credito = 0.00,a_debito   = 0.00;
    	    Double b_total = 0.00,b_credito = 0.00,b_debito   = 0.00;
    	    Double c_total = 0.00,c_credito = 0.00,c_debito   = 0.00;
    	    Double d_total = 0.00,d_credito = 0.00,d_debito   = 0.00;
    	                        
            //AGRUPA POR DIA/MES/ANO E CONSIDERA APENAS VENDAS
            cVendas = banco.db.rawQuery("select vendas.data, sum(vendas_itens.qtde * vendas_itens.valor), sum(vendas_itens.acrescimo), sum(vendas_itens.desconto) from vendas " +
                      "join vendas_itens on vendas._id = vendas_itens.vendaid " +
                      "join produtos on vendas_itens.produtoid = produtos.produtoid and vendas_itens.linhaid = produtos.linhaid and vendas_itens.colunaid = produtos.colunaid and vendas_itens.unidadeid = produtos.unidadeid " + 
                      "join clientes on vendas.CPF_CNPJ = clientes.CPF_CNPJ where vendas.operacao = 0 group by vendas.data order by vendas._id desc",null);
            startManagingCursor(cVendas);
            if (cVendas.moveToFirst()) {
                do {
                	
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
             	    String DataHoje = sdf.format(new Date());
                	
                	//A - hoje
                	if(cVendas.getString(0).equals(DataHoje)){
                		a_total   = a_total + cVendas.getDouble(1);
                		a_credito = a_credito + cVendas.getDouble(2);
                		a_debito  = a_debito + cVendas.getDouble(3);
                	}
                	
                	//B - ontem
                	
                	
                	//C - este mes
                	
                	
                	//E - linha de total
                	d_total   = d_total + cVendas.getDouble(1);
            		d_credito = d_credito + cVendas.getDouble(2);
            		d_debito  = d_debito + cVendas.getDouble(3);
                	
                	} while (cVendas.moveToNext());
            }
            if (cVendas != null || !cVendas.isClosed()) {
           	 cVendas.close();
            }
    	    
    	    Lista_AdicionaLinha(mylist, a, a_total, a_credito, a_debito);
    	    Lista_AdicionaLinha(mylist, b, b_total, b_credito, b_debito);
    	    Lista_AdicionaLinha(mylist, c, c_total, c_credito, c_debito);
    	    Lista_AdicionaLinha(mylist, d, d_total, d_credito, d_debito);
    	    
    		
    	}else {
    		
    		txtFiltro.setVisibility(View.VISIBLE);
    		btnSync.setVisibility(View.VISIBLE);
    		//listaCons.setVisibility(View.VISIBLE);
   	 
        //CARREGA TODOS OS PEDIDOS
    	String campoAgrupar  = "";
    	String campoOrdenar  = "";
    	String where = "";
    	String valorAgrupar = "";
    	
    	
    	if (Tipo==1){campoAgrupar="vendas.data"; campoOrdenar = "vendas.data order by vendas._id desc";}
    	else if (Tipo==2){campoAgrupar="vendas.data"; campoOrdenar = "vendas.data order by vendas._id desc";}
    	else if (Tipo==3){campoAgrupar="clientes.nome"; campoOrdenar = campoAgrupar + "";}
    	else if (Tipo==4){campoAgrupar="clientes.cidade"; campoOrdenar = campoAgrupar + "";}
    	else if (Tipo==5){campoAgrupar="produtos.descricao"; campoOrdenar = campoAgrupar + "";}
    	else if (Tipo==6){campoAgrupar="produtos.grupo"; campoOrdenar = campoAgrupar + "";}
    	
    	//considera apenas 'vendas'
    	where = "where vendas.operacao = 0";
    	
    	//caso tenha filtrado alguma coisa
    	if(!txtFiltro.getText().toString().equals(""))
    	  {where = " and " + campoAgrupar + " LIKE " + "'%" + txtFiltro.getText().toString() + "%'";}
    	
        cVendas = banco.db.rawQuery("select " + campoAgrupar + ", sum(vendas_itens.qtde * vendas_itens.valor), sum(vendas_itens.acrescimo), sum(vendas_itens.desconto) from vendas " +
                  "join vendas_itens on vendas._id = vendas_itens.vendaid " +
        		  "join produtos on vendas_itens.produtoid = produtos.produtoid and vendas_itens.linhaid = produtos.linhaid and vendas_itens.colunaid = produtos.colunaid and vendas_itens.unidadeid = produtos.unidadeid " + 
                  "join clientes on vendas.CPF_CNPJ = clientes.CPF_CNPJ " + where + " group by " + campoOrdenar,null);
	    startManagingCursor(cVendas);
	    
	    //CABEÇALHO
        //Lista_Cabecalho(mylist);
                
        //FILTRA OS PEDIDOS POR DIA
	    //ArrayList<String> filtro = new ArrayList<String>();
	   
	    String dia     = "";
	    Double total   = 0.00;
	    Double credito = 0.00;
	    Double debito  = 0.00;
	    
	    if (cVendas.moveToFirst()) {
            do {
            	
            	//armazena o campo agrupado
            	if(Tipo==2){campoAgrupar=cVendas.getString(0).substring(3);}
            	else{campoAgrupar=cVendas.getString(0);}

            	//busca o total + debito + credito
            	if(dia.equals("")){
            		dia     = campoAgrupar;
            		total   = cVendas.getDouble(1);
            		credito = cVendas.getDouble(2);
            		debito  = cVendas.getDouble(3);
            	}
            	else if(!dia.equals(campoAgrupar)){
            		//adiciona na lista
            		Lista_AdicionaLinha(mylist, dia, total, credito, debito);
            		
            		//aramazena o novo dia
            		dia     = campoAgrupar;
            		total   = cVendas.getDouble(1);
            		credito = cVendas.getDouble(2);
            		debito  = cVendas.getDouble(3);  
            	}
            	else{
            		total = total + cVendas.getDouble(1);
            		credito = credito + cVendas.getDouble(2);
            		debito  = debito + cVendas.getDouble(3);
            	}
            	
            	//linha de total
            	t_total = t_total + cVendas.getDouble(1);
        		t_credito = t_credito + cVendas.getDouble(2);
        		t_debito  = t_debito + cVendas.getDouble(3);
            		
            	//caso seja o ultimo
            	if(cVendas.isLast()){
            		//adiciona na lista
            		Lista_AdicionaLinha(mylist, dia, total, credito, debito);
            	}
            	
            } while (cVendas.moveToNext());
         }
         if (cVendas != null || !cVendas.isClosed()) {
        	 cVendas.close();
         }
         
         //adiciona a linha de total
         Lista_AdicionaLinha(mylist, "Total Geral R$", t_total, t_credito, t_debito); 
	    
	    
	   // for(int x=0; x <  listCidades.length-1; x++)
	   // {
	    // 	if (listCidades[x].indexOf("- " + UF) > 0){
	   //  		filtro.add(listCidades[x]);
	   //  	}
	   // }
	    //filtro.add("Dia 1");
	    //filtro.add("Dia 2");
	        
         /*
	    ArrayAdapter adpCid2 = new ArrayAdapter<String>(frm_sys_relatorios.this, android.R.layout.simple_spinner_item, filtro);
	    adpCid2.setDropDownViewResource(android.R.layout.simple_spinner_item);
	  
	    ListAdapter lsadapter  = adpCid2;
        ListView listaCons     = (ListView) findViewById(R.id.listView1);
		listaCons.setAdapter(lsadapter);
		listaCons.setFastScrollEnabled(true);*/
         
       
         
         
      
         /*
         map = new HashMap<String, String>();
         map.put("col1", "02/01/2012");
         map.put("col2", "45.659");
         map.put("col3", "12.48");
         map.put("col4", "125.98");
         mylist.add(map); */
         
         // ...
     
        // SimpleAdapter mSchedule = new SimpleAdapter(this, mylist, R.layout.lay_sys_relatorios_linha,
        //             new String[] {"train", "from", "to"}, new int[] {R.id.TRAIN_CELL, R.id.FROM_CELL, R.id.TO_CELL});
        // list.setAdapter(mSchedule);
         
         
         /*
 	    lblFiltro.setText("Registros : " + (mylist.size()-1));
        
     	ListAdapter adapter = new SimpleAdapter(this, mylist, R.layout.lay_sys_relatorios_linha,
     	                      new String[] {"col1", "col2", "col3", "col4", "col5"}, new int[] {R.id.txtDescricao, R.id.txtTotal, R.id.txtCredito, R.id.txtDebito, R.id.txtSaldo}) {
            
     	   @Override
            public void setViewText(TextView v, String text) {
              super.setViewText(v, convText(v, text));
            }
            
        };
     	
     	
     	ListAdapter lsadapter  = adapter;
   		listaCons.setAdapter(lsadapter);
   		listaCons.setFastScrollEnabled(true);
          */
    	}
    	
    	
    	lblFiltro.setText("Registros: " + (mylist.size()-1) + " [ref. aos pedidos salvos no aparelho]");
        
     	ListAdapter adapter = new SimpleAdapter(this, mylist, R.layout.lay_sys_relatorios_linha,
     	                      new String[] {"col1", "col2", "col3", "col4", "col5"}, new int[] {R.id.txtDescricao, R.id.txtTotal, R.id.txtCredito, R.id.txtDebito, R.id.txtSaldo}) {
            
     	   @Override
            public void setViewText(TextView v, String text) {
              super.setViewText(v, convText(v, text));
            }
            
        };
     	
     	
     	ListAdapter lsadapter  = adapter;
   		listaCons.setAdapter(lsadapter);
   		listaCons.setFastScrollEnabled(true);
    	
    	
    }
    
    private String convText(TextView v, String text) {
		 
    	 if (v.getId() == R.id.txtSaldo)
   		    {
    		 if((!(text=="Saldo R$")) && (!text.equalsIgnoreCase("0,00"))){
     	 	     if(text.indexOf("-") > -1 )
     	 	       {
     	 	    	v.setTextColor(getResources().getColor(R.color.cor_vermelho));
     	 	    	return text;
     	 	       }
     	 	     else
     	 	       {	 
     	 	    	v.setTextColor(getResources().getColor(R.color.cor_verde));
     	 	    	return text;
     	 	       }
    		 }
   			}
   	     
   	   				 		 
   	     return text;
   	     
   	} 
    
    
    private void Lista_Cabecalho(ArrayList lista){
    	
    	  HashMap<String, String> map = new HashMap<String, String>();
          map.put("col1", cmbOperacoes.getSelectedItem().toString());
          map.put("col2", "Bruto R$");
          map.put("col3", "Acrésc.");
          map.put("col4", "Desc.");
          map.put("col5", "Líquido R$");
          lista.add(map);
    	
    	
    }
    
    private void Lista_AdicionaLinha(ArrayList lista, String Descricao,Double Total, Double Credito, Double Debito){
    	
    	   HashMap<String, String> map = new HashMap<String, String>();
           map.put("col1", Descricao);
           map.put("col2", myCustDecFormatter.format(Total));
           map.put("col3", myCustDecFormatter.format(Credito));
           map.put("col4", myCustDecFormatter.format(Debito));
           map.put("col5", myCustDecFormatter.format(Total+Credito-Debito));
           lista.add(map);
    	
    }
    
}