import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { toast } from 'react-toastify';
import { useAuth } from '../../context/AuthContext';
import { login as loginApi } from '../../api/endpoints/authApi';
import './Login.css';

const loginSchema = Yup.object().shape({
  email: Yup.string()
    .email('Invalid email address')
    .required('Email is required'),
  password: Yup.string()
    .min(6, 'Password must be at least 6 characters')
    .required('Password is required'),
  rememberMe: Yup.boolean(),
});

const Login = () => {
  const navigate = useNavigate();
  const { login, isAuthenticated, user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showDemoCredentials, setShowDemoCredentials] = useState(false);

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated && user) {
      if (user.role === 'ROLE_ADMIN') {
        navigate('/admin', { replace: true });
      } else if (user.role === 'ROLE_FACULTY') {
        navigate('/faculty', { replace: true });
      } else if (user.role === 'ROLE_STUDENT') {
        navigate('/student', { replace: true });
      }
    }
  }, [isAuthenticated, user, navigate]);

  const demoCredentials = [
    { role: 'Admin', email: 'admin@cvr.ac.in', password: 'admin123' },
    { role: 'Faculty', email: 'rajesh.kumar@cvr.ac.in', password: 'faculty123' },
    { role: 'Student', email: 'cse21a001@cvr.ac.in', password: 'student123' },
  ];

  const handleSubmit = async (values, { setSubmitting }) => {
    setLoading(true);
    try {
      const response = await loginApi({
        email: values.email,
        password: values.password,
      });
      
      login(response.user, {
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
      }, values.rememberMe);

      toast.success(`Welcome back!`);

      // Redirect based on role with replace to prevent back navigation
      if (response.user.role === 'ROLE_ADMIN') {
        navigate('/admin', { replace: true });
      } else if (response.user.role === 'ROLE_FACULTY') {
        navigate('/faculty', { replace: true });
      } else {
        navigate('/student', { replace: true });
      }
    } catch (error) {
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          'Login failed. Please check your credentials.';
      toast.error(errorMessage);
      setLoading(false);
      setSubmitting(false);
    }
  };

  const fillDemoCredentials = (setFieldValue, email, password) => {
    setFieldValue('email', email);
    setFieldValue('password', password);
    toast.info('Demo credentials filled. Click Sign In to continue.');
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <div className="logo-container">
            <div className="logo-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M2 17L12 22L22 17" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M2 12L12 17L22 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </div>
            <h1>Student Activity Tracker</h1>
          </div>
          <p>Sign in to access your dashboard</p>
        </div>

        <Formik
          initialValues={{ email: '', password: '', rememberMe: false }}
          validationSchema={loginSchema}
          onSubmit={handleSubmit}
        >
          {({ isSubmitting, setFieldValue }) => (
            <Form className="login-form">
              <div className="form-group">
                <label htmlFor="email">Email Address</label>
                <div className="input-wrapper">
                  
                  <Field
                    type="email"
                    name="email"
                    id="email"
                    className="form-input"
                    placeholder="your-email@gmail.com"
                    autoComplete="email"
                  />
                </div>
                <ErrorMessage name="email" component="div" className="error-message" />
              </div>

              <div className="form-group">
                <label htmlFor="password">Password</label>
                <div className="input-wrapper">
                
                  <Field
                    type={showPassword ? 'text' : 'password'}
                    name="password"
                    id="password"
                    className="form-input"
                    placeholder="Enter your password"
                    autoComplete="current-password"
                  />
                  <button
                    type="button"
                    className="password-toggle"
                    onClick={() => setShowPassword(!showPassword)}
                    tabIndex="-1"
                  >
                    {showPassword ? (
                      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M17.94 17.94C16.2306 19.243 14.1491 19.9649 12 20C5 20 1 12 1 12C2.24389 9.68192 3.96914 7.65663 6.06 6.06M9.9 4.24C10.5883 4.0789 11.2931 3.99836 12 4C19 4 23 12 23 12C22.393 13.1356 21.6691 14.2048 20.84 15.19M14.12 14.12C13.8454 14.4147 13.5141 14.6512 13.1462 14.8151C12.7782 14.9791 12.3809 15.0673 11.9781 15.0744C11.5753 15.0815 11.1752 15.0074 10.8016 14.8565C10.4281 14.7056 10.0887 14.4811 9.80385 14.1962C9.51897 13.9113 9.29439 13.5719 9.14351 13.1984C8.99262 12.8248 8.91853 12.4247 8.92563 12.0219C8.93274 11.6191 9.02091 11.2218 9.18488 10.8538C9.34884 10.4859 9.58525 10.1546 9.88 9.88" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        <path d="M1 1L23 23" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                      </svg>
                    ) : (
                      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M1 12C1 12 5 4 12 4C19 4 23 12 23 12C23 12 19 20 12 20C5 20 1 12 1 12Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                      </svg>
                    )}
                  </button>
                </div>
                <ErrorMessage name="password" component="div" className="error-message" />
              </div>

              <div className="form-options">
                <label className="checkbox-label">
                  <Field type="checkbox" name="rememberMe" />
                  <span>Remember me</span>
                </label>
                <a href="#" className="forgot-password" onClick={(e) => { e.preventDefault(); toast.info('Password reset feature coming soon!'); }}>
                  Forgot password?
                </a>
              </div>

              <button
                type="submit"
                className="login-button"
                disabled={isSubmitting || loading}
              >
                {loading ? (
                  <>
                    <span className="spinner"></span>
                    Signing in...
                  </>
                ) : (
                  'Sign In'
                )}
              </button>

              <div className="divider">
                <span>OR</span>
              </div>

              <button
                type="button"
                className="demo-button"
                onClick={() => setShowDemoCredentials(!showDemoCredentials)}
              >
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                  <path d="M12 16V12M12 8H12.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                </svg>
                {showDemoCredentials ? 'Hide' : 'Show'} Demo Credentials
              </button>

              {showDemoCredentials && (
                <div className="demo-credentials">
                  <p className="demo-title">Click to use demo credentials:</p>
                  {demoCredentials.map((cred, index) => (
                    <button
                      key={index}
                      type="button"
                      className="demo-credential-item"
                      onClick={() => fillDemoCredentials(setFieldValue, cred.email, cred.password)}
                    >
                      <span className="demo-role">{cred.role}</span>
                      <span className="demo-email">{cred.email}</span>
                    </button>
                  ))}
                </div>
              )}
            </Form>
          )}
        </Formik>

        <div className="login-footer">
          <p>© 2024 Student Activity Tracker. All rights reserved.</p>
        </div>
      </div>
    </div>
  );
};

export default Login;
