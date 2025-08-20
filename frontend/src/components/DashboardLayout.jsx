import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/DashboardLayout.css';
import Footer from './Footer';

const DashboardLayout = ({ children }) => {
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [profileVisible, setProfileVisible] = useState(false);
  const [profile, setProfile] = useState(null);

  const sidebarRef = useRef(null);
  const profileRef = useRef(null);

  const storedUsername = localStorage.getItem('username') || 'User';
  const token = localStorage.getItem('token');

  const handleLogout = () => {
    localStorage.clear();
    window.location.href = '/';
  };

  const toggleProfileSidebar = async () => {
    if (!profileVisible) {
      try {
        const res = await axios.get('http://localhost:8089/api/users/profile', {
          headers: { Authorization: `Bearer ${token}` },
        });
        setProfile(res.data);
      } catch (err) {
        console.error('Failed to fetch profile', err);
      }
    }
    setProfileVisible(!profileVisible);
  };

  const handleDeleteAccount = async () => {
    const confirmDelete = window.confirm('Do you really want to delete your account?');
    if (!confirmDelete) return;

    try {
      await axios.delete('http://localhost:8089/api/users/delete-account', {
        headers: { Authorization: `Bearer ${token}` },
      });
      alert('Account deleted successfully.');
      localStorage.clear();
      navigate('/');
    } catch (err) {
      console.error('Failed to delete account:', err);
      alert('Error deleting account.');
    }
  };

  
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        profileVisible &&
        profileRef.current &&
        !profileRef.current.contains(event.target) &&
        !event.target.closest('.user-profile')
      ) {
        setProfileVisible(false);
      }
      if (
        sidebarOpen &&
        sidebarRef.current &&
        !sidebarRef.current.contains(event.target) &&
        !event.target.closest('.menu-icon')
      ) {
        setSidebarOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [profileVisible, sidebarOpen]);

  return (
    <div className={`dashboard-container ${sidebarOpen ? 'sidebar-open' : ''}`}>
     
      <nav className="navbar">
        <div className="navbar-left">
          <div className="menu-icon" onClick={() => setSidebarOpen(prev => !prev)}>
            <span className="menu-bar"></span>
            <span className="menu-bar"></span>
            <span className="menu-bar"></span>
          </div>
          <div className="logo" onClick={() => navigate('/user-dashboard')}>
            <span className="logo-icon">🏆</span>
            <span className="logo-text">BiddingZone</span>
          </div>
        </div>

        <div className="navbar-right">
          <div className="user-profile" onClick={toggleProfileSidebar}>
            <span className="user-icon">👤</span>
            <span className="username">{storedUsername}</span>
          </div>
          <button className="logout-button" onClick={handleLogout}>
            <span className="logout-icon">🚪</span>
            <span className="logout-text">Logout</span>
          </button>
        </div>
      </nav>

      
      <div
        className={`sidebar ${sidebarOpen ? 'open' : 'closed'}`}
        ref={sidebarRef}
      >
        <div className="sidebar-header">
          <h3>Menu</h3>
        </div>
        <ul className="sidebar-menu">
          <li onClick={() => navigate('/user-dashboard')}>
            <span className="menu-icon">📊</span>
            <span>Dashboard</span>
          </li>
          <li onClick={() => navigate('/live-auctions')}>
            <span className="menu-icon">🔥</span>
            <span>Live Auctions</span>
          </li>
          <li onClick={() => navigate('/create-auction')}>
            <span className="menu-icon">➕</span>
            <span>Create Auction</span>
          </li>
          <li onClick={() => navigate('/my-bids')}>
            <span className="menu-icon">💲</span>
            <span>My Bids</span>
          </li>
          <li onClick={() => navigate('/my-auction')}>
            <span className="menu-icon">🏷️</span>
            <span>My Auctions</span>
          </li>
          <li onClick={() => navigate('/contact')}>
            <span className="menu-icon">📞</span>
            <span>Contact Us</span>
          </li>
          <li onClick={() => navigate('/guidelines')}>
            <span className="menu-icon">📘</span>
            <span>Guidelines</span>
          </li>
          <li onClick={() => navigate('/aboutus')}>
            <span className="menu-icon">ℹ️</span>
            <span>About Us</span>
          </li>
        </ul>
        <div className="sidebar-footer">
          <p>© 2025 BiddingZone</p>
        </div>
      </div>

      
      {profileVisible && (
        <div className="profile-sidebar" ref={profileRef}>
          <h3>User Profile</h3>
          {profile ? (
            <>
              <p><strong>Full Name:</strong> {profile.fullname}</p>
              <p><strong>Username:</strong> {profile.username}</p>
              <p><strong>Email:</strong> {profile.email}</p>
              <p><strong>Mobile:</strong> {profile.mobile}</p>
              <button className="delete-account-btn" onClick={handleDeleteAccount}>
                🗑️ Delete Account
              </button>
            </>
          ) : (
            <p>Loading...</p>
          )}
        </div>
      )}

      
      <div className="main-content">{children}</div>

      <Footer />
    </div>
  );
};

export default DashboardLayout;










