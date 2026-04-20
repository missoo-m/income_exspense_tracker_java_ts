import { useContext } from "react"
import { UserContext } from "../context/userContext"
import { useNavigate } from "react-router-dom";
import { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { API_PATHS } from "../utils/apiPaths";
import axiosInstance from "../utils/axiosInstance";



export const useUserAuth = () => {
    const {user, updateUser, clearUser } = useContext(UserContext);
    const navigate = useNavigate();

    const authQuery = useQuery({
        queryKey: ["auth", "me"],
        queryFn: async () => {
            const response = await axiosInstance.get(API_PATHS.AUTH.GET_USER_INFO);
            return response.data;
        },
        enabled: !!localStorage.getItem("token") && !user,
        retry: false,
    });

    useEffect(() => {
        if (authQuery.data) {
            updateUser(authQuery.data);
        }
    }, [authQuery.data, updateUser]);

    useEffect(() => {
        if (authQuery.isError) {
            clearUser();
            navigate("/login");
        }
    }, [authQuery.isError, clearUser, navigate]);

    return authQuery;
};