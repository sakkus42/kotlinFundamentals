package com.example.yemektarifleri

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import java.io.ByteArrayOutputStream
import java.lang.Exception

class AddFood : Fragment(), View.OnClickListener {
    var selelectedImage : Uri? = null
    var selectedBitMap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_food, container, false)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1){
            if (grantResults.size > 0 && grantResults[0] != PackageManager.PERMISSION_DENIED){
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, 2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            selelectedImage = data.data
            try {
                context?.let {
                    if (selelectedImage != null){
                        val source = if(Build.VERSION.SDK_INT >= 28) ImageDecoder.createSource(it.contentResolver, selelectedImage!!) else null
                        selectedBitMap = if(source != null) ImageDecoder.decodeBitmap(source) else MediaStore.Images.Media.getBitmap(it.contentResolver, selelectedImage)
                        view?.findViewById<ImageView>(R.id.imageView)?.setImageBitmap(selectedBitMap)
                    }
                }
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.imageView).setOnClickListener(this)
        view.findViewById<Button>(R.id.save).setOnClickListener(this)
        arguments?.let {
            var loc = AddFoodArgs.fromBundle(it).loc
            if (loc.equals("menu")){
                addFragment(view);
            }else{
                showFragment(view, it)
            }
        }
    }

    private fun showFragment(view: View, it: Bundle){
        view.findViewById<Button>(R.id.save).visibility = View.INVISIBLE
        val selectedId = AddFoodArgs.fromBundle(it).id
        println(selectedId)
        context?.let {
            try {
                val db = it.openOrCreateDatabase("Foods", Context.MODE_PRIVATE, null)
                val cursor = db.rawQuery("SELECT * FROM foods WHERE id = ?", arrayOf(selectedId.toString()))
                val foodNameIndex = cursor.getColumnIndex("foodName")
                val foodRecipeIndex = cursor.getColumnIndex("foodRecipe")
                val imageIndex = cursor.getColumnIndex("image")
                while (cursor.moveToNext()){
                    view.findViewById<EditText>(R.id.foodName).text = cursor.getString(foodNameIndex).toEditable()
                    view.findViewById<EditText>(R.id.foodRecipe).text = cursor.getString(foodRecipeIndex).toEditable()
                    val byteArray = cursor.getBlob(imageIndex)
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    view.findViewById<ImageView>(R.id.imageView).setImageBitmap(bitmap)
                }
                cursor.close()
            } catch (e: Exception){
                e.printStackTrace()
            }
    }

    }

    private fun addFragment(view: View){
        view.findViewById<EditText>(R.id.foodName).text = null
        view.findViewById<EditText>(R.id.foodRecipe).text = null
        view.findViewById<Button>(R.id.save).visibility = View.VISIBLE
        val backgroundImage = BitmapFactory.decodeResource(context?.resources, R.drawable.background)
        view.findViewById<ImageView>(R.id.imageView).setImageBitmap(backgroundImage)
    }

    override fun onClick(p0: View?) {
        when (p0?.id){
            R.id.imageView -> choosenImage()
            R.id.save -> saveFood()
        }
    }

    private fun choosenImage(){
        activity?.let {
            if (ContextCompat.checkSelfPermission(it.applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_DENIED){
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }else {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, 2)
            }
        }
    }

    private fun saveFood(){
        val foodName = view?.findViewById<EditText>(R.id.foodName)?.text.toString()
        val foodRecipe = view?.findViewById<EditText>(R.id.foodRecipe)?.text.toString()
        selectedBitMap?.let {
            val shortenedBitmap = scaleBitMap(selectedBitMap!!, 250)
            val outputStream =ByteArrayOutputStream()
            shortenedBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                val database = context?.openOrCreateDatabase("Foods", Context.MODE_PRIVATE, null)
                database?.execSQL("CREATE TABLE IF NOT EXISTS foods (id INTEGER PRIMARY KEY, foodName VARCHAR, foodRecipe VARCHAR, image BLOB)")
                val sqlString = "INSERT INTO foods (foodName, foodRecipe, image) VALUES (?, ?, ?)"
                val statement = database?.compileStatement(sqlString)
                statement?.let {
                    statement.bindString(1, foodName)
                    statement.bindString(2, foodRecipe)
                    statement.bindBlob(3, byteArray)
                    statement.execute()
                }
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
        val action = AddFoodDirections.actionAddFoodToListFood()
        view?.let {
            Navigation.findNavController(requireView()).navigate(action)
        }
    }

    private fun scaleBitMap(selectedBitMap: Bitmap, maxSize: Int): Bitmap{
        var width = selectedBitMap.width
        var height = selectedBitMap.height
        val bitmapRate: Double = width.toDouble() / height.toDouble()

        if (bitmapRate > 1){
            width = maxSize
            val shortenedHeight = width / bitmapRate
            height = shortenedHeight.toInt()
        } else{
            height = maxSize
            val shortenedWidth = height * bitmapRate
            width = shortenedWidth.toInt()
        }
        return Bitmap.createScaledBitmap(selectedBitMap, width, height, true)
    }

    private fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)
}