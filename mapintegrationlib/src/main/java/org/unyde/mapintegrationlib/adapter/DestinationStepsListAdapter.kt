package org.unyde.mapintegrationlib.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.unyde.mapintegrationlib.R
import org.unyde.mapintegrationlib.interfaces.StepsClickList
import org.unyde.mapintegrationlib.util.Constants
import java.util.ArrayList

class DestinationStepsListAdapter(internal var instruction_list: MutableList<String>?
                      ,internal var  instruction_site_list :MutableList<String>?
                      ,internal var  instruction_direction_list :MutableList<Int>?
                      ,internal var context: Context
) : RecyclerView.Adapter<DestinationStepsListAdapterViewholder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationStepsListAdapterViewholder {
        val view = LayoutInflater.from(context).inflate(R.layout.steps_list_item, parent, false)

        return DestinationStepsListAdapterViewholder(view)
    }

    override fun getItemCount(): Int {

         if (instruction_list?.size != 0) {
             return  instruction_list!!.size
        } else {
            return 0
        }
    }

    override fun onBindViewHolder(holder: DestinationStepsListAdapterViewholder, position: Int) {



        when (instruction_direction_list!!.get(position)) {
            Constants.DESTINATION -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.location_current_map)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }

            Constants.HEAD_TOWARDS_EAST -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.head_towards_east_black)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }

            Constants.HEAD_TOWARDS_NORTH -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.head_towards_north_black)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }
            Constants.HEAD_TOWARDS_NORTH_EAST -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.head_towards_north_east_black)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }
            Constants.HEAD_TOWARDS_NORTH_WEST -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.head_towards_north_west_black)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }
            Constants.HEAD_TOWARDS_SOUTH -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.head_towards_south_black)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }
            Constants.HEAD_TOWARDS_SOUTH_EAST -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.head_towards_south_east_black)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }
            Constants.HEAD_TOWARDS_SOUTH_WEST -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.head_towards_south_west_black)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }
//            Constants.HEAD_TOWARDS_SOUTH_WEST -> {
//                holder.instruction_image.background = context.resources.getDrawable(R.drawable.location_current_map)
//                holder.instruction_text.setText(instruction_list!!.get(position))
//            }
            Constants.HEAD_TOWARDS_WEST -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.head_towards_west__black)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }
            Constants.TAKE_SLIGHT_LEFT -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.take_slight_left_black)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }
            Constants.TAKE_SLIGHT_RIGHT -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.take_slight_right_black)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }
            Constants.TAKE_STRAIGHT -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.take_straight_black)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }
            Constants.TURN_LEFT -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.turn_left_black)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }
            Constants.TURN_RIGHT -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.turn_right_black)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }

            Constants.LIFT -> {
                holder.instruction_image.background = context.resources.getDrawable(R.drawable.lift_icon_black)
                holder.instruction_text.setText(instruction_list!!.get(position))
            }


        }


    }
}

class DestinationStepsListAdapterViewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var instruction_text : TextView
    var instruction_image : ImageView


    init {
        instruction_text = itemView.findViewById(R.id.instruction_text)
        instruction_image = itemView.findViewById(R.id.instruction_image)


    }
}
