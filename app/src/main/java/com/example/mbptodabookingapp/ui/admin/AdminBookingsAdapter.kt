package com.example.mbptodabookingapp.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mbptodabookingapp.R
import com.example.mbptodabookingapp.data.models.Booking
import com.example.mbptodabookingapp.databinding.ItemAdminBookingBinding

class AdminBookingsAdapter : ListAdapter<Booking, AdminBookingsAdapter.VH>(DiffCb()) {

    inner class VH(val b: ItemAdminBookingBinding) : RecyclerView.ViewHolder(b.root)

    class DiffCb : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(o: Booking, n: Booking) = o.id == n.id
        override fun areContentsTheSame(o: Booking, n: Booking) = o == n
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemAdminBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val booking = getItem(position)
        val ctx = holder.b.root.context

        holder.b.tvBookingId.text     = "Booking #${booking.id}"
        holder.b.tvPassengerName.text = "Passenger: ${booking.passenger_name ?: "—"}"
        holder.b.tvPickup.text        = booking.pickup_address
        holder.b.tvDropoff.text       = booking.dropoff_address
        holder.b.tvDate.text          = booking.created_at.take(16)
        holder.b.tvStatus.text        = booking.status.replace("_", " ")
        holder.b.tvStatus.setTextColor(ContextCompat.getColor(ctx, statusColor(booking.status)))
    }

    private fun statusColor(status: String): Int = when (status) {
        "requested"   -> R.color.statusRequested
        "accepted"    -> R.color.statusAccepted
        "in_progress" -> R.color.statusInProgress
        "completed"   -> R.color.statusCompleted
        "rejected"    -> R.color.statusRejected
        "cancelled"   -> R.color.statusCancelled
        else          -> R.color.grey_text
    }
}
