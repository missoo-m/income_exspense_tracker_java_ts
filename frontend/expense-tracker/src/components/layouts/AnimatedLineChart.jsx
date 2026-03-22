import React from 'react';
import { 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  Tooltip, 
  ResponsiveContainer 
} from 'recharts';

const data = [
  { name: 'Янв', income: 4000, expenses: 2400 },
  { name: 'Фев', income: 3000, expenses: 1398 },
  { name: 'Мар', income: 2000, expenses: 9800 },
  { name: 'Апр', income: 2780, expenses: 3908 },
  { name: 'Май', income: 1890, expenses: 4800 },
  { name: 'Июн', income: 2390, expenses: 3800 },
];

const PRIMARY_PINK = '#e11d48'; 
const LIGHT_PINK = '#ff8fab';  

const AnimatedLineChart = () => {
  return (
    <ResponsiveContainer width="100%" height="100%">
      <AreaChart
        data={data}
        margin={{ top: 10, right: 10, left: -25, bottom: 0 }}
      >
        {/* Градиент для заливки области под линией (Area) */}
        <defs>
          <linearGradient id="colorIncome" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor={LIGHT_PINK} stopOpacity={0.8}/>
            <stop offset="95%" stopColor={LIGHT_PINK} stopOpacity={0}/>
          </linearGradient>
          <linearGradient id="colorExpenses" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor={PRIMARY_PINK} stopOpacity={0.8}/>
            <stop offset="95%" stopColor={PRIMARY_PINK} stopOpacity={0}/>
          </linearGradient>
        </defs>

        {/* Ось X (Месяцы) */}
        <XAxis dataKey="name" stroke="#a1a1aa" tickLine={false} />
        
        {/* Ось Y (Значения) - Скрываем для чистоты дизайна */}
        <YAxis hide={true} domain={[0, 'dataMax + 1000']} />
        
        {/* Всплывающая подсказка при наведении */}
        <Tooltip 
            contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} 
            formatter={(value, name) => [`$${value.toLocaleString()}`, name === 'income' ? 'Доход' : 'Расход']}
        />

        {/* Линия Дохода (Area) */}
        <Area 
          type="monotone" 
          dataKey="income" 
          stroke={LIGHT_PINK} 
          fillOpacity={1} 
          fill="url(#colorIncome)" 
          dot={false}
          strokeWidth={3}
          isAnimationActive={true} 
          animationDuration={1500}
        />

        {/* Линия Расходов (Area) */}
        <Area 
          type="monotone" 
          dataKey="expenses" 
          stroke={PRIMARY_PINK} 
          fillOpacity={1} 
          fill="url(#colorExpenses)" 
          dot={false}
          strokeWidth={3}
          isAnimationActive={true}
          animationDuration={2000} 
        />
        
      </AreaChart>
    </ResponsiveContainer>
  );
};

export default AnimatedLineChart;