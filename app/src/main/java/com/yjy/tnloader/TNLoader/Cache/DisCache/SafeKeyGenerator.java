package com.yjy.tnloader.TNLoader.Cache.DisCache;

import com.yjy.tnloader.TNLoader.Cache.LruCache;
import com.yjy.tnloader.TNLoader.Resource.Key;
import com.yjy.tnloader.TNLoader.Utils.Util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by yjy on 2018/1/31.
 */

class SafeKeyGenerator {
    private final LruCache<Key, String> loadIdToSafeHash = new LruCache<Key, String>(1000);

    public String getSafeKey(Key key) {
        String safeKey;
        synchronized (loadIdToSafeHash) {
            safeKey = loadIdToSafeHash.get(key);
        }
        if (safeKey == null) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                key.updateDiskCacheKey(messageDigest);
                safeKey = Util.sha256BytesToHex(messageDigest.digest());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            synchronized (loadIdToSafeHash) {
                loadIdToSafeHash.put(key, safeKey);
            }
        }
        return safeKey;
    }
}
