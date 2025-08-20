import React, { useEffect, useState } from 'react';
import axios from 'axios';
import '../styles/MyAuction.css';
import DashboardLayout from '../components/DashboardLayout'; 

const MyAuction = () => {
  const [auctions, setAuctions] = useState([]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) return;

    const fetchData = async () => {
      try {
        const res = await axios.get(`http://localhost:8089/bids/seller-auction-history`, {
          headers: {
            Authorization: `Bearer ${token}`, 
          },
        });
        setAuctions(Array.isArray(res.data) ? res.data : []);
      } catch (error) {
        console.error('Error fetching seller auction data:', error);
      }
    };

    fetchData();
  }, []);

  return (
    <DashboardLayout> 
      <div className="bids-container">
        <h2 className="bids-title">My Auctions</h2>
        {auctions.length === 0 ? (
          <p className="no-bids-message">No auction history available.</p>
        ) : (
          auctions.map((auction) => (
            <div className="bid-card" key={auction.productId}>
              <img
                src={`http://localhost:8089${auction.imageUrl}`}
                alt={auction.productName}
                className="bid-image"
                onError={(e) => {
                  e.target.src = 'https://via.placeholder.com/200x150?text=No+Image';
                }}
              />

              <div className="bid-details">
                <div className="row">
                  <span><strong>Product ID:</strong> {auction.productId}</span>
                  <span><strong>Product Name:</strong> {auction.productName}</span>
                  <span><strong>Category:</strong> {auction.category}</span>
                </div>

                <div className="row">
                  <span><strong>Base Price:</strong> ₹{auction.basePrice}</span>
                  <span><strong>Current Bid:</strong> ₹{auction.currentBidPrice}</span>
                  <span><strong>Final Bid:</strong> ₹{auction.finalPrice}</span>
                </div>

                <div className="row">
                  <span><strong>Bids Count:</strong> {auction.totalBids}</span>
                  <span>
                    <strong>Bid Status:</strong>{' '}
                    <span className={`bid-status ${auction.bidStatus.toLowerCase()}`}>
                      {auction.bidStatus}
                    </span>
                  </span>
                  <span><strong>Time Left:</strong> {auction.remainingTime}</span>
                </div>

                <div className="row">
                  <span><strong>Buyer Name:</strong> {auction.buyerName}</span>
                  <span><strong>Buyer Number:</strong> {auction.buyerMobile}</span>
                  <span><strong></strong></span>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </DashboardLayout>
  );
};

export default MyAuction;
