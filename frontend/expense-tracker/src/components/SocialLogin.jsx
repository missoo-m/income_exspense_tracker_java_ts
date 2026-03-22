import React from 'react';
import { FaTiktok, FaGithub, FaLinkedinIn, FaInstagram, FaGoogle } from 'react-icons/fa'; 


const SocialLogin = () => {
  const ulClasses = "flex justify-center items-center mt-6 mb-4"; 
  const iconBaseClasses = "relative mx-2"; 
  
  const linkBaseClasses = "relative overflow-hidden flex justify-center items-center w-12 h-12 rounded-full text-gray-700 bg-white transition-all duration-300 ease-in-out hover:shadow-xl hover:text-white group"; 
  
  // Компонент для "заливки" при наведении
  const FilledEffect = ({ bgColorClass = 'bg-black' }) => (
    <div className={`absolute top-auto bottom-0 left-0 w-full h-0 transition-all duration-300 ease-in-out group-hover:h-full ${bgColorClass}`}></div>
  );

  const Tooltip = ({ children, bgColorClass = 'bg-black' }) => (
    <div 
      className={`absolute left-1/2 -top-10 -translate-x-1/2 text-white px-2 py-1 rounded text-xs opacity-0 invisible transition-all duration-300 ease group-hover:opacity-100 group-hover:visible group-hover:-top-12 ${bgColorClass}`}
      style={{ whiteSpace: 'nowrap' }}
    >
      {children}
    </div>
  );

  return (
    <>
      
      {/* Иконки соцсетей в одну линию */}
      <ul className={ulClasses}>
       
      {/* LinkedIn */}
      <li className={iconBaseClasses + " group"}>
        <a href="https://linkedin.com/" aria-label="LinkedIn" className={linkBaseClasses}>
          <FilledEffect bgColorClass="bg-[#0274b3]" /> 
          <FaLinkedinIn className="relative z-10 w-6 h-6" /> {/* Готовая иконка */}
        </a>
        <Tooltip bgColorClass="bg-[#0274b3]">LinkedIn</Tooltip>
      </li>

      {/* GitHub */}
      <li className={iconBaseClasses + " group"}>
        <a href="https://github.com/missoo-m" aria-label="GitHub" className={linkBaseClasses}>
          <FilledEffect bgColorClass="bg-[#24262a]" />
          <FaGithub className="relative z-10 w-6 h-6" /> {/* Готовая иконка */}
        </a>
        <Tooltip bgColorClass="bg-[#24262a]">GitHub</Tooltip>
      </li>

      {/* Instagram */}
      <li className={iconBaseClasses + " group"}>
        <a href="https://www.instagram.com/geell.ya/" aria-label="Instagram" className={linkBaseClasses}>
          
          <div className="absolute top-auto bottom-0 left-0 w-full h-0 transition-all duration-300 ease-in-out group-hover:h-full bg-gradient-to-r from-[#405de6] via-[#b33ab4] to-[#e1306c]"></div>
          <FaInstagram className="relative z-10 w-6 h-6" /> {/* Готовая иконка */}
        </a>
        <Tooltip bgColorClass="bg-gradient-to-r from-[#405de6] via-[#b33ab4] to-[#e1306c]">Instagram</Tooltip>
      </li>

      <li className={iconBaseClasses + " group"}>
        <a href="https://www.tiktok.com/@angeeell.ya" aria-label="TikTok" className={linkBaseClasses}>
          {/* Цвет TikTok - черный  */}
          <FilledEffect bgColorClass="bg-black" /> 
          <FaTiktok className="relative z-10 w-6 h-6" /> {/* Готовая иконка */}
        </a>
        <Tooltip bgColorClass="bg-black">TikTok</Tooltip>
      </li>
    </ul>
    
    </>

  );
};

export default SocialLogin;