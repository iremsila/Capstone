package com.iremsilayildirim.capstone.ui.mainpage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.Preview
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.iremsilayildirim.capstone.R
import com.google.mlkit.vision.text.Text
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainPageFragment : Fragment() {

    private lateinit var previewView: PreviewView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_page, container, false)
        previewView = view.findViewById(R.id.previewView)
        startCamera() // Kamera başlatma işlemi
        return view
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll() // Önceki kamera işlemlerini temizle

                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )

                // Görüntü üzerinde analiz işlemi için ImageAnalysis ekleyebilirsiniz
                val imageAnalysis = ImageAnalysis.Builder().build()
                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(requireContext()),
                    { imageProxy ->
                        processImageProxy(imageProxy) // Görüntü analizi
                    }
                )
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

            } catch (e: Exception) {
                Log.e("MainPageFragment", "Kamera başlatılamadı: ", e)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // Görüntüyü analiz etme işlemi
    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    handleRecognizedText(visionText) // Tanınan metni işleme
                }
                .addOnFailureListener { e ->
                    Log.e("MainPageFragment", "Metin tanıma başarısız: $e")
                }
                .addOnCompleteListener {
                    imageProxy.close() // ImageProxy'yi kapat
                }
        }
    }

    private fun handleRecognizedText(text: Text) {
        val recognizedText = text.text
        Log.d("MainPageFragment", "Tanınan metin: $recognizedText")
        // Burada tanınan metni kullanabilirsiniz (örneğin ilacın adını burada gösterebilirsiniz)
    }
}
