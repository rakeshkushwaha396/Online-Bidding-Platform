import React, { useState } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import '../styles/forgot.css';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [otpVerified, setOtpVerified] = useState(false);
  const navigate = useNavigate();

  const sendOTP = async () => {
    try {
      const form = new URLSearchParams();
      form.append('email', email);

      await axios.post('http://localhost:8089/api/users/forgot-password', form, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      });

      toast.success('OTP sent to email');
      setOtpSent(true);
    } catch (err) {
      toast.error(err.response?.data || 'Failed to send OTP');
    }
  };

  const verifyOTP = async () => {
    try {
      const form = new URLSearchParams();
      form.append('email', email);
      form.append('otp', otp);

      await axios.post('http://localhost:8089/api/users/verify-forgot-otp', form, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      });

      toast.success('OTP verified successfully');
      setOtpVerified(true);
    } catch (err) {
      toast.error(err.response?.data || 'Invalid OTP');
    }
  };

  const resetPassword = async () => {
    try {
      const form = new URLSearchParams();
      form.append('email', email);
      form.append('newPassword', newPassword);

      await axios.post('http://localhost:8089/api/users/reset-password', form, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      });

      toast.success('Password reset successful');
      setTimeout(() => navigate('/'), 1500);
    } catch (err) {
      toast.error(err.response?.data || 'Error resetting password');
    }
  };

  return (
    <div className="auth-wrapper">
      <h1 className="auth-title">BiddingZone Password Recovery</h1>
      <div className="auth-box">
        <h2>Forgot Password</h2>

        <input
          type="email"
          placeholder="Enter your registered email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        {!otpSent && <button onClick={sendOTP}>Send OTP</button>}

        {otpSent && !otpVerified && (
          <>
            <input
              type="text"
              placeholder="Enter OTP"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              required
            />
            <button onClick={verifyOTP}>Verify OTP</button>
          </>
        )}

        {otpVerified && (
          <>
            <input
              type="password"
              placeholder="Enter New Password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />
            <button onClick={resetPassword}>Reset Password</button>
          </>
        )}
      </div>
    </div>
  );
};

export default ForgotPassword;
