package com.iremsilayildirim.capstone.ui.mainpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.camera.view.PreviewView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.iremsilayildirim.capstone.R

class MainPageFragment : Fragment() {

    private lateinit var previewView: PreviewView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_page, container, false)

        // Kamera butonuna tıklama
        val cameraButton: View = view.findViewById(R.id.ic_camera)
        cameraButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainPageFragment_to_cameraFragment)
        }

        return view
    }


    private lateinit var recognizedTextView: TextView

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            recognizedTextView = view.findViewById(R.id.recognizedTextView)

            val recognizedText = arguments?.getString("recognizedText") // OCR metni
            recognizedTextView.text = recognizedText
        }


}
