package com.xtooltech.baic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {

   lateinit var  ecuState:EcuState
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ecuState=findViewById(R.id.ecustate)
    }

    fun click_move(view: View) {
        ecustate.dragMove()
    }
    fun click_zout(view: View) {
        ecuState.zoomOut(0.2F)
    }
    fun click_zin(view: View) {
        ecuState.zoomIn(0.2F)
    }
}