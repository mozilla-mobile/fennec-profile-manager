/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.data

interface Repository<T, K> {
    fun create(k: K)
    fun remove(k: K)
    fun update(t: T, k: K)
    fun get(k: K): T?
    fun getAll(): List<T>
}
