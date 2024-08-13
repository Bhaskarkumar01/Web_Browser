package com.example.webbrowser.acttivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.webbrowser.R
import com.example.webbrowser.adapter.bookmarkadapter
import com.example.webbrowser.databinding.ActivityBookmarkBinding

class BookmarkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding=ActivityBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rcbookmark.layoutManager=LinearLayoutManager(this)
        binding.rcbookmark.setItemViewCacheSize(5)
        binding.rcbookmark.hasFixedSize()
        binding.rcbookmark.adapter=bookmarkadapter(this,true)

    }
}