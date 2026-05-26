package com.example.messmaster.managerdashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.messmaster.R
import com.example.messmaster.managerdashboard.model.CurrentMessResponse
import com.example.messmaster.managerdashboard.model.MessStatisticsResponse
import com.example.messmaster.managerdashboard.model.UpdateUserProfileRequest
import com.example.messmaster.model.ErrorResponse
import com.example.messmaster.model.UserProfileResponse
import com.example.messmaster.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentProfile : Fragment() {

    private var userID: Int = 0
    private var currentProfile: UserProfileResponse? = null

    private lateinit var txtProfileName: TextView
    private lateinit var txtProfileEmail: TextView
    private lateinit var txtProfilePhone: TextView
    private lateinit var txtProfileNid: TextView
    private lateinit var txtProfileMessName: TextView
    private lateinit var txtProfileMessAddress: TextView
    private lateinit var txtProfileTotalMembers: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnOpenSettings: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_manager_profile,
            container,
            false
        )

        txtProfileName = view.findViewById(R.id.txtProfileName)
        txtProfileEmail = view.findViewById(R.id.txtProfileEmail)
        txtProfilePhone = view.findViewById(R.id.txtProfilePhone)
        txtProfileNid = view.findViewById(R.id.txtProfileNid)
        txtProfileMessName = view.findViewById(R.id.txtProfileMessName)
        txtProfileMessAddress = view.findViewById(R.id.txtProfileMessAddress)
        txtProfileTotalMembers = view.findViewById(R.id.txtProfileTotalMembers)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnOpenSettings = view.findViewById(R.id.btnOpenSettings)

        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userID = prefs.getInt("userID", 0)

        btnEditProfile.setOnClickListener { showEditProfileDialog() }
        btnOpenSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        loadUserProfile()
        loadMessInfo()
        loadMessStatistics()

        return view
    }

    private fun loadUserProfile() {
        if (userID == 0) {
            Toast.makeText(requireContext(), "User ID not found.", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.managerService.getUserById(userID)
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        currentProfile = response.body()
                        bindUserProfile(response.body()!!)
                    } else {
                        showError(response)
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun bindUserProfile(profile: UserProfileResponse) {
        txtProfileName.text = profile.name
        txtProfileEmail.text = profile.email
        txtProfilePhone.text = profile.phone
        txtProfileNid.text = profile.nid
    }

    private fun loadMessInfo() {
        RetrofitClient.managerService.getCurrentMess()
            .enqueue(object : Callback<CurrentMessResponse> {
                override fun onResponse(
                    call: Call<CurrentMessResponse>,
                    response: Response<CurrentMessResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        val messInfo = response.body()!!.messInfo
                        txtProfileMessName.text = messInfo.mess_name
                        txtProfileMessAddress.text = messInfo.mess_address
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
                        txtProfileTotalMembers.text = response.body()!!.totalMembers.toString()
                    } else {
                        showError(response)
                    }
                }

                override fun onFailure(call: Call<MessStatisticsResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun showEditProfileDialog() {
        val profile = currentProfile
        if (profile == null) {
            Toast.makeText(requireContext(), "Profile is still loading.", Toast.LENGTH_SHORT).show()
            return
        }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
        }
        val inputName = EditText(requireContext()).apply {
            hint = "Name"
            setText(profile.name)
        }
        val inputPhone = EditText(requireContext()).apply {
            hint = "Phone"
            setText(profile.phone)
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }

        container.addView(inputName)
        container.addView(inputPhone)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit profile")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = inputName.text.toString().trim()
                        val phone = inputPhone.text.toString().trim()

                        when {
                            name.isEmpty() -> inputName.error = "Name is required"
                            phone.isEmpty() -> inputPhone.error = "Phone is required"
                            else -> updateProfile(name, phone, this)
                        }
                    }
                }
                show()
            }
    }

    private fun updateProfile(name: String, phone: String, dialog: AlertDialog) {
        RetrofitClient.managerService.updateUserProfile(UpdateUserProfileRequest(name, phone))
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        currentProfile = response.body()
                        bindUserProfile(response.body()!!)
                        dialog.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "Profile updated successfully.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(requireContext(), getErrorMessage(response), Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun showError(response: Response<*>) {
        Toast.makeText(requireContext(), getErrorMessage(response), Toast.LENGTH_LONG).show()
    }

    private fun getErrorMessage(response: Response<*>) : String {
        return try {
            val errorBody = response.errorBody()?.string()

            if(errorBody.isNullOrEmpty()){
                "Something went wrong"
            } else{
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

                when(val message = errorResponse.message){
                    is String -> message
                    is List<*> -> message.joinToString("\n")
                    else -> errorResponse.error ?: "Something went wrong"
                }
            }
        } catch (e: Exception){
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
}
