import CustomPieChart from "../Charts/CustomPieChart";

const COLORS = [
  "#ff8fab",
  "#ffc6ff",
  "#ffcfd2",
  "#e11d48",
  "#f0abfc",
  "#b5179e",
  "#f97316",
];

const ExpensesByCategoryChart = ({ data = [] }) => {
  const chartData = data.map((item) => ({
    name: item.category || "Без категории",
    amount: item.total || 0,
  }));

  const total = chartData.reduce((sum, x) => sum + (x.amount || 0), 0);

  return (
    <div className="card">
      <div className="flex items-center justify-between">
        <h5 className="text-lg">Расходы по категориям</h5>
      </div>

      <CustomPieChart
        data={chartData}
        label="Всего расходов"
        totalAmount={`$${total.toFixed(2)}`}
        colors={COLORS}
        showTextAnchor
      />
    </div>
  );
};

export default ExpensesByCategoryChart;

