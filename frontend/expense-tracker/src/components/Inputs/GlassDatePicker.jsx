import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

const toYMD = (date) => {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
};

const toYM = (date) => {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  return `${y}-${m}`;
};

const GlassDatePicker = ({ label, value, onChange, picker = "date" }) => {
  const selected = value ? new Date(picker === "month" ? `${value}-01` : value) : null;
  const isMonthPicker = picker === "month";

  return (
    <div className="min-w-[210px]">
      {label ? <label className="text-xs text-gray-500">{label}</label> : null}
      <DatePicker
        selected={selected}
        onChange={(date) => onChange(date ? (isMonthPicker ? toYM(date) : toYMD(date)) : "")}
        dateFormat={isMonthPicker ? "yyyy-MM" : "yyyy-MM-dd"}
        placeholderText={isMonthPicker ? "YYYY-MM" : "YYYY-MM-DD"}
        showMonthYearPicker={isMonthPicker}
        className="custom-date-input"
        calendarClassName="glass-datepicker"
        popperClassName="glass-datepicker-popper"
        showPopperArrow={false}
      />
    </div>
  );
};

export default GlassDatePicker;
