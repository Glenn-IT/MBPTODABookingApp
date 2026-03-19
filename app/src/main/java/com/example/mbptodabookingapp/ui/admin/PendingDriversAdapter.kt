package com.example.mbptodabookingapp.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mbptodabookingapp.data.models.PendingDriver
import com.example.mbptodabookingapp.databinding.ItemPendingDriverBinding

class PendingDriversAdapter(
    private val onApprove: (PendingDriver) -> Unit,
    private val onReject:  (PendingDriver) -> Unit
) : ListAdapter<PendingDriver, PendingDriversAdapter.VH>(DiffCb()) {

    inner class VH(val b: ItemPendingDriverBinding) : RecyclerView.ViewHolder(b.root)

    class DiffCb : DiffUtil.ItemCallback<PendingDriver>() {
        override fun areItemsTheSame(o: PendingDriver, n: PendingDriver) = o.id == n.id
        override fun areContentsTheSame(o: PendingDriver, n: PendingDriver) = o == n
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPendingDriverBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = getItem(position)
        holder.b.tvName.text      = d.name
        holder.b.tvEmail.text     = d.email
        holder.b.tvLicenseNo.text = "License: ${d.license_no ?: "—"}"
        holder.b.tvVehicleNo.text = "Vehicle: ${d.vehicle_no ?: "—"}"
        holder.b.btnApprove.setOnClickListener { onApprove(d) }
        holder.b.btnReject.setOnClickListener  { onReject(d) }
    }
}

