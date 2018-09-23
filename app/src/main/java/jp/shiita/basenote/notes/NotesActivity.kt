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
import jp.shiita.basenote.util.setTintCompat
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
        setContentView(R.layout.act_notes)

        // Set up the toolbar.
        val toolbar = findViewById(R.id.add_edit_note_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setHomeAsUpIndicator(R.drawable.ic_menu_white)
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
        val tags = listOf(
                R.id.menu_drawer_all,
                R.id.menu_drawer_red,
                R.id.menu_drawer_orange,
                R.id.menu_drawer_yellow,
                R.id.menu_drawer_green,
                R.id.menu_drawer_light_blue,
                R.id.menu_drawer_blue,
                R.id.menu_drawer_purple,
                R.id.menu_drawer_black)

        // icon color
        navigationView.itemIconTintList = null
        tags.forEachIndexed { i, id ->
            val color = resources.obtainTypedArray(R.array.tag_color).getColor(i, 0)
            navigationView.menu.findItem(id).run { icon = icon.setTintCompat(color) }
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            filterTag = tags.indexOf(menuItem.itemId)
            notesPresenter.filterNotes(filterTag)
            navigationView.menu.run { tags.forEach { id -> findItem(id).isChecked = false } }
            menuItem.isChecked = true
            val title = getString(R.string.app_name) +
                    if (filterTag != 0) " [${resources.getStringArray(R.array.tag_color_item)[filterTag]}]"
                    else ""
            supportActionBar?.title = title
            drawerLayout.closeDrawers()
            true
        }
    }
}
