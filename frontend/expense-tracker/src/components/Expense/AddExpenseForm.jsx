import { useState } from "react";
import Input from "../Inputs/Input";
import EmojiPickerPopup from "../EmojiPickerPopup";
import toast from "react-hot-toast";

const AddExpenseForm = ({ onAddExpense, categories = [] }) => {
  const [expense, setExpense] = useState({
    categoryId: "", // теперь храним ID категории, а не название
    description: "",
    amount: "",
    date: "",
    icon: "",
  });

  const handleChange = (key, value) =>
    setExpense({
      ...expense,
      [key]: value,
    });

  const handleSubmit = () => {
    // Находим выбранную категорию по ID
    const selectedCategory = categories.find((c) => String(c._id) === String(expense.categoryId));
    
    if (!selectedCategory) {
      toast.error("Пожалуйста, выберите категорию");
      return;
    }

    // Отправляем данные так, чтобы бэкенд получил:
    // generalCategory (общая категория) + description (уточнение)
    onAddExpense({
      ...expense,
      generalCategory: selectedCategory.name,
      category: selectedCategory.name,
    });
  };

  return (
    <div>
      <EmojiPickerPopup
        icon={expense.icon}
        onSelect={(selectedIcon) => handleChange("icon", selectedIcon)}
      />

      {/* Только выпадающий список категорий */}
      <div className="mb-4">
        <label className="block text-xs font-medium mb-1">
          Категория <span className="text-red-500">*</span>
        </label>
        <select
          className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"
          value={expense.categoryId}
          onChange={(e) => handleChange("categoryId", e.target.value)}
          required
        >
          <option value="">Выберите категорию</option>
          {categories.map((cat) => (
            <option key={cat._id} value={cat._id}>
              {cat.name} {cat.default ? "(Default)" : ""}
            </option>
          ))}
        </select>
        <p className="text-xs text-gray-400 mt-1">
          Добавить дополнительные категории можно в менеджере категорий.
        </p>
      </div>

      {/* Поле для уточнения */}
      <Input
        value={expense.description}
        onChange={({ target }) => handleChange("description", target.value)}
        label="Описание (на что были потрачены деньги?)"
        placeholder="Продукты, такси, билет в кино..."
        type="text"
      />

      <Input
        value={expense.amount}
        onChange={({ target }) => handleChange("amount", target.value)}
        label="Сумма"
        placeholder="0.00"
        type="number"
        min="0"
        step="0.01"
      />

      <Input
        value={expense.date}
        onChange={({ target }) => handleChange("date", target.value)}
        label="Date"
        type="date"
      />

      <div className="flex justify-end mt-6">
        <button
          type="button"
          className="add-btn add-btn-fill"
          onClick={handleSubmit}
        >
          Добавить рассходы
        </button>
      </div>
    </div>
  );
};

export default AddExpenseForm;