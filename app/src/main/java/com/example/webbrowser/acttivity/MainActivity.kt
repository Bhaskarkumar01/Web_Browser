package com.example.webbrowser.acttivity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import android.view.Gravity
import android.view.WindowManager
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.webbrowser.R
import com.example.webbrowser.acttivity.MainActivity.Companion.page
import com.example.webbrowser.acttivity.MainActivity.Companion.tabsbtn
import com.example.webbrowser.adapter.tabadapter
import com.example.webbrowser.databinding.ActivityMainBinding
import com.example.webbrowser.databinding.BookmarkDailogBinding
import com.example.webbrowser.databinding.FeaturesBinding
import com.example.webbrowser.databinding.TabviewBinding
import com.example.webbrowser.fragment.Browsefragment
import com.example.webbrowser.fragment.HomeFragment
import com.example.webbrowser.model.bookmark
import com.example.webbrowser.model.tab
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MainActivity : AppCompatActivity() {
     lateinit var binding:ActivityMainBinding
     private var printjob:PrintJob?=null
    companion object{

        var tablist:ArrayList<tab> =ArrayList()
        private var fullscreen:Boolean=true
        var isDesctopsite:Boolean=false

        var bookmarklist: ArrayList<bookmark> =ArrayList()

        var bookmarkIndex:Int = -1

        lateinit var page:ViewPager2

        lateinit var tabsbtn:MaterialTextView

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.attributes.layoutInDisplayCutoutMode= WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES


        //binding -to use safely access view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAllbookmark()

        tablist.add(tab("home",HomeFragment()))

        binding.page.adapter=TabsAdapter(supportFragmentManager,lifecycle)
        binding.page.isUserInputEnabled=false

       page=binding.page

        tabsbtn=binding.tabsbtn

        initializeview()
        changedfullscreen(enable = true)
    }

    override fun onBackPressed() {
var frag: Browsefragment?=null
try{
    frag= tablist[binding.page.currentItem].fragment as Browsefragment
}catch (e:Exception){}
        when{
            frag?.binding?.webview?.canGoBack()==true->frag.binding.webview.goBack()
            binding.page.currentItem!=0 ->{
                tablist.removeAt(binding.page.currentItem)
                binding.page.adapter?.notifyDataSetChanged()
                binding.page.currentItem= tablist.size-1


            }
            else-> super.onBackPressed()
        }
    }



        private inner class TabsAdapter(fa: FragmentManager,lc:Lifecycle) : FragmentStateAdapter(fa,lc) {
            override fun getItemCount(): Int = tablist.size

            override fun createFragment(position: Int): Fragment = tablist[position].fragment
        }





    private fun initializeview(){


        binding.tabsbtn.setOnClickListener{
            val viewtab=layoutInflater.inflate(R.layout.tabview,binding.root,false)
            val tabbinding=TabviewBinding.bind(viewtab)

            val dialogtabs =MaterialAlertDialogBuilder(this,R.style.roundCornerdialog).setView(viewtab)
                .setTitle("select Tabs")
                .setPositiveButton("home"){self,_ ->
                    changetab("home",HomeFragment())
                    self.dismiss()
                }
                .setNeutralButton("google"){self,_ ->
                    changetab("google",Browsefragment(newurl = "www.google.com"))
                    self.dismiss()
                }
                .create()



            tabbinding.tabRv.setHasFixedSize(true)
            tabbinding.tabRv.layoutManager=LinearLayoutManager(this)
            tabbinding.tabRv.adapter=tabadapter(this,dialogtabs)

            dialogtabs.show()


            val pbtn =dialogtabs.getButton(AlertDialog.BUTTON_POSITIVE)
            val nbtn=dialogtabs.getButton(AlertDialog.BUTTON_NEUTRAL)

            pbtn.isAllCaps=false
            nbtn.isAllCaps=false

            pbtn.setTextColor(Color.BLACK)
            nbtn.setTextColor(Color.BLACK)

            pbtn.setCompoundDrawablesRelativeWithIntrinsicBounds(ResourcesCompat
                .getDrawable(resources,R.drawable.ichome_24,theme),
                null,null,null)

            nbtn.setCompoundDrawablesRelativeWithIntrinsicBounds(ResourcesCompat
                .getDrawable(resources,R.drawable.add_24,theme),
                null,null,null)




        }


        binding.setbtn.setOnClickListener{

            var frag: Browsefragment?=null
            try{
                frag= tablist[binding.page.currentItem].fragment as Browsefragment
            }catch (e:Exception){}
            val view=layoutInflater.inflate(R.layout.features,binding.root,false)
            val dailogbinding=FeaturesBinding.bind(view)

            val dialog =MaterialAlertDialogBuilder(this).setView(view).create()

            dialog.window?.apply {
               attributes.gravity=Gravity.BOTTOM
                attributes.y=50
                setBackgroundDrawable(ColorDrawable(0xFFFFFFFF.toInt()))
            }
            dialog.show()
            if(fullscreen){
                dailogbinding.fullScn.setIconTintResource(R.color.cool_blue)
                dailogbinding.fullScn.setTextColor(ContextCompat.getColor(this, R.color.cool_blue))
            }
           frag?.let {
               bookmarkIndex=isBookmarked(it.binding.webview.url!!)
               if(bookmarkIndex!=-1){
                   dailogbinding.bookmarkbtn.setIconTintResource(R.color.cool_blue)
                   dailogbinding.bookmarkbtn.setTextColor(ContextCompat.getColor(this, R.color.cool_blue))
               }
           }
            if(isDesctopsite){
                dailogbinding.desktopbtn.setIconTintResource(R.color.cool_blue)
                dailogbinding.desktopbtn.setTextColor(ContextCompat.getColor(this,
                    R.color.cool_blue
                ))
            }



            dailogbinding.backbtn.setOnClickListener{
                onBackPressed()
            }

            dailogbinding.forwardbtn.setOnClickListener{
              frag?.apply {


                  if(binding.webview.canGoForward()){
                      binding.webview.goForward()
                  }

              }
            }

            dailogbinding.savebtn.setOnClickListener {
                dialog.dismiss()
                 if(frag!=null)
                     saveAsPdf(web=frag.binding.webview)
                else
                    Snackbar.make(binding.root,"open a webpage",3000).show()

            }

            dailogbinding.fullScn.setOnClickListener {
               fullscreen = if(fullscreen){
                    changedfullscreen(false)
                    dailogbinding.fullScn.setIconTintResource(R.color.black)
                    dailogbinding.fullScn.setTextColor(ContextCompat.getColor(this, R.color.black))
                   false
                }else{
                    changedfullscreen(true)
                    dailogbinding.fullScn.setIconTintResource(R.color.cool_blue)
                    dailogbinding.fullScn.setTextColor(ContextCompat.getColor(this,
                        R.color.cool_blue
                    ))
                   true
                }
            }
            dailogbinding.desktopbtn.setOnClickListener {
                frag?.binding?.webview?.apply {
                    isDesctopsite = if(isDesctopsite){
                        settings.userAgentString=null
                        dailogbinding.desktopbtn.setIconTintResource(R.color.black)
                        dailogbinding.desktopbtn.setTextColor(ContextCompat.getColor(this@MainActivity,
                            R.color.black
                        ))
                        false
                    }else{
                        settings.userAgentString="Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:129.0) Gecko/20100101 Firefox/129.0"
                        settings.useWideViewPort=true
                        evaluateJavascript(
                            "document.querySelector('meta[name=\"viewport\"]').setAttribute('content'," +
                                    " 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));",
                            null
                        )
                        dailogbinding.desktopbtn.setIconTintResource(R.color.cool_blue)
                        dailogbinding.desktopbtn.setTextColor(ContextCompat.getColor(this@MainActivity,
                            R.color.cool_blue
                        ))
                        true
                    }
                    reload()
                    dialog.dismiss()
                }

            }

            dailogbinding.bookmarkbtn.setOnClickListener {
               frag?.let {

                   if(bookmarkIndex == -1){
                       val viewbookmark=layoutInflater.inflate(R.layout.bookmark_dailog,binding.root,false)
                       val bookmarkdailogbinding=BookmarkDailogBinding.bind(viewbookmark)

                       val dialogbookmark =MaterialAlertDialogBuilder(this)
                           .setTitle("Add Bookmark")
                           .setMessage("url:${it.binding.webview.url}")
                           .setPositiveButton("Add"){self,_->
                              try {
                                  val array= ByteArrayOutputStream()
                                  it.webIcon?.compress(Bitmap.CompressFormat.PNG,100,array)

                                  bookmarklist.add(bookmark(name=bookmarkdailogbinding.bookmarkTitle.text.toString(),
                                      url = it.binding.webview.url!!,array.toByteArray()))
                              }catch (e:Exception){
                                  bookmarklist.add(bookmark(name=bookmarkdailogbinding.bookmarkTitle.text.toString(),
                                      url = it.binding.webview.url!!))
                              }
                               self.dismiss()}
                           .setNegativeButton("Cancel"){self,_-> self.dismiss()}
                           .setView(viewbookmark).create()

                       dialogbookmark.show()
                       bookmarkdailogbinding.bookmarkTitle.setText(it.binding.webview.title)

                   }else{
                       val dialogbookmark =MaterialAlertDialogBuilder(this)
                           .setTitle("Remove Bookmark")
                           .setMessage("url:${it.binding.webview.url}")
                           .setPositiveButton("Remove"){self,_->
                               bookmarklist.removeAt(bookmarkIndex)
                               self.dismiss()}
                           .setNegativeButton("Cancel"){self,_-> self.dismiss()}
                           .create()

                       dialogbookmark.show()

                   }
               }

                dialog.dismiss()


            }


        }
    }

    override fun onResume() {
        super.onResume()
        printjob?.let {
            when{
                it.isCompleted->Snackbar.make(binding.root,"pdf is saved->${it.info.label}",3000).show()
                it.isFailed->Snackbar.make(binding.root,"pdf is not saved->${it.info.label}",3000).show()
            }
        }
    }
    private fun saveAsPdf(web: WebView){


        val printManager=getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName="${URL(web.url).host}_${SimpleDateFormat("HH:mm d_MM_yy", Locale.ENGLISH).format(Calendar.getInstance().time)}"
        val printAdapter=web.createPrintDocumentAdapter(jobName)
        val printAttributes=PrintAttributes.Builder()


        printjob=printManager.print(jobName,printAdapter,printAttributes.build())
    }


    private fun changedfullscreen(enable: Boolean){
        if(enable){
            WindowCompat.setDecorFitsSystemWindows(window,false)
            WindowInsetsControllerCompat(window,binding.root).let{controller->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior=WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            }

        }else{
            WindowCompat.setDecorFitsSystemWindows(window,true)
            WindowInsetsControllerCompat(window,binding.root).show(WindowInsetsCompat.Type.systemBars())

        }
    }

    fun isBookmarked(url:String):Int{
          bookmarklist.forEachIndexed{
              index,bookmark->
              if (bookmark.url==url) return index
          }
        return -1
    }

    fun saveBookmarks(){
        //for storing bookmark data using shared preference
        val editor=getSharedPreferences("BOOKMARK", MODE_PRIVATE).edit()
        val data=GsonBuilder().create().toJson(bookmarklist)

        editor.putString("bookmarklist",data)
        editor.apply()
    }


    fun getAllbookmark(){
        //for getting all bookmark data using shared preference from storage
     bookmarklist= ArrayList()
        val editor=getSharedPreferences("BOOKMARK", MODE_PRIVATE)

        val data=editor.getString("bookmarklist",null)
        if(data!=null){
            val list:ArrayList<bookmark> = GsonBuilder().create().fromJson(data,object:TypeToken<ArrayList<bookmark>>(){}.type)
            bookmarklist.addAll(list)
        }


    }


    }
@SuppressLint("NotifyDataSetChanged")
fun changetab(url:String, fragment:Fragment,isbackground: Boolean =false){
    MainActivity.tablist.add(tab(name=url,fragment=fragment))
   page.adapter?.notifyDataSetChanged()

    tabsbtn.text=MainActivity.tablist.size.toString()
    if(!isbackground)
        page.currentItem= MainActivity.tablist.size-1

}

@Suppress("DEPRECATION")
fun checkForInternet(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    } else {
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        networkInfo.isConnected
    }
}