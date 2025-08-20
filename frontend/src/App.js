import React, { useEffect, useState } from 'react';
import { Routes, Route, Navigate, useLocation } from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import ForgotPassword from './pages/ForgotPassword';
import UserDashboard from './pages/UserDashboard';
import AdminDashboard from './pages/AdminDashboard';
import { ToastContainer } from 'react-toastify';
import CreateAuction from './pages/CreateProduct';
import LiveAuctions from './pages/LiveAuctions';
import MyAuction from './pages/MyAuction';
import MyBids from './pages/MyBids';
import './App.css';
import AboutUs1 from './pages/AboutUs1';
import ViewDetails from './pages/ViewDetails';
import VerifiedUsers from './pages/VerifiedUsers';
import FrontPage from './pages/FrontPage'; 
import ContactUsForm from './pages/ContactUsForm';
import Guidelines from './pages/Guidelines';
import AboutUs from './pages/aboutUs';
import DashboardLayout from './components/DashboardLayout';
import ContactUsForm1 from './pages/ContactUsForm1';
import ForgetUsername from './pages/ForgetUsername';
const App = () => {
  const [isAdmin, setIsAdmin] = useState(localStorage.getItem('isAdmin') === 'true');
  const [token, setToken] = useState(localStorage.getItem('token'));
  const location = useLocation();

  useEffect(() => {
    const handleStorage = () => {
      setIsAdmin(localStorage.getItem('isAdmin') === 'true');
      setToken(localStorage.getItem('token'));
    };

    handleStorage();
    window.addEventListener('storage', handleStorage);
    return () => window.removeEventListener('storage', handleStorage);
  }, [location]);

  return (
    <>
      <ToastContainer />
      <Routes>
        <Route
          path="/"
          element={
            token
              ? isAdmin
                ? <Navigate to="/admin-dashboard" />
                : <Navigate to="/user-dashboard" />
              : <FrontPage /> 
          }
        />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/user-dashboard" element={<UserDashboard />} />
        <Route path="/admin-dashboard" element={
          isAdmin ? <AdminDashboard /> : <Navigate to="/" />
        } />
        <Route path="/admin/verified-users" element={<VerifiedUsers />} />
        <Route path="/create-auction" element={<CreateAuction />} />
        <Route path="/live-auctions" element={<LiveAuctions />} />
        <Route path="/my-auction" element={<MyAuction />} />
        <Route path="/my-bids" element={<MyBids />} />
        
<Route path="/aboutus" element={<DashboardLayout><AboutUs /></DashboardLayout>} />
<Route path="/contact" element={<DashboardLayout><ContactUsForm /></DashboardLayout>} />
<Route path="/guidelines" element={<DashboardLayout><Guidelines /></DashboardLayout>} />
        <Route path="/about1" element={<AboutUs1 />} />
        <Route path="/contact1" element={<ContactUsForm1 />} />
    <Route path="/forget-username" element={<ForgetUsername />} />

        <Route path="/product/:id" element={<ViewDetails />} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </>
  );
};

export default App;
