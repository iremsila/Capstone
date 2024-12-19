package com.iremsilayildirim.capstone.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.iremsilayildirim.capstone.R
import com.iremsilayildirim.capstone.databinding.FragmentCameraBinding

class CameraFragment : Fragment(R.layout.fragment_camera) {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var previewView: PreviewView
    private lateinit var recognizeTextButton: Button
    private lateinit var ocrResultTextView: TextView  // Değişken adı doğru şekilde burada tanımlandı
    private lateinit var loadingIndicator: ProgressBar

    private var imageAnalysis: ImageAnalysis? = null

    private val cameraPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            startCamera()
        } else {
            Log.e("CameraFragment", "Kamera izni verilmedi!")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCameraBinding.bind(view)

        previewView = binding.previewView
        ocrResultTextView = binding.ocrResultTextView // Bu satırda doğru ad kullanılıyor
        loadingIndicator = binding.loadingIndicator
        recognizeTextButton = binding.recognizeTextButton

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            cameraPermissionRequest.launch(Manifest.permission.CAMERA)
        }

        recognizeTextButton.setOnClickListener {
            analyzeCurrentFrame()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            imageAnalysis = ImageAnalysis.Builder().build().apply {
                setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
                    // Şu anda otomatik tanıma yok, kullanıcı tetikleyince yapılacak
                    imageProxy.close()
                }
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraFragment", "Kamera başlatılamadı", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun analyzeCurrentFrame() {
        imageAnalysis?.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
            processImageProxy(imageProxy)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            showLoading(true)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    showRecognizedText(visionText)
                    // 3-5 saniye bekleyip, ardından main sayfaya yönlendirme
                    Handler(Looper.getMainLooper()).postDelayed({
                        showLoading(false)
                        navigateToMainPage(visionText)
                    }, 3000)
                }
                .addOnFailureListener { e ->
                    Log.e("CameraFragment", "Metin tanıma başarısız: $e")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun showRecognizedText(text: Text) {
        ocrResultTextView.text = text.text // Burada ocrResultTextView doğru adla kullanılıyor
    }

    private fun showLoading(isLoading: Boolean) {
        loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToMainPage(visionText: Text) {
        // Ana sayfaya yönlendirme ve metni geçirme
        val action = CameraFragmentDirections.actionCameraFragmentToMainPageFragment(visionText.text)
        findNavController().navigate(action)
    }
}
