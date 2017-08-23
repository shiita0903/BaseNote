package com.example.shiita.notepad.notes

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.example.shiita.notepad.R
import com.example.shiita.notepad.util.addFragmentToActivity
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class NotesActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var notesPresenter: NotesPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.notes_act)

        // Set up the toolbar.
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setHomeAsUpIndicator(R.drawable.ic_menu)
            it.setDisplayHomeAsUpEnabled(true)
        }

        // Set up the navigation drawer.
        drawerLayout = (findViewById(R.id.drawer_layout) as DrawerLayout).apply {
            setStatusBarBackground(R.color.colorPrimaryDark)
        }
        val navigationView = findViewById(R.id.nav_view) as NavigationView
        setupDrawerContent(navigationView)

        val notesFragment = supportFragmentManager.findFragmentById(R.id.contentFrame)
                as NotesFragment? ?: NotesFragment.newInstance().also {
                    addFragmentToActivity(supportFragmentManager, it, R.id.contentFrame)
        }

        // Create the presenter
        notesPresenter = NotesPresenter(notesFragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Open the navigation drawer when the home icon is selected from the toolbar.
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Close the navigation drawer when an item is selected.
            menuItem.isChecked = true
            drawerLayout.closeDrawers()
            true
        }
    }
}
