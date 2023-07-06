package uz.mobile.id.utils

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.annotation.Dimension
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.view_custom_button.view.*
import uz.mobile.id.R
import uz.mobile.id.utils.ext.dpToPx
import uz.mobile.id.utils.ext.hide
import uz.mobile.id.utils.ext.show

class CustomButton : RelativeLayout {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        LayoutInflater.from(context).inflate(R.layout.view_custom_button, this, true)
        attrs?.let {
            val array = context.obtainStyledAttributes(attrs, R.styleable.CustomButton)
            if (array.hasValue(R.styleable.CustomButton_buttonText)) {
                tvBtnText.text = array.getString(R.styleable.CustomButton_buttonText)
            }
            tvBtnText.setTextColor(array.getColor(R.styleable.CustomButton_buttonTextColor, ContextCompat.getColor(context, android.R.color.white)))
            tvBtnText.setTextSize(Dimension.DP, array.getDimension(R.styleable.CustomButton_buttonTextSize, context.dpToPx(14).toFloat()))
            val color = array.getColor(R.styleable.CustomButton_buttonProgressColor, Color.WHITE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                progress.indeterminateDrawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
            } else {
                @Suppress("DEPRECATION")
                progress.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
            array.recycle()
        }
        setButtonEnabled(false)
    }

    fun setButtonText(text: String) {
        tvBtnText.text = text
    }

    fun setButtonText(resId: Int) {
        tvBtnText.text = context.getString(resId)
    }

    fun setButtonTextColor(color: Int) {
        tvBtnText.setTextColor(color)
    }

    fun showLoading() {
        changeLoadingState(true)
    }

    fun hideLoading() {
        changeLoadingState(false)
    }

    private fun changeLoadingState(isLoading: Boolean) {
        isClickable = !isLoading
        isFocusable = !isLoading

        if(isLoading) {
            progress.show()
            tvBtnText.hide()
            return
        }

        progress.hide()
        tvBtnText.show()
    }

    fun setButtonEnabled(enabled: Boolean) {
        isEnabled = enabled
        isClickable = enabled
        isFocusable = enabled
        if (enabled) {
            tvBtnText.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        } else {
            tvBtnText.setTextColor(ContextCompat.getColor(context!!, R.color.grey))
        }
    }
}