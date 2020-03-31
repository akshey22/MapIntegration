package org.unyde.mapintegrationlib.InternalNavigation

import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.unyde.mapintegrationlib.ApplicationContext
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.Marker_Internal_Nav
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.mapview.RouteLayer
import org.unyde.mapintegrationlib.InternalNavigation.view.ModelRenderer
import org.unyde.mapintegrationlib.database.DatabaseClient
import org.unyde.mapintegrationlib.database.entity.PathNode
import java.lang.Float


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

    var source_instruction_list: ArrayList<String>? = null
    var source_instruction_site_list: ArrayList<String>? = null
    var source_instruction_direction_list: ArrayList<Int>? = null
    var destination_instruction_list: ArrayList<String>? = null
    var destination_instruction_site_list: ArrayList<String>? = null
    var destination_instruction_direction_list: ArrayList<Int>? = null
    var total_calorie:Int?=0
    var total_steps:Int?=0


    companion object {
        val TAG = "Cluster3DMap"
        var mActionMode: IndoorMode? = null
        var pan_value: kotlin.Float? =2f
    }

    fun init() {

        mActionMode = IndoorMode.NORMAL
        routeLayer = RouteLayer()
        source_instruction_list = ArrayList<String>()
        source_instruction_site_list = ArrayList<String>()
        source_instruction_direction_list = ArrayList<Int>()
        destination_instruction_list = ArrayList<String>()
        destination_instruction_site_list = ArrayList<String>()
        destination_instruction_direction_list = ArrayList<Int>()


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
            if(source_beacon_floor!=destination_floor_level)
            {
                show3DMapNavigation(source_beacon_floor!!.toInt(),false)
                show3DMapNavigationDestination(destination_floor_level!!.toInt())
            }
            else
            {
                show3DMapNavigation(source_beacon_floor!!.toInt(),true)
            }


        } catch (e: Exception) {
            if(!source_beacon_id.equals(destination_beacon_id))
            {
                Log.e("Cluster3DMapDirection", e.message)
                calorieCallback!!.onCalorieSteps("","", null, null, null)
            }

        }


    }

    fun show3DMapNavigation(floor: Int?,isSameFloor:Boolean?) {
        try {
            var allNode: List<PathNode>

            val allMarker = ArrayList<Marker_Internal_Nav>()

            routeLayer!!.path_node_array = routeLayer!!.selected_floor_path_mapping[Integer.parseInt(floor.toString())]!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()


            for (i in 0 until routeLayer!!.path_node_array!!.size) {
                allNode = DatabaseClient.getInstance(ApplicationContext.get().applicationContext)!!.db!!.pathNodeList().findById(routeLayer!!.path_node_array[i],clusterId)
                if (allNode!!.size > 0) {
                    // allMarker!!.add(Marker_Internal_Nav(java.lang.Float.valueOf(if (allNode!!.get(0).site_map_coord_x != "") allNode!!.get(0).site_map_coord_x else "0")!!, java.lang.Float.valueOf(if (allNode!!.get(0).site_map_coord_y != "") allNode!!.get(0).site_map_coord_y else "0")!!, java.lang.Float.valueOf(if (allNode!!.get(0).site_map_coord_z != "") allNode!!.get(0).site_map_coord_z else "0")!!))
                    allMarker!!.add(Marker_Internal_Nav(java.lang.Float.valueOf(if (allNode!!.get(0).site_map_coord_x != "") allNode!!.get(0).site_map_coord_x else "0")!!, java.lang.Float.valueOf(if (allNode!!.get(0).site_map_coord_y != "") allNode!!.get(0).site_map_coord_y else "0")!!, java.lang.Float.valueOf(if (allNode!!.get(0).site_map_coord_z != "") allNode!!.get(0).site_map_coord_z else "0")!!, allNode!!.get(0).store_id!!, allNode!!.get(0).floor_name!!, allNode!!.get(0).site_id!!, allNode!!.get(0).store_name))
                }
            }

            var path_node_array_1: Array<String>? = null
            if (routeLayer!!.selected_floor_path_mapping.size > 0) {
                for ((k, v) in routeLayer!!.selected_floor_path_mapping) {
                    println("$k = $v")
                    path_node_array_1 = "$v"!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                }
                path_node_array=path_node_array_1
            }


            var totCalorie = routeLayer!!.tot_calorie.toInt()
            total_calorie=totCalorie+total_calorie!!
            var totSteps = routeLayer!!.tot_steps
            total_steps=totSteps+total_steps!!
            source_instruction_list = routeLayer!!.instruction_list as ArrayList<String>
            source_instruction_site_list = routeLayer!!.instruction_site_list as ArrayList<String>
            source_instruction_direction_list = routeLayer!!.instruction_direction_list as ArrayList<Int>

            if(isSameFloor!!)
            {
                calorieCallback!!.onCalorieSteps(totCalorie.toString(), totSteps.toString(), source_instruction_list, source_instruction_site_list, source_instruction_direction_list)
            }


        } catch (e: java.lang.Exception) {
            Log.e("Clauste3D", ""+e.message)
            calorieCallback!!.onCalorieSteps("","", null, null, null)

        }
    }

    fun show3DMapNavigationDestination(floor: Int?) {
        try {
            var allNode: List<PathNode>

            val allMarker = ArrayList<Marker_Internal_Nav>()

            routeLayer!!.path_node_array = routeLayer!!.selected_floor_path_mapping[Integer.parseInt(floor.toString())]!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()


            for (i in 0 until routeLayer!!.path_node_array!!.size) {
                allNode = DatabaseClient.getInstance(ApplicationContext.get().applicationContext)!!.db!!.pathNodeList().findById(routeLayer!!.path_node_array[i],clusterId)
                if (allNode!!.size > 0) {
                    // allMarker!!.add(Marker_Internal_Nav(java.lang.Float.valueOf(if (allNode!!.get(0).site_map_coord_x != "") allNode!!.get(0).site_map_coord_x else "0")!!, java.lang.Float.valueOf(if (allNode!!.get(0).site_map_coord_y != "") allNode!!.get(0).site_map_coord_y else "0")!!, java.lang.Float.valueOf(if (allNode!!.get(0).site_map_coord_z != "") allNode!!.get(0).site_map_coord_z else "0")!!))
                    allMarker!!.add(Marker_Internal_Nav(java.lang.Float.valueOf(if (allNode!!.get(0).site_map_coord_x != "") allNode!!.get(0).site_map_coord_x else "0")!!, java.lang.Float.valueOf(if (allNode!!.get(0).site_map_coord_y != "") allNode!!.get(0).site_map_coord_y else "0")!!, java.lang.Float.valueOf(if (allNode!!.get(0).site_map_coord_z != "") allNode!!.get(0).site_map_coord_z else "0")!!, allNode!!.get(0).store_id!!, allNode!!.get(0).floor_name!!, allNode!!.get(0).site_id!!, allNode!!.get(0).store_name))
                }
            }

            var path_node_array_1: Array<String>? = null
            if (routeLayer!!.selected_floor_path_mapping.size > 0) {
                for ((k, v) in routeLayer!!.selected_floor_path_mapping) {
                    println("$k = $v")
                    path_node_array_1 = "$v"!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                }
                path_node_array=path_node_array_1
            }


            var totCalorie = routeLayer!!.tot_calorie.toInt()
            total_calorie=totCalorie+total_calorie!!
            var totSteps = routeLayer!!.tot_steps
            total_steps=totSteps+total_steps!!
            destination_instruction_list = routeLayer!!.instruction_list as ArrayList<String>
             destination_instruction_site_list = routeLayer!!.instruction_site_list as ArrayList<String>
            destination_instruction_direction_list = routeLayer!!.instruction_direction_list as ArrayList<Int>

            calorieCallback!!.onCalorieSteps1(total_calorie.toString(), total_steps.toString(),source_instruction_list,source_instruction_site_list,source_instruction_direction_list, destination_instruction_list, destination_instruction_site_list, destination_instruction_direction_list)

        } catch (e: java.lang.Exception) {
            Log.e("Clauste3D", ""+e.message)
            calorieCallback!!.onCalorieSteps("","", null, null, null)

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
        fun onCalorieSteps1(calorie: String?, steps: String?, instruction_list: MutableList<String>?, instruction_site_list: MutableList<String>?, instruction_direction_list: MutableList<Int>?, destination_instruction_list: MutableList<String>?, destination_instruction_site_list: MutableList<String>?, destination_instruction_direction_list: MutableList<Int>?)
    }

}
