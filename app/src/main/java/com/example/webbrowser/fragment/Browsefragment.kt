package com.example.webbrowser.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ShareCompat
import com.example.webbrowser.acttivity.MainActivity
import com.example.webbrowser.R
import com.example.webbrowser.acttivity.changetab
import com.example.webbrowser.databinding.FragmentBrowsefragmentBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import android.util.Base64
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView


class Browsefragment(private var newurl:String): Fragment() {


   lateinit var binding: FragmentBrowsefragmentBinding
   var webIcon : Bitmap? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_browsefragment, container, false)
        binding= FragmentBrowsefragmentBinding.bind(view)
        registerForContextMenu(binding.webview)

        binding.webview.apply {
            when{
                URLUtil.isValidUrl(newurl)->loadUrl(newurl)
                newurl.contains(".com", ignoreCase = true)->loadUrl(newurl)
                //default search engine
                else->loadUrl("https://www.google.com/search?q=$newurl")
            }
        }


        return view
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()
        val mainref=requireActivity() as MainActivity




        MainActivity.tablist[MainActivity.page.currentItem].name=binding.webview.url.toString()
        MainActivity.tabsbtn.text=MainActivity.tablist.size.toString()


        binding.webview.setDownloadListener { url, _, _,_,_ ->  startActivity(Intent(Intent.ACTION_VIEW).setData(
            Uri.parse(url)))}

        mainref.binding.refressbtn.visibility=View.VISIBLE
        mainref.binding.refressbtn.setOnClickListener {
            binding.webview.reload()
        }

        binding.webview.apply {

            settings.setSupportZoom(true)
            settings.builtInZoomControls=true
            settings.displayZoomControls=false

            settings.javaScriptEnabled=true
            webViewClient =object:WebViewClient(){

                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                    if(MainActivity.isDesctopsite)
                        view?.evaluateJavascript(
                            "document.querySelector('meta[name=\"viewport\"]').setAttribute('content'," +
                                    " 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));",
                            null
                        )
                   // view?.zoomOut()
                }


                override fun doUpdateVisitedHistory(
                    view: WebView?,
                    url: String?,
                    isReload: Boolean
                ) {
                    super.doUpdateVisitedHistory(view, url, isReload)

                    mainref.binding.searchBar.setText(url)
                    MainActivity.tablist[MainActivity.page.currentItem].name=url.toString()


                    view?.zoomOut()
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    mainref.binding.progressbar.progress=0
                    super.onPageStarted(view, url, favicon)
                    mainref.binding.progressbar.visibility=View.VISIBLE

                    //for making clear or total view of youtube like website
                    if(url!!.contains("you", ignoreCase = false))mainref.binding.root.transitionToEnd()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    mainref.binding.progressbar.visibility=View.GONE
                }
            }

            //to run the application in full screen.
            webChromeClient = object:WebChromeClient(){
                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                    //for setting the icon to the search bar
                    super.onReceivedIcon(view, icon)

                   try {
                       mainref.binding.searchIcon.setImageBitmap(icon)

                       webIcon= icon
                       MainActivity.bookmarkIndex=mainref.isBookmarked(view?.url!!)

                       if(MainActivity.bookmarkIndex!=-1){
                           val array=ByteArrayOutputStream()
                           icon!!.compress(Bitmap.CompressFormat.PNG,100,array)
                           MainActivity.bookmarklist[MainActivity.bookmarkIndex].image=array.toByteArray()


                       }
                   }catch(e:Exception){}

                }



                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    super.onShowCustomView(view, callback)
                    binding.webview.visibility=View.GONE
                    binding.costomview.visibility=View.VISIBLE
                    binding.costomview.addView(view)
                    mainref.binding.root.transitionToEnd()

                }

                override fun onHideCustomView() {
                    super.onHideCustomView()
                    binding.webview.visibility=View.VISIBLE
                    binding.costomview.visibility=View.GONE
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    mainref.binding.progressbar.progress=newProgress
                }
            }



            binding.webview.setOnTouchListener{_,motionEvent->
                mainref.binding.root.onTouchEvent(motionEvent)
                return@setOnTouchListener false
            }







        }

        binding.webview.reload()
    }

    override fun onPause() {

        super.onPause()
        (requireActivity() as MainActivity).saveBookmarks()

        binding.webview.apply {
            clearHistory()
            clearMatches()
            clearFormData()
            clearSslPreferences()

            clearCache(true)
            CookieManager.getInstance().removeAllCookies(null)
            WebStorage.getInstance().deleteAllData()

        }
    }


    override fun onCreateContextMenu(
        menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val result=binding.webview.hitTestResult
        when(result.type){
            WebView.HitTestResult.IMAGE_TYPE->{
                 menu.add("view image")
                 menu.add("save image")
                menu.add("share link")
                menu.add("close")
            }

            WebView.HitTestResult.SRC_ANCHOR_TYPE,WebView.HitTestResult.ANCHOR_TYPE->{
                menu.add("open in new tab")
                menu.add("open tab in background")
                menu.add("share link")
                menu.add("close")
            }

            WebView.HitTestResult.EDIT_TEXT_TYPE,WebView.HitTestResult.UNKNOWN_TYPE->{}
            else->{
                menu.add("open in new tab")
                menu.add("open tab in background")
                menu.add("share link")
                menu.add("close")
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val message=Handler().obtainMessage()
        //taking all data or information into the message
        binding.webview.requestFocusNodeHref(message)
        val url=message.data.getString("url")
        //for image
        val imgurl=message.data.getString("src")

        when(item.title){
            "open in new tab"->{
                changetab(url.toString(),Browsefragment(url.toString()))
            }

            "open tab in background"->{
                changetab(url.toString(),Browsefragment(url.toString()),true)
            }


            "view image"->{
                if(imgurl!= null){
                    //base64 for checking it is a image or simple url
                    if(imgurl.contains("base64")){

                        //to change base64 to image
                        val bitessubstring=imgurl.substring(imgurl.indexOf(",")+1)
                        val decodedbyte = Base64.decode(bitessubstring,Base64.DEFAULT)

                        val finalimg=BitmapFactory.decodeByteArray(decodedbyte,0,decodedbyte.size)

                        val imgview= ShapeableImageView(requireContext())
                        imgview.setImageBitmap(finalimg)




                        val imagedialog=MaterialAlertDialogBuilder(requireContext()).setView(imgview).create()
                        imagedialog.show()

                        imgview.layoutParams.width=Resources.getSystem().displayMetrics.widthPixels
                        imgview.layoutParams.height=(Resources.getSystem().displayMetrics.heightPixels * .75).toInt()
                        imgview.requestLayout()


                        imagedialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))



                    }else{
                       changetab(imgurl,Browsefragment(imgurl))
                    }
                }
            }
            "save image"->{
                if(imgurl!= null){
                    //base64 for checking it is a image or simple url
                    if(imgurl.contains("base64")){

                        //to change base64 to image
                        val bitessubstring=imgurl.substring(imgurl.indexOf(",")+1)
                        val decodedbyte = Base64.decode(bitessubstring,Base64.DEFAULT)

                        val finalimg=BitmapFactory.decodeByteArray(decodedbyte,0,decodedbyte.size)
                        MediaStore.Images.Media.insertImage(requireActivity().contentResolver,finalimg,"image",null)


                        Snackbar.make(binding.root,"Image saved",4000).show()
                    }else{
                       startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(imgurl)))
                    }
                }


            }
            "share link"->{
                val tempurl=url ?: imgurl
                if(tempurl != null){
                    //base64 for checking it is a image or simple url
                    if(tempurl.contains("base64")){

                        //to change base64 to image
                        val bitessubstring=tempurl.substring(tempurl.indexOf(",")+1)
                        val decodedbyte = Base64.decode(bitessubstring,Base64.DEFAULT)

                        val finalimg=BitmapFactory.decodeByteArray(decodedbyte,0,decodedbyte.size)
                      val path=  MediaStore.Images.Media.insertImage(requireActivity().contentResolver,finalimg,"image",null)



                        ShareCompat.IntentBuilder(requireContext()).setChooserTitle("Sharing url!").setType("image/*")
                            .setStream(Uri.parse(path))
                            .startChooser()

                    }else{
                        ShareCompat.IntentBuilder(requireContext()).setChooserTitle("Sharing url!").setType("text/plain")
                            .setText(tempurl)
                            .startChooser()
                    }
                }

                else{
                    Snackbar.make(binding.root,"not a valid link",4000).show()
                }
            }
            "close"->{}


        }


        return super.onContextItemSelected(item)
    }


}