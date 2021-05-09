package com.example.wheremybuzz.utils

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

object RXDisposableManager {
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    fun add(disposable: Disposable?){
        disposable?.let{
            getCompositeDisposable().add(disposable)
        }
    }

    fun dispose(){
        getCompositeDisposable().dispose()
    }

    private fun getCompositeDisposable(): CompositeDisposable{
        if (compositeDisposable.isDisposed){
            compositeDisposable = CompositeDisposable()
        }
        return compositeDisposable
    }
}