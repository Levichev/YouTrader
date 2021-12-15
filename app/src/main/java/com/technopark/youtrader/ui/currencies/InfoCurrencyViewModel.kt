package com.technopark.youtrader.ui.currencies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.technopark.youtrader.base.BaseViewModel
import com.technopark.youtrader.model.HistoryOperationItem
import com.technopark.youtrader.model.InfoCurrencyModel
import com.technopark.youtrader.model.Result
import com.technopark.youtrader.network.firebase.FirebaseRepository
import com.technopark.youtrader.network.firebase.IFirebaseRepository
import com.technopark.youtrader.repository.CryptoCurrencyRepository
import com.technopark.youtrader.repository.CryptoTransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class InfoCurrencyViewModel @Inject constructor(
    private val repository: CryptoTransactionRepository,
    private val apiRepository: CryptoCurrencyRepository,
    private val firebaseRepository: IFirebaseRepository
) : BaseViewModel() {

    private val infoCurrencyModel: InfoCurrencyModel = InfoCurrencyModel()
    private val _screenState = MutableLiveData<Result<InfoCurrencyModel>>()
    val screenState: LiveData<Result<InfoCurrencyModel>> = _screenState


    fun updateCurrencyTransactions(currencyId: String) {
        viewModelScope.launch {
            _screenState.value = Result.Loading
            var ticker: String = ""
            repository.getCurrency(currencyId).collect {
                currency ->
                ticker = currency.symbol
            }

            firebaseRepository.getCurrencyTransactionsById(currencyId)
                .catch { error ->
                    _screenState.value = Result.Error(error)
                }
                .collect { currencyTransactions ->
                    infoCurrencyModel.operationItemList =
                        currencyTransactions.map { transaction ->
                            HistoryOperationItem(
                                transaction,
                                ticker
                            )
                        }
                }
        }
    }

    private fun calcProfit(amount: Double, price: Double, oldTotalPrice: Double): Double {
        return amount * price - oldTotalPrice
    }

    private fun calcProfitPercentage(amount: Double, price: Double, oldTotalPrice: Double): Double {
        return calcProfit(amount, price, oldTotalPrice) / oldTotalPrice
    }

    private fun asPercent(number: Double): Double {
        return number * MULTIPLY_NUM
    }

    fun updateCurrencyInformation(id: String) {
        viewModelScope.launch {
            _screenState.value = Result.Loading
            repository.getCurrency(id).collect {
                currency ->
                infoCurrencyModel.cryptoCurrency = currency
            }

            firebaseRepository.getTotalPrice(id).collect {
                price ->
                infoCurrencyModel.totalPrice = price
            }

            firebaseRepository.getTotalAmount(id).collect {
                amount ->
                infoCurrencyModel.totalAmount = amount
            }

            apiRepository.getCurrencyById(id)
                .catch { error ->
                    _screenState.value = Result.Error(error)
                }.collect {
                    infoCurrencyModel.absChange = calcProfit(
                        infoCurrencyModel.totalAmount,
                        it.priceUsd,
                        infoCurrencyModel.totalPrice
                    )

                    infoCurrencyModel.relativeChange = asPercent(
                        calcProfitPercentage(
                            infoCurrencyModel.totalAmount,
                            it.priceUsd,
                            infoCurrencyModel.totalPrice
                        )
                    )

                    infoCurrencyModel.totalPrice = infoCurrencyModel.totalAmount * it.priceUsd
                    _screenState.value = Result.Success(infoCurrencyModel)
                }
        }
    }

    companion object {
        private const val TAG = "InfoCurrencyViewModel"
        private const val MULTIPLY_NUM = 100.0
    }
}
