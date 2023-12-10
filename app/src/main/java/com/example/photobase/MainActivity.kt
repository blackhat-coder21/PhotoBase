package com.example.photobase

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import com.example.photobase.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {

    private lateinit var binding :ActivityMainBinding
    private lateinit var storageRef : StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var imageUri :  Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()
        registerClickEvents()
    }

    private fun registerClickEvents(){
        binding.uploadBtn.setOnClickListener{
            uploadImage()
        }

        binding.showAllBtn.setOnClickListener{
            startActivity(Intent(this, ImagesActivity::class.java))
        }

        binding.imageView.setOnClickListener{
            resultLauncher.launch("image/*")
        }
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()){
        imageUri = it
        binding.imageView.setImageURI(it)
        }

    private fun initVars(){
        storageRef = FirebaseStorage.getInstance().reference.child("Images")
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    private fun uploadImage(){
        binding.progressBar.visibility = View.VISIBLE
        storageRef = storageRef.child(System.currentTimeMillis().toString())
        imageUri?.let {
            storageRef.putFile(it).addOnCompleteListener{task->
                if(task.isSuccessful){
                    storageRef.downloadUrl.addOnSuccessListener { uri->
                        val map = HashMap<String, Any>()
                        map["pic"] = uri.toString()

                        firebaseFirestore.collection("images").add(map).addOnCompleteListener{ firestoreTask->
                            if(firestoreTask.isSuccessful){
                                Toast.makeText(this,"Uploaded Successfully", Toast.LENGTH_SHORT).show()
                            }
                            else{
                                Toast.makeText(this,firestoreTask.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                            binding.progressBar.visibility = View.INVISIBLE
                            binding.imageView.setImageResource(R.drawable.vector)
                        }
                    }
                }else{
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.imageView.setImageResource(R.drawable.vector)
                }
            }
        }
    }
}