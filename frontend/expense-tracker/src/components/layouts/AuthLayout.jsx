import { LuTrendingUpDown } from "react-icons/lu"
import AnimatedLineChart from './AnimatedLineChart';
import logo from '../../assets/images/logo.png';

const AuthLayout = ({ children }) => {

  return (
    <div className="flex">
      <div className="w-screen h-screen md:w-[60vw] px-12 pt-8 pb-12">
        {/* Логотип увеличенный */}
        <div className="flex items-center gap-3 mb-8">
          <img 
            src={logo} 
            alt="Логотип" 
            className="w-12 h-12 object-contain"
          />
          <span className="text-2xl font-semibold text-gray-800">Tracker</span>
        </div>
        {children}
      </div>

      <div className="hidden md:block w-[40vw] h-screen bg-[#ffe5ec] bg-auth-bg-img bg-cover bg-no-repeat bg-center overflow-hidden p-8 relative">
        <div className="w-48 h-48 rounded-[40px] bg-[#ff8fab] absolute -top-7 -left-5"/>
        <div className="w-48 h-56 rounded-[40px] border-[20px] border-[#e11d48] absolute top-[30%] -right-10"/>  
        <div className="w-48 h-48 rounded-[40px] bg-[#ff8fab] absolute -bottom-7 -left-5"/>

        <div className="grid grid-cols-1 z-20">
          <StatsInfoCard 
            icon={<LuTrendingUpDown />}
            label="Отслеживайте доходы и расходы"
            value="430,000"
            color="bg-[#e11d48]"
          />
        </div>

        <div className="absolute top-[30%] left-[38%] -translate-x-1/2 w-[65%] h-64 bg-white/80 backdrop-blur-sm rounded-xl shadow-2xl shadow-[#ff8fab]/30 py-8 px-4 z-10 border border-[#ffb3c6]">
          <h3 className="text-sm font-medium text-gray-700 mb-2">Ежемесячный поток</h3>
          <AnimatedLineChart />
        </div>

        <div className="loader absolute bottom-8 left-2/3 -translate-x-1/2 w-80 lg:w-[90%] shadow-lg shadow-[#ff8fab]/15">
          <div className="wrapper">
            <div className="catContainer">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 733 673"
                className="catbody"
              >
                <path
                  fill="currentColor"
                  d="M111.002 139.5C270.502 -24.5001 471.503 2.4997 621.002 139.5C770.501 276.5 768.504 627.5 621.002 649.5C473.5 671.5 246 687.5 111.002 649.5C-23.9964 611.5 -48.4982 303.5 111.002 139.5Z"
                ></path>
                <path fill="currentColor" d="M184 9L270.603 159H97.3975L184 9Z"></path>
                <path fill="currentColor" d="M541 0L627.603 150H454.397L541 0Z"></path>
              </svg>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 158 564"
                className="tail"
              >
                <path
                  fill="currentColor"
                  d="M5.97602 76.066C-11.1099 41.6747 12.9018 0 51.3036 0V0C71.5336 0 89.8636 12.2558 97.2565 31.0866C173.697 225.792 180.478 345.852 97.0691 536.666C89.7636 553.378 73.0672 564 54.8273 564V564C16.9427 564 -5.4224 521.149 13.0712 488.085C90.2225 350.15 87.9612 241.089 5.97602 76.066Z"
                ></path>
              </svg>
              <div className="text">
                <span className="bigzzz">Z</span>
                <span className="zzz">Z</span>
              </div>
            </div>
            <div className="wallContainer">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 500 126"
                className="wall"
              >
                <line strokeWidth="6" stroke="currentColor" y2="3" x2="450" y1="3" x1="50"></line>
                <line strokeWidth="6" stroke="currentColor" y2="85" x2="400" y1="85" x1="100"></line>
                <line strokeWidth="6" stroke="currentColor" y2="122" x2="375" y1="122" x1="125"></line>
                <line strokeWidth="6" stroke="currentColor" y2="43" x2="500" y1="43"></line>
                <line strokeWidth="6" stroke="currentColor" y2="1.99391" x2="115.5" y1="43.0061" x1="115.5"></line>
                <line strokeWidth="6" stroke="currentColor" y2="2.00002" x2="189" y1="43.0122" x1="189"></line>
                <line strokeWidth="6" stroke="currentColor" y2="2.00612" x2="262.5" y1="43.0183" x1="262.5"></line>
                <line strokeWidth="6" stroke="currentColor" y2="2.01222" x2="336" y1="43.0244" x1="336"></line>
                <line strokeWidth="6" stroke="currentColor" y2="2.01833" x2="409.5" y1="43.0305" x1="409.5"></line>
                <line strokeWidth="6" stroke="currentColor" y2="43" x2="153" y1="84.0122" x1="153"></line>
                <line strokeWidth="6" stroke="currentColor" y2="43" x2="228" y1="84.0122" x1="228"></line>
                <line strokeWidth="6" stroke="currentColor" y2="43" x2="303" y1="84.0122" x1="303"></line>
                <line strokeWidth="6" stroke="currentColor" y2="43" x2="378" y1="84.0122" x1="378"></line>
                <line strokeWidth="6" stroke="currentColor" y2="84" x2="192" y1="125.012" x1="192"></line>
                <line strokeWidth="6" stroke="currentColor" y2="84" x2="267" y1="125.012" x1="267"></line>
                <line strokeWidth="6" stroke="currentColor" y2="84" x2="342" y1="125.012" x1="342"></line>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AuthLayout;

const StatsInfoCard = ({ icon, label, value, color }) => {
  return (
    <div className="flex gap-6 bg-white p-4 rounded-xl shadow-md shadow-pink-400/10 border border-gray-200/50 z-10">
      <div className={`w-12 h-12 flex items-center justify-center text-[26px] text-white ${color} rounded-full drop-shadow-xl`}>
        {icon}
      </div>
      <div>
        <h6 className="text-xs text-gray-500 mb-1">{label}</h6>
        <span className="text-[20px] ">${value}</span>
      </div>
    </div>
  );
};