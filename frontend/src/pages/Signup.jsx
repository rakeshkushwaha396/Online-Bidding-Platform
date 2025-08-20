import React, { useState } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import '../styles/signup.css';
import qs from 'qs';

const Signup = () => {
  const [formData, setFormData] = useState({
    username: '',
    fullname: '',
    email: '',
    password: '',
    mobile: '',
    aadhaarNo: '',
    panNo: '',
    is18Plus: false
  });

  const [files, setFiles] = useState({
    aadhaarPhoto: null,
    panPhoto: null
  });

  const [otpState, setOtpState] = useState({
    sent: false,
    code: '',
    isSubmitting: false
  });

  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleFileChange = (e) => {
    const { name, files: selectedFiles } = e.target;
    setFiles(prev => ({
      ...prev,
      [name]: selectedFiles[0]
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setOtpState(prev => ({ ...prev, isSubmitting: true }));
      
      if (!formData.is18Plus) {
        toast.error('You must be 18+ to register');
        return;
      }

      const data = new FormData();
      Object.entries(formData).forEach(([key, value]) => {
        data.append(key, value);
      });
      
      if (files.aadhaarPhoto) data.append('aadhaarPhoto', files.aadhaarPhoto);
      if (files.panPhoto) data.append('panPhoto', files.panPhoto);

      await axios.post('http://localhost:8089/api/users/register', data, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });

      toast.success('OTP sent to your email!');
      setOtpState(prev => ({ ...prev, sent: true }));
    } catch (error) {
      toast.error(error.response?.data?.message || 'Registration failed');
    } finally {
      setOtpState(prev => ({ ...prev, isSubmitting: false }));
    }
  };

  const verifyOtp = async () => {
  try {
    setOtpState(prev => ({ ...prev, isSubmitting: true }));

    const data = qs.stringify({
      email: formData.email,
      otp: otpState.code
    });

    await axios.post('http://localhost:8089/api/users/verify-otp', data, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    });

    toast.success('Registration successful!');
    navigate('/login');
  } catch (error) {
    toast.error(error.response?.data?.message || 'Verification failed');
  } finally {
    setOtpState(prev => ({ ...prev, isSubmitting: false }));
  }
};


  return (
    <div className="signup-container">
      <div className="signup-card">
        <header className="signup-header">
          <h1>Create Your Account</h1>
          <p>🎯 Join now and bid on exclusive items before anyone else!</p>
        </header>

        {!otpState.sent ? (
          <form onSubmit={handleSubmit} className="signup-form">
            <div className="form-grid">
              <div className="form-field">
                <label>Username*</label>
                <input
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  placeholder=""
                  required
                />
              </div>

              <div className="form-field">
                <label>Full Name*</label>
                <input
                  type="text"
                  name="fullname"
                  value={formData.fullname}
                  onChange={handleChange}
                  placeholder=""
                  required
                />
              </div>

              <div className="form-field">
                <label>Email*</label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder=""
                  required
                />
              </div>

              <div className="form-field">
                <label>Password*</label>
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="••••••••"
                  minLength="8"
                  required
                />
              </div>

              <div className="form-field">
                <label>Mobile*</label>
                <input
                  type="tel"
                  name="mobile"
                  value={formData.mobile}
                  onChange={handleChange}
                  placeholder=""
                  pattern="[0-9]{10}"
                  required
                />
              </div>

              <div className="form-field">
                <label>Aadhaar Number*</label>
                <input
                  type="text"
                  name="aadhaarNo"
                  value={formData.aadhaarNo}
                  onChange={handleChange}
                  placeholder=""
                  pattern="[0-9]{12}"
                  required
                />
              </div>

              <div className="form-field">
                <label>PAN Number*</label>
                <input
                  type="text"
                  name="panNo"
                  value={formData.panNo}
                  onChange={handleChange}
                  placeholder=""
                  pattern="[A-Z]{5}[0-9]{4}[A-Z]{1}"
                  required
                />
              </div>

              <div className="form-field file-upload">
                <label>Aadhaar Photo(jpg, jpeg, png)*</label>
                <div className="file-input">
                  <input
                    type="file"
                    name="aadhaarPhoto"
                    onChange={handleFileChange}
                    accept="image/*,.pdf"
                    required
                  />
                  <span>{files.aadhaarPhoto?.name || 'Select file'}</span>
                </div>
              </div>

              <div className="form-field file-upload">
                <label>PAN Photo(jpg, jpeg, png)*</label>
                <div className="file-input">
                  <input
                    type="file"
                    name="panPhoto"
                    onChange={handleFileChange}
                    accept="image/*,.pdf"
                    required
                  />
                  <span>{files.panPhoto?.name || 'Select file'}</span>
                </div>
              </div>
            </div>

            <div className="terms-agreement">
              <label className="checkbox">
                <input
                  type="checkbox"
                  name="is18Plus"
                  checked={formData.is18Plus}
                  onChange={handleChange}
                  required
                />
                <span>I confirm I am 18 years or older*</span>
              </label>
            </div>

            <button type="submit" disabled={otpState.isSubmitting}>
              {otpState.isSubmitting ? 'Processing...' : 'Register & Send OTP'}
            </button>
          </form>
        ) : (
          <div className="otp-verification">
            <div className="form-field">
              <label>Verification Code*</label>
              <input
                type="text"
                value={otpState.code}
                onChange={(e) => setOtpState(prev => ({ ...prev, code: e.target.value }))}
                placeholder="Enter 4-digit OTP"
                maxLength="4"
                required
              />
              <p className="otp-instruction">
                We've sent a verification code to {formData.email}
              </p>
            </div>

            <button 
              onClick={verifyOtp}
              disabled={otpState.isSubmitting || otpState.code.length !== 4}
            >
              {otpState.isSubmitting ? 'Verifying...' : 'Verify & Complete Registration'}
            </button>
          </div>
        )}

        <div className="signup-footer">
          <p>
            Already have an account? <a href="/login">Sign in</a>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Signup;