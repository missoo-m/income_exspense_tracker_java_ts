
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { useUserAuth } from "../../hooks/useUserAuth";
import { API_PATHS } from "../../utils/apiPaths";
import axiosInstance from "../../utils/axiosInstance";
import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { LuTrash2, LuPlus, LuPencil } from "react-icons/lu";
import Modal from "../../components/Modal";
import DeleteAlert from "../../components/DeleteAlert";

const ManageContent = () => {
    useUserAuth();
    const [contentList, setContentList] = useState([]);
    const [loading, setLoading] = useState(true);

    const [editingItem, setEditingItem] = useState(null);
    const [formData, setFormData] = useState({});

    const [newContent, setNewContent] = useState({
        type: 'news',
        title: '',
        content: '',
    });

    const [openDeleteAlert, setOpenDeleteAlert] = useState({
        show: false,
        data: null,
    });

    const fetchContent = async () => {
        try {
            setLoading(true);
            const response = await axiosInstance.get(API_PATHS.ADMIN.GET_ALL_CONTENT);
            const sortedContent = response.data.sort((a, b) => new Date(b.date) - new Date(a.date));
            setContentList(sortedContent);
        } catch (error) {
            toast.error("Не удалось загрузить контент.");
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setNewContent((prev) => ({ ...prev, [name]: value }));
    };

    const handleEditClick = (item) => {
        setEditingItem(item);
        setFormData({
            title: item.title || '',
            content: item.content || '',
            type: item.type
        });
    };

    const handleEditFormChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleUpdate = async (e) => {
        e.preventDefault();
        if (!editingItem) return;

        if (formData.type === 'news' && (!formData.title || !formData.content)) {
            toast.error("Для новостных сообщений необходимы заголовок и содержание.");
            return;
        }

        let dataToSend = formData;
        let updatePath = API_PATHS.ADMIN.UPDATE_CONTENT(editingItem?._id ?? editingItem?.id);

        if (formData.type === 'currency') {
            toast.error("Курсы валют следует обновлять через специальную форму (Добавить новый контент).");
            return;
        }

        try {
            await axiosInstance.put(updatePath, dataToSend);

            toast.success("Элемент содержимого успешно обновляется.");
            setEditingItem(null);
            fetchContent();
        } catch (error) {
            toast.error(error.response?.data?.message || "Ошибка при обновлении содержимого.");
            console.error("Ошибка обновления содержимого:", error);
        }
    };

    const handleDelete = async (id) => {
        try {
            await axiosInstance.delete(API_PATHS.ADMIN.DELETE_CONTENT(id));

            setOpenDeleteAlert({ show: false, data: null });
            toast.success("Сведения о контенте успешно удалены.");
            fetchContent();
        } catch (error) {
            console.error(
                "Ошибка при удалении содержимого:",
                error.response?.data?.message || error.message
            );
        }
    };

    const handleAddContent = async (e) => {
        e.preventDefault();
        if (newContent.type === 'news' && (!newContent.title || !newContent.content)) {
            toast.error("Для новостных сообщений необходимы заголовок и содержание.");
            return;
        }

        try {
            const dataToSend = {
                type: 'news',
                title: newContent.title,
                content: newContent.content,
            };

            await axiosInstance.post(API_PATHS.ADMIN.ADD_CONTENT, dataToSend);
            toast.success("Добавлен новостной контент!");

            setNewContent({
                type: 'news',
                title: '',
                content: '',
            });
            fetchContent();

        } catch (error) {
            toast.error(error.response?.data?.message || "Не удалось добавить контент.");
        }
    };

    useEffect(() => {
        fetchContent();
    }, []);

    const formatContentPreview = (item) => {
        return item.content.substring(0, 150) + (item.content.length > 150 ? '...' : '');
    };

    return (
        <DashboardLayout activeMenu="Manage Content">

            <h2 className="text-xl font-bold text-[#7f1d3f] mb-6">Управление общедоступным контентом</h2>
            <div className="card mb-8 p-8 border-[#ffdde8]">

                <form onSubmit={handleAddContent}>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                        <div className="md:col-span-1">
                            <label className="text-sm font-medium text-slate-600 mb-1 block">Тип контента</label>
                            <select
                                name="type"
                                value={newContent.type}
                                onChange={handleInputChange}
                                className="custom-date-input"
                            >
                                <option value="news">Новости</option>
                            </select>
                        </div>

                        {newContent.type === 'news' && (
                            <div className="md:col-span-2">
                                <label className="text-sm font-medium text-slate-600 mb-1 block">Заголовок</label>

                                <input
                                    name="title"
                                    value={newContent.title}
                                    onChange={handleInputChange}
                                    placeholder="Введите название новости"
                                    type="text"
                                    className="custom-date-input"
                                />
                            </div>
                        )}

                    </div>

                    <div className="mb-4">
                        <label className="text-sm font-medium text-slate-600 mb-1 block">
                            Новостной контент
                        </label>
                        <textarea
                            name="content"
                            value={newContent.content}
                            onChange={handleInputChange}
                            rows={6}
                            placeholder='Подробно опишите новость. Для лучшей читаемости используйте абзацы.'
                            className="custom-date-input w-full h-48 resize-y p-4"
                        />
                    </div>



                    <button type="submit" className="add-btn add-btn-fill h-[52px] px-6 text-base mt-6 w-full flex items-center justify-center gap-2">
                        <LuPlus size={18} />
                        Добавить новость 
                    </button>
                </form>
            </div>

            <h3 className="text-xl font-semibold mb-4 text-[#7f1d3f]">Существующий контент({contentList.length})</h3>

            {loading ? (
                <p>Загрузка...</p>
            ) : (
                <div className="space-y-4">
                    {contentList.map((item) => (
                        <div key={item?._id ?? item?.id} className="p-5 bg-white rounded-xl border border-[#ffe5ec] flex justify-between items-start transition-all duration-300 hover:shadow-md hover:scale-[1.01]">
                            <div className="flex-grow">
                                <span className={`
                                    text-xs font-bold px-3 py-1 rounded-full 
                                    ${item.type === 'news'
                                        ? 'bg-[#ffe5ec] text-[#ff8fab]'
                                        : 'bg-green-100 text-green-700'
                                    } 
                                    mr-3 
                                `}>
                                    {item.type.toUpperCase()}
                                </span>

                                <h4 className="text-xl font-bold text-gray-800 mt-2">
                                    {item.title || 'Exchange rates (see details)'}
                                </h4>
                                <p className="text-sm text-gray-600 mt-2">
                                    {formatContentPreview(item)}
                                </p>
                                <p className="text-xs text-gray-400 mt-3">
                                    Опубликовано: {new Date(item.date).toLocaleString()}
                                </p>
                            </div>

                            <div className="flex space-x-2 shrink-0 pt-1">
                                {item.type === 'news' && (
                                    <button onClick={() => handleEditClick(item)} className="p-2 rounded-full text-[#7f1d3f] bg-[#ffe5ec] hover:bg-[#ffb3c6] hover:text-white transition duration-200" title="Edit news">
                                        <LuPencil size={20} />
                                    </button>
                                )}

                                <button
                                    onClick={() => setOpenDeleteAlert({ show: true, data: item?._id ?? item?.id })}
                                    className="p-2 rounded-full text-gray-400 bg-white-50 hover:bg-[#ffe5ec] hover:text-red-600 transition duration-200"
                                    title="Delete content"
                                >
                                    <LuTrash2 size={20} />
                                </button>
                            </div>
                        </div>
                    ))}
                    {!contentList.length && <p className="text-gray-500 p-4 rounded-xl shadow-lg bg-white">No content has been added yet.</p>}
                </div>
            )}

            {editingItem && (
                <Modal
                    title={`Editing: ${editingItem.title || 'Exchange rates'}`}
                    isOpen={!!editingItem}
                    onClose={() => setEditingItem(null)}
                >
                    <form onSubmit={handleUpdate} className="space-y-4">
                        {editingItem.type === 'news' && (
                            <div>
                                <label className="text-[13px] text-slate-800">Heading</label>
                                <input
                                    name="title"
                                    value={formData.title}
                                    onChange={handleEditFormChange}
                                    placeholder="Введите название новости"
                                    type="text"
                                    className="custom-date-input w-full"
                                />
                            </div>
                        )}
                        <div>
                            <label className="text-[13px] text-slate-800">Content</label>
                            <textarea
                                name="content"
                                value={formData.content}
                                onChange={handleEditFormChange}
                                rows={4}
                                className="custom-date-input w-full resize-y p-4"
                                placeholder="Введите контент..."
                                disabled={editingItem.type === 'currency'}
                            />
                            {editingItem.type === 'currency' && (
                                <p className="text-xs text-red-500 mt-1">
                                    Курсы валют можно редактировать только через форму «Добавить новый контент».
                                </p>
                            )}
                        </div>

                        <div className="flex justify-end space-x-2">
                            <button
                                type="button"
                                onClick={() => setEditingItem(null)}
                                className="secondary-btn"
                            >
                                Отвена
                            </button>
                            <button
                                type="submit"
                                className="add-btn"
                                disabled={editingItem.type === 'currency'}
                            >
                                Сохранить
                            </button>
                        </div>
                    </form>
                </Modal>
            )}
            <Modal

                isOpen={openDeleteAlert.show}

                onClose={() => setOpenDeleteAlert({ show: false, data: null })}

                title="Удалить элемент"

            >

                <DeleteAlert

                    content="Вы уверены, что хотите удалить эту деталь элемента?"

                    onDelete={() => handleDelete(openDeleteAlert.data)}

                />

            </Modal>
        </DashboardLayout>
    );
};

export default ManageContent;