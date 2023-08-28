package edu.epn.wachiteam.moviles.coco_tourism

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import edu.epn.wachiteam.moviles.coco_tourism.databinding.FragmentLoginBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }

        binding.tvRegister.setOnClickListener{
            findNavController().navigate(R.id.action_login_to_register)
        }

//        Intent(context,MainActivity::class.java).run(::startActivity)
        login()
//        binding.btnLogin.setOnClickListener { login() }
    }

    fun login(){
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        loginAuthResponse.launch(signInIntent)
    }

    fun afterSuccesfulLogin(res:IdpResponse){
        val token = FirebaseAuth.getInstance().currentUser!!.getIdToken(true)
        token.addOnSuccessListener {
            Log.i("Cookie",it.toString())
        }
            .addOnFailureListener { Log.i("Cookie","Login no bueno") }

        Intent(context,MainActivity::class.java).run(::startActivity)
        requireActivity().finish()
    }

    private val loginAuthResponse = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ){
            res: FirebaseAuthUIAuthenticationResult ->
        if(res.resultCode == AppCompatActivity.RESULT_OK){
            if(res.idpResponse != null){
                afterSuccesfulLogin(res.idpResponse!!)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}