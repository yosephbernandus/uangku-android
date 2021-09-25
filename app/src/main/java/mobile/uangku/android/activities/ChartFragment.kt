package mobile.uangku.android.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_chart.*
import kotlinx.android.synthetic.main.fragment_chart.swipeRefreshLayout
import mobile.uangku.android.R
import mobile.uangku.android.core.API
import mobile.uangku.android.core.LoadingDialog
import mobile.uangku.android.core.Preferences
import mobile.uangku.android.models.Transaction
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

class ChartFragment : Fragment() {

    lateinit var preferences: Preferences
    lateinit var fragmentContext: Context
    lateinit var transactions: RealmResults<Transaction>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactions = Realm.getDefaultInstance().where(Transaction::class.java).findAll()

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            syncTransaction()
        }

        setPieChart()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context!!
    }

    fun syncTransaction(withLoadingDialog: Boolean = false) {
        var loadingDialog: LoadingDialog? = null
        if (withLoadingDialog) {
            loadingDialog = LoadingDialog(fragmentContext)
            loadingDialog.show()
        }

        val request = API.createGetRequest(fragmentContext, "transactions", null)
        request.getAsJSONObject(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                if (withLoadingDialog) loadingDialog!!.dismissIfNeeded()
                if (!isVisible) return

                if (swipeRefreshLayout != null) swipeRefreshLayout.isRefreshing = false
                val realm = Realm.getDefaultInstance() as Realm

                realm.executeTransactionAsync(Realm.Transaction { bgRealm ->
                    Transaction.fromJSONArray(bgRealm, response.getJSONArray("transactions"))
                }, Realm.Transaction.OnSuccess {
                    setPieChart()
                })
            }

            override fun onError(error: ANError) {
                if (withLoadingDialog) loadingDialog!!.dismissIfNeeded()
                swipeRefreshLayout.isRefreshing = false
                API.handleErrorResponse(fragmentContext, error)
            }
        })
    }

    fun setPieChart() {

        val income = transactions.where().equalTo("type", Transaction.Type.INCOME.ordinal)
            .findAll().sum("amount")
        val outcome = transactions.where().equalTo("type", Transaction.Type.OUTCOME.ordinal)
            .findAll().sum("amount")
        val totalAmount = transactions.where().findAll().sum("amount")

        var outcomeAmount = (outcome.toDouble() / totalAmount.toDouble()) * 100
        var outcomePercentage = BigDecimal(outcomeAmount).setScale(2, RoundingMode.HALF_EVEN).toFloat()

        var incomeAmount = (income.toDouble() / totalAmount.toDouble()) * 100
        var incomePercentage = BigDecimal(incomeAmount).setScale(2, RoundingMode.HALF_EVEN).toFloat()

        // yvalues
        val piechartentry: ArrayList<PieEntry> = ArrayList()
        piechartentry.add( PieEntry(outcomePercentage, "Pengeluaran"))
        piechartentry.add( PieEntry(incomePercentage, "Pemasukan"))

        // dataset collors
        val colors: ArrayList<Int> = ArrayList()
        colors.add(resources.getColor(R.color.minus))
        colors.add(resources.getColor(R.color.linkSection))

        // Fill the chart
        val piedataset = PieDataSet(piechartentry, "")
        piedataset.colors = colors

        // Create space
        piedataset.sliceSpace = 2f

        val data = PieData(piedataset)
        pieChart.data = data

        pieChart.holeRadius = 5f
//        pieChart.setBackgroundColor(resources.getColor(R.color.softBlue))

        pieChart.description.text = "Persentase Saldo"
        pieChart.animateY(3000)
    }
}