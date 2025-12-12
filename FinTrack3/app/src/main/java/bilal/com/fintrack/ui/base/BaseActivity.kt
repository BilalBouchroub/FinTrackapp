package bilal.com.fintrack.ui.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import bilal.com.fintrack.data.remote.TokenManager
import bilal.com.fintrack.utils.LocaleHelper

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(base: Context) {
        val tokenManager = TokenManager(base)
        val language = tokenManager.getLanguage()
        super.attachBaseContext(LocaleHelper.setLocale(base, language))
    }
}
