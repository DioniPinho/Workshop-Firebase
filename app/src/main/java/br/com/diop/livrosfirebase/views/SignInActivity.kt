package br.com.diop.livrosfirebase.views

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import br.com.diop.livrosfirebase.R
import br.com.diop.livrosfirebase.utils.FirebaseConstants
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignInActivity : AppCompatActivity() {
    private var mGoogleAPiClient: GoogleApiClient? = null
    private var mFirebaseAuth: FirebaseAuth? = null
    private val mAuthStateListener: FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mFirebaseAuth = FirebaseAuth.getInstance()
        initGoogleSignIn()
    }

    private fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(FirebaseConstants.GOOGLE_SIGN_IN_KEY)
                .requestEmail()
                .build()

        mGoogleAPiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this) { Toast.makeText(this@SignInActivity, getString(R.string.error_google_signin), Toast.LENGTH_LONG).show() }
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    private fun signIn() {
        val signIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleAPiClient)
        startActivityForResult(signIntent, RC_GOOGLE_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.id!!)

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mFirebaseAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "signInWithCredential:onComplete" + task.isSuccessful)
                    if (!task.isSuccessful) {
                        Log.d(TAG, "signInWithCredential", task.exception)
                        Toast.makeText(this@SignInActivity, "Authentication failed.", Toast.LENGTH_LONG).show()
                    } else {
                        finish()
                        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                    }
                }
    }

    fun clickGoogleSignIn(view: View) {
        signIn()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                Toast.makeText(this@SignInActivity, getString(R.string.error_google_signin), Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {

        private val RC_GOOGLE_SIGN_IN = 1
        private val TAG = "SignInActivity"
    }
}
