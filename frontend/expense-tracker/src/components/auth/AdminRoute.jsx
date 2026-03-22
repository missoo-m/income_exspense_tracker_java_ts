import { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { UserContext } from '../../context/userContext';
import { useUserAuth } from '../../hooks/useUserAuth'; 

const AdminRoute = ({ children }) => {
    useUserAuth(); 
    const { user } = useContext(UserContext);

    if (user === null) {
        return <div className="p-8">Loading user data...</div>;
    }

    if (user.role !== 'ADMIN') {
        return <Navigate to="/dashboard" replace />;
    }

    return children;
};

export default AdminRoute;