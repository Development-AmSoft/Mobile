package smart.mobile.cadastro.pedido;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.R;
import smart.mobile.utils.error.ErroGeralController;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class frm_cad_pedido_item extends Activity {

	private DB_LocalHost banco;
	private boolean Sincronizado = false;
	private EditText edtCPFCNPJ;
	private boolean loading = false;
	private DecimalFormat myCustDecFormatter = new DecimalFormat("0.00");

	private final int MN_VOLTAR = 0;
	private final int MN_SALVAR = 1;

	private boolean v_incluindo = false;
	private long v_pedidoid;
	private long v_listaid;
	private long v_itemid;
	private long v_produtoid;
	private long v_unidadeid;
	private String v_descricao;
	private long v_linhaid;
	private long v_colunaid;
	private String v_linha;
	private String v_coluna;
	private String v_UND;
	private Double v_valor_cad;
	private Double v_desc_max;
	private Double v_qtde;
	private Double v_valor;
	private Double v_estoque;
	private Double v_percentual_st;
	private Double v_percentual_ipi;
	private Double v_peso;
	private String v_cliente_id;
	private long   v_tipo_operacao;

	private String descontoPorcentagemAntigo = "0.0";
	private String descontoAntigo = "0.0";
	private String acrescimoPorcentagemAntigo = "0.0";
	private String acrescimoAntigo = "0.0";

	// EditText txtCodigo;
	TextView txtDescricao;
	TextView txtUND;
	TextView txtValorCad;
	TextView txtDescMax;
	EditText txtQtde;
	EditText txtValor;

	TextView txtSaldo;
	EditText txtAcrescimo;
	EditText txtDesconto;
	TextView txtTotal;

	Button btnVoltar;
	Button btnSalvar;
	private EditText txtPesoProduto;
	private ImageView qtdePlus;
	private ImageView qtdeRemove;
	private EditText txtDescontoPorcentagem;
	private EditText txtAcrescimoPorcentagem;

	private EditText valorCampo;

	@Override
	public void onCreate(Bundle icicle) {

		// CARREGA O LAYOUT
		super.onCreate(icicle);
		setContentView(R.layout.lay_cad_pedido_item);
		setTitle("SmartMobile - Produto do Pedido");

		// carrega dados do banco
		this.banco = new DB_LocalHost(this);
		this.banco.DB_SaldoFlexLoad();

		ErroGeralController erro = new ErroGeralController(this, banco);

		txtDescontoPorcentagem = (EditText) findViewById(R.id.txtDescontoPorcentagem);
		txtAcrescimoPorcentagem = (EditText) findViewById(R.id.txtAcrescimoPorcentagem);

		// PARAMETROS
		Bundle b = getIntent().getExtras();
		v_incluindo = b.getBoolean("incluindo");
		v_pedidoid = b.getLong("pedidoid");
		v_itemid = b.getLong("itemid");

		v_produtoid = b.getLong("produtoid");
		v_unidadeid = b.getLong("unidadeid");
		v_descricao = b.getString("descricao");
		v_linhaid = b.getLong("linhaid");
		v_colunaid = b.getLong("colunaid");
		v_linha = b.getString("linha");
		v_coluna = b.getString("coluna");

		v_UND = b.getString("und");
		v_valor_cad = b.getDouble("valor_cad");
		v_desc_max = b.getDouble("desc_max");
		v_qtde = b.getDouble("qtde");
		v_valor = b.getDouble("valor");
		v_estoque = b.getDouble("estoque");
		v_percentual_st = b.getDouble("percentual_st");
		v_percentual_ipi = b.getDouble("percentual_ipi");
		v_peso = b.getDouble("peso");

		// CARREGA OS CAMPOS
		// txtCodigo = ((EditText)findViewById(R.id.txtCodigo));
		// txtCodigo.setText(String.valueOf(v_produtoid));
		txtDescricao = ((TextView) findViewById(R.id.txtDescricao));
		txtDescricao.setText(String.valueOf(v_produtoid) + " - " + v_descricao);
		if ((!v_linha.trim().equals("")) || (!v_coluna.trim().equals(""))) {
			txtDescricao.setText(txtDescricao.getText().toString() + "\n" + v_linha + ": " + v_coluna);
		}

		txtUND = ((TextView) findViewById(R.id.txtUND));
		txtUND.setText(String.valueOf(v_UND));
		txtValorCad = ((TextView) findViewById(R.id.txtValorCad));
		txtValorCad.setText(myCustDecFormatter.format(v_valor_cad));
		txtDescMax = ((TextView) findViewById(R.id.txtDescMax));
		txtDescMax.setText(myCustDecFormatter.format(v_desc_max) + "%");
		txtQtde = ((EditText) findViewById(R.id.txtQtde));
		txtQtde.setText(myCustDecFormatter.format(v_qtde));
		txtValor = ((EditText) findViewById(R.id.txtValor));
		txtValor.setText(myCustDecFormatter.format(v_valor));
		txtPesoProduto = (EditText) findViewById(R.id.pesoProduto);
		txtPesoProduto.setText(myCustDecFormatter.format((v_peso * v_qtde)) + "");

		// SALDO FLEX
		Log.i("erroX", "passo1");
		txtSaldo = ((EditText) findViewById(R.id.txtSaldo));
		txtSaldo.setText(myCustDecFormatter.format(banco.temp_SaldoFlex));

		// TOTAIS
		Log.i("erroX", "passo2");
		txtAcrescimo = ((EditText) findViewById(R.id.txtAcrescimo)); //txtAcrescimo.setEnabled(false);
		txtDesconto = ((EditText) findViewById(R.id.txtDesconto)); //txtDesconto.setEnabled(false);
		txtTotal = ((EditText) findViewById(R.id.txtTotal));

		Log.i("erroX", "passo3");
		btnVoltar = ((Button) findViewById(R.id.btnVoltar));
		btnSalvar = ((Button) findViewById(R.id.btnSalvar));

		qtdePlus = (ImageView) findViewById(R.id.qtdePlus);
		qtdeRemove = (ImageView) findViewById(R.id.qtdeRemove);

		qtdePlus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String quantidadeS = txtQtde.getText().toString();
				if (!quantidadeS.isEmpty()) {
					Number num = null;
					try {
						num = NumberFormat.getInstance().parse(quantidadeS);
					} catch (Exception e) {
						num = Double.parseDouble(quantidadeS);
						e.printStackTrace();
					}
					int quantidade = num.intValue();
					txtQtde.setText((++quantidade) + "");
				} else {
					txtQtde.setText("1");
				}

			}
		});

		qtdeRemove.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String quantidadeS = txtQtde.getText().toString();
				if (!quantidadeS.isEmpty()) {
					Number num = null;
					try {
						num = NumberFormat.getInstance().parse(quantidadeS);
					} catch (Exception e) {
						num = Double.parseDouble(quantidadeS);
						e.printStackTrace();
					}
					int quantidade = num.intValue();
					if (quantidade > 0) {
						txtQtde.setText((--quantidade) + "");
					}
				} else {
					txtQtde.setText("1");
				}

			}
		});

		Log.i("erroX", "passo4");
		btnVoltar.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		btnSalvar.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				SalvarProduto();
			}
		});

		// INCLUINDO - CARREGA INFORMACOES DA LISTA DE PRECO
		// if(v_incluindo){
		String tipoListaXX = "";
		Cursor cLista = banco.db.rawQuery("select vendas.listaid,tipo_lista,percentual,clientes.CPF_CNPJ,vendas.OPERACAO from vendas join listas_precos on vendas.listaid = listas_precos.listaid JOIN clientes on vendas.CPF_CNPJ = clientes.CPF_CNPJ where vendas._id = " + String.valueOf(v_pedidoid), null);
		if (cLista.moveToFirst()) {
			v_cliente_id = cLista.getString(3);
			v_tipo_operacao = cLista.getLong(4);

			// AlertDialog.Builder msg = new
			// AlertDialog.Builder(frm_cad_pedido_item.this);
			// msg.setMessage(cLista.getString(0) + "x" + cLista.getString(1) +
			// "x" + cLista.getString(2)).show();

			String listaid = cLista.getString(0);
			if (!listaid.equals("0")) {

				tipoListaXX = cLista.getString(1);
				if (cLista.getString(1).equals("D")) { // DESCONTO

					if (v_incluindo) {
						v_valor = v_valor - ((v_valor * cLista.getDouble(2)) / 100);
					}

					// desconsidera o desconto maximo do produto e considera
					// apenas o da lista
					v_desc_max = cLista.getDouble(2);
					txtDescMax.setText(myCustDecFormatter.format(v_desc_max) + "%");

				} else if (cLista.getString(1).equals("A")) { // ACRESCIMO

					// SE FOR A LISTA COD 14 = +20% NA INCAS CONSIDERA QUE PODE
					// DAR 20% DE DESCONTO NOS PRODUTOS
					if (banco.Banco.toUpperCase().trim().equals("INCAS") && cLista.getString(0).equalsIgnoreCase("14")) {

						if (v_incluindo) {
							v_valor = v_valor + ((v_valor * cLista.getDouble(2)) / 100);
						}

						v_desc_max = 20.00;
						txtDescMax = ((TextView) findViewById(R.id.txtDescMax));
						txtDescMax.setText(myCustDecFormatter.format(v_desc_max) + "%");

						v_valor_cad = v_valor_cad + ((v_valor_cad * cLista.getDouble(2)) / 100);
						;
						txtValorCad.setText(myCustDecFormatter.format(v_valor_cad));

					} else {

						if (v_incluindo) {
							v_valor = v_valor + ((v_valor * cLista.getDouble(2)) / 100);
						}

						// consida o desconto maximo em cima do valor j� com
						// acr�scimo
						v_valor_cad = v_valor_cad + ((v_valor_cad * cLista.getDouble(2)) / 100);
						;
						txtValorCad.setText(myCustDecFormatter.format(v_valor_cad));

					}

				} else if (cLista.getString(1).equals("X")) { // PERSONALIZADA

					Cursor cListaX = banco.db.rawQuery("select tipo,percentual from listas_precos_produtos where listaid = " + listaid + " and produtoid = " + String.valueOf(v_produtoid), null);
					if (cListaX.moveToFirst()) {

						tipoListaXX = cListaX.getString(0);
						if (cListaX.getString(0).equals("D")) { // DESCONTO

							if (v_incluindo) {
								v_valor = v_valor - ((v_valor * cListaX.getDouble(1)) / 100);
							}

							// desconsidera o desconto maximo do produto e
							// considera apenas o da lista
							v_desc_max = cListaX.getDouble(1);
							txtDescMax.setText(myCustDecFormatter.format(v_desc_max) + "%");
						} else if (cListaX.getString(0).equals("A")) { // ACRESCIMO

							if (v_incluindo) {
								v_valor = v_valor + ((v_valor * cListaX.getDouble(1)) / 100);
							}

							// consida o desconto maximo em cima do valor j� com
							// acr�scimo
							v_valor_cad = v_valor_cad + ((v_valor_cad * cListaX.getDouble(1)) / 100);
							;
							txtValorCad.setText(myCustDecFormatter.format(v_valor_cad));

						}

					}

				}
			}

			// INFORMA O VALOR FINAL DO PRODUTO DE ACORDO COM A LISTA
			txtValor.setText(myCustDecFormatter.format(v_valor));
		}
		// }

		txtValorCad.setText(txtValorCad.getText().toString().replace(",", "."));
		txtQtde.setText(txtQtde.getText().toString().replace(",", "."));
		txtValor.setText(txtValor.getText().toString().replace(",", "."));

		if (v_incluindo) {
			txtQtde.setText(banco.QtdeProduto);
			CalcTotal();
		}else{
			CalcTotal();
		}


		// mostra se tem lista de acr�scimo ou desconto
		if (!tipoListaXX.equals("")) {
			txtValorCad.setText(txtValorCad.getText().toString() + " " + tipoListaXX);
		}

		// posiciona o foco no campo de quantidade
		txtQtde.selectAll();
		txtQtde.requestFocus();

		txtQtde.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				((EditText) v).selectAll();

			}
		});

		txtQtde.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				// caso digitar '.' e j� existir muda todo o texto para '0.' para
				// ele digitar as decimais
				//if(txtQtde.isFocused()){
//
//					if (s.equals(".") &&
//							(txtQtde.getText().toString().indexOf(".")
//									!=
//							 	 txtQtde.getText().toString().lastIndexOf("."))) {
//					txtQtde.setText("0");
					//s = "0.";
					//s = "";
//				}}

			}

			public void afterTextChanged(Editable s) {

				//remove o ultimo/segundo ponto adicionado para evitar erros
				if ((txtQtde.getText().toString().indexOf(".") > -1)
					&&
				(txtQtde.getText().toString().indexOf(".")
						!=
						txtQtde.getText().toString().lastIndexOf("."))){


					txtQtde.setText(txtQtde.getText().toString().substring(0,txtQtde.getText().toString().length()-1));
					txtQtde.setSelection(txtQtde.getText().toString().length());

				}
				else{
					if ((!s.toString().equals(".")) && (!s.toString().equals(""))) {
						CalcTotal();
					}
				}



			}
		});

		txtValor.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				((EditText) v).selectAll();

			}
		});

		txtDesconto.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				txtDesconto.removeTextChangedListener(wtDesconto);
				((EditText) v).selectAll();
				txtDesconto.addTextChangedListener(wtDesconto);
			}
		});

		txtDescontoPorcentagem.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				txtDescontoPorcentagem.removeTextChangedListener(wtPorcentagemDesconto);
				((EditText) v).selectAll();
				txtDescontoPorcentagem.addTextChangedListener(wtPorcentagemDesconto);
			}
		});

		txtAcrescimo.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				txtAcrescimo.removeTextChangedListener(wtAcrescimo);
				((EditText) v).selectAll();
				txtAcrescimo.addTextChangedListener(wtAcrescimo);

			}
		});

		txtAcrescimoPorcentagem.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				txtAcrescimoPorcentagem.addTextChangedListener(wtPorcentagemAcrescimo);
				((EditText) v).selectAll();
				txtAcrescimoPorcentagem.addTextChangedListener(wtPorcentagemAcrescimo);
			}
		});

		txtValor.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				// caso digitar '.' e j� exitir muda todo o texto para '0.' para
				// ele digitar as decimais
				//if (s.equals(".") && (txtQtde.getText().toString().indexOf(".") <= -1)) {
				//	txtQtde.setText("0.");
				//	s = "0.";
				//}

			}

			public void afterTextChanged(Editable s) {

				//remove o ultimo/segundo ponto adicionado para evitar erros
				if ((txtValor.getText().toString().indexOf(".") > -1)
						&&
						(txtValor.getText().toString().indexOf(".")
								!=
								txtValor.getText().toString().lastIndexOf("."))){


					txtValor.setText(txtValor.getText().toString().substring(0,txtValor.getText().toString().length()-1));
					txtValor.setSelection(txtValor.getText().toString().length());

				} else {
					if ((!s.toString().equals(".")) && (!s.toString().equals(""))) {
						CalcTotal();
					}
				}

			}
		});

		txtDescontoPorcentagem.addTextChangedListener(wtPorcentagemDesconto);
		txtDesconto.addTextChangedListener(wtDesconto);

		txtAcrescimoPorcentagem.addTextChangedListener(wtPorcentagemAcrescimo);
		txtAcrescimo.addTextChangedListener(wtAcrescimo);

		// ((InputMethodManager)
		// getSystemService(frm_cad_pedido_item.INPUT_METHOD_SERVICE)).showSoftInput(txtQtde,
		// 0);

		// InputMethodManager imm = (InputMethodManager)
		// getSystemService(frm_cad_pedido_item.INPUT_METHOD_SERVICE);
		// if(imm != null) {
		// imm.toggleSoftInput(0, 0);
		// imm.showSoftInput(txtQtde, InputMethodManager.SHOW_FORCED);
		// }

		if (v_tipo_operacao > 0){

            txtValor.setEnabled(false);

			txtAcrescimo.setEnabled(false);
			txtAcrescimoPorcentagem.setEnabled(false);

		    txtDesconto.setEnabled(false);
			txtDescontoPorcentagem.setEnabled(false);

		}

	}


	int positionDesconto;
	TextWatcher wtDesconto = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			positionDesconto = start;

			if (count < after || (start == 0 && count > 0)) {
				positionDesconto++;
			}
		}

		@Override
		public void afterTextChanged(Editable s) {

			if(txtDesconto.isFocused()) {

				if(txtDesconto.getText().toString().replace(" ", "").equalsIgnoreCase("") || txtDesconto.getText().toString().replace(" ","").equalsIgnoreCase("0")){
   				   txtDescontoPorcentagem.setText("0");}

				valorCampo = txtDesconto;
				String valorNovo = txtDesconto.getText().toString();
				valorNovo = valorNovo.replace(",", ".");
				double valorDouble = 0;
				try {
					valorDouble = Double.parseDouble(valorNovo);
					valorDouble = valorDouble / Double.parseDouble(txtQtde.getText().toString());

				} catch (Exception e) {

				}

				if (!(valorDouble + "").equals(descontoAntigo)) {
					CalcDescontoTotal(false, valorDouble + "");
				}

				if (positionDesconto <= txtDesconto.getText().length()) {
					txtDesconto.setSelection(positionDesconto);
				}
			}

		}
	};

	int positionPorcentagemDesconto;
	TextWatcher wtPorcentagemDesconto = new TextWatcher() {

		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			// caso digitar '.' e j� exitir muda todo o texto para '0.' para
			// ele digitar as decimais
			if (s.equals(".") && (txtQtde.getText().toString().indexOf(".") <= -1)) {
				// txtQtde.setText("0.");
				s = "0.";
			}
			positionPorcentagemDesconto = start;

			if (count < after || (start == 0 && count > 0)) {
				positionPorcentagemDesconto++;
			}

		}

		public void afterTextChanged(Editable s) {

			if(txtDescontoPorcentagem.isFocused()) {

				if(txtDescontoPorcentagem.getText().toString().replace(" ", "").equalsIgnoreCase("") || txtDescontoPorcentagem.getText().toString().replace(" ","").equalsIgnoreCase("0")){
					txtDesconto.setText("0");}

				valorCampo = txtDescontoPorcentagem;
				String valorNovo = txtDescontoPorcentagem.getText().toString();
				valorNovo = valorNovo.replace(",", ".");
				double valorDouble = 0;
				try {
					valorDouble = Double.parseDouble(valorNovo);
				} catch (Exception e) {

				}

				if (!valorNovo.equals(descontoPorcentagemAntigo)) {
					CalcDescontoTotal(true, valorDouble + "");
				}

				if (positionPorcentagemDesconto <= txtDescontoPorcentagem.getText().length()) {
					txtDescontoPorcentagem.setSelection(positionPorcentagemDesconto);
				}

			}

		}

	};


	int positionAcrescimo;
	TextWatcher wtAcrescimo = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			positionAcrescimo = start;

			if (count < after || (start == 0 && count > 0)) {
				positionAcrescimo++;
			}
		}

		@Override
		public void afterTextChanged(Editable s) {

			if(txtAcrescimo.isFocused()) {
				if(txtAcrescimo.getText().toString().replace(" ", "").equalsIgnoreCase("") || txtAcrescimo.getText().toString().replace(" ","").equalsIgnoreCase("0")){
					txtAcrescimoPorcentagem.setText("0");}

				valorCampo = txtAcrescimo;
				String valorNovo = txtAcrescimo.getText().toString();
				valorNovo = valorNovo.replace(",", ".");
				double valorDouble = 0;
				try {
					valorDouble = Double.parseDouble(valorNovo);

					valorDouble = valorDouble / Double.parseDouble(txtQtde.getText().toString());
				} catch (Exception e) {
				}

				if (!(valorDouble + "").equals(acrescimoAntigo)) {
					CalcAcrescimoTotal(false, valorDouble + "");
				}

				if (positionAcrescimo <= txtAcrescimo.getText().length()) {
					txtAcrescimo.setSelection(positionAcrescimo);
				}
			}
		}
	};

	int positionPorcentagemAcrescimo;
	TextWatcher wtPorcentagemAcrescimo = new TextWatcher() {

		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			// caso digitar '.' e j� exitir muda todo o texto para '0.' para
			// ele digitar as decimais
			if (s.equals(".") && (txtQtde.getText().toString().indexOf(".") <= -1)) {
				// txtQtde.setText("0.");
				s = "0.";
			}
			positionPorcentagemAcrescimo = start;

			if (count < after || (start == 0 && count > 0)) {
				positionPorcentagemAcrescimo++;
			}

		}

		public void afterTextChanged(Editable s) {

			if(txtAcrescimoPorcentagem.isFocused()) {

				if(txtAcrescimoPorcentagem.getText().toString().replace(" ", "").equalsIgnoreCase("") || txtAcrescimoPorcentagem.getText().toString().replace(" ","").equalsIgnoreCase("0")){
				txtAcrescimo.setText("0");}

				valorCampo = txtAcrescimoPorcentagem;
				String valorNovo = txtAcrescimoPorcentagem.getText().toString();
				valorNovo = valorNovo.replace(",", ".");
				double valorDouble = 0;
				try {
					valorDouble = Double.parseDouble(valorNovo);
				} catch (Exception e) {

				}
				if (!valorNovo.equals(acrescimoPorcentagemAntigo)) {
					CalcAcrescimoTotal(true, valorDouble + "");
				}
				if (positionPorcentagemAcrescimo <= txtAcrescimoPorcentagem.getText().length()) {
					txtAcrescimoPorcentagem.setSelection(positionPorcentagemAcrescimo);
				}

			}
		}

	};



	private void CalcAcrescimoTotal(boolean porcentagem, String valorNovo) {

		if(valorNovo.replace(" ","").equalsIgnoreCase("") || valorNovo.replace(" ","").equalsIgnoreCase("0")){
			txtAcrescimo.setText("0");
			txtAcrescimoPorcentagem.setText("0");
		}else{
			txtDesconto.setText("0");
			txtDescontoPorcentagem.setText("0");
		}

			if (porcentagem) {

				double valorNovoDouble = Double.parseDouble(valorNovo);

				acrescimoPorcentagemAntigo = valorNovo;

				double valor_acrescimo = (((v_valor_cad * valorNovoDouble) / 100) * Double.parseDouble(txtQtde.getText().toString()));
				txtAcrescimo.setText(myCustDecFormatter.format(valor_acrescimo));

				double valorFinal = v_valor_cad + (valor_acrescimo / Double.parseDouble(txtQtde.getText().toString()));
				txtValor.setText(myCustDecFormatter.format(valorFinal));
				txtValor.setText(txtValor.getText().toString().replace(",","."));

			} else {

				acrescimoAntigo = valorNovo;

				txtValor.setText(myCustDecFormatter.format((v_valor_cad + (Double.parseDouble(valorNovo)))));

				txtValor.setText(txtValor.getText().toString().replace(",", "."));

				double perc_acrescimo = ((Double.parseDouble(valorNovo) * 100) / v_valor_cad);

				txtAcrescimoPorcentagem.setText(myCustDecFormatter.format(perc_acrescimo));


		}
	}

	private void CalcDescontoTotal(boolean porcentagem, String valorNovo) {

		if(valorNovo.replace(" ","").equalsIgnoreCase("") || valorNovo.replace(" ","").equalsIgnoreCase("0")){
			txtDesconto.setText("0");
			txtDescontoPorcentagem.setText("0");
		}else{
			txtAcrescimo.setText("0");
			txtAcrescimoPorcentagem.setText("0");
		}

			if (porcentagem) {
				double valorNovoDouble = Double.parseDouble(valorNovo);

				descontoPorcentagemAntigo = valorNovo;

				double valor_desconto = (((v_valor_cad * valorNovoDouble) / 100) * Double.parseDouble(txtQtde.getText().toString()));
				txtDesconto.setText(myCustDecFormatter.format(valor_desconto));

				double valorFinal = v_valor_cad - (valor_desconto / Double.parseDouble(txtQtde.getText().toString()));

				txtValor.setText(myCustDecFormatter.format(valorFinal));
				txtValor.setText(txtValor.getText().toString().replace(",","."));


			} else {


				//Log.i("a",myCustDecFormatter.format((v_valor_cad - (Double.parseDouble(valorNovo)))));

				descontoAntigo = valorNovo;

				txtValor.setText(myCustDecFormatter.format((v_valor_cad - (Double.parseDouble(valorNovo)))));

				txtValor.setText(txtValor.getText().toString().replace(",","."));

				double perc_desconto = ((Double.parseDouble(valorNovo) * 100) / v_valor_cad);

				txtDescontoPorcentagem.setText(myCustDecFormatter.format(perc_desconto));

		}
	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemAdd = menu.add(0, MN_VOLTAR, Menu.NONE, "Voltar");
		MenuItem itemRem = menu.add(0, MN_SALVAR, Menu.NONE, "Salvar");

		// Assign icons
		itemAdd.setIcon(R.drawable.ico_voltar);
		itemRem.setIcon(R.drawable.ico_salvar);

		// Allocate shortcuts to each of them.
		itemAdd.setShortcut('0', 'v');
		itemRem.setShortcut('1', 's');
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case MN_VOLTAR:
				finish();
				return true;

			case MN_SALVAR:
				SalvarProduto();
				return true;
			// refresh = new Intent(this, frmConsClientes.class);
			// startActivity(refresh);

		}
		return false;
	}

	private void SalvarProduto() {

		if (txtQtde.getText().toString().trim().equals("")) {
			txtQtde.setText("0");
		}
		if (txtValor.getText().toString().trim().equals("")) {
			txtValor.setText("0");
		}

		// arredondando para 2 casas decimais
		BigDecimal bd = new BigDecimal(v_valor_cad);
		BigDecimal rounded = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		v_valor_cad = rounded.doubleValue();

		txtQtde.setText(txtQtde.getText().toString().replace(",", "."));
		txtValor.setText(txtValor.getText().toString().replace(",", "."));
		txtAcrescimo.setText(txtAcrescimo.getText().toString().replace(",", "."));
		txtDesconto.setText(txtDesconto.getText().toString().replace(",", "."));

		Float valor_minimo = Float.valueOf(myCustDecFormatter.format(v_valor_cad - ((v_valor_cad * v_desc_max) / 100)).replace(",", "."));

		Double valorfim = Double.valueOf(txtValor.getText().toString().replace(",", "."));
		Double qtdefim = Double.valueOf(txtQtde.getText().toString());

		Double acrescimo = 0.00;
		Double desconto = 0.00;

		Double flex_acrescimo = 0.00;
		Double flex_desconto = 0.00;

		// caso trabalhe com desconto flex calcula os valores
		if (banco.temp_cSaldoFlex) {

			// calculando acrescimo ou desconto para flex >> n�o considera o
			// 'desconto max.' nem 'acresc/desc de lista de pre�o'
			if (valorfim > v_valor_cad) {
				flex_acrescimo = (valorfim - v_valor_cad) * qtdefim;

			} else if (valorfim < valor_minimo) {
				flex_desconto = (valor_minimo - valorfim) * qtdefim;
			}
		}

		// arredondando para 2 casas decimais
		BigDecimal bdDesc = new BigDecimal(flex_desconto);
		BigDecimal rDesc = bdDesc.setScale(2, BigDecimal.ROUND_HALF_UP);
		flex_desconto = rDesc.doubleValue();

		// arredondando para 2 casas decimais
		BigDecimal bdSaldo = new BigDecimal(banco.temp_SaldoFlex);
		BigDecimal rSaldo = bdSaldo.setScale(2, BigDecimal.ROUND_HALF_UP);
		banco.temp_SaldoFlex = rSaldo.doubleValue();

		// validacao de valores
		if (Float.valueOf(txtQtde.getText().toString()) <= 0) {
			txtQtde.requestFocus();
			banco.MostraMsg(this, "Informe a Qtde !!!");
		} else if (banco.Banco.toUpperCase().trim().equals("TOCARI") && (Float.valueOf(txtQtde.getText().toString()) > v_estoque)) {
			txtQtde.requestFocus();
			banco.MostraMsg(this, "Produto n�o possui Estoque Suficiente !!!" + "\nQtde em Estoque: " + myCustDecFormatter.format(v_estoque));
		} else if (Float.valueOf(txtValor.getText().toString().replace(",", ".")) <= 0) {
			txtValor.requestFocus();
			banco.MostraMsg(this, "Informe o Valor !!!");
		} else if ((!banco.temp_cSaldoFlex) && (Float.valueOf(txtValor.getText().toString().replace(",", ".")) < valor_minimo)) {
			txtValor.requestFocus();
			banco.MostraMsg(this, "Valor M�nimo � de " + myCustDecFormatter.format(valor_minimo) + " !!!");
			// Toast.makeText(this,"Valor m�nimo � de " +
			// myCustDecFormatter.format(valor_minimo) + " !!!",
			// Toast.LENGTH_LONG).show();
		} else if ((banco.temp_cSaldoFlex) && (flex_desconto > 0) && (flex_desconto > banco.temp_SaldoFlex)) {
			txtValor.requestFocus();
			banco.MostraMsg(this, "Saldo Flex insuficiente !!!" + "\nDispon�vel: " + myCustDecFormatter.format(banco.temp_SaldoFlex) + "\nDesconto Flex: " + myCustDecFormatter.format(flex_desconto));
		} else {

			banco.TempPed_AddProdutosID(String.valueOf(v_produtoid), String.valueOf(v_linhaid), String.valueOf(v_colunaid));
			banco.TempPed_SetQtdePRodVenda(txtQtde.getText().toString());

			// calculando acrescimo ou desconto
			if (valorfim > v_valor_cad) {
				acrescimo = (valorfim - v_valor_cad) * qtdefim;
			} else {
				desconto = (v_valor_cad - valorfim) * qtdefim;
			}

			Cursor cLista = banco.db.rawQuery("select sum(valor) as total, (select total from vendas where _id = " + v_pedidoid + ") as total_item from titulos where nome like '" + String.valueOf(v_cliente_id) + "'", null);
			if (cLista.moveToFirst()) {
				double totalPendencia = cLista.getDouble(0) + cLista.getDouble(1);
				double limite = 0;

				Cursor cListaLimite = banco.db.rawQuery("select LIMITE from clientes where CPF_CNPJ like '" + String.valueOf(v_cliente_id) + "'", null);
				if (cListaLimite.moveToFirst()) {
					try {
						limite = Double.parseDouble(cListaLimite.getString(0));
					} catch (NumberFormatException e) {
						limite = 0;
					}
					double valorConta = totalPendencia + ((v_valor_cad + acrescimo - desconto) * Double.valueOf(txtQtde.getText().toString()));

					double peso = v_peso * Double.valueOf(txtQtde.getText().toString());

					banco.TB_VENDAS_ITENS_INSERIR(v_pedidoid, v_itemid, Integer.valueOf(String.valueOf(v_produtoid)), Integer.valueOf(String.valueOf(v_unidadeid)), Integer.valueOf(String.valueOf(v_linhaid)), Integer.valueOf(String.valueOf(v_colunaid)), txtDescricao.getText().toString(), Double.valueOf(txtQtde.getText().toString()), acrescimo, desconto, v_valor_cad, flex_acrescimo, flex_desconto, v_percentual_st, v_percentual_ipi, peso, null);
					Toast.makeText(frm_cad_pedido_item.this, "Produto: " + txtDescricao.getText().toString() + " incluso no pedido !!!", Toast.LENGTH_SHORT).show();

					if (valorConta > limite && limite != 0) {
						AlertDialog ad = new AlertDialog.Builder(this).create();
						ad.setCancelable(false); // This blocks the 'BACK'
						// button
						ad.setTitle("SmartMobile");
						ad.setIcon(R.drawable.ico_info);
						ad.setMessage("O limite de cr�dito dispon�vel de R$ " + myCustDecFormatter.format(limite) + " foi atingido. O pedido passar� por an�lise financeira para ser aprovado. ");
						ad.setButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								finish();
							}
						});
						ad.show();
					} else {
						finish();
					}

				}
			}

		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		banco.closeHelper();
	}

	private void CalcTotal() {

		Double acrescimo = 0.00;
		Double desconto = 0.00;
		Double bruto = 0.00;
		Double liquido = 0.00;
		double acrescimoPorcentagem = 0;
		double descontoPorcentagem = 0;

		//aqui � se o usu�rio estiver DIGITANDO no CAMPO VALOR
		if(txtValor.isFocused()) {

			// calcula o valor de acrescimo/desconto
			if ((!txtValor.getText().toString().trim().equals("")) && (!txtQtde.getText().toString().trim().equals(""))) {

				String valorFimString = txtValor.getText().toString();
				valorFimString = valorFimString.replace("..", ".");
				valorFimString = valorFimString.replace(",", ".");
				valorFimString = valorFimString.replace("..", ".");

				Double valorfim = Double.valueOf(valorFimString);
				Double qtdefim = Double.valueOf(txtQtde.getText().toString());

				try {
					txtPesoProduto.setText(myCustDecFormatter.format((v_peso * qtdefim)) + "");
				} catch (Exception e) {
					txtPesoProduto.setText("0");
				}

				// arredondando para 2 casas decimais
				BigDecimal bd = new BigDecimal(v_valor_cad);
				BigDecimal rounded = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
				v_valor_cad = rounded.doubleValue();

				if (valorfim > v_valor_cad) {
					acrescimo = (valorfim - v_valor_cad) * qtdefim;
					if (v_valor_cad == 0) {
						acrescimoPorcentagem = ((valorfim - v_valor_cad) * 100) / valorfim;
					} else {
						acrescimoPorcentagem = ((valorfim - v_valor_cad) * 100) / v_valor_cad;
					}

				} else {
					if (v_valor_cad == 0) {
						desconto = 0.0;
						descontoPorcentagem = 0;
					} else {
						desconto = (v_valor_cad - valorfim) * qtdefim;
						descontoPorcentagem = ((v_valor_cad - valorfim) * 100) / v_valor_cad;
					}
				}

				bruto = v_valor_cad * qtdefim;
				liquido = valorfim * qtdefim;

			}

			txtDesconto.removeTextChangedListener(wtDesconto);
			txtDescontoPorcentagem.removeTextChangedListener(wtPorcentagemDesconto);
			txtAcrescimo.removeTextChangedListener(wtAcrescimo);
			txtAcrescimoPorcentagem.removeTextChangedListener(wtPorcentagemAcrescimo);
			if (valorCampo == null) {
				txtAcrescimo.setText(myCustDecFormatter.format(acrescimo));
				txtDesconto.setText(myCustDecFormatter.format(desconto));
				txtTotal.setText(myCustDecFormatter.format(liquido));

				txtAcrescimoPorcentagem.setText(myCustDecFormatter.format(acrescimoPorcentagem));
				txtDescontoPorcentagem.setText(myCustDecFormatter.format(descontoPorcentagem));
			} else if (valorCampo == txtAcrescimo) {
				txtDesconto.setText(myCustDecFormatter.format(desconto));
				txtTotal.setText(myCustDecFormatter.format(liquido));

				txtAcrescimoPorcentagem.setText(myCustDecFormatter.format(acrescimoPorcentagem));
				txtDescontoPorcentagem.setText(myCustDecFormatter.format(descontoPorcentagem));
			} else if (valorCampo == txtDesconto) {
				txtAcrescimo.setText(myCustDecFormatter.format(acrescimo));
				txtTotal.setText(myCustDecFormatter.format(liquido));

				txtAcrescimoPorcentagem.setText(myCustDecFormatter.format(acrescimoPorcentagem));
				txtDescontoPorcentagem.setText(myCustDecFormatter.format(descontoPorcentagem));
			} else if (valorCampo == txtAcrescimoPorcentagem) {
				txtAcrescimo.setText(myCustDecFormatter.format(acrescimo));
				txtDesconto.setText(myCustDecFormatter.format(desconto));
				txtTotal.setText(myCustDecFormatter.format(liquido));

				txtDescontoPorcentagem.setText(myCustDecFormatter.format(descontoPorcentagem));
			} else if (valorCampo == txtDescontoPorcentagem) {
				txtAcrescimo.setText(myCustDecFormatter.format(acrescimo));
				txtDesconto.setText(myCustDecFormatter.format(desconto));
				txtTotal.setText(myCustDecFormatter.format(liquido));

				txtAcrescimoPorcentagem.setText(myCustDecFormatter.format(acrescimoPorcentagem));
			}

			valorCampo = null;

			txtDescontoPorcentagem.addTextChangedListener(wtPorcentagemDesconto);
			txtDesconto.addTextChangedListener(wtDesconto);
			txtAcrescimo.addTextChangedListener(wtAcrescimo);
			txtAcrescimoPorcentagem.addTextChangedListener(wtPorcentagemAcrescimo);

		}else{

			//aqui � quando o usu�rio digita ACRESCIMO ou DESCONTO
			try{
				//Double qtdefim = Double.valueOf(txtQtde.getText().toString().replace("..", ".").replace(",", "."));
				//Double valorfim = Double.valueOf(txtValor.getText().toString().replace("..", ".").replace(",", "."));
				//Double descontofim = Double.valueOf(txtDesconto.getText().toString().replace("..", ".").replace(",", "."));
				//Double acrescimofim = Double.valueOf(txtAcrescimo.getText().toString().replace("..", ".").replace(",", "."));

				Double qtdefim = Double.valueOf(txtQtde.getText().toString().replace("..", ".").replace(",", "."));
				Double valorfim = Double.valueOf(txtValor.getText().toString().replace("..", ".").replace(",", "."));

				//Log.i("total-double", (myCustDecFormatter.format((qtdefim * valorfim) - descontofim + acrescimofim)));
				//txtTotal.setText(myCustDecFormatter.format((qtdefim * valorfim) - descontofim + acrescimofim));

				txtTotal.setText(myCustDecFormatter.format((qtdefim * valorfim)));

			} catch (Exception e) {
				Log.i("totalERRO",e.getMessage());
			}

		}
	}

	public static String SomenteNumeros(String str) {
		if (str != null) {
			return str.replaceAll("[^0123456789]", "");
		} else {
			return "";
		}
	}

}
