package smart.mobile.utils.ZipDownload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.provider.MediaStore.Files;
import android.util.Log;

public class UnzipFile
{
	private final String PATH = "/data/data/smart.mobile";

	public UnzipFile()
	{
		// TODO Auto-generated constructor stub
	}

	public void unZipIt(String zipFile, String outputFolder)
	{

		byte[] buffer = new byte[1024];

		try
		{

			// create output directory is not exists
			File folder = new File(PATH + File.separator + outputFolder);

			if (!folder.exists())
			{
				folder.mkdirs();
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(PATH + File.separator + zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null)
			{
				String fileName = ze.getName();
				File newFile = new File(PATH + outputFolder + fileName);

				Log.v("", "file unzip : " + newFile.getAbsoluteFile());

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				if (newFile.getAbsoluteFile().toString().contains(".jpg"))
				{
					new File(newFile.getParent()).mkdirs();
					FileOutputStream fos = new FileOutputStream(newFile);

					int len;
					while ((len = zis.read(buffer)) > 0)
					{
						fos.write(buffer, 0, len);
					}

					fos.close();
				} else
				{
					newFile.mkdirs();
				}

				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

			System.out.println("Done");

		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
