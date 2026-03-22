
import { LuDownload } from "react-icons/lu";
import TransactionInfoCard from "../Cards/TransactionInfoCard";
import moment from "moment";

const ExpenseList =({transactions,onDelete,onDownload}) =>{
  return (
      <div className="card">
        <div className="flex items-center justify-between">
            <h5 className="text-lg"> Все расходы </h5>

            <button className="card-btn" onClick={onDownload}>
                <LuDownload className="text-base"/> Скачать
            </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2">
            {transactions?.map((expense) => (
                <TransactionInfoCard
                key={expense?._id ?? expense?.id}
                title={expense.category}
                icon={expense.icon}
                date={moment(expense.date).format("Do MMM YYYY")}
                amount={expense.amount}
                type="expense"
                onDelete={() => onDelete(expense?._id ?? expense?.id)}
                generalCategory={expense.generalCategory}
                description={expense.description}
            />
            ))}
        </div>
      </div>
  )


}
export default ExpenseList