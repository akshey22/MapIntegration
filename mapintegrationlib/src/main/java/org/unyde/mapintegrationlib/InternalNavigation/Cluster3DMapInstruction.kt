package org.unyde.mapintegrationlib.InternalNavigation

import android.app.Activity
import android.net.Uri
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.lang.Float
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.unyde.mapintegrationlib.ApplicationContext
import org.unyde.mapintegrationlib.InternalNavigation.demo.MainSceneLoader
import org.unyde.mapintegrationlib.InternalNavigation.demo.SceneLoader
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.Marker_Internal_Nav
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.mapview.RouteLayer
import org.unyde.mapintegrationlib.InternalNavigation.view.ModelRenderer
import org.unyde.mapintegrationlib.InternalNavigation.view.ModelSurfaceView
import org.unyde.mapintegrationlib.database.DatabaseClient
import org.unyde.mapintegrationlib.database.entity.PathNode
import org.unyde.mapintegrationlib.interfaces.FloorClickListner
import org.unyde.mapintegrationlib.util.Constants


class Cluster3DMapInstruction(internal var activity: AppCompatActivity, var calorieCallback: CalorieStepsCallback?, var clusterId: String) {


    var renderer: ModelRenderer? = null
    var routeLayer: RouteLayer? = null
    var path_node_array: Array<String>? = null
    val paramUri: Uri? = null
    val paramType: Int = -1
    var init_scene_animation: Boolean = true
    internal var dbNode: List<PathNode>? = null
    private var destination_beacon_site_id: String? = null
    private var destination_store_name: String? = null
    private var destination_floor_level: String? = null
    private var destination_cordinate_x: String? = null
    private var destination_cordinate_y: String? = null
    private var destination_cordinate_z: String? = null
    //internal var calorieCallback: CalorieStepsCallback? = null

    var temp_connection_list_source: java.util.HashMap<String, List<String>>? = null
    var temp_connection_list_destination: java.util.HashMap<String, List<String>>? = null


    companion object {
        val TAG = "Cluster3DMap"
        var mActionMode: IndoorMode? = null
        var pan_value: kotlin.Float? =2f
    }

    fun init() {

        mActionMode = IndoorMode.NORMAL
        routeLayer = RouteLayer()



    }


    fun getDirection(source_beacon_id: String, destination_beacon_id: String, source_beacon_floor: String) {
        // remove_obj_by_class("pin");
        // mActionMode = IndoorMode.DIRECTION
        try {
            // setStoreMarkers(source_beacon_floor!!.toInt())
            temp_connection_list_source = HashMap()
            temp_connection_list_destination = HashMap()
            destination_data(destination_beacon_id)
            getConnectionList(source_beacon_floor!!.toInt())
            routeLayer!!.node_connection_list = temp_connection_list_source
            routeLayer!!.setGraph();

            if (source_beacon_floor!!.toInt() == destination_floor_level!!.toInt()) {

                routeLayer!!.getpathforsame_floor(source_beacon_floor, source_beacon_id, destination_beacon_id)

            } else {
                setConnectionList_dest(destination_floor_level!!.toInt())
                routeLayer!!.node_connection_list_dest = temp_connection_list_destination
                routeLayer!!.setGraph1()
                val liftnodelist_source = DatabaseClient.getInstance(ApplicationContext.get().applicationContext)!!.db!!.pathNodeList().findLiftnodeByFloor(source_beacon_floor, "Lift",clusterId)
                val liftnodelist_dest = DatabaseClient.getInstance(ApplicationContext.get().applicationContext)!!.db!!.pathNodeList().findLiftnodeByFloor(destination_floor_level, "Lift",clusterId)
                routeLayer!!.getpathfordifferent_floor(source_beacon_floor, source_beacon_id, liftnodelist_source, liftnodelist_dest, destination_beacon_id, destination_floor_level)

            }
            val distict_floor_array = ArrayList<String>()
            distict_floor_array!!.add(source_beacon_floor)
            distict_floor_array!!.add(destination_floor_level!!)

        } catch (e: Exception) {
            if(!source_beacon_id.equals(destination_beacon_id))
            {
                Log.e("Cluster3DMapDirection", e.message)
                calorieCallback!!.onCalorieSteps("","", null, null, null)
            }

        }


    }

    private fun destination_data(destination_beacon_id: String) {

        try {
            dbNode = DatabaseClient.getInstance(ApplicationContext.get().applicationContext)!!.db!!.pathNodeList().findById(destination_beacon_id,clusterId)
            if (dbNode != null && dbNode!!.size != 0) {
                destination_beacon_site_id = dbNode!!.get(0).getSite_id()
                destination_store_name = dbNode!!.get(0).getStore_name()
                destination_cordinate_x = dbNode!!.get(0).getSite_map_coord_x()
                destination_cordinate_y = dbNode!!.get(0).getSite_map_coord_y()
                destination_cordinate_z = dbNode!!.get(0).getSite_map_coord_z()
                destination_floor_level = dbNode!!.get(0).getFloor_level()
            }
        } catch (e: Exception) {

        }


    }

    private fun getConnectionList(floor_level: Int) {
        try {
            val node = DatabaseClient.getInstance(ApplicationContext.get().applicationContext)!!.db!!.pathNodeList().findByFloor_level(floor_level,clusterId)

            val nodeConnection = DatabaseClient.getInstance(ApplicationContext.get().applicationContext)!!.db!!.pathNodeConnectList().findByFloorLevel(floor_level,clusterId)
            temp_connection_list_source!!.clear()
            for (i in nodeConnection.indices) {
                val key = nodeConnection[i].site_id
                if (temp_connection_list_source!!.containsKey(key)) {
                    val temp_list = temp_connection_list_source!![key]
                    (temp_list!! as java.util.ArrayList).add(nodeConnection[i].site_id_connect)
                    temp_connection_list_source!![key] = temp_list
                } else {
                    val temp_list = java.util.ArrayList<String>()
                    temp_list.add(nodeConnection[i].site_id_connect)
                    temp_connection_list_source!![key] = temp_list
                }
            }
        } catch (e: Exception) {

        }


    }


    private fun setConnectionList_dest(floor_level: Int) {
        try {
            val nodeConnection =DatabaseClient.getInstance(ApplicationContext.get().applicationContext)!!.db!!.pathNodeConnectList().findByFloorLevel(floor_level,clusterId)
            temp_connection_list_destination!!.clear()
            for (i in nodeConnection.indices) {
                val key = nodeConnection[i].site_id
                if (temp_connection_list_destination!!.containsKey(key)) {
                    val temp_list = temp_connection_list_destination!![key]
                    (temp_list!! as java.util.ArrayList).add(nodeConnection[i].site_id_connect)
                    temp_connection_list_destination!![key] = temp_list

                } else {
                    val temp_list = java.util.ArrayList<String>()
                    temp_list.add(nodeConnection[i].site_id_connect)
                    temp_connection_list_destination!![key] = temp_list
                }
            }
        } catch (e: Exception) {

        }

    }


    enum class IndoorMode {
        NORMAL,
        DIRECTION,
        NAVIGATION
    }

    interface CalorieStepsCallback {
        fun onCalorieSteps(calorie: String?, steps: String?, instruction_list: MutableList<String>?, instruction_site_list: MutableList<String>?, instruction_direction_list: MutableList<Int>?)
    }

}
