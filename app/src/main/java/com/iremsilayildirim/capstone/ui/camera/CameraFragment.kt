package com.iremsilayildirim.capstone.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.iremsilayildirim.capstone.R
import com.iremsilayildirim.capstone.databinding.FragmentCameraBinding
import com.iremsilayildirim.capstone.utils.DrugNameExtractor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File

class CameraFragment : Fragment(R.layout.fragment_camera) {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var previewView: PreviewView
    private lateinit var recognizeTextButton: Button
    private lateinit var ocrResultTextView: TextView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var imageCapture: ImageCapture

    private val cameraPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            startCamera()
        } else {
            Log.e("CameraFragment", "Camera permission denied!")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCameraBinding.bind(view)

        previewView = binding.previewView
        ocrResultTextView = binding.ocrResultTextView
        loadingIndicator = binding.loadingIndicator
        recognizeTextButton = binding.recognizeTextButton

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            cameraPermissionRequest.launch(Manifest.permission.CAMERA)
        }

        recognizeTextButton.setOnClickListener {
            captureImageAndAnalyze()
            // Butonu gizle ve metin tanı işlemine başla
            recognizeTextButton.visibility = View.GONE
            recognizeTextButton.text = "Recognizing..."
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

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraFragment", "Camera start failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun captureImageAndAnalyze() {
        val file = File(requireContext().externalCacheDir, "photo.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Log.d("CameraFragment", "Image saved to ${file.absolutePath}")
                displayCapturedImage(file)
                analyzeImage(file)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraFragment", "Image capture failed: ${exception.message}")
            }
        })
    }

    private fun displayCapturedImage(file: File) {
        // Fotoğrafı PreviewView içinde göstermek için bir ImageView ekleyebilirsiniz
        val imageBitmap = BitmapFactory.decodeFile(file.absolutePath)
        previewView.visibility = View.INVISIBLE  // PreviewView'ı gizleyelim
        val capturedImageView = ImageView(requireContext())
        capturedImageView.setImageBitmap(imageBitmap)
        binding.root.addView(capturedImageView)

        // Metin tanı butonunu ekranın üst kısmına taşıyalım
        val params = recognizeTextButton.layoutParams as ConstraintLayout.LayoutParams
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        recognizeTextButton.layoutParams = params
    }

    private fun fetchDrugInfo(drugName: String) {
        val url = "https://api.fda.gov/drug/label.json?search=brand_name:\"$drugName\"&limit=1"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.e("fetchDrugInfo", "Failed to fetch drug info: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val jsonObject = JSONObject(responseData)
                    val results = jsonObject.optJSONArray("results")
                    if (results != null && results.length() > 0) {
                        val drugDetails = results.getJSONObject(0)
                        val drugDescription = drugDetails.optString("description", "No description available")
                        Log.d("fetchDrugInfo", "Drug description: $drugDescription")

                        // UI'da açıklamayı göstermek
                        activity?.runOnUiThread {
                            ocrResultTextView.text = drugDescription
                        }

                    } else {
                        Log.d("fetchDrugInfo", "No results found for $drugName")
                    }
                } else {
                    Log.e("fetchDrugInfo", "Failed to fetch data: ${response.message}")
                }
            }
        })
    }


    @OptIn(ExperimentalGetImage::class)
    private fun analyzeImage(file: File) {
        val image = InputImage.fromFilePath(requireContext(), Uri.fromFile(file))

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        showLoading(true)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val recognizedText = visionText.text
                val drugName = DrugNameExtractor.extractDrugName(recognizedText)
                if (drugName != null) {
                    // İlaç adı bulundu, OpenFDA API'ye gönderebilirsiniz
                    fetchDrugInfo(drugName)
                } else {
                    Log.d("CameraFragment", "No drug name found.")
                }
                showRecognizedText(visionText)
                showLoading(false)
            }
            .addOnFailureListener { e ->
                Log.e("CameraFragment", "Text recognition failed: $e")
                showLoading(false)
            }
    }

    private fun showRecognizedText(text: Text) {
        val recognizedText = text.text.ifEmpty { "No text recognized" }
        ocrResultTextView.text = recognizedText
        binding.ocrResultCard.visibility = View.VISIBLE  // CardView görünür hale gelecek
    }

    private fun showLoading(isLoading: Boolean) {
        loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
