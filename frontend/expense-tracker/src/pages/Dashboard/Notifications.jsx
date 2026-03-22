import { useEffect, useState } from "react";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { useUserAuth } from "../../hooks/useUserAuth";
import axiosInstance from "../../utils/axiosInstance";
import { API_PATHS } from "../../utils/apiPaths";
import toast from "react-hot-toast";

const Notifications = () => {
  useUserAuth();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchAll = async () => {
    setLoading(true);
    try {
      const res = await axiosInstance.get(API_PATHS.NOTIFICATION.LIST);
      setItems(res.data || []);
    } catch (e) {
      toast.error(e.response?.data?.message || "Не удалось загрузить уведомления");
    } finally {
      setLoading(false);
    }
  };

  const markRead = async (id) => {
    try {
      await axiosInstance.put(API_PATHS.NOTIFICATION.MARK_READ(id));
      setItems((prev) =>
        prev.map((n) => (String(n._id) === String(id) ? { ...n, read: true } : n))
      );
    } catch (e) {
      toast.error(e.response?.data?.message || "Не удалось отметить как прочитанное");
    }
  };

  useEffect(() => {
    fetchAll();
  }, []);

  return (
    <DashboardLayout activeMenu="Уведомления">
      <div className="my-5 mx-auto">
        <div className="card border-[#ffdde8]">
          <div className="flex items-center justify-between">
            <h5 className="text-lg text-[#7f1d3f]">Уведомления</h5>
            <button type="button" className="add-btn add-btn-fill h-[52px] px-6 text-base" onClick={fetchAll}>
              Обновить
            </button>
          </div>

          {loading ? (
            <p className="text-sm text-gray-500 mt-4">Загрузка...</p>
          ) : items.length ? (
            <div className="mt-4 space-y-3">
              {items.map((n) => (
                <div
                  key={n._id}
                  className={`p-4 rounded-xl border transition-all duration-300 ${
                    n.read ? "border-[#ffe5ec] bg-white" : "border-[#ffb3c6] bg-[#fff6f9]"
                  }`}
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <div className="text-sm font-medium text-[#7f1d3f]">{n.type === "BUDGET_EXCEEDED" ? "Превышение бюджета" : n.type}</div>
                      <div className="text-sm text-gray-700 mt-1">{n.message}</div>
                      <div className="text-xs text-gray-400 mt-2">
                        {n.month ? `${n.month} • ` : ""}
                        {n.generalCategory ? `${n.generalCategory} • ` : ""}
                        {n.createdAt ? new Date(n.createdAt).toLocaleString() : ""}
                      </div>
                    </div>
                    {!n.read && (
                      <button
                        type="button"
                        className="custom-date-input h-9 px-3 text-xs w-auto"
                        onClick={() => markRead(n._id)}
                      >
                        Прочитано
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-gray-500 mt-4">Нет уведомлений.</p>
          )}
        </div>
      </div>
    </DashboardLayout>
  );
};

export default Notifications;