package com.example.web_api_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.web_api_project.databinding.ActivityMainBinding
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private val mealsUrl = "http://192.168.1.107/Final-Project/controller/getAllMeals.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = resources.getColor(R.color.PC)
        val queue = Volley.newRequestQueue(this)
        val categoriesRequest = JsonObjectRequest(
            Request.Method.GET, mealsUrl, null,
            { response ->
                val success = response.getBoolean("success")
                if (success) {
                    getMealsData(response)
                }
            },
            { error ->
                Log.e("bmr", "Error retrieving meals: ${error.message}")
            }
        )

        queue.add(categoriesRequest)
    }

    private fun getMealsData(response: JSONObject) {
        val mealsData = response.getJSONArray("data")
        for (i in 0 until mealsData.length()) {
            val mealObject = mealsData.getJSONObject(i)
            val id = mealObject.getString("id")
            val name = mealObject.getString("name")
            val details = mealObject.getString("details")
            val price = mealObject.getString("price")
            val quantity = mealObject.getString("quantity")
            val image = mealObject.getString("image")
            val createdDate = mealObject.getString("created_at")

            // قم بطباعة بيانات الوجبة في Log
            Log.d("bmr", "Meal ID: $id")
            Log.d("bmr", "Meal Name: $name")
            Log.d("bmr", "Meal Details: $details")
            Log.d("bmr", "Meal Price: $price")
            Log.d("bmr", "Meal Quantity: $quantity")
            Log.d("bmr", "Meal Image: $image")
            Log.d("bmr", "Meal Created Date: $createdDate")
        }
    }

}

