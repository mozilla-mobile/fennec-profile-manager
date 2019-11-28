package org.mozilla.fpm.data

interface Repository<T, K> {
    fun add(t: T)
    fun remove(k: K)
    fun update(t: T, k: K)
    fun get(k: K): T
    fun getAll(): List<T>
}