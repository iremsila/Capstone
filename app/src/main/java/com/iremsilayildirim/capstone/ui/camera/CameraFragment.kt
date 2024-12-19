package com.iremsilayildirim.capstone.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.iremsilayildirim.capstone.R

class CameraFragment : Fragment(R.layout.fragment_camera) {

    private lateinit var surfaceView: SurfaceView
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null


    private val cameraPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            view?.let { startCamera(it.findViewById(R.id.previewView)) }
        } else {
            // İzin verilmediyse, kullanıcıya uyarı gösterebilirsiniz.
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        surfaceView = view.findViewById(R.id.surfaceView)
        val previewView = view.findViewById<PreviewView>(R.id.previewView) // Correct initialization

        // SurfaceView holder setup (if still needed)
        val holder: SurfaceHolder = surfaceView.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    startCamera(view.findViewById(R.id.previewView)) // Pass previewView
                } else {
                    cameraPermissionRequest.launch(Manifest.permission.CAMERA)
                }
            }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraCaptureSession?.close()
                cameraDevice?.close()
            }
        })
    }


    private fun startCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll() // Clean up previous camera usage

                val imageAnalysis = ImageAnalysis.Builder().build().apply {
                    setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
                        processImageProxy(imageProxy) // Analyze image
                    }
                }

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                Log.e("CameraFragment", "Failed to start camera: ", e)
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

    override fun onDestroyView() {
        super.onDestroyView()
        cameraCaptureSession?.close()
        cameraDevice?.close()
    }
}
