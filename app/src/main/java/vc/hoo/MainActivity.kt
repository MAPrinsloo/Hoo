package vc.hoo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import vc.hoo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var MainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainBinding = ActivityMainBinding.inflate(layoutInflater)
        val MainView = MainBinding.root
        setContentView(MainView)
    }
}