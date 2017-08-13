package br.com.diop.livrosfirebase.views

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.format.DateFormat
import android.util.Log
import android.view.View

import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

import java.io.File
import java.util.Date

import br.com.diop.livrosfirebase.R
import br.com.diop.livrosfirebase.databinding.ActivityBookRegistryBinding
import br.com.diop.livrosfirebase.domain.Book
import br.com.diop.livrosfirebase.utils.FirebaseConstants

class BookRegistryActivity : AppCompatActivity() {
    private var mBookBinding: ActivityBookRegistryBinding? = null
    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null
    private var booksRef: DatabaseReference? = null
    private var storageRef: StorageReference? = null
    private var mAuth: FirebaseAuth? = null

    private var isNewBook: Boolean = false
    private var path: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // para fazer a vm ignorar o file URI exposure. em sdk acima do 24 que exige o uso de FileProvider
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        mBookBinding = DataBindingUtil.setContentView<ActivityBookRegistryBinding>(this, R.layout.activity_book_registry)

        val book = intent.getSerializableExtra(BOOK_EXTRA) as Book
        isNewBook = book == null
        if (isNewBook) {
            mBookBinding!!.book = Book()
        } else {
            mBookBinding!!.book = book
        }
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage!!.getReferenceFromUrl(FirebaseConstants.FIREBASE_STORAGE_URL).child("covers")
        booksRef = database!!.getReference("books").child(mAuth!!.currentUser!!.uid)
    }

    fun saveRegister(view: View) {

        if (path != null && path!!.exists()) {
            val coverRef = storageRef!!.child(path!!.name)

            val uploadTask = coverRef.putFile(Uri.fromFile(path))
            uploadTask.addOnFailureListener { Log.d(TAG, "onFailureUpload()") }.addOnSuccessListener { taskSnapshot ->
                val imageUrl = taskSnapshot.downloadUrl
                mBookBinding!!.book.cover = imageUrl!!.toString()
                saveBook()
            }
        } else {
            saveBook()
        }

    }

    private fun saveBook() {
        val book = mBookBinding!!.book
        if (isNewBook) {
            booksRef!!.push().setValue(book)
        } else {
            booksRef!!.child(book.id).setValue(book)
        }
        finish()
    }

    fun takePicture(view: View) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            val photoName = DateFormat.format("yyyy-MM-dd_hhmmss", Date()).toString()

            path = File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES),
                    photoName)

            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(path)), RC_CAMERA)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            Glide.with(this).load(path!!.absolutePath).into(mBookBinding!!.ivBookRegistry)
        }
    }

    companion object {

        val BOOK_EXTRA = "book"
        private val RC_CAMERA = 0
        private val TAG = "BookRegistryActivity"
    }

}
