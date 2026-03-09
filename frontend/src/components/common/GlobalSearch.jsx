import React, { useState } from 'react';
import './GlobalSearch.css';

const GlobalSearch = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [isOpen, setIsOpen] = useState(false);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      // Navigate to search results page or show results
      console.log('Searching for:', searchTerm);
      setIsOpen(false);
    }
  };

  return (
    <div className="global-search">
      <form onSubmit={handleSearch} className="search-form">
        <input
          type="text"
          placeholder="Search students, faculty, courses..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          onFocus={() => setIsOpen(true)}
          className="search-input"
        />
        <button type="submit" className="search-button">
          <i className="fas fa-search"></i>
        </button>
      </form>
    </div>
  );
};

export default GlobalSearch;
