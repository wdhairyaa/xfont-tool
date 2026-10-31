package com.monotype.android.font.kapp.custom_xfont

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "Font applied via Language & Region settings", Toast.LENGTH_SHORT).show()
        finish()
    }
}
