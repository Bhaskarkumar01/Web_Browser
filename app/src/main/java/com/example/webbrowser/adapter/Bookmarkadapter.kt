package com.example.webbrowser.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.example.webbrowser.R
import com.example.webbrowser.acttivity.MainActivity
import com.example.webbrowser.acttivity.changetab
import com.example.webbrowser.acttivity.checkForInternet
import com.example.webbrowser.databinding.BookmarkViewBinding
import com.example.webbrowser.databinding.LongBookmarkViewBinding
import com.example.webbrowser.fragment.Browsefragment
import com.google.android.material.snackbar.Snackbar


class bookmarkadapter(private val context:Context,private val isActivity:Boolean=false): RecyclerView.Adapter<bookmarkadapter.Myholder>() {

    private val color=context.resources.getIntArray(R.array.myColor)


    class Myholder(binding: BookmarkViewBinding?=null,bindingl:LongBookmarkViewBinding?=null):RecyclerView.ViewHolder((binding?.root ?: bindingl?.root)!!) {
   val image=(binding?.bookmarkicon ?: bindingl?.bookmarkicon)!!
        val name=(binding?.bookmarkname ?: bindingl?.bookmarkname)!!
        val root=(binding?.root ?: bindingl?.root)!!

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Myholder {
        if(isActivity)
            return Myholder(bindingl=LongBookmarkViewBinding.inflate(LayoutInflater.from(context),parent,false))
       return Myholder(binding = BookmarkViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: Myholder, position: Int) {
       try {
           val icon=BitmapFactory.decodeByteArray(MainActivity.bookmarklist[position].image,0,MainActivity.bookmarklist[position].image!!.size)
            holder.image.background=icon.toDrawable(context.resources)
       }catch (e:Exception){
           holder.image.setBackgroundColor(color[(color.indices).random()])
           holder.image.text= MainActivity.bookmarklist[position].name[0].toString()
       }
        holder.name.text= MainActivity.bookmarklist[position].name

        holder.root.setOnClickListener{

            when{
               checkForInternet(context)->
                    {changetab(MainActivity.bookmarklist[position].name,
                        Browsefragment(newurl=MainActivity.bookmarklist[position].url))
                        if(isActivity)(context as Activity).finish()
                    }
                else-> Snackbar.make(holder.root, "Internet not available", 2500).show()
            }
        }

    }

    override fun getItemCount(): Int {
        return MainActivity.bookmarklist.size
    }
}