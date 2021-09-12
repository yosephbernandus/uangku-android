package mobile.uangku.android.activities.transaction

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail_transaction.*
import mobile.uangku.android.R
import mobile.uangku.android.core.DateUtils
import mobile.uangku.android.core.Utils
import mobile.uangku.android.models.Category
import mobile.uangku.android.models.Transaction

class DetailTransactionActivity : AppCompatActivity() {

    lateinit var transaction: Transaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_transaction)

        transaction = Transaction.get(intent.getIntExtra("id", 0))!!

        var notes = "-"
        if (!transaction.notes.isNullOrEmpty())
            notes = transaction.notes!!

        textTransactionName.text = transaction.name.toString()
        dateTransaction.text = DateUtils.toDisplayString(transaction.created!!)
        textNotes.text = notes
        transactionDetailAmount.text = "Rp. ${Utils.addThousandSeparator(transaction.amount!!)}"
        if (transaction.type == Transaction.Type.INCOME.ordinal){
            textType.text = Transaction.Type.INCOME.value
            textType.setTextColor(resources.getColor(R.color.linkSection))
        } else {
            textType.text = Transaction.Type.OUTCOME.value
            textType.setTextColor(resources.getColor(R.color.minus))
        }

        if (transaction.categoryId != null) {
            var category = Category[transaction.categoryId!!]
            if (category != null)
                transactionIcon.setImageURI(category.logoUrl)
        }
    }

    fun closeIconOnClick(view: View) {
        finish()
    }

}