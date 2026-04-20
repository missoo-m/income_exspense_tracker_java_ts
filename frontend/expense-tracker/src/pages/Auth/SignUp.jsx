import { useState } from "react";
import AuthLayout from "../../components/layouts/AuthLayout";
import { Link, useNavigate } from 'react-router-dom';
import Input from "../../components/Inputs/Input";
import ProfilePhotoSelector from "../../components/Inputs/ProfilePhotoSelector";
import axiosInstance from "../../utils/axiosInstance";
import { API_PATHS } from "../../utils/apiPaths";
import { useContext } from "react";
import { UserContext } from "../../context/userContext";
import uploadImage from "../../utils/uploadImage";
import OAuthButtons from "../../components/OAuthButtons";
import { Controller, useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";

const signUpSchema = z.object({
  fullName: z.string().trim().min(1, "Пожалуйста, введите ваше имя"),
  email: z.string().trim().email("Пожалуйста, введите действительный адрес электронной почты"),
  password: z.string().min(8, "Пароль должен содержать минимум 8 символов"),
});

const SignUp = () => {
  const [profilePic, setProfilePic] = useState(null);

  const { updateUser } = useContext(UserContext);
  const navigate = useNavigate();

  const {
    control,
    handleSubmit,
    formState: { errors },
    setError,
  } = useForm({
    resolver: zodResolver(signUpSchema),
    defaultValues: {
      fullName: "",
      email: "",
      password: "",
    },
  });

  const signUpMutation = useMutation({
    mutationFn: async ({ fullName, email, password }) => {
      let profileImageUrl = "";
      if (profilePic) {
        const imgUploadRes = await uploadImage(profilePic);
        profileImageUrl = imgUploadRes.imageURL || "";
      }

      const response = await axiosInstance.post(API_PATHS.AUTH.REGISTER, {
        fullName,
        email,
        password,
        profileImageUrl
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

  const handleSignUp = async (data) => {
    signUpMutation.mutate(data);
  };

  return (
    <AuthLayout>
      <div className="lg:w-[100%] h-auto md:h-full mt-10 md:mt-0 flex flex-col justify-center">
        <h3 className="text-xl font-semibold text-black">Создать аккаунт</h3>
        <p className="text-xs text-slate-700 mt-[5px] mb-6">
          Присоединяйтесь к нам, заполнив форму ниже
        </p>

        <form onSubmit={handleSubmit(handleSignUp)}>
          <ProfilePhotoSelector image={profilePic} setImage={setProfilePic} />

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Controller
              name="fullName"
              control={control}
              render={({ field }) => (
                <Input
                  value={field.value}
                  onChange={field.onChange}
                  label="Полное имя"
                  placeholder="Иван Иванов"
                  type="text"
                />
              )}
            />

            <Controller
              name="email"
              control={control}
              render={({ field }) => (
                <Input
                  value={field.value}
                  onChange={field.onChange}
                  label="Адрес электронной почты"
                  placeholder="ivan@example.com"
                  type="text"
                />
              )}
            />

            <div className="col-span-2">
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
            </div>
          </div>

          {errors.fullName && <p className="text-red-500 text-xs pb-2.5"> {errors.fullName.message} </p>}
          {errors.email && <p className="text-red-500 text-xs pb-2.5"> {errors.email.message} </p>}
          {errors.password && <p className="text-red-500 text-xs pb-2.5"> {errors.password.message} </p>}
          {errors.root?.message && <p className="text-red-500 text-xs pb-2.5"> {errors.root.message} </p>}

          <button type="submit" className="btn-primary" disabled={signUpMutation.isPending}>
            Зарегистрироваться
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

          {/* Кнопки OAuth */}
          <OAuthButtons />

          <p className="text-[13px] text-slate-800 mt-6 text-center">
            Уже есть аккаунт?{" "}
            <Link className="font-medium text-primary underline hover:text-[#e11d48]" to="/login">
              Войти
            </Link>
          </p>
        </form>
      </div>
    </AuthLayout>
  );
};

export default SignUp;