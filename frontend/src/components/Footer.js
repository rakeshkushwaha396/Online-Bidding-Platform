import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Footer.css';

const Footer = () => {
  const navigate = useNavigate();

  return (
    <footer className="footer-container">
      <div className="footer-content">
        <div className="footer-left">
          <span className="footer-title">Online Bidding Platform</span>
          <span className="footer-tagline">Your trusted marketplace since 2025</span>
        </div>
        <div className="footer-right">
  <span onClick={() => navigate('/guidelines')} className="footer-link">Guidelines</span>
  <span className="footer-divider">|</span>
  <span onClick={() => navigate('/contact')} className="footer-link">Contact</span>
  <span className="footer-divider">|</span>
  <span onClick={() => navigate('/aboutus')} className="footer-link">About</span>
</div>
      </div>
      <div className="footer-copyright">
        <span>© 2025 Online Bidding Platform. All rights reserved.</span>
      </div>
    </footer>
  );
};

export default Footer;