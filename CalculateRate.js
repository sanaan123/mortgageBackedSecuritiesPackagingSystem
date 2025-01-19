import React, { useState } from "react";
import axios from "../api";

function CalculateRate({ filters, setRate }) {
    const [result, setResult] = useState(null);

    const calculateRate = () => {
        axios.post("/api/calculateRate", filters).then((response) => {
            setResult(response.data);
            setRate(response.data.rate);
        });
    };

    return (
        <div>
            <h2>Calculate Rate</h2>
            <button onClick={calculateRate}>Calculate Rate</button>
            {result && (
                <div>
                    <p>Weighted Rate: {result.weightedRate}</p>
                    <p>Total Loan Amount: ${result.totalLoanAmount}</p>
                </div>
            )}
        </div>
    );
}
export default CalculateRate;
