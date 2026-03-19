package com.example.mbptodabookingapp.ui.driver

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mbptodabookingapp.data.models.Booking
import com.example.mbptodabookingapp.databinding.ItemRideRequestBinding

class RideRequestsAdapter(
    private val onView: (Booking) -> Unit
) : ListAdapter<Booking, RideRequestsAdapter.VH>(DiffCb()) {

    inner class VH(val b: ItemRideRequestBinding) : RecyclerView.ViewHolder(b.root)

    class DiffCb : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(o: Booking, n: Booking) = o.id == n.id
        override fun areContentsTheSame(o: Booking, n: Booking) = o == n
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRideRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val b = getItem(position)
        holder.b.tvBookingId.text = "Booking #${b.id}"
        holder.b.tvPickup.text    = "Pickup: ${b.pickup_address}"
        holder.b.tvDropoff.text   = "Dropoff: ${b.dropoff_address}"
        holder.b.btnViewRequest.setOnClickListener { onView(b) }
    }
}

