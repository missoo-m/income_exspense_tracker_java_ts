
import CustomPieChart from "../Charts/CustomPieChart";
import { useState } from "react";
import { useEffect } from "react";

const COLORS = [
    "#ff8fab", 
    "#ffc6ff", 
    "#ffcfd2", 
    "#e11d48", 
    "#f0abfc", 
    "#b5179e", 
];

const RecentIncomeWithChart =( { data, totalIncome}) =>{

    const [chartData, setChartData ] = useState([]);

    const prepareChartData = () => {
        const dataArr = data?.map((item) => ({
            name: item?.source,
            amount: item?.amount,
        }));

        setChartData(dataArr);
    };

    useEffect(() => {
        prepareChartData();

        return () => {};
    }, [data]);

  return (
    <div className="card">
        <div className="flex items-center justify-between">
            <h5 className="text-lg"> Доход за последние 60 дней </h5>
        </div>

        <CustomPieChart
           data={chartData}
           label= "Общий доход"
           totalAmount={`$${totalIncome}`}
           showTextAnchor
           colors={COLORS}
        />
    </div>
  )
}

export default RecentIncomeWithChart