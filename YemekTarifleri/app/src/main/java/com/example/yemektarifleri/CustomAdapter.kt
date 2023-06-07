package com.example.yemektarifleri

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(var foodNameArray: ArrayList<String>, var idArray: ArrayList<Int>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    class ViewHolder (v : View) : RecyclerView.ViewHolder(v) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerow, parent, false)
        return ViewHolder(itView)
    }

    override fun getItemCount(): Int {
        return foodNameArray.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.textRow).text = foodNameArray[position]
        holder.itemView.setOnClickListener {
            val action = ListFoodDirections.actionListFoodToAddFood("main", position + 1)
            Navigation.findNavController(it).navigate(action)
        }
    }

}