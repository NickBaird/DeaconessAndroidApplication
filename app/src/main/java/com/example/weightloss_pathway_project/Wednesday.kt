package com.example.weightloss_pathway_project

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class Wednesday : Fragment() {
    private lateinit var dateString : String
    private lateinit var goalList : ArrayList<CharSequence>
    private lateinit var plannedDatabase: DatabaseReference
    private var firebaseUser : FirebaseUser? = null
    private var currentNutritionalGoals: ArrayList<NutritionalGoals>? = null
    private var currentFitnessGoals: ArrayList<FitnessGoals>? = null
    private var currentPlannedGoals: ArrayList<DefinedGoal>? = null
    private lateinit var list : ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {

        val view : View = inflater.inflate(R.layout.fragment_wednesday, container, false)

        list = view.findViewById(R.id.goalsList)

        initialize()
        gettingGoals()
        createDateGoal()

        // Inflate the layout for this fragment
        return view
    }

    // Initialize all variables
    private fun initialize(){
        dateString = String()
        goalList = ArrayList()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        plannedDatabase = Firebase.database.reference.child("users").child(firebaseUser!!.uid).child("plannedGoals")
        currentFitnessGoals = ArrayList()
        currentNutritionalGoals = ArrayList()
        currentPlannedGoals = ArrayList()

        try{
            // Set the date
            dateString = getDate()!!
            goalList = ArrayList<CharSequence>(getGoals()!!)
        }
        catch(e : Exception){

        }
    }

    // Get date selection from activity
    private fun getDate() : String?{
        val data = arguments
        return data?.getString("date")
    }

    private fun getGoals() : ArrayList<CharSequence>?{
        val data = arguments
        return data?.getCharSequenceArrayList("goalList")
    }

    // Get goals from the database
    // Query occurs asynchronously and requires downhill listview setting within function
    private fun gettingGoals(){
        // Access Database
        val postListener2 = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                for (dataSnapShot in dataSnapshot.children) {
                    val newPlan = dataSnapShot.getValue<DefinedGoal>()!!
                    currentPlannedGoals?.add(newPlan)
                }
                //createFitnessGoals()
                createPlannedGoals()

                // Will set listview values here
                Handler(Looper.getMainLooper()).postDelayed({
                    setListView()
                }, 1000)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        plannedDatabase.addValueEventListener((postListener2))
    }

    // creates listview occurrence for date
    private fun createDateGoal(){
        if (dateString != String()) {
            goalList.add(0, dateString)
            goalList.add(1, "--- WEEKLY GOALS ---")
            goalList.add(goalList.size, "--- PLANNED GOALS --- ")
        }
    }

    // creates fitness goal and adds to list for listview occurrence
    private fun createPlannedGoals() {
        if (currentPlannedGoals != null) {
            for (item: DefinedGoal in currentPlannedGoals!!) {
                if (item.date == dateString) {
                    goalList.add(item.goal)
                }
            }
        }
    }

    // sets listview values
    private fun setListView(){
        val arrayAdapter = activity?.baseContext?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_list_item_1, goalList
            )
        }

        var found = false;
        list.adapter = arrayAdapter
        list.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                val clickedItem = list.getItemAtPosition(position) as String
                for (item : DefinedGoal in currentPlannedGoals!!){
                    if (clickedItem == item.goal){
                        planActivity(R.layout.activity_planned_view, item)
                        found = true
                    }
                }
                if(!found)
                    weeklyPlanActivity(R.layout.activity_created_goal_weekly)
            }
    }

    // Intent that will open PlanningGoals activity when activated
    private fun planActivity(view: Int, definedGoal : DefinedGoal){
        val intent = Intent(activity, PlannedView::class.java)
        intent.putExtra("viewGoal", definedGoal)
        requireActivity().startActivity(intent)
    }

    private fun weeklyPlanActivity(view: Int){
        val intent = Intent(activity, CreatedGoalWeekly::class.java)
        intent.putExtra("dateSelection", dateString)
        requireActivity().startActivity(intent)
    }
}