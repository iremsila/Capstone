package com.iremsilayildirim.capstone.ui.mainpage

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.iremsilayildirim.capstone.R

class MainPageFragment : Fragment() {

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNavView = view.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val fabCamera = view.findViewById<FloatingActionButton>(R.id.fab_camera)

        bottomNavView.visibility = View.VISIBLE
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    // Home işlemi
                    true
                }
                R.id.menu_profile -> {
                    // Profile işlemi
                    true
                }
                R.id.menu_settings -> {
                    // Settings işlemi
                    true
                }
                else -> false
            }
        }

        // Kamera FAB'ı için onClickListener
        fabCamera.setOnClickListener {
            // Kamera izni kontrolü
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            } else {
                openCamera() // İzin verilmişse kamerayı aç
            }
        }

        // Kamera işlemi için launcher oluşturuluyor
        takePictureLauncher = registerForActivityResult(TakePicture()) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Fotoğraf çekildi", Toast.LENGTH_SHORT).show()
                // Burada çekilen fotoğraf ile işlem yapabilirsiniz.
            } else {
                Toast.makeText(requireContext(), "Fotoğraf çekme başarısız", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Kamera işlemi için açma fonksiyonu
    private fun openCamera() {
        val cameraUri: Uri = createImageUri() // Fotoğrafı kaydetmek için URI oluşturuluyor
        takePictureLauncher.launch(cameraUri) // Kamera başlatılıyor
    }

    // Fotoğraf için URI oluşturma metodu
    private fun createImageUri(): Uri {
        // Fotoğrafın kaydedileceği URI'yi buradan oluşturabilirsiniz
        val contentResolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    // İzin sonucu
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera() // İzin verildiyse kamerayı aç
            } else {
                Toast.makeText(requireContext(), "Kamera izni gereklidir", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
