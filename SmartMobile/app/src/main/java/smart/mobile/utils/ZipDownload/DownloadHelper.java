package smart.mobile.utils.ZipDownload;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import smart.mobile.outras.sincronismo.download.DB_Sincroniza;
import android.app.ProgressDialog;
import android.util.Log;

public class DownloadHelper
{
	private final String PATH = "/data/data/smart.mobile/";
	private ProgressDialog dialog;
	private DB_Sincroniza db;

	public DownloadHelper()
	{
		db = null;
	}

	public void DownloadFromUrl(String link, String fileName)
	{ // this is the downloader method
		final int BUFFER_SIZE = 23 * 1024;
		try
		{
			URL url = new URL(link);
			File file = new File(PATH + fileName);
			long startTime = System.currentTimeMillis();
			Log.d("ImageManager", "download begining");
			Log.d("ImageManager", "download url:" + url);
			Log.d("ImageManager", "downloaded file name:" + fileName);
			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();
			int tamanho = ucon.getContentLength();
			if (db != null)
			{
				db.Progresso_Max(tamanho);
			}
			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);

			FileOutputStream fos = new FileOutputStream(file);

			byte[] baf = new byte[BUFFER_SIZE];
			int actual = 0;
			int totalPlus = 0;
			while (actual != -1)
			{
				
				fos.write(baf, 0, actual);
				actual = bis.read(baf, 0, BUFFER_SIZE);
				totalPlus += actual;
				if (db != null)
				{
					db.Progresso_Posicao(totalPlus);
				}
			}

			fos.close();

			Log.d("ImageManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
		} catch (IOException e)
		{
			Log.d("ImageManager", "Error: " + e);
		}
	}

	public void setDB(DB_Sincroniza db_Sincroniza)
	{
		this.db = db_Sincroniza;

	}
}
