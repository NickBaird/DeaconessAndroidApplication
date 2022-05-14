package com.example.weightloss_pathway_project

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class StartUpBootReceiver : BroadcastReceiver() {

    var TAG = "NOTI"
    private lateinit var messages : DatabaseReference
    private lateinit var messageListener : ValueEventListener
    private var last : DataSnapshot? = null
    private var first = false
    private val CHANNEL_ID = "chat_channel_id"
    private val notificationId = 101

    override fun onReceive(context: Context?, intent: Intent) {
        //Toast.makeText(context, FirebaseAuth.getInstance().uid, Toast.LENGTH_LONG).show()
       /* messages = Firebase.database.reference.child("users").child(FirebaseAuth.getInstance().uid!!).child("coaches")

        val intent = Intent(context, Message::class.java)
        if (context != null)
            context.startForegroundService(intent)

        createNotificationChannel(context!!)
        messages = Firebase.database.reference.child("users").child(FirebaseAuth.getInstance().uid!!).child("coaches")
        messageListener = object : ValueEventListener {
            override fun onDataChange(messages : DataSnapshot) {
                if(last != null)
                    compareMessages(messages, context)
                last = messages
            }
            override fun onCancelled(databaseError: DatabaseError) { Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException()) }
        }
        messages.addValueEventListener(messageListener as ValueEventListener)
        */
        if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
            var serviceIntent = Intent(".NotificationService")
            context?.startService(serviceIntent)
        }
    }

    private fun compareMessages(messages : DataSnapshot, context: Context) {
        messages.children.forEach { coach ->
            coach.children.forEach { message ->
                if(coach.key?.let { message.key?.let { it1 -> last?.child(it)?.child(it1)?.value } } == null) {
                    chatNotification("Message Received", message.value as String, context)
                    Toast.makeText(context, message.value as String, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
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
                getSystemService(context, this::class.java) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun chatNotification(header : String, desc : String, context: Context) {
        val intent = Intent(context, Message::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.wellness_image)
            .setContentTitle(header)
            .setContentText(desc)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }

        Toast.makeText(context, "Currently Under Construction", Toast.LENGTH_LONG).show()
    }


}