import {
    LuLayoutDashboard ,
    LuHandCoins,
    LuWalletMinimal,
    LuLogOut,
    LuUsers,        
    LuGlobe,        
    LuBell,
    LuPiggyBank,
    LuBadgeDollarSign,
} from "react-icons/lu";
import { HiOutlineUser } from "react-icons/hi"; 

export const SIDE_MENU_DATA = [
 {
    id: "01",
    label: "Главная",
    icon: LuLayoutDashboard,
    path: "/dashboard",
 },
 {
    id: "02",
    label: "Доходы",
    icon: LuWalletMinimal,
    path: "/income",
 },
 {
    id: "03",
    label: "Расходы",
    icon: LuHandCoins,
    path: "/expense",
 },

  {
    id: "04",
    label: "Новости и курсы",
    icon: LuGlobe,
    path: "/news",
    role: "NORMAL", 
  },
  {
    id: "04b",
    label: "Бюджеты",
    icon: LuPiggyBank,
    path: "/budgets",
  },
  {
    id: "04c",
    label: "Уведомления",
    icon: LuBell,
    path: "/notifications",
  },
  {
    id: "04d",
    label: "Подписки",
    icon: LuBadgeDollarSign,
    path: "/subscriptions",
  },

  {
    id: "05",                
    label: "Профиль",
    icon: HiOutlineUser,
    path: "/profile-edit",
 },

  {
    id: "07",
    label: "Пользователи",
    icon: LuUsers,
    path: "/admin/users",
    role: "ADMIN", 
  },
  {
    id: "08",
    label: "Контент",
    icon: LuGlobe,
    path: "/admin/content",
    role: "ADMIN", 
  },

 {
    id: "09",
    label: "Выход",
    icon: LuLogOut,
    path: "logout",
 },
];