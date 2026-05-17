package com.example.messmaster.commondashboard

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.messmaster.R
import com.example.messmaster.model.ErrorResponse
import com.example.messmaster.commondashboard.model.createMess.CreateMessRequest
import com.example.messmaster.commondashboard.model.createMess.CreateMessResponse
import com.example.messmaster.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateMessActivity : AppCompatActivity() {

    lateinit var btnBack: ImageButton
    lateinit var etMessName: EditText
    lateinit var etAddress: EditText
    lateinit var btnCreateMess: Button

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comm_dashboard_create_mess)

        RetrofitClient.init(applicationContext)

        btnBack = findViewById<ImageButton>(R.id.btnBack)
        etMessName = findViewById<EditText>(R.id.etMessName)
        etAddress = findViewById<EditText>(R.id.etAddress)
        btnCreateMess = findViewById<Button>(R.id.btnCreateMess)

        btnBack.setOnClickListener {
            finish()
        }

        btnCreateMess.setOnClickListener {
            val messName = etMessName.text.toString().trim()
            val messAddress= etAddress.text.toString().trim()

            if(validation(messName, messAddress)){
                createMess(messName, messAddress)
            }
        }
    }

    private fun createMess(messName: String, messAddress: String) {
        btnCreateMess.isEnabled = false

        val request = CreateMessRequest(
            name = messName,
            address = messAddress
        )

        RetrofitClient.commMessService.createMess(request)
            .enqueue(object : Callback<CreateMessResponse> {
                override fun onResponse(
                    call: Call<CreateMessResponse>,
                    response: Response<CreateMessResponse>
                ) {
                    btnCreateMess.isEnabled = true

                    if(response.isSuccessful){
                        Toast.makeText(this@CreateMessActivity, response.body()?.message ?: "Mess Created successfully",
                            Toast.LENGTH_SHORT).show()

//                        val intent = Intent(this@CreateMessActivity, ManagerActivity::class.java)
//                        startActivity(intent)
//                        finish()
                    }
                    else{
                        val errorMessage = getErrorMessage(response)

                        Toast.makeText(this@CreateMessActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CreateMessResponse>, t: Throwable){
                    btnCreateMess.isEnabled = true

                    Toast.makeText(this@CreateMessActivity, t.message ?: "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getErrorMessage(response: Response<CreateMessResponse>) : String {
        return try{
            val errorBody = response.errorBody()?.string()

            if(errorBody.isNullOrEmpty()){
                "Something went wrong"
            }
            else{
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

                when(val message = errorResponse.message){
                    is String -> message
                    is List<*> -> message.joinToString("\n")
                    else -> errorResponse.error ?: "Something went wrong"
                }
            }
        }
        catch (e: Exception) {
            "Something went wrong"
        }
    }

    private fun validation(messName: String, messAddress: String): Boolean{
        if(messName.isEmpty()){
            etMessName.error = "Mess name can't be empty"
            return false
        }
        if(messName.length > 200){
            etMessName.error = "Mess name can't be greater than 200 characters"
            return false
        }
        if (messAddress.isEmpty()){
            etAddress.error = "Address can't be empty"
            return false
        }

        return true
    }
}