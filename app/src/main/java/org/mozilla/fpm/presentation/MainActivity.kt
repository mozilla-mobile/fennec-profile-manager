/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.mozilla.fpm.BuildConfig
import org.mozilla.fpm.R
import org.mozilla.fpm.data.BackupRepositoryImpl
import org.mozilla.fpm.data.PrefsManager
import org.mozilla.fpm.models.Backup
import org.mozilla.fpm.presentation.mvp.MainContract
import org.mozilla.fpm.presentation.mvp.MainPresenter
import org.mozilla.fpm.utils.Utils

class MainActivity : AppCompatActivity(), MainContract.View, BackupsRVAdapter.MenuListener {
    private lateinit var presenter: MainPresenter
    private lateinit var adapter: BackupsRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        BackupRepositoryImpl.setContext(applicationContext)
        title = getString(R.string.app_name_full)
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
        refresh_layout.isEnabled = false

        if (PrefsManager.checkFirstRun()) showFirstrun()

        getBackups()
      
        create_fab.setOnClickListener {
            attemptCreate()
            hideFirstrun()
        }
        import_fab.setOnClickListener {
            presenter.importBackup()
            hideFirstrun()
        }
    }

    override fun onBackupsLoaded(data: List<Backup>) {
        if (data.isEmpty()) prompt.visibility = View.VISIBLE else {
            prompt.visibility = View.GONE
            adapter.updateData(data)
            adapter.setListener(this@MainActivity)
        }
    }

    override fun onBackupCreated(backup: Backup) {
        prompt.visibility = View.GONE
        adapter.add(backup)
        adapter.setListener(this@MainActivity)
    }

    override fun onApplyClick(item: Backup) {
        if (Utils.makeFirefoxPackageContext(this) == null) {
            showMessage(getString(R.string.error_shareduserid, BuildConfig.FIREFOX_PACKAGE_NAME))
            return
        }

        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
        builder.setTitle(getString(R.string.warning_title))
        builder.setMessage(getString(R.string.warning_message))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ -> presenter.applyBackup(item.name) }
        builder.setNegativeButton(getString(R.string.no), null)
        builder.show()
    }

    override fun onShareClick(item: Backup) {
        TODO("not implemented")
    }

    @SuppressLint("InflateParams")
    override fun onEditClick(item: Backup, position: Int) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
        val inflater = layoutInflater
        builder.setTitle(getString(R.string.edit_backup_name))
        val dialogLayout = inflater.inflate(R.layout.alert_input, null)
        val input = dialogLayout.findViewById<EditText>(R.id.input)
        input.setText(item.name.replace(".zip", ""))
        builder.setView(dialogLayout)
        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
            run {
                if (input.text.isEmpty()) {
                    showMessage(getString(R.string.error_input_null))
                    return@setPositiveButton
                }

                presenter.renameBackup(item, input.text.toString())
                adapter.update(Backup(input.text.toString(), item.createdAt), position)
            }
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.show()
    }

    override fun onDeleteClick(item: Backup, position: Int) {
        presenter.deleteBackup(item.name)
        adapter.delete(position)

        if (adapter.itemCount == 0) prompt.visibility = View.VISIBLE
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

    override fun showLoading() {
        refresh_layout.isRefreshing = true
    }

    override fun hideLoading() {
        refresh_layout.isRefreshing = false
    }

    override fun onBackupApplied() {
        showMessage(getString(R.string.backup_applied))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    /**
     * In order to allow for easier debugging and ease of access to backup files,
     * debug variants will store the backup archives inexternal storage.
     */
    fun getBackups() {
        if (!BuildConfig.DEBUG) {
            presenter.getBackups()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE
            )
            return
        }

        presenter.getBackups()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showMessage(getString(R.string.storage_permission_denied))
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE
                    )
                    Log.i(TAG, "Permission has been denied by user")
                } else {
                    presenter.getBackups()
                    Log.i(TAG, "Permission has been granted by user")
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("InflateParams")
    fun attemptCreate() {
        if (Utils.makeFirefoxPackageContext(this) == null) {
            showMessage(getString(R.string.error_shareduserid, BuildConfig.FIREFOX_PACKAGE_NAME))
            return
        }

        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
        val inflater = layoutInflater
        builder.setTitle(getString(R.string.set_backup_name))
        val dialogLayout = inflater.inflate(R.layout.alert_input, null)
        val input = dialogLayout.findViewById<EditText>(R.id.input)
        builder.setView(dialogLayout)
        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
            run {
                if (input.text.isEmpty()) {
                    showMessage(getString(R.string.error_input_null))
                    return@setPositiveButton
                }

                presenter.createBackup(input.text.toString())
            }
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.show()
    }

    fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private val TAG = MainActivity::class.java.canonicalName
        private const val STORAGE_REQUEST_CODE = 100
    }
}
