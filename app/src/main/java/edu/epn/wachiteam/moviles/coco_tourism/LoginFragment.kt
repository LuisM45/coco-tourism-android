package edu.epn.wachiteam.moviles.coco_tourism

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import edu.epn.wachiteam.moviles.coco_tourism.databinding.FragmentLoginBinding
import edu.epn.wachiteam.moviles.coco_tourism.services.FireUser

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                Intent(context,MainActivity::class.java).run(::startActivity)
                requireActivity().finish()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Intent(context,MainActivity::class.java).run(::startActivity)
                requireActivity().finish()
            } else -> {
            requireActivity().finish()
        }
        }
    }

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


//        requestPermissions()
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

    fun requestPermissions(){

        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))

    }

    fun afterSuccesfulLogin(res:IdpResponse){
        val token = FirebaseAuth.getInstance().currentUser!!.getIdToken(true)
        token.addOnSuccessListener {
            Log.i("Cookie",it.toString())
            FireUser.initialize()
        }
            .addOnFailureListener { Log.i("Cookie","Login no bueno") }

        requestPermissions()
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