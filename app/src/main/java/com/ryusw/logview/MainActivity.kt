package com.ryusw.logview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.ryusw.logview.api.LogViewApi
import com.ryusw.logview.callback.LogInitCallBackInterface
import com.ryusw.logview.context.LogViewInitContext


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_start_log_view).setOnClickListener {
            LogViewApi(
                this,
                LogViewInitContext.Builder()
                    .setLogFilter(arrayOf("Accessing hidden", "View"))
                    .setLogResultCallBackInterface(object : LogInitCallBackInterface{
                        override fun onSuccess() {
                            Toast.makeText(this@MainActivity, "success", Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailure(errorCode: Int, errorMsg: String?) {
                            Toast.makeText(this@MainActivity, "fail : $errorMsg", Toast.LENGTH_SHORT).show()
                        }

                    }).build()
            ).startLogView()
        }

        findViewById<Button>(R.id.btn_create_log).setOnClickListener {
            Log.d(TAG, "onCreate: log create")
        }

        findViewById<Button>(R.id.btn_exception).setOnClickListener {
            throw Exception("에러")
        }
    }
    companion object{
        private const val TAG = "MainActivity..."
    }
}