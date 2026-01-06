package com.logan.shinningviewapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.logan.shinningviewapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            sendEmail(
                this,
                "【请修改标题!】",
                "【反馈，请描述问题，收到后会第一时间处理!】",
                "notwalnut@163.com"
            )
        }
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
            R.id.action_settings -> {
                openBrowser(getString(R.string.github_com_project_url))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun openBrowser(url: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        val content_url = Uri.parse(url)
        intent.data = content_url
        startActivity(Intent.createChooser(intent, "请选择浏览器"))
    }

    /**
     * 邮件分享
     *
     * @param context 上下文
     * @param title   邮件主题
     * @param content 邮件内容
     * @param address 邮件地址
     */
    fun sendEmail(context: Context, title: String?, content: String?, address: String) {
        val uri = Uri.parse("mailto:$address")
        val emailIntent = Intent(Intent.ACTION_SENDTO, uri)
        // 对方邮件地址
        emailIntent.putExtra(Intent.EXTRA_EMAIL, address)
        // 标题内容
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        // 邮件文本内容
        emailIntent.putExtra(Intent.EXTRA_TEXT, content)
        context.startActivity(Intent.createChooser(emailIntent, "选择邮箱"))
    }
}