package com.gri.urbanfitness

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private val ZXING_CAMERA_PERMISSION = 1
    private var mScannerView: ZXingScannerView? = null
    private var urlText: EditText? = null
    private var dniText: EditText? = null

    val MY_PREFS_NAME = "MyPrefsFile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mScannerView = ZXingScannerView(this)
        //setContentView(mScannerView)
        val rl = findViewById<View>(R.id.scannLayout) as RelativeLayout
        rl.addView(mScannerView)
        //mScannerView?.setResultHandler(this)
        mScannerView?.setSoundEffectsEnabled(true)
        mScannerView?.setAutoFocus(true)

        urlText = findViewById(R.id.urlText)
        dniText = findViewById(R.id.dniText)

        val prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        val name = prefs.getString("dni", "11222333")
        dniText?.setText(name)

        val okBtn = findViewById<Button>(R.id.button)
        okBtn.setOnClickListener(View.OnClickListener {
            sendRequest()
        })

        val scannBtn = findViewById<Button>(R.id.button2)
        scannBtn.setOnClickListener(View.OnClickListener {
            mScannerView?.setResultHandler(this)
            mScannerView?.startCamera()
        })
    }

    fun sendRequest() {
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute(Runnable {
            sendPostRequest()
            handler.post(Runnable {
                Toast.makeText(this, "Gracias por venir.", Toast.LENGTH_LONG).show()
            })
        })
    }
    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA), ZXING_CAMERA_PERMISSION)
        } else {
            mScannerView?.setResultHandler(this) // Register ourselves as a handler for scan results.
            mScannerView?.startCamera() // Start camera on resume
        }
    }

    override fun onPause() {
        super.onPause()
        mScannerView?.stopCamera() // Stop camera on pause
    }
    override fun handleResult(rawResult: Result?) {
        mScannerView?.stopCamera()
        rawResult?.getText()?.let {
            Log.v("tag", it)
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            if (it.contains("gym", ignoreCase = true)) {
                sendRequest()
            }
        }; // Prints scan results
        Log.v("tag", rawResult?.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            ZXING_CAMERA_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Toast.makeText(this, "Muchas gracias, escanee el código QR.", Toast.LENGTH_LONG).show()
                } else {
                    // permission denied
                    Toast.makeText(this, "Para utilizar esta aplicación debe dar acceso a la cápara para poder leer el código QR.", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    fun sendPostRequest() {
        val editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit()
        editor.putString("dni", dniText?.text.toString())
        editor.apply()

        val url = urlText?.text.toString() + dniText?.text.toString()
        val mURL = URL(url)

        try {
            with(mURL.openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                println("URL : $url")
                println("Response Code : $responseCode")

                BufferedReader(InputStreamReader(inputStream)).use {
                    val response = StringBuffer()

                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    println("Response : $response")
                }
            }
        } catch (ex: Exception) {
            this.runOnUiThread(Runnable {
                Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()
            })
        }
    }
}

