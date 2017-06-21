package smart.mobile.utils.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import smart.mobile.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FullScreenImageAdapter extends PagerAdapter
{

	private Activity _activity;
	private ArrayList<String> _imagePaths;
	private LayoutInflater inflater;

	// constructor
	public FullScreenImageAdapter(Activity activity, ArrayList<String> imagePaths)
	{
		this._activity = activity;
		this._imagePaths = imagePaths;
	}

	@Override
	public int getCount()
	{
		return this._imagePaths.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object)
	{
		return view == ((RelativeLayout) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		TouchImageView imgDisplay;
		Button btnClose;
		TextView posicao;
		TextView total;

		inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container, false);

		posicao = (TextView) viewLayout.findViewById(R.id.posicaoImagem);
		posicao.setText((position + 1) + "");
		total = (TextView) viewLayout.findViewById(R.id.totalImagem);
		total.setText("/" + _imagePaths.size());

		imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);
		btnClose = (Button) viewLayout.findViewById(R.id.btnClose);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		if (_imagePaths.size() == 1)
		{
			if (_imagePaths.get(0).equals(""))
			{
				imgDisplay.setImageDrawable(viewLayout.getContext().getResources().getDrawable(R.drawable.semimagem));
			} else
			{
				Bitmap bitmap = BitmapFactory.decodeFile(_imagePaths.get(position), options);
				imgDisplay.setImageBitmap(bitmap);
			}
		} else
		{
			Bitmap bitmap = BitmapFactory.decodeFile(_imagePaths.get(position), options);
			imgDisplay.setImageBitmap(bitmap);
		}

		// close button click event
		btnClose.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				_activity.finish();
			}
		});

		((ViewPager) container).addView(viewLayout);

		return viewLayout;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		((ViewPager) container).removeView((RelativeLayout) object);

	}

	public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth,
            int reqHeight) {
 
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
 
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
 
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bmp = BitmapFactory.decodeFile(path, options);
        return bmp;
        }
 
    public static int calculateInSampleSize(BitmapFactory.Options options,
            int reqWidth, int reqHeight) {
 
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
 
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
             }
         }
         return inSampleSize;
        }
	
}
