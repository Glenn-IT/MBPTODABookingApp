package com.example.mbptodabookingapp.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mbptodabookingapp.data.models.AdminUser
import com.example.mbptodabookingapp.databinding.ItemUserBinding

class UsersAdapter(
    private val onActivate:   (AdminUser) -> Unit,
    private val onDeactivate: (AdminUser) -> Unit,
    private val onDelete:     (AdminUser) -> Unit
) : ListAdapter<AdminUser, UsersAdapter.VH>(DiffCb()) {

    inner class VH(val b: ItemUserBinding) : RecyclerView.ViewHolder(b.root)

    class DiffCb : DiffUtil.ItemCallback<AdminUser>() {
        override fun areItemsTheSame(o: AdminUser, n: AdminUser) = o.id == n.id
        override fun areContentsTheSame(o: AdminUser, n: AdminUser) = o == n
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val u = getItem(position)
        holder.b.tvName.text   = u.name
        holder.b.tvEmail.text  = u.email
        holder.b.tvRole.text   = "Role: ${u.role}"
        holder.b.tvStatus.text = "Status: ${u.status}"

        holder.b.btnActivate.isEnabled   = u.status == "inactive"
        holder.b.btnDeactivate.isEnabled = u.status == "active"

        holder.b.btnActivate.setOnClickListener   { onActivate(u) }
        holder.b.btnDeactivate.setOnClickListener { onDeactivate(u) }
        holder.b.btnDelete.setOnClickListener     { onDelete(u) }
    }
}

