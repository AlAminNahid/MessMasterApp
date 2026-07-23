package com.bilimbistudio.messmaster.managerdashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bilimbistudio.messmaster.R
import com.bilimbistudio.messmaster.managerdashboard.model.expense.CurrentMonthMealExpense
import java.text.NumberFormat
import java.util.Locale

class ExpenseRecordAdapter(
    private val onItemClick: (CurrentMonthMealExpense) -> Unit
) : RecyclerView.Adapter<ExpenseRecordAdapter.ExpenseViewHolder>() {

    private var items: List<CurrentMonthMealExpense> = emptyList()

    fun submitList(newItems: List<CurrentMonthMealExpense>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense_record, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount(): Int = items.size

    class ExpenseViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val txtMemberName: TextView = itemView.findViewById(R.id.txtExpenseMemberName)
        private val txtDate: TextView = itemView.findViewById(R.id.txtExpenseDate)
        private val txtAmountChip: TextView = itemView.findViewById(R.id.txtExpenseAmountChip)
        private val txtAmount: TextView = itemView.findViewById(R.id.txtExpenseAmount)
        private val txtAmountDate: TextView = itemView.findViewById(R.id.txtExpenseAmountDate)
        private val txtDescription: TextView = itemView.findViewById(R.id.txtExpenseDescription)

        fun bind(expense: CurrentMonthMealExpense, onItemClick: (CurrentMonthMealExpense) -> Unit) {
            val formattedAmount = "৳${formatAmount(expense.amount)}"
            txtMemberName.text = expense.member_name
            txtDate.text = expense.date
            txtAmountChip.text = formattedAmount
            txtAmount.text = formattedAmount
            txtAmountDate.text = expense.date
            txtDescription.text = expense.description
            itemView.setOnClickListener { onItemClick(expense) }
        }

        private fun formatAmount(amount: Double): String {
            return NumberFormat.getNumberInstance(Locale.US).apply {
                maximumFractionDigits = 2
                minimumFractionDigits = 0
            }.format(amount)
        }
    }
}
