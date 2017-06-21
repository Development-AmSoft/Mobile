package smart.mobile.utils.error;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.Locale;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.DB_ServerHost;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;

public class ErroGeral implements UncaughtExceptionHandler {

	private Context context;
	private DB_LocalHost banco;
	private DB_ServerHost server;
	private String uuid;
	private static Context context1;

	public ErroGeral(Context ctx, DB_LocalHost banco, DB_ServerHost server, String uuid) {
		context = ctx;
		context1 = ctx;
		this.banco = banco;
		this.server = server;
		this.uuid = uuid;
	}

	private StatFs getStatFs() {
		File path = Environment.getDataDirectory();
		return new StatFs(path.getPath());
	}

	private long getAvailableInternalMemorySize(StatFs stat) {
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	private long getTotalInternalMemorySize(StatFs stat) {
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	private void addInformation(StringBuilder message) {
		message.append("Locale: ").append(Locale.getDefault()).append('\n');
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi;
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			message.append("Version: ").append(pi.versionName).append('\n');
			message.append("Package: ").append(pi.packageName).append('\n');
		} catch (Exception e) {
			Log.e("CustomExceptionHandler", "Error", e);
			message.append("Could not get Version information for ").append(context.getPackageName());
		}
		message.append("Phone Model: ").append(android.os.Build.MODEL).append('\n');
		message.append("Android Version: ").append(android.os.Build.VERSION.RELEASE).append('\n');
		message.append("Board: ").append(android.os.Build.BOARD).append('\n');
		message.append("Brand: ").append(android.os.Build.BRAND).append('\n');
		message.append("Device: ").append(android.os.Build.DEVICE).append('\n');
		message.append("Host: ").append(android.os.Build.HOST).append('\n');
		message.append("ID: ").append(android.os.Build.ID).append('\n');
		message.append("Model: ").append(android.os.Build.MODEL).append('\n');
		message.append("Product: ").append(android.os.Build.PRODUCT).append('\n');
		message.append("Type: ").append(android.os.Build.TYPE).append('\n');
		StatFs stat = getStatFs();
		message.append("Total Internal memory: ").append(getTotalInternalMemorySize(stat)).append('\n');
		message.append("Available Internal memory: ").append(getAvailableInternalMemorySize(stat)).append('\n');
	}

	public void uncaughtException(Thread t, Throwable e) {
		try {
			StringBuilder report = new StringBuilder();
			Date curDate = new Date();
			report.append("Error Report collected on : ").append(curDate.toString()).append('\n').append('\n');
			report.append("Informations :").append('\n');
			addInformation(report);
			report.append('\n').append('\n');
			report.append("Stack:\n");
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			report.append(result.toString());
			printWriter.close();
			report.append('\n');
			report.append("**** End of current Report ***");
			Log.e(ErroGeral.class.getName(), "Error while sendErrorMail" + report);
			if (!result.toString().isEmpty()) {
				sendErrorMail(report);
			}
		} catch (Throwable ignore) {
			Log.e(ErroGeral.class.getName(), "Error while sending error e-mail", ignore);
		}
	}

	/**
	 * This method for call alert dialog when application crashed!
	 */
	public void sendErrorMail(final StringBuilder errorContent) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				builder.setTitle("Desculpe...!");
				builder.create();
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						StringBuilder sqlEnvio = new StringBuilder();
						sqlEnvio.append("SP_INSERE_MOBILE_MENSAGEM @RETORNOID = ?, ");

						sqlEnvio.append("@UUID = ");
						sqlEnvio.append("'");
						
						sqlEnvio.append(uuid);
						sqlEnvio.append("'");
						sqlEnvio.append(", ");

						sqlEnvio.append("@TIPO = ");
						sqlEnvio.append("'ERRO'");
						sqlEnvio.append(", ");

						sqlEnvio.append("@MENSAGEM = ");
						sqlEnvio.append("'");
						sqlEnvio.append(errorContent.toString());
						sqlEnvio.append("'");
						
						
						
						String retorno = server.Sql_Executa(sqlEnvio.toString());
						System.out.println(retorno);
						System.exit(0);
					}
				});
				builder.setMessage("Ocorreu um erro no SmartMobile!");
				builder.show();
				Looper.loop();
			}
		}.start();
	}
}