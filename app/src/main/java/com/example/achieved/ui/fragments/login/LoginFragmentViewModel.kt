package com.example.achieved.ui.fragments.login

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*

class LoginFragmentViewModel : ViewModel() {

    private val _getSmsCode = MutableLiveData<String>()
    val getSmsCode: LiveData<String> get() = _getSmsCode
    private lateinit var token: String
    private val mAuth = FirebaseAuth.getInstance()
    private val _isSignedInSuccessful = MutableLiveData<Boolean>()
    val isSignedInSuccessful: LiveData<Boolean> get() = _isSignedInSuccessful
    private var _authException: Exception? = Exception()
    var authException: Exception? = _authException
        private set
    private val _isRegisterTextClicked = MutableLiveData<Boolean>()
    val isRegisterTextClicked get() = _isRegisterTextClicked

    fun isPhoneNumberCorrect(number: String): Boolean {
        if (number.length == 13) {
            return true
        }
        return false
    }

    fun authentication(phoneAuthOptions: PhoneAuthOptions) {
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
    }

    fun getCallback(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                val smsCode = credential.smsCode
                smsCode?.let {
                    _getSmsCode.value = it
                    verifyOtp(smsCode)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
            }

            override fun onCodeSent(
                verificationId: String,
                resendToken: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, resendToken)
                token = verificationId
            }
        }

        return callback
    }

    fun verifyOtp(smsCode: String) {
        val credentials = PhoneAuthProvider.getCredential(token, smsCode)
        signInWithPhoneAuthCredential(credentials)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isSignedInSuccessful.value = true
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        _isSignedInSuccessful.value = false
                        task.exception?.let {
                            _authException = it
                        }
                    } else {
                        _isSignedInSuccessful.value = false
                        _authException = null
                    }
                }
            }
    }

    fun getAskToRegisterSpannableString(text: String): SpannableString {
        val spannableString = SpannableString(text)
        val clickRegister = object : ClickableSpan() {
            override fun onClick(view: View) {
                _isRegisterTextClicked.value = true
            }
        }
        spannableString.setSpan(
            clickRegister,
            23,
            spannableString.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        return spannableString
    }

}