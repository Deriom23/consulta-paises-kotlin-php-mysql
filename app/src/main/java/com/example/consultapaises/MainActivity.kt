package com.example.consultapaises

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.consultapaises.model.ApiResponse
import com.example.consultapaises.model.Country
import com.example.consultapaises.network.ApiClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {
    private lateinit var layout: TextInputLayout
    private lateinit var input: TextInputEditText
    private lateinit var searchButton: MaterialButton
    private lateinit var progress: ProgressBar
    private lateinit var card: MaterialCardView
    private lateinit var flag: ImageView
    private lateinit var name: TextView
    private lateinit var details: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_main)
        layout = findViewById(R.id.countryLayout); input = findViewById(R.id.countryInput)
        searchButton = findViewById(R.id.searchButton); progress = findViewById(R.id.progressBar)
        card = findViewById(R.id.resultCard); flag = findViewById(R.id.flagImage)
        name = findViewById(R.id.countryName); details = findViewById(R.id.countryDetails)
        searchButton.setOnClickListener { search() }
        findViewById<MaterialButton>(R.id.historyButton).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        input.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEARCH) { search(); true } else false
        }
    }

    private fun search() {
        layout.error = null
        val query = SearchValidator.normalize(input.text?.toString().orEmpty())
        if (query == null) { layout.error = getString(R.string.empty_country); return }
        loading(true)
        ApiClient.service.searchCountry(query).enqueue(object : Callback<ApiResponse<Country>> {
            override fun onResponse(call: Call<ApiResponse<Country>>, response: Response<ApiResponse<Country>>) {
                loading(false); val body = response.body()
                if (response.isSuccessful && body?.success == true && body.data != null) show(body.data)
                else { card.visibility = View.GONE; message(body?.message ?: getString(R.string.connection_error)) }
            }
            override fun onFailure(call: Call<ApiResponse<Country>>, t: Throwable) {
                loading(false); card.visibility = View.GONE; message(getString(R.string.connection_error))
            }
        })
    }

    private fun show(country: Country) {
        name.text = "${country.name} (${country.code})"
        details.text = "${getString(R.string.capital)}: ${country.capital}\n${getString(R.string.continent)}: ${country.continent}\n" +
            "${getString(R.string.population)}: ${NumberFormat.getIntegerInstance().format(country.population)}\n" +
            "${getString(R.string.income_level)}: ${country.incomeLevel}\n${getString(R.string.coordinates)}: ${country.coordinates}"
        Glide.with(this).load(country.flagUrl).into(flag); card.visibility = View.VISIBLE
    }

    private fun loading(value: Boolean) { progress.visibility = if (value) View.VISIBLE else View.GONE; searchButton.isEnabled = !value }
    private fun message(value: String) = Toast.makeText(this, value, Toast.LENGTH_LONG).show()
}
