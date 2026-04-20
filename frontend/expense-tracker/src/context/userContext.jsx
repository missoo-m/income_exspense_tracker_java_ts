import { createContext } from "react";
import { useAuthStore } from "../store/authStore";

export const UserContext = createContext();

const UserProvider = ({ children }) => {
    const user = useAuthStore((state) => state.user);
    const updateUserStore = useAuthStore((state) => state.updateUser);
    const clearAuth = useAuthStore((state) => state.clearAuth);

    const updateUser = (userData) => {
        updateUserStore(userData);
    };

    const clearUser = () => {
        clearAuth();
    };

    return (
        <UserContext.Provider
        value= {{
            user,
            updateUser,
            clearUser,
        }}
        >
            { children }
        </UserContext.Provider>
    );
}

export default UserProvider;