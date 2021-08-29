package mobile.uangku.android.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
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
import io.realm.Case
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
    var lastModificationTime: Long = 0
    var delay: Long = 500 // in ms
    var handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_selection)

        val inputFinish = Runnable {
            if (System.currentTimeMillis() >= lastModificationTime + delay) {
                setupData()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false

        categorySearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(value: Editable?) {
                if (value.isNullOrBlank()) return
                lastModificationTime = System.currentTimeMillis()
                handler.postDelayed(inputFinish, delay)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

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

    fun closeIconOnClick(view: View) {
        finish()
    }

    fun setupData() {
        val query = Realm.getDefaultInstance().where(Category::class.java)

        val key = categorySearch.text.toString()
        if (key.length > 2)
            query.contains("name", key, Case.INSENSITIVE)

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