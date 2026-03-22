import DashboardLayout from "../../components/layouts/DashboardLayout";
import { useState } from "react";
import IncomeOverview from "../../components/Income/IncomeOverview";
import axiosInstance from "../../utils/axiosInstance";
import { API_PATHS } from "../../utils/apiPaths";
import { useEffect } from "react";
import Modal from "../../components/Modal";
import AddIncomeForm from "../../components/Income/AddIncomeForm";
import toast from "react-hot-toast";
import IncomeList from "../../components/Income/IncomeList";
import DeleteAlert from "../../components/DeleteAlert";
import { useUserAuth } from "../../hooks/useUserAuth";
import GlassDatePicker from "../../components/Inputs/GlassDatePicker";

const Income = () => {
  useUserAuth();

  const [incomeData, setIncomeData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filters, setFilters] = useState({ from: "", to: "", source: "" });
  const [openDeleteAlert, setOpenDeleteAlert] = useState({
    show: false,
    data: null,
  });
  const [openAddIncomeModal, setOpenAddIncomeModal] = useState(false);

  // Получить все доходы
  const fetchIncomeDetails = async (nextPage = page) => {
    if (loading) return;

    try {
      setLoading(true);
      const params = {
        page: nextPage,
        size: 10,
        from: filters.from || undefined,
        to: filters.to || undefined,
        source: filters.source || undefined,
      };
      const response = await axiosInstance.get(API_PATHS.INCOME.GET_ALL_INCOME, { params });
      const data = response.data;

      const items = Array.isArray(data) ? data : Array.isArray(data?.items) ? data.items : [];
      const normalized = items.map((x) => ({ ...x, _id: x?._id ?? x?.id }));
      setIncomeData(normalized);
      setPage(typeof data?.page === "number" ? data.page : 0);
      setTotalPages(typeof data?.totalPages === "number" ? data.totalPages : 1);
    } catch (error) {
      console.log("Что-то пошло не так. Пожалуйста, попробуйте снова", error);
      toast.error(error?.response?.data?.message || `Не удалось загрузить доходы (${error?.response?.status || "network"})`);
    } finally {
      setLoading(false);
    }
  };

  // Добавить доход
  const handleAddIncome = async (income) => {
    const { source, amount, date, icon } = income;

    if (!source.trim()) {
      toast.error("Источник обязателен");
      return;
    }

    if (!amount || isNaN(amount) || Number(amount) <= 0) {
      toast.error("Сумма должна быть числом больше 0");
      return;
    }

    if (!date) {
      toast.error("Дата обязательна");
      return;
    }

    try {
      await axiosInstance.post(API_PATHS.INCOME.ADD_INCOME, {
        source,
        amount,
        date,
        icon,
      });

      setOpenAddIncomeModal(false);
      toast.success("Доход успешно добавлен");
      fetchIncomeDetails(0);
    } catch (error) {
      console.error(
        "Ошибка при добавлении дохода:",
        error.response?.data?.message || error.message
      );
    }
  };

  // Удалить доход
  const deleteIncome = async (id) => {
    try {
      await axiosInstance.delete(API_PATHS.INCOME.DELETE_INCOME(id));

      setOpenDeleteAlert({ show: false, data: null });
      toast.success("Доход успешно удален");
      fetchIncomeDetails(0);
    } catch (error) {
      console.error(
        "Ошибка при удалении дохода:",
        error.response?.data?.message || error.message
      );
    }
  };

  // Скачать доходы в Excel
  const handleDownloadIncomeDetails = async () => {
    try {
      const response = await axiosInstance.get(
        API_PATHS.INCOME.DOWNLOAD_INCOME,
        {
          responseType: "blob",
        }
      );
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", "income_details.xlsx");
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch {
      console.error("Ошибка при скачивании доходов:", error);
      toast.error("Не удалось скачать доходы. Попробуйте снова.");
    }
  };

  useEffect(() => {
    fetchIncomeDetails(0);
    return () => {};
  }, []);

  return (
    <DashboardLayout activeMenu="Доходы">
      <div className="my-5 mx-auto">
        <div className="card mb-4">
          <div className="flex flex-wrap items-end gap-3">
            <div>
              <GlassDatePicker
                label="От"
                value={filters.from}
                onChange={(v) => setFilters((p) => ({ ...p, from: v }))}
              />
            </div>
            <div>
              <GlassDatePicker
                label="До"
                value={filters.to}
                onChange={(v) => setFilters((p) => ({ ...p, to: v }))}
              />
            </div>
            <div className="flex-1 min-w-[220px]">
              <label className="text-xs text-gray-500">Источник</label>
              <input
                className="custom-date-input"
                type="text"
                value={filters.source}
                onChange={(e) => setFilters((p) => ({ ...p, source: e.target.value }))}
                placeholder="Зарплата, Фриланс..."
              />
            </div>
            <button
              type="button"
              className="add-btn add-btn-fill h-[52px] px-6 text-base"
              onClick={() => fetchIncomeDetails(0)}
            >
              Применить
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 gap-6">
          <div className="">
            <IncomeOverview
              transactions={incomeData}
              onAddIncome={() => setOpenAddIncomeModal(true)}
            />
          </div>

          <IncomeList
            transactions={incomeData}
            onDelete={(id) => {
              setOpenDeleteAlert({ show: true, data: id });
            }}
            onDownload={handleDownloadIncomeDetails}
          />
        </div>

        <div className="flex items-center justify-between mt-4">
          <button
            type="button"
            className="add-btn"
            disabled={page <= 0}
            onClick={() => fetchIncomeDetails(page - 1)}
          >
            Назад
          </button>
          <div className="text-xs text-gray-500">
            Страница {page + 1} из {Math.max(totalPages, 1)}
          </div>
          <button
            type="button"
            className="add-btn"
            disabled={page + 1 >= totalPages}
            onClick={() => fetchIncomeDetails(page + 1)}
          >
            Вперед
          </button>
        </div>

        <Modal
          isOpen={openAddIncomeModal}
          onClose={() => setOpenAddIncomeModal(false)}
          title="Добавить доход"
        >
          <AddIncomeForm onAddIncome={handleAddIncome} />
        </Modal>

        <Modal
          isOpen={openDeleteAlert.show}
          onClose={() => setOpenDeleteAlert({ show: false, data: null })}
          title="Удалить доход"
        >
          <DeleteAlert
            content="Вы уверены, что хотите удалить этот доход?"
            onDelete={() => deleteIncome(openDeleteAlert.data)}
          />
        </Modal>
      </div>
    </DashboardLayout>
  );
};

export default Income;