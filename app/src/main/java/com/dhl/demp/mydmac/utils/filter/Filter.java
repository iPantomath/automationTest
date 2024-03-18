package com.dhl.demp.mydmac.utils.filter;

/**
 * Created by robielok on 11/29/2017.
 */

public interface Filter<T> {
    boolean match(T item);
}
