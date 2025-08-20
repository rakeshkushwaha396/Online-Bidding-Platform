import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import '../styles/forgot.css';

const ForgetUsername = () => {
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [otpVerified, setOtpVerified] = useState(false);
  const navigate = useNavigate();

  const sendOtp = async () => {
    try {
      const form = new FormData();
      form.append('email', email);

      const res = await axios.post('http://localhost:8089/api/users/forgot-username', form, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      toast.success(res.data.message || 'OTP sent to your email.');
      setOtpSent(true);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to send OTP');
    }
  };

  const verifyOtp = async () => {
    try {
      const form = new FormData();
      form.append('email', email);
      form.append('otp', otp);

      const res = await axios.post('http://localhost:8089/api/users/verify-forgot-username-otp', form, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      toast.success(res.data.message || 'Username has been sent to your email.');
      setOtpVerified(true);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid or expired OTP');
    }
  };

  
  useEffect(() => {
    if (otpVerified) {
      const timer = setTimeout(() => {
        navigate('/login');
      }, 2000); 
      return () => clearTimeout(timer);
    }
  }, [otpVerified, navigate]);

  return (
    <div className="auth-wrapper">
      <h1 className="auth-title">BiddingZone Username Recovery</h1>
      <div className="auth-box">
        <h2>Forgot Username</h2>

        <input
          type="email"
          placeholder="Enter your registered email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        {!otpSent && <button onClick={sendOtp}>Send OTP</button>}

        {otpSent && !otpVerified && (
          <>
            <input
              type="text"
              placeholder="Enter OTP"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              required
            />
            <button onClick={verifyOtp}>Verify OTP</button>
          </>
        )}

        {otpVerified && (
          <p className="success-msg">
            ✅ Your username has been sent to your registered email. Redirecting to login...
          </p>
        )}
      </div>
    </div>
  );
};

export default ForgetUsername;
