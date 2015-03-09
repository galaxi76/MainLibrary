package com.soundtouch.main_library.activities.main_activity;

import java.lang.ref.SoftReference;
import java.util.HashMap;


public class Cache<CacheKey, CacheData>
{
  public interface IDataBuilder<CacheKey, CacheData>
  {
    CacheData build(CacheKey cacheKey);
  }

  private final IDataBuilder<CacheKey, CacheData>           _cacheBuilder;
  private final HashMap<CacheKey, SoftReference<CacheData>> _cache = new HashMap<CacheKey, SoftReference<CacheData>>();

  public Cache(final IDataBuilder<CacheKey, CacheData> builder)
  {
    _cacheBuilder = builder;
  }

  public CacheData getItemFromCache(final CacheKey key)
  {
    SoftReference<CacheData> softReference = _cache.get(key);
    CacheData result = softReference == null ? null : softReference.get();
    if (result == null)
    {
      result = _cacheBuilder.build(key);
      softReference = new SoftReference<CacheData>(result);
      _cache.put(key, softReference);
    }
    // else
    // Logger.log(LogLevel.DEBUG, "got item from cache :" + key);
    return result;
  }

  public void putItemIntoCache(final CacheKey key, final CacheData data)
  {
    final SoftReference<CacheData> softReference = new SoftReference<CacheData>(data);
    _cache.put(key, softReference);
  }
}
