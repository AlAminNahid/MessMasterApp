package com.bilimbistudio.messmaster.managerdashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bilimbistudio.messmaster.R
import com.bilimbistudio.messmaster.managerdashboard.model.utility.CurrentMonthUtilityEntry
import com.bilimbistudio.messmaster.managerdashboard.model.utility.InsertUtilityCostRequest
import com.bilimbistudio.messmaster.managerdashboard.viewmodel.ManagerSharedViewModel
import com.bilimbistudio.messmaster.managerdashboard.viewmodel.UtilityViewModel
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FragmentUtility : Fragment() {

    private val sharedViewModel: ManagerSharedViewModel by activityViewModels { ManagerSharedViewModel.Factory }
    private val utilityViewModel: UtilityViewModel by viewModels { UtilityViewModel.Factory }

    private var messID: Int = 0
    private var totalMembers: Int = 0
    private var editUtilityDialog: AlertDialog? = null

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
        btnEditCurrentUtility.setOnClickListener {
            if (messID == 0) {
                Toast.makeText(requireContext(), "Mess info is still loading.", Toast.LENGTH_SHORT).show()
            } else {
                utilityViewModel.loadUtilityEntries(messID)
            }
        }

        observeStates()
        return view
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    sharedViewModel.currentMessState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                messID = state.data.messInfo.mess_id
                                utilityViewModel.loadUtilityBills(messID)
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    sharedViewModel.messStatisticsState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                totalMembers = state.data.totalMembers
                                updatePerPersonFromTotal()
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    utilityViewModel.utilityBillsState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                val bills = state.data
                                txtElectricityBill.text = "৳${formatAmount(bills.electricity)}"
                                txtInternetBill.text = "৳${formatAmount(bills.internet)}"
                                txtGasBill.text = "৳${formatAmount(bills.gas)}"
                                txtMaidBill.text = "৳${formatAmount(bills.maid)}"
                                txtTotalUtilityCost.text = "৳${formatAmount(bills.totalUtilityBill)}"
                                updatePerPerson(bills.totalUtilityBill)
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    utilityViewModel.utilityEntriesState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                utilityViewModel.consumeUtilityEntries()
                                val entry = state.data.firstOrNull()
                                if (entry == null) {
                                    Toast.makeText(requireContext(), "No utility bill added for this month yet.", Toast.LENGTH_SHORT).show()
                                } else {
                                    showEditUtilityDialog(entry)
                                }
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    utilityViewModel.insertState.collect { state ->
                        when (state) {
                            is UiState.Loading -> btnAddUtility.isEnabled = false
                            is UiState.Success -> {
                                btnAddUtility.isEnabled = true
                                inputElectricityAmount.text?.clear()
                                inputInternetAmount.text?.clear()
                                inputGasAmount.text?.clear()
                                inputMaidAmount.text?.clear()
                                Toast.makeText(
                                    requireContext(),
                                    "Utility bills saved for ${inputUtilityMonth.text}.",
                                    Toast.LENGTH_LONG
                                ).show()
                                utilityViewModel.consumeInsertState()
                                utilityViewModel.loadUtilityBills(messID)
                            }
                            is UiState.Error -> {
                                btnAddUtility.isEnabled = true
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                utilityViewModel.consumeInsertState()
                            }
                            else -> Unit
                        }
                    }
                }

                launch {
                    utilityViewModel.updateState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                editUtilityDialog?.dismiss()
                                editUtilityDialog = null
                                Toast.makeText(requireContext(), "Utility entry updated.", Toast.LENGTH_SHORT).show()
                                utilityViewModel.consumeUpdateState()
                                utilityViewModel.loadUtilityBills(messID)
                            }
                            is UiState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                utilityViewModel.consumeUpdateState()
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
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

        utilityViewModel.insertUtilityCost(
            InsertUtilityCostRequest(
                mess_id = messID,
                electricity = electricity!!,
                internet = internet!!,
                gas = gas!!,
                maid = maid!!
            )
        )
    }

    private fun showEditUtilityDialog(entry: CurrentMonthUtilityEntry) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_utility, null)
        val inputElectricity = dialogView.findViewById<EditText>(R.id.inputEditElectricity)
        val inputInternet = dialogView.findViewById<EditText>(R.id.inputEditInternet)
        val inputGas = dialogView.findViewById<EditText>(R.id.inputEditGas)
        val inputMaid = dialogView.findViewById<EditText>(R.id.inputEditMaid)

        inputElectricity.setText(entry.electricity.toString())
        inputInternet.setText(entry.internet.toString())
        inputGas.setText(entry.gas.toString())
        inputMaid.setText(entry.maid.toString())

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialogView.findViewById<Button>(R.id.btnEditUtilityCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnEditUtilitySave).setOnClickListener {
            val electricity = inputElectricity.text.toString().trim().toDoubleOrNull()
            val internet = inputInternet.text.toString().trim().toDoubleOrNull()
            val gas = inputGas.text.toString().trim().toDoubleOrNull()
            val maid = inputMaid.text.toString().trim().toDoubleOrNull()

            when {
                electricity == null || electricity < 0.0 -> inputElectricity.error = "Invalid amount"
                internet == null || internet < 0.0 -> inputInternet.error = "Invalid amount"
                gas == null || gas < 0.0 -> inputGas.error = "Invalid amount"
                maid == null || maid < 0.0 -> inputMaid.error = "Invalid amount"
                else -> utilityViewModel.updateUtilityCost(
                    entry.id,
                    InsertUtilityCostRequest(
                        mess_id = messID,
                        electricity = electricity,
                        internet = internet,
                        gas = gas,
                        maid = maid
                    )
                )
            }
        }

        editUtilityDialog = dialog
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.92).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun showMonthPicker() {
        val months = resources.getStringArray(R.array.manager_month_names)
        AlertDialog.Builder(requireContext())
            .setTitle("Select month")
            .setItems(months) { _, which -> inputUtilityMonth.setText(months[which]) }
            .show()
    }

    private fun updatePerPersonFromTotal() {
        val totalText = txtTotalUtilityCost.text.toString().replace("৳", "").replace(",", "").trim().toDoubleOrNull() ?: 0.0
        updatePerPerson(totalText)
    }

    private fun updatePerPerson(totalUtilityBill: Double) {
        val perPerson = if (totalMembers > 0) totalUtilityBill / totalMembers else 0.0
        txtUtilityPerPerson.text = "৳${formatAmount(perPerson)}"
    }

    private fun currentMonthName(): String = SimpleDateFormat("MMMM", Locale.US).format(Date())

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

    private fun formatAmount(amount: Double): String {
        return NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }.format(amount)
    }
}
