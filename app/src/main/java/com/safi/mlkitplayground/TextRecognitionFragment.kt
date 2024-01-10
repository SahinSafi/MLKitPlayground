package com.safi.mlkitplayground

import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.safi.mlkitplayground.databinding.FragmentTextRecognitionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class TextRecognitionFragment : Fragment() {

    private var _binding : FragmentTextRecognitionBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var imageAnalyzer: ImageAnalyzer
    private lateinit var imageCapture: ImageCapture

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTextRecognitionBinding.inflate(layoutInflater, container, false)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))

        imageAnalyzer = ImageAnalyzer(ImageAnalyzer.DEVANAGARI) {
            binding.textView.text = it.text
        }

        binding.analyzeBtn.setOnClickListener {
            takePicture()
        }

        return binding.root
    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        val preview : Preview = Preview.Builder().build()
        binding.cameraPreview.scaleType = PreviewView.ScaleType.FIT_CENTER
        preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        imageCapture = ImageCapture.Builder()
            .setTargetRotation(binding.root.display.rotation)
            .build()

//        val imageAnalysis = ImageAnalysis.Builder()
//            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//            .build()
//
//        imageAnalysis.setAnalyzer(Executors.newFixedThreadPool(1)) { imageProxy ->
//            imageAnalyzer.analyze(imageProxy)
//            imageProxy.close()
//        }

        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageCapture, preview)
    }


    private fun takePicture(){
        imageCapture.takePicture(Executors.newCachedThreadPool(),
            object : ImageCapture.OnImageCapturedCallback() {

                override fun onCaptureStarted() {
                    super.onCaptureStarted()
                    setProgressVisibility(true)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    setProgressVisibility(false)
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    imageAnalyzer.analyze(image)
                    setProgressVisibility(false)
                }

            }
        )
    }

    private fun setProgressVisibility(isProgressing : Boolean){
        CoroutineScope(Dispatchers.Main).launch {
            binding.analyzeBtn.isEnabled = !isProgressing
            binding.progressBar.isVisible = isProgressing
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}