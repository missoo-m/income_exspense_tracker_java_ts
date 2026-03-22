import { useEffect, useMemo, useState } from "react";
import { LuChevronLeft, LuChevronRight } from "react-icons/lu";
import axiosInstance from "../../utils/axiosInstance";
import { API_PATHS } from "../../utils/apiPaths";

const fmt = (d) => {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
};

const SmartCalendar = () => {
  const [month, setMonth] = useState(new Date());
  const [totals, setTotals] = useState({});

  useEffect(() => {
    const load = async () => {
      const from = fmt(new Date(month.getFullYear(), month.getMonth(), 1));
      const to = fmt(new Date(month.getFullYear(), month.getMonth() + 1, 0));
      try {
        const res = await axiosInstance.get(API_PATHS.EXPENSE.GET_ALL_EXPENSE, {
          params: { page: 0, size: 300, from, to },
        });
        const items = Array.isArray(res.data) ? res.data : res.data?.items || [];
        const byDay = {};
        items.forEach((e) => {
          const key = e?.date;
          byDay[key] = (byDay[key] || 0) + (Number(e?.amount) || 0);
        });
        setTotals(byDay);
      } catch {
        setTotals({});
      }
    };
    load();
  }, [month]);

  const { grid, max } = useMemo(() => {
    const y = month.getFullYear();
    const m = month.getMonth();
    const first = new Date(y, m, 1);
    const shift = (first.getDay() + 6) % 7;
    const dim = new Date(y, m + 1, 0).getDate();
    const arr = [];
    for (let i = 0; i < shift; i += 1) arr.push(null);
    for (let d = 1; d <= dim; d += 1) arr.push(new Date(y, m, d));
    while (arr.length % 7 !== 0) arr.push(null);
    return { grid: arr, max: Math.max(0, ...Object.values(totals)) };
  }, [month, totals]);

  const hexToRgb = (hex) => {
    const normalized = hex.replace("#", "");
    const value = parseInt(normalized, 16);
    return {
      r: (value >> 16) & 255,
      g: (value >> 8) & 255,
      b: value & 255,
    };
  };

  const rgbToHex = ({ r, g, b }) =>
    `#${[r, g, b]
      .map((x) => Math.max(0, Math.min(255, Math.round(x))).toString(16).padStart(2, "0"))
      .join("")}`;

  const interpolate = (a, b, t) => ({
    r: a.r + (b.r - a.r) * t,
    g: a.g + (b.g - a.g) * t,
    b: a.b + (b.b - a.b) * t,
  });

  const getHeatColor = (sum) => {
    if (!sum || sum <= 0) {
      return "#ffffff";
    }

    const ratio = max > 0 ? Math.max(0, Math.min(1, sum / max)) : 0;

    // Full gradient scale:
    // 0% #ffe5ec -> 25% #ffcfd2 -> 50% #ffb3c6 -> 75% #ff8fab -> 100% #e11d48
    const stops = [
      { at: 0.0, color: "#ffe5ec" },
      { at: 0.25, color: "#ffcfd2" },
      { at: 0.5, color: "#ffb3c6" },
      { at: 0.75, color: "#ff8fab" },
      { at: 1.0, color: "#e11d48" },
    ];

    for (let i = 0; i < stops.length - 1; i += 1) {
      const left = stops[i];
      const right = stops[i + 1];
      if (ratio >= left.at && ratio <= right.at) {
        const localT = (ratio - left.at) / (right.at - left.at || 1);
        return rgbToHex(interpolate(hexToRgb(left.color), hexToRgb(right.color), localT));
      }
    }

    return "#ffffff";
  };

  return (
    <div className="card">
      <div className="flex items-center justify-between mb-4">
        <h5 className="text-lg">Умный календарь</h5>
        <div className="flex items-center gap-2">
          <button type="button" className="add-btn" onClick={() => setMonth(new Date(month.getFullYear(), month.getMonth() - 1, 1))}>
            <LuChevronLeft />
          </button>
          <div className="text-sm min-w-[150px] text-center font-medium text-[#7f1d3f]">
            {month.toLocaleString("default", { month: "long", year: "numeric" })}
          </div>
          <button type="button" className="add-btn" onClick={() => setMonth(new Date(month.getFullYear(), month.getMonth() + 1, 1))}>
            <LuChevronRight />
          </button>
        </div>
      </div>

      <div className="grid grid-cols-7 gap-2 mb-2 text-xs text-gray-500">
        {["Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"].map((d) => (
          <div key={d} className="text-center">{d}</div>
        ))}
      </div>

      <div className="grid grid-cols-7 gap-2">
        {grid.map((d, i) => {
          if (!d) return <div key={`e-${i}`} className="h-12 rounded-xl bg-white/20" />;
          const key = fmt(d);
          const sum = totals[key] || 0;
          const bg = getHeatColor(sum);
          const textColor = max > 0 && sum / max >= 0.72 ? "#ffffff" : "#4b5563";
          return (
            <div
              key={key}
              title={sum ? `${key}: ${sum.toLocaleString()} потрачено` : `${key}: нет трат`}
              className="min-h-12 h-12 rounded-xl flex items-center justify-center text-sm transition-all duration-300 hover:scale-[1.03]"
              style={{ backgroundColor: bg, color: textColor }}
            >
              {d.getDate()}
            </div>
          );
        })}
      </div>

      <div className="mt-4 flex items-center justify-end gap-2 text-xs text-gray-500">
        <span>Низкие</span>
        <div className="flex items-center gap-1">
          {["#ffe5ec", "#ffcfd2", "#ffb3c6", "#ff8fab", "#e11d48"].map((c) => (
            <span
              key={c}
              className="h-3 w-5 rounded-sm border border-white/50"
              style={{ backgroundColor: c }}
            />
          ))}
        </div>
        <span>Высокие</span>
      </div>
    </div>
  );
};

export default SmartCalendar;
