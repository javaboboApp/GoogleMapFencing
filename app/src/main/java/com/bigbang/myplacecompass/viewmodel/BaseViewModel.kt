package com.bigbang.myplacecompass.viewmodel

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseViewModel : ViewModel() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()


    fun subscribe(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    fun cleanCompositeDisposable(){
        compositeDisposable.clear()
    }

}