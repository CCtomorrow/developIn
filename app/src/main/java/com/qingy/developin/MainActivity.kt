package com.qingy.developin

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import com.qingy.developin.databinding.ActivityMainBinding
import com.qingy.util.GLog

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    companion object {
        val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            GLog.e(
                TAG,
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCBmA66kUcrqnfUlT7UbETahPhEflnlA483n+P+sRa5/Fe1y5Ntobet+HHe91LiPJYODfucFbvme9MdVMAhNeej6TBwe6N6/R1TlZPMndUAb74cGKRMZw+97B1z7TI5K8tIQ45EOX6UBNxTcr+R2yvS7Fn11py7Dl27SRw1Np2AFgSTnVDaEjxA+K1cGINUTe9IKC01h/f82QXAWNBvF7k7YkrtzjlPOUAgTN30xrcFXyAUkhFzR7ruBONbj0DrA95WCTovDbPQGq2Wt2d7MOcdoL2WB6k534zRNsC9X15iP01emIZJZbKfdwgzQHvbtLtkj8lcC3P/AostWoSUJ5lPAgMBAAECggEAEJ9GVFvJ+ynUUOoLswdoFXc0uORT93HrbsENZ1+jgJuDTIgeEtPJUYEqaeTjNq16ubbFbRHElh2PyJVUR244leeVFPhm0aGhn1TyLi6YYYPzqHrop/wYIs4kQPhNltOtMUmo98tAFxBAiluPgs/P4S/7ofKzC7MeBqWr/o9QyFrb45utXXFZ/n5Q9lHFoIfkuxp83tYLWXvCprN9X3laZpHkgsG+2CA483LxFZzHQnGKeyfoLpX17YCK6DMxltx0Bbj+99WqkNWkJ3hG3HnXSpVtVAq0xEukM5z0DdKeGIlHyWd9L/dQKE+wxHLhZkJaR3dicTimdAR1IQROiau3QQKBgQC5ny9flD5mYRSa9LrqfsraKsdgBgbLSUhQYUh6jXRNocYQm3qjqPZchK1OZ9zJKnWhuGPZRkTTbDDTNO6bvoPITH3kKRCsfwhpE+7qzd1Ibi6Rvk2f5bnjnyyixw0qiTisKbha/SUwwaSTF1JYj03uHls7w/oOz4zoUBnTPBTM3wKBgQCyurN4hGuEBIxwyw0TvSnAa8qpc+Cc+IqNdkQLe3jZTJzN0rbwaFhrBLHBmcUNfZNHhVfXIgscg6QtHvMu4xqVwgGZm2DoLU/BZrDnu2ANZ5zPZjEHGwCNYgAjXAS6mp0A/s+F4s2wt4PaJxZXpTbZpO3Hnyk4dTu842vznRNRkQKBgBBwVJW2X0BZ4KVIfzU9PWHM4BNU9U0TlF/p/a080cv+Q7g8zMOptwfCiEalVBxMslY8KjuXyYif0EMkMO2CFdGrVIrcSCm+plQH++S3jp1XhLBLljxO8AOEmUNhNX1K4cX9e+IzARQsUhLkwdot9szby2CjnoWpQ+VfEqpua7kpAoGAIe5kCCtLpiKe7wL9lpPBC3DrZsRDDkn5M+YSAQQN62MqGcE5Tebhcl/Px2uEB42hmmBsTsF9zooHbzGDBZHECE5us1TWTv3bOGJgj335PFJBaPaDtE+iCTVjxVLA6xRTXk8B4u8uEbClbZMiIL2S7jrrkYxb7QYEs1WRtwxJEKECgYEAgi2zR6b1HAVfYAlVbSoGcq1cvM+yQ4KZRJaZcEqJoZNUtiHrY7zgkMG6xKOFxTzcCA1lDiM/XP8TZ9ik1AEghiD4zylJwNpaSH4gXHkfrLbJJVRzPzIiZDSnfwDkEMBAKc0t9Es1F6/gOdrENcOVo/2bbvb6Mk0pM0E7oBh8RpM="
            )
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        GLog.e(TAG, "onCreate", "xx", "xx")

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}