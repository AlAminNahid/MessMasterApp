package com.example.messmaster.commondashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messmaster.R
import com.example.messmaster.commondashboard.model.MessItems

class Messes(
    private val messes: MutableList<MessItems>,
    private val onJoinClick: (MessItems) -> Unit
) : RecyclerView.Adapter<Messes.MessViewHolder>() {

    class MessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val messNameText: TextView = itemView.findViewById(R.id.messNameText)
        val messAddressText: TextView = itemView.findViewById(R.id.messAddressText)
        val joinMessButton: Button = itemView.findViewById(R.id.joinMessButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_available_mess, parent, false)

        return MessViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessViewHolder, position: Int) {
        val mess = messes[position]

        holder.messNameText.text = mess.name
        holder.messAddressText.text = mess.address

        holder.joinMessButton.setOnClickListener {
            onJoinClick(mess)
        }
    }

    override fun getItemCount(): Int = messes.size

    fun updateList(newList: List<MessItems>) {
        messes.clear()
        messes.addAll(newList)
        notifyDataSetChanged()
    }
}