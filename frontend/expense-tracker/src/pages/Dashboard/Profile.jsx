import { useState, useEffect } from "react";
import DashboardLayout from "../../components/layouts/DashboardLayout";
import Input from "../../components/Inputs/Input";
import ProfilePhotoSelector from "../../components/Inputs/ProfilePhotoSelector";
import axiosInstance from "../../utils/axiosInstance";
import { API_PATHS } from "../../utils/apiPaths";
import { useUserAuth } from "../../hooks/useUserAuth";
import { useContext } from "react";
import { UserContext } from "../../context/userContext";
import toast from "react-hot-toast";

const Profile = () => {
  useUserAuth();

  const { user, updateUser } = useContext(UserContext);

  const [fullName, setFullName] = useState("");
  const [profilePic, setProfilePic] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user) {
      setFullName(user.fullName || "");
      setPreviewUrl(user.profileImageUrl || null);
    }
  }, [user]);

  const handleUpdateProfile = async (e) => {
    e.preventDefault();

    if (!fullName.trim()) {
      toast.error("Имя обязательно");
      return;
    }

    setLoading(true);

    try {
      const formData = new FormData();
      formData.append("fullName", fullName);
      if (profilePic) {
        formData.append("profileImage", profilePic);
      }

      const res = await axiosInstance.put(API_PATHS.AUTH.UPDATE, formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      updateUser(res.data.user);
      toast.success("Профиль успешно обновлен");
      setPreviewUrl(res.data.user.profileImageUrl);
      setProfilePic(null);
    } catch (error) {
      console.error(error);
      toast.error(
        error.response?.data?.message || "Что-то пошло не так. Попробуйте снова"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <DashboardLayout activeMenu="Профиль">
      <div className="max-w-md mx-auto mt-10 p-6 card border-[#ffdde8]">
        <h3 className="text-xl font-semibold mb-4 text-[#7f1d3f]">Редактировать профиль</h3>

        <form onSubmit={handleUpdateProfile}>
          <ProfilePhotoSelector image={profilePic} setImage={setProfilePic} />

          <Input
            value={fullName}
            onChange={({ target }) => setFullName(target.value)}
            label="Полное имя"
            placeholder="Иван Иванов"
            type="text"
          />

          <Input
            value={user?.email || ""}
            label="Email адрес"
            type="text"
            placeholder="Email нельзя изменить"
            disabled
          />

          <button
            type="submit"
            className="add-btn add-btn-fill h-[52px] px-6 text-base mt-4 w-full justify-center"
            disabled={loading}
          >
            {loading ? "Обновление..." : "Обновить профиль"}
          </button>
        </form>
      </div>
    </DashboardLayout>
  );
};

export default Profile;