
import { LuPlus } from "react-icons/lu";
import { useState } from "react";
import { useEffect } from "react";
import { prepareIncomeBarChartData } from "../../utils/helper";
import CustomBarChart1 from "../Charts/CustomBarChart1";

const IncomeOverview =({transactions,onAddIncome}) =>{
    const [chartData, setChartData] = useState([])

    useEffect(() => {
        const result = prepareIncomeBarChartData(transactions);
        setChartData(result);

        return () => {};
    }, [transactions]);
  return (
      <div className="card">
        <div className="flex items-center justify-between">
            <div className="">
                <h5 className="text-lg"> Обзор доходов </h5>
                <p className="text-xs text-gray-400 mt-0.5">
                    Отслеживайте свои доходы с течением времени и анализируйте динамику своих заработков.
                </p>
            </div>

            <button className="add-btn" onClick={onAddIncome}>
                <LuPlus className="text-lg" />
                Добавить доход
            </button>
        </div>

        <div className="mt-10">
            <CustomBarChart1 data={chartData}/>

        </div>
      </div>
  )

}

export default IncomeOverview