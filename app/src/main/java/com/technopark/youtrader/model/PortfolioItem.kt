package com.technopark.youtrader.model

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.technopark.youtrader.R
import com.technopark.youtrader.databinding.PortfolioItemBinding
import com.technopark.youtrader.utils.Constants.Companion.SIMPLE_PRECISION
import com.technopark.youtrader.utils.Constants.Companion.USD_SYMBOL
import com.technopark.youtrader.utils.roundTo
import com.xwray.groupie.viewbinding.BindableItem

class PortfolioItem(
    val portfolioCurrencyInfo: PortfolioCurrencyInfo,
    private val change: String
) : BindableItem<PortfolioItemBinding>() {

    override fun bind(viewBinding: PortfolioItemBinding, position: Int) {
        with(viewBinding) {
            currencyName.text = portfolioCurrencyInfo.id
            currencyCount.text = portfolioCurrencyInfo.amount.toString()
            price.text = roundTo( portfolioCurrencyInfo.price,SIMPLE_PRECISION).plus(USD_SYMBOL)
            changePrice.text = change
            setPortfolioPriceTextColor(changePrice)
        }
    }

    private fun setPortfolioPriceTextColor(changePrice: TextView) {
        if (changePrice.text[0] == '-') changePrice.setTextColor(
            ContextCompat.getColor(
                changePrice.context,
                R.color.red
            )
        )
        else changePrice.setTextColor(
            ContextCompat.getColor(
                changePrice.context,
                R.color.green
            )
        )
    }

    override fun getLayout(): Int = R.layout.portfolio_item

    override fun initializeViewBinding(view: View): PortfolioItemBinding =
        PortfolioItemBinding.bind(view)
}
