package com.example.webbrowser.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.webbrowser.acttivity.MainActivity
import com.example.webbrowser.R
import com.example.webbrowser.acttivity.BookmarkActivity
import com.example.webbrowser.acttivity.changetab
import com.example.webbrowser.acttivity.checkForInternet
import com.example.webbrowser.model.bookmark
import com.example.webbrowser.adapter.bookmarkadapter
import com.example.webbrowser.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar


class HomeFragment : Fragment() {


    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)
        return view
    }


    override fun onResume() {

        super.onResume()


        val mainref = requireActivity() as MainActivity

        MainActivity.tablist[MainActivity.page.currentItem].name="Home"
        MainActivity.tabsbtn.text=MainActivity.tablist.size.toString()
         binding.searchView.setQuery("",false)
        mainref.binding.searchBar.setText("")
        mainref.binding.searchIcon.setImageResource(R.drawable.baseline_search_24)


        mainref.binding.refressbtn.visibility=View.GONE


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(result: String?): Boolean {
                if (checkForInternet(requireContext())) {
                   changetab(result!!, Browsefragment(result))
                } else {
                    Snackbar.make(binding.root, "Internet not available", 2500).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false


        })
        mainref.binding.bottom.setOnClickListener {
            if (checkForInternet(requireContext())) {
                changetab(
                    mainref.binding.searchBar.text.toString(),
                    Browsefragment(mainref.binding.searchBar.text.toString())
                )
            } else {
                Snackbar.make(binding.root, "Internet not available", 2500).show()
            }


        }






        binding.recycleview.setHasFixedSize(true)
        binding.recycleview.setItemViewCacheSize(5)
        binding.recycleview.layoutManager=GridLayoutManager(requireContext(),5)
        binding.recycleview.adapter= bookmarkadapter(requireContext())

        if(MainActivity.bookmarklist.size<1)
            binding.viewall.visibility=View.GONE
        binding.viewall.setOnClickListener {
            startActivity(Intent(requireContext(),BookmarkActivity::class.java))
        }


    }
}