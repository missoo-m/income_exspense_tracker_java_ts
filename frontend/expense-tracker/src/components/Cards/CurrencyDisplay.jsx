import { LuDollarSign, LuEuro, LuRussianRuble, LuJapaneseYen, LuClock } from 'react-icons/lu';

const currencyIcons = {
    USD: LuDollarSign,
    EUR: LuEuro,
    RUB: LuRussianRuble,
    YEN: LuJapaneseYen,
};
const formatUpdateDate = (dateString) => {
    if (!dateString) {
        return 'Дата неизвестна';
    }
    const dateObj = new Date(dateString);

    if (isNaN(dateObj.getTime())) {
        return 'Дата неверна';
    }

    return `${dateObj.toLocaleDateString()} в ${dateObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
};

const CurrencyDisplay = ({ currencyData, loading }) => {
    
    if (loading) {
        return (
            <div className="p-4 rounded-xl border border-[#ffe5ec] bg-white animate-pulse">
                <p className="text-gray-500 text-center text-sm">Скачивание курсов...</p>
            </div>
        );
    }

    if (!currencyData || !currencyData.rates || Object.keys(currencyData.rates).length === 0) {
        return (
            <div className="p-4 rounded-xl border border-[#ffe5ec] bg-white">
                <p className="text-gray-600 font-medium text-sm">Доступных обменных курсов для отображения нет.</p>
            </div>
        );
    }
    
    const rates = currencyData.rates;
    
    return (
        <div className="space-y-3">
            <h3 className="text-xl font-bold text-[#7f1d3f] mb-4">Курсы валют</h3>
            
            {Object.entries(rates).map(([code, rate]) => {
                const IconComponent = currencyIcons[code] || LuDollarSign;
                
                return (
                    <div 
                        key={code}
                        className="p-4 bg-white rounded-xl border border-[#ffe5ec] transition-all duration-300 hover:shadow-md hover:translate-y-[-1px]"
                    >
                        <div className="flex items-center justify-between">
                            
                            <div className="flex items-center">
                                <span className="p-1 rounded-full mr-3 bg-[#ffe5ec] text-[#e11d48]">
                                    <IconComponent size={18} /> 
                                </span>
                                <span className="text-lg font-bold text-gray-800">{code}</span> 
                            </div>
                            <span className="text-xl font-extrabold text-gray-900 tracking-tight">
                                {rate.toFixed(2)}
                            </span>
                        </div>
                    </div>
                );
            })}
            <div className="flex items-center text-xs text-gray-500 mt-3 px-1.5">
                <LuClock size={14} className="mr-1" />
                <span> Updated: {formatUpdateDate(currencyData.date)}</span>
            </div>
        </div>
    );
};

export default CurrencyDisplay;