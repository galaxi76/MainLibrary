 package com.soundtouch.main_library.activities.main_activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

import com.soundtouch.main_library.Logger;
import com.soundtouch.main_library.Logger.LogLevel;

/** a class for that contains the thumbnails images ids of all of the categories */
public class CategoriesThumbnailsManager {
	// protected String[][] _thumbnailsImagesIds = null;
	protected HashMap<Category, ArrayList<String>> _thumbnailsImagesIds = null;
	protected HashMap<Category, ArrayList<String>> _imagesAccordingToThumbNum = null;
	
	public enum Category {
		UNKNOWN(-1), ANIMALS(0), WILD_ANIMALS(1), WILD_BIRDS(2), VEHICLES(3), MUSICAL_INSTRUMENTS(
				4), HOUSEHOLD(5), ANIMALS_4(6), WILD_ANIMALS_4(7), WILD_BIRDS_4(
				8), VEHICLES_4(9), MUSICAL_INSTRUMENTS_4(10), HOUSEHOLD_4(11), ANIMALS_6(
				12), WILD_ANIMALS_6(13), WILD_BIRDS_6(14), VEHICLES_6(15), MUSICAL_INSTRUMENTS_6(
				16), HOUSEHOLD_6(17), ANIMALS_9(18), WILD_ANIMALS_9(19), WILD_BIRDS_9(
				20), VEHICLES_9(21), MUSICAL_INSTRUMENTS_9(22), HOUSEHOLD_9(23);
		private int _categoryNumber;

		Category(final int categoryNumber) {
			_categoryNumber = categoryNumber;
		}

		public int getCategoryNumber() {
			return _categoryNumber;
		}

		public static Category fromValue(final int value) {
			for (final Category enumValue : Category.values())
				if (enumValue.getCategoryNumber() == value)
					return enumValue;
			return UNKNOWN;
		}
	}

	public CategoriesThumbnailsManager() {
		initThumbnailsImagesIds();
	}

	protected static ArrayList<String> createThumbnailsIdsArray(
			final String[] ids) {
		final ArrayList<String> result = new ArrayList<String>();
		for (final String string : ids)
			result.add(string);
		return result;
	}

	/**
	 * initializes the categories images array.you should override this method
	 * and put there the correct images resources ids to show
	 */
	public void initThumbnailsImagesIds() {
		if (_thumbnailsImagesIds != null)
			return;
		_thumbnailsImagesIds = new HashMap<CategoriesThumbnailsManager.Category, ArrayList<String>>();
		_thumbnailsImagesIds.put(Category.ANIMALS, new ArrayList<String>());
		_thumbnailsImagesIds
				.put(Category.WILD_ANIMALS, new ArrayList<String>());
		_thumbnailsImagesIds.put(Category.WILD_BIRDS, new ArrayList<String>());
		_thumbnailsImagesIds.put(Category.VEHICLES, new ArrayList<String>());
		_thumbnailsImagesIds.put(Category.MUSICAL_INSTRUMENTS,
				new ArrayList<String>());
		_thumbnailsImagesIds.put(Category.HOUSEHOLD, new ArrayList<String>());
		
	}

	/** returns the number of thumbnails for the specified category index */
	public int getNumberOfThumbnailsForCategory(final Category category) {

		final ArrayList<String> arrayList = _thumbnailsImagesIds.get(category);
		if (arrayList == null)
			return 0;
		return arrayList.size();
	}

	/**
	 * returns the thumbnail image id of the specified category index and the
	 * thumbnail index
	 */
	public String getThumbnailImage(final Category category,
			final int thumbnailIndex) 
	{
		final ArrayList<String> arrayList = _thumbnailsImagesIds.get(category);

		if (arrayList == null) {
			Logger.log(LogLevel.WARNING, "got an invalid category");
			return null;
		}
		if (thumbnailIndex < 0 || thumbnailIndex >= arrayList.size()) {
			Logger.log(LogLevel.WARNING, "got an invalid thumbnail index:"
					+ thumbnailIndex + " when size is:" + arrayList.size());
			return null;
		}
		return arrayList.get(thumbnailIndex);
	}

	protected void arrangeImages() {
		_imagesAccordingToThumbNum = new HashMap<CategoriesThumbnailsManager.Category, ArrayList<String>>();
		int numOfCategories = _thumbnailsImagesIds.size();
		Category[] cat = { Category.ANIMALS, Category.WILD_ANIMALS,
				Category.WILD_BIRDS, Category.HOUSEHOLD, Category.VEHICLES,
				Category.MUSICAL_INSTRUMENTS };
		for (int i = 0; i < numOfCategories; i++) {
			ArrayList<String> imagesToPut = new ArrayList<String>();

			for (int j = 0; j < 4; j++) {
				imagesToPut.add(_thumbnailsImagesIds.get(cat[i]).get(j));
			}
			_imagesAccordingToThumbNum.put(cat[i], imagesToPut);
		}
	}

	public ArrayList<String> getThumbnailsArrayList(Category category) {
		return _thumbnailsImagesIds.get(category);
	}
	protected void setThumbnailsForCategory(Category category , ArrayList<String> thumbnailsArray){
		_thumbnailsImagesIds.put(category, thumbnailsArray);
	}
	
	protected void initSubCategories(Category category){
		switch(category){
		
		case ANIMALS:
		    _thumbnailsImagesIds.put(Category.ANIMALS_4, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"sheep" , "dog" ,"cow" , "cat"} ))));
		    _thumbnailsImagesIds.put(Category.ANIMALS_6, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"sheep" , "dog" ,"cow" , "cat" ,"chicken" ,"horse"} ))));
		    _thumbnailsImagesIds.put(Category.ANIMALS_9, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"sheep","dog","horse","cow","chicken","pig","duck" ,"donkey","cat"} ))));
		    break;
		case WILD_ANIMALS:
		    _thumbnailsImagesIds.put(Category.WILD_ANIMALS_4, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"elephant" , "monkey" ,"lion" , "zebra"} ))));
		    _thumbnailsImagesIds.put(Category.WILD_ANIMALS_6, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"elephant" , "monkey" , "zebra", "dolphin"  ,  "bear","lion" } ))));
		    _thumbnailsImagesIds.put(Category.WILD_ANIMALS_9, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"elephant" ,"kangaroo","monkey" ,"lion" , "zebra" , "dolphin", "bear" , "leopard","camel"} ))));
		    break;
		case WILD_BIRDS:
		    _thumbnailsImagesIds.put(Category.WILD_BIRDS_4, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"flamingo" , "owl" ,"penguin" , "parrot"} ))));
		    _thumbnailsImagesIds.put(Category.WILD_BIRDS_6, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"flamingo" , "owl" ,"penguin" , "parrot" ,"ostrich", "pelican"} ))));
		    _thumbnailsImagesIds.put(Category.WILD_BIRDS_9, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"flamingo" , "owl" ,"penguin" , "parrot" ,"ostrich", "goose" ,"peacock", "eagle","swan"} ))));
		    break;
		case VEHICLES:
		    _thumbnailsImagesIds.put(Category.VEHICLES_4, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"airplane" , "ship" ,"car" , "train"} ))));
		    _thumbnailsImagesIds.put(Category.VEHICLES_6, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"airplane" , "firetruck" ,  "bicycle" , "ship" , "car" , "train"}))));
		    _thumbnailsImagesIds.put(Category.VEHICLES_9, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"airplane" , "firetruck"  ,"bicycle" , "bus" ,"car","carriage","truck","ship","train"} ))));
		    break;
		case MUSICAL_INSTRUMENTS:
		    _thumbnailsImagesIds.put(Category.MUSICAL_INSTRUMENTS_4, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"piano" , "flute" ,"drums" , "guitar"} ))));
		    _thumbnailsImagesIds.put(Category.MUSICAL_INSTRUMENTS_6, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"trumpet","violin","piano","flute" ,"drums","guitar"} ))));
		    _thumbnailsImagesIds.put(Category.MUSICAL_INSTRUMENTS_9, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"trumpet","accordion","violin","piano","flute","xylophone","harmonica","drums","guitar"} ))));
		    break;
		case HOUSEHOLD:
		    _thumbnailsImagesIds.put(Category.HOUSEHOLD_9, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"clock" ,"mixer", "faucet","cooking","dishes","bottle","door" , "toy","cleaning"} ))));
		    _thumbnailsImagesIds.put(Category.HOUSEHOLD_6, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"clock" , "door" ,"cooking" ,"toy","faucet","cleaning"} ))));
		    _thumbnailsImagesIds.put(Category.HOUSEHOLD_4, new ArrayList<String>((createThumbnailsIdsArray(new String[] {"clock" , "door" ,"cooking" , "faucet"} ))));
		    break;
		}	
	}
	
	protected void initAllSubCategories(){
		
		initSubCategories(Category.ANIMALS);
		initSubCategories(Category.WILD_ANIMALS);
		initSubCategories(Category.HOUSEHOLD);
		initSubCategories(Category.MUSICAL_INSTRUMENTS);
		initSubCategories(Category.WILD_BIRDS);
		initSubCategories(Category.VEHICLES);
	}
	

	/**
	 * Get the matching sub cateogry for any given cateogry and the amount of items should be displayed
	 */
	public Category getSubCategory(final Category givenCategory, int numOfThumbnails) {
		Logger.log(LogLevel.DEBUG,"GOT NUM OF THUMBNAILS " + numOfThumbnails);
		switch (givenCategory) {
		case ANIMALS:
			return getSubCategory(Category.ANIMALS, Category.ANIMALS_4,
					Category.ANIMALS_6, Category.ANIMALS_9,numOfThumbnails);
		case WILD_ANIMALS:
			return getSubCategory(Category.WILD_ANIMALS, Category.WILD_ANIMALS_4,
					Category.WILD_ANIMALS_6, Category.WILD_ANIMALS_9,numOfThumbnails);
		case WILD_BIRDS:
			return getSubCategory(Category.WILD_BIRDS, Category.WILD_BIRDS_4,
					Category.WILD_BIRDS_6, Category.WILD_BIRDS_9,numOfThumbnails);
		case VEHICLES:
			getSubCategory(Category.VEHICLES, Category.VEHICLES_4,
					Category.VEHICLES_6, Category.VEHICLES_9,numOfThumbnails);
		case MUSICAL_INSTRUMENTS:
			getSubCategory(Category.MUSICAL_INSTRUMENTS,
					Category.MUSICAL_INSTRUMENTS_4,
					Category.MUSICAL_INSTRUMENTS_6,
					Category.MUSICAL_INSTRUMENTS_9,numOfThumbnails);
		case HOUSEHOLD:
			getSubCategory(Category.HOUSEHOLD, Category.HOUSEHOLD_4,
					Category.HOUSEHOLD_6, Category.HOUSEHOLD_9,numOfThumbnails);
		}
		return givenCategory;
	}

	protected Category getSubCategory(Category givenCategory, Category category4,
			Category category6, Category category9,int numOfThumbnails) {
		Category _currentCategory;
		if(getThumbnailsArrayList(givenCategory).size() < 4){
			_currentCategory = givenCategory;
			return givenCategory;
		}
		switch (numOfThumbnails) {
		case 4:
			_currentCategory = category4;
			return category4;
		case 6:
			_currentCategory = category6;
			return category6;
		case 9:
			_currentCategory = category9;
			return category9;
		case 12:
			_currentCategory = givenCategory;
			return givenCategory;
		}
		return givenCategory;
	}
	
	public boolean isCategoryFull(Category category){
		if(_thumbnailsImagesIds.get(category).size() == 12){
			return true;
		}
		return false;
	}
}
