package com.safi.mlkitplayground

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ImageAnalyzer(private val languageType : String, private val onAnalyzeFinished:(text : Text) -> Unit) : ImageAnalysis.Analyzer {

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {

        imageProxy.image?.let { mediaImage ->

            val builder = if (languageType == DEVANAGARI) DevanagariTextRecognizerOptions.Builder().build()
            else TextRecognizerOptions.Builder().build()

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val recognizer = TextRecognition.getClient(builder)

            recognizer.process(image).addOnSuccessListener { text ->
                onAnalyzeFinished.invoke(text)
                mediaImage.close()
                imageProxy.close()
            }.addOnFailureListener {
                Log.i("analyze", "error: ${it.message}")
            }
        }

    }

    companion object {
        val DEVANAGARI = "DEVANAGARI"
        val LATIN = "LATIN"
    }
}