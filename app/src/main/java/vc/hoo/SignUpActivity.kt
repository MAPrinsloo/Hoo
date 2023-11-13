package vc.hoo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vc.hoo.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    //binding
    lateinit var SignUpBinding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    //----------------------------------------------------------------------------------------//
    //OnCreate()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SignUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        val SignUpView = SignUpBinding.root
        FirebaseApp.initializeApp(this)
        setContentView(SignUpView)
        auth = FirebaseAuth.getInstance()

        //----------------------------------------------------------------------------------------//
        //back button click
        SignUpBinding.mtSignUp.setOnClickListener()
        {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            );
        }
        //----------------------------------------------------------------------------------------//
        //Create button click
        SignUpBinding.btnCreateAccount.setOnClickListener()
        {
            var email = SignUpBinding.tietEmail.text.toString()
            var username = SignUpBinding.tietUsername.text.toString()
            var password = SignUpBinding.tietPassword.text.toString()
            var conPassword = SignUpBinding.tietConfirmPassword.text.toString()

            if (ValidateInputs(email,username, password, conPassword, SignUpBinding) == true)
            {
                SignUp(email, username, password)
            }
        }
    }

    //----------------------------------------------------------------------------------------//
    //Validate all user inputs
    private fun ValidateInputs(
        Email: String,
        Username: String,
        Password: String,
        ConfirmPassword: String,
        Binding: ActivitySignUpBinding
    ): Boolean {
        var inputsAreValid = true
        val maxUserLength = 16
        val minUserLength = 3
        val minPassLength = 8
        val specialChars = Regex("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+")


        Binding.tilUsername.error = "";
        Binding.tilPassword.error = "";
        Binding.tilConfirmPassword.error = "";

        if (Username.length < minUserLength || Username.length > maxUserLength) {
            Binding.tilUsername.error = "Must be 3-16 characters in length"
            inputsAreValid = false
        }
        if (Password.length < minPassLength) {
            Binding.tilPassword.error = "Must be at least 8 characters long"
            inputsAreValid = false
        }
        if (!Password.contains(specialChars)) {
            Binding.tilPassword.error = "Must contain a special character"
            inputsAreValid = false
        }
        if (Password != ConfirmPassword) {
            Binding.tilConfirmPassword.error = "The passwords entered do not match"
            inputsAreValid = false
        }
        if(Email.length == 0)
        {
            Binding.tilEmail.error = "Please enter an email address"
        }
        return inputsAreValid
    }

    //----------------------------------------------------------------------------------------//
    //Capture Method
    private fun CreateAccount(
        Username: String,
        Email: String,
        binding: ActivitySignUpBinding)
    {
        val least_spotted: String = ""
        val least_spotted_num: Long = 0
        val most_spotted: String = ""
        val most_spotted_num: Long = 0
        val max_distance: Int = 2
        val metric: Boolean = true
        val total_spotted: Int = 0
        val db = FirebaseFirestore.getInstance()

        //Firestore path.
        val usernameColRef = db.collection(Username)
        val userDocRef = usernameColRef.document("user_details")
        usernameColRef.get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                //Create user if unique
                val userData = hashMapOf(
                    "least_spotted" to least_spotted,
                    "least_spotted_num" to least_spotted_num,
                    "most_spotted" to most_spotted,
                    "most_spotted_num" to most_spotted_num,
                    "total_spotted" to total_spotted,
                    "metric" to metric,
                    "max_distance" to max_distance,
                    "email" to Email
                )
                userDocRef.set(userData)
            } else {
                binding.tilUsername.error = "'$Username' has already been taken"
            }
        }
    }
    //----------------------------------------------------------------------------------------//
    //
    private fun SignUp(email: String,username: String,password: String)
    {
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                CreateAccount(username, email, SignUpBinding)
                val sharedPref = getSharedPreferences("username", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                val intent = Intent(this, DocumentActivity::class.java)
                editor.putString("username", username)
                editor.apply()
                intent.putExtra("username", username)
                overridePendingTransition(
                    vc.hoo.R.anim.slide_in_right,
                    vc.hoo.R.anim.slide_out_left
                )
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception ->
            SignUpBinding.tilEmail.error = "Email already in use."
        }
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