import { useEffect, useMemo, useState } from "react";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { useUserAuth } from "../../hooks/useUserAuth";
import axiosInstance from "../../utils/axiosInstance";
import { API_PATHS } from "../../utils/apiPaths";
import toast from "react-hot-toast";
import GlassDatePicker from "../../components/Inputs/GlassDatePicker";

function currentMonth() {
  const d = new Date();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  return `${d.getFullYear()}-${m}`;
}

const Budgets = () => {
  useUserAuth();

  const [month, setMonth] = useState(currentMonth());
  const [categories, setCategories] = useState([]);
  const [budgets, setBudgets] = useState([]);
  const [loading, setLoading] = useState(false);

  const budgetsByCategory = useMemo(() => {
    const map = new Map();
    (Array.isArray(budgets) ? budgets : []).forEach((b) => map.set(b.generalCategory, b));
    return map;
  }, [budgets]);

  const fetchCategories = async () => {
    const res = await axiosInstance.get(API_PATHS.CATEGORY.GET_ALL);
    setCategories(res.data || []);
  };

  const fetchBudgets = async () => {
    setLoading(true);
    try {
      const res = await axiosInstance.get(API_PATHS.BUDGET.GET_BY_MONTH(month));
      setBudgets(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      toast.error(e.response?.data?.message || "Не удалось загрузить бюджеты");
      setBudgets([]);
    } finally {
      setLoading(false);
    }
  };

  const upsertBudget = async (generalCategory, amount) => {
    const value = amount === "" ? "" : Number(amount);
    if (value !== "" && (Number.isNaN(value) || value < 0)) {
      toast.error("Неверная сумма");
      return;
    }

    try {
      const res = await axiosInstance.post(API_PATHS.BUDGET.UPSERT, {
        month,
        generalCategory,
        amount: value === "" ? 0 : value,
      });
      const updated = res.data;
      setBudgets((prev) => {
        const idx = prev.findIndex((b) => b._id === updated._id);
        if (idx >= 0) {
          const copy = [...prev];
          copy[idx] = updated;
          return copy;
        }
        return [...prev, updated];
      });
      toast.success("Бюджет сохранен");
    } catch (e) {
      toast.error(e.response?.data?.message || "Не удалось сохранить бюджет");
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    fetchBudgets();
  }, [month]);

  return (
    <DashboardLayout activeMenu="Budgets">
      <div className="my-5 mx-auto">
        <div className="card mb-4 border-[#ffdde8]">
          <div className="flex items-center justify-between gap-4">
            <h5 className="text-lg text-[#7f1d3f]">Ежемесячные бюджеты</h5>
            <div className="flex items-center gap-2">
              <GlassDatePicker
                label="Месяц"
                value={month}
                onChange={setMonth}
                picker="month"
              />
            </div>
          </div>
          <p className="text-xs text-gray-500 mt-2">
            Установите бюджет по каждой категории расходов на выбранный месяц.
          </p>
        </div>

        <div className="card">
          {loading ? (
            <p className="text-sm text-gray-500">Загрузка...</p>
          ) : (
            <div className="space-y-3">
              {categories.map((c) => {
                const existing = budgetsByCategory.get(c.name);
                const value = existing?.amount ?? "";
                return (
                  <div
                    key={c._id}
                    className="flex items-center justify-between gap-3 border-b border-[#ffe5ec] pb-3"
                  >
                    <div>
                      <div className="text-sm font-medium text-[#7f1d3f]">
                        {c.name}
                      </div>
                      <div className="text-xs text-gray-500">Сумма бюджета</div>
                    </div>
                    <div className="flex items-center gap-2">
                      <input
                        className="custom-date-input w-32"
                        type="number"
                        min="0"
                        step="0.01"
                        defaultValue={value}
                        onBlur={(e) => upsertBudget(c.name, e.target.value)}
                      />
                    </div>
                  </div>
                );
              })}
              {categories.length === 0 && (
                <p className="text-sm text-gray-500">Категории пока не добавлены</p>
              )}
            </div>
          )}
        </div>
      </div>
    </DashboardLayout>
  );
};

export default Budgets;