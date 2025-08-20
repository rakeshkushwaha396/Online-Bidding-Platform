import React, { useState } from 'react';
import axios from 'axios';
import '../styles/ContactUsForm.css';

const ContactUsForm = () => {
  const [formData, setFormData] = useState({
    username: '',
    mobileNumber: '',
    email: '',
    description: '',
  });

  const [successMsg, setSuccessMsg] = useState('');
  const [errors, setErrors] = useState({});
  const [mobileError, setMobileError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;

    if (name === "mobileNumber") {
      if (!/^\d*$/.test(value)) {
        setMobileError("Mobile number must contain digits only.");
      } else if (value.length !== 10 && value.length > 0) {
        setMobileError("Mobile number must be exactly 10 digits.");
      } else {
        setMobileError("");
      }
    }

    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSuccessMsg('');
    setErrors({});

    if (mobileError || formData.mobileNumber.length !== 10) {
      setMobileError("Please enter a valid 10-digit mobile number.");
      return;
    }

    try {
      await axios.post('https://localhost:7218/api/aboutus', formData);
      setSuccessMsg("Message sent successfully to Admin!");
      setFormData({
        username: '',
        mobileNumber: '',
        email: '',
        description: '',
      });
    } catch (error) {
      if (error.response && error.response.status === 400) {
        const apiErrors = error.response.data.errors;
        setErrors(apiErrors || {});
      } else {
        alert('An error occurred while sending the message.');
        console.error('Submission error:', error.response?.data || error.message);
      }
    }
  };

  return (
    <div className="form-page-new">
      <div className="form-card">
        <h2> Contact Us</h2>
        <hr /><br></br>
        <form onSubmit={handleSubmit}>
          
          <div className="input-group">
            <input type="text" name="username" value={formData.username} onChange={handleChange} required />
            <label>Name</label>
            {errors.Username && <p className="error">{errors.Username[0]}</p>}
          </div>

        
          <div className="input-group">
            <input type="text" name="mobileNumber" value={formData.mobileNumber} onChange={handleChange} required maxLength={10} />
            <label>Mobile Number</label>
            {mobileError && <p className="error">{mobileError}</p>}
            {errors.MobileNumber && <p className="error">{errors.MobileNumber[0]}</p>}
          </div>

        
          <div className="input-group">
            <input type="email" name="email" value={formData.email} onChange={handleChange} required />
            <label>Email</label>
            {errors.Email && <p className="error">{errors.Email[0]}</p>}
          </div>

          
          <div className="input-group">
            <textarea name="description" rows="3" value={formData.description} onChange={handleChange} required></textarea>
            <label>Description</label>
            {errors.Description && <p className="error">{errors.Description[0]}</p>}
          </div>

          <button type="submit" className="submit-btn"> Send Message</button>
          {successMsg && <p className="success">{successMsg}</p>}
        </form>
      </div>
    </div>
  );
};

export default ContactUsForm;
