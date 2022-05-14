package com.example.weightloss_pathway_project

import android.app.*
import android.app.PendingIntent.getActivity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler

import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.protobuf.Value
import java.util.*


class NotificationService : Service() {
    var TAG = "NOTI"
    private var messages : DatabaseReference? = null
    private var messageListener : ValueEventListener? = null
    private var last : DataSnapshot? = null
    private var pause = false

    private val CHANNEL_ID = "chat_channel_id"
    private val notificationId = 101

    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onCreate() {
        createNotificationChannel()
        if(FirebaseAuth.getInstance().uid!! != null) {
            messages =
                Firebase.database.reference.child("users").child(FirebaseAuth.getInstance().uid!!)
                    .child("coaches")
            messageListener = object : ValueEventListener {
                override fun onDataChange(messages: DataSnapshot) {
                    Log.e(TAG, "messageReceived")
                    if (last != null)
                        compareMessages(messages)
                    last = messages
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }
            messages?.addValueEventListener(messageListener as ValueEventListener)
            Log.e(TAG, "onCreate")
        }
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        if(FirebaseAuth.getInstance().uid == null)
            messages?.removeEventListener(messageListener as ValueEventListener)
        super.onDestroy()
    }

    private fun compareMessages(messages : DataSnapshot) {
        messages.children.forEach { coach ->
            coach.children.forEach { message ->
                if(coach.key?.let { message.key?.let { it1 -> last?.child(it)?.child(it1)?.value } } == null) {
                    chatNotification("Message Received", message.value as String)
                }
            }
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

    private fun chatNotification(header : String, desc : String) {
        val intent = Intent(applicationContext, Message::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        }

        val intent2 = Intent(applicationContext, Main::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        }

        val intents = arrayOf<Intent>(intent2, intent)
        val pendingIntent: PendingIntent = PendingIntent.getActivities(applicationContext, 0, intents, PendingIntent.FLAG_IMMUTABLE)


        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.fui_ic_mail_white_24dp)
            .setContentTitle(header)
            .setContentText(desc)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }

        //Toast.makeText(applicationContext, "Currently Under Construction", Toast.LENGTH_LONG).show()
    }
}