package smart.mobile.consulta.produtos;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.squareup.picasso.Picasso;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.model.ProdutoComparacao;
import smart.mobile.R;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class frm_cons_produtos_adapter extends SimpleCursorAdapter {

	private int TipoLayout = 0; // 0 -- simples 1 -- completo
	private int ProdDetalhar = 0; // produto a mostrar o layout detalhado
	private final String PATH = "/data/data/smart.mobile/";

	private Cursor c;
	private Context context;
	private DB_LocalHost banco;
	private DecimalFormat myCustDecFormatter = new DecimalFormat("#,##0.00");

	private long pedidoId;

	private ArrayList<ProdutoComparacao> produtosAdds;
	private long ListaID;
	private Cursor cursorEstoque;

	public frm_cons_produtos_adapter(Context context, int layout, Cursor c, DB_LocalHost bancoX, String[] from, int[] to, int xTipoLayout, int xProdDetalhar, long pedidoId, ArrayList<ProdutoComparacao> produtosAdds, Long ListaID) {
		super(context, layout, c, from, to);
		this.TipoLayout = xTipoLayout;
		this.ProdDetalhar = xProdDetalhar;

		this.c = c;
		this.banco = bancoX;
		this.context = context;
		this.pedidoId = pedidoId;
		this.produtosAdds = produtosAdds;
		this.ListaID = ListaID;

		if (this.TipoLayout == 2) {
			this.cursorEstoque = banco.db.rawQuery("SELECT PRODUTOID, LINHAID, COLUNAID, UNIDADEID FROM estoque", null);
		}

	}

	public View getView(int pos, View inView, ViewGroup parent) {
		View v = inView;
		this.getCursor().moveToPosition(pos);

		final ViewHolder holder;

		if ((v == null) || (ProdDetalhar > 0)) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if ((ProdDetalhar > 0) && (TipoLayout == 0)) {
				if (getCursor().getInt(getCursor().getColumnIndex("_id")) == ProdDetalhar) {
					TipoLayout = 1;
				} else {
					TipoLayout = 0;
				}
			}

			if (TipoLayout == 0) {
				v = inflater.inflate(R.layout.lay_cons_produtos_simples, parent, false);
			} else if (TipoLayout == 1) {
				v = inflater.inflate(R.layout.lay_cons_produtos, parent, false);
			} else if (TipoLayout == 2) {
				v = inflater.inflate(R.layout.lay_cons_produtos_estoque_lista, parent, false);
			}

			holder = new ViewHolder();

			if (TipoLayout == 2) {
				holder.txtCodigoBarra = (TextView) v.findViewById(R.id.txtCodigoBarra);
				holder.txtCodigoBarra.setText(getCursor().getString(getCursor().getColumnIndex("CODIGO_BARRA")));
			}

			holder.txtDescricao = (TextView) v.findViewById(R.id.txtDescricao);
			holder.txtCodigo = (TextView) v.findViewById(R.id.txtCodigo);
			holder.txtUN = (TextView) v.findViewById(R.id.txtUN);
			holder.txtGrupo = (TextView) v.findViewById(R.id.txtGrupo);
			holder.txtEstoque = (TextView) v.findViewById(R.id.txtEstoque);
			try {
			holder.lblST = (TextView) v.findViewById(R.id.lblST);
			} catch (Exception e) {

			}
			holder.txtST = (TextView) v.findViewById(R.id.txtST);
			holder.txtValor = (TextView) v.findViewById(R.id.txtValor);
			holder.txtCodigoInterno = (TextView) v.findViewById(R.id.txtCodigoInterno);

			holder.txtUN1 = (TextView) v.findViewById(R.id.txtUN1);
			holder.txtGrupo1 = (TextView) v.findViewById(R.id.txtGrupo1);
			holder.txtEstoque1 = (TextView) v.findViewById(R.id.txtEstoque1);
			try {
			holder.lblST1 = (TextView) v.findViewById(R.id.lblST1);
			} catch (Exception e) {

			}
			holder.txtST1 = (TextView) v.findViewById(R.id.txtST1);
			holder.txtCodigoInterno1 = (TextView) v.findViewById(R.id.txtCodigoInterno1);
			try {
				holder.txtMarca = (TextView) v.findViewById(R.id.txtMarca);
			} catch (Exception e) {

			}

			holder.imgCheck = (ImageView) v.findViewById(R.id.imgCheck);
			holder.imgFoto = (ImageView) v.findViewById(R.id.imagemProduto);
			v.setTag(holder);

		} else {
			holder = (ViewHolder) v.getTag();
		}

		// caso exitir dados de grade
		if ((!getCursor().getString(getCursor().getColumnIndex("LINHA")).trim().equals("")) || (!getCursor().getString(getCursor().getColumnIndex("COLUNA")).trim().equals(""))) {
			holder.txtDescricao.setText(getCursor().getString(getCursor().getColumnIndex("DESCRICAO")) + "\n" + "Grade >> "+getCursor().getString(getCursor().getColumnIndex("LINHA")) + " : " + getCursor().getString(getCursor().getColumnIndex("COLUNA")));
		} else {
			holder.txtDescricao.setText(getCursor().getString(getCursor().getColumnIndex("DESCRICAO")));
		}

		holder.txtValor.setText(myCustDecFormatter.format(getCursor().getDouble(getCursor().getColumnIndex("VALOR"))));
		// 22/09/2016 - ref. chamado 5998 - adicionado parametro da 'Lista de Preço' para exibir já o preço final na Consulta de Produtos
		if(ListaID > 0){
			try{
				Log.i("ref.5898--z--ListaID ", String.valueOf(ListaID));

				Double v_valor = getCursor().getDouble(getCursor().getColumnIndex("VALOR"));

				String tipoListaXX = "";
				Cursor cLista = banco.db.rawQuery("select listaid,tipo_lista,percentual from listas_precos where listaid = " + String.valueOf(ListaID), null);
				if (cLista.moveToFirst()) {

					tipoListaXX = cLista.getString(1);
					if (cLista.getString(1).equals("D")) { // DESCONTO
						v_valor = v_valor - ((v_valor * cLista.getDouble(2)) / 100);
					} else if (cLista.getString(1).equals("A")) { //ACRESCIMO
						v_valor = v_valor + ((v_valor * cLista.getDouble(2)) / 100);
					} else if (cLista.getString(1).equals("X")) { // PERSONALIZADA
						Cursor cListaX = banco.db.rawQuery("select tipo,percentual from listas_precos_produtos where listaid = " + String.valueOf(ListaID) + " and produtoid = " + getCursor().getString(getCursor().getColumnIndex("PRODUTOID")), null);
						if (cListaX.moveToFirst()) {
							tipoListaXX = cListaX.getString(0);
							if (cListaX.getString(0).equals("D")) { // DESCONTO
								v_valor = v_valor - ((v_valor * cListaX.getDouble(1)) / 100);
							} else if (cListaX.getString(0).equals("A")) { // ACRESCIMO
								v_valor = v_valor + ((v_valor * cListaX.getDouble(1)) / 100);
							}
						}
					}

					//holder.txtValor.setText(holder.txtValor.getText() + ">" + myCustDecFormatter.format(v_valor));
					holder.txtValor.setText(myCustDecFormatter.format(v_valor));

				}

			} catch (Exception ex) {
				Log.i("ref.5898--0--ListaID-erro", ex.getMessage());
		}

		}

		if (TipoLayout == 0) {// layout simples

			//
			holder.txtDescricao.setText(holder.txtDescricao.getText().toString() + " " + getCursor().getString(getCursor().getColumnIndex("UND")));

			if (getCursor().getDouble(getCursor().getColumnIndex("ESTOQUE")) <= 0) {
				holder.txtDescricao.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
			} else {
				holder.txtDescricao.setTextColor(context.getResources().getColor(R.color.all_black));
			}

		} else {
			if (TipoLayout == 1 || TipoLayout == 2) {// layout completo

				holder.txtCodigo.setText(getCursor().getString(getCursor().getColumnIndex("PRODUTOID")));

				LinearLayout codInterno = (LinearLayout) v.findViewById(R.id.codigoInterno);
				LinearLayout codInterno1 = (LinearLayout) v.findViewById(R.id.codigoInterno1);

				if (!getCursor().getString(getCursor().getColumnIndex("PRODUTOID")).equals(getCursor().getString(getCursor().getColumnIndex("CODIGO")))) {
					codInterno.setVisibility(View.VISIBLE);
					holder.txtCodigoInterno.setText(getCursor().getString(getCursor().getColumnIndex("CODIGO")));

					codInterno1.setVisibility(View.VISIBLE);
					holder.txtCodigoInterno1.setText(getCursor().getString(getCursor().getColumnIndex("CODIGO")));
				} else {
					codInterno.setVisibility(View.GONE);
					codInterno1.setVisibility(View.VISIBLE);
				}

				holder.txtUN.setText(getCursor().getString(getCursor().getColumnIndex("UND")));
				holder.txtUN1.setText(getCursor().getString(getCursor().getColumnIndex("UND")));

				holder.txtGrupo.setText(getCursor().getString(getCursor().getColumnIndex("GRUPO")));
				holder.txtGrupo1.setText(getCursor().getString(getCursor().getColumnIndex("GRUPO")));

				try {
					holder.txtMarca.setText(getCursor().getString(getCursor().getColumnIndex("MARCA")));
				} catch (Exception e) {

				}

				// caso já tiver o cliente selecionado
				if (getCursor().getDouble(getCursor().getColumnIndex("VALOR")) > 0) {

					// caso tenha imposto de IPI
					Double ValorIPI = ((getCursor().getDouble(getCursor().getColumnIndex("VALOR")) * getCursor().getDouble(getCursor().getColumnIndex("ALIQUOTA_IPI"))) / 100);
					Double ValorIcmsSub = 0.00;

					String UF = "Todas";

					// caso tenha imposto de SUBs. TRIBUTARIA [valida apenas quando estiver selecionado o CLIENTE+UF]
					if (getCursor().getString(getCursor().getColumnIndex("UF")) != null) {
						if (!getCursor().getString(getCursor().getColumnIndex("UF")).equals("")) {

							UF = getCursor().getString(getCursor().getColumnIndex("UF"));

							if (getCursor().getString(getCursor().getColumnIndex("ALIQUOTA_UF")) != null) {

								Double ValorICMS = ((getCursor().getDouble(getCursor().getColumnIndex("VALOR")) * getCursor().getDouble(getCursor().getColumnIndex("ALIQUOTA_UF"))) / 100);

								Double BaseIcms = getCursor().getDouble(getCursor().getColumnIndex("VALOR"));
								Double BaseIcmsSub = (BaseIcms + ValorIPI) + (((BaseIcms + ValorIPI) * getCursor().getDouble(getCursor().getColumnIndex("SUBS_IVA"))) / 100);
								ValorIcmsSub = ((BaseIcmsSub * getCursor().getDouble(getCursor().getColumnIndex("SUBS_ALIQ"))) / 100) - ValorICMS;

								//holder.txtST.setText(getCursor().getString(getCursor().getColumnIndex("UF")) + ": " + myCustDecFormatter.format(getCursor().getDouble(getCursor().getColumnIndex("VALOR")) + ValorIcmsSub));
								//holder.txtST1.setText(getCursor().getString(getCursor().getColumnIndex("UF")) + ": " + myCustDecFormatter.format(getCursor().getDouble(getCursor().getColumnIndex("VALOR")) + ValorIcmsSub));
							}
						}
					}

					try {

						holder.lblST.setText("R$+ST+IPI:");
						holder.lblST1.setText("R$+ST+IPI:");

					} catch (Exception e) {

					}

					if ((ValorIPI > 0) || (ValorIcmsSub > 0)) {
						//holder.txtST.setText("UF>"+UF + ": " + myCustDecFormatter.format(getCursor().getDouble(getCursor().getColumnIndex("VALOR")) + ValorIcmsSub + ValorIPI));
						//holder.txtST1.setText("UF>"+UF + ": " + myCustDecFormatter.format(getCursor().getDouble(getCursor().getColumnIndex("VALOR")) + ValorIcmsSub + ValorIPI));

						holder.txtST.setText(myCustDecFormatter.format(getCursor().getDouble(getCursor().getColumnIndex("VALOR")) + ValorIcmsSub + ValorIPI));
						holder.txtST1.setText(myCustDecFormatter.format(getCursor().getDouble(getCursor().getColumnIndex("VALOR")) + ValorIcmsSub + ValorIPI));


						try {

							if (UF.equalsIgnoreCase("ZZ")) {
								UF = "Todas";
							}

							holder.lblST.setText("R$+ST+IPI(UF>" + UF + "):");
							holder.lblST1.setText("R$+ST+IPI(UF>" + UF + "):");

						} catch (Exception e) {

						}

					} else {
						holder.txtST.setText("");
						holder.txtST1.setText("");
					}

				} else {
					holder.txtST.setText("");
					holder.txtST1.setText("");
				}

				holder.txtEstoque.setText(myCustDecFormatter.format(getCursor().getDouble(getCursor().getColumnIndex("ESTOQUE"))));
				holder.txtEstoque1.setText(myCustDecFormatter.format(getCursor().getDouble(getCursor().getColumnIndex("ESTOQUE"))));

				if (getCursor().getDouble(getCursor().getColumnIndex("VALOR")) <= 0) {
					// txtValor.setBackgroundColor(context.getResources().getColor(R.color.solid_red));
					holder.txtValor.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
				} else {
					holder.txtValor.setTextColor(context.getResources().getColor(R.color.all_black));
				}

				if (getCursor().getDouble(getCursor().getColumnIndex("ESTOQUE")) <= 0) {
					// txtEstoque.setBackgroundColor(context.getResources().getColor(R.color.solid_red));
					holder.txtDescricao.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
					holder.txtEstoque.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
					holder.txtEstoque1.setTextColor(context.getResources().getColor(R.color.cor_vermelho));
				} else {
					holder.txtDescricao.setTextColor(context.getResources().getColor(R.color.all_black));
					holder.txtEstoque.setTextColor(context.getResources().getColor(R.color.all_black));
					holder.txtEstoque1.setTextColor(context.getResources().getColor(R.color.all_black));
				}

				try {

					File imageProduto = new File(PATH + "imagens/" + getCursor().getString(getCursor().getColumnIndex("PRODUTOID")) + "_1.jpg");

					if (imageProduto.isFile()) {
						Picasso.with(context).load(imageProduto).centerCrop().resize(50, 50).skipMemoryCache().into(holder.imgFoto);
						holder.imgFoto.setVisibility(View.VISIBLE);

						//12/05/2016 - JAKSON GAVA - ref. analise 247 : ao clicar na foto sempre exibe as imagens
						holder.imgFoto.setClickable(true);
						holder.imgFoto.setOnClickListener(null);
						holder.imgFoto.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								//Toast.makeText(context,	"The favorite list would appear on clicking this icon",Toast.LENGTH_LONG).show();

								Intent intent = new Intent(context, frm_full_image.class);
								Bundle params = new Bundle();

								//Log.w("imgProdutoID", getCursor().getString(getCursor().getColumnIndex("PRODUTOID")));
								//Log.w("imgProdutoID2", holder.txtCodigo.getText().toString());

								params.putString("idProduto",  holder.txtCodigo.getText().toString().trim() + "");
								intent.putExtras(params);
								context.startActivity(intent);
							}
						});

					} else {
						holder.imgFoto.setVisibility(View.GONE);
					}

					// carrega a imagem

				} catch (Exception e) {

				}
			}
		}

		// validação de 'produto incluso no pedido'
		if (banco.TempPed_ExisteProdutosID(getCursor().getString(getCursor().getColumnIndex("PRODUTOID")), getCursor().getString(getCursor().getColumnIndex("LINHAID")), getCursor().getString(getCursor().getColumnIndex("COLUNAID")))) {
			v.setBackgroundColor(context.getResources().getColor(R.color.fundoProdutoSel));
		} else {
			// imgCheck.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ico_check0));
			boolean jaComprado = false;
			if (produtosAdds != null) {
				for (int i = 0; i < produtosAdds.size(); i++) {
					ProdutoComparacao produto = produtosAdds.get(i);

					if (produto.getProdutoId() == getCursor().getInt(getCursor().getColumnIndex("PRODUTOID")) && produto.getColunaId() == getCursor().getInt(getCursor().getColumnIndex("COLUNAID")) && produto.getLinhaId() == getCursor().getInt(getCursor().getColumnIndex("LINHAID")) && produto.getUnidadeId() == getCursor().getInt(getCursor().getColumnIndex("UNIDADEID"))) {

						LinearLayout lay = (LinearLayout) v.findViewById(R.id.llProdutoJaAdd);
						lay.setVisibility(v.VISIBLE);

						holder.qtd = (TextView) v.findViewById(R.id.qtdeJaAdd);
						holder.data = (TextView) v.findViewById(R.id.dataJaAdd);

						holder.qtd.setText(produto.getQtde() + "");
						holder.data.setText(produto.getData());

						jaComprado = true;
						break;
					}
				}
			}

			if (jaComprado) {
				v.setBackgroundColor(context.getResources().getColor(R.color.fundoProdutoJaAdd));
			} else {
				v.setBackgroundColor(context.getResources().getColor(R.color.all_white));
			}
		}

		if (TipoLayout == 2) {
			cursorEstoque.moveToFirst();
			// PRODUTOID, LINHAID, COLUNAID, UNIDADEID
			for (int i = 0; i < cursorEstoque.getCount(); i++) {
				cursorEstoque.moveToPosition(i);
				if (cursorEstoque.getInt(cursorEstoque.getColumnIndex("PRODUTOID")) == getCursor().getInt(getCursor().getColumnIndex("PRODUTOID")) && cursorEstoque.getInt(cursorEstoque.getColumnIndex("LINHAID")) == getCursor().getInt(getCursor().getColumnIndex("LINHAID")) && cursorEstoque.getInt(cursorEstoque.getColumnIndex("COLUNAID")) == getCursor().getInt(getCursor().getColumnIndex("COLUNAID")) && cursorEstoque.getInt(cursorEstoque.getColumnIndex("UNIDADEID")) == getCursor().getInt(getCursor().getColumnIndex("UNIDADEID"))) {
					v.setBackgroundColor(context.getResources().getColor(R.color.fundoProdutoJaAdd));
					break;
				} else {
					v.setBackgroundColor(context.getResources().getColor(R.color.all_white));
				}
			}
		}

		if (getCursor().getInt(getCursor().getColumnIndex("DESTAQUE")) == 1) {
			holder.imgCheck.setVisibility(View.VISIBLE);
			holder.imgCheck.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.star));
		} else {
			holder.imgCheck.setVisibility(View.GONE);
		}

		return v;
	}

	static class ViewHolder {
		TextView qtd, data, txLista, txDescricao, txtDescricao, txtCodigo, txtUN, txtGrupo, txtEstoque, lblST, txtST, txtValor, txtCodigoInterno, txtUN1, txtGrupo1, txtEstoque1, lblST1, txtST1, txtCodigoInterno1, txtCodigoBarra, txtMarca;
		ImageView imgCheck, imgFoto;
	}

	public long getPedidoId() {
		return pedidoId;
	}

	public void setPedidoId(long pedidoId) {
		this.pedidoId = pedidoId;
	}

	private double valorLista(int produtoId, double valor) {
		double v_valor = valor;
		Cursor cLista = banco.db.rawQuery("select vendas.listaid,tipo_lista,percentual, clientes.CPF_CNPJ from vendas join listas_precos on vendas.listaid = listas_precos.listaid JOIN clientes on vendas.CPF_CNPJ = clientes.CPF_CNPJ where vendas._id = " + String.valueOf(pedidoId), null);
		if (cLista.moveToFirst()) {

			String listaid = cLista.getString(0);

			if (!listaid.equals("0")) {

				if (cLista.getString(1).equals("D")) { // DESCONTO

					v_valor = v_valor - ((v_valor * cLista.getDouble(2)) / 100);

				} else if (cLista.getString(1).equals("A")) { // ACRESCIMO

					// SE FOR A LISTA COD 14 = +20% NA INCAS CONSIDERA QUE PODE
					// DAR 20% DE DESCONTO NOS PRODUTOS
					if (banco.Banco.toUpperCase().trim().equals("INCAS") && cLista.getString(0).equalsIgnoreCase("14")) {

						v_valor = v_valor + ((v_valor * cLista.getDouble(2)) / 100);

					} else {

						v_valor = v_valor + ((v_valor * cLista.getDouble(2)) / 100);

					}

				} else if (cLista.getString(1).equals("X")) { // PERSONALIZADA

					Cursor cListaX = banco.db.rawQuery("select tipo,percentual from listas_precos_produtos where listaid = " + listaid + " and produtoid = " + String.valueOf(produtoId), null);
					if (cListaX.moveToFirst()) {

						if (cListaX.getString(0).equals("D")) { // DESCONTO
							v_valor = v_valor - ((v_valor * cListaX.getDouble(1)) / 100);

						} else if (cListaX.getString(0).equals("A")) { // ACRESCIMO
							v_valor = v_valor + ((v_valor * cListaX.getDouble(1)) / 100);
						}

					}

				}
			}
		}
		return v_valor;
	}
}