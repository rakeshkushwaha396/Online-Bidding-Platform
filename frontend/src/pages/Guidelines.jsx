import React from 'react';
import '../styles/Guidelines.css';


const Guidelines = () => {
  return (
    <div className="guidelines-container">
      <h1 className="guidelines-title">Tips for Creating a Successful Auction</h1>
      <ul className="guideline-list">
        <li>Use clear, high-quality images</li>
        <li>Write detailed descriptions including conditions and any flaws</li>
        <li>Set a reasonable starting price to attract bidders</li>
        <li>Choose appropriate auction duration</li>
        <li>Select the most accurate category to help buyers find your item</li>
      </ul>

      <hr className="guideline-separator" />

      <h1 className="guidelines-title">Tips for Placing a Successful Bid</h1>
      <ul className="guideline-list">
        <li>Research the item’s value before bidding</li>
        <li>Monitor auctions from the start</li>
        <li>void emotional bidding — don’t get caught in a bidding war</li>
        <li>Place bids strategically shortly before the auction ends</li>
      </ul>
    </div>
  );
};

export default Guidelines;
