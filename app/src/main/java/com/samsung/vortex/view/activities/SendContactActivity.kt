package com.samsung.vortex.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.databinding.ActivitySendContactBinding
import com.samsung.vortex.model.PhoneContact
import com.samsung.vortex.model.User
import com.samsung.vortex.utils.FirebaseUtils
import com.samsung.vortex.utils.Utils
import com.squareup.picasso.Picasso

class SendContactActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySendContactBinding
    private lateinit var contactImage: String
    private lateinit var contactName: String
    private lateinit var contactPhone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isViewContact: Boolean = intent.getBooleanExtra("IsViewContact", false)
        val isViewContactFromProfile: Boolean = intent.getBooleanExtra(getString(R.string.isviewcontactfromprofile), false)

        val name: String? = intent.getStringExtra(getString(R.string.NAME))
        val phone: String? = intent.getStringExtra(getString(R.string.PHONE))
        val image: String? = intent.getStringExtra(getString(R.string.IMAGE))
        val receiver: String? = intent.getStringExtra(getString(R.string.VISIT_USER_ID))

        setupUI(name, phone, image, isViewContact)

        if (isViewContactFromProfile) {
            binding.sendContact.visibility = View.GONE
            binding.checkBox.visibility = View.GONE
            val contactId = intent.getStringExtra("contactId")
            VortexApplication.userDatabaseReference.child(contactId!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val user = snapshot.getValue(User::class.java)!!

                            binding.name.text = user.name
                            binding.phoneNumber.text = user.phone_number
                            Picasso.get().load(user.image).into(binding.image)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
            })
        } else if (!isViewContact) {
            binding.sendContact.setOnClickListener {
                if (image == null)
                    FirebaseUtils.sendContact(PhoneContact(phone!!, name!!), Utils.currentUser!!.uid, receiver!!)
                else
                    FirebaseUtils.sendContact(PhoneContact(phone!!, name!!, image), Utils.currentUser!!.uid, receiver!!)
                finish()
            }
        } else {
            binding.sendContact.visibility = View.GONE
            binding.addContact.visibility = View.VISIBLE
            val contactId = intent.getStringExtra("contactId")
            contactId?.let {
                VortexApplication.contactsDatabaseReference.child(it)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            contactImage = snapshot.child(getString(R.string.IMAGE)).getValue(String::class.java).toString()
                            contactName = snapshot.child(getString(R.string.NAME)).getValue(String::class.java).toString()
                            contactPhone = snapshot.child(getString(R.string.PHONE)).getValue(String::class.java).toString()

                            binding.name.text = contactName
                            binding.phoneNumber.text = contactPhone
                            Picasso.get().load(contactImage).into(binding.image)
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }

            binding.addContact.setOnClickListener {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@SendContactActivity)
                builder
                    .setMessage("Create a new contact or add to an existing contact?")
                    .setTitle("Save Contact")
                    .setPositiveButton("New") { _, _ ->
                        val intent = Intent(Intent.ACTION_INSERT)
                        intent.type = ContactsContract.Contacts.CONTENT_TYPE
                        intent.putExtra(ContactsContract.Intents.Insert.NAME,contactName)
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE,contactPhone)
                        this@SendContactActivity.startActivity(intent)
                    }
                    .setNegativeButton("Existing") { _, _ ->
                        val intent = Intent(Intent.ACTION_INSERT_OR_EDIT)
                        intent.type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
                        intent.putExtra(ContactsContract.Intents.Insert.NAME,contactName)
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE,contactPhone)
                        this@SendContactActivity.startActivity(intent)
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun setupUI(name: String?, phone: String?, image: String?, isViewContact: Boolean?) {
        if (isViewContact == true)
            binding.chatToolBar.mainAppBar.title = "View Contact"
        else
            binding.chatToolBar.mainAppBar.title = "Send Contact"

        binding.name.text = name
        binding.phoneNumber.text = phone
        Picasso.get().load(image).into(binding.image)
    }
}