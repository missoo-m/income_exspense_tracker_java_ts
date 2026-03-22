
import CustomPieChart from "../Charts/CustomPieChart";

const COLORS =[ "#ff8fab", "#ffcfd2","#ffc6ff"];

const FinanceOverview =({totalBalance,totalIncome,totalExpense}) =>{
    const balanceData =[
        { name: "Общий баланс", amount: totalBalance},
        { name: "Всего расходов", amount: totalExpense},
        { name: "Всего доходов", amount: totalIncome},
    ];

return (
  <div className="card">
    <div className="flex items-center justify-between">
        <h5 className="text-lg">Общий баланс</h5>
    </div>

    <CustomPieChart
      data={balanceData}
      label="Общий баланс"
      totalAmount={`$${totalBalance}`}
      colors={COLORS}
      showTextAnchor
    />
  </div>

  );
}
export default FinanceOverview