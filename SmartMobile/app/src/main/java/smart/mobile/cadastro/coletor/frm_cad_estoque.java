package smart.mobile.cadastro.coletor;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.PrincipalClasse;
import smart.mobile.R;
import smart.mobile.consulta.produtos.frm_cons_produtos;
import smart.mobile.model.Estoque;
import smart.mobile.utils.ConfiguracaoInicialTela;
import smart.mobile.utils.error.ErroGeralController;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class frm_cad_estoque extends Activity {

	private static final int MN_EXCLUIR = 0;
	private Context context;
	private DB_LocalHost banco;
	private EditText txtFiltro;
	private SimpleCursorAdapter adpter;
	private Cursor cursor;
	private ListView lvEstoque;
	private TextView txtDescricao;
	private TextView txtUnd;
	private EditText txtEstoque;
	private Estoque estoque;
	private Button btAdd;
	private ImageButton btFiltro;
	private PrincipalClasse aplication;
	private EditText txtObs;
	private EditText txtAlterada;
	private EditText txtAcrescimo;
	private EditText txtDecrescimo;
	private EditText campoAlterando;
	private ImageView btColetor;

	public frm_cad_estoque() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(Bundle icicle) {

		context = this;

		super.onCreate(icicle);
		setContentView(R.layout.activity_ctela_base);
		setTitle("SmartMobile - Coletor de Estoque");

		ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.layoutpadrao_estoque, false);

		RelativeLayout footer = (RelativeLayout) findViewById(R.id.FooterEsconder);
		footer.setVisibility(View.GONE);

		// carrega dados do banco
		this.banco = new DB_LocalHost(this);

		ErroGeralController erro = new ErroGeralController(this, banco);

		estoque = new Estoque();

		aplication = (PrincipalClasse) getApplication();
		aplication.setEstoqueProduto(null);

		txtFiltro = (EditText) findViewById(R.id.edtFiltro);
		lvEstoque = (ListView) findViewById(R.id.listViewEstoque);
		txtDescricao = (TextView) findViewById(R.id.descricaoProdutoAdd);
		txtUnd = (TextView) findViewById(R.id.edtUni);
		txtEstoque = (EditText) findViewById(R.id.edtQtdeEstoque);
		txtObs = (EditText) findViewById(R.id.edtObs);

		txtAlterada = (EditText) findViewById(R.id.edtQtdeAlterada);
		txtAcrescimo = (EditText) findViewById(R.id.edtAcrescimo);
		txtDecrescimo = (EditText) findViewById(R.id.edtDecrescimo);

		btFiltro = (ImageButton) findViewById(R.id.btnFiltro);
		btAdd = (Button) findViewById(R.id.btnAdd);
		btColetor = (ImageView) findViewById(R.id.btColetor);

//		setarFoco(txtAcrescimo);
//		setarFoco(txtDecrescimo);
//		setarFoco(txtAlterada);

		txtFiltro.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					txtFiltro.postDelayed(new Runnable() {
						@Override
						public void run() {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(txtFiltro.getWindowToken(), 0);
						}
					}, 100);
				} else {
					txtFiltro.postDelayed(new Runnable() {
						@Override
						public void run() {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.showSoftInput(txtFiltro, InputMethodManager.SHOW_FORCED);
						}
					}, 100);
				}

			}
		});
		txtObs.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					txtObs.postDelayed(new Runnable() {
						@Override
						public void run() {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(txtObs.getWindowToken(), 0);
						}
					}, 100);
				} else {
					txtObs.postDelayed(new Runnable() {
						@Override
						public void run() {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.showSoftInput(txtObs, InputMethodManager.SHOW_FORCED);
						}
					}, 100);
				}

			}
		});

		txtAcrescimo.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				if (campoAlterando != null) {
					if (campoAlterando != txtAlterada) {
						txtAlterada.setText(acrescimoDecrescimoValorFinal() + "");
					}
				}
			}
		});

		txtDecrescimo.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				if (campoAlterando != null) {
					if (campoAlterando != txtAlterada) {
						txtAlterada.setText(acrescimoDecrescimoValorFinal() + "");
					}
				}

			}
		});

		txtDecrescimo.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					txtDecrescimo.postDelayed(new Runnable() {
						@Override
						public void run() {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.showSoftInput(txtDecrescimo, InputMethodManager.SHOW_FORCED);

						}
					}, 100);

					campoAlterando = txtDecrescimo;
					txtAcrescimo.clearFocus();
					txtAlterada.clearFocus();
				} else {
					txtDecrescimo.postDelayed(new Runnable() {
						@Override
						public void run() {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(txtDecrescimo.getWindowToken(), 0);
						}
					}, 100);
				}

			}
		});

		txtAcrescimo.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				
				if (hasFocus) {
					txtAcrescimo.postDelayed(new Runnable() {
						@Override
						public void run() {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.showSoftInput(txtAcrescimo, InputMethodManager.SHOW_FORCED);

						}
					}, 100);
					
					campoAlterando = txtAcrescimo;
					txtDecrescimo.clearFocus();
					txtAlterada.clearFocus();
				} else {
					txtAcrescimo.postDelayed(new Runnable() {
						@Override
						public void run() {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(txtAcrescimo.getWindowToken(), 0);
						}
					}, 100);
				}
			}
		});

		txtAlterada.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				if (arg1) {
					txtAlterada.postDelayed(new Runnable() {
						@Override
						public void run() {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.showSoftInput(txtAlterada, InputMethodManager.SHOW_FORCED);

						}
					}, 100);
					
					
					
					campoAlterando = txtAlterada;
					txtAcrescimo.clearFocus();
					txtDecrescimo.clearFocus();
				} else {
					txtAlterada.postDelayed(new Runnable() {
						@Override
						public void run() {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(txtAlterada.getWindowToken(), 0);
						}
					}, 100);
				}

			}
		});

		txtAlterada.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (campoAlterando == txtAlterada) {
					acrescimoDecrescimoValorFinalAlterada();
				}
			}
		});

		lvEstoque.setSelected(true);
		lvEstoque.setClickable(true);
		lvEstoque.setFastScrollEnabled(true);
		lvEstoque.setDrawSelectorOnTop(true);
		lvEstoque.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final long idSql = id;
				CharSequence[] items = new CharSequence[]{"Alterar", "Excluir"};
				AlertDialog.Builder builder = new AlertDialog.Builder(frm_cad_estoque.this);
				builder.setTitle("Estoque - Opções");
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						if (item == 0) {

							Cursor cursorProduto = banco.db.rawQuery("SELECT estoque._id as _id, estoque.OBS as OBS, produtos.CODIGO_BARRA, produtos.DESCRICAO, produtos.UND, produtos.PRODUTOID, produtos.LINHAID, produtos.COLUNAID, produtos.UNIDADEID, estoque.ACRESCIMO, estoque.DECRESCIMO FROM estoque inner join produtos on produtos.PRODUTOID = estoque.PRODUTOID AND produtos.LINHAID = estoque.LINHAID AND produtos.COLUNAID = estoque.COLUNAID AND produtos.UNIDADEID = estoque.UNIDADEID WHERE estoque._id = " + idSql, null);
							cursorProduto.moveToFirst();
							if (cursorProduto.getCount() == 1) {
								Estoque estoquee = new Estoque();
								estoquee.setId(cursorProduto.getLong((cursorProduto.getColumnIndex("_id"))));
								estoquee.setColunaId(cursorProduto.getInt(cursorProduto.getColumnIndex("COLUNAID")));
								estoquee.setLinhaId(cursorProduto.getInt(cursorProduto.getColumnIndex("LINHAID")));
								estoquee.setProdutoId(cursorProduto.getInt(cursorProduto.getColumnIndex("PRODUTOID")));
								estoquee.setUnidadeId(cursorProduto.getInt(cursorProduto.getColumnIndex("UNIDADEID")));
								estoquee.setDescricao(cursorProduto.getString(cursorProduto.getColumnIndex("DESCRICAO")));
								estoquee.setUnd(cursorProduto.getString(cursorProduto.getColumnIndex("UND")));
								estoquee.setObs(cursorProduto.getString(cursorProduto.getColumnIndex("OBS")));
								estoquee.setAcrescimo(cursorProduto.getDouble(cursorProduto.getColumnIndex("ACRESCIMO")));
								estoquee.setDecrescimo(cursorProduto.getDouble(cursorProduto.getColumnIndex("DECRESCIMO")));

								txtFiltro.setText(cursorProduto.getString(cursorProduto.getColumnIndex("CODIGO_BARRA")) + "     ");

								estoque = estoquee;

								alterarProduto(estoquee);
								selecionarAcrecimo();
							}

						} else if (item == 1) {
							SQLiteStatement insertStmt = banco.db.compileStatement("DELETE FROM ESTOQUE where _id = " + idSql);
							insertStmt.execute();
							CarregarDados();
						}

					}
				});
				AlertDialog alert = builder.create();
				alert.show();

			}
		});

		btFiltro.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(txtEstoque.getWindowToken(), 0);
				Intent intent = new Intent(frm_cad_estoque.this, frm_cons_produtos.class);
				Bundle c = new Bundle();
				c.putInt("tipoLista", 2);
				intent.putExtras(c);
				startActivity(intent);

			}
		});

		btAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!txtEstoque.getText().toString().isEmpty()) {
					estoque.setEstoque(Double.parseDouble(txtEstoque.getText().toString()));
					estoque.setAcrescimo(Double.parseDouble(txtAcrescimo.getText().toString()));
					estoque.setDecrescimo(Double.parseDouble(txtDecrescimo.getText().toString()));
					estoque.setObs(txtObs.getText().toString());
					adicionarEstoque(estoque);
					CarregarDados();
					limparDados();
					txtFiltro.setText("");
					txtFiltro.requestFocus();
				}
			}
		});

		btColetor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(context);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.lay_dialog_estoque);
				dialog.setCancelable(false);

				final EditText contagem = (EditText) dialog.findViewById(R.id.txtDialogContagem);
				final EditText cod = (EditText) dialog.findViewById(R.id.txtDialogCod);
				final TextView descricao = (TextView) dialog.findViewById(R.id.txtDialogDesc);

				cod.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
						// TODO Auto-generated method stub

					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
						// TODO Auto-generated method stub

					}

					@Override
					public void afterTextChanged(Editable arg0) {
						int tamanhoValor = txtFiltro.getText().toString().trim().length();
						if (arg0.toString().length() == tamanhoValor) {
							if (arg0.toString().trim().equals(txtFiltro.getText().toString().trim())) {

								double contagemValor = Double.parseDouble(contagem.getText().toString());
								contagem.setText((++contagemValor) + "");

								arg0.clear();

							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(frm_cad_estoque.this);
								builder.setMessage("O produto escaneado não é o [" + txtDescricao.getText().toString() + "]").setCancelable(true).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialogg, int id) {
										dialogg.dismiss();
									}
								}).setTitle("Coletor");

								AlertDialog alert = builder.create();
								alert.show();
							}
						}

					}
				});

				Button btCancelar = (Button) dialog.findViewById(R.id.btCancelar);
				btCancelar.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
					}
				});

				Button btSalvar = (Button) dialog.findViewById(R.id.btSalvar);
				btSalvar.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						campoAlterando = txtAlterada;
						txtAlterada.setText(contagem.getText().toString());
						dialog.dismiss();

					}

				});

				if (!txtFiltro.getText().toString().trim().isEmpty() && !txtDescricao.getText().toString().trim().isEmpty()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(frm_cad_estoque.this);
					builder.setMessage("Deseja iniciar a contagem com qtde = 1?").setCancelable(true).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogg, int id) {
							contagem.setText("1.0");
							dialogg.dismiss();
							dialog.show();
						}
					}).setNegativeButton("Não", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialogg, int which) {
							contagem.setText(txtAlterada.getText().toString());
							dialogg.dismiss();
							dialog.show();

						}
					}).setTitle("Coletor");

					descricao.setText(txtDescricao.getText().toString());

					AlertDialog alert = builder.create();
					alert.show();

				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(frm_cad_estoque.this);
					builder.setMessage("É necessário ter um produto selecionado.").setCancelable(true).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogg, int id) {
							dialogg.dismiss();
						}
					}).setTitle("Coletor");

					AlertDialog alert = builder.create();
					alert.show();
				}

			}
		});

		txtFiltro.setHint("Cód. Barras");
		txtFiltro.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			public void afterTextChanged(Editable s) {
				final Editable ss = s;

				// CarregaDados(false, 0);
				String valor = s.toString().trim();

				if (s.toString().trim().equals("")) {
					DefineTeclado();
				}
				if (s.toString().length() <= 14) {
					if (valor.length() == 14 || valor.length() == 13 || valor.length() == 12 || valor.length() == 8) {
						final Cursor cursorProduto = banco.db.rawQuery("SELECT produtos._id, estoque._id as IDESTOQUE, produtos.DESCRICAO, produtos.UND, produtos.PRODUTOID, produtos.LINHAID, produtos.COLUNAID, produtos.UNIDADEID, produtos.ESTOQUE, estoque.ACRESCIMO, estoque.DECRESCIMO FROM produtos left join estoque on produtos.PRODUTOID = estoque.PRODUTOID AND produtos.LINHAID = estoque.LINHAID AND produtos.COLUNAID = estoque.COLUNAID AND produtos.UNIDADEID = estoque.UNIDADEID WHERE produtos.FATOR = 1 AND produtos.CODIGO_BARRA like '" + valor + "'", null);
						cursorProduto.moveToFirst();
						if (cursorProduto.getCount() == 1) {
							Cursor cursorEstoque = banco.db.rawQuery("SELECT _id, PRODUTOID, LINHAID, COLUNAID, UNIDADEID FROM estoque WHERE estoque.UNIDADEID = " + cursorProduto.getString(cursorProduto.getColumnIndex("UNIDADEID")) + " AND estoque.COLUNAID = " + cursorProduto.getString(cursorProduto.getColumnIndex("COLUNAID")) + " AND estoque.LINHAID = " + cursorProduto.getString(cursorProduto.getColumnIndex("LINHAID")) + " AND estoque.PRODUTOID = " + cursorProduto.getString(cursorProduto.getColumnIndex("PRODUTOID")), null);
							cursorEstoque.moveToFirst();

							if (cursorEstoque.getCount() != 0) {
								AlertDialog.Builder builder = new AlertDialog.Builder(frm_cad_estoque.this);
								builder.setTitle("Coletor de dados").setMessage("Produto já adicionado na lista, deseja edita-lo?!").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										aletarProdutoPopup(cursorProduto);
										dialog.dismiss();
									}
								}).setNegativeButton("Não", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										ss.clear();
										dialog.dismiss();
									}
								});
								AlertDialog alert = builder.create();
								alert.show();
							} else {
								aletarProdutoPopup(cursorProduto);
							}

						} else if (cursorProduto.getCount() > 1) {
							Intent intent = new Intent(frm_cad_estoque.this, frm_cons_produtos.class);
							estoque = null;
							Bundle c = new Bundle();
							c.putInt("tipoLista", 2);
							c.putString("codigoBarra", s.toString());
							intent.putExtras(c);
							startActivity(intent);
						} else {
							if (s.toString().length() == 14) {
								AlertDialog.Builder builder = new AlertDialog.Builder(frm_cad_estoque.this);
								builder.setTitle("Busca de Produtos").setMessage("Produto não encontrado!").setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.dismiss();
									}
								});
								AlertDialog alert = builder.create();
								alert.show();
							}
						}

					} else {
						limparDados();
					}
				}

			}
		});

		CarregarDados();

	}

	private void aletarProdutoPopup(Cursor cursorProduto){
		cursorProduto.moveToFirst();
		estoque = new Estoque();
		estoque.setColunaId(cursorProduto.getInt(cursorProduto.getColumnIndex("COLUNAID")));
		estoque.setLinhaId(cursorProduto.getInt(cursorProduto.getColumnIndex("LINHAID")));
		estoque.setProdutoId(cursorProduto.getInt(cursorProduto.getColumnIndex("PRODUTOID")));
		estoque.setUnidadeId(cursorProduto.getInt(cursorProduto.getColumnIndex("UNIDADEID")));
		estoque.setDescricao(cursorProduto.getString(cursorProduto.getColumnIndex("DESCRICAO")));
		estoque.setUnd(cursorProduto.getString(cursorProduto.getColumnIndex("UND")));
		estoque.setEstoque(cursorProduto.getDouble(cursorProduto.getColumnIndex("ESTOQUE")));
		estoque.setAcrescimo(cursorProduto.getDouble(cursorProduto.getColumnIndex("ACRESCIMO")));
		estoque.setDecrescimo(cursorProduto.getDouble(cursorProduto.getColumnIndex("DECRESCIMO")));
		if (cursorProduto.getInt(cursorProduto.getColumnIndex("IDESTOQUE")) > 0) {
			estoque.setId(cursorProduto.getInt(cursorProduto.getColumnIndex("IDESTOQUE")));
		}

		alterarProduto(estoque);

		selecionarAcrecimo();
	}

	private double acrescimoDecrescimoValorFinal() {
		String acrescimoRetorno = txtAcrescimo.getText().toString().isEmpty() ? "0.0" : txtAcrescimo.getText().toString();
		String decrescimoRetorno = txtDecrescimo.getText().toString().isEmpty() ? "0.0" : txtDecrescimo.getText().toString();
		return (estoque.getEstoque() + Double.parseDouble(acrescimoRetorno)) - Double.parseDouble(decrescimoRetorno);

	}

	private void acrescimoDecrescimoValorFinalAlterada() {
		double alterada;
		try {
			alterada = Double.parseDouble(txtAlterada.getText().toString());
		} catch (NumberFormatException e) {
			alterada = 0;
		}
		double finalValor;
		if (estoque.getEstoque() < alterada) {
			finalValor = alterada - estoque.getEstoque();
			txtAcrescimo.setText(finalValor + "");
			txtDecrescimo.setText("0.0");
		} else {
			finalValor = estoque.getEstoque() - alterada;
			txtDecrescimo.setText(finalValor + "");
			txtAcrescimo.setText("0.0");
		}
	}

//	private void setarFoco(final EditText edit) {
//
//		edit.setOnFocusChangeListener(new OnFocusChangeListener() {
//
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				if (!hasFocus) {
//					edit.postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//							imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
//						}
//					}, 100);
//				} else {
//					edit.postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//							imm.showSoftInput(edit, InputMethodManager.SHOW_FORCED);
//
//						}
//					}, 100);
//					edit.selectAll();
//				}
//			}
//		});
//
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemExcluir = menu.add(0, MN_EXCLUIR, Menu.NONE, "Excluir Lista");

		// Assign icons.
		// Assign icons
		// itemSobre.setIcon(R.drawable.ico_info);
		// itemOpcoes.setIcon(R.drawable.ico_opcoes);

		// Allocate shortcuts to each of them.
		itemExcluir.setShortcut('0', 's');

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {

		case MN_EXCLUIR:
			if (!((Activity) context).isFinishing()) {
				// show dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(frm_cad_estoque.this);
				builder.setMessage("Atenção!! Todos os itens da lista serão excluidos. Deseja continuar?").setCancelable(true).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						String where = "_id IS NOT NULL";
						banco.db.delete("ESTOQUE", where, null);
						CarregarDados();
					}
				}).setNegativeButton("Não", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).setTitle("SmartMobile - Excluir Lista").setIcon(R.drawable.ico_warning);

				AlertDialog alert = builder.create();
				alert.show();
			}

		default:
			break;
		}
		return false;
	}

	private void selecionarAcrecimo() {

		txtAlterada.requestFocus();

		txtAlterada.selectAll();

	}

	private void limparDados() {
		campoAlterando = null;
		txtEstoque.setText("");
		txtDescricao.setText("");
		txtUnd.setText("");
		txtObs.setText("");
		txtAcrescimo.setText("");
		txtDecrescimo.setText("");
		txtAlterada.setText("");
		
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		DefineTeclado();

		if (aplication.getEstoqueProduto() != null) {
			estoque = aplication.getEstoqueProduto();

			Cursor cursor = banco.db.rawQuery("SELECT _id, acrescimo, decrescimo FROM estoque WHERE PRODUTOID = " + estoque.getProdutoId() + " AND LINHAID = " + estoque.getLinhaId() + " AND COLUNAID = " + estoque.getColunaId() + " AND UNIDADEID = " + estoque.getUnidadeId(), null);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				estoque.setId(cursor.getLong(cursor.getColumnIndex("_id")));
				estoque.setAcrescimo(cursor.getDouble(cursor.getColumnIndex("ACRESCIMO")));
				estoque.setDecrescimo(cursor.getDouble(cursor.getColumnIndex("DECRESCIMO")));
			}

			aplication.setEstoqueProduto(null);
			txtFiltro.setText(estoque.getCodigoBarra() + "    ");
			alterarProduto(estoque);
			selecionarAcrecimo();
		}
	}

	private void alterarProduto(Estoque estoque) {
		limparDados();
		Cursor cursorBuscaEstoque = banco.db.rawQuery("SELECT ESTOQUE, ACRESCIMO, DECRESCIMO FROM estoque WHERE PRODUTOID = " + estoque.getProdutoId() + " AND COLUNAID = " + estoque.getColunaId() + " AND LINHAID = " + estoque.getColunaId() + " AND UNIDADEID = " + estoque.getUnidadeId() + " AND ACRESCIMO = " + estoque.getAcrescimo() + " AND DECRESCIMO = " + estoque.getDecrescimo(), null);
		cursorBuscaEstoque.moveToFirst();
		if (cursorBuscaEstoque.getCount() > 0) {
			estoque.setEstoque(cursorBuscaEstoque.getDouble(cursorBuscaEstoque.getColumnIndex("ESTOQUE")));
			estoque.setAcrescimo(cursorBuscaEstoque.getDouble(cursorBuscaEstoque.getColumnIndex("ACRESCIMO")));
			estoque.setDecrescimo(cursorBuscaEstoque.getDouble(cursorBuscaEstoque.getColumnIndex("DECRESCIMO")));
		}
		txtEstoque.setText(estoque.getEstoque() + "");
		txtAcrescimo.setText(estoque.getAcrescimo() + "");
		txtDecrescimo.setText(estoque.getDecrescimo() + "");

		txtDescricao.setText(estoque.getDescricao());
		txtUnd.setText(estoque.getUnd());
		txtAlterada.setText(((estoque.getEstoque() + estoque.getAcrescimo()) - estoque.getDecrescimo()) + "");
		if (estoque.getObs() != null) {
			txtObs.setText(estoque.getObs());
		}
	}

	private void CarregarDados() {

		cursor = banco.db.rawQuery("SELECT estoque._id as _id, produtos.DESCRICAO as DESCRICAO, produtos.UND as UND, estoque.ESTOQUE as ESTOQUE, estoque.OBS as OBS, produtos.ESTOQUE as ESTOQUE_ANTIGO, estoque.ACRESCIMO as ASCRECIMO, estoque.DECRESCIMO as DECRESCIMO FROM estoque INNER JOIN produtos ON produtos.PRODUTOID = estoque.PRODUTOID AND produtos.LINHAID = estoque.LINHAID AND produtos.COLUNAID = estoque.COLUNAID AND produtos.UNIDADEID = estoque.UNIDADEID", null);

		SimpleCursorAdapter adpter = new frm_cad_estoque_adpter(this, R.layout.lay_cons_produtos_estoque, cursor, new String[] {}, new int[] {});

		lvEstoque.setAdapter(adpter);
	}

	private void DefineTeclado() {

		if (!txtFiltro.getText().toString().equals("")) {

			try {
				// VERIFICA SE O FILTRO É UM NUMERO
				Integer.parseInt(txtFiltro.getText().toString());

				// MANTEM O TECLADO NO PADRAO NUMERICO CASO TENHA FILTRADO
				// CODIGO(INCAS)

				txtFiltro.setInputType(InputType.TYPE_CLASS_NUMBER);

			} catch (NumberFormatException nfe) {
				txtFiltro.setInputType(InputType.TYPE_CLASS_TEXT);
			}

			txtFiltro.selectAll();
			txtFiltro.requestFocus();

		} else {
			txtFiltro.setInputType(InputType.TYPE_CLASS_TEXT);
		}
	}

	private void adicionarEstoque(Estoque estoque) {
		SQLiteStatement insertStmt;
		if (estoque.getId() > 0) {

			insertStmt = banco.db.compileStatement("UPDATE ESTOQUE set PRODUTOID = ?, LINHAID = ?, COLUNAID = ?, UNIDADEID = ?, ESTOQUE= ?, OBS = ?, ACRESCIMO = ?, DECRESCIMO = ? where _id = " + estoque.getId());

		} else {

			insertStmt = banco.db.compileStatement("INSERT INTO ESTOQUE (PRODUTOID, LINHAID, COLUNAID, UNIDADEID, ESTOQUE, OBS, ACRESCIMO, DECRESCIMO) VALUES (?,?,?,?,?,?,?,?)");
		}
		insertStmt.bindLong(1, estoque.getProdutoId());
		insertStmt.bindLong(2, estoque.getLinhaId());
		insertStmt.bindLong(3, estoque.getColunaId());
		insertStmt.bindLong(4, estoque.getUnidadeId());
		insertStmt.bindDouble(5, estoque.getEstoque());
		insertStmt.bindString(6, estoque.getObs());
		insertStmt.bindDouble(7, estoque.getAcrescimo());
		insertStmt.bindDouble(8, estoque.getDecrescimo());
		insertStmt.execute();
	}

}
