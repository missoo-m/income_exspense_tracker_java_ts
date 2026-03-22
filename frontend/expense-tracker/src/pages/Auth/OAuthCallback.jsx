import { useEffect, useContext } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axiosInstance from "../../utils/axiosInstance";
import { API_PATHS } from "../../utils/apiPaths";
import { UserContext } from "../../context/userContext";

const OAuthCallback = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { updateUser, clearUser } = useContext(UserContext);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const token = params.get("token");

    const run = async () => {
      if (!token) {
        clearUser();
        navigate("/login");
        return;
      }

      localStorage.setItem("token", token);
      try {
        const res = await axiosInstance.get(API_PATHS.AUTH.GET_USER_INFO);
        updateUser(res.data);
        navigate("/dashboard");
      } catch {
        localStorage.removeItem("token");
        clearUser();
        navigate("/login");
      }
    };

    run();
  }, [location.search, navigate, updateUser, clearUser]);

  return (
    <div className="w-screen h-screen flex items-center justify-center">
      <p className="text-sm text-gray-600">Signing you in...</p>
    </div>
  );
};

export default OAuthCallback;

