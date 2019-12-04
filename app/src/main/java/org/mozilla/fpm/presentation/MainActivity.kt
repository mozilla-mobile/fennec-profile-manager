/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.presentation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.mozilla.fpm.R
import org.mozilla.fpm.data.BackupRepositoryImpl
import org.mozilla.fpm.data.PrefsManager
import org.mozilla.fpm.models.Backup
import org.mozilla.fpm.presentation.mvp.MainContract
import org.mozilla.fpm.presentation.mvp.MainPresenter

class MainActivity : AppCompatActivity(), MainContract.View, BackupsRVAdapter.MenuListener {
    private lateinit var presenter: MainPresenter
    private lateinit var adapter: BackupsRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        BackupRepositoryImpl.setContext(applicationContext)
        title = getString(R.string.app_name)
        presenter = MainPresenter()
        presenter.attachView(this@MainActivity)

        adapter = BackupsRVAdapter()
        backups_rv.layoutManager = LinearLayoutManager(this@MainActivity)
        backups_rv.addItemDecoration(
            DividerItemDecoration(
                this@MainActivity,
                LinearLayoutManager.VERTICAL
            )
        )
        backups_rv.adapter = adapter

        if (PrefsManager.checkFirstRun()) showFirstrun()

        presenter.getBackups()

        create_fab.setOnClickListener { presenter.createBackup().also { hideFirstrun() } }
        import_fab.setOnClickListener { presenter.importBackup().also { hideFirstrun() } }
    }

    override fun onBackupsLoaded(data: List<Backup>) {
        adapter.updateData(data)
        adapter.setListener(this@MainActivity)
    }

    override fun onBackupCreated(backup: Backup) {
        adapter.add(backup)
    }

    override fun onApplyClick(item: Backup) {
        TODO("not implemented")
    }

    override fun onShareClick(item: Backup) {
        TODO("not implemented")
    }

    override fun onEditClick(item: Backup) {
        TODO("not implemented")
    }

    override fun onDeleteClick(item: Backup) {
        TODO("not implemented")
    }

    override fun showFirstrun() {
        create_label.visibility = View.GONE
        import_label.visibility = View.VISIBLE
        PrefsManager.setFirstRunComplete()
    }

    override fun hideFirstrun() {
        create_label.visibility = View.GONE
        import_label.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}
