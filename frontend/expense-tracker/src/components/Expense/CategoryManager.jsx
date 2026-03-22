import { useState, useEffect } from "react";
import axiosInstance from "../../utils/axiosInstance";
import { API_PATHS } from "../../utils/apiPaths";
import toast from "react-hot-toast";

const CategoryManager = ({ categories, onChange }) => {
  const [name, setName] = useState("");
  const [editingId, setEditingId] = useState(null);
  const [editingName, setEditingName] = useState("");

  // Разделяем категории на стандартные и пользовательские
  const defaultCategories = categories.filter(cat => cat.default);
  const userCategories = categories.filter(cat => !cat.default);

  const handleCreate = async () => {
    if (!name.trim()) {
      toast.error("Укажите название категории");
      return;
    }
    try {
      await axiosInstance.post(API_PATHS.CATEGORY.ADD, { name });
      onChange && onChange();
      setName("");
      toast.success("Категория создана");
    } catch (error) {
      toast.error(error.response?.data?.message || "Не удалось создать категорию");
    }
  };

  const handleUpdate = async (id) => {
    if (!editingName.trim()) {
      toast.error("Укажите название категории");
      return;
    }
    try {
      await axiosInstance.put(API_PATHS.CATEGORY.UPDATE(id), {
        name: editingName,
      });
      onChange && onChange();
      setEditingId(null);
      setEditingName("");
      toast.success("Категория обновлена");
    } catch (error) {
      toast.error(error.response?.data?.message || "Не удалось обновить категорию");
    }
  };

  const handleDelete = async (id) => {
    try {
      await axiosInstance.delete(API_PATHS.CATEGORY.DELETE(id));
      onChange && onChange();
      toast.success("Категория удалена");
    } catch (error) {
      toast.error(error.response?.data?.message || "Не удалось удалить категорию");
    }
  };

  useEffect(() => {
    if (editingId == null) {
      setEditingName("");
    }
  }, [editingId]);

  return (
    <div className="card mb-4">
      <h5 className="text-lg mb-3">Категории расходов</h5>

      {/* Добавление новой категории */}
      <div className="flex gap-2 mb-6">
        <input
          type="text"
          className="input flex-1"
          placeholder="Название новой категории"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <button type="button" className="add-btn add-btn-fill" onClick={handleCreate}>
          Добавить
        </button>
      </div>

      {/* Стандартные категории */}
      {defaultCategories.length > 0 && (
        <div className="mb-4">
          <h6 className="text-sm font-medium text-gray-500 mb-2">Категории по умолчанию</h6>
          <div className="flex flex-wrap gap-2">
            {defaultCategories.map((cat) => (
              <span
                key={cat._id}
                className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm"
              >
                {cat.name}
              </span>
            ))}
          </div>
        </div>
      )}

      {/* Пользовательские категории */}
      {userCategories.length > 0 && (
        <div>
          <h6 className="text-sm font-medium text-gray-500 mb-2">Ваши категории</h6>
          <ul className="space-y-2">
            {userCategories.map((cat) => (
              <li key={cat._id} className="flex items-center justify-between text-sm">
                {editingId === cat._id ? (
                  <>
                    <input
                      type="text"
                      className="input flex-1 mr-2"
                      value={editingName}
                      onChange={(e) => setEditingName(e.target.value)}
                    />
                    <button
                      type="button"
                      className="text-xs text-green-600 mr-2"
                      onClick={() => handleUpdate(cat._id)}
                    >
                      Сохранить
                    </button>
                    <button
                      type="button"
                      className="text-xs text-gray-500"
                      onClick={() => setEditingId(null)}
                    >
                      Отмена
                    </button>
                  </>
                ) : (
                  <>
                    <span>{cat.name}</span>
                    <div className="flex items-center gap-2">
                      <button
                        type="button"
                        className="text-xs text-blue-600"
                        onClick={() => {
                          setEditingId(cat._id);
                          setEditingName(cat.name);
                        }}
                      >
                        Переименовать 
                      </button>
                      <button
                        type="button"
                        className="text-xs text-red-600"
                        onClick={() => handleDelete(cat._id)}
                      >
                        Удалить
                      </button>
                    </div>
                  </>
                )}
              </li>
            ))}
          </ul>
        </div>
      )}

      {categories.length === 0 && (
        <p className="text-xs text-gray-500">Категории пока отсутствуют. Добавьте свою первую категорию!</p>
      )}
    </div>
  );
};

export default CategoryManager;