package smart.mobile.consulta.vendas;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.DB_ServerHost;
import smart.mobile.R;
import smart.mobile.utils.ConfiguracaoInicialTela;
import smart.mobile.utils.error.ErroGeralController;
import smart.mobile.utils.ExecuteVenda;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class frm_cons_vendas extends Activity {
	private Handler h = new Handler();
	private EditText vendaDe;
	private EditText vendaAte;
	private EditText vendaFiltro;
	private Spinner vendaDatas;
	private ListView vendaListView;
	private Adapter adapter;
	private DB_ServerHost service;
	private DB_LocalHost banco;
	private SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
	private SimpleDateFormat formatt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private HashMap<Integer, String> valorSpinner;
	private ArrayList<String> colunaSpiner = new ArrayList<String>();
	private String valoresColunas;
	private String colunaSelecionada;
	private ArrayList<ValoresColuna> valoresColunasArray = new ArrayList<ValoresColuna>();

	private String separadorLinha = "#l#";
	private String separadorColuna = "#c#";
	private Context context;
	private ListView lista;
	private ProgressDialog myProgressDialog;
	private double valorTotal;

	private ArrayList<VendasConsulta> arrayLista = new ArrayList<frm_cons_vendas.VendasConsulta>();
	private String retornoPrincipal = "";
	private ImageButton vendaProcurar;
	private ImageButton vendaAgrupar;
	private TextView vendaSomaTotal;
	private String buscaSimples = "";

	public frm_cons_vendas() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// conexao com banco e servidor
		banco = new DB_LocalHost(this);
		service = new DB_ServerHost(this, banco.ServidorOnline, banco.Banco);
		ErroGeralController erro = new ErroGeralController(this, banco);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ctela_base);
		setTitle("SmartMobile - Relatório de Vendas [Online]");

		ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.lay_cons_vendas, false);
		RelativeLayout footer = (RelativeLayout) findViewById(R.id.FooterEsconder);
		footer.setVisibility(View.GONE);

		context = this;

		vendaDe = (EditText) findViewById(R.id.vendaDe);
		vendaAte = (EditText) findViewById(R.id.vendaAte);
		vendaDatas = (Spinner) findViewById(R.id.vendaDatas);
		vendaFiltro = (EditText) findViewById(R.id.vendaFiltro);
		vendaAgrupar = (ImageButton) findViewById(R.id.botaoAgrupar);
		vendaProcurar = (ImageButton) findViewById(R.id.btnConsEmpresa);
		// vendaOrdenar = (ImageButton) findViewById(R.id.btnVendaOrdenar);
		vendaSomaTotal = (TextView) findViewById(R.id.vendaSomaTotal);

		vendaAgrupar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				agruparFuncao();
			}
		});

		vendaProcurar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// ativarPopUp();

				h.post(new Runnable() {

					@Override
					public void run() {
						try {
							if (vendaDe.getText().toString().length() != 10 || vendaAte.getText().toString().length() != 10) {
								retornoPrincipal = "Formato da Data Incorreto - Ex: 20/10/1999";
								throw new Exception("Formato da Data Incorreto - Ex: 20/10/1999");
							} else {
								try {
									Date a = formatDate.parse(vendaDe.getText().toString());
									Date b = formatDate.parse(vendaAte.getText().toString());
								} catch (Exception e) {
									retornoPrincipal = "Formato da Data Incorreto - Ex: 20/10/1999";
									throw new Exception("Formato da Data Incorreto - Ex: 20/10/1999");
								}
							}

							ExecuteVenda venda = new ExecuteVenda(context) {

								@Override
								public void execute(Context context) {
									try {
										if (buscaSimples.isEmpty()) {
											montarLista(null);
										} else {
											montarLista(buscaSimples);
										}
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								};

							};

						} catch (Exception e) {
							funcaoErro();
						}
					}
				});

			}
		});
		// dias datas
		setarValorSpinner();

		Calendar hoje = Calendar.getInstance();

		vendaDe.setText(formatDate.format(hoje.getTime()));
		vendaAte.setText(formatDate.format(diasAtras(45).getTime()));

		// h.post(new Runnable() {
		//
		// @Override
		// public void run() {
		// ativarPopUp();
		// Handler hh = new Handler();
		// hh.postDelayed(new Runnable() {
		//
		// @Override
		// public void run() {
		// setarValorColunas();
		// try {
		// montarLista(null);
		// Log.v("", "");
		// } catch (Exception e) {
		// funcaoErro();
		// }
		//
		// Handler handler = new Handler();
		// handler.postDelayed(new Runnable() {
		// public void run() {
		// myProgressDialog.dismiss();
		//
		// }
		// }, 400);
		//
		//
		// }
		// }, 200);
		// }
		// });

		ExecuteVenda venda = new ExecuteVenda(this) {

			@Override
			public void execute(Context context) {
				setarValorColunas();
				try {
					montarLista(null);
					Log.v("", "");
				} catch (Exception e) {
					funcaoErro();
				}

			}

		};

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemAdd = menu.add(0, 1, Menu.NONE, "Voltar");
		MenuItem itemOrdenar = menu.add(0, 2, Menu.NONE, "Ordenar por");
		MenuItem itemAgrupar = menu.add(0, 3, Menu.NONE, "Agrupar por");

		// Assign icons
		itemAdd.setIcon(R.drawable.ico_voltar);

		// Allocate shortcuts to each of them.
		itemAdd.setShortcut('0', 'v');
		itemOrdenar.setShortcut('1', 'o');
		itemAgrupar.setShortcut('2', 'a');

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:

			finish();
			break;
		case 2:
			AlertDialog.Builder builder2 = new AlertDialog.Builder(frm_cons_vendas.this);
			builder2.setTitle("Ordenar lista por");
			builder2.setSingleChoiceItems(colunaSpiner.toArray(new String[colunaSpiner.size()]), banco.cons_cli_indexTIPO,

			new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					colunaSelecionada = colunaSpiner.get(which);
					final DialogInterface dd = dialog;

					ExecuteVenda venda = new ExecuteVenda(context) {

						@Override
						public void execute(Context context) {
							try {
								montarLista(null);
							} catch (Exception e) {
								funcaoErro();
							}
							dd.cancel();
						}
					};

				}

			});

			AlertDialog alert2 = builder2.create();
			alert2.show();
			break;
		case 3:

			agruparFuncao();
			break;
		default:
			break;
		}
		return false;
	}

	private void agruparFuncao() {
		AlertDialog.Builder builder = new AlertDialog.Builder(frm_cons_vendas.this);
		builder.setTitle("Agrupar lista por");
		String array[] = colunaSpiner.toArray(new String[(colunaSpiner.size() + 1)]);
		array[colunaSpiner.size()] = "Busca Simples";
		builder.setSingleChoiceItems(array, banco.cons_cli_indexTIPO,

		new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				final DialogInterface dd = dialog;
				final int wi = which;
				ExecuteVenda v = new ExecuteVenda(context) {

					@Override
					public void execute(Context context) {
						try {
							// ativarPopUp();
							if (wi == colunaSpiner.size()) {
								montarLista(null);
								buscaSimples = "";
							} else {
								buscaSimples = colunaSpiner.get(wi);
								montarLista(colunaSpiner.get(wi));
							}
						} catch (Exception e) {
							funcaoErro();
						}
						dd.cancel();

					}
				};
			}

		});

		AlertDialog alert = builder.create();

		alert.show();

	}

	public void ativarPopUp() {
		myProgressDialog = ProgressDialog.show(frm_cons_vendas.this, "Carregando Informações", "Carregando Informações das Vendas");
		myProgressDialog.setCancelable(false);
		myProgressDialog.setCanceledOnTouchOutside(false);
		myProgressDialog.show();
	}

	public void desativarPopUp() {
		if (myProgressDialog != null) {
			myProgressDialog.dismiss();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	private void funcaoErro() {
		myProgressDialog.dismiss();

		if (!retornoPrincipal.isEmpty()) {
			if (!retornoPrincipal.contains(separadorColuna)) {
				montarMensagemErro(retornoPrincipal, false);
			}
		} else if (!valoresColunas.isEmpty()) {
			if (!valoresColunas.contains(separadorColuna)) {
				if (valoresColunas.contains("java.apache.SQLException")) {
					valoresColunas = valoresColunas.replace("java.apache.SQLException", "");
				}
				montarMensagemErro(valoresColunas, true);
			}
		} else if (valoresColunas.isEmpty()) {
			montarMensagemErro("Conexão: Não foi possível conectar ao servidor " + banco.ServidorOnline + "\n\r1) Verifique sua conexão" + "\n\r2) Sincronize novamente", true);
		}

	}

	private void montarLista(String valor) throws Exception {

		if (valor == null) {
			retornoPrincipal = service.Sql_Select(montarSQLVendas(null) + montarSQLVendasWhere() + montarSQLVendasOrder(null));
			Log.i("retorno", retornoPrincipal);
			try {

				montarObjetosEArrays(false);

				lista = (ListView) findViewById(R.id.vendaListView);
				lista.setAdapter(null);
				ListAdapter lsadapter = new frm_cons_vendas_adapter(this, R.layout.lay_cons_vendas_adp, arrayLista);
				lista.setAdapter(lsadapter);
				lista.setFastScrollEnabled(true);
			} catch (Exception e) {
				throw e;
			}
		} else {
			retornoPrincipal = service.Sql_Select(montarSQLVendas(valor) + montarSQLVendasWhere() + montarSQLVendasOrder(valor));
			Log.i("retorno", retornoPrincipal);
			montarObjetosEArrays(true);

			lista = (ListView) findViewById(R.id.vendaListView);
			lista.setAdapter(null);
			ListAdapter lsadapter = new frm_cons_vendas_adapter(this, R.layout.lay_cons_vendas_simples_adp, arrayLista);
			lista.setAdapter(lsadapter);
			lista.setFastScrollEnabled(true);
		}
		// myProgressDialog.dismiss();

	}

	private void montarObjetosEArrays(boolean simples) throws Exception {
		DecimalFormat df = new DecimalFormat("#,###.00");

		arrayLista = new ArrayList<frm_cons_vendas.VendasConsulta>();
		valorTotal = 0;
		try {
			String linhas[] = retornoPrincipal.split(separadorLinha);

			if (linhas.length == 1 && linhas[0].isEmpty()) {
				vendaSomaTotal.setText("0,00");
			} else {

				for (int i = 0; i < linhas.length; i++) {
					/*
					 * 0 - EMPRESAID 1- VENDEDORID 2- VENDAID 3- DATA_PED 4-
					 * DATA_FAT 5- NOME 6- CNPJ 7- TOTAL 8- SITUACAO
					 */
					String colunas[] = linhas[i].split(separadorColuna);

					VendasConsulta consultaItem = new VendasConsulta();
					if (!simples) {
						consultaItem.setVendaId(colunas[2]);

						Date a = null;
						Date b = null;
						try {
							a = formatt.parse(colunas[3]);
							b = formatt.parse(colunas[4]);

						} catch (ParseException e) {
						}
						consultaItem.setDataPed(formatDate.format(a));
						if (b != null) {
							consultaItem.setDataFat(formatDate.format(b));
						} else {
							consultaItem.setDataFat("Não Faturada");
						}
						consultaItem.setNome(colunas[5]);
						consultaItem.setCnpj(colunas[6]);

						double total = Double.parseDouble(colunas[7]);
						consultaItem.setTotal(df.format(total));
						consultaItem.setSituacao(colunas[8]);

						valorTotal += total;
					} else {
						try {
							Date a = formatt.parse(colunas[0]);
							consultaItem.setCnpj(formatDate.format(a));
						} catch (ParseException e) {
							try {
								double total = Double.parseDouble(colunas[0]);
								consultaItem.setCnpj(df.format(total));
							} catch (NumberFormatException ee) {
								consultaItem.setCnpj(colunas[0]);
							}

						}

						double total = Double.parseDouble(colunas[1]);
						consultaItem.setTotal(df.format(total));
						valorTotal += total;
					}

					arrayLista.add(consultaItem);
				}

				vendaSomaTotal.setText(df.format(valorTotal));
			}
		} catch (Exception e) {
			throw e;
		}

	}

	private void setarValorColunas() {

		new Thread() {
			@Override
			public void run() {
				valoresColunas = service.Sql_Select("SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'vw_mobile_vendas';");
				String linhas[] = valoresColunas.split(separadorLinha);
				try {
					for (int i = 0; i < linhas.length; i++) {
						String colunas[] = linhas[i].split(separadorColuna);
						try {
							ValoresColuna valor = new ValoresColuna();
							valor.setNome(colunas[0]);
							valor.setType(colunas[1]);

							colunaSpiner.add(colunas[0]);

							valoresColunasArray.add(valor);
						} catch (Exception e) {

						}

					}

				} catch (ArrayIndexOutOfBoundsException e) {
					throw e;
				}
			}

		}.run();

	}

	private void setarValoresSpinnerGroup() {

	}

	private void setarValorSpinner() {
		valorSpinner = new HashMap<Integer, String>();
		String valores[] = { "45 Dias", "30 Dias", "15 Dias", "7 Dias" };
		for (int i = 0; i < valores.length; i++) {
			valorSpinner.put(i, valores[i]);
		}

		ArrayAdapter<String> adpOperacoes = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valores);
		vendaDatas.setAdapter(adpOperacoes);

		vendaDatas.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				switch (arg2) {
				case 0:
					trocarDia(45);
					break;
				case 1:
					trocarDia(30);
					break;
				case 2:
					trocarDia(15);
					break;
				case 3:
					trocarDia(7);
					break;
				default:
					break;
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});

	}

	private void trocarDia(int dias) {
		vendaDe.setText(formatDate.format(Calendar.getInstance().getTime()));
		vendaAte.setText(formatDate.format(diasAtras(dias).getTime()));
	}

	private Calendar diasAtras(int dias) {
		Calendar retorno = Calendar.getInstance();
		retorno.add(Calendar.DAY_OF_MONTH, (dias * -1));
		return retorno;
	}

	private String montarSQLVendas(String valor) {
		StringBuilder retorno = new StringBuilder();
		retorno.append("SELECT ");

		if (valor == null) {
			if (!valoresColunasArray.isEmpty()) {
				for (int i = 0; i < valoresColunasArray.size(); i++) {
					ValoresColuna coluna = valoresColunasArray.get(i);

					if (i != 0) {
						retorno.append(", ");
					}
					retorno.append(coluna.getNome());

				}
			} else {
				retorno.append("*");
			}
		} else {
			retorno.append(valor);
			retorno.append(", SUM(TOTAL)");
		}
		retorno.append(" FROM ");
		retorno.append("VW_MOBILE_VENDAS ");

		return retorno.toString();
	}

	private String montarSQLVendasWhere() {
		StringBuilder retorno = new StringBuilder();
		retorno.append("WHERE ");

		String filtro = vendaFiltro.getText().toString();

		try {
			retorno.append("DATA_PED BETWEEN ");
			retorno.append("CONVERT(datetime,'");
			retorno.append(retornoDatePadraoSqlHora(vendaAte.getText().toString(), "00:00:00"));
			retorno.append("',103)");
			retorno.append(" AND ");
			retorno.append("CONVERT(datetime,'");
			retorno.append(retornoDatePadraoSqlHora(vendaDe.getText().toString(), "23:59:59"));
			retorno.append("',103)");
			retorno.append(" AND ");
		} catch (Exception e) {

		}
		retorno.append("EMPRESAID = ");
		retorno.append(banco.EmpresaID);
		retorno.append(" AND ");
		retorno.append("VENDEDORID = ");
		retorno.append(banco.VendedorID);

		if (!filtro.isEmpty()) {
			retorno.append(" AND ");
			retorno.append("(");
			boolean primeiro = true;
			for (int i = 0; i < valoresColunasArray.size(); i++) {
				ValoresColuna coluna = valoresColunasArray.get(i);

				if (verificarValoresNaoInseridos(coluna)) {
					if (coluna.getType().equalsIgnoreCase("int")) {
						try {
							int filtroInt = Integer.parseInt(filtro);
							if (!primeiro) {
								retorno.append("OR ");
							} else {
								primeiro = false;
							}
							retorno.append(coluna.getNome());
							retorno.append(" = ");
							retorno.append(filtroInt);
							retorno.append(" ");
						} catch (Exception e) {

						}
					} else if (coluna.getType().equalsIgnoreCase("money")) {
						try {
							double filtroDouble = Double.parseDouble(filtro);
							if (!primeiro) {
								retorno.append("OR ");
							} else {
								primeiro = false;
							}
							retorno.append(coluna.getNome());
							retorno.append(" = ");
							retorno.append(filtroDouble);
							retorno.append(" ");
						} catch (Exception e) {

						}
					} else if (coluna.getType().equalsIgnoreCase("datetime")) {
						try {
							String data = retornoDatePadraoSql(filtro);
							if (!primeiro) {
								retorno.append("OR ");
							} else {
								primeiro = false;
							}
							retorno.append(coluna.getNome());
							retorno.append(" = ");
							retorno.append("CONVERT(datetime,'");
							retorno.append(data);
							retorno.append("',103) ");
						} catch (Exception e) {

						}
					} else {
						if (!primeiro) {
							retorno.append("OR ");
						} else {
							primeiro = false;
						}
						retorno.append(coluna.getNome());
						retorno.append(" LIKE ");
						retorno.append("'%");
						retorno.append(filtro);
						retorno.append("%' ");
					}
				}

			}
			retorno.append(")");
			primeiro = true;

		}

		return retorno.toString();
	}

	private boolean verificarValoresNaoInseridos(ValoresColuna coluna) {

		if (coluna.getNome().equalsIgnoreCase("VENDEDORID")) {
			return false;
		} else if (coluna.getNome().equalsIgnoreCase("EMPRESAID")) {
			return false;
		} else if (coluna.getNome().equalsIgnoreCase("DATA_PED")) {
			return false;
		}

		return true;
	}

	private String retornoDatePadraoSql(String data) throws Exception {
		Date antiga = null;
		antiga = formatDate.parse(data);
		return new SimpleDateFormat("dd/MM/yyyy").format(antiga);
	}

	private String retornoDatePadraoSqlHora(String data, String hora) throws Exception {
		Date antiga = null;
		antiga = formatDate.parse(data);
		return new SimpleDateFormat("dd/MM/yyyy " + hora).format(antiga);
	}

	private Date retornoStringPadraoDate(String data) throws Exception {
		Date antiga = null;
		antiga = formatDate.parse(data);
		return antiga;
	}

	private String montarSQLVendasOrder(String valor) {
		StringBuilder retorno = new StringBuilder();
		if (valor == null) {
			retorno.append(" order by ");
			if (colunaSelecionada != null) {
				retorno.append(colunaSelecionada);
				retorno.append(" DESC");
			} else {
				retorno.append("VENDAID DESC");
			}
		} else {
			retorno.append(" GROUP BY ");
			retorno.append(valor);
		}

		return retorno.toString();
	}

	public class ValoresColuna {
		private String nome;
		private String type;

		public ValoresColuna() {
			// TODO Auto-generated constructor stub
		}

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}

	public class VendasConsulta {

		private String vendaId;
		private String dataPed;
		private String dataFat;
		private String total;
		private String situacao;
		private String nome;
		private String cnpj;

		public VendasConsulta() {
			// TODO Auto-generated constructor stub
		}

		public String getVendaId() {
			return vendaId;
		}

		public void setVendaId(String vendaId) {
			this.vendaId = vendaId;
		}

		public String getDataPed() {
			return dataPed;
		}

		public void setDataPed(String dataPed) {
			this.dataPed = dataPed;
		}

		public String getDataFat() {
			return dataFat;
		}

		public void setDataFat(String dataFat) {
			this.dataFat = dataFat;
		}

		public String getTotal() {
			return total;
		}

		public void setTotal(String total) {
			this.total = total;
		}

		public String getSituacao() {
			return situacao;
		}

		public void setSituacao(String situacao) {
			this.situacao = situacao;
		}

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public String getCnpj() {
			return cnpj;
		}

		public void setCnpj(String cnpj) {
			this.cnpj = cnpj;
		}

	}

	private void montarMensagemErro(String mensagem, final boolean fechar) {
		Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Error");
		alert.setMessage(mensagem);
		alert.setPositiveButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (fechar) {
					frm_cons_vendas.this.finish();
				}

			}
		});
		alert.show();
	}

}
