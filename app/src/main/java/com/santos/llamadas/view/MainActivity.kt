package com.santos.llamadas.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.santos.llamadas.BuildConfig
import com.santos.llamadas.R
import com.santos.llamadas.databinding.ActivityMainBinding
import com.santos.llamadas.model.PhotoData
import com.santos.llamadas.viewmodel.PhotoAdapter
import java.io.File
import java.io.IOException
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private var photoList: MutableList<PhotoData> = mutableListOf()
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PhotoAdapter
    private lateinit var pahtToContact: String
    private var nameFile: String? = null
    private var numberFile: String? = null
    private lateinit var files: File
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var phoneNumber:String
    private lateinit var nameContact: String


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Llamadas)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        files = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/llamadas/")!!

        if (!files.exists()) files.mkdir()

        getPhotosData()

        initRecyclerView()

        binding.btnAddPhoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (binding.lyNameContact.visibility == View.GONE) {
                binding.lyNameContact.visibility = View.VISIBLE
                binding.lyPhoneNumber.visibility = View.VISIBLE
                binding.etNameContact.setText("")
                binding.etPhoneNumber.setText("")
                binding.btnAddPhoto.text = getString(R.string.make_photo)
            }
            else {
                if (binding.etPhoneNumber.text.toString() == "" || binding.etNameContact.text.toString() == "") {
                    binding.lyNameContact.apply {
                        helperText = getString(R.string.require)
                        setHelperTextColor(getColorStateList(R.color.red))
                    }

                    binding.lyPhoneNumber.apply {
                        helperText = getString(R.string.require)
                        setHelperTextColor(getColorStateList(R.color.red))
                    }
                } else {
                    var fileImage: File? = null
                    try {
                        fileImage = createImage()
                    } catch (exc: IOException) {
                        println("ha ocurrido un error")
                        Log.e("error", exc.toString())
                    }

                    if (fileImage != null) {
                        val photoUri = FileProvider.getUriForFile(
                            this,
                            BuildConfig.APPLICATION_ID + ".fileprovider", fileImage
                        )
                        println("ZZZZZZZZZZZZZZZZZZZZZZZ")
                        println(photoUri)

                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                        startForResult.launch(intent)
                    }
                }
            }
        }
    }


    private fun getPhotosData() {
        if (files.listFiles()?.isNotEmpty() == true) {
            println(">>>>>>")
            println("ENTRANDO AL BUCLE")
            println(">>>>>>")
            for (f in files.listFiles()!!) {
                nameFile = (f.toString()).split("/").lastOrNull()
                numberFile = nameFile.toString().split("_")[0]
                nameFile = nameFile.toString().split("_")[1]

                photoList.add(
                    PhotoData(
                        nameFile!!,
                        numberFile!!,
                        f.toString()
                    )
                )
            }
        }

    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            println("vista visible")
            if (result.resultCode == Activity.RESULT_OK) {
                BitmapFactory.decodeFile(pahtToContact)
                println(pahtToContact)
                photoList.add(
                    0,
                    PhotoData(
                        binding.etNameContact.text.toString(),
                        binding.etPhoneNumber.text.toString(),
                        pahtToContact
                    )
                )

                adapter.notifyItemInserted(0)
                binding.lyNameContact.visibility = View.GONE
                binding.lyPhoneNumber.visibility = View.GONE

            }

        }


    private fun initRecyclerView() {
        adapter = PhotoAdapter(
            photoList,
            { photo -> onItemSelected(photo) },
            { position -> onDeleteListener(position) }
        )
        layoutManager =
            if (getRotation(applicationContext) == "vertical") { //es vertical o portrait.
                GridLayoutManager(this, 2)
            } else { // es horizontal o landscape.
                GridLayoutManager(this, 4)
            }
        binding.rvPhotos.layoutManager = layoutManager
        binding.rvPhotos.adapter = adapter
    }

    private fun onDeleteListener(position: Int) {
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setTitle(getString(R.string.confirm_delete) + " " + photoList[position].namePhoto.replace("."," "))
            setPositiveButton(R.string.yes) { _, _ ->
                File(photoList[position].photo).delete()
                photoList.removeAt(position)
                adapter.notifyItemRemoved(position)
            }
            setNegativeButton(
                R.string.cancel
            ) { _, _ ->
                // User cancelled the dialog
            }
        }
        builder.show()
    }


    private fun onItemSelected(contact: PhotoData) {
        binding.lyPhoneNumber.visibility = View.GONE
        binding.lyNameContact.visibility = View.GONE
        phoneNumber = contact.phoneNumber
        nameContact = contact.namePhoto
        Toast.makeText(this, "llamando a $nameContact", Toast.LENGTH_SHORT).show()
        requestCallPermissions()

        ///
    }

    private fun requestCallPermissions() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) -> {
                call()
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }

    }

    private fun call() {
        startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber")))
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isGranted ->
        if (isGranted){
            call()
        }else{
            Toast.makeText(this,"Necesitas aceptar los permisos para hacer uso de la aplicacion",Toast.LENGTH_LONG).show()
        }
    }

    private fun getRotation(context: Context): String {
        return when ((context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.orientation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> "vertical"
            Surface.ROTATION_90 -> "horizontal"
            else -> "horizontal"
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createImage(): File {
        val name = binding.etNameContact.text.toString().replace(" ",".")
        val contact: String = binding.etPhoneNumber.text.toString() + "_" + name + "_r"
        val contactFile = File.createTempFile(contact, ".jpg", files)
        pahtToContact = contactFile.absolutePath
        return contactFile
    }
}