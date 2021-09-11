package mobile.uangku.android.activities.transaction

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_goal.*
import mobile.uangku.android.R
import mobile.uangku.android.core.API
import mobile.uangku.android.core.LoadingDialog
import mobile.uangku.android.core.Preferences
import mobile.uangku.android.core.Sync
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

    fun setupUI(){

    }
}