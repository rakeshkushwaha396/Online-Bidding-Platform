import React, { useEffect, useState } from 'react';
import axios from 'axios';
import '../styles/MyBids.css';
import { toast } from 'react-toastify';
import DashboardLayout from '../components/DashboardLayout';
import { connectUserWebSocket, disconnectWebSocket } from '../utils/websocket';

const MyBids = () => {
  const [bids, setBids] = useState([]);
  const [username, setUsername] = useState('');

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      toast.error("User not logged in");
      return;
    }

    // ✅ Get bid history
    axios.get("http://localhost:8089/bids/bid-history", {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => {
        setBids(res.data || []);
        const wonAny = res.data.some((bid) => bid.status === "WON");
        if (wonAny) toast.success("🎉 Congratulations! You've won one or more auctions.");

        // ✅ Decode username from JWT (or store separately)
        const base64Payload = token.split('.')[1];
        const payload = JSON.parse(atob(base64Payload));
        const uname = payload.sub || payload.username || payload.user_name;
        setUsername(uname);

        // ✅ Connect to WebSocket after username is known
        connectUserWebSocket(uname, handleWebSocketUpdate);
      })
      .catch((err) => {
        toast.error("Failed to load your bids");
        console.error(err);
      });

    return () => {
      disconnectWebSocket();
    };
  }, []);

  // ✅ Update bid status when WebSocket message received
  const handleWebSocketUpdate = (data) => {
    setBids(prev =>
      prev.map(bid =>
        bid.productId === data.productId
          ? {
              ...bid,
              status: data.status,
              finalPrice: data.winningPrice,
              currentBidPrice: data.winningPrice,
            }
          : bid
      )
    );
  };

  return (
    <DashboardLayout>
      {bids.length === 0 ? (
        <div className="no-bids-message">
          <h3>You haven't placed any bids yet!</h3>
        </div>
      ) : (
        <div className="bids-container">
          <h2 className="bids-title">My Bids</h2>
          {bids.map((bid, idx) => (
            <div key={idx} className="bid-card">
              <img
                src={`http://localhost:8089${bid.imageUrl}`}
                alt={bid.productName}
                className="bid-image"
                onError={(e) => {
                  e.target.src = 'https://via.placeholder.com/200x150?text=No+Image';
                }}
              />
              <div className="bid-details">
                <div className="row">
                  <span><strong>Product ID:</strong> {bid.productId}</span>
                  <span><strong>Product Name:</strong> {bid.productName}</span>
                  <span><strong>Category:</strong> {bid.category}</span>
                </div>
                <div className="row">
                  <span><strong>Your Bid:</strong> ₹{bid.yourBidPrice}</span>
                  <span>
                    <strong>Final Price:</strong>{" "}
                    <span style={{ color: bid.status === "WON" ? "#065f46" : "#1f2937" }}>
                      ₹{bid.finalPrice}
                    </span>
                  </span>
                  <span><strong>Current Bid:</strong> ₹{bid.currentBidPrice}</span>
                </div>
                <div className="row">
                  <span>
                    <strong>Status:</strong>{" "}
                    <span className={`status-${bid.status.toLowerCase()}`}>{bid.status}</span>
                  </span>
                  <span><strong>Bid Status:</strong> {bid.bidStatus}</span>
                  <span><strong>Time Left:</strong> {bid.remainingTime}</span>
                </div>
                <div className="row">
                  <span><strong>Seller Name:</strong> {bid.sellerName}</span>
                  <span><strong>Seller Mobile:</strong> {bid.sellerPhone}</span>
                </div>
                {bid.status === "WON" && (
                  <div className="win-banner">🎉 You won this auction!</div>
                )}
                {bid.status === "LOST" && (
                  <div className="lose-banner">❌ You lost this auction.</div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </DashboardLayout>
  );
};

export default MyBids;
