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
import com.example.web_api_project.R
import com.example.web_api_project.databinding.ActivityEditMealsBinding
import com.example.web_api_project.model.Meals
import com.squareup.picasso.Picasso
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

class EditMealsActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private lateinit var binding: ActivityEditMealsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditMealsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = resources.getColor(R.color.PC)
        binding.progressBar.visibility = View.VISIBLE
        val data = intent.getParcelableExtra<Meals>("meal")
        binding.edNameMeals.setText(data!!.name)
        binding.edIngreMeals.setText(data.details)
        binding.edOriginUpdate.setText(data.price.toString())
        binding.edQun.setText(data.quantity.toString())
        binding.ratingBarUpdate.rating = data.rate
        Picasso.get().load(data.image!!.replace("localhost", "192.168.137.1"))
            .into(binding.imgUpdate)
        getImage(binding)

        binding.progressBar.visibility = View.GONE

        binding.btnUpdate.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val nameUpdate = binding.edNameMeals.text.toString().trim()
            val descriptionUpdate = binding.edIngreMeals.text.toString().trim()
            val priceUpdate = binding.edOriginUpdate.text.toString().trim()
            val quantityUpdate = binding.edQun.text.toString().trim()
            val ratingUpdate = binding.ratingBarUpdate.rating
            val imageUpdate = imageUri
            val id = data.id

            if (nameUpdate.isNotEmpty() && descriptionUpdate.isNotEmpty() && priceUpdate.isNotEmpty() && quantityUpdate.isNotEmpty() && ratingUpdate > 0 && imageUpdate != null && id != null) {
                val priceValue = priceUpdate.toFloatOrNull()
                val quantityValue = quantityUpdate.toIntOrNull()
                if (priceValue != null && quantityValue != null) {
                    val requestBodyName = nameUpdate.toRequestBody("text/plain".toMediaTypeOrNull())
                    val requestBodyDetails =
                        descriptionUpdate.toRequestBody("text/plain".toMediaTypeOrNull())
                    val requestBodyPrice =
                        priceValue.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val requestBodyQuantity =
                        quantityValue.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val requestBodyRate =
                        ratingUpdate.toString().toRequestBody("text/plain".toMediaTypeOrNull())

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
                        val allData = intent.getParcelableArrayListExtra<Meals>("all-data")
                        var found = false
                        if (allData != null) {
                            for (i in allData) {
                                if (i.name == nameUpdate) {
                                    Toast.makeText(
                                        this@EditMealsActivity,
                                        "The meal name already exists!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    found = true
                                    break
                                }
                            }
                        }
                        if (!found) {
                            updateMealInServer(
                                requestBodyName,
                                requestBodyDetails,
                                requestBodyPrice,
                                requestBodyQuantity,
                                imagePart,
                                requestBodyRate,
                                id.toInt(),
                                this
                            )
                        }

                        clearFields(binding)
                        binding.progressBar.visibility = View.GONE
                    } catch (e: Exception) {
                        // معالجة الخطأ في حالة حدوث مشكلة في إنشاء الملف المؤقت أو قراءة الصورة
                        Log.e("bmr", "Error: ${e.message}")
                        binding.progressBar.visibility = View.GONE
                    }
                } else {
                    // إظهار رسالة خطأ في حالة عدم توفر القيم الصحيحة للسعر والكمية
                    Log.e("bmr", "Error: Invalid price or quantity")
                    binding.progressBar.visibility = View.GONE
                }
            } else {
                // إظهار رسالة خطأ في حالة عدم توفر كل البيانات المطلوبة
                Log.e("bmr", "Error: Incomplete data")
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun clearFields(binding: ActivityEditMealsBinding) {
        binding.progressBar.visibility = View.VISIBLE

        binding.edNameMeals.text.clear()
        binding.edIngreMeals.text.clear()
        binding.edOriginUpdate.text.clear()
        binding.edQun.text.clear()
        binding.imgUpdate.setImageResource(R.drawable.ic_upload)
        binding.ratingBarUpdate.rating = 0.0f
        binding.edQun.text.clear()
        binding.progressBar.visibility = View.GONE
    }

    private fun getImage(binding: ActivityEditMealsBinding) {
        val pickImage =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    imageUri = uri
                    binding.imgUpdate.setImageURI(imageUri)
                }
            }

        val permissions =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    pickImage.launch("image/*")
                } else {
                    Toast.makeText(
                        this@EditMealsActivity,
                        " R.string.permission_denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        binding.imgUpdate.setOnClickListener {
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

    private fun updateMealInServer(
        mealName: RequestBody,
        details: RequestBody,
        price: RequestBody,
        quantity: RequestBody,
        image: MultipartBody.Part,
        rate: RequestBody,
        id: Int,
        activity: EditMealsActivity
    ) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1/Final-Project/controller/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val call = apiService.updateMeal(mealName, details, price, quantity, image, rate, id)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Meal has not been Edit \uD83D\uDE0B",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("bmr", "Success: ${response}")
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(activity, MealsActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, 1000)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Error while modifying data ☹️",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("bmr", "Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Error while modifying data ☹️",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("bmr", "Error: ${t.message}")
            }
        })
    }

    interface ApiService {
        @Multipart
        @POST("updateMeal.php")
        fun updateMeal(
            @Part("name") mealName: RequestBody,
            @Part("details") ingredients: RequestBody,
            @Part("price") price: RequestBody,
            @Part("quantity") quantity: RequestBody,
            @Part image: MultipartBody.Part,
            @Part("rate") rate: RequestBody,
            @Part("id") id: Int
        ): Call<ResponseBody>
    }
}
