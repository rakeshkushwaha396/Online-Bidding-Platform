import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/FrontPage.css';

const FrontPage = () => {
  const navigate = useNavigate();

  const auctionItems = [
    {
      id: 1,
      title: "Vintage Film Camera",
      image: "/images/1.jpg",
      currentBid: "₹2,450",
      bids: 12,
      timeLeft: "2h 45m",
      seller: "CameraCollector"
    },
    {
      id: 2,
      title: "Luxury Swiss Watch - Gold Plated",
      image: "/images/2.jpg",
      currentBid: "₹20,450",
      bids: 28,
      timeLeft: "1h 15m",
      seller: "TimeMaster"
    },
    {
      id: 3,
      title: "Original Oil Painting - Abstract Art",
      image: "/images/3.jpg",
      currentBid: "₹2,000",
      bids: 7,
      timeLeft: "4h 30m",
      seller: "ArtGallery"
    }
  ];

  return (
    <div className="front-page-container">
      
      <div className="navbar">
        <h1 className="app-title">🏆 BiddingZone</h1>
        <ul className="nav-links">
          <li onClick={() => navigate('/')}>
            <span className="menu-icon"></span>
            <span>Home</span>
          </li>
          <li onClick={() => navigate('/about1')}>
            <span className="menu-icon"></span>
            <span>About Us</span>
          </li>
          <li onClick={() => navigate('/contact1')}>
            <span className="menu-icon"></span>
            <span>Contact Us</span>
          </li>
        </ul>
      </div>

      <div className="front-page">
        <div className="header">
          <div className="branding">
            <h1>The Future of <br /><span>Online Auction</span></h1>
            <p className="subtitle">
              Discover unique items, place competitive bids, and sell your treasures to a global audience.
              Join thousands of buyers and sellers in our trusted marketplace.
            </p>
          </div>
          <div className="auth-buttons">
            <button onClick={() => navigate('/signup')} className="auth-btn">Signup</button>
            <button onClick={() => navigate('/login')} className="auth-btn">Login</button>
          </div>
        </div>

        <div className="auction-section">
          <h2>Live Auctions</h2>
          <div className="auction-grid">
            {auctionItems.map((item) => (
              <div key={item.id} className="auction-card">
                <div className="image-wrapper">
                  <img src={item.image} alt={item.title} className="auction-image" />
                  <div className="time-left">{item.timeLeft} left</div>
                </div>
                <div className="auction-details">
                  <h3>{item.title}</h3>
                  <div className="seller-info">Seller: <strong>{item.seller}</strong></div>
                  <div className="bid-info">
                    <div><span className="bid-label">Current Bid:</span> <span className="bid-amount">{item.currentBid}</span></div>
                    <div className="bid-count">{item.bids} Bids</div>
                  </div>
                  <button className="bid-btn">Place Bid</button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default FrontPage;
