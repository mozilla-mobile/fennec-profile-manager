/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.presentation

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import org.mozilla.fpm.BuildConfig
import org.mozilla.fpm.R
import org.mozilla.fpm.data.BackupRepositoryImpl
import org.mozilla.fpm.data.BackupRepositoryImpl.MIME_TYPE
import org.mozilla.fpm.data.PrefsManager
import org.mozilla.fpm.models.Backup
import org.mozilla.fpm.presentation.mvp.MainContract
import org.mozilla.fpm.presentation.mvp.MainPresenter
import org.mozilla.fpm.utils.PermissionUtils.Companion.checkStoragePermission
import org.mozilla.fpm.utils.PermissionUtils.Companion.validateStoragePermissionOrShowRationale
import org.mozilla.fpm.utils.Utils.Companion.makeFirefoxPackageContext
import org.mozilla.fpm.utils.Utils.Companion.showMessage

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
        backups_rv.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    ACTION_DOWN -> create_fab.hide()
                    ACTION_UP -> create_fab.show()
                }
                return false
            }

            @Suppress("EmptyFunctionBlock")
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            @Suppress("EmptyFunctionBlock")
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
        backups_rv.layoutManager = LinearLayoutManager(this)
        backups_rv.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
        backups_rv.adapter = adapter
        refresh_layout.isEnabled = false

        if (PrefsManager.checkFirstRun()) showFirstrun()

        if (checkStoragePermission(this, BACKUPS_STORAGE_REQUEST_CODE)) presenter.getBackups()

        create_fab.setOnClickListener {
            attemptCreate()
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

    override fun onBackupImported(backup: Backup) {
        prompt.visibility = View.GONE
        adapter.add(backup)
        adapter.setListener(this@MainActivity)
    }

    override fun onApplyClick(item: Backup) {
        if (makeFirefoxPackageContext(this) == null) {
            showMessage(
                this,
                getString(R.string.error_shareduserid, BuildConfig.FIREFOX_PACKAGE_NAME)
            )
            return
        }

        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
        builder.setTitle(getString(R.string.warning_title))
        builder.setMessage(getString(R.string.warning_message))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            with(applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager) {
                killBackgroundProcesses(BuildConfig.FIREFOX_PACKAGE_NAME)
            }
            presenter.applyBackup(item.name)
        }
        builder.setNegativeButton(getString(R.string.no), null)
        builder.show()
    }

    @SuppressLint("InflateParams")
    override fun onEditClick(item: Backup, position: Int) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
        val inflater = layoutInflater
        builder.setTitle(getString(R.string.edit_backup_name))
        val dialogLayout = inflater.inflate(R.layout.alert_input, null)
        val input = dialogLayout.findViewById<EditText>(R.id.input)
        input.setText(item.name.substringBefore(".$MIME_TYPE"))
        builder.setView(dialogLayout)
        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
            val updatedBackupName = input.text.toString()
            run {
                if (updatedBackupName.isEmpty()) {
                    showMessage(this@MainActivity, getString(R.string.error_input_null))
                    return@setPositiveButton
                }

                if (item.name.substringBefore(".$MIME_TYPE") == updatedBackupName) {
                    showMessage(this, getString(R.string.error_input_unchanged))
                    return@setPositiveButton
                }

                presenter.renameBackup(item, updatedBackupName, position)
            }
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.show()
    }

    override fun onDeleteClick(item: Backup, position: Int) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
        builder.setTitle(getString(R.string.delete_backup))
        builder.setMessage(getString(R.string.delete_backup_message))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            run {
                presenter.deleteBackup(item.name)
                adapter.delete(position)

                if (adapter.itemCount == 0) prompt.visibility = View.VISIBLE
            }
        }
        builder.setNegativeButton(getString(R.string.no), null)
        builder.show()
    }

    override fun showFirstrun() {
        create_label.visibility = View.GONE
        PrefsManager.setFirstRunComplete()
    }

    override fun hideFirstrun() {
        create_label.visibility = View.GONE
    }

    override fun showLoading() {
        refresh_layout.isEnabled = true
        refresh_layout.isRefreshing = true
    }

    override fun hideLoading() {
        refresh_layout.isEnabled = false
        refresh_layout.isRefreshing = false
    }

    override fun showBackupCreatedWithDifferentNameMessage(actualBackupName: String) {
        showMessage(this, getString(R.string.backup_created_with_different_name_message, actualBackupName))
    }

    override fun onBackupRenamed(renamedBackup: Backup, position: Int) {
        adapter.update(renamedBackup, position)
    }

    override fun onBackupApplied() {
        showMessage(this, getString(R.string.backup_applied))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            BACKUPS_STORAGE_REQUEST_CODE -> {
                if (validateStoragePermissionOrShowRationale(
                        this,
                        permissions,
                        grantResults
                    )
                ) presenter.getBackups()
            }

            CREATE_STORAGE_REQUEST_CODE -> {
                if (validateStoragePermissionOrShowRationale(
                        this,
                        permissions,
                        grantResults
                    )
                ) attemptCreate()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("InflateParams")
    fun attemptCreate() {
        if (makeFirefoxPackageContext(this) == null) {
            showMessage(
                this,
                getString(R.string.error_shareduserid, BuildConfig.FIREFOX_PACKAGE_NAME)
            )
            return
        }

        if (!checkStoragePermission(this, CREATE_STORAGE_REQUEST_CODE)) return

        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
        val inflater = layoutInflater
        builder.setTitle(getString(R.string.set_backup_name))
        val dialogLayout = inflater.inflate(R.layout.alert_input, null)
        val input = dialogLayout.findViewById<EditText>(R.id.input)
        builder.setView(dialogLayout)
        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
            run {
                if (input.text.isEmpty()) {
                    showMessage(this@MainActivity, getString(R.string.error_input_null))
                    return@setPositiveButton
                }

                presenter.createBackup(input.text.toString())
            }
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.show()
    }

    companion object {
        private val TAG = MainActivity::class.java.canonicalName

        private const val BACKUPS_STORAGE_REQUEST_CODE = 1001
        private const val CREATE_STORAGE_REQUEST_CODE = 1002
    }
}
