package com.example.web_api_project.adapter

import com.example.web_api_project.ui.meals.EditMealsActivity
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.web_api_project.R
import com.example.web_api_project.databinding.MealsItemBinding
import com.example.web_api_project.model.Meals
import com.example.web_api_project.ui.meals.MealsActivity
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


class MealsAdapter(private var activity: Activity, private var data: ArrayList<Meals>) :
    RecyclerView.Adapter<MealsAdapter.MyViewHolder>() {
    class MyViewHolder(var binding: MealsItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = MealsItemBinding.inflate(activity.layoutInflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = data[position]
        holder.binding.tvName.text = item.name
        holder.binding.tvIngredients.text = item.details
        holder.binding.tvPrice.text = "price: "+item.price.toString() + "$"
        holder.binding.tvQun.text = "quantity: "+item.quantity.toString()
        holder.binding.ratingBar2.rating = item.rate
        Log.e("bmr",item.image.toString())
        Picasso.get().load(item.image!!.replace("localhost", "192.168.137.1")).into(holder.binding.imgItem)


        holder.binding.root.setOnLongClickListener { view ->
            val popupMenu = PopupMenu(activity, holder.binding.root)
            popupMenu.menuInflater.inflate(R.menu.options_menu_edit, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.edit -> {
                        val intent = Intent(activity, EditMealsActivity::class.java)
                        intent.putExtra("meal",item)
                        intent.putExtra("all-data",data)
                        activity.startActivity(intent)
                    }

                    R.id.delete -> {
                        deleteMealFromServer(item.id!!)
                        Toast.makeText(activity,"Meal Deleted Successfully",Toast.LENGTH_SHORT).show()
                        data.remove(item)
                        notifyDataSetChanged()
                    }


                }
                true
            }
            popupMenu.show()



            true
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun deleteMealFromServer(mealId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.137.1/Final-Project/controller/") // قم بتعيين عنوان URL الصحيح هنا
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val call = apiService.deleteMealById(mealId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // تنفيذ الإجراءات المناسبة عندما يتم حذف العنصر بنجاح
                    Log.e("bmr", "Success")
                } else {
                    // تنفيذ الإجراءات المناسبة عند حدوث خطأ في الاستجابة
                    Log.e("bmr", "Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // تنفيذ الإجراءات المناسبة عند حدوث خطأ في الشبكة
                Log.e("bmr", "Error: ${t.message}")
            }
        })
    }

    interface ApiService {
        // قم بتعريف طريقة لحذف وجبة الطعام من الخادم بواسطة معرف الوجبة
        @FormUrlEncoded
        @POST("deleteMealById.php")
        fun deleteMealById(@Field("id") mealId: String): Call<ResponseBody>
    }

}