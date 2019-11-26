/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.presentation.mvp

import org.mozilla.fpm.data.BackupsRepository

class MainPresenter : MainContract.Presenter {

    private var view: MainContract.View? = null

    private val backupsRepository: BackupsRepository = BackupsRepository()

    override fun getBackups() {
        view?.onBackupsLoaded(backupsRepository.getSampleData())
    }

    override fun importBackup() {
        // todo
    }

    override fun createBackup() {
        // todo
    }

    override fun attachView(view: MainContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }
}
