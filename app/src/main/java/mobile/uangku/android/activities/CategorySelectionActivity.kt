package mobile.uangku.android.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.facebook.drawee.view.SimpleDraweeView
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_category_selection.*
import kotlinx.android.synthetic.main.category_item.view.*
import mobile.uangku.android.R
import mobile.uangku.android.core.API
import mobile.uangku.android.core.LoadingDialog
import mobile.uangku.android.models.Category
import org.json.JSONObject

class CategorySelectionActivity : AppCompatActivity() {
    lateinit var categories: RealmResults<Category>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_selection)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false

        syncCategory()
    }

    fun syncCategory() {
        val loadingDialog = LoadingDialog(this)
        loadingDialog.show()

        val request = API.createGetRequest(this, "categories", null)
        request.getAsJSONObject(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                loadingDialog.dismissIfNeeded()
                val realm = Realm.getDefaultInstance()
                realm.executeTransactionAsync(Realm.Transaction { bgRealm ->
                    Category.fromJSONArray(bgRealm, response.getJSONArray("categories"))
                }, Realm.Transaction.OnSuccess {
                    setupData()
                })
            }

            override fun onError(error: ANError) {
                loadingDialog.dismissIfNeeded()
                API.handleErrorResponse(this@CategorySelectionActivity, error)
            }
        })
    }

    fun setupData() {
        val query = Realm.getDefaultInstance().where(Category::class.java)

        categories = query.equalTo("isActive", true).findAll().sort("name")
        recyclerView.adapter = RecyclerViewAdapter()
        recyclerView.adapter!!.notifyDataSetChanged()
    }

    fun selectCategory(category: Category) {
        val intent = Intent()
        var name = category.name
        var logo = category.logoUrl
        intent.putExtra("name", name)
        intent.putExtra("categoryID", category.id)
        intent.putExtra("logo", logo)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    internal inner class RecyclerViewAdapter: RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.category_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val category = categories[position]
            holder.category = category!!
            holder.nameTextView.text = category.name
            holder.icon.setImageURI(category.logoUrl)
        }

        override fun getItemCount(): Int {
            return categories.size
        }

        internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

            lateinit var category: Category
            val nameTextView: TextView = view.name
            val icon: SimpleDraweeView = view.icon

            init {
                view.setOnClickListener(this)
            }

            override fun onClick(view: View) {
                selectCategory(category)
            }

        }
    }
}