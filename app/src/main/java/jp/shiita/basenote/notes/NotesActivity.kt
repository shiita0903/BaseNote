package jp.shiita.basenote.notes

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.google.android.gms.ads.MobileAds
import dagger.android.support.DaggerAppCompatActivity
import jp.shiita.basenote.R
import jp.shiita.basenote.data.NotesRepository
import jp.shiita.basenote.util.replaceFragment
import javax.inject.Inject

class NotesActivity : DaggerAppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var notesPresenter: NotesPresenter
    @Inject lateinit var fragment: NotesFragment
    @Inject lateinit var notesRepository: NotesRepository

    var filterTag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this, resources.getString(R.string.banner_ad_unit_id))    // 広告読み込み
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

        if (savedInstanceState == null) {
            supportFragmentManager.replaceFragment(R.id.container, fragment)
        }

        // Create the presenter
        notesPresenter = NotesPresenter(fragment, notesRepository)
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
            val title = getString(R.string.app_name) +
                    if (filterTag != 0) " [" + resources.getStringArray(R.array.tag_color_item)[filterTag] + "]"
                    else ""
            supportActionBar?.title = title
            drawerLayout.closeDrawers()
            true
        }
    }
}
