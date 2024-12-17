package com.iremsilayildirim.capstone.ui.register

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.iremsilayildirim.capstone.R
import com.iremsilayildirim.capstone.data.dao.UserDao
import com.iremsilayildirim.capstone.data.database.DatabaseBuilder
import com.iremsilayildirim.capstone.data.model.User
import com.iremsilayildirim.capstone.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var userDao: UserDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        // Veritabanı ve DAO başlatma
        val database = DatabaseBuilder.getInstance(requireContext())
        userDao = database.userDao()

        setupListeners()

        binding.registerButton.setOnClickListener {
            if (isInternetAvailable()) {
                val firstName = binding.firstNameEditText.text.toString().trim()
                val lastName = binding.lastNameEditText.text.toString().trim()
                val age = binding.ageEditText.text.toString().trim()
                val email = binding.emailEditText.text.toString().trim()
                val password = binding.passwordEditText.text.toString().trim()
                val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

                if (firstName.isEmpty() || lastName.isEmpty() || age.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                } else if (password != confirmPassword) {
                    binding.passwordMismatchErrorText.visibility = View.VISIBLE
                } else {
                    binding.passwordMismatchErrorText.visibility = View.GONE
                    registerUser(firstName, lastName, age, email, password)
                }
            } else {
                Toast.makeText(requireContext(), "No internet connection. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.backToLoginText.setOnClickListener {
            if (isInternetAvailable()) {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            } else {
                Toast.makeText(requireContext(), "No internet connection. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }

    private fun registerUser(firstName: String, lastName: String, age: String, email: String, password: String) {
        lifecycleScope.launch {
            val existingUser = userDao.getUserByEmailAndPassword(email, password)
            if (existingUser == null) {
                // Kullanıcıyı veritabanına ekle
                val user = User(firstName = firstName, lastName = lastName, age = age, email = email, password = password)
                userDao.insert(user)
                Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                // Kayıt sonrası yapılacak işlemler (örneğin, giriş sayfasına yönlendirme)
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            } else {
                Toast.makeText(requireContext(), "This email is already registered", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkPasswordStrength(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = binding.passwordEditText.text.toString()
                if (password != s.toString()) {
                    binding.passwordMismatchErrorText.visibility = View.VISIBLE
                } else {
                    binding.passwordMismatchErrorText.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun checkPasswordStrength(password: String) {
        val numReg = Regex(".*[0-9].*")
        val letterReg = Regex(".*[A-Za-z].*")
        val strength: Double = when {
            password.length >= 8 && numReg.containsMatchIn(password) && letterReg.containsMatchIn(password) -> 1.0
            password.length >= 8 -> 0.75
            password.length >= 6 -> 0.5
            password.isNotEmpty() -> 0.25
            else -> 0.0
        }

        val progressDrawable = when (strength) {
            1.0 -> R.drawable.password_strength_bar_great
            0.75 -> R.drawable.password_strength_bar_strong
            0.5 -> R.drawable.password_strength_bar_acceptable
            0.25 -> R.drawable.password_strength_bar_short
            else -> R.drawable.password_strength_bar_short
        }
        binding.passwordStrengthIndicator.progressDrawable = requireContext().getDrawable(progressDrawable)

        binding.passwordStrengthIndicator.progress = (strength * 100).toInt()
        binding.passwordStrengthText.text = when (strength) {
            1.0 -> "Your password is great"
            0.75 -> "Your password is strong"
            0.5 -> "Your password is acceptable but not strong"
            0.25 -> "Your password is too short"
            else -> ""
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
