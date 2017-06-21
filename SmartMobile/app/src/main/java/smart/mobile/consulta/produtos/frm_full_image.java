package smart.mobile.consulta.produtos;

import java.io.File;
import java.util.ArrayList;

import smart.mobile.R;
import smart.mobile.utils.ZipDownload.Utils;
import smart.mobile.utils.image.FullScreenImageAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class frm_full_image extends Activity {

	private Utils utils;
	private FullScreenImageAdapter adapter;
	private ViewPager viewPager;
	private ArrayList<String> listaPaths;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen_view);

		viewPager = (ViewPager) findViewById(R.id.pager);

		utils = new Utils(getApplicationContext());

		Intent i = getIntent();
		int position = i.getIntExtra("position", 0);

		Intent intent = getIntent();
		Bundle params = intent.getExtras();
		if (params != null) {
			if (!params.isEmpty()) {
				String retorno = params.getString("idProduto");
				if (!retorno.isEmpty()) {
					listaPaths = montarListaImagem(retorno);
				}

				if (listaPaths.size() == 0) {
					listaPaths.add("");
				}
				adapter = new FullScreenImageAdapter(frm_full_image.this, listaPaths);

				viewPager.setAdapter(adapter);

				// displaying selected image first
				viewPager.setCurrentItem(position);
			}
		}
	}

	private ArrayList<String> montarListaImagem(String id) {
		String PATH = "/data/data/smart.mobile/imagens/";
		ArrayList<String> retorno = new ArrayList<String>();

		for (int i = 1; i < 1000; i++) {
			File file = new File(PATH + id + "_" + i + ".jpg");

			if (file.isFile()) {
				retorno.add(file.getAbsolutePath());
			} else {
				break;
			}
		}

		return retorno;
	}
}