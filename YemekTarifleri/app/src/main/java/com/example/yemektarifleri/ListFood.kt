package com.example.yemektarifleri

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

class ListFood : Fragment() {
    val foodNamesArray = ArrayList<String>()
    val idArray = ArrayList<Int>()
    private lateinit var customAdapter : CustomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customAdapter = CustomAdapter(foodNamesArray, idArray)
        val recyclerView = view.findViewById<RecyclerView>(R.id.myRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = customAdapter
        sqlDataImport()
    }

    private fun sqlDataImport(){
        try {
            activity?.let {
                val database = it.openOrCreateDatabase("Foods", Context.MODE_PRIVATE, null)
                val cursor = database.rawQuery("SELECT * FROM foods", null)
                val foodNameIndex = cursor.getColumnIndex("foodName");
                val id = cursor.getColumnIndex("id")
                foodNamesArray.clear()
                idArray.clear()

                while (cursor.moveToNext()){
                    foodNamesArray.add(cursor.getString(foodNameIndex))
                    idArray.add(cursor.getInt(id))
                }
                customAdapter.notifyDataSetChanged()
                cursor.close()
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
}