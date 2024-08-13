package com.example.webbrowser.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.example.webbrowser.R
import com.example.webbrowser.acttivity.MainActivity
import com.example.webbrowser.acttivity.changetab
import com.example.webbrowser.acttivity.checkForInternet
import com.example.webbrowser.databinding.BookmarkViewBinding
import com.example.webbrowser.databinding.LongBookmarkViewBinding
import com.example.webbrowser.databinding.TabBinding
import com.example.webbrowser.databinding.TabviewBinding
import com.example.webbrowser.fragment.Browsefragment
import com.google.android.material.snackbar.Snackbar


class tabadapter(private val context:Context, private val dialog:AlertDialog): RecyclerView.Adapter<tabadapter.Myholder>() {




    class Myholder(binding: TabBinding):RecyclerView.ViewHolder(binding.root) {
   val cancel=binding.cancelbtn
        val name=binding.tabname
        val root=binding.root

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Myholder {
       return Myholder(TabBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override  fun onBindViewHolder(holder: Myholder, position: Int) {
        holder.name.text=MainActivity.tablist[position].name
        holder.root.setOnClickListener {
            MainActivity.page.currentItem=position
            dialog.dismiss()
        }
        holder.cancel.setOnClickListener {
            if(MainActivity.tablist.size==1||position==MainActivity.page.currentItem)
                Snackbar.make(MainActivity.page,"can't remove this page",4000).show()
            else {
                MainActivity.tablist.removeAt(position)
                notifyDataSetChanged()
                MainActivity.page.adapter?.notifyItemRemoved(position)
            }

        }
    }

    override fun getItemCount(): Int {
        return MainActivity.tablist.size
    }
}