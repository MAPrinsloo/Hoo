package vc.hoo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vc.hoo.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    lateinit var LoginBinding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    //----------------------------------------------------------------------------------------//
    //OnCreate()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding
        LoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        val LoginView = LoginBinding.root
        //initialize db
        FirebaseApp.initializeApp(this)
        setContentView(LoginView)

        //LoginBinding.tietUsername.setText("pablo")
        //LoginBinding.tietPassword.setText("testing#")

        auth = FirebaseAuth.getInstance()
        overridePendingTransition(
            vc.hoo.R.anim.slide_in_left,
            vc.hoo.R.anim.slide_out_right
        )
        //----------------------------------------------------------------------------------------//
        //Sign Up button clicked
        LoginBinding.btnSignUp.setOnClickListener() {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            overridePendingTransition(
                vc.hoo.R.anim.slide_in_right,
                vc.hoo.R.anim.slide_out_left
            )
        }

        //----------------------------------------------------------------------------------------//
        //Login button clicked
        LoginBinding.btnLogin.setOnClickListener()
        {
            var username = LoginBinding.tietUsername.text.toString()
            var password = LoginBinding.tietPassword.text.toString()

            if (ValidateInputs(username, password, LoginBinding) == true) {
                loginUser(username, password, LoginBinding)
            }
        }
    }
    //----------------------------------------------------------------------------------------//
    //Login method
    private fun loginUser(username: String, password: String, binding: ActivityLoginBinding) {
        binding.tilUsername.error = "";
        binding.tilPassword.error = "";
        val db = FirebaseFirestore.getInstance()
        //Check if there is a directory for the username
        val usersCollection = db.collection(username)
        usersCollection.document("user_details")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    //User document exists
                    val userEmail = documentSnapshot.getString("email") ?: ""
                    auth.signInWithEmailAndPassword(userEmail,password).addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            val sharedPref = getSharedPreferences("username", Context.MODE_PRIVATE)
                            val editor = sharedPref.edit()
                            editor.putString("username", username)
                            editor.apply()
                            val intent = Intent(this, DocumentActivity::class.java)
                            intent.putExtra("username", username)
                            startActivity(intent)
                            overridePendingTransition(
                                vc.hoo.R.anim.slide_in_right,
                                vc.hoo.R.anim.slide_out_left
                            )
                            finish()
                        }
                    }.addOnFailureListener { exception ->
                        binding.tilUsername.error = "Combination incorrect"
                        binding.tilPassword.error = "Combination incorrect"
                    }
                }
                else
                {
                    binding.tilUsername.error = "Combination incorrect"
                    binding.tilPassword.error = "Combination incorrect"
                }
        }
    }

    //----------------------------------------------------------------------------------------//
    //Validates all user input
    private fun ValidateInputs(
        Username: String,
        Password: String,
        Binding: ActivityLoginBinding
    ): Boolean {
        var inputsAreValid = true

        Binding.tilUsername.error = "";
        Binding.tilPassword.error = "";

        if (Username.isEmpty()) {
            Binding.tilUsername.error = "Please enter a username"
            inputsAreValid = false
        }
        if (Password.isEmpty()) {
            Binding.tilPassword.error = "Please enter a password"
            inputsAreValid = false
        }
        return inputsAreValid
    }
    //----------------------------------------------------------------------------------------//
    //Runs animation on new intent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Set custom enter animation when the activity is relaunched
        overridePendingTransition(
            vc.hoo.R.anim.slide_in_left,
            vc.hoo.R.anim.slide_out_right
        )
    }
    //----------------------------------------------------------------------------------------//
    //Disable Backpressing
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {}
}
/*
References

Slide Animation
https://www.geeksforgeeks.org/how-to-add-slide-animation-between-activities-in-android/

Adding icons to list views - list adapter
https://medium.com/@hasperong/custom-listview-using-kotlin-7ceb1caaf3cf

Callback functionality
https://www.baeldung.com/kotlin/callback-functions

Regex
https://www.baeldung.com/kotlin/regular-expressions

Mapbox routing and markers
//https://docs.mapbox.com/android/navigation/examples/render-route-line/
https://docs.mapbox.com/mapbox-gl-js/api/markers/#geolocatecontrol
https://docs.mapbox.com/android/navigation/guides/migrate-to-v2/#core-components-in-v2
https://docs.mapbox.com/android/navigation/guides/turn-by-turn-navigation/route-generation/
https://docs.mapbox.com/android/navigation/guides/get-started/initialization/
https://docs.mapbox.com/android/navigation/guides/get-started/initialization/#create-the-mapboxnavigation-object
https://docs.mapbox.com/android/navigation/guides/ui-components/route-line/
https://docs.mapbox.com/android/navigation/guides/get-started/install/
https://docs.mapbox.com/android/navigation/guides/migrate-to-v2/#navigationmaproute-was-replaced
*/