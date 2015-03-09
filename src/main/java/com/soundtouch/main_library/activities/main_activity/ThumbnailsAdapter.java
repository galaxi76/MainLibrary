package com.soundtouch.main_library.activities.main_activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


import com.soundtouch.main_library.R;
import com.soundtouch.main_library.activities.main_activity.CategoriesThumbnailsManager.Category;

/**
 * a class for the gridView of the main activity , for updating its thumbnails
 * when choosing categories
 */
public class ThumbnailsAdapter extends BaseAdapter {
	public static int _numOfThumbnails = 12;
	public final static int CACHE_SIZE_LIMIT = 10 * 1024 * 1024;
	protected final HashMap<Category, ArrayList<Bitmap>> _imageCache = new HashMap<CategoriesThumbnailsManager.Category, ArrayList<Bitmap>>();
	public final static int NUMBER_OF_THUMBNAILS_PER_ROW_4 = 2;
	public final static int NUMBER_OF_THUMBNAILS_PER_ROW_6 = 2;
	public final static int NUMBER_OF_THUMBNAILS_PER_ROW_9 = 3;
	public final static int NUMBER_OF_THUMBNAILS_PER_ROW_12 = 3;
	public static int _numOfThumbnailsPerRow = NUMBER_OF_THUMBNAILS_PER_ROW_12;
	public static int _numOfThumbnailsPerColumn = _numOfThumbnails
			/ _numOfThumbnailsPerRow;
	public static final int NUMBER_OF_CATEGORIES = 6;
	public static final String CLASS_TAG = "ImageAdapter:";
	protected GridView.LayoutParams _layoutParametersForEachThumbnail = null;
	public int _gridViewHeightWhenFull = 0;
	private int _thumbnailWidth = 0;
	protected Category _currentCategory = Category.ANIMALS;
	public Category _superCategory = Category.ANIMALS;
	protected CategoriesThumbnailsManager _categoriesThumbnailsManager;
	private LayoutInflater _inflater;
	private Context _appContext;

	/**
	 * CTOR.
	 * 
	 * @param gv
	 *            the gridview to handle
	 * @param gridViewHeightWhenFull
	 *            the height of the gridView when it is full , without any kind
	 *            of ads
	 */
	public ThumbnailsAdapter(
			final CategoriesThumbnailsManager categoriesThumbnailsManager) {
		_categoriesThumbnailsManager = categoriesThumbnailsManager;
	}

	/**
	 * cache imageViews in order to access them faster from the image adapter .
	 * the imageViews must not change their content , since they are accessed by
	 * the image adapter.
	 */
	protected void runCachingProcess() {
		// int numberOfCachedImageViews = 0;
		// ImageView thumbnailImageView;
		for (final Category category : Category.values())
			_imageCache.put(category, new ArrayList<Bitmap>());
		long totalCacheUsage = 0;
		final Resources resources = _appContext.getResources();
		for (int thumbnailIndex = 0; thumbnailIndex < _numOfThumbnails; ++thumbnailIndex)
			for (final Category category : Category.values()) {
				final int numberOfThumbnailsForCurrentCategory = _categoriesThumbnailsManager
						.getNumberOfThumbnailsForCategory(category);
				if (thumbnailIndex >= numberOfThumbnailsForCurrentCategory)
					continue;
				// final String
				// bitmapId=_categoriesThumbnailsManager.getThumbnailImage(category,thumbnailIndex);
				// if(bitmapId==null)
				// continue;
				// final int
				// resID=resources.getIdentifier(bitmapId,"drawable",_appContext.getPackageName());
				// final Bitmap
				// imageToStore=BitmapFactory.decodeResource(resources,resID);
				final Bitmap imageToStore = getBitmapByIndex(thumbnailIndex,
						category, resources);
				if (imageToStore == null) {
					continue;
				}
				final int imageSize = getBitmapSize(imageToStore);
				totalCacheUsage += imageSize;
				if (totalCacheUsage > CACHE_SIZE_LIMIT) {
					// Logger.log(LogLevel.DEBUG, "number of cached images:" +
					// numberOfCachedImageViews);
					imageToStore.recycle();
					return;
				}
				// Logger.log(LogLevel.DEBUG, "number of cached images:" +
				// numberOfCachedImageViews);
				_imageCache.get(category).add(imageToStore);
				// ++numberOfCachedImageViews;
			}
		// Logger.log(LogLevel.DEBUG, "number of cached images:" +
		// numberOfCachedImageViews);
	}

	protected Bitmap getBitmapByIndex(int thumbnailIndex, Category category,
			Resources resources) {
		final String bitmapId = _categoriesThumbnailsManager.getThumbnailImage(
				category, thumbnailIndex);
		if (bitmapId == null) {
			return null;
		}
		final int resID = resources.getIdentifier(bitmapId, "drawable",
				_appContext.getPackageName());
		final Bitmap bitmap = BitmapFactory.decodeResource(resources, resID);
		return bitmap;
	}

	/**
	 * Get the size in bytes of a bitmap.
	 * 
	 * @param bitmap
	 * @return size in bytes
	 */
	public static int getBitmapSize(final Bitmap bitmap) {
		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
		// return bitmap.getByteCount();
		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	public void initializeImageAdapter(final GridView gridViewToHandle,
			final int gridViewHeightWhenFull) {
		_gridViewHeightWhenFull = gridViewHeightWhenFull;
		gridViewToHandle.setNumColumns(_numOfThumbnailsPerRow);
		_thumbnailWidth = gridViewToHandle.getWidth() / _numOfThumbnailsPerRow;
		final Context context = gridViewToHandle.getContext();
		_appContext = context.getApplicationContext();
		_inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		runCachingProcess();
	}

	public int getGridViewHeight() {
		return _gridViewHeightWhenFull;
	}

	protected boolean setThumbnailSizeIfNeeded(final View thumbnail) {
		// if(_layoutParametersForEachThumbnail==null)
		// {
		// final int gridViewHeight=getGridViewHeight();
		// final int
		// thumbnailHeight=gridViewHeight/NUMBER_OF_THUMBNAILS_PER_COLUMN;
		// Logger.log(LogLevel.DEBUG, "got height : " + thumbnailHeight);
		// _layoutParametersForEachThumbnail=new
		// GridView.LayoutParams(_thumbnailWidth,thumbnailHeight);
		// return true;
		// }
		// final LayoutParams lastUsedLayoutParams=thumbnail.getLayoutParams();
		// if(lastUsedLayoutParams!=_layoutParametersForEachThumbnail)
		// {
		// Logger.log(LogLevel.DEBUG, "got into if");
		//
		// thumbnail.setLayoutParams(_layoutParametersForEachThumbnail);
		// return true;
		// }

		final int gridViewHeight = getGridViewHeight();
		final int thumbnailHeight = gridViewHeight / _numOfThumbnailsPerColumn;
		_layoutParametersForEachThumbnail = new GridView.LayoutParams(
				_thumbnailWidth, thumbnailHeight);
		thumbnail.setLayoutParams(_layoutParametersForEachThumbnail);
		return true;
	}

	/**
	 * returns an imageView for each item that the adapter needs to give to
	 * whoever uses it
	 */
	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {

		// Logger.log(LogLevel.DEBUG, "showing thumbnail of position:" +
		// position);
		View inflatedView = null;// =convertView;
		if (inflatedView == null)
			inflatedView = _inflater.inflate(
					R.layout.main_activity_grid_view_thumbnail, parent, false);
		final String bitmapId = _categoriesThumbnailsManager.getThumbnailImage(
				_currentCategory, position);
		inflatedView.setContentDescription(bitmapId);
		final ImageView thumbnailImageView = (ImageView) inflatedView
				.findViewById(R.id.mainActivity_gridViewThumbnail);

		final ArrayList<Bitmap> arrayList = _imageCache.get(_currentCategory);
		// final ArrayList<Bitmap> arrayList =
		// _imagesAccordingToItemsAmount.get(_currentCategory);
		// final View thumbnailView =
		// _inflater.inflate(R.layout.main_activity_grid_view_thumbnail, null);
		// try fetching image from cache:
		if (position < arrayList.size()) {
			final Bitmap thumbnailBitmap = arrayList.get(position);
			if (thumbnailBitmap != null && !thumbnailBitmap.isRecycled()) {
				thumbnailImageView.setImageBitmap(thumbnailBitmap);
				// Log.d(App.APPLICATION_TAG, CLASS_TAG + "got cached image:" +
				// _currentCategory + " " + position);
				setThumbnailSizeIfNeeded(inflatedView);
				return inflatedView;
			}
		}
		// if we didn't get image from cache , create it now:
		// ImageView thumbnailImageView = (ImageView) convertView;
		final int resID = _appContext.getResources().getIdentifier(bitmapId,
				"drawable", _appContext.getPackageName());
		thumbnailImageView.setImageResource(resID);
		setThumbnailSizeIfNeeded(inflatedView);
		return inflatedView;
	}

	/** returns the number of items for the image adapter */
	@Override
	public int getCount() {
		return _numOfThumbnails;
	}

	@Override
	public Object getItem(final int position) {
		return null;
	}

	@Override
	public long getItemId(final int position) {
		return 0;
	}

	public void setNumOfItemsToShow(int numOfItemsToShow) {
		_numOfThumbnails = numOfItemsToShow;
		resetNumOfThumbnailsPerRow();
		resetNumOfThumbnailsPerColumn();
	}

	protected void resetNumOfThumbnailsPerRow() {
		switch (_numOfThumbnails) {
		case 4:
			_numOfThumbnailsPerRow = NUMBER_OF_THUMBNAILS_PER_ROW_4;
			break;
		case 6:
			_numOfThumbnailsPerRow = NUMBER_OF_THUMBNAILS_PER_ROW_6;
			break;
		case 9:
			_numOfThumbnailsPerRow = NUMBER_OF_THUMBNAILS_PER_ROW_9;
			break;
		case 12:
			_numOfThumbnailsPerRow = NUMBER_OF_THUMBNAILS_PER_ROW_12;
			break;
		}
	}

	protected void resetNumOfThumbnailsPerColumn() {
		_numOfThumbnailsPerColumn = _numOfThumbnails / _numOfThumbnailsPerRow;
	}

	public int getNumOfItemsToShow() {
		return _numOfThumbnails;
	}

	public Category getCategory() {
		return _currentCategory;
	}

	public ArrayList<String> getThumbnailsArray() {
		return _categoriesThumbnailsManager
				.getThumbnailsArrayList(_currentCategory);
	}

	/**
	 * Given two indices ,this method swaps two objects accordingly in the
	 * thumbnails array and updates the imageCache as well .
	 **/
	public void swapTwoIndicesInThumbArrayAndCache(int firstIdx, int secondIdx) {
		final int cacheArraySize = _imageCache.get(_currentCategory).size();
		final ArrayList<Bitmap> catBitmapArray = _imageCache
				.get(_currentCategory);
		final Resources resources = _appContext.getResources();
		String tmp = getThumbnailsArray().get(firstIdx);
		Bitmap tmpBitmap = null;
		// / Create temporary bitmap in case both of the indices are found in
		// the _imageCache
		if ((firstIdx < cacheArraySize) && (secondIdx < cacheArraySize)) {
			tmpBitmap = getBitmapByIndex(firstIdx, _currentCategory, resources);
		}

		// / Set the firstIndx to be the object of secondIndx
		if (firstIdx < cacheArraySize) {
			Bitmap bitmapToSet = getBitmapByIndex(secondIdx, _currentCategory,
					resources);
			catBitmapArray.set(firstIdx, bitmapToSet);
		}

		getThumbnailsArray().set(firstIdx, getThumbnailsArray().get(secondIdx));

		// Set the secondIndx to be the tmp object
		if (secondIdx < cacheArraySize) {

			catBitmapArray.set(secondIdx, tmpBitmap);
		}
		getThumbnailsArray().set(secondIdx, tmp);
	}

	/**
	 * sets the category index for the image adapter. note that in order to
	 * update the gridview, you still need to call notifyDataSetChanged
	 */
	public void setCategory(final Category givenCategory) {
		switch (givenCategory) {
		case ANIMALS:
			setSubCategories(Category.ANIMALS, Category.ANIMALS_4,
					Category.ANIMALS_6, Category.ANIMALS_9);
			return;
		case WILD_ANIMALS:
			setSubCategories(Category.WILD_ANIMALS, Category.WILD_ANIMALS_4,
					Category.WILD_ANIMALS_6, Category.WILD_ANIMALS_9);
			return;
		case WILD_BIRDS:
			setSubCategories(Category.WILD_BIRDS, Category.WILD_BIRDS_4,
					Category.WILD_BIRDS_6, Category.WILD_BIRDS_9);
			return;
		case VEHICLES:
			setSubCategories(Category.VEHICLES, Category.VEHICLES_4,
					Category.VEHICLES_6, Category.VEHICLES_9);
			return;
		case MUSICAL_INSTRUMENTS:
			setSubCategories(Category.MUSICAL_INSTRUMENTS,
					Category.MUSICAL_INSTRUMENTS_4,
					Category.MUSICAL_INSTRUMENTS_6,
					Category.MUSICAL_INSTRUMENTS_9);
			return;
		case HOUSEHOLD:
			setSubCategories(Category.HOUSEHOLD, Category.HOUSEHOLD_4,
					Category.HOUSEHOLD_6, Category.HOUSEHOLD_9);
			return;
		}
	}

	protected void setSubCategories(Category givenCategory, Category category4,
			Category category6, Category category9) {
		if(_categoriesThumbnailsManager.getThumbnailsArrayList(givenCategory).size() < 4){
			_currentCategory = givenCategory;
			return;
		}
		switch (_numOfThumbnails) {
		case 4:
			_currentCategory = category4;
			return;
		case 6:
			_currentCategory = category6;
			return;
		case 9:
			_currentCategory = category9;
			return;
		case 12:
			_currentCategory = givenCategory;
			return;
		}
	}

}
