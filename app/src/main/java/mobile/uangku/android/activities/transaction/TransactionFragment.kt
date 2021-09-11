package mobile.uangku.android.activities.transaction

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.facebook.drawee.view.SimpleDraweeView
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_goal.swipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.android.synthetic.main.transaction_item.view.*
import mobile.uangku.android.R
import mobile.uangku.android.core.*
import mobile.uangku.android.models.Category
import mobile.uangku.android.models.Transaction
import org.json.JSONObject

class TransactionFragment : Fragment() {

    lateinit var preferences: Preferences
    lateinit var fragmentContext: Context

    val key ="transactions"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = Preferences(fragmentContext)

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            syncTransaction()
        }

        if (Sync.isNeeded(fragmentContext, key, 60))
            syncTransaction()

        addTransaction.setOnClickListener {
            startActivity(Intent(fragmentContext, EditTransactionActivity::class.java))
        }
        syncCategory()
    }

    fun syncCategory() {
        val loadingDialog = LoadingDialog(fragmentContext)
        loadingDialog.show()

        val request = API.createGetRequest(fragmentContext, "categories", null)
        request.getAsJSONObject(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                loadingDialog.dismissIfNeeded()
                val realm = Realm.getDefaultInstance()
                realm.executeTransactionAsync{ bgRealm ->
                    Category.fromJSONArray(bgRealm, response.getJSONArray("categories"))
                }
            }

            override fun onError(error: ANError) {
                loadingDialog.dismissIfNeeded()
                API.handleErrorResponse(fragmentContext, error)
            }
        })
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
                    Sync.setSyncTime(fragmentContext, key)
                    setupUI()
                })
            }

            override fun onError(error: ANError) {
                if (withLoadingDialog) loadingDialog!!.dismissIfNeeded()
                swipeRefreshLayout.isRefreshing = false
                API.handleErrorResponse(fragmentContext, error)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        syncTransaction()
        setupUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context!!
    }

    fun setupUI(){
        if(!isAdded)
            return

        val transactions = Realm.getDefaultInstance().where(Transaction::class.java).findAll()
        val incomeAmount = transactions.where().equalTo("type", Transaction.Type.INCOME.ordinal)
            .findAll().sum("amount")
        val outcomeAmount = transactions.where().equalTo("type", Transaction.Type.OUTCOME.ordinal)
            .findAll().sum("amount")

        if (transactions.size == 0) {
            transactionListRecylerView.visibility = View.GONE
            return
        }

        textIncomeAmount.text = "Rp. ${Utils.addThousandSeparator(incomeAmount.toDouble())}"
        textOutcomeAmount.text = "Rp. ${Utils.addThousandSeparator(outcomeAmount.toDouble())}"
        transactionListRecylerView.visibility = View.VISIBLE
        transactionListRecylerView.adapter = RecyclerViewAdapter(transactions)
        transactionListRecylerView.layoutManager = LinearLayoutManager(fragmentContext)
        transactionListRecylerView.isNestedScrollingEnabled = false
    }

    internal inner class RecyclerViewAdapter(var transactions: RealmResults<Transaction>): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val transaction = transactions[position]

            var created = transaction!!.created
            var name = transaction!!.name
            var amount = transaction!!.amount
            var type = transaction!!.type

            if (transaction != null && transaction.categoryId != null) {
                var category = Category[transaction.categoryId!!]
                if (category != null)
                    holder.transactionCategoryIcon.setImageURI(category.logoUrl)
            }

            holder.textMonthDate.text = DateUtils.toDisplayStringMonth(created!!)
            holder.textTransactionName.text = name
            if (type == Transaction.Type.INCOME.ordinal) {
                holder.textTransactionAmount.text = "+ Rp. ${Utils.addThousandSeparator(amount)}"
                holder.textTransactionAmount.setTextColor(resources.getColor(R.color.linkSection))
            } else {
                holder.textTransactionAmount.text = "- Rp. ${Utils.addThousandSeparator(amount)}"
                holder.textTransactionAmount.setTextColor(resources.getColor(R.color.minus))
            }
            holder.textTransactionCreated.text = DateUtils.toDisplayString(created!!)
        }

        override fun getItemCount(): Int {
            return transactions.size
        }

        internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
            var id = 0
            val textMonthDate: TextView = view.textMonthDate
            val textTransactionName: TextView = view.textTransactionName
            val textTransactionCreated: TextView = view.textTransactionCreated
            val textTransactionAmount: TextView = view.textTransactionAmount
            val transactionCategoryIcon: SimpleDraweeView = view.transactionCategoryIcon

            init {
                view.setOnClickListener(this)
            }

            override fun onClick(v: View) {
                TODO("Not yet implemented")
            }

        }
    }
}