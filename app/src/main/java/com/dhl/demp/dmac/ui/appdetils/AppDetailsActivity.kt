package com.dhl.demp.dmac.ui.appdetils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.dhl.demp.dmac.ext.inFragmentTransaction
import dagger.hilt.android.AndroidEntryPoint
import mydmac.R

@AndroidEntryPoint
class AppDetailsActivity : AppCompatActivity(R.layout.activity_app_details) {
    companion object {
        private const val EXTRA_APP_ID = "app_id"

        fun start(context: Context, appId: String, options: Bundle? = null) {
            val intent = Intent(context, AppDetailsActivity::class.java)
                .putExtra(EXTRA_APP_ID, appId)

            context.startActivity(intent, options)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar()

        if (savedInstanceState == null) {
            val appId = intent.getStringExtra(EXTRA_APP_ID)
                ?: throw IllegalStateException("app_id is required")

            inFragmentTransaction {
                replace(R.id.fragmentContainer, AppDetailsFragment.newInstance(appId))
            }
        }
    }

    private fun setupToolbar() {
        with (findViewById<Toolbar>(R.id.toolbar)) {
            setNavigationIcon(R.drawable.ic_back)
            setSupportActionBar(this)
            
            setNavigationOnClickListener {
                supportFinishAfterTransition()
            }
        }
    }
}