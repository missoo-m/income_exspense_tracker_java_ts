//export const BASE_URL = "http://backend:8000";
// export const BASE_URL = "http://localhost:8000";
// Бэкенд на Java работает на порту 5001
export const BASE_URL = "http://localhost:5001";

export const API_PATHS ={
    AUTH: {
        LOGIN: "/api/v1/auth/login",
        REGISTER: "/api/v1/auth/register",
        GET_USER_INFO: "/api/v1/auth/getUser",
        UPDATE: "/api/v1/auth/update",

    },

    ADMIN: {
        GET_ALL_USERS: "/api/v1/admin/users",
        DELETE_USER: (userId) => `/api/v1/admin/users/${userId}`,
        ADD_CONTENT: "/api/v1/admin/content", 
        GET_ALL_CONTENT: "/api/v1/admin/content/admin", 
        UPDATE_CONTENT: (id) => `/api/v1/admin/content/${id}`, 
        DELETE_CONTENT: (id) => `/api/v1/admin/content/${id}`,
    },
    PUBLIC: {
        GET_NEWS: "/api/v1/public/news",
        GET_CURRENCIES: "/api/v1/public/currencies",
    },

    DASHBOARD:{
        GET_DATA: "/api/v1/dashboard",
    },
    BUDGET: {
        GET_BY_MONTH: (month) => `/api/v1/budgets?month=${encodeURIComponent(month)}`,
        UPSERT: "/api/v1/budgets",
        DELETE: (id) => `/api/v1/budgets/${id}`,
    },
    NOTIFICATION: {
        LIST: "/api/v1/notifications",
        MARK_READ: (id) => `/api/v1/notifications/${id}/read`,
    },
    CATEGORY: {
        GET_ALL: "/api/v1/categories",
        ADD: "/api/v1/categories",
        UPDATE: (id) => `/api/v1/categories/${id}`,
        DELETE: (id) => `/api/v1/categories/${id}`,
    },
    INCOME: {
        ADD_INCOME: "/api/v1/income/add",
        GET_ALL_INCOME: "/api/v1/income/get",
        DELETE_INCOME: (incomeId) => `/api/v1/income/${incomeId}`,
        DOWNLOAD_INCOME: `/api/v1/income/downloadexcel`,
    },
    EXPENSE:{
        ADD_EXPENSE:"/api/v1/expense/add",
        GET_ALL_EXPENSE: "/api/v1/expense/get",
        DELETE_EXPENSE: (expenseId) => `/api/v1/expense/${expenseId}`,
        DOWNLOAD_EXPENSE: `/api/v1/expense/downloadexcel`,
    },
    IMAGE: {
        UPLOAD_IMAGE: "/api/v1/auth/upload-image",
    }
}