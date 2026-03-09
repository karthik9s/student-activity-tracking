import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ children, allowedRoles }) => {
  const { isAuthenticated, user, loading } = useAuth();

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    // User is authenticated but doesn't have the right role
    // Redirect to their appropriate dashboard
    if (user?.role === 'ROLE_ADMIN') {
      return <Navigate to="/admin" replace />;
    } else if (user?.role === 'ROLE_FACULTY') {
      return <Navigate to="/faculty" replace />;
    } else if (user?.role === 'ROLE_STUDENT') {
      return <Navigate to="/student" replace />;
    }
    // Fallback to login if role is unknown
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default ProtectedRoute;
