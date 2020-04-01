package org.unyde.mapintegrationlib.InternalNavigation.indoornav.mapview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import org.unyde.mapintegrationlib.ApplicationContext;
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.Path_node;
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.hipster.algorithm.Hipster;
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.hipster.graph.GraphBuilder;
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.hipster.graph.GraphSearchProblem;
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.hipster.graph.HipsterGraph;
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.hipster.model.problem.SearchProblem;
import org.unyde.mapintegrationlib.R;
import org.unyde.mapintegrationlib.database.DatabaseClient;
import org.unyde.mapintegrationlib.database.entity.PathNode;
import org.unyde.mapintegrationlib.util.Constants;
import org.unyde.mapintegrationlib.util.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


/**
 * RouteLayer
 *
 * @author: onlylemi
 */
public class RouteLayer_Direction extends MapBaseLayer {

    private List<Integer> routeList; // routes list
    private List<PointF> nodeList; // nodes list

    private float routeWidth; // the width of route

    private Bitmap routeStartBmp;
    private Bitmap routeEndBmp;
    public Path_node path_node;
    private List<String> nodeName;
    private List<PointF> marks;
    public int my_target_index = -1;

    public String my_target_beacon = null;
    public String my_source_beacon = null;
    HipsterGraph<String, Double> graph, graph1;
    public String current_beacon_id;
    SearchProblem p;

    public Boolean sequential_straight_status = false;

    private Paint paint;

    public String path_node_array[];
    List<String> splited_path_source, splited_path_dest;
    public int selected_index_of_splitted_path;
    public HashMap<Integer, String> selected_floor_path_mapping;
    public Float tot_calorie=0.0f;
    public Integer tot_steps=0;

    public List<String> instruction_list;
    public List<String> instruction_site_list;
    public List<Integer> instruction_direction_list;


    public List<String> destination_instruction_list;
    public List<String> destination_instruction_site_list;
    public List<Integer> destination_instruction_direction_list;


    public boolean multifloor_status = false;
    public boolean src_floor_status = false;

    String TAG="RouteLayer";

    public HashMap<String, List<String>> node_connection_list, node_connection_list_dest;


    public RouteLayer_Direction(MapView mapView) {
        this(mapView, null, null);
    }

    public RouteLayer_Direction(MapView mapView, List<String> nodeName, List<PointF> marks) {
        this(mapView, nodeName, marks, null, null);
    }


    public RouteLayer_Direction() {
        splited_path_dest = new ArrayList<String>();
        splited_path_source = new ArrayList<String>();
        selected_floor_path_mapping = new HashMap<Integer, String>();
        node_connection_list = new HashMap<String, List<String>>();
        instruction_list = new ArrayList<String>();
        instruction_site_list = new ArrayList<String>();
        instruction_direction_list = new ArrayList<Integer>();

        destination_instruction_list = new ArrayList<String>();
        destination_instruction_site_list = new ArrayList<String>();
        destination_instruction_direction_list = new ArrayList<Integer>();
    }

    public RouteLayer_Direction(MapView mapView, List<String> nodeName, List<PointF> marks, List<PointF> nodeList, List<Integer> routeList) {
        super(mapView);
        node_connection_list = new HashMap<String, List<String>>();
        this.nodeList = nodeList;
        this.routeList = routeList;
        this.nodeName = nodeName;
        this.marks = marks;
        path_node = new Path_node();
        /*for (int i = 0; i < this.nodeName.size(); i++) {
            path_node.set_node_data(nodeName.get(i), new Point((int) marks.get(i).x, (int) marks.get(i).y));
        }*/
        initLayer();
    }


    public void getpathfordifferent_floor(String source_floor_level, String source_site_id, List<PathNode> source_floor_connector_node_list, List<PathNode> destination_floor_connector_node_list, String destination_site_id, String destination_floor_level) {

        try {
            List<String> return_splitted_path;
            HashMap<String, String> intermediate_node_map;
            intermediate_node_map = new HashMap<String, String>();
            return_splitted_path = new ArrayList<>();
            reset_splited_path();
            for (int i = 0; i < source_floor_connector_node_list.size(); i++) {
                String source_node_name = source_floor_connector_node_list.get(i).getNode_name();
                String source_node_connector_site_id = source_floor_connector_node_list.get(i).getSite_id();
                for (int j = 0; j < destination_floor_connector_node_list.size(); j++) {
                    if (destination_floor_connector_node_list.get(j).getNode_name().equals(source_node_name)) {
                        intermediate_node_map.put(source_node_connector_site_id, destination_floor_connector_node_list.get(j).getSite_id());
                        make_combine_graph_path(source_site_id, source_node_connector_site_id, destination_floor_connector_node_list.get(j).getSite_id(), destination_site_id);
                    }
                }
            }
            setshortestpathofsrcanddestfloor(source_floor_level, destination_floor_level);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }


    }


    public void getpathforsame_floor(String source_floor_level, String source_site, String destination_site) {



        List<String> return_splitted_path;
        return_splitted_path = new ArrayList<>();
        reset_splited_path();

        p = GraphSearchProblem
                .startingFrom(source_site)
                .in(graph)
                .takeCostsFromEdges()
                .build();
        String process_path = Hipster.createDijkstra(p).search(destination_site).toString();
        String process_path_new = ((process_path.replace("[", "")).replace("]", "")).replace(" ", "");
        String process_new_latest = process_path_new.replace("\n", "");
        //splited_path.add(process_new_latest);
        //splited_path_source.add(process_new_latest);
        //path_node_array = process_new_latest.split(",");
        selected_floor_path_mapping.put(Integer.parseInt(source_floor_level), process_new_latest);
        float calories_val = calories_calculation(process_new_latest)[0];
        instruction_list.clear();
        instruction_direction_list.clear();
        instruction_site_list.clear();


        multifloor_status = false;
        src_floor_status = true;
        get_instruction_list_by_floor(process_new_latest);
        tot_calorie = calories_val;
        tot_steps= (int) calories_calculation(process_new_latest)[1];
    }


    public void setshortestpathofsrcanddestfloor(String source_floor_level, String destination_floor_level) {

        int shortest_path_length = 10000; //should be total number of beacon

        for (int i = 0; i < splited_path_source.size(); i++) {
            int temp_length = splited_path_source.get(i).split(",").length + splited_path_dest.get(i).split(",").length;
            if (temp_length < shortest_path_length) {
                shortest_path_length = temp_length;
                selected_index_of_splitted_path = i;
            }

        }

        selected_floor_path_mapping.put(Integer.parseInt(source_floor_level), splited_path_source.get(selected_index_of_splitted_path));
        selected_floor_path_mapping.put(Integer.parseInt(destination_floor_level), splited_path_dest.get(selected_index_of_splitted_path));
        float calories_val = calories_calculation(splited_path_source.get(selected_index_of_splitted_path))[0];
        float calories_val1 = calories_calculation(splited_path_dest.get(selected_index_of_splitted_path))[0];
        instruction_list.clear();
        instruction_direction_list.clear();
        instruction_site_list.clear();
        destination_instruction_list.clear();
        destination_instruction_direction_list.clear();
        destination_instruction_site_list.clear();


        multifloor_status = true;
        src_floor_status = true;
        get_instruction_list_by_floor(splited_path_source.get(selected_index_of_splitted_path));
        src_floor_status = false;
        destination_get_instruction_list_by_floor(splited_path_dest.get(selected_index_of_splitted_path));

        tot_calorie = calories_val + calories_val1;
        tot_steps= (int) calories_calculation(splited_path_source.get(selected_index_of_splitted_path))[1]+(int) calories_calculation(splited_path_dest.get(selected_index_of_splitted_path))[1];
        Log.e(TAG, "setshortestpathofsrcanddestfloor: " + selected_floor_path_mapping.size());


    }


    public void get_instruction_list_by_floor(String site_list)
    {
        Random rand = new Random();
        List<String> instruction = new ArrayList<>();
        String site_list_array[];
        site_list_array = site_list.split(",");
        sequential_straight_status = false;
        for(int i=0;i<site_list_array.length;i++)
        {
            //instruction_site_list.add(site_list_array[i]);
            // instruction_direction_list.add(Integer.toString(rand.nextInt(6 - 1) + 1));
            if(i==0)
            {
                generate_dynamic_instruction_initial(site_list_array[i],site_list_array[i+1]);
                //instruction_list.add("Your Location");
                //instruction_direction_list.add("0");

            }
            else if(i==site_list_array.length-1)
            {
                if(multifloor_status && src_floor_status)
                {
                    instruction_site_list.add(site_list_array[i]);
                    instruction_list.add("Take Lift");
                    instruction_direction_list.add(Constants.LIFT);
                }
                else
                {
                    instruction_site_list.add(site_list_array[i]);
                    instruction_list.add("Your Destination");
                    instruction_direction_list.add(Constants.DESTINATION);
                }

            }
            else
            {
                generate_dynamic_instruction_intermediate(site_list_array[i-1],site_list_array[i],site_list_array[i+1]);
                //instruction_list.add("Head Straight");
                //instruction_direction_list.add("1");
                // Math
            }

        }
    }

    public void destination_get_instruction_list_by_floor(String site_list)
    {
        Random rand = new Random();
        List<String> instruction = new ArrayList<>();
        String site_list_array[];
        site_list_array = site_list.split(",");
        sequential_straight_status = false;
        for(int i=0;i<site_list_array.length;i++)
        {
            //instruction_site_list.add(site_list_array[i]);
            // instruction_direction_list.add(Integer.toString(rand.nextInt(6 - 1) + 1));
            if(i==0)
            {
                destination_generate_dynamic_instruction_initial(site_list_array[i],site_list_array[i+1]);
                //instruction_list.add("Your Location");
                //instruction_direction_list.add("0");

            }
            else if(i==site_list_array.length-1)
            {
                if(multifloor_status && src_floor_status)
                {
                    destination_instruction_site_list.add(site_list_array[i]);
                    destination_instruction_list.add("Take Lift");
                    destination_instruction_direction_list.add(Constants.LIFT);
                }
                else
                {
                    destination_instruction_site_list.add(site_list_array[i]);
                    destination_instruction_list.add("Your Destination");
                    destination_instruction_direction_list.add(Constants.DESTINATION);
                }

            }
            else
            {
                destination_generate_dynamic_instruction_intermediate(site_list_array[i-1],site_list_array[i],site_list_array[i+1]);
                //instruction_list.add("Head Straight");
                //instruction_direction_list.add("1");
                // Math
            }

        }
    }


    public void generate_dynamic_instruction_initial(String site1,String site2)
    {
        instruction_site_list.add(site1);
        float[] src_coord = get_node_coordinate(site1);
        float[] dest_coord = get_node_coordinate(site2);
        float delta_x = dest_coord[0] - src_coord[0];
        float delta_z = dest_coord[2] - src_coord[2];
        float delta_y = 0;


        float vlen = android.opengl.Matrix.length(delta_x, delta_y, delta_z);
        delta_x /= vlen;
        delta_y /= vlen;
        delta_z /= vlen;

        float[] base_vector = new float[]{0,0,1};
        float[] generated_vector = new float[] {delta_x,delta_y,delta_z};

        //angle
        float var1 = (base_vector[0]*generated_vector[0])+(base_vector[2]*generated_vector[2]);
        float var2 = (float)(Math.sqrt((base_vector[0]*base_vector[0])+(base_vector[2]*base_vector[2]))*Math.sqrt((generated_vector[0]*generated_vector[0])+(generated_vector[2]*generated_vector[2])));

        double angla = Math.acos(var1/var2);
        double angle_degree = Math.toDegrees(angla);
        float degree = (float)(-angle_degree-45);
        if(degree<=23 && degree>-23)
        {
            instruction_list.add("Head towards North ");
            instruction_direction_list.add(Constants.HEAD_TOWARDS_NORTH);
        }
        else if(degree<=-23&& degree>-67)
        {
            instruction_list.add("Head towards North West ");
            instruction_direction_list.add(Constants.HEAD_TOWARDS_NORTH_WEST);
        }
        else if(degree<=-67 && degree>-113)
        {
            instruction_list.add("Head towards  West ");
            instruction_direction_list.add(Constants.HEAD_TOWARDS_WEST);
        }
        else if(degree<=-113 && degree>-157)
        {
            instruction_list.add("Head towards  South West ");
            instruction_direction_list.add(Constants.HEAD_TOWARDS_SOUTH_WEST);
        }
        else if(degree<=-157 && degree>-203)
        {
            instruction_list.add("Head towards South");
            instruction_direction_list.add(Constants.HEAD_TOWARDS_SOUTH);
        }
        else if(degree<=-203 && degree>-247)
        {
            instruction_list.add("Head towards South East");
            instruction_direction_list.add(Constants.HEAD_TOWARDS_SOUTH_EAST);
        }
        else if(degree<=-247 && degree>-293)
        {
            instruction_list.add("Head towards East");
            instruction_direction_list.add(Constants.HEAD_TOWARDS_EAST);
        }
        else if(degree<=-293 && degree>-337)
        {
            instruction_list.add("Head towards North East");
            instruction_direction_list.add(Constants.HEAD_TOWARDS_NORTH_EAST);
        }
        else if(degree<=-337 && degree>=360)
        {
            instruction_list.add("Head towards North ");
            instruction_direction_list.add(Constants.HEAD_TOWARDS_NORTH);
        }


        else if(degree<=67 && degree>23)
        {
            instruction_list.add("Head towards North East");
            instruction_direction_list.add(Constants.HEAD_TOWARDS_NORTH_EAST);
        }

        else if(degree<=113 && degree>67)
        {
            instruction_list.add("Head towards East");
            instruction_direction_list.add(Constants.HEAD_TOWARDS_EAST);
        }
        else if(degree<=157 && degree>113)
        {
            instruction_list.add("Head towards South East");
            instruction_direction_list.add(Constants.HEAD_TOWARDS_SOUTH_EAST);
        }
        else if(degree<=180 && degree>157)
        {
            instruction_list.add("Head towards South");
            instruction_direction_list.add(Constants.HEAD_TOWARDS_SOUTH);
        }

        else
        {
            instruction_list.add("Finding Angle"+degree);
            instruction_direction_list.add(Constants.HEAD_TOWARDS_SOUTH);
        }









    }


    public void destination_generate_dynamic_instruction_initial(String site1,String site2)
    {
        destination_instruction_site_list.add(site1);
        float[] src_coord = get_node_coordinate(site1);
        float[] dest_coord = get_node_coordinate(site2);
        float delta_x = dest_coord[0] - src_coord[0];
        float delta_z = dest_coord[2] - src_coord[2];
        float delta_y = 0;


        float vlen = android.opengl.Matrix.length(delta_x, delta_y, delta_z);
        delta_x /= vlen;
        delta_y /= vlen;
        delta_z /= vlen;

        float[] base_vector = new float[]{0,0,1};
        float[] generated_vector = new float[] {delta_x,delta_y,delta_z};

        //angle
        float var1 = (base_vector[0]*generated_vector[0])+(base_vector[2]*generated_vector[2]);
        float var2 = (float)(Math.sqrt((base_vector[0]*base_vector[0])+(base_vector[2]*base_vector[2]))*Math.sqrt((generated_vector[0]*generated_vector[0])+(generated_vector[2]*generated_vector[2])));

        double angla = Math.acos(var1/var2);
        double angle_degree = Math.toDegrees(angla);
        float degree = (float)(-angle_degree-45);
        if(degree<=23 && degree>-23)
        {
            destination_instruction_list.add("Head towards North ");
            destination_instruction_direction_list.add(Constants.HEAD_TOWARDS_NORTH);
        }
        else if(degree<=-23&& degree>-67)
        {
            destination_instruction_list.add("Head towards North West ");
            destination_instruction_direction_list.add(Constants.HEAD_TOWARDS_NORTH_WEST);
        }
        else if(degree<=-67 && degree>-113)
        {
            destination_instruction_list.add("Head towards  West ");
            destination_instruction_direction_list.add(Constants.HEAD_TOWARDS_WEST);
        }
        else if(degree<=-113 && degree>-157)
        {
            destination_instruction_list.add("Head towards  South West ");
            destination_instruction_direction_list.add(Constants.HEAD_TOWARDS_SOUTH_WEST);
        }
        else if(degree<=-157 && degree>-203)
        {
            destination_instruction_list.add("Head towards South");
            destination_instruction_direction_list.add(Constants.HEAD_TOWARDS_SOUTH);
        }
        else if(degree<=-203 && degree>-247)
        {
            destination_instruction_list.add("Head towards South East");
            destination_instruction_direction_list.add(Constants.HEAD_TOWARDS_SOUTH_EAST);
        }
        else if(degree<=-247 && degree>-293)
        {
            destination_instruction_list.add("Head towards East");
            destination_instruction_direction_list.add(Constants.HEAD_TOWARDS_EAST);
        }
        else if(degree<=-293 && degree>-337)
        {
            destination_instruction_list.add("Head towards North East");
            destination_instruction_direction_list.add(Constants.HEAD_TOWARDS_NORTH_EAST);
        }
        else if(degree<=-337 && degree>=360)
        {
            destination_instruction_list.add("Head towards North ");
            destination_instruction_direction_list.add(Constants.HEAD_TOWARDS_NORTH);
        }


        else if(degree<=67 && degree>23)
        {
            destination_instruction_list.add("Head towards North East");
            destination_instruction_direction_list.add(Constants.HEAD_TOWARDS_NORTH_EAST);
        }

        else if(degree<=113 && degree>67)
        {
            destination_instruction_list.add("Head towards East");
            destination_instruction_direction_list.add(Constants.HEAD_TOWARDS_EAST);
        }
        else if(degree<=157 && degree>113)
        {
            destination_instruction_list.add("Head towards South East");
            destination_instruction_direction_list.add(Constants.HEAD_TOWARDS_SOUTH_EAST);
        }
        else if(degree<=180 && degree>157)
        {
            destination_instruction_list.add("Head towards South");
            destination_instruction_direction_list.add(Constants.HEAD_TOWARDS_SOUTH);
        }

        else
        {
            destination_instruction_list.add("Finding Angle"+degree);
            destination_instruction_direction_list.add(Constants.HEAD_TOWARDS_SOUTH);
        }









    }

    public void generate_dynamic_instruction_intermediate(String prev_site1,String site1,String site2)
    {

        float[] prev_src_coord = get_node_coordinate(prev_site1);
        float[] src_coord =  get_node_coordinate(site1);
        float[] dest_coord = get_node_coordinate(site2);

       /* float m1 = (prev_src_coord[0]-src_coord[0])/(prev_src_coord[2]-src_coord[2]);
        float m2 =  (src_coord[0]-dest_coord[0])/(src_coord[2]-dest_coord[2]);

        double angla = Math.atan((m2-m1)/(1+m1*m2));
        double angle_degree = Math.toDegrees(angla);

        instruction_list.add("Turn Left"+(m2-m1)/(1+m1*m2));*/


        float[] base_vector = new float[]{src_coord[0]-prev_src_coord[0],0,src_coord[2]-prev_src_coord[2]};
        /*float base_vlen = android.opengl.Matrix.length(base_vector[0],0, base_vector[2]);
        base_vector[0] /= base_vlen;
        base_vector[1] /= base_vlen;
        base_vector[2] /= base_vlen;*/

        float[] next_vector = new float[]{dest_coord[0]-src_coord[0],0,dest_coord[2]-src_coord[2]};
        /*float next_vlen = android.opengl.Matrix.length(next_vector[0],0, next_vector[2]);
        next_vector[0] /= next_vlen;
        next_vector[1] /= next_vlen;
        next_vector[2] /= next_vlen;*/




        //angle
        //float var1 = (base_vector[0]*next_vector[0])+(base_vector[2]*next_vector[2]); //cos
        float var1 = (base_vector[0]*next_vector[2])-(base_vector[2]*next_vector[0]);
        float var2 = (float)(Math.sqrt((base_vector[0]*base_vector[0])+(base_vector[2]*base_vector[2]))*Math.sqrt((next_vector[0]*next_vector[0])+(next_vector[2]*next_vector[2])));

        double angla = Math.asin(var1/var2);
        double angle_degree = Math.toDegrees(angla);
        // instruction_list.add("Turn Left"+angle_degree);
        System.out.println(""+angle_degree);

        if(angle_degree>=0)
        {
            if(angle_degree<=20 && !sequential_straight_status)
            {
                sequential_straight_status = true;
                instruction_site_list.add(site1);
                instruction_list.add("Take Straight");
                instruction_direction_list.add(Constants.TAKE_STRAIGHT);
            }
            else if(angle_degree>20 && angle_degree<=40)
            {
                sequential_straight_status = false;
                instruction_site_list.add(site1);
                instruction_list.add("Take Slight Right");
                instruction_direction_list.add(Constants.TAKE_SLIGHT_RIGHT);
            }
            else if(angle_degree>40 && angle_degree<=90)
            {
                sequential_straight_status = false;
                instruction_site_list.add(site1);
                instruction_list.add("Turn Right");
                instruction_direction_list.add(Constants.TURN_RIGHT);
            }

        }

        if(angle_degree<0)
        {
            if(angle_degree>=-20  && !sequential_straight_status)
            {
                sequential_straight_status = true;
                instruction_site_list.add(site1);
                instruction_list.add("Take Straight");
                instruction_direction_list.add(Constants.TAKE_STRAIGHT);
            }
            else if(angle_degree<-20 && angle_degree>=-40)
            {
                sequential_straight_status = false;
                instruction_site_list.add(site1);
                instruction_list.add("Take Slight Left");
                instruction_direction_list.add(Constants.TAKE_SLIGHT_LEFT);

            }
            else if(angle_degree<-40 && angle_degree>=-90)
            {
                sequential_straight_status = false;
                instruction_site_list.add(site1);
                instruction_list.add("Turn Left");
                instruction_direction_list.add(Constants.TURN_LEFT);

            }

        }







    }


    public void destination_generate_dynamic_instruction_intermediate(String prev_site1,String site1,String site2)
    {

        float[] prev_src_coord = get_node_coordinate(prev_site1);
        float[] src_coord =  get_node_coordinate(site1);
        float[] dest_coord = get_node_coordinate(site2);

       /* float m1 = (prev_src_coord[0]-src_coord[0])/(prev_src_coord[2]-src_coord[2]);
        float m2 =  (src_coord[0]-dest_coord[0])/(src_coord[2]-dest_coord[2]);

        double angla = Math.atan((m2-m1)/(1+m1*m2));
        double angle_degree = Math.toDegrees(angla);

        instruction_list.add("Turn Left"+(m2-m1)/(1+m1*m2));*/


        float[] base_vector = new float[]{src_coord[0]-prev_src_coord[0],0,src_coord[2]-prev_src_coord[2]};
        /*float base_vlen = android.opengl.Matrix.length(base_vector[0],0, base_vector[2]);
        base_vector[0] /= base_vlen;
        base_vector[1] /= base_vlen;
        base_vector[2] /= base_vlen;*/

        float[] next_vector = new float[]{dest_coord[0]-src_coord[0],0,dest_coord[2]-src_coord[2]};
        /*float next_vlen = android.opengl.Matrix.length(next_vector[0],0, next_vector[2]);
        next_vector[0] /= next_vlen;
        next_vector[1] /= next_vlen;
        next_vector[2] /= next_vlen;*/




        //angle
        //float var1 = (base_vector[0]*next_vector[0])+(base_vector[2]*next_vector[2]); //cos
        float var1 = (base_vector[0]*next_vector[2])-(base_vector[2]*next_vector[0]);
        float var2 = (float)(Math.sqrt((base_vector[0]*base_vector[0])+(base_vector[2]*base_vector[2]))*Math.sqrt((next_vector[0]*next_vector[0])+(next_vector[2]*next_vector[2])));

        double angla = Math.asin(var1/var2);
        double angle_degree = Math.toDegrees(angla);
        // instruction_list.add("Turn Left"+angle_degree);
        System.out.println(""+angle_degree);

        if(angle_degree>=0)
        {
            if(angle_degree<=20 && !sequential_straight_status)
            {
                sequential_straight_status = true;
                destination_instruction_site_list.add(site1);
                destination_instruction_list.add("Take Straight");
                destination_instruction_direction_list.add(Constants.TAKE_STRAIGHT);
            }
            else if(angle_degree>20 && angle_degree<=40)
            {
                sequential_straight_status = false;
                destination_instruction_site_list.add(site1);
                destination_instruction_list.add("Take Slight Right");
                destination_instruction_direction_list.add(Constants.TAKE_SLIGHT_RIGHT);
            }
            else if(angle_degree>40 && angle_degree<=90)
            {
                sequential_straight_status = false;
                destination_instruction_site_list.add(site1);
                destination_instruction_list.add("Turn Right");
                destination_instruction_direction_list.add(Constants.TURN_RIGHT);
            }

        }

        if(angle_degree<0)
        {
            if(angle_degree>=-20  && !sequential_straight_status)
            {
                sequential_straight_status = true;
                destination_instruction_site_list.add(site1);
                destination_instruction_list.add("Take Straight");
                destination_instruction_direction_list.add(Constants.TAKE_STRAIGHT);
            }
            else if(angle_degree<-20 && angle_degree>=-40)
            {
                sequential_straight_status = false;
                destination_instruction_site_list.add(site1);
                destination_instruction_list.add("Take Slight Left");
                destination_instruction_direction_list.add(Constants.TAKE_SLIGHT_LEFT);

            }
            else if(angle_degree<-40 && angle_degree>=-90)
            {
                sequential_straight_status = false;
                destination_instruction_site_list.add(site1);
                destination_instruction_list.add("Turn Left");
                destination_instruction_direction_list.add(Constants.TURN_LEFT);

            }

        }







    }


    public float[] crossProduct(float[] a, float[] b) {
        // AxB = (AyBz − AzBy, AzBx − AxBz, AxBy − AyBx)
        //(r)[0] = (a)[1] * (b)[2] - (b)[1] * (a)[2]; \
        //(r)[1] = (a)[2] * (b)[0] - (b)[2] * (a)[0]; \
        //(r)[2] = (a)[0] * (b)[1] - (b)[0] * (a)[1];
        float x = a[1] * b[2] - a[2] * b[1];
        float y = a[2] * b[0] - a[0] * b[2];
        float z = a[0] * b[1] - a[1] * b[0];
        return new float[]{x, y, z};
    }


    public float[] calories_calculation(String site_list) {
        float ret_calories = 0.0f;
        float dist = 0;
        String site_list_array[];
        site_list_array = site_list.split(",");
        if (site_list_array.length > 1) {
            for (int i = 0; i < site_list_array.length - 1; i++) {
                String src_site = site_list_array[i];
                String dest_site = site_list_array[i + 1];
                dist += calculate_distance(src_site, dest_site);
            }
        }

        float number_of_step = dist * 34;
        ret_calories = .035f * number_of_step;
        return new float[]{ret_calories,number_of_step};

    }

    public float calculate_distance(String src_site, String dest_site) {
        float[] src_coord = get_node_coordinate(src_site);
        float[] dest_coord = get_node_coordinate(dest_site);

        float distance = 0;
        float delta_x = dest_coord[0] - src_coord[0];
        float delta_z = dest_coord[2] - src_coord[2];
        float xc = ((delta_x * delta_x) + (delta_z * delta_z));
        distance = (float) Math.sqrt(xc);


        return distance;
    }


    public float[] get_node_coordinate(String site_id) {
        float node_x = 0, node_y = 0, node_z = 0;
        //List<PathNode> node = MyApplication.Companion.get().getDb().pathNodeList().findById(site_id, String.valueOf(Helper.hex2decimal(site_id.substring(8,13).toString())));
        List<PathNode> node = DatabaseClient.Companion.getInstance(ApplicationContext.get().getApplicationContext()).getDb().pathNodeList().findById(site_id, String.valueOf(Helper.hex2decimal(site_id.substring(8,13).toString())));
        if (node != null && node.size() != 0) {


            node_x = Float.valueOf(node.get(0).getSite_map_coord_x());
            node_y = Float.valueOf(node.get(0).getSite_map_coord_y());
            node_z = Float.valueOf(node.get(0).getSite_map_coord_z());

        }

        float[] temp = new float[]{node_x, node_y, node_z};

        return temp;

    }


    public void reset_splited_path() {
        splited_path_source.clear();
        splited_path_dest.clear();
        selected_floor_path_mapping.clear();
    }


    public void make_combine_graph_path(String source_site_id, String source_connector_site_id, String dest_connector_site_id, String destination_site_id) {

        p = GraphSearchProblem
                .startingFrom(source_site_id)
                .in(graph)
                .takeCostsFromEdges()
                .build();
        String process_path = Hipster.createDijkstra(p).search(source_connector_site_id).toString();
        String process_path_new = ((process_path.replace("[", "")).replace("]", "")).replace(" ", "");
        String process_new_latest = process_path_new.replace("\n", "");
        //splited_path.add(process_new_latest);
        splited_path_source.add(process_new_latest);

        p = GraphSearchProblem
                .startingFrom(dest_connector_site_id)
                .in(graph1)
                .takeCostsFromEdges()
                .build();
        String process_path1 = Hipster.createDijkstra(p).search(destination_site_id).toString();
        String process_path_new1 = ((process_path1.replace("[", "")).replace("]", "")).replace(" ", "");
        String process_new_latest1 = process_path_new1.replace("\n", "");
        //splited_path.add(process_new_latest1);
        splited_path_dest.add(process_new_latest1);
        // path_node_array = process_new_latest.split(",");


    }


    public void setGraph() {

        GraphBuilder<String, Double> gb = new GraphBuilder<>();
        for (int i = 0; i < node_connection_list.size(); i++) {
            String temp_key = node_connection_list.keySet().toArray()[i].toString();
            for (int j = 0; j < node_connection_list.get(temp_key).size(); j++) {
                gb.connect(temp_key).to(node_connection_list.get(temp_key).get(j)).withEdge(1d);
            }
            //gb.connect(node_connection_list.keySet().toArray()[i].toString()).to(node_connection_list.get(node_connection_list.keySet().toArray()[i].toString())).withEdge(1d);

        }
        graph = gb.createDirectedGraph();
    }

    public void setGraph1() {

        GraphBuilder<String, Double> gb = new GraphBuilder<>();
        for (int i = 0; i < node_connection_list_dest.size(); i++) {
            String temp_key = node_connection_list_dest.keySet().toArray()[i].toString();
            for (int j = 0; j < node_connection_list_dest.get(temp_key).size(); j++) {
                gb.connect(temp_key).to(node_connection_list_dest.get(temp_key).get(j)).withEdge(1d);
            }
            //gb.connect(node_connection_list.keySet().toArray()[i].toString()).to(node_connection_list.get(node_connection_list.keySet().toArray()[i].toString())).withEdge(1d);

        }
        graph1 = gb.createDirectedGraph();
    }


    private void initLayer() {
        this.routeWidth = 10;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        routeStartBmp = BitmapFactory.decodeResource(mapView.getResources(),
                R.drawable.mark_touch);
        routeEndBmp = BitmapFactory.decodeResource(mapView.getResources(),
                R.drawable.mark_touch);

        splited_path_dest = new ArrayList<String>();
        splited_path_source = new ArrayList<String>();

        /*graph =
                GraphBuilder.<String,Double>create()
                        .connect("a7000000000000000000000000000000").to("a8000000000000000000000000000000").withEdge(1d)
                        .connect("a8000000000000000000000000000000").to("a9000000000000000000000000000000").withEdge(1d)
                        .connect("a9000000000000000000000000000000").to("aa000000000000000000000000000000").withEdge(1d)
                        .connect("aa000000000000000000000000000000").to("ab000000000000000000000000000000").withEdge(1d)
                        .connect("ab000000000000000000000000000000").to("a2000000000000000000000000000000").withEdge(1d)
                        .connect("ab000000000000000000000000000000").to("a3000000000000000000000000000000").withEdge(1d)
                        .connect("a3000000000000000000000000000000").to("a4100101000128010000101000010100").withEdge(1d)
                        .connect("a3000000000000000000000000000000").to("a5100102000128010000101000010100").withEdge(1d)
                       .createDirectedGraph();
                       */


        //String process_path = Hipster.createDijkstra(p).search(nodeName.get(num)).toString();
        selected_index_of_splitted_path = -1;
        selected_floor_path_mapping = new HashMap<Integer, String>();
    }

    public void set_destination_node(String num) {
        my_target_beacon = num;
        //my_target_index = num;
        //String process_path = Hipster.createDijkstra(p).search(nodeName.get(num)).toString();
        String process_path = Hipster.createDijkstra(p).search(num).toString();
        String process_path_new = ((process_path.replace("[", "")).replace("]", "")).replace(" ", "");
        String process_new_latest = process_path_new.replace("\n", "");
        path_node_array = process_new_latest.split(",");
    }

    public void set_source_node(String beacon_id) {


        p = GraphSearchProblem
                .startingFrom(beacon_id)
                .in(graph)
                .takeCostsFromEdges()
                .build();
        my_source_beacon = beacon_id;
        if (my_target_beacon != null) {
            set_destination_node(my_target_beacon);
        }


    }

    @Override
    public void onTouch(MotionEvent event) {

    }

    @Override
    public void draw(Canvas canvas, Matrix currentMatrix, float currentZoom, float
            currentRotateDegrees) {
        int active_next_index;
        if (path_node_array != null) {
            canvas.translate(0, 0);
            canvas.save();

            for (int i = 0; i < path_node_array.length - 1; i++) {
                int active_index = path_node.node_key.indexOf(path_node_array[i]);

                // mPath.moveTo(path_node.node_cordinate.get(active_index).x,path_node.node_cordinate.get(active_index).y);
                active_next_index = path_node.node_key.indexOf(path_node_array[i + 1]);

                float[] goal11 = {path_node.node_cordinate.get(active_index).x,
                        path_node.node_cordinate.get(active_index).y};
                float[] goal21 = {path_node.node_cordinate.get(active_next_index).x,
                        path_node.node_cordinate.get(active_next_index).y};
                currentMatrix.mapPoints(goal11);
                currentMatrix.mapPoints(goal21);
                paint.setStrokeWidth(routeWidth);
                canvas.drawLine(goal11[0], goal11[1], goal21[0], goal21[1], paint);
            }
            canvas.restore();
        }


    }

    public void setNodeList(List<PointF> nodeList) {
        this.nodeList = nodeList;
    }

    public void setRouteList(List<Integer> routeList) {
        this.routeList = routeList;
    }
}
