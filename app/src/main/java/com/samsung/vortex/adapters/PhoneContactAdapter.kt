package com.samsung.vortex.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.samsung.vortex.R
import com.samsung.vortex.model.PhoneContact
import com.samsung.vortex.view.activities.SendContactActivity
import com.squareup.picasso.Picasso

class PhoneContactAdapter(context: Context, arrayList: ArrayList<PhoneContact>, private val receiverId: String) :
    ArrayAdapter<PhoneContact>(context, 0, arrayList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var currentItemView = convertView
        if (currentItemView == null) {
            currentItemView =
                LayoutInflater.from(context).inflate(R.layout.contacts_list_item, parent, false)
        }
        val currentPhoneContact: PhoneContact? = getItem(position)
        val image = currentItemView!!.findViewById<ImageView>(R.id.image)
        val name = currentItemView.findViewById<TextView>(R.id.name)
        val status = currentItemView.findViewById<TextView>(R.id.status)
        if (currentPhoneContact!!.image.isNotEmpty())
            Picasso.get().load(currentPhoneContact.image).placeholder(R.drawable.profile_image).into(image)
        name.text = currentPhoneContact.name
        status.text = currentPhoneContact.status
        val finalCurrentItemView = currentItemView
        currentItemView.setOnClickListener {
            val intent = Intent(finalCurrentItemView.context, SendContactActivity::class.java)
            intent.putExtra("name", currentPhoneContact.name)
            intent.putExtra("phone", currentPhoneContact.phone)
            intent.putExtra("image", currentPhoneContact.image)
            intent.putExtra(
                finalCurrentItemView.context.getString(R.string.VISIT_USER_ID),
                receiverId
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            finalCurrentItemView.context.startActivity(intent)
        }
        return currentItemView
    }
}