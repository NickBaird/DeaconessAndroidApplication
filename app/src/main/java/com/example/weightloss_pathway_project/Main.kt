package com.example.weightloss_pathway_project

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class Main : AppCompatActivity() {
    private val CHANNEL_ID = "chat_channel_id"
    private val notificationId = 101
    private var notificationSystem : Intent? = null

    private lateinit var myToggle: ActionBarDrawerToggle
    private var currentUser : Client? = null
    private var firebaseUser : FirebaseUser? = null
    private lateinit var colorDatabase: DatabaseReference
    private lateinit var accountDatabase: DatabaseReference
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var nav: NavigationView
    private lateinit var colar : String
    private lateinit var newGoals : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getColor()
        Handler(Looper.getMainLooper()).postDelayed({
            setContentView(R.layout.activity_main)
            initialize()
            loggedIn()
            createNotificationChannel()
            onClick()},1500)

    }

    override fun onStop() {
        super.onStop()
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onBackPressed() {

    }

    override fun onResume() {
        super.onResume()

        //if(notificationSystem != null)
        //   stopService(notificationSystem)
    }

    private fun initialize(){
        accountDatabase = Firebase.database.reference.child("users").child(firebaseUser!!.uid).child("account")
        // link set new goals button
        newGoals = findViewById(R.id.mainSetGoalsBtn)
        // gets id's of UI components for modification
        drawerLayout= findViewById(R.id.mainNavDrawer)
        nav = findViewById(R.id.navView)

        myToggle = ActionBarDrawerToggle(this, drawerLayout, 0, 0)
        drawerLayout.addDrawerListener(myToggle)

        myToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        notificationSystem = Intent(applicationContext, NotificationService::class.java)
        startService(notificationSystem)

        // provides functionality whenever item clicked on navigation bar in main menu
        nav.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.dailyViewGoals -> {
                    Log.e("color", colar)
                    weeklyTabActivity(R.layout.activity_weekly_tab)
                }
                R.id.viewChat -> {
                    chatActivity(R.layout.activity_message)
                }
                R.id.navSettings -> {
                    settingsActivity(R.layout.activity_settings)
                }
                R.id.navContactUs -> {
                    Toast.makeText(this, "Currently Under Construction", Toast.LENGTH_LONG).show()
                    //contactUsActivity(R.layout.activity_fire_base)
                }
                R.id.navAbout -> {
                    aboutActivity(R.layout.activity_about)
                }
                R.id.navHelp -> {
                    Toast.makeText(this, "Currently Under Construction", Toast.LENGTH_LONG).show()
                    //helpActivity(R.layout.activity_help)
                }
                R.id.navSignOut -> {
                    val snackbar: Snackbar = Snackbar
                        .make(findViewById(R.id.navSignOut), "Confirm Sign Out?", Snackbar.LENGTH_LONG)
                        snackbar.setAction("YES"){
                            FirebaseAuth.getInstance().signOut()
                            Toast.makeText(this, "Successfully Logged Out", Toast.LENGTH_LONG).show()
                            loginActivity(R.layout.activity_login)
                            stopService(notificationSystem)
                        }

                    snackbar.show()
                }

            }
            true}
    }

    private fun onClick(){
        // create action when newGoals button pressed
        newGoals.setOnClickListener{
            setWeeklyActivity(R.layout.activity_created_goal_weekly)
        }
    }

    // Intent that will open login activity when activated
    private fun loginActivity(view: Int){
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }

    // Intent that will open about activity when activated
    private fun aboutActivity(view: Int){
        val intent = Intent(this, About::class.java)
        intent.putExtra("color", colar)
        startActivity(intent)
    }

    // Intent that will open weekly activity when activated
    private fun setWeeklyActivity(view: Int){
        val intent = Intent(this, SelectedGoalWeekly::class.java)
        intent.putExtra("color", colar)
        startActivity(intent)
    }

    // Intent that will open contact us activity when activated
    private fun contactUsActivity(view: Int){
        val intent = Intent(this, Firebase::class.java)
        intent.putExtra("color", colar)
        startActivity(intent)
    }

    // Intent that will open WeeklyTab activity when activated
    private fun weeklyTabActivity(view: Int){
        val intent = Intent(this, WeeklyTab::class.java)
        intent.putExtra("color", colar)
        startActivity(intent)
    }

    // Intent that will open Chat activity when activated
    private fun chatActivity(view: Int) {
        val intent = Intent(this, Message::class.java)
        intent.putExtra("color", colar)
        startActivity(intent)
    }

    // Intent that will open login activity when activated
    private fun settingsActivity(view: Int){
        val intent = Intent(this, Settings::class.java)
        intent.putExtra("color", colar)
        startActivity(intent)
    }

    // Intent that will open login activity when activated
    private fun helpActivity(view: Int){
        val intent = Intent(this, Help::class.java)
        intent.putExtra("color", colar)
        startActivity(intent)
    }


    // important for navigation UI purposes
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (myToggle.onOptionsItemSelected(item)) {return true}
        return super.onOptionsItemSelected(item)
    }

    private fun loggedIn(){
        // Access Database
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                currentUser = dataSnapshot.getValue<Client?>()

                // This value will be dynamic based on the user logged in
                val welcomeName = "Welcome, ${currentUser?.firstname}"

                // Will be dynamic base on current Motivational Quote provided
                val welcomeMessage = findViewById<TextView>(R.id.mainWelcomeMessage)
                welcomeMessage.text = welcomeName
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        accountDatabase.addValueEventListener(postListener)
    }

    fun getColor(){
        // getting access to current user
        firebaseUser = FirebaseAuth.getInstance().currentUser
        colorDatabase = Firebase.database.reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("colorTheme")

        colar = ""

        val postListener2 = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI

                colar = dataSnapshot.getValue<String>()!!
                modifyTheme()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        colorDatabase.addValueEventListener((postListener2))
    }

    private fun modifyTheme(){
        val window = this.window
        val col = ColorChange()
        val c = col.defineThemeColor(colar)
        val color = ColorDrawable(Color.parseColor(c))

        if (colar == "Red"){
            setTheme(R.style.redTheme)
            window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.red)
            supportActionBar?.setBackgroundDrawable(color)
        }
        else if (colar == "Orange"){
            setTheme(R.style.orangeTheme)
            window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.orange)
            supportActionBar?.setBackgroundDrawable(color)
        }
        else if (colar == "Yellow"){
            setTheme(R.style.yellowTheme)
            window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.yellow)
            supportActionBar?.setBackgroundDrawable(color)
        }
        else if (colar == "Green"){
            setTheme(R.style.greenTheme)
            window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.green)
            supportActionBar?.setBackgroundDrawable(color)
        }
        else if (colar == "Blue"){
            setTheme(R.style.blueTheme)
            window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.blue)
            supportActionBar?.setBackgroundDrawable(color)
        }
        else if (colar == "Purple"){
            setTheme(R.style.purpleTheme)
            window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.purple)
            supportActionBar?.setBackgroundDrawable(color)
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name ="Message Receieved"
            val descriptionText = "Chat Message"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun chatNotification() {
        val intent = Intent(this, Message::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.wellness_image)
            .setContentTitle("Example Header")
            .setContentText("Example Description")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }
    }
}