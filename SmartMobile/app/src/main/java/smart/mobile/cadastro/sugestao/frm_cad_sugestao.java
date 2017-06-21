package smart.mobile.cadastro.sugestao;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.DB_ServerHost;
import smart.mobile.R;
import smart.mobile.utils.ConfiguracaoInicialTela;
import smart.mobile.utils.DeviceUuidFactory;
import smart.mobile.utils.error.ErroGeralController;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class frm_cad_sugestao extends Activity {

	private DB_LocalHost banco;
	private String uuid;
	private DB_ServerHost server;
	private EditText sugestao;
	private Button enviar;
	private ProgressDialog myProgressDialog;
	private String resposta;
	private Button voltar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ctela_base);
		setTitle("SmartMobile - Força de Vendas");

		Handler handler = new Handler();

		ConfiguracaoInicialTela.carregarDadosIniciais(this, R.layout.lay_sys_sugestao, true);

		// carrega empresa e vendedor
		banco = new DB_LocalHost(this);
		banco.DB_ConfigLoad();
		banco.DB_SaldoFlexLoad();
		server = new DB_ServerHost(this, banco.ServidorOnline, "Group");
		ErroGeralController erro = new ErroGeralController(this, banco);

		try {
			Bundle b = getIntent().getExtras();
			resposta = b.getString("resposta");// DATA, MENSAGEM, RETORNO

			if (!resposta.isEmpty()) {

				LinearLayout lyMensagem = (LinearLayout) findViewById(R.id.corpoMensagemAntiga);
				lyMensagem.setVisibility(View.VISIBLE);

				// LinearLayout lyFixo = (LinearLayout)
				// findViewById(R.id.fixoSugestao);
				// LinearLayout.LayoutParams params =
				// (LinearLayout.LayoutParams) lyFixo.getLayoutParams();
				// params.setMargins(0, 5, 0, 0);
				// lyFixo.setLayoutParams(params);

				TextView tvMensagemAntiga = (TextView) findViewById(R.id.mensagemAntiga);
				TextView tvDataAntiga = (TextView) findViewById(R.id.dataAntiga);
				TextView tvResposta = (TextView) findViewById(R.id.resposta);
				TextView tvTituloResposta = (TextView) findViewById(R.id.tituloResposta);

				final String[] colunas = resposta.split("#c#");// DATA,
																// MENSAGEM,
																// RETORNO

				if (colunas.length > 0) {
					tvTituloResposta.setText("Se deseja responder, descreva abaixo:");
					tvDataAntiga.setText(colunas[0]);
					tvMensagemAntiga.setText(colunas[1]);
					tvResposta.setText(colunas[2]);
					handler.post(new Runnable() {

						@Override
						public void run() {

							String select = "update versoes_mobiles_mensagens set lida = 1 where mensagemid = " + colunas[3] + ";";
							server.Sql_Select(select);

						}
					});

				}
			}
		} catch (Exception e) {
		}

		uuid = new DeviceUuidFactory(this).getDeviceUuid().toString();

		sugestao = (EditText) findViewById(R.id.edtSugestao);
		enviar = (Button) findViewById(R.id.btnSalvar);
		voltar = (Button) findViewById(R.id.btnVoltar);

		voltar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		enviar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!sugestao.getText().toString().isEmpty()) {
					
					myProgressDialog = new ProgressDialog(frm_cad_sugestao.this);
					myProgressDialog.setIcon(R.drawable.ico_refresh);
					myProgressDialog.setTitle("Sincronizando");
					myProgressDialog.setMessage("Sincronizando sugestão");
					myProgressDialog.setIndeterminate(false);
					myProgressDialog.setCancelable(false);
					myProgressDialog.setCanceledOnTouchOutside(false);
					myProgressDialog.show();

					StringBuilder sqlEnvio = new StringBuilder();
					sqlEnvio.append("SP_INSERE_MOBILE_MENSAGEM @RETORNOID = ?, ");

					sqlEnvio.append("@UUID = ");
					sqlEnvio.append("'");
					sqlEnvio.append(uuid);
					sqlEnvio.append("'");
					sqlEnvio.append(", ");

					sqlEnvio.append("@TIPO = ");
					sqlEnvio.append("'sugestão'");
					sqlEnvio.append(", ");

					sqlEnvio.append("@MENSAGEM = ");
					sqlEnvio.append("'");
					sqlEnvio.append(sugestao.getText().toString());
					sqlEnvio.append("'");

					String retorno = server.Sql_Executa(sqlEnvio.toString());
					System.out.println(retorno);

					try {
						Integer.parseInt(retorno);
						myProgressDialog.dismiss();

						Builder alert = new AlertDialog.Builder(frm_cad_sugestao.this);
						alert.setTitle("Erro na Sincronização");
						alert.setMessage("Mensagem enviada a AmSoft [Equipe de Projetos]. Você receberá uma resposta automaticamente após verificação.");
						alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						});
						alert.show();

						sugestao.setText("");
					} catch (Exception e) {
						myProgressDialog.dismiss();

						Builder alert = new AlertDialog.Builder(frm_cad_sugestao.this);
						alert.setTitle("Erro na Sincronização");
						alert.setMessage(retorno);
						alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						});
						alert.show();
					}
				} else {
					Builder alert = new AlertDialog.Builder(frm_cad_sugestao.this);
					alert.setTitle("Valores Vazios!");
					alert.setMessage("Por favor insira uma sugestão.");
					alert.setPositiveButton("OK", null);
					alert.show();
				}
			}
		});

	}

}
