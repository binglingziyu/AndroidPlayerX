package online.testdata.player.x.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import online.testdata.player.x.databinding.ActivitySplashBinding
import online.testdata.player.x.ui.main.MainActivity

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val binding: ActivitySplashBinding by lazy { ActivitySplashBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.root.postDelayed({
            startActivity(Intent(this, MainActivity::class.java)).also {
                finish()
            }
        }, 2000)
    }


}