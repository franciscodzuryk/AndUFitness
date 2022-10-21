package com.gri.urbanfitness

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
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
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute(Runnable {
            sendPostRequest()
            handler.post(Runnable {
                //UI Thread work here
            })
        })
    }

    override fun onPause() {
        super.onPause()
        mScannerView?.stopCamera() // Stop camera on pause
    }
    override fun handleResult(rawResult: Result?) {
        rawResult?.getText()?.let { Log.v("tag", it) }; // Prints scan results
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
        //var reqParam = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(userName, "UTF-8")
        //reqParam += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8")
        val mURL = URL("http://192.168.68.114:8000/api/check/83064310V")

        with(mURL.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "POST"

            //val wr = OutputStreamWriter(getOutputStream());
            //wr.write(reqParam);
            //wr.flush();

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
    }
}

