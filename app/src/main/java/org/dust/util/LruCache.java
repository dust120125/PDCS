package org.dust.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by DuST on 2017/5/20.
 */

/**
 * 基於 LRU 演算法用以儲存 Objects 的 Cache。
 */
public class LruCache<K, V> extends HashMap<K, V> {

    private Queue<Object> counter;

    private int cacheSize;

    public LruCache(int size){
        super();
        this.cacheSize = size;
        counter = new LinkedList<>();
    }

    @Override
    public V get(Object key) {
        V result = super.get(key);
        if (result == null) return null;

        counter.remove(key);
        counter.offer(key);
        return result;
    }

    @Override
    public V put(K key, V value) {
        if (counter.size() >= cacheSize){
            counter.poll();
        }
        counter.offer(key);
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        counter.remove(key);
        return super.remove(key);
    }
}
