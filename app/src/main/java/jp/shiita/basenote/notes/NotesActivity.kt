package jp.shiita.basenote.notes

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import jp.shiita.basenote.R
import jp.shiita.basenote.util.addFragmentToActivity

class NotesActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var notesPresenter: NotesPresenter

    var filterTag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())    // Fabricの起動
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
        val tags = mapOf(
                R.id.list_navigation_menu_item_all to 0,
                R.id.list_navigation_menu_item_red to 1,
                R.id.list_navigation_menu_item_orange to 2,
                R.id.list_navigation_menu_item_yellow to 3,
                R.id.list_navigation_menu_item_green to 4,
                R.id.list_navigation_menu_item_light_blue to 5,
                R.id.list_navigation_menu_item_blue to 6,
                R.id.list_navigation_menu_item_purple to 7,
                R.id.list_navigation_menu_item_black to 8)

        navigationView.itemIconTintList = null  // アイコンの色が表示されるように
        navigationView.setNavigationItemSelectedListener { menuItem ->
            filterTag = tags[menuItem.itemId]!!
            notesPresenter.filterNotes(filterTag)
            (0 until navigationView.menu.size()).forEach { navigationView.menu.getItem(it).isChecked = false }
            menuItem.isChecked = true
            drawerLayout.closeDrawers()
            true
        }
    }
}
