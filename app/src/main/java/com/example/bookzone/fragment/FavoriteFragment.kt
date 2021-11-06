package com.example.bookzone.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.bookzone.R
import com.example.bookzone.adapter.FavoriteRecyclerAdapter
import com.example.bookzone.database.BookDatabase
import com.example.bookzone.database.BookEntity
import com.example.bookzone.model.Book


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FavoriteFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null


    lateinit var recyclerFavorite: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: FavoriteRecyclerAdapter
    lateinit var favoriteProgressLayout: RelativeLayout
    lateinit var favoriteProgressBar: ProgressBar

    var dbBookList = listOf<BookEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_favorite, container, false)


        recyclerFavorite = view.findViewById(R.id.recyclerFavorite)
        favoriteProgressLayout =  view.findViewById(R.id.favoriteProgressLayout)
        favoriteProgressBar = view.findViewById(R.id.favoriteProgressBar)

        favoriteProgressLayout.visibility = View.VISIBLE

        layoutManager = GridLayoutManager(activity as Context,2)

        dbBookList = RetrieveFavorites(activity as Context).execute().get()


        if(activity != null){

            favoriteProgressLayout.visibility = View.GONE
            recyclerAdapter = FavoriteRecyclerAdapter(activity as Context , dbBookList)
            recyclerFavorite.adapter = recyclerAdapter
            recyclerFavorite.layoutManager = layoutManager
        }else {
            Toast.makeText(activity as Context, "Some error occurred", Toast.LENGTH_SHORT).show()
        }
        return view
    }

    class RetrieveFavorites(val context:Context): AsyncTask<Void,Void,List<BookEntity>>(){

        override fun doInBackground(vararg params: Void?): List<BookEntity> {

            val db = Room.databaseBuilder(context,BookDatabase::class.java,"book-db").build()
            return  db.bookDao().getAllBooks()
        }
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            FavoriteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

