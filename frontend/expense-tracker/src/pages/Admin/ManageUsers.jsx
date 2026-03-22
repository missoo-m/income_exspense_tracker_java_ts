
import DashboardLayout from "../../components/layouts/DashboardLayout";
import { useUserAuth } from "../../hooks/useUserAuth";
import { API_PATHS } from "../../utils/apiPaths";
import axiosInstance from "../../utils/axiosInstance";
import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { LuTrash2 } from "react-icons/lu";
import Modal from "../../components/Modal";
import DeleteAlert from "../../components/DeleteAlert";

const ManageUsers = () => {
    useUserAuth();
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [openDeleteAlert, setOpenDeleteAlert] = useState({
        show: false,
        data: null,
    })

    const fetchUsers = async () => {
        try {
            setLoading(true);
            const response = await axiosInstance.get(API_PATHS.ADMIN.GET_ALL_USERS);
            const data = response.data;
            const list = Array.isArray(data) ? data : Array.isArray(data?.items) ? data.items : [];
            setUsers(list.map((u) => ({ ...u, _id: u?._id ?? u?.id })));
        } catch (error) {
            toast.error(error?.response?.data?.message || `Не удалось загрузить пользователей (${error?.response?.status || "сеть"})`);
            console.error("Ошибка при получении данных о пользователях:", error?.response?.status, error?.response?.data || error);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (userId) => {
        try {
            await axiosInstance.delete(API_PATHS.ADMIN.DELETE_USER(userId));

            setOpenDeleteAlert({ show: false, data: null });
            toast.success("Данные пользователя успешно удалены");
            fetchUsers();
        } catch (error) {
            toast.error(error?.response?.data?.message || `Не удалось удалить пользователя. (${error?.response?.status || "network"})`);
            console.error("Ошибка при удалении пользователя:", error?.response?.status, error?.response?.data || error);
        }
    };




    useEffect(() => {
        fetchUsers();
    }, []);

    return (
        <DashboardLayout activeMenu="Manage Users">
            <h2 className="text-xl font-bold text-[#7f1d3f] mb-6">Управление пользователями</h2>

            {loading ? (
                <div className="card p-6 border-[#ffdde8]">
                    <p className="text-center text-gray-500">Загрузка...</p>
                </div>
            ) : (
                <div className="card p-6 overflow-x-auto border-[#ffdde8]">
                    <div className="flex items-center justify-end mb-4">
                        <button type="button" className="add-btn add-btn-fill h-[52px] px-6 text-base" onClick={fetchUsers}>
                            Обновить
                        </button>
                    </div>
                    <table className="min-w-full divide-y divide-[#ffb3c6]">

                        <thead className="bg-[#ffe5ec] rounded-t-2xl">
                            <tr>
                                <th className="px-6 py-4 text-left text-xs font-bold text-[#e11d48] uppercase tracking-wider">ФИО</th>
                                <th className="px-6 py-4 text-left text-xs font-bold text-[#e11d48] uppercase tracking-wider">Почта</th>
                                <th className="px-6 py-4 text-left text-xs font-bold text-[#e11d48] uppercase tracking-wider">Роль</th>
                                <th className="px-6 py-4 text-left text-xs font-bold text-[#e11d48] uppercase tracking-wider">Действия</th>
                            </tr>
                        </thead>

                        <tbody className="bg-white divide-y divide-gray-100">
                            {users.map((user) => (
                                <tr key={user._id} className="hover:bg-[#fff7f8] transition duration-150">
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-gray-900">{user.fullName}</td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{user.email}</td>

                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <span className={`
                                px-3 py-1 text-xs font-semibold rounded-full 
                                ${user.role === 'ADMIN'
                                                ? 'bg-[#ffc2d1] text-[#e11d48]' 
                                                : 'bg-green-100 text-green-600' 
                                            }
                            `}>
                                            {user.role}
                                        </span>
                                    </td>

                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                        <button
                                            onClick={() => setOpenDeleteAlert({ show: true, data: user._id })}
                                            className={`
                                    custom-date-input h-10 px-3 w-auto
                                    transition duration-200
                                    ${user.role === 'ADMIN'
                                                    ? 'text-gray-400 cursor-not-allowed'
                                                    : 'text-red-500 bg-white hover:bg-[#ffe5ec] hover:text-red-600' 
                                                }
                                `}
                                            disabled={user.role === 'ADMIN'}
                                            title={user.role === 'ADMIN' ? 'Cant remove administrator' : 'Delete user'}
                                        >
                                            <LuTrash2 size={18} />
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                    {!users.length && (
                        <div className="p-4 text-sm text-gray-500">
                            No users found (or request failed). Check browser Network for `GET /api/v1/admin/users`.
                        </div>
                    )}
                    <Modal
                        isOpen={openDeleteAlert.show}
                        onClose={() => setOpenDeleteAlert({ show: false, data: null })}
                        title="Delete user"
                    >
                        <DeleteAlert
                            content={`Are you sure you want to delete the user?`}
                            onDelete={() => handleDelete(openDeleteAlert.data)}
                        />
                    </Modal>
                </div>
            )}


        </DashboardLayout>
    );
};

export default ManageUsers;