import DashboardLayout from "../../components/layouts/DashboardLayout";
import { useUserAuth } from "../../hooks/useUserAuth";
import { API_PATHS } from "../../utils/apiPaths";
import axiosInstance from "../../utils/axiosInstance";
import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import CurrencyDisplay from "../../components/Cards/CurrencyDisplay";
import TorchCube from "../../components/TorchCube"; 

const News = () => {
    useUserAuth(); 
    
    const [news, setNews] = useState([]);
    const [currencyData, setCurrencyData] = useState(null); 
    const [loading, setLoading] = useState(true);

    const fetchContent = async () => {
        try {
            const results = await Promise.allSettled([
                axiosInstance.get(API_PATHS.PUBLIC.GET_NEWS),
                axiosInstance.get(API_PATHS.PUBLIC.GET_CURRENCIES),
            ]);

            const [newsResult, currencyResult] = results;

            if (newsResult.status === "fulfilled") {
                setNews(Array.isArray(newsResult.value.data) ? newsResult.value.data : []);
            } else {
                setNews([]);
                console.error("Ошибка загрузки новостей:", newsResult.reason);
            }

            if (currencyResult.status === "fulfilled") {
                const raw = currencyResult.value.data || {};
                const processedCurrencyData = {
                    ...raw,
                    date: raw.date || new Date().toISOString(),
                };
                setCurrencyData(processedCurrencyData);
            } else {
                setCurrencyData(null);
                console.error("Ошибка загрузки курсов валют:", currencyResult.reason);
            }
            
        } catch (error) {
            if (error.response?.status === 404 && error.config.url.includes('currencies')) {
            } else {
                toast.error("Не удалось загрузить контент.");
                console.error("Ошибка загрузки контента:", error);
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchContent();
    }, []);

    return (
        <DashboardLayout activeMenu="Новости и курсы">
            <h4 className="text-xl font-bold text-[#7f1d3f] mb-6">Новости и курсы валют</h4>
            
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6"> 
            
                <div 
                    className="col-span-1 h-fit"
                >
                    <CurrencyDisplay currencyData={currencyData} loading={loading} />
                    <TorchCube />
                </div>

                <div className="col-span-1 lg:col-span-2 space-y-4"> 
                    
                    <h3 className="text-xl font-bold text-[#7f1d3f] mb-1">Последние новости</h3>
                    
                    {loading && news.length === 0 ? (
                        <div className="p-4 rounded-xl border border-[#ffe5ec] bg-white">
                             <p className="text-center text-sm text-gray-500">Загрузка новостей...</p>
                        </div>
                    ) : news.length > 0 ? (
                        
                        news.map((item, index) => (
                            <div 
                                key={`${item._id}-${index}`}
                                className="p-4 bg-white rounded-xl border border-[#ffe5ec] transition-all duration-300 hover:shadow-md hover:translate-y-[-1px]"
                            >
                                <h4 className="text-lg font-bold text-gray-800">{item.title}</h4>
                                <p className="text-xs font-medium text-gray-500 mb-2">
                                    Опубликовано: {new Date(item.date || item.createdAt).toLocaleDateString()}
                                </p>
                                <p className="text-sm text-gray-700">{item.content}</p>
                            </div>
                        ))
                        
                    ) : (
                        <p className="text-gray-600 p-4 bg-white rounded-xl border border-[#ffe5ec]">Нет доступных новостей.</p>
                    )}
                </div>
            </div>
            
        </DashboardLayout>
    );
};

export default News;