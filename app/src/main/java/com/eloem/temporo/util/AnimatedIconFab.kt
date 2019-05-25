package com.eloem.temporo.util

import android.content.Context
import android.graphics.drawable.Animatable
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import com.eloem.temporo.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Error

class AnimatedIconFab @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = com.google.android.material.R.attr.floatingActionButtonStyle
) : FloatingActionButton(context, attrs, defStyleAttr) {
    
    enum class Icon {
        ADD {
            @DrawableRes
            override val simpleDrawableRes = R.drawable.add_to_check

            override fun transitionDrawableRes(to: Icon): Int = when(to) {
                CHECK -> R.drawable.add_to_check
                REFRESH -> R.drawable.add_to_refresh
                NEXT -> R.drawable.add_to_next
                else -> throw Error("Unknown transition from: ${this::class.simpleName} to $to")
            }
        },
        NEXT {
            @DrawableRes
            override val simpleDrawableRes = R.drawable.next_to_refresh

            override fun transitionDrawableRes(to: Icon): Int = when(to) {
                CHECK -> R.drawable.next_to_check
                REFRESH -> R.drawable.next_to_refresh
                ADD -> R.drawable.next_to_add
                else -> throw Error("Unknown transition from: ${this::class.simpleName} to $to")
            }
        },
        CHECK {
            @DrawableRes
            override val simpleDrawableRes = R.drawable.check_to_add

            override fun transitionDrawableRes(to: Icon): Int = when(to) {
                ADD -> R.drawable.check_to_add
                REFRESH -> R.drawable.check_to_refresh
                NEXT -> R.drawable.check_to_next
                else -> throw Error("Unknown transition from: ${this::class.simpleName} to $to")
            }
        },
        REFRESH{
            @DrawableRes
            override val simpleDrawableRes = R.drawable.refresh_to_check

            override fun transitionDrawableRes(to: Icon): Int = when(to) {
                CHECK -> R.drawable.refresh_to_check
                ADD -> R.drawable.refresh_to_add
                NEXT ->  R.drawable.refresh_to_next
                else -> throw Error("Unknown transition from: ${this::class.simpleName} to $to")
            }
        };

        abstract val simpleDrawableRes: Int
        @DrawableRes
        abstract fun transitionDrawableRes(to: Icon): Int
    }
    
    private lateinit var currentIcon: Icon
    
    var icon: Icon
        get() = currentIcon
        set(value) {
            currentIcon = value
            setImageResource(icon.simpleDrawableRes)
        }
    
    fun animateToIcon(icon: Icon) {
        if (icon == currentIcon) return
        setAnimatableAndStart(currentIcon.transitionDrawableRes(icon))
        currentIcon = icon
    }
    
    private fun setAnimatableAndStart(@DrawableRes resourceId: Int) {
        val drawable = context.getDrawable(resourceId)
        val animatable = drawable as Animatable
        setImageDrawable(drawable)
        animatable.start()
    }
}