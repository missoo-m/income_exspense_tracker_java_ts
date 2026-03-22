import React from "react";
import { LuPlus } from "react-icons/lu";
import { useState } from "react";
import { useEffect } from "react";
import { prepareExpenseLineChartData } from "../../utils/helper";
import CustomBarChart1 from "../Charts/CustomBarChart1";
import CustomLineChart from "../Charts/CustomLineChart";

const ExpenseOverview =({transactions,onExpenseIncome}) =>{
    const [chartData, setChartData] = useState([])

    useEffect(() => {
        const result = prepareExpenseLineChartData(transactions);
        setChartData(result);

        return () => {};
    }, [transactions]);
  return (
      <div className="card">
        <div className="flex items-center justify-between">
            <div className="">
                <h5 className="text-lg"> Обзор расходов </h5>
                <p className="text-xs text-gray-400 mt-0.5">
                    Отслеживайте динамику своих расходов с течением времени и получайте представление 
                    о том, куда уходят ваши деньги.
                </p>
            </div>

            <button className="add-btn" onClick={onExpenseIncome}>
                <LuPlus className="text-lg" />
                Добавить расходы
            </button>
        </div>

        <div className="mt-10">
            <CustomLineChart data={chartData}/>

        </div>
      </div>
  )

}

export default ExpenseOverview