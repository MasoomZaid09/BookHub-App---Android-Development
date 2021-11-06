package com.example.bookzone.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookzone.R
import com.example.bookzone.database.BookDatabase
import com.example.bookzone.database.BookEntity
import com.example.bookzone.util.ConnectionManager
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class DescriptionActivity : AppCompatActivity() {

    lateinit var DescToolbar:Toolbar
    lateinit var btnAddFavorite:Button
    lateinit var imgDescBookImage:ImageView
    lateinit var txtDescBookName:TextView
    lateinit var txtDescBookAuthor:TextView
    lateinit var txtDescBookPrice:TextView
    lateinit var txtDescBookRating:TextView
    lateinit var txtDescBookContent:TextView
    lateinit var DescProgressLayout:RelativeLayout
    lateinit var DescProgressBar:ProgressBar



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        DescToolbar=findViewById(R.id.DescToolbar)
        btnAddFavorite=findViewById(R.id.btnAddFavorite)
        imgDescBookImage = findViewById(R.id.imgDescBookImage)
        txtDescBookName = findViewById(R.id.txtDescBookName)
        txtDescBookAuthor = findViewById(R.id.txtDescBookAuthor)
        txtDescBookPrice = findViewById(R.id.txtDescBookPrice)
        txtDescBookRating = findViewById(R.id.txtDescBookRating)
        txtDescBookContent = findViewById(R.id.txtDescBookContent)
        DescProgressLayout = findViewById(R.id.DescProgressLayout)
        DescProgressBar = findViewById(R.id.DescProgressBar)

        DescProgressBar.visibility = View.VISIBLE
        DescProgressLayout.visibility = View.VISIBLE

        setSupportActionBar(DescToolbar)
        supportActionBar?.title = "Book details"

        var bookId:String? = "100"



        if (intent != null){

            bookId = intent.getStringExtra("book_id")
        }else{
            finish()
            Toast.makeText(this,"Something went Wrong",Toast.LENGTH_LONG).show()
        }

        if (bookId == "100"){
            finish()
            Toast.makeText(this,"Something went Wrong",Toast.LENGTH_LONG).show()
        }



        val queue = Volley.newRequestQueue(this)
        val url ="http://13.235.250.119/v1/book/get_book/"

//        use json for jsonRequest parameter
        val jsonParams = JSONObject()
        jsonParams.put("book_id",bookId)


//       Create Request for Post Request
        if (ConnectionManager().checkConnectivity(this)) {
            val jsonObjectRequest =
                @SuppressLint("SetTextI18n")
                object : JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {
                    try {
                        val success = it.getBoolean("success")
                        if (success) {
                            val bookJsonObject = it.getJSONObject("book_data")
                            DescProgressLayout.visibility = View.GONE

                            Picasso.get().load(bookJsonObject.getString("image"))
                                .error(R.drawable.default_book_cover).into(imgDescBookImage)
                            txtDescBookName.text = bookJsonObject.getString("name")
                            txtDescBookAuthor.text = bookJsonObject.getString("author")
                            txtDescBookPrice.text = bookJsonObject.getString("price")
                            txtDescBookRating.text = bookJsonObject.getString("rating")
                            txtDescBookContent.text = bookJsonObject.getString("description")

                            val bookImageUrl = bookJsonObject.getString("image")

                            val bookEntity = BookEntity(
                                bookId?.toInt() as Int,
                                txtDescBookName.text.toString(),
                                txtDescBookAuthor.text.toString(),
                                txtDescBookRating.text.toString(),
                                txtDescBookPrice.text.toString(),
                                txtDescBookContent.text.toString(),
                                bookImageUrl
                            )

                            val checkFav = DBAsyncTask(applicationContext,bookEntity,1).execute()
                            val isFav = checkFav.get()

//                          Add text and color on updation
                            if (isFav){
                                btnAddFavorite.text = "Remove From Favorite"
                                val favColor = ContextCompat.getColor(applicationContext,R.color.favorite_button_color)
                                btnAddFavorite.setBackgroundColor(favColor)
                            }
                            else{
                                btnAddFavorite.text = "Add To Favorite"
                                val noFavColor = ContextCompat.getColor(applicationContext,R.color.orange)
                                btnAddFavorite.setBackgroundColor(noFavColor)
                            }


                            btnAddFavorite.setOnClickListener {
                                if (!DBAsyncTask(applicationContext,bookEntity,1).execute().get()){

                                    val async = DBAsyncTask(applicationContext,bookEntity,2).execute()
                                    val result =  async.get()

                                    if (result){
                                        Toast.makeText(this,"Added to Favorites",Toast.LENGTH_SHORT).show()
                                        btnAddFavorite.text = "Remove From Favorite"
                                        val favColor = ContextCompat.getColor(applicationContext,R.color.favorite_button_color)
                                        btnAddFavorite.setBackgroundColor(favColor)
                                    }else{
                                        Toast.makeText(this,"Error",Toast.LENGTH_LONG).show()
                                    }
                                }else{
                                    val async = DBAsyncTask(applicationContext,bookEntity,3).execute()
                                    val result =  async.get()

                                    if (result){
                                        Toast.makeText(this,"Removed From Favorites",Toast.LENGTH_SHORT).show()
                                        btnAddFavorite.text = "Added To Favorite"
                                        val noFavColor = ContextCompat.getColor(applicationContext,R.color.orange)
                                        btnAddFavorite.setBackgroundColor(noFavColor)
                                    }else{
                                        Toast.makeText(this,"Error",Toast.LENGTH_LONG).show()
                                    }

                                }

                            }


                        } else {
                            Toast.makeText(this, "Not get Value", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Exception Found", Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener {
                        Toast.makeText(this, "Error is founded", Toast.LENGTH_LONG).show()
                }) {

                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "49be6ddc25851c"
                        return headers
                    }

                }
            queue.add(jsonObjectRequest)
        }else{
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Error")
            dialog.setMessage("Check your internet connection")
            dialog.setPositiveButton("Open Settings"){text,listener ->
                val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingIntent)
                finish()
            }
            dialog.setNegativeButton("Exit"){text,listener ->
                ActivityCompat.finishAffinity(this)
            }
            dialog.create()
            dialog.show()
        }

    }

}

class DBAsyncTask(val context: Context,val bookEntity: BookEntity,val mode:Int) :
    AsyncTask<Void, Void, Boolean>(){
        val  db = Room.databaseBuilder(
            context,
            BookDatabase::class.java,
            "book-db"
        ).build()


    override fun doInBackground(vararg params: Void?): Boolean {
        when(mode){
            1 -> {
                val book:BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                db.close()
                return book != null
            }

            2 -> {
                db.bookDao().insertBook(bookEntity)
                db.close()
                return true
            }

            3 -> {
                db.bookDao().deleteBook(bookEntity)
                db.close()
                return true
            }
        }
        return false
    }


}