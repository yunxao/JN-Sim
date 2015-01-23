/* 
 * @(#)LinkedHashMap.java   0.1   2002/01
 *
 * (C) Copyright Infonet Group 2002 - FUNDP
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package infonet.javasim.util;

import java.util.*;

/**
 * <p>
 * LinkedHashMap is a extension of HashMap which uses linked list as bucket
 * (<code>infonet.javasim.util.Bucket</code>), it allow to store the
 * different values of a key collision into a single bucket.
 * Be aware (thank's JCVD :-) that this implementation is <strong>synchronised</strong> : all
 * inherited methods have been overridded to be synchronised and new methodes are synchronised to.
 * 
 * <p>
 * Most of the methods of HashMap are simply wrapped into a synchronized call to
 * <code>super.theMethods()</code>, and other are "really" overridded, i.e. have new implementation in
 * addition to be synchronized.
 *
 * <p>
 * The "really" overridded methods are only :
 * <ul>
 *  <li><code>Object put(Object key, Object value)</code></li>
 *  <li><code>boolean containsValue(Object value)</code></li>
 * </ul>
 *
 * <p>
 * New Methods are : 
 * <ul>
 *  <li><code>Bucket put(Object key, Object value)</code></li>
 *  <li><code>Object remove(Object key, Object value)</code></li>
 *  <li><code>LinkedList allValues()</code></li>
 *  <li><code>boolean containsBucket(infonet.javasim.util.Bucket bucket)</code></li>
 *  <li><code>boolean removeAll(Object key, Object value)</code></li>
 * </ul>
 *
 * @author Pierre Reinbold
 * @version 0.1 2002/01
 */
public class LinkedHashMap extends HashMap
{
    /**
     * Default Constructor, the LinkedHashMap is : 
     * <ul>
     * <li>unsynchronized</li>
     * <li>created with default capacity (101) and load factor 75%</li>
     * </ul>
     */
    public LinkedHashMap()
    {
        super();
    }
    
    /**
     * Constructor, the LinkedHashMap is : 
     * <ul>
     * <li>unsynchronized</li>
     * <li>created with capacity (101) of initialCapacity and load factor 75%</li>
     * </ul>
     *
     * @param initialCapacity   the initial capacity
     */
    public LinkedHashMap(int initialCapacity)
    {
        super(initialCapacity);
    }
    
    /**
     * Constructor, the LinkedHashMap is : 
     * <ul>
     * <li>unsynchronized</li>
     * <li>created with given capacity and load factor</li>
     * </ul>
     *
     * @param initialCapacity   the initial capacity
     * @param loadFactor        the initial load factor
     */
    public LinkedHashMap(int initialCapacity, float loadFactor)
    {
        super(initialCapacity, loadFactor);
    }
//*********** Synchronized Methods *******************************************************************

    public synchronized void clear()
    {
        super.clear();
    }

    public synchronized Object clone()
    {
        return(super.clone());
    }
        
    public synchronized Collection values()
    {
        return(super.values());
    }
        
    public synchronized void putAll(Map t)
    {
        super.putAll(t);
    }
    
    public synchronized Object remove(Object key)
    {
        return(super.remove(key));
    }
    
    public synchronized Object get(Object key)
    {
        return(super.get(key));
    }
    
    public synchronized boolean containsKey(Object key)
    {
        return(super.containsKey(key));
    }
    
    public synchronized boolean isEmpty()
    {
        return(super.isEmpty());
    }
    
    public synchronized int size()
    {
        return(super.size());
    }
    
    public synchronized Set keySet()
    {
        return(super.keySet());
    }

    public synchronized Set entrySet()
    {
        return(super.entrySet());
    }

//*********** Overrrided Methods *******************************************************************
    
    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the new value
     * value is placed and the end of the corresponding bucket.
     *
     * @param key           the key with which the specified value is to be associated.
     * @param value         the value to be associated with the specified key.
     *
     * @return              <code>null</code>
     *
     */
    public synchronized Object put(Object key, Object value)
    {
        if (containsKey(key))
        {
            // insert at the end of the corresponding bucket
            ((Bucket) get(key)).add(value);
        }
        else
        {
            // insert a new synchronised bucket corresponding to key and containing value
            List l = Collections.synchronizedList(new Bucket());
            Bucket newBucket = new Bucket(l);
            newBucket.add(value);
            super.put(key, newBucket);
        }
        return(null);
    }
    
    /**
     * Returns true if this map maps one or more keys to a bucket containing the
     * specified value.
     *
     * @param value         the value whose presence in this map is to be tested.
     *
     * @return              <code>true</code> if this map maps one or more keys to a bucket 
     *                      containing the specified value.
     */
    public synchronized boolean containsValue(Object value)
    {
        // We get the Key Set and sequentially check if the value is contained in the
        // corresponding bucket
        boolean findValue = false;
        Iterator keys = ((Set) keySet()).iterator();
        while (keys.hasNext() && (! findValue))
        {
            findValue = ((Bucket) get(keys.next())).contains(value);
        }
        return findValue;
    }

//*********** New Methods **************************************************************************

    /**
     * Returns true if this map maps one or more keys to the specified bucket, an entire linked list
     * of values.
     *
     * @param bucket        the bucket whose presence in this map is to be tested.
     *
     * @return              <code>true</code> if this map maps one or more keys to the specified bucket 
     */
    public synchronized boolean containsBucket(Bucket bucket)
    {
        return(super.containsValue(bucket));
    }
    
    /**
     * Remove the first occurence of value in the bucket associated with key, if present.
     *  
     * @param key           the key associated with the bucket containing the value to be removed.
     * @param value         the value that must be removed if present in the bucket associated with
     *                      the given key.
     *
     * @return              <code>true</code> if this map maps one or more keys to a bucket
     *                      containing the specified value
     */
    public synchronized boolean remove(Object key, Object value)
    {
        if (containsKey(key))   // to ensure that get(key) is not null
        {
            return(((Bucket) get(key)).remove(value));
        }
        else
        {
            return(false);
        }
    }

    /**
     * Remove all occurence(s) of value in the bucket associated with key, if present.
     *  
     * @param key           the key associated with the bucket containing the value to be removed.
     * @param value         the value that must be removed if present in the bucket associated with
     *                      the given key.
     *
     * @return              <code>true</code> if this map maps one or more keys to a bucket
     *                      containing the specified value
     */
    public synchronized boolean removeAll(Object key, Object value)
    {
        if (containsKey(key))   // to ensure that get(key) is not null
        {
            Bucket theBucket = (Bucket) get(key);
            if (theBucket.contains(value))
            {
                while(theBucket.remove(value)){}
                return(true);
            }
            else
            {
                return(false);
            }
        }
        else
        {
            return(false);
        }
    }

    
    /**
     * Returns true if this map maps one or more keys to a bucket containing the
     * specified value.
     *
     * @param key           the key associated with the bucket containing the value to be tested.
     * @param value         the value whose presence in this map is to be tested.
     *
     * @return              <code>true</code> if this map maps key to a bucket 
     *                      containing the specified value.
     */
    public synchronized boolean containsValue(Object key, Object value)
    {
        if (containsKey(key))   // to ensure that get(key) is not null
        {
            return(((Bucket) get(key)).contains(value));
        }
        else
        {
            return(false);
        }
    }

    /**
     * Returns the key that this map maps to a bucket containing the
     * specified value, if such a key exists.
     *
     * @param value         the value whose presence in this map is to be tested.
     *
     * @return              the key mapped to a bucket containing the specified value, if such a key
     *                      exists, <code>null</code> in other cases.
     */
    public synchronized Object whatKeyMapsToValue(Object value)
    {
        // We get the Key Set and sequentially check if the value is contained in the
        // corresponding bucket. We use the toArray() method of the Key Set to return the
        // corresponding key.
        boolean findValue = false;
        Object[] keys = ((Set) keySet()).toArray();
        int i;
        for (i = 0; i < keys.length; i++)
        {
            if (((Bucket) get(keys[i])).contains(value))
            {
                return(keys[i]);
            }
        }
        return(null);
    }
    
    /**
     * Returns all the values contained in this table. 
     * The values of the different buckets are put in a LinkedList in the order of the iterator of
     * the keySet.
     *
     * @return              A LinkedList containing all the values contained in all the buckets of
     *                      the table.
     */
    public synchronized LinkedList allValues()
    {
        // We get the Key Set and sequentially add all the values contained in the
        // corresponding buckets.
        Iterator keys = ((Set) keySet()).iterator();
        LinkedList list = new LinkedList();
        while (keys.hasNext())
        {
            list.addAll((Bucket) get(keys.next()));
        }
        return(list);
    }


    
}
