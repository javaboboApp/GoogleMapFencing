package com.bigbang.myplacecompass.util

import android.content.Context
import android.widget.Toast
import java.time.Duration

object ToastHelpers {
    fun showToast(context: Context, message: String, duration: Int) {
        Toast.makeText(context, message, duration).show()
    }
}