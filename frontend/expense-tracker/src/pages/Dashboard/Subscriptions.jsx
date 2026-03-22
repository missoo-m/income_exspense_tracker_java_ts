import { useEffect, useMemo, useState } from "react";
import { LuBellRing, LuPlus } from "react-icons/lu";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { useUserAuth } from "../../hooks/useUserAuth";

const seed = [
  { id: "n1", name: "Netflix", amount: 499, nextDate: "2026-03-25" },
  { id: "s1", name: "Spotify", amount: 199, nextDate: "2026-03-15" },
  { id: "r1", name: "Rent", amount: 15000, nextDate: "2026-04-01" },
];
const LS_KEY = "expense_tracker_subscriptions";

const CountUpValue = ({ value }) => {
  const [display, setDisplay] = useState(0);

  useEffect(() => {
    const start = performance.now();
    const from = 0;
    const duration = 900;

    const run = (now) => {
      const p = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - p, 3);
      setDisplay(Math.round(from + (value - from) * eased));
      if (p < 1) requestAnimationFrame(run);
    };

    requestAnimationFrame(run);
  }, [value]);

  return <>{display.toLocaleString()}</>;
};

const Subscriptions = () => {
  useUserAuth();

  const [list, setList] = useState(seed);
  const [showAdd, setShowAdd] = useState(false);
  const [form, setForm] = useState({ name: "", amount: "", nextDate: "" });

  const monthlyTotal = useMemo(
    () => list.reduce((sum, x) => sum + (Number(x.amount) || 0), 0),
    [list]
  );

  useEffect(() => {
    try {
      const raw = localStorage.getItem(LS_KEY);
      if (!raw) return;
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) {
        setList(parsed);
      }
    } catch {
      // ignore invalid local cache
    }
  }, []);

  useEffect(() => {
    try {
      localStorage.setItem(LS_KEY, JSON.stringify(list));
    } catch {
      // ignore write issues
    }
  }, [list]);

  const isDueSoon = (nextDate) => {
    const now = new Date();
    const target = new Date(nextDate);
    const diffDays = Math.ceil((target - now) / (1000 * 60 * 60 * 24));
    return diffDays >= 0 && diffDays <= 3;
  };

  const addSubscription = () => {
    if (!form.name.trim() || !form.amount || !form.nextDate) return;
    setList((prev) => [
      ...prev,
      {
        id: `u-${Date.now()}`,
        name: form.name.trim(),
        amount: Number(form.amount),
        nextDate: form.nextDate,
      },
    ]);
    setForm({ name: "", amount: "", nextDate: "" });
    setShowAdd(false);
  };

  return (
    <DashboardLayout activeMenu="Subscriptions">
      <div className="my-5 mx-auto">
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h4 className="text-xl font-semibold text-gray-900">Активные подписки</h4>
            <button
              type="button"
              className="add-btn add-btn-fill h-[52px] px-6 text-base"
              onClick={() => setShowAdd((v) => !v)}
            >
              <LuPlus className="text-lg" />
              Добавить подписку
            </button>
          </div>

          <div className="mb-4 text-sm text-gray-600">
            Общая сумма подписок в месяц:{" "}
            <span className="font-semibold text-[#e11d48]">
              <CountUpValue value={monthlyTotal} /> $
            </span>
          </div>

          {showAdd && (
            <div className="mb-5 grid grid-cols-1 md:grid-cols-4 gap-3">
              <input
                className="custom-date-input"
                placeholder="Название"
                value={form.name}
                onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))}
              />
              <input
                className="custom-date-input"
                type="number"
                placeholder="Сумма"
                value={form.amount}
                onChange={(e) => setForm((p) => ({ ...p, amount: e.target.value }))}
              />
              <input
                className="custom-date-input"
                type="date"
                value={form.nextDate}
                onChange={(e) => setForm((p) => ({ ...p, nextDate: e.target.value }))}
              />
              <button type="button" className="add-btn add-btn-fill h-[52px] px-6 text-base" onClick={addSubscription}>
                Сохранить
              </button>
            </div>
          )}

          <div className="space-y-3">
            {list.map((s) => (
              <div key={s.id} className="p-4 rounded-xl border border-[#ffe5ec] bg-white flex items-center justify-between">
                <div>
                  <div className="font-medium text-gray-900">{s.name}</div>
                  <div className="text-xs text-gray-500">
                    Следующий платеж: {new Date(s.nextDate).toLocaleDateString()}
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-sm font-semibold text-[#7f1d3f]">
                    {Number(s.amount).toLocaleString()} $/мес
                  </span>
                  {isDueSoon(s.nextDate) ? (
                    <LuBellRing className="text-[#e11d48]" title="Платеж в ближайшие 3 дня" />
                  ) : null}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default Subscriptions;
