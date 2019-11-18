/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.mozilla.fpm.R
import org.mozilla.fpm.models.Backup
import org.mozilla.fpm.presentation.mvp.MainContract
import org.mozilla.fpm.presentation.mvp.MainPresenter

class MainActivity : AppCompatActivity(), MainContract.View {
    private val TAG = MainActivity::class.java.canonicalName

    private lateinit var presenter: MainPresenter
    private lateinit var adapter: BackupsRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        title = getString(R.string.app_name)
        presenter = MainPresenter()
        presenter.attachView(this@MainActivity)

        adapter = BackupsRVAdapter()
        backups_rv.layoutManager = LinearLayoutManager(this@MainActivity)
        backups_rv.addItemDecoration(DividerItemDecoration(this@MainActivity, LinearLayoutManager.VERTICAL))
        backups_rv.adapter = adapter
        presenter.getBackups()

        create_fab.setOnClickListener {presenter.createBackup()}
        import_fab.setOnClickListener {presenter.importBackup()}
    }

    override fun onBackupsLoaded(data: List<Backup>) {
        adapter.updateData(data)
    }

    override fun onBackupCreated(backup: Backup) {
        adapter.add(backup)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}
