package com.example.web_api_project.ui.meals

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import com.example.web_api_project.R
import com.example.web_api_project.databinding.ActivityAddMealsBinding
import com.example.web_api_project.model.Meals
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.io.IOException

interface ApiService {
    @Multipart
    @POST("insertNewMeal.php")
    fun addMeal(
        @Part("name") mealName: okhttp3.RequestBody,
        @Part("details") ingredients: okhttp3.RequestBody,
        @Part("price") price: okhttp3.RequestBody,
        @Part("quantity") quantity: okhttp3.RequestBody,
        @Part image: MultipartBody.Part,
        @Part("rate") rate: okhttp3.RequestBody
    ): Call<ResponseBody>
}

class AddMealsActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private lateinit var binding: ActivityAddMealsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMealsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = resources.getColor(R.color.PC)

        getImage(binding)

        binding.btnAdd.setOnClickListener {
            val mealName = binding.edNameRset.text.toString().trim()
            val details = binding.edDescriptionRest.text.toString().trim()
            val price = binding.edPriceMeals.text.toString().trim()
            val quantity = binding.edQun.text.toString().trim()
            val rate = binding.ratingBarAdd.rating.toString().trim()

            if (mealName.isEmpty() || details.isEmpty() || imageUri == null) {
                Toast.makeText(
                    this@AddMealsActivity,
                    R.string.fail_fields,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val priceValue = price.toFloatOrNull()
                val quantityValue = quantity.toIntOrNull()
                val rateValue = rate.toFloatOrNull()

                if (priceValue != null && quantityValue != null && rateValue != null) {
                    val requestBodyName = mealName.toRequestBody("text/plain".toMediaTypeOrNull())
                    val requestBodyDetails = details.toRequestBody("text/plain".toMediaTypeOrNull())
                    val requestBodyPrice =
                        priceValue.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val requestBodyQuantity =
                        quantityValue.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val requestBodyRate =
                        rateValue.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                    try {
                        val inputStream = contentResolver.openInputStream(imageUri!!)
                        val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                        file.createNewFile()
                        file.outputStream().use { outputStream ->
                            val buffer = ByteArray(4 * 1024)
                            var read: Int
                            while (inputStream!!.read(buffer).also { read = it } != -1) {
                                outputStream.write(buffer, 0, read)
                            }
                        }
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        val imagePart =
                            MultipartBody.Part.createFormData("image", file.name, requestFile)
                        val data = intent.getParcelableArrayListExtra<Meals>("all-data_add")
                        var found = false
                        if (data != null) {
                            for (i in data) {
                                if (i.name == mealName) {
                                    Toast.makeText(
                                        this@AddMealsActivity,
                                        "The meal name already exists!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    found = true
                                    break
                                }
                            }
                        }
                        if (!found) {
                            addMealToServer(
                                requestBodyName,
                                requestBodyDetails,
                                requestBodyPrice,
                                requestBodyQuantity,
                                imagePart,
                                requestBodyRate,
                                this
                            )
                            clearFields(binding)
                        }




                    } catch (e: IOException) {
                        Log.e("bmr", "Error: ${e.message}")
                    }
                } else {
                    Toast.makeText(
                        this@AddMealsActivity,
                        "R.string.invalid_input",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getImage(binding: ActivityAddMealsBinding) {
        val pickImage =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    imageUri = uri
                    binding.imgAddMeals.setImageURI(imageUri)
                }
            }

        val permissions =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    pickImage.launch("image/*")
                } else {
                    Toast.makeText(
                        this@AddMealsActivity,
                        "R.string.permission_denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        binding.imgAddMeals.setOnClickListener {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pickImage.launch("image/*")
            } else {
                permissions.launch(permission)
            }
        }
    }

    private fun clearFields(binding: ActivityAddMealsBinding) {
        binding.progressBar.visibility = View.VISIBLE

        binding.edNameRset.text.clear()
        binding.edDescriptionRest.text.clear()
        binding.edPriceMeals.text.clear()
        binding.imgAddMeals.setImageResource(R.drawable.ic_upload)
        binding.ratingBarAdd.rating = 0.0f
        binding.edQun.text.clear()
        binding.progressBar.visibility = View.GONE
    }

    private fun addMealToServer(
        mealName: okhttp3.RequestBody,
        ingredients: okhttp3.RequestBody,
        price: okhttp3.RequestBody,
        quantity: okhttp3.RequestBody,
        imagePart: MultipartBody.Part,
        rate: okhttp3.RequestBody,
        activity: AddMealsActivity
    ) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1/Final-Project/controller/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val call = apiService.addMeal(mealName, ingredients, price, quantity, imagePart, rate)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // المعالجة عندما يكون الاستجابة ناجحة
                    Toast.makeText(
                        applicationContext,
                        "Meal has been added \uD83D\uDE0B",
                        Toast.LENGTH_SHORT
                    ).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(activity, MealsActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, 1000)
                    Log.e("bmr", "Success")
                    Log.e("bmr", response.body().toString())
                } else {
                    // المعالجة عندما يكون هناك خطأ في الاستجابة
                    try {
                        val errorBody = response.errorBody()?.string()
                        val jsonObject = JSONObject(errorBody)
                        val errorMessage = jsonObject.getString("count")
                        Toast.makeText(
                            applicationContext,
                            errorMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("bmr", "Error: $errorMessage")
                    } catch (e: Exception) {
                        Toast.makeText(
                            applicationContext,
                            "Meal has not been added! ☹️",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("bmr", "Error: ${response.message()}")
                    }
                }
            }


            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // تنفيذ الإجراءات المناسبة عند حدوث خطأ في الشبكة
                Log.e("bmr", "Error: ${t.message}")
            }
        })
    }
}
