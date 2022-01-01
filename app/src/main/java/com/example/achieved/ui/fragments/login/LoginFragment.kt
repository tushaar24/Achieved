package com.example.achieved.ui.fragments.login

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.achieved.R
import com.example.achieved.databinding.FragmentLoginBinding
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup the text in view asking user to register if they don't have account
        setAskToRegister()

        // handling click events
        binding.apply {

            btnLogin.setOnClickListener {

                val phoneNumber = "+91${binding.etMobileNumber.text.toString().trim()}"

                if (viewModel.isPhoneNumberCorrect(phoneNumber)) {
                    binding.otpView.visibility = View.VISIBLE
                    binding.otpView.requestFocus()
                    binding.btnLogin.visibility = View.GONE
                    binding.btnVerifyOtp.visibility = View.VISIBLE
                    val mCallback = viewModel.getCallback()
                    val phoneAuthOptions = getOptions(mCallback, phoneNumber)
                    viewModel.authentication(phoneAuthOptions)

                } else {
                    Toast.makeText(context, "Please enter correct number.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            btnVerifyOtp.setOnClickListener {
                val smsCode = binding.otpView.text.toString()
                viewModel.verifyOtp(smsCode)
            }
        }

        // observing all livedata
        viewModel.apply {

            getSmsCode.observe(requireActivity(), { smsCode ->
                binding.otpView.setText(smsCode)
            })

            isSignedInSuccessful.observe(requireActivity(), { isSignedInSuccessful ->
                if (isSignedInSuccessful) {
                    Toast.makeText(requireContext(), "Signed In Successful", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    if (viewModel.authException != null) {
                        Toast.makeText(requireContext(), "Invalid Otp", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error while signing in",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            })

            isRegisterTextClicked.observe(requireActivity(), {
                TODO("implement go to register screen")
            })
        }
    }

    private fun getOptions(
        callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks,
        phoneNumber: String,
    ): PhoneAuthOptions {
        return PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callback)
            .build()
    }

    private fun setAskToRegister() {
        val text = getString(R.string.text_to_register)
        binding.tvTextToRegister.movementMethod = LinkMovementMethod.getInstance()
        val spannableText = viewModel.getAskToRegisterSpannableString(text)
        binding.tvTextToRegister.text = spannableText
    }

}