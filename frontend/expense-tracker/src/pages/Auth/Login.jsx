import { useState } from "react";
import AuthLayout from "../../components/layouts/AuthLayout";
import { Link, useNavigate } from 'react-router-dom';
import Input from "../../components/Inputs/Input";
import { validateEmail } from "../../utils/helper";
import axiosInstance from "../../utils/axiosInstance";
import { API_PATHS } from "../../utils/apiPaths";
import { UserContext } from "../../context/userContext";
import { useContext } from "react";
import SocialLogin from "../../components/SocialLogin";
import OAuthButtons from "../../components/OAuthButtons";

const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);

  const { updateUser } = useContext(UserContext);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();

    if (!validateEmail(email)) {
      setError("Пожалуйста, введите действительный адрес электронной почты");
      return;
    }

    if (!password) {
      setError("Пожалуйста, введите пароль");
      return;
    }

    setError("");

    try {
      const response = await axiosInstance.post(API_PATHS.AUTH.LOGIN, {
        email,
        password,
      });
      const { token, user } = response.data;

      if (token) {
        localStorage.setItem("token", token);
        updateUser(user);
        navigate("/dashboard");
      }
    } catch (error) {
      if (error.response && error.response.data.message) {
        setError(error.response.data.message);
      } else {
        setError("Произошла ошибка. Пожалуйста, попробуйте еще раз.");
      }
    }
  };

  return (
    <AuthLayout>
      <div className="lg:w-[70%] h-3/4 md:h-full flex flex-col justify-center">
        <h3 className="text-xl font-semibold text-black">Добро пожаловать</h3>
        <p className="text-xs text-slate-700 mt-[5px] mb-6">
          Пожалуйста, введите свои данные для входа в систему.
        </p>

        <form onSubmit={handleLogin}>
          <Input
            value={email}
            onChange={({ target }) => setEmail(target.value)}
            label="Адрес электронной почты"
            placeholder="john@example.com"
            type="text"
          />

          <Input
            value={password}
            onChange={({ target }) => setPassword(target.value)}
            label="Пароль"
            placeholder="Минимум 8 символов"
            type="password"
          />

          {error && <p className="text-red-500 text-xs pb-2.5"> {error} </p>}

          <button type="submit" className="btn-primary">
            Войти
          </button>

          {/* Разделитель */}
          <div className="relative my-6">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-300"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-3 bg-white text-gray-500">или</span>
            </div>
          </div>

          {/* Кнопки OAuth в один ряд */}
          <OAuthButtons />

          <p className="text-[13px] text-slate-800 mt-6 text-center">
            У вас нет аккаунта?{" "}
            <Link className="font-medium text-primary underline hover:text-[#e11d48]" to="/signup">
              Зарегистрироваться
            </Link>
          </p>
        </form>
      </div>
    </AuthLayout>
  );
};

export default Login;