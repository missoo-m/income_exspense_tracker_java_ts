import AuthLayout from "../../components/layouts/AuthLayout";
import { Link, useNavigate } from 'react-router-dom';
import Input from "../../components/Inputs/Input";
import axiosInstance from "../../utils/axiosInstance";
import { API_PATHS } from "../../utils/apiPaths";
import { UserContext } from "../../context/userContext";
import { useContext } from "react";
import OAuthButtons from "../../components/OAuthButtons";
import { useMutation } from "@tanstack/react-query";
import { useForm, Controller } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";

const loginSchema = z.object({
  email: z.string().trim().email("Пожалуйста, введите действительный адрес электронной почты"),
  password: z.string().min(1, "Пожалуйста, введите пароль"),
});

const Login = () => {
  const { updateUser } = useContext(UserContext);
  const navigate = useNavigate();

  const {
    control,
    handleSubmit,
    formState: { errors },
    setError,
  } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const loginMutation = useMutation({
    mutationFn: async ({ email, password }) => {
      const response = await axiosInstance.post(API_PATHS.AUTH.LOGIN, {
        email,
        password,
      });
      return response.data;
    },
    onSuccess: ({ token, user }) => {
      if (token) {
        localStorage.setItem("token", token);
        updateUser(user);
        navigate("/dashboard");
      }
    },
    onError: (error) => {
      const errorMessage = error?.response?.data?.message
        || "Произошла ошибка. Пожалуйста, попробуйте еще раз.";
      setError("root", { message: errorMessage });
    },
  });

  const handleLogin = async (data) => {
    loginMutation.mutate(data);
  };

  return (
    <AuthLayout>
      <div className="lg:w-[70%] h-3/4 md:h-full flex flex-col justify-center">
        <h3 className="text-xl font-semibold text-black">Добро пожаловать</h3>
        <p className="text-xs text-slate-700 mt-[5px] mb-6">
          Пожалуйста, введите свои данные для входа в систему.
        </p>

        <form onSubmit={handleSubmit(handleLogin)}>
          <Controller
            name="email"
            control={control}
            render={({ field }) => (
              <Input
                value={field.value}
                onChange={field.onChange}
                label="Адрес электронной почты"
                placeholder="john@example.com"
                type="text"
              />
            )}
          />
          {errors.email && <p className="text-red-500 text-xs pb-2.5"> {errors.email.message} </p>}

          <Controller
            name="password"
            control={control}
            render={({ field }) => (
              <Input
                value={field.value}
                onChange={field.onChange}
                label="Пароль"
                placeholder="Минимум 8 символов"
                type="password"
              />
            )}
          />
          {errors.password && <p className="text-red-500 text-xs pb-2.5"> {errors.password.message} </p>}

          {errors.root?.message && <p className="text-red-500 text-xs pb-2.5"> {errors.root.message} </p>}

          <button type="submit" className="btn-primary" disabled={loginMutation.isPending}>
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