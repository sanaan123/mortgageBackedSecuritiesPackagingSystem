import React, { useState, useEffect } from "react";
import axios from "../api";

function SearchResults({ filters, setSearchResults }) {
    const [results, setResults] = useState([]);

    useEffect(() => {
        if (Object.keys(filters).length > 0) {
            axios.post("/api/search", filters).then((response) => {
                setResults(response.data);
                setSearchResults(response.data);
            }).catch((error) => {
                console.error("Error fetching search results:", error);
            });
        }
    }, [filters, setSearchResults]);

    return (
        <div>
            <h2>Search Results</h2>
            {results.length > 0 ? (
                <table>
                    <thead>
                    <tr>
                        <th>Loan Amount</th>
                        <th>Rate Spread</th>
                        <th>Lien Status</th>
                    </tr>
                    </thead>
                    <tbody>
                    {results.map((result, index) => (
                        <tr key={index}>
                            <td>{result.loan_amount}</td>
                            <td>{result.rate_spread}</td>
                            <td>{result.lien_status}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            ) : (
                <p>No results found</p>
            )}
        </div>
    );
}
export default SearchResults;
