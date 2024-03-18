package com.dhl.demp.dmac.ui.contacts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import mydmac.R

class ContactsActivity : AppCompatActivity(R.layout.activity_contacts) {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ContactsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar()

        findViewById<TextView>(R.id.contact_info).text = HtmlCompat.fromHtml(getString(R.string.contact_info_html), HtmlCompat.FROM_HTML_MODE_LEGACY)
        findViewById<TextView>(R.id.feedback_info).text = HtmlCompat.fromHtml(getString(R.string.feedback_info_html), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setBackgroundColor(getColor(R.color.build_type_color))

        setSupportActionBar(toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}