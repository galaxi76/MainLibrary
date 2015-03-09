package com.soundtouch.main_library.resource_getter;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.resources_extractor.FileInfo;
import com.soundtouch.main_library.App;
import com.soundtouch.main_library.Logger;
import com.soundtouch.main_library.Logger.LogLevel;

public class FileMappingDbHelper extends SQLiteOpenHelper
  {
  protected static final String CLASS_TAG =FileMappingDbHelper.class.getCanonicalName();

  public FileMappingDbHelper(final Context context)
    {
    super(context,CLASS_TAG+".db",null,App.getAppVersionCode());
    }

  private static class FileMappingTable
    {
    private static final String TABLE       ="fileMappingTable";
    private static final String FILE_NAME   ="fileName";
    private static final String FILE_SIZE   ="fileSize";
    private static final String FILE_OFFSET ="fileOffset";
    }

  @Override
  public void onCreate(final SQLiteDatabase db)
    {
    createNewFileMappingTable(db);
    }

  private void createNewFileMappingTable(final SQLiteDatabase db)
    {
    final String sqlFormat="create table %s( %s integer primary key autoincrement, %s text not null unique , %s integer not null , %s integer not null)";
    final String sql=String.format(sqlFormat,FileMappingTable.TABLE,BaseColumns._ID,FileMappingTable.FILE_NAME,FileMappingTable.FILE_SIZE,FileMappingTable.FILE_OFFSET);
    db.execSQL(sql);
    }

  public void addFileMapping(final FileInfo fileInfo)
    {
    if(fileInfo==null)
      return;
    SQLiteDatabase db=null;
    final ContentValues values=new ContentValues();
    values.put(FileMappingTable.FILE_NAME,fileInfo.getFileName());
    values.put(FileMappingTable.FILE_OFFSET,fileInfo.getOffsetToHeader());
    values.put(FileMappingTable.FILE_SIZE,fileInfo.getFileSize());
    try
      {
      db=this.getWritableDatabase();
      // Logger.log(LogLevel.DEBUG, "adding all new data to new session " + currentSession);
      db.insert(FileMappingTable.TABLE,null,values);
      // Logger.log(LogLevel.DEBUG, "sucessfully submitted appLaunchReport");
      }
    catch(final Exception e)
      {
      Logger.log(LogLevel.WARNING,"couldn't write to database"+e);
      }
    finally
      {
      if(db!=null)
        db.close();
      }
    }

  public void addFileMapping(final HashMap<String,FileInfo> fileInfoMapping)
    {
    if(fileInfoMapping==null)
      return;
    // Logger.log(LogLevel.DEBUG, "writing to database...");
    SQLiteDatabase db=null;
    try
      {
      db=this.getWritableDatabase();
      db.beginTransaction();
      final InsertHelper insertHelper=new InsertHelper(db,FileMappingTable.TABLE);
      // Logger.log(LogLevel.DEBUG, "adding all new data to new session " + currentSession);
      // final ContentValues values = new ContentValues();
      final int fileNameIdx=insertHelper.getColumnIndex(FileMappingTable.FILE_NAME);
      final int fileSizeIdx=insertHelper.getColumnIndex(FileMappingTable.FILE_SIZE);
      final int fileOffsetIdx=insertHelper.getColumnIndex(FileMappingTable.FILE_OFFSET);
      for(final FileInfo fileInfo : fileInfoMapping.values())
        {
        insertHelper.prepareForReplace();
        insertHelper.bind(fileOffsetIdx,fileInfo.getOffsetToHeader());
        insertHelper.bind(fileNameIdx,fileInfo.getFileName());
        insertHelper.bind(fileSizeIdx,fileInfo.getFileSize());
        insertHelper.execute();
        }
      db.setTransactionSuccessful();
      // Logger.log(LogLevel.DEBUG, "sucessfully submitted appLaunchReport");
      }
    catch(final Exception e)
      {
      Logger.log(LogLevel.WARNING,"couldn't write to database"+e);
      }
    finally
      {
      // Logger.log(LogLevel.DEBUG, "done writing to database ");
      if(db!=null)
        {
        db.endTransaction();
        db.close();
        }
      }
    }

  public HashMap<String,FileInfo> getFileMapping()
    {
    final HashMap<String,FileInfo> result=new HashMap<String,FileInfo>();
    SQLiteDatabase db=null;
    Cursor cursor=null;
    try
      {
      db=this.getReadableDatabase();
      final String[] columnsToReturn= {FileMappingTable.FILE_NAME,FileMappingTable.FILE_OFFSET,FileMappingTable.FILE_SIZE};
      final String rowsToReturn=null;
      final String[] selectionArgs=null;
      final String groupBy=null,having=null,sortBy=null;
      cursor=db.query(FileMappingTable.TABLE,columnsToReturn,rowsToReturn,selectionArgs,groupBy,having,sortBy);
      final int fileNameIdx=cursor.getColumnIndex(FileMappingTable.FILE_NAME);
      final int fileOffsetIdx=cursor.getColumnIndex(FileMappingTable.FILE_OFFSET);
      final int fileSizeIdx=cursor.getColumnIndex(FileMappingTable.FILE_SIZE);
      if(cursor.moveToFirst())
        for(;!cursor.isAfterLast();cursor.moveToNext())
          {
          final String fileName=cursor.getString(fileNameIdx);
          final long fileOffset=cursor.getLong(fileOffsetIdx);
          final long fileSize=cursor.getLong(fileSizeIdx);
          final FileInfo fileInfo=new FileInfo(fileName,fileOffset,fileSize);
          result.put(fileName,fileInfo);
          }
      }
    catch(final Exception e)
      {
      return null;
      }
    finally
      {
      if(cursor!=null)
        cursor.close();
      if(db!=null)
        db.close();
      }
    return result;
    }

  public FileInfo getFileMapping(final String fileNameToGetItsInfo)
    {
    FileInfo result=null;
    SQLiteDatabase db=null;
    Cursor cursor=null;
    try
      {
      db=this.getReadableDatabase();
      final String[] columnsToReturn= {FileMappingTable.FILE_NAME,FileMappingTable.FILE_OFFSET,FileMappingTable.FILE_SIZE};
      final String rowsToReturn=FileMappingTable.FILE_NAME+"='"+fileNameToGetItsInfo+"'";
      final String[] selectionArgs=null;
      final String groupBy=null,having=null,sortBy=null;
      cursor=db.query(FileMappingTable.TABLE,columnsToReturn,rowsToReturn,selectionArgs,groupBy,having,sortBy);
      final int fileNameIdx=cursor.getColumnIndex(FileMappingTable.FILE_NAME);
      final int fileOffsetIdx=cursor.getColumnIndex(FileMappingTable.FILE_OFFSET);
      final int fileSizeIdx=cursor.getColumnIndex(FileMappingTable.FILE_SIZE);
      if(cursor.moveToFirst()&&cursor.getCount()!=0)
        {
        final String fileName=cursor.getString(fileNameIdx);
        final long fileOffset=cursor.getLong(fileOffsetIdx);
        final long fileSize=cursor.getLong(fileSizeIdx);
        result=new FileInfo(fileName,fileOffset,fileSize);
        }
      }
    catch(final Exception e)
      {
      return null;
      }
    finally
      {
      if(cursor!=null)
        cursor.close();
      if(db!=null)
        db.close();
      }
    return result;
    }

  @Override
  public void onUpgrade(final SQLiteDatabase db,final int oldVersion,final int newVersion)
    {
    Logger.log(LogLevel.DEBUG,"file mapping DB is being deleted because of an upgrade of the app");
    final String sqlFormat="DROP TABLE IF EXISTS %s;";
    final String sql=String.format(sqlFormat,FileMappingTable.TABLE);
    try
      {
      db.execSQL(sql);
      }
    catch(final Exception e)
      {
      Logger.log(LogLevel.WARNING,"error while deleting file mapping DB : "+e);
      }
    createNewFileMappingTable(db);
    }
  }
