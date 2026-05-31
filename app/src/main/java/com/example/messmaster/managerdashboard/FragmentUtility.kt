package com.example.messmaster.managerdashboard

import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.messmaster.R
import com.example.messmaster.managerdashboard.model.CurrentMessResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityBillsResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityEntry
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityEntriesResponse
import com.example.messmaster.managerdashboard.model.InsertUtilityCostRequest
import com.example.messmaster.managerdashboard.model.InsertUtilityCostResponse
import com.example.messmaster.managerdashboard.model.MessStatisticsResponse
import com.example.messmaster.model.ErrorResponse
import com.example.messmaster.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FragmentUtility : Fragment() {

    private var messID: Int = 0
    private var totalMembers: Int = 0

    private lateinit var inputElectricityAmount: EditText
    private lateinit var inputInternetAmount: EditText
    private lateinit var inputGasAmount: EditText
    private lateinit var inputMaidAmount: EditText
    private lateinit var inputUtilityMonth: EditText
    private lateinit var btnAddUtility: Button
    private lateinit var btnEditCurrentUtility: Button
    private lateinit var txtTotalUtilityCost: TextView
    private lateinit var txtUtilityPerPerson: TextView
    private lateinit var txtElectricityBill: TextView
    private lateinit var txtInternetBill: TextView
    private lateinit var txtGasBill: TextView
    private lateinit var txtMaidBill: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_manager_utility, container, false)

        inputElectricityAmount = view.findViewById(R.id.inputElectricityAmount)
        inputInternetAmount = view.findViewById(R.id.inputInternetAmount)
        inputGasAmount = view.findViewById(R.id.inputGasAmount)
        inputMaidAmount = view.findViewById(R.id.inputMaidAmount)
        inputUtilityMonth = view.findViewById(R.id.inputUtilityMonth)
        btnAddUtility = view.findViewById(R.id.btnAddUtility)
        btnEditCurrentUtility = view.findViewById(R.id.btnEditCurrentUtility)
        txtTotalUtilityCost = view.findViewById(R.id.txtTotalUtilityCost)
        txtUtilityPerPerson = view.findViewById(R.id.txtUtilityPerPerson)
        txtElectricityBill = view.findViewById(R.id.txtElectricityBill)
        txtInternetBill = view.findViewById(R.id.txtInternetBill)
        txtGasBill = view.findViewById(R.id.txtGasBill)
        txtMaidBill = view.findViewById(R.id.txtMaidBill)

        inputUtilityMonth.setText(currentMonthName())
        inputUtilityMonth.setOnClickListener { showMonthPicker() }
        btnAddUtility.setOnClickListener { submitUtilityEntry() }
        btnEditCurrentUtility.setOnClickListener { loadCurrentMonthUtilityEntryForEdit() }

        loadCurrentMess()
        loadMessStatistics()

        return view
    }

    private fun loadCurrentMess() {
        RetrofitClient.managerService.getCurrentMess()
            .enqueue(object : Callback<CurrentMessResponse> {
                override fun onResponse(
                    call: Call<CurrentMessResponse>,
                    response: Response<CurrentMessResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        messID = response.body()!!.messInfo.mess_id
                        loadCurrentMonthUtilityBills()
                    } else {
                        showError(response)
                    }
                }

                override fun onFailure(call: Call<CurrentMessResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun loadMessStatistics() {
        RetrofitClient.managerService.getMessStatistics()
            .enqueue(object : Callback<MessStatisticsResponse> {
                override fun onResponse(
                    call: Call<MessStatisticsResponse>,
                    response: Response<MessStatisticsResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        totalMembers = response.body()!!.totalMembers
                        updatePerPersonFromTotal()
                    } else {
                        showError(response)
                    }
                }

                override fun onFailure(call: Call<MessStatisticsResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun loadCurrentMonthUtilityBills() {
        if (messID == 0) return

        RetrofitClient.managerService.getCurrentMonthUtilityBills(messID)
            .enqueue(object : Callback<CurrentMonthUtilityBillsResponse> {
                override fun onResponse(
                    call: Call<CurrentMonthUtilityBillsResponse>,
                    response: Response<CurrentMonthUtilityBillsResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        val bills = response.body()!!
                        txtElectricityBill.text = "৳${formatAmount(bills.electricity)}"
                        txtInternetBill.text = "৳${formatAmount(bills.internet)}"
                        txtGasBill.text = "৳${formatAmount(bills.gas)}"
                        txtMaidBill.text = "৳${formatAmount(bills.maid)}"
                        txtTotalUtilityCost.text = "৳${formatAmount(bills.totalUtilityBill)}"
                        updatePerPerson(bills.totalUtilityBill)
                    } else {
                        showError(response)
                    }
                }

                override fun onFailure(call: Call<CurrentMonthUtilityBillsResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun submitUtilityEntry() {
        val electricity = readAmount(inputElectricityAmount)
        val internet = readAmount(inputInternetAmount)
        val gas = readAmount(inputGasAmount)
        val maid = readAmount(inputMaidAmount)

        when {
            messID == 0 -> {
                Toast.makeText(requireContext(), "Mess info is still loading.", Toast.LENGTH_SHORT).show()
                return
            }
            electricity == null || internet == null || gas == null || maid == null -> {
                Toast.makeText(requireContext(), "Enter valid utility amounts.", Toast.LENGTH_SHORT).show()
                return
            }
            electricity + internet + gas + maid <= 0.0 -> {
                inputElectricityAmount.error = "Enter at least one amount"
                return
            }
        }

        btnAddUtility.isEnabled = false
        val request = InsertUtilityCostRequest(
            mess_id = messID,
            electricity = electricity,
            internet = internet,
            gas = gas,
            maid = maid
        )

        RetrofitClient.managerService.insertUtilityCost(request)
            .enqueue(object : Callback<InsertUtilityCostResponse> {
                override fun onResponse(
                    call: Call<InsertUtilityCostResponse>,
                    response: Response<InsertUtilityCostResponse>
                ) {
                    if (!isAdded) return

                    btnAddUtility.isEnabled = true
                    if (response.isSuccessful && response.body() != null) {
                        inputElectricityAmount.text?.clear()
                        inputInternetAmount.text?.clear()
                        inputGasAmount.text?.clear()
                        inputMaidAmount.text?.clear()
                        Toast.makeText(
                            requireContext(),
                            "Utility bills saved for ${inputUtilityMonth.text}.",
                            Toast.LENGTH_LONG
                        ).show()
                        loadCurrentMonthUtilityBills()
                    } else {
                        showError(response)
                    }
                }

                override fun onFailure(call: Call<InsertUtilityCostResponse>, t: Throwable) {
                    if (!isAdded) return
                    btnAddUtility.isEnabled = true
                    showNetworkError(t)
                }
            })
    }

    private fun loadCurrentMonthUtilityEntryForEdit() {
        if (messID == 0) return

        RetrofitClient.managerService.getCurrentMonthUtilityEntries(messID)
            .enqueue(object : Callback<CurrentMonthUtilityEntriesResponse> {
                override fun onResponse(
                    call: Call<CurrentMonthUtilityEntriesResponse>,
                    response: Response<CurrentMonthUtilityEntriesResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        val entry = response.body()!!.entries.firstOrNull()
                        if (entry == null) {
                            Toast.makeText(
                                requireContext(),
                                "No utility bill added for this month yet.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showEditUtilityDialog(entry)
                        }
                    } else {
                        showError(response)
                    }
                }

                override fun onFailure(call: Call<CurrentMonthUtilityEntriesResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun showEditUtilityDialog(entry: CurrentMonthUtilityEntry) {
        val content = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
        val electricityInput = dialogInput(entry.electricity.toString(), "Electricity")
        val internetInput = dialogInput(entry.internet.toString(), "Internet")
        val gasInput = dialogInput(entry.gas.toString(), "Gas")
        val maidInput = dialogInput(entry.maid.toString(), "Maid")

        content.addView(dialogLabel("Electricity"))
        content.addView(electricityInput)
        content.addView(dialogLabel("Internet"))
        content.addView(internetInput)
        content.addView(dialogLabel("Gas"))
        content.addView(gasInput)
        content.addView(dialogLabel("Maid"))
        content.addView(maidInput)

        val dialog = showFormDialog(
            title = "Edit Utility",
            subtitle = "Update the running month utility bill.",
            content = content
        ) { dialog ->
            val electricity = electricityInput.text.toString().trim().toDoubleOrNull()
            val internet = internetInput.text.toString().trim().toDoubleOrNull()
            val gas = gasInput.text.toString().trim().toDoubleOrNull()
            val maid = maidInput.text.toString().trim().toDoubleOrNull()

            when {
                electricity == null || electricity < 0.0 -> electricityInput.error = "Invalid amount"
                internet == null || internet < 0.0 -> internetInput.error = "Invalid amount"
                gas == null || gas < 0.0 -> gasInput.error = "Invalid amount"
                maid == null || maid < 0.0 -> maidInput.error = "Invalid amount"
                else -> updateUtilityEntry(
                    entry.id,
                    InsertUtilityCostRequest(
                        mess_id = messID,
                        electricity = electricity,
                        internet = internet,
                        gas = gas,
                        maid = maid
                    ),
                    dialog
                )
            }
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun updateUtilityEntry(
        utilityID: Int,
        request: InsertUtilityCostRequest,
        dialog: AlertDialog
    ) {
        RetrofitClient.managerService.updateUtilityCost(utilityID, request)
            .enqueue(object : Callback<InsertUtilityCostResponse> {
                override fun onResponse(
                    call: Call<InsertUtilityCostResponse>,
                    response: Response<InsertUtilityCostResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful) {
                        dialog.dismiss()
                        Toast.makeText(requireContext(), "Utility entry updated.", Toast.LENGTH_SHORT).show()
                        loadCurrentMonthUtilityBills()
                    } else {
                        showError(response)
                    }
                }

                override fun onFailure(call: Call<InsertUtilityCostResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun showFormDialog(
        title: String,
        subtitle: String,
        content: View,
        onSave: (AlertDialog) -> Unit
    ): AlertDialog {
        val dialogView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(22), dp(24), dp(20))
            setBackgroundResource(R.drawable.bg_dialog_rounded)
        }
        dialogView.addView(TextView(requireContext()).apply {
            text = title
            gravity = Gravity.CENTER
            textSize = 20f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(requireContext().getColor(R.color.black))
        })
        dialogView.addView(TextView(requireContext()).apply {
            text = subtitle
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(requireContext().getColor(R.color.text_secondary))
            setPadding(0, dp(6), 0, dp(16))
        })
        dialogView.addView(content)

        val actions = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(20), 0, 0)
        }
        val cancelButton = Button(requireContext()).apply {
            text = "Cancel"
            setTextColor(requireContext().getColor(R.color.black))
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                requireContext().getColor(R.color.white)
            )
            layoutParams = LinearLayout.LayoutParams(0, dp(52), 1f).apply {
                marginEnd = dp(8)
            }
        }
        val saveButton = Button(requireContext()).apply {
            text = "Save"
            setTextColor(requireContext().getColor(R.color.white))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                requireContext().getColor(R.color.black)
            )
            layoutParams = LinearLayout.LayoutParams(0, dp(52), 1f).apply {
                marginStart = dp(8)
            }
        }
        actions.addView(cancelButton)
        actions.addView(saveButton)
        dialogView.addView(actions)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener { dialog.dismiss() }
        saveButton.setOnClickListener { onSave(dialog) }
        return dialog
    }

    private fun dialogInput(value: String, hintText: String): EditText {
        return EditText(requireContext()).apply {
            hint = hintText
            setText(value)
            setSingleLine(true)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setBackgroundResource(R.drawable.bg_input_manager)
            setPadding(dp(16), 0, dp(16), 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            )
        }
    }

    private fun dialogLabel(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 13f
            setPadding(0, dp(12), 0, dp(4))
            setTextColor(requireContext().getColor(R.color.text_secondary))
        }
    }

    private fun readAmount(input: EditText): Double? {
        val value = input.text.toString().trim()
        if (value.isEmpty()) return 0.0

        val amount = value.toDoubleOrNull()
        if (amount == null || amount < 0.0) {
            input.error = "Invalid amount"
            return null
        }

        input.error = null
        return amount
    }

    private fun showMonthPicker() {
        val months = resources.getStringArray(R.array.manager_month_names)
        AlertDialog.Builder(requireContext())
            .setTitle("Select month")
            .setItems(months) { _, which ->
                inputUtilityMonth.setText(months[which])
            }
            .show()
    }

    private fun updatePerPersonFromTotal() {
        val totalText = txtTotalUtilityCost.text.toString()
            .replace("৳", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: 0.0
        updatePerPerson(totalText)
    }

    private fun updatePerPerson(totalUtilityBill: Double) {
        val perPerson = if (totalMembers > 0) totalUtilityBill / totalMembers else 0.0
        txtUtilityPerPerson.text = "৳${formatAmount(perPerson)}"
    }

    private fun currentMonthName(): String {
        return SimpleDateFormat("MMMM", Locale.US).format(Date())
    }

    private fun formatAmount(amount: Double): String {
        return NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }.format(amount)
    }

    private fun showError(response: Response<*>) {
        Toast.makeText(requireContext(), getErrorMessage(response), Toast.LENGTH_LONG).show()
    }

    private fun getErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()

            if (errorBody.isNullOrEmpty()) {
                "Something went wrong"
            } else {
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

                when (val message = errorResponse.message) {
                    is String -> message
                    is List<*> -> message.joinToString("\n")
                    else -> errorResponse.error ?: "Something went wrong"
                }
            }
        } catch (e: Exception) {
            "Something went wrong"
        }
    }

    private fun showNetworkError(t: Throwable) {
        if (!isAdded) return

        Toast.makeText(
            requireContext(),
            "Network error: ${t.message}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
