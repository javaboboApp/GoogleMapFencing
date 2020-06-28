package com.bigbang.myplacecompass.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import com.bigbang.myplacecompass.BuildConfig
import com.bigbang.myplacecompass.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_maps.*

object SnackBarHelper {
    fun makeSnackBar(context: Context,view: View, resId: Int, duration: Int) : Snackbar {
     return   Snackbar.make(
            view,
            resId,
            duration
        )
    }
}