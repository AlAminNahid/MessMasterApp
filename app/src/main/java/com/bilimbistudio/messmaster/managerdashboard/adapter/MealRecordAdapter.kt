package com.bilimbistudio.messmaster.managerdashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bilimbistudio.messmaster.R
import com.bilimbistudio.messmaster.managerdashboard.model.meal.CurrentMonthMeal

class MealRecordAdapter(
    private val onItemClick: (CurrentMonthMeal) -> Unit
) : RecyclerView.Adapter<MealRecordAdapter.MealViewHolder>() {

    private var items: List<CurrentMonthMeal> = emptyList()

    fun submitList(newItems: List<CurrentMonthMeal>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_record, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount(): Int = items.size

    class MealViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val txtMemberName: TextView = itemView.findViewById(R.id.txtMealMemberName)
        private val txtDate: TextView = itemView.findViewById(R.id.txtMealDate)
        private val txtTypeChip: TextView = itemView.findViewById(R.id.txtMealTypeChip)
        private val txtCount: TextView = itemView.findViewById(R.id.txtMealCount)
        private val txtType: TextView = itemView.findViewById(R.id.txtMealType)

        fun bind(meal: CurrentMonthMeal, onItemClick: (CurrentMonthMeal) -> Unit) {
            txtMemberName.text = meal.member_name
            txtDate.text = meal.date
            txtTypeChip.text = meal.meal_type
            txtCount.text = meal.meal_count.toString()
            txtType.text = meal.meal_type
            itemView.setOnClickListener { onItemClick(meal) }
        }
    }
}
