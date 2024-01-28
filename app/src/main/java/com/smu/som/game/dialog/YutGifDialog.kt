package com.smu.som.game.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.smu.som.R

class YutGifDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.yut_gif_image_view)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.attributes?.windowAnimations = 0

        // Glide 설정
        val glideOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .skipMemoryCache(true)
            .override(Target.SIZE_ORIGINAL)

        Glide.with(context)
            .setDefaultRequestOptions(glideOptions)
    }
    private var gifResourceId = 0

    fun showYutGif(yut : Int) {
        show()
        when(yut) {
            1 ->  gifResourceId = R.drawable.yut_do
            2 ->  gifResourceId = R.drawable.yut_gae
            3 ->  gifResourceId = R.drawable.yut_gul
            4 ->  gifResourceId = R.drawable.yut_yut
            5 ->  gifResourceId = R.drawable.yut_mo
            0 ->  gifResourceId = R.drawable.yut_backdo
        }
        gifImageView(gifResourceId)
    }

    private fun gifImageView(gifResourceId: Int) {
        val gifImageView = findViewById<ImageView>(R.id.gif_image_view)
        gifImageView.setImageResource(gifResourceId)

        Glide.with(context)
            .asGif()
            .load(gifResourceId)
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // GIF 애니메이션이 완료되면 ImageView를 숨김
                    resource?.setLoopCount(1) // 1회만 재생하도록 설정
                    resource?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable?) {
                            dismiss()
                        }
                    })
                    return false
                }
            })
            .into(gifImageView)
    }
}