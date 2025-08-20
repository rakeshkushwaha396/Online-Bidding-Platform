import React from 'react';
import '../styles/AboutUs.css';

const teamMembers = [
  { 
    name: 'Ashmit Chaudhary', 
    photo: '/images/as.jpg' 
  },
  { 
    name: 'Chaitanya Avhad', 
    photo: '/images/c.jpg' 
  },
  { 
    name: 'Ayush Deshmukh', 
    photo: '/images/a.jpg' 
  },
  { 
    name: 'Rakesh Kushwaha', 
    photo: '/images/r.jpg' 
  },
];

const AboutUs = () => {
  return (
    <div className="about-container">
      <h1 className="about-title">Our Team</h1>
      <hr className="team-divider" />
      
      <div className="team-grid">
        {teamMembers.map((member, index) => (
          <div className="team-member" key={index}>
            <img 
              src={member.photo} 
              alt={member.name} 
              className="member-photo"
            />
            <div className="member-name">{member.name}</div>
            <div className="member-role">{member.role}</div>
          </div>
        ))}
      </div>

      <div className="project-description">
        <h2 className="description-title">Innovating Auctions with Secure, Real-Time Technology</h2>
        <p className="description-text">
          We built BiddingZone as a full-stack online bidding platform to revolutionize how users buy and sell goods through transparent, regulated auctions.
        </p>
        
        <div className="features-section">
          <h3 className="features-title">What We Do Differently:</h3>
          <ul className="features-list">
            <li><strong>Developer-Crafted Security</strong>
              <ul>
                <li>Implemented JWT authentication with OTP verification and mandatory Aadhaar/PAN checks</li>
                <li>Designed admin-approval systems to validate users before bidding</li>
                <li>Enforced rate-limiting and input validation to prevent fraud</li>
                <p></p>
              </ul>
            </li>
            
            <li><strong>Real-Time Auction Experience</strong>
              <ul>
                <li>Engineered WebSocket-based live bidding with higher-bid enforcement</li>
                <li>Developed automated email/SMTP notifications for bids, wins, and alerts</li>
                <li>Created dual-role flexibility (buyer/seller) with unified dashboards</li>
                <p></p>
              </ul>
            </li>
            
            <li><strong>Regulatory Compliance</strong>
              <ul>
                <li>Age-gated (18+) registration with document uploads</li>
                <li>Admin-monitored transactions for fair play</li>
              </ul>
            </li>
          </ul>
        </div>
        
        <div className="tech-stack">
          <h3 className="tech-title">Our Tech Stack</h3>
          <p>Frontend: React.js (responsive UI with form validations)</p>
          <p>Backend: Spring Boot (auction logic) + .NET (contact/feedback)</p>
          <p>Database: MySQL (ACID-compliant bid tracking)</p>
        </div>
        
        <div className="mission-section">
          <h3 className="mission-title">Why It Matters</h3>
          <p>As developers, we prioritized:</p>
          <ul className="mission-list">
            <li>✓ Transparency: Real-time bid visibility</li>
            <li>✓ Security: Document-verified users + admin controls</li>
            <li>✓ Performance: Optimized with Spring Data JPA and WebSocket</li>
            <p></p>
          </ul>
          <p className="contact-note">For enquiries, get in touch with our support team.</p>
        </div>
      </div>
    </div>
  );
};

export default AboutUs;