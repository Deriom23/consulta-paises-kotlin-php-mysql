package com.example.consultapaises

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.consultapaises.model.ApiResponse
import com.example.consultapaises.model.HistoryItem
import com.example.consultapaises.network.ApiClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryActivity : AppCompatActivity() {
    private lateinit var container: LinearLayout
    private lateinit var empty: TextView
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_history)
        container = findViewById(R.id.historyContainer); empty = findViewById(R.id.emptyText)
        progress = findViewById(R.id.historyProgress)
        findViewById<MaterialButton>(R.id.clearButton).setOnClickListener { confirmClear() }
        loadHistory()
    }

    private fun loadHistory() {
        progress.visibility = View.VISIBLE
        ApiClient.service.getHistory().enqueue(object : Callback<ApiResponse<List<HistoryItem>>> {
            override fun onResponse(call: Call<ApiResponse<List<HistoryItem>>>, response: Response<ApiResponse<List<HistoryItem>>>) {
                progress.visibility = View.GONE; val body = response.body()
                if (response.isSuccessful && body?.success == true) render(body.data.orEmpty())
                else message(body?.message ?: getString(R.string.connection_error))
            }
            override fun onFailure(call: Call<ApiResponse<List<HistoryItem>>>, t: Throwable) {
                progress.visibility = View.GONE; message(getString(R.string.connection_error))
            }
        })
    }

    private fun render(items: List<HistoryItem>) {
        container.removeAllViews(); empty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        items.forEach { item ->
            val text = TextView(this).apply {
                setPadding(dp(18), dp(16), dp(18), dp(16)); setTextColor(getColor(R.color.navy)); textSize = 16f
                this.text = "${item.countryName} (${item.countryCode})\n${getString(R.string.searched_as, item.searchTerm)}\n${item.searchedAt}"
            }
            val card = MaterialCardView(this).apply { radius = dp(16).toFloat(); cardElevation = dp(2).toFloat(); addView(text) }
            container.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })
        }
    }

    private fun confirmClear() = MaterialAlertDialogBuilder(this).setTitle(R.string.clear_history)
        .setMessage(R.string.clear_question).setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.delete) { _, _ -> clearHistory() }.show()

    private fun clearHistory() {
        ApiClient.service.clearHistory().enqueue(object : Callback<ApiResponse<Map<String, Int>>> {
            override fun onResponse(call: Call<ApiResponse<Map<String, Int>>>, response: Response<ApiResponse<Map<String, Int>>>) {
                if (response.isSuccessful && response.body()?.success == true) { message(getString(R.string.history_cleared)); render(emptyList()) }
                else message(response.body()?.message ?: getString(R.string.connection_error))
            }
            override fun onFailure(call: Call<ApiResponse<Map<String, Int>>>, t: Throwable) = message(getString(R.string.connection_error))
        })
    }
    private fun message(value: String) = Toast.makeText(this, value, Toast.LENGTH_LONG).show()
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
