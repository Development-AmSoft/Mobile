package smart.mobile.utils.error;

import android.content.Context;

import smart.mobile.outras.sincronismo.DB_LocalHost;
import smart.mobile.outras.sincronismo.DB_ServerHost;
import smart.mobile.utils.DeviceUuidFactory;

public class ErroGeralController {

	private DB_ServerHost server;
	private DB_LocalHost banco;

	public ErroGeralController(Context context, DB_LocalHost banco) {
		this.banco = banco;
		server = new DB_ServerHost(context, banco.ServidorOnline, "Group");
		
		DeviceUuidFactory uuid = new DeviceUuidFactory(context);
		
		Thread.setDefaultUncaughtExceptionHandler(new ErroGeral(context, banco, server, uuid.getDeviceUuid().toString()));
	}

}
