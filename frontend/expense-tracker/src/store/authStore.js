import { create } from "zustand";

const getInitialToken = () => localStorage.getItem("token");

export const useAuthStore = create((set) => ({
  user: null,
  token: getInitialToken(),

  updateUser: (userData) => set({ user: userData }),

  setAuth: ({ token, user }) => {
    if (token) {
      localStorage.setItem("token", token);
    }

    set({
      token: token || null,
      user: user || null,
    });
  },

  clearAuth: () => {
    localStorage.removeItem("token");
    set({
      token: null,
      user: null,
    });
  },
}));
