import './TorchCube.css'; 

const TorchCube = () => {
    return (
        <div className="p-4 bg-gray-800 rounded-xl shadow-lg flex flex-col justify-between items-center h-80 relative"> 
            
            <div className="flex-grow w-full flex justify-center items-start pt-6">
                <label className="container" htmlFor="torch-checkbox">
                    <input type="checkbox" id="torch-checkbox" />
                    
                    <div className="torch">
                        <div className="light-effect"></div>
                        <div className="glow-effect"></div>
                        
                        <div className="particles">
                            <span></span><span></span><span></span><span></span>
                            <span></span><span></span><span></span><span></span>
                        </div>
                        <div className="smoke">
                            <span></span><span></span><span></span><span></span>
                        </div>
                        
                        <div className="head">
                            <div className="face top"><div></div><div></div><div></div><div></div></div>
                            <div className="face left"><div></div><div></div><div></div><div></div></div>
                            <div className="face right"><div></div><div></div><div></div><div></div></div>
                        </div>
                        
                        <div className="stick">
                            <div className="side side-left">
                                <div></div><div></div><div></div><div></div>
                                <div></div><div></div><div></div><div></div>
                                <div></div><div></div><div></div><div></div>
                                <div></div><div></div><div></div><div></div>
                            </div>
                            <div className="side side-right">
                                <div></div><div></div><div></div><div></div>
                                <div></div><div></div><div></div><div></div>
                                <div></div><div></div><div></div><div></div>
                                <div></div><div></div><div></div><div></div>
                            </div>
                        </div>
                    </div>

                    <div className="simple-text">Click to light</div>
                
                </label>
            </div>
           
            <div className="w-full text-center pb-1"> 
                 <p className="text-white text-[10px] font-light tracking-wider opacity-60">
                    * Здесь могла быть ваша реклама
                 </p>
            </div>
        </div>
    );
};

export default TorchCube;