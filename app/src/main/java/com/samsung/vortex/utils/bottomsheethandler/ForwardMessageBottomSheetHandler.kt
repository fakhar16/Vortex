package com.samsung.vortex.utils.bottomsheethandler

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.samsung.vortex.R
import com.samsung.vortex.adapters.ContactAdapter
import com.samsung.vortex.model.Message
import com.samsung.vortex.model.User
import com.samsung.vortex.repository.ContactsRepositoryImpl
import com.samsung.vortex.utils.FirebaseUtils
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.view.activities.ChatActivity
import java.util.Locale

class ForwardMessageBottomSheetHandler {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var bottomSheetDialog: BottomSheetDialog
        private lateinit var message: Message
        @SuppressLint("StaticFieldLeak")
        private lateinit var mContext: Context
        @SuppressLint("StaticFieldLeak")
        private lateinit var adapter: ContactAdapter

        fun start(context: Context, msg: Message) {
            val contentView = View.inflate(context, R.layout.forward_message_bottom_sheet_layout, null)
            message = msg
            mContext = context
            bottomSheetDialog = BottomSheetDialog(context)
            bottomSheetDialog.setContentView(contentView)
            bottomSheetDialog.setCanceledOnTouchOutside(false)
            bottomSheetDialog.show()
            val cancel = bottomSheetDialog.findViewById<TextView>(R.id.cancel)
            val search = bottomSheetDialog.findViewById<SearchView>(R.id.search)
            val ll = bottomSheetDialog.findViewById<LinearLayout>(R.id.upperBar)

            showContactList(bottomSheetDialog, context)

            //Cancel button handler
            cancel!!.setOnClickListener {
                if (ll!!.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                    ll.layoutDirection = View.LAYOUT_DIRECTION_LTR
                    (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
                    search!!.clearFocus()
                } else {
                    bottomSheetDialog.dismiss()
                }
            }

            //search focus handler
            search!!.setOnQueryTextFocusChangeListener { _: View?, b: Boolean ->
                if (b) ll!!.layoutDirection = View.LAYOUT_DIRECTION_RTL
            }


            //search filter handler
            search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    filter(newText)
                    return true
                }
            })
        }

        private fun filter(text: String) {
            val filteredList: ArrayList<User> = ArrayList()
            for (item in ContactsRepositoryImpl.getInstance().getContacts().value!!) {
                if (item.name.lowercase(Locale.ROOT).contains(text.lowercase(Locale.getDefault()))) {
                    filteredList.add(item)
                }
            }
            if (filteredList.isNotEmpty()) {
                adapter.filterList(filteredList)
            } else {
                adapter.filterList(ArrayList())
            }
        }

        private fun showContactList(dialog: BottomSheetDialog?, context: Context) {
            val contactList = dialog!!.findViewById<RecyclerView>(R.id.contactList)!!
            contactList.layoutManager = LinearLayoutManager(context)
            adapter = ContactAdapter(context, ContactsRepositoryImpl.getInstance().getContacts().value!!)
            contactList.addItemDecoration(DividerItemDecoration(contactList.context, DividerItemDecoration.VERTICAL))
            contactList.adapter = adapter
        }

        fun forwardMessage(context: Context, receiver: String) {
            when (message.type) {
                context.getString(R.string.TEXT) -> FirebaseUtils.sendMessage(message.message, Utils.currentUser!!.uid, receiver)
                context.getString(R.string.IMAGE) -> {
                    FirebaseUtils.forwardImage(context, message, receiver, "")
                }
                context.getString(R.string.VIDEO) -> {
                    FirebaseUtils.forwardVideo(context, message, receiver, "")
                }
                context.getString(R.string.PDF_FILES) -> {
        //                FirebaseUtils.forwardDoc(context, message, receiver, "")
                }
            }
            bottomSheetDialog.dismiss()
            sendUserToChatActivity(receiver)
        }

        private fun sendUserToChatActivity(receiver: String) {
            val intent = Intent(mContext, ChatActivity::class.java)
            intent.putExtra(mContext.getString(R.string.VISIT_USER_ID), receiver)
            mContext.startActivity(intent)
        }
    }
}