package com.example.web_api_project.ui.meals

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.web_api_project.R
import com.example.web_api_project.adapter.MealsAdapter
import com.example.web_api_project.databinding.ActivityMealsBinding
import com.example.web_api_project.model.Meals
import org.json.JSONObject

class MealsActivity : AppCompatActivity() {
    private val mealsUrl = "http://192.168.137.1/Final-Project/controller/getAllMeals.php"
    private lateinit var binding: ActivityMealsBinding
    private val data = ArrayList<Meals>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = resources.getColor(R.color.PC)
        if (data.isEmpty()){
            binding.imageView5.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
            binding.tvChoose.text = "Add meals to view \uD83E\uDD58"
        }else{
            binding.tvChoose.text = "All meals \uD83C\uDF72"
        }
        binding.fab.setOnClickListener {
            val intent = Intent(this, AddMealsActivity::class.java)
            intent.putExtra("all-data_add",data)
            startActivity(intent)
        }

        val queue = Volley.newRequestQueue(this)
        val mealsRequest = JsonObjectRequest(
            Request.Method.GET, mealsUrl, null,
            { response ->
                val success = response.getBoolean("success")
                if (success) {
                    getMealsData(response)
                    displayMeals()
                } else {
                    // تنفيذ الإجراء المناسب في حالة عدم النجاح
                }
            },
            { error ->
                Log.e("bmr", "Error retrieving meals: ${error.message}")
                // تنفيذ الإجراء المناسب في حالة حدوث خطأ
            }
        )

        queue.add(mealsRequest)
    }

    private fun getMealsData(response: JSONObject) {
        val mealsData = response.getJSONArray("data")
        for (i in 0 until mealsData.length()) {
            val mealObject = mealsData.getJSONObject(i)
            val id = mealObject.getString("id")
            val name = mealObject.getString("name")
            val details = mealObject.getString("details")
            val price = mealObject.getString("price").toFloat()
            val quantity = mealObject.getString("quantity").toInt()
            val image = mealObject.getString("image")
            val rate = mealObject.getString("rate").toFloat()
            val createdDate = mealObject.getString("created_at")

            val meal = Meals(id, name, details, price, quantity, image, rate,createdDate)
            data.add(meal)
        }
    }

    private fun displayMeals() {
        if (data.isEmpty()){
            binding.imageView5.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
            binding.tvChoose.text = "Add meals to view \uD83E\uDD58"
        }else{
            binding.tvChoose.text = "All meals \uD83C\uDF72"
        }
        binding.rvMeals.layoutManager = LinearLayoutManager(this)
        val mealsAdapter = MealsAdapter(this, data)
        binding.rvMeals.adapter = mealsAdapter
        if (data.isEmpty()) {
            binding.imageView5.visibility = View.VISIBLE
            binding.imageView5.visibility = View.GONE
            binding.tvChoose.text = "Add meals to view \uD83E\uDD58"
        } else {
            binding.imageView5.visibility = View.GONE
            binding.tvChoose.text = "All meals \uD83C\uDF72"

        }
    }
}
