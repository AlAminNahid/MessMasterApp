package com.example.messmaster.commondashboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messmaster.R
import com.example.messmaster.commondashboard.adapter.Messes
import com.example.messmaster.commondashboard.model.AllMessResponse
import com.example.messmaster.commondashboard.model.joinMess.JoinMessRequest
import com.example.messmaster.commondashboard.model.joinMess.JoinMessResponse
import com.example.messmaster.commondashboard.model.MessItems
import com.example.messmaster.network.RetrofitClient
import com.example.messmaster.model.ErrorResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JoinMessActivity: AppCompatActivity() {

    lateinit var btnBack: ImageButton
    lateinit var searchMess: EditText
    lateinit var availableMessRecyclerView: RecyclerView
    lateinit var messAdapter: Messes
    private var allMesses = mutableListOf<MessItems>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comm_dashboard_join_mess)

        RetrofitClient.init(applicationContext)

        btnBack = findViewById<ImageButton>(R.id.btnBack)
        searchMess = findViewById<EditText>(R.id.searchMess)
        availableMessRecyclerView = findViewById<RecyclerView>(R.id.availableMessRecyclerView)

        messAdapter = Messes(
            messes = mutableListOf(),
            onJoinClick = { mess ->
                joinMess(mess.id)
            }
        )

        availableMessRecyclerView.layoutManager = LinearLayoutManager(this)
        availableMessRecyclerView.adapter = messAdapter

        btnBack.setOnClickListener {
            finish()
        }

        searchMess.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMesses(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        loadAllMesses()
    }

    private fun loadAllMesses() {
        RetrofitClient.messService.getAllMesses()
            .enqueue(object : Callback<AllMessResponse>{
                override fun onResponse(
                    call: Call<AllMessResponse>,
                    response: Response<AllMessResponse>
                ) {
                    if(response.isSuccessful){
                        val messes = response.body()?.messes ?: emptyList()
                        allMesses.clear()
                        allMesses.addAll(messes)
                        messAdapter.updateList(allMesses)

                        if(messes.isEmpty()) {
                            Toast.makeText(this@JoinMessActivity, "No Mess available", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        val errorMessage = getAllMessesErrorMessage(response)

                        Toast.makeText(this@JoinMessActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AllMessResponse>, t: Throwable){
                    Toast.makeText(this@JoinMessActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun filterMesses(query: String){
        val filteredList = if (query.isBlank()) {
            allMesses
        } else {
            allMesses.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.address.contains(query, ignoreCase = true)
            }
        }

        messAdapter.updateList(filteredList)
    }

    private fun joinMess(messID: Int){
        val request = JoinMessRequest(messID)

        RetrofitClient.messService.joinMess(request)
            .enqueue(object : Callback<JoinMessResponse> {
                override fun onResponse(
                    call: Call<JoinMessResponse>,
                    response: Response<JoinMessResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@JoinMessActivity, response.body()?.message ?: "Joined successfully", Toast.LENGTH_SHORT).show()

//                        val intent = Intent(
//                            this@JoinMessActivity,
//                            MemberDashboardActivity::class.java
//                        )
//                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                        startActivity(intent)
//                        finish()
                    }
                    else{
                        val errorMessage = getJoinMessErrorMessage(response)

                        Toast.makeText(this@JoinMessActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<JoinMessResponse>, t: Throwable) {
                    Toast.makeText(this@JoinMessActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getJoinMessErrorMessage(response: Response<JoinMessResponse>) : String {
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

    private fun getAllMessesErrorMessage(response: Response<AllMessResponse>) : String {
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
}