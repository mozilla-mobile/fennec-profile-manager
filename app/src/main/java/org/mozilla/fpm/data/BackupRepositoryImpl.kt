package org.mozilla.fpm.data

import android.content.Context
import org.mozilla.fpm.models.Backup

object BackupRepositoryImpl: BackupRepository {
    lateinit var ctx: Context

    override fun add(t: Backup) {

    }

    override fun remove(k: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(t: Backup, k: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(k: Int): Backup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAll(): List<Backup> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}