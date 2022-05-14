package com.example.weightloss_pathway_project

import android.app.ActivityManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.text.LineBreaker
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import androidx.core.view.setPadding
import com.example.weightloss_pathway_project.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.protobuf.Value
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Message : AppCompatActivity() {

    private lateinit var send : Button
    private lateinit var messageText : EditText
    private lateinit var messageDisplay : LinearLayout
    private lateinit var messageTextBackground : ConstraintLayout
    private lateinit var messageBottomPanel : ConstraintLayout
    private lateinit var messagesScroll : ScrollView
    private var firebaseUser : FirebaseUser? = null
    private var notificationSystem : Intent? = null
    private lateinit var colar : String
    private lateinit var colorDatabase: DatabaseReference
    private var messagesFromUser : ArrayList<Pair<Long, String>>? = null
    private var messagesToUser : HashMap<String, ArrayList<Pair<Long, String>>>? = null
    private var allMessages : HashMap<String, HashMap<Long, String>>? = null
    private var everyMessage : HashMap<Long, String>? = null
    private var coachNames : HashMap<String, String>? = null
    private var coachListener : ValueEventListener? = null
    private var userMessagesListener : ValueEventListener? = null
    private var coachMessagesListener : ValueEventListener? = null
    private lateinit var userMessages : DatabaseReference
    private lateinit var coaches : DatabaseReference
    private lateinit var allCoaches : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getColor()
        Handler(Looper.getMainLooper()).postDelayed({
            setContentView(R.layout.activity_message)
            initialize()
            loadMessages()
            onClick()
        }, 500)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        intent = Intent(this, Main::class.java)
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        allCoaches.removeEventListener(coachListener as ValueEventListener)
        userMessages.removeEventListener(userMessagesListener as ValueEventListener)
        coaches.removeEventListener(coachMessagesListener as ValueEventListener)
    }

    private fun initialize(){
        send = findViewById(R.id.sendMessage)
        messageText= findViewById(R.id.messageText)
        messageDisplay = findViewById(R.id.messages)
        messageTextBackground = findViewById(R.id.messageTextBackground)
        messageBottomPanel = findViewById(R.id.messageBottomPanel)
        messagesScroll = findViewById(R.id.messagesScroll)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        messagesFromUser = ArrayList()
        messagesToUser = HashMap()
        allMessages = HashMap()
        everyMessage = HashMap()
        coachNames = HashMap()
        userMessages = Firebase.database.reference.child("users").child(firebaseUser!!.uid).child("messages")
        coaches = Firebase.database.reference.child("users").child(firebaseUser!!.uid).child("coaches")
        allCoaches = Firebase.database.reference.child("coaches")
        notificationSystem = Intent(applicationContext, NotificationService::class.java)
    }

    private fun loadMessages() {


        coachListener = object : ValueEventListener {
            override fun onDataChange(coaches : DataSnapshot) {
                coaches.children.forEach { coach ->
                    coachNames?.set(coach.key.toString(), coach.child("display").getValue() as String)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) { Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException()) }
        }
        allCoaches.addValueEventListener(coachListener as ValueEventListener)

        userMessagesListener = object : ValueEventListener {
            override fun onDataChange(messages: DataSnapshot) {
                allMessages?.set("user", HashMap())
                messages.children.forEach { message ->
                    message.key?.let { allMessages?.get("user")?.set(it.toLong(), message.getValue().toString()) }
                    message.key?.let { everyMessage?.set(it.toLong(), message.getValue().toString()) }
                }
                refreshChatMessages()
            }
            override fun onCancelled(databaseError: DatabaseError) { Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException()) }
        }
        userMessages.addValueEventListener(userMessagesListener as ValueEventListener)

         coachMessagesListener = object : ValueEventListener {
            override fun onDataChange(messages: DataSnapshot) {
                messages.children.forEach { coach ->

                    messagesToUser?.set(coach.key.toString(), ArrayList())
                    allMessages?.set(coach.key.toString(), HashMap())
                    coach.children.forEach { message ->
                        message.key?.let { allMessages?.get(coach.key.toString())?.set(it.toLong(), message.getValue().toString()) }
                        message.key?.let { everyMessage?.set(it.toLong(), message.getValue().toString())
                        }
                    }
                    refreshChatMessages()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) { Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException()) }
        }
        coaches.addValueEventListener(coachMessagesListener as ValueEventListener)
    }

    private fun refreshChatMessages() {
        messageDisplay.removeAllViews()
        val sorted = everyMessage?.toList()?.sortedBy { (key, value) -> key }?.toMap()
        if (sorted != null) {
            for(entry in sorted) {
                print("Key: " + entry.key)
                println("    Value: " + entry.value)

                var constraint: ConstraintLayout? = null;
                var spacer = ConstraintLayout(this@Message)
                spacer.minHeight = 50
                allMessages?.forEach { person ->
                    if(person.value?.get(entry.key) != null)
                        if(person.key == "user") {
                            constraint = createMessage(allMessages?.get("user")?.get(entry.key).toString(), "You", true)
                            messageDisplay.addView(constraint)
                            messageDisplay.addView(spacer)
                        }
                        else {
                            constraint = createMessage(allMessages?.get(person.key)?.get(entry.key).toString(), coachNames?.get(person.key).toString(),false)
                            messageDisplay.addView(constraint)
                            messageDisplay.addView(spacer)
                        }
                }
            }
            messagesScroll.postDelayed ({
                messagesScroll.fullScroll(View.FOCUS_DOWN)
            }, 50)
        }
    }

    private fun createMessage(message : String, name : String, right : Boolean) : ConstraintLayout {
        var constraint = ConstraintLayout(this@Message)
        constraint.maxWidth = (messageDisplay.width / 1.75).toInt()
        constraint.setPadding(24)
        if(right) {
            constraint.setBackgroundColor(Color.BLUE)
            constraint.setBackgroundResource(R.drawable.rounded_user_chat_messages)
            constraint.translationX = (messageDisplay.width / 3.00).toFloat()
        } else
            constraint.setBackgroundResource(R.drawable.rounded_coach_chat_messages)
            //constraint.setBackgroundColor(Color.GREEN)

        var headText = TextView(this@Message)
        headText.setText(name)
        headText.setTypeface(null, Typeface.BOLD)
        headText.setPadding(0, 0, 16, 0)

        var text = TextView(this@Message)
        text.setText(message)
        text.breakStrategy = LineBreaker.BREAK_STRATEGY_BALANCED
        text.maxWidth = constraint.maxWidth


        var child = LinearLayout(this@Message)
        child.setPadding(24)
        child.addView(headText)
        child.addView(text)
        child.setLayoutParams( LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        child.setOrientation(LinearLayout.VERTICAL);

        constraint.addView(child)
        return constraint
    }

    private fun onClick() {
        send.setOnClickListener {
            var userMessages = Firebase.database.reference.child("users").child(firebaseUser!!.uid).child("messages").child(System.currentTimeMillis().toString())
            if(messageText.text.isNotEmpty()) {
                userMessages.setValue(messageText.text.toString(), null)
                messageText.text.clear()
            }
        }
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

    fun modifyTheme(){
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


}