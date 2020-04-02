package org.unyde.mapintegrationlib

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.balysv.materialripple.MaterialRippleLayout
import org.unyde.mapintegrationlib.InternalNavigation.Cluster3DMap
import org.unyde.mapintegrationlib.InternalNavigation.Cluster3DMapInstruction
import org.unyde.mapintegrationlib.adapter.DestinationStepsListAdapter
import org.unyde.mapintegrationlib.adapter.SourceStepsListAdapter
import org.unyde.mapintegrationlib.util.Constants

class DirectionInstructionActivity : AppCompatActivity(),
    Cluster3DMapInstruction.CalorieStepsCallback, SensorEventListener {



    ///////////////Source Beacon
    private var source_beacon_siteid_i_m_here: String? = null
    private var source_store_name_i_m_here: String? = null
    private var source_floor_level_i_m_here: String? = null
    private var source_floor: String? = null
    private var source_floor_name: String? = null

    ///////////////Destination

    private var destination_store: Int = 0
    private var destination_site: String = ""
    private var dest_floor_level: String = ""
    private var destination_store_name: String = ""
    private var destination_store_address: String = ""
    private var destination_floor: String? = null
    private var destination_floor_name: String? = null
    private var destination_logo: String? = null

    ///////////////////////////////Views
    private var back_button: ImageView? = null
    internal var w: Window? = null

    ////////////////////////Map


    /////////////////////////////////Common
    var isViaBeacon: Boolean? = false
    var cluster_id: String? = null
    var mall_name: String? = null
    private var floor: Int = 0
    var cluster3DMapInstruction: Cluster3DMapInstruction? = null
    private var shownFloorMap: String? = null

    ////////////////////////////////
    var instruction_list: MutableList<String>? = null
    var instruction_site_list: MutableList<String>? = null
    var instruction_direction_list: MutableList<Int>? = null
    internal var mSensorManager: SensorManager? = null

    var source :TextView?=null
    var destination :TextView?=null
    var steps :TextView?=null
    var floor_1 :TextView?=null
    var floor_2:TextView?=null
    var steps_recycler :RecyclerView?=null
    var steps_recycler_1 :RecyclerView?=null
    var cc_1 :CardView?=null
    var cc_2 :CardView?=null
    var btnStartNavigation :MaterialRippleLayout?=null

    ///////////////////////////////////
    var sourceStepsListAdapter: SourceStepsListAdapter? = null
    var destinationStepsListAdapter: DestinationStepsListAdapter? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direction_instruction)
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        back_button = findViewById(R.id.back_button)
        source = findViewById(R.id.source)
        destination = findViewById(R.id.destination)
        steps = findViewById(R.id.steps)
        floor_1 = findViewById(R.id.floor_1)
        floor_2 = findViewById(R.id.floor_2)
        steps_recycler = findViewById(R.id.steps_recycler)
        steps_recycler_1 = findViewById(R.id.steps_recycler_1)
        steps_recycler?.setLayoutManager(GridLayoutManager(this@DirectionInstructionActivity, 1, RecyclerView.VERTICAL, false))
        steps_recycler_1?.setLayoutManager(GridLayoutManager(this@DirectionInstructionActivity, 1, RecyclerView.VERTICAL, false))
        cc_1 = findViewById(R.id.cc_1)
        cc_2 = findViewById(R.id.cc_2)
        btnStartNavigation = findViewById(R.id.btnStartNavigation)
        ///////////from Other Activity
        source_beacon_siteid_i_m_here = intent.getStringExtra("source_site_id")
        source_floor_level_i_m_here = intent.getStringExtra("source_floor")
        source_store_name_i_m_here = intent.getStringExtra("source_store_name")

        source_floor = intent.getStringExtra("source_floor")
        source_floor_name = intent.getStringExtra("source_floor_name")
        source!!.text=source_store_name_i_m_here+", "+source_floor_name
        destination_site = intent.getStringExtra("destination_site_id")
        dest_floor_level = intent.getStringExtra("destination_floor_level")
        destination_floor = intent.getStringExtra("destination_floor_level")
        destination_floor_name = intent.getStringExtra("destination_floor_name")
        destination_store_name = intent.getStringExtra("destination_store_name")
        destination!!.text=destination_store_name+", "+destination_floor_name
        destination_logo = intent.getStringExtra("destination_logo")

        destination_store_address = intent.getStringExtra("destination_store_address")
        destination_store = intent.getIntExtra("destination_store_id", 0)
        cluster_id = getIntent().getStringExtra("cluster_id")
        mall_name = getIntent().getStringExtra("mall_name")
        isViaBeacon = getIntent().getBooleanExtra("isViaBeacon", false)
        ////////////////////////////
        floor = source_floor_level_i_m_here!!.toInt()
        if(source_floor_level_i_m_here!!.toInt()!=dest_floor_level.toInt())
        {
          cc_2!!.visibility=View.VISIBLE
            floor_1!!.text=source_floor_name
            floor_2!!.text=destination_floor_name
        }
        else
        {
            cc_2!!.visibility=View.GONE
            floor_1!!.text=source_floor_name
        }
        shownFloorMap = floor.toString()
        cluster3DMapInstruction = Cluster3DMapInstruction(this,  this, cluster_id!!)
        cluster3DMapInstruction!!.init()
        cluster3DMapInstruction!!.getDirection(source_beacon_siteid_i_m_here!!, destination_site, source_floor_level_i_m_here!!)

        back_button!!.setOnClickListener {
            finish()
        }


        btnStartNavigation!!.setOnClickListener{
            var prodIn = Intent(this@DirectionInstructionActivity, ClusterMapNavigationActivity::class.java)
            prodIn.putExtra("source_site_id", source_beacon_siteid_i_m_here)
            prodIn.putExtra("source_floor", source_floor_level_i_m_here)
            prodIn.putExtra("source_floor_name", source_floor_name)
            prodIn.putExtra("source_store_name", source_store_name_i_m_here)
            prodIn.putExtra("destination_site_id", destination_site)
            prodIn.putExtra("destination_floor_level", destination_floor)
            prodIn.putExtra("destination_floor_name", destination_floor_name)
            prodIn.putExtra("destination_store_id", destination_store)
            prodIn.putExtra("destination_store_name", destination_store_name)
            prodIn.putExtra("destination_store_address", destination_store_address)
            prodIn.putExtra("destination_logo", destination_logo)
            prodIn.putExtra("cluster_id", cluster_id)
            prodIn.putExtra("mall_name", mall_name)
            prodIn.putExtra("isViaBeacon", false)
            prodIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(prodIn)
        }

    }


    override fun onCalorieSteps(
        calorie: String?,
        steps_count: String?,
        instruction_list: MutableList<String>?,
        instruction_site_list: MutableList<String>?,
        instruction_direction_list: MutableList<Int>?
    ) {
        try {

            if (calorie.equals("")) {
               // Pref_manager.customToastNew(this@Cluster3DLocateMapActivity, "No Result Found", "")
               // d_nav_linear!!.visibility = View.GONE
               // nav_bottomsheet?.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                steps!!.text="Route Steps("+steps_count+" steps)"
                this.instruction_list = instruction_list
                this.instruction_site_list = instruction_site_list
                this.instruction_direction_list = instruction_direction_list
                sourceStepsListAdapter = SourceStepsListAdapter(instruction_list, instruction_site_list, instruction_direction_list, this@DirectionInstructionActivity)
                steps_recycler?.setAdapter(sourceStepsListAdapter)
                sourceStepsListAdapter!!.notifyDataSetChanged()
            }


        } catch (e: Exception) {

        }
    }

    override fun onCalorieSteps1(
        calorie: String?,
        steps_count: String?,
        instruction_list: MutableList<String>?,
        instruction_site_list: MutableList<String>?,
        instruction_direction_list: MutableList<Int>?,
        destination_instruction_list: MutableList<String>?,
        destination_instruction_site_list: MutableList<String>?,
        destination_instruction_direction_list: MutableList<Int>?
    ) {
        try {

            if (calorie.equals("")) {
                // Pref_manager.customToastNew(this@Cluster3DLocateMapActivity, "No Result Found", "")
                // d_nav_linear!!.visibility = View.GONE
                // nav_bottomsheet?.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                steps!!.text="Route Steps("+steps_count+" steps)"
                this.instruction_list = instruction_list
                this.instruction_site_list = instruction_site_list
                this.instruction_direction_list = instruction_direction_list
                sourceStepsListAdapter = SourceStepsListAdapter(instruction_list, instruction_site_list, instruction_direction_list, this@DirectionInstructionActivity)
                steps_recycler?.setAdapter(sourceStepsListAdapter)
                sourceStepsListAdapter!!.notifyDataSetChanged()
                destinationStepsListAdapter = DestinationStepsListAdapter(destination_instruction_list, destination_instruction_site_list, destination_instruction_direction_list, this@DirectionInstructionActivity)
                steps_recycler_1?.setAdapter(destinationStepsListAdapter)
                destinationStepsListAdapter!!.notifyDataSetChanged()
            }


        } catch (e: Exception) {

        }
    }


    fun getDirection() {

        try {

            if (!Cluster3DMap.mActionMode!!.equals(Cluster3DMap.IndoorMode.NAVIGATION)) {
                /*if (nav_bottomsheet?.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    nav_bottomsheet?.setState(BottomSheetBehavior.STATE_EXPANDED);

                } else {
                    nav_bottomsheet!!.isHideable = true
                    nav_bottomsheet?.setState(BottomSheetBehavior.STATE_HIDDEN);

                }*/
            }
            cluster3DMapInstruction!!.getDirection(source_beacon_siteid_i_m_here!!, destination_site, source_floor_level_i_m_here!!)

        } catch (e: Exception) {
            Log.e("3Dlocategetdirection", "" + e.message)
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        try {
            val degree = Math.round(event!!.values[0]).toFloat()
            Cluster3DMap.scene!!.orientation = degree;
        } catch (e: Exception) {

        }

    }
    override fun onPause() {
        super.onPause()
        try {
            mSensorManager!!.unregisterListener(this)
        } catch (e: Exception) {

        }


    }


    override fun onResume() {
        super.onResume()
        try {
            mSensorManager!!.registerListener(this, mSensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME)
        } catch (e: Exception) {
            Log.e("Cluster3DNavigation", "" + e.message)
        }

    }

}
