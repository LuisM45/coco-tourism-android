package edu.epn.wachiteam.moviles.coco_tourism

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import edu.epn.wachiteam.moviles.coco_tourism.databinding.FragmentRegisterBinding
import edu.epn.wachiteam.moviles.coco_tourism.services.FireUser
import org.chromium.base.Log

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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

        fun requestPermissions(){

            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))

        }
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        with(binding){
            btnRegister.setOnClickListener {view->
                if(binding.etEmail.text.isNullOrBlank()){
                    tvExceptions.text = "El nombre de usuario no puede quedar en blanco"
                    return@setOnClickListener
                }
                if(binding.etPassword.text.isNullOrBlank()){
                    tvExceptions.text = "La contraseña no puede quedar en blanco"
                    return@setOnClickListener
                }
                if(binding.etPassword.text.toString() != binding.etPasswordToo.text.toString()){
                    tvExceptions.text = "Las contraseñas no coinciden"
                    return@setOnClickListener
                }
                val username =  binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()
                FireUser.createUserWithEmailAndPassword(username,password)
                    .then({
                        requestPermissions()
                    },{
                        Log.e("Cookie","e33")
                        tvExceptions.text = it.localizedMessage.toString()
                    })
            }
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}