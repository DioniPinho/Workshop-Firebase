package br.com.diop.livrosfirebase.views

import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast

import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import br.com.diop.livrosfirebase.R
import br.com.diop.livrosfirebase.adapters.BookAdapter
import br.com.diop.livrosfirebase.adapters.BookHolder
import br.com.diop.livrosfirebase.databinding.ActivityMainBinding
import br.com.diop.livrosfirebase.domain.Book
import br.com.diop.livrosfirebase.utils.FirebaseConstants

class MainActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mBooksRef: DatabaseReference? = null
    private var storage: FirebaseStorage? = null
    private var mMainBinding: ActivityMainBinding? = null
    private var mAdapter: FirebaseRecyclerAdapter<Book, BookHolder>? = null
    private var storageRef: StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainBinding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.d(TAG, "onAuthStateChanged:signed_in")
                initFirebase()
                initUI()
            } else {
                Log.d(TAG, "onAuthStateChanged:signed_out")
                finish()
                startActivity(Intent(this@MainActivity, SignInActivity::class.java))

            }
        }
    }

    private fun initFirebase() {
        mDatabase = FirebaseDatabase.getInstance()
        mBooksRef = mDatabase!!.getReference("books").child(mFirebaseAuth!!.currentUser!!.uid)
        storage = FirebaseStorage.getInstance()
        storageRef = storage!!.getReferenceFromUrl(FirebaseConstants.FIREBASE_STORAGE_URL)
    }

    private fun initUI() {
        mMainBinding!!.recyclerMain.setHasFixedSize(true)
        mMainBinding!!.recyclerMain.setHasFixedSize(true)
        mMainBinding!!.recyclerMain.layoutManager = LinearLayoutManager(this)

        mAdapter = BookAdapter(mBooksRef, BookAdapter.BookClickListener { book ->
            startActivity(Intent(this@MainActivity, BookRegistryActivity::class.java)
                    .putExtra(BookRegistryActivity.BOOK_EXTRA, book))
        })
        mMainBinding!!.recyclerMain.adapter = mAdapter
        swipeConfig()
    }

    public override fun onStart() {
        super.onStart()
        mFirebaseAuth!!.addAuthStateListener(mAuthStateListener!!)
    }

    public override fun onStop() {
        super.onStop()
        mFirebaseAuth!!.removeAuthStateListener(mAuthStateListener!!)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_sign_out) {
            mFirebaseAuth!!.getInstance().signOut()
        }
        return super.onOptionsItemSelected(item)
    }

    fun newBook(view: View) {
        startActivity(Intent(this, BookRegistryActivity::class.java))
    }

    private fun swipeConfig() {
        val swipe = object : ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position = viewHolder.adapterPosition
                val id = mAdapter!!.getRef(position).key
                val book = mAdapter!!.getItem(position)

                if (book.cover != null) {
                    val uri = Uri.parse(book.cover)
                    val fileName = uri.lastPathSegment

                    val imageCoverRef = storageRef!!.child(fileName)
                    imageCoverRef.delete().addOnSuccessListener { mBooksRef!!.child(id).removeValue() }.addOnFailureListener { Toast.makeText(this@MainActivity, R.string.remove_registry_error, Toast.LENGTH_SHORT).show() }
                } else {
                    mBooksRef!!.child(id).removeValue()
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipe)
        itemTouchHelper.attachToRecyclerView(mMainBinding!!.recyclerMain)
    }

    companion object {

        init {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        }

        private val TAG = "MainActivity"
    }


}
