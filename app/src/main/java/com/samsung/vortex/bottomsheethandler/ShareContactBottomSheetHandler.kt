package com.samsung.vortex.bottomsheethandler

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.ListView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.adapters.PhoneContactAdapter
import com.samsung.vortex.model.PhoneContact
import com.samsung.vortex.model.User
import com.samsung.vortex.utils.Utils.Companion.TAG

class ShareContactBottomSheetHandler {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var bottomSheetDialog: BottomSheetDialog
        private lateinit var phoneContacts: ArrayList<PhoneContact>

        private lateinit var adapter: PhoneContactAdapter

        private lateinit var users: ArrayList<User>
        private lateinit var receiver: String

        fun start(context: Context, messageReceiverId: String) {
            val contentView = View.inflate(context, R.layout.share_contact_bottom_sheet_layout, null)
            bottomSheetDialog = BottomSheetDialog(context)
            bottomSheetDialog.setContentView(contentView)
            bottomSheetDialog.show()
            phoneContacts = ArrayList()
            users = ArrayList()
            receiver = messageReceiverId
            userDatabaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.value != null) {
                        for (dataSnapshot in snapshot.children) {
                            if (dataSnapshot.hasChild(
                                    VortexApplication.application.getString(R.string.NAME)
                                )
                            ) {
                                val user: User = dataSnapshot.getValue(User::class.java)!!
                                if (user.uid != FirebaseAuth.getInstance().uid) {
                                    users.add(user)
                                }
                            }
                        }
                    }
                    Log.i(TAG, "onDataChange: $users")
                    loadContactListFromPhone()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        @SuppressLint("Range")
        private fun loadContactListFromPhone() {
            @SuppressLint("Recycle") val phones: Cursor? = VortexApplication.application.applicationContext.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
            while (phones?.moveToNext() == true) {
                val phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))
                if (phone != null) {
                    val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val user: User? = getUserByPhone(users, phone)
                    val phoneContact = PhoneContact(phone)
                    if (name != null)
                        phoneContact.name = name
                    if (user != null) {
                        if (user.image.isNotEmpty())
                            phoneContact.image = user.image
                        if (user.status.isNotEmpty())
                            phoneContact.status = user.status
                    }
                    if (!isNumberExist(name)) phoneContacts.add(phoneContact)
                }
            }
            setupListView()
        }

        private fun isNumberExist(name: String?): Boolean {
            for (phoneContact in phoneContacts) {
                if (phoneContact.name == name) return true
            }
            return false
        }

        private fun getUserByPhone(users: ArrayList<User>, phone: String): User? {
            for (user in users) {
                if (user.phone_number == phone) return user
            }
            return null
        }

        private fun setupListView() {
            adapter = PhoneContactAdapter(VortexApplication.application, phoneContacts, receiver)
            val contactList = bottomSheetDialog.findViewById<ListView>(R.id.contactList)!!
            contactList.adapter = adapter
        }
    }
}