package com.p47.longnote

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 1
    private val STORAGE_PERMISSION_REQUEST_CODE = 1
    private lateinit var drawView: DrawView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawView = findViewById(R.id.drawView)
        drawView.bringToFront();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

        val clearButton = findViewById<ImageButton>(R.id.clearButton)
        val saveButton = findViewById<ImageButton>(R.id.saveButton)
        val undoButton = findViewById<ImageButton>(R.id.undoButton)
        val redoButton = findViewById<ImageButton>(R.id.redoButton)
        val eraserButton = findViewById<ImageButton>(R.id.eraserButton)


        clearButton.setOnClickListener {
            drawView.clearCanvas()
        }

        saveButton.setOnClickListener {
            saveDrawing()
        }

        undoButton.setOnClickListener {
            drawView.undo()
        }

        redoButton.setOnClickListener {
            drawView.redo()
        }

        eraserButton.setOnClickListener {
            // Toggle eraser mode on/off in your DrawView\
            if (!(drawView.currentColor == drawView.eraserInd)){
                eraserButton.setBackgroundColor(Color.LTGRAY);
                /*
                val text = "Eraser ON"
                val duration = Toast.LENGTH_SHORT

                val toast = Toast.makeText(applicationContext, text, duration)
                toast.show()
                 */
                drawView.setColor(drawView.eraserInd)
            }
            else {
                eraserButton.setBackgroundColor(Color.TRANSPARENT)
                /*
                val text = "Eraser OFF"
                val duration = Toast.LENGTH_SHORT

                val toast = Toast.makeText(applicationContext, text, duration)
                toast.show()
                 */
                drawView.setColor(0)
            }

        }

    }



    private fun saveDrawing() {
        val drawing = drawView.getDrawingBitmap()
        val width = drawing.width
        val height = drawing.height
        val whiteBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        whiteBitmap.eraseColor(ContextCompat.getColor(this, android.R.color.white))
        val canvas = Canvas(whiteBitmap)
        canvas.drawBitmap(drawing, 0f, 0f, null)

        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission()
            return
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Drawing_$timeStamp.png"

        val picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val file = File(picturesDirectory, fileName)

        try {
            val fos = FileOutputStream(file)
            whiteBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
            Toast.makeText(this, "Drawing saved!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving drawing", Toast.LENGTH_SHORT).show()
        }
    }


    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            // Explain why the app needs this permission and prompt the user to grant it
            AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("This permission is needed to save the drawing to the device's external storage.")
                .setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION
                    )
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveDrawing()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
