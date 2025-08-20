import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
import '../styles/auth.css';

const Login = () => {
  const [credentials, setCredentials] = useState({
    username: '',
    password: ''
  });
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setCredentials(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const loginUser = async (e) => {
    e.preventDefault();
    setIsLoading(true);

    const { username, password } = credentials;
    const isAdmin = username.toLowerCase() === 'admin';
    const loginURL = isAdmin 
      ? 'http://localhost:8089/admin/login'
      : 'http://localhost:8089/api/users/login';

    try {
      let res;
      if (isAdmin) {
        const formData = new FormData();
        formData.append('username', username);
        formData.append('password', password);

        res = await axios.post(loginURL, formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
      } else {
        const formParams = new URLSearchParams();
        formParams.append('username', username);
        formParams.append('password', password);

        res = await axios.post(loginURL, formParams, {
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        });
      }

      const { message, token, userId, greeting, fullname } = res.data;

      // Store user data
      localStorage.setItem('token', token);
      localStorage.setItem('userId', userId);
      localStorage.setItem('username', username);
      if (isAdmin) localStorage.setItem('isAdmin', 'true');
      if (fullname) localStorage.setItem('fullname', fullname);
      if (greeting) localStorage.setItem('greeting', greeting);

      toast.success(message || (isAdmin ? 'Admin login successful' : 'Login successful'));
      navigate(isAdmin ? '/admin-dashboard' : '/user-dashboard');

    } catch (err) {
      console.error('Login error:', err);
      const errorMsg = err.response?.data?.message || 
                      (typeof err.response?.data === 'string' ? err.response.data : 'Login failed. Please try again.');
      toast.error(errorMsg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <div className="auth-icon">
            <span className="icon">👤</span>
          </div>
          <h2>Welcome to BiddingZone</h2>
          <p>Your premier destination for exclusive auctions</p>
          
          <div className="platform-highlights">
            <div className="highlight-item">
              <span>🏆</span>
              <p>Access multiple live auctions</p>
            </div>
            <div className="highlight-item">
              <span>💎</span>
              <p>Bid on rare collectibles</p>
            </div>
            <div className="highlight-item">
              <span>🔐</span>
              <p>Bid Smart. Trade Secure.</p>
            </div>
          </div>
        </div>

        <form onSubmit={loginUser} className="auth-form">
          <div className="input-group">
            <span className="input-icon"></span>
            <input
              type="text"
              name="username"
              placeholder="    Username"
              value={credentials.username}
              onChange={handleChange}
              required
            />
          </div>

          <div className="input-group">
            <span className="input-icon"></span>
            <input
              type="password"
              name="password"
              placeholder="Password"
              value={credentials.password}
              onChange={handleChange}
              required
            
            />
          </div>

          <button type="submit" disabled={isLoading}>
            {isLoading ? (
              <div className="spinner-dual-ring"></div>

            ) : (
              'Login →'
            )}
          </button>

          <div className="auth-links">
            <span onClick={() => navigate('/forgot-password')}>Forgot Password?</span>
             <span onClick={() => navigate('/forget-username')}>Forgot Username?</span>
            <span onClick={() => navigate('/signup')}>
              New to BiddingZone? <strong>Join Now</strong>
            </span>
          </div>

        
        </form>
      </div>
    </div>
  );
};

export default Login;