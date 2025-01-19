import React, { useState } from "react";
import axios from "../api";

function AddMortgage() {
    const [mortgage, setMortgage] = useState({
        applicant_income_000s: '',
        loan_amount_000s: '',
        msamd: '',
        applicant_sex: '',
        loan_type: '',
        applicant_ethnicity: '',
        locationId: ''
    });

    const handleChange = (event) => {
        const { name, value } = event.target;
        setMortgage(prevMortgage => ({ ...prevMortgage, [name]: value }));
    };

    const handleSubmit = (event) => {
        event.preventDefault();
        // Ensure fields are not empty and have valid numbers
        const { applicant_income_000s, loan_amount_000s, msamd, applicant_sex, loan_type, applicant_ethnicity, locationId } = mortgage;
        if (applicant_income_000s && loan_amount_000s && msamd && applicant_sex && loan_type && applicant_ethnicity && locationId) {
            const mortgageData = {
                applicant_income_000s: parseInt(applicant_income_000s),
                loan_amount_000s: parseInt(loan_amount_000s),
                msamd: parseInt(msamd),
                applicant_sex: parseInt(applicant_sex),
                loan_type: parseInt(loan_type),
                applicant_ethnicity: parseInt(applicant_ethnicity),
                locationId: parseInt(locationId)
            };
            axios.post("/api/mortgages", mortgageData)
                .then(response => {
                    alert("Mortgage added successfully!");
                    setMortgage({
                        applicant_income_000s: '',
                        loan_amount_000s: '',
                        msamd: '',
                        applicant_sex: '',
                        loan_type: '',
                        applicant_ethnicity: '',
                        locationId: ''
                    });
                })
                .catch(error => {
                    console.error("There was an error adding the mortgage!", error);
                });
        } else {
            alert("Please fill in all fields with valid data.");
        }
    };

    return (
        <div>
            <h2>Add Mortgage</h2>
            <form onSubmit={handleSubmit} className="add-mortgage-form">
                <div>
                    <label>Applicant Income (000s)</label>
                    <input
                        type="number"
                        name="applicant_income_000s"
                        value={mortgage.applicant_income_000s}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>Loan Amount (000s)</label>
                    <input
                        type="number"
                        name="loan_amount_000s"
                        value={mortgage.loan_amount_000s}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>MSAMD</label>
                    <input
                        type="number"
                        name="msamd"
                        value={mortgage.msamd}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>Applicant Sex</label>
                    <input
                        type="number"
                        name="applicant_sex"
                        value={mortgage.applicant_sex}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>Loan Type</label>
                    <input
                        type="number"
                        name="loan_type"
                        value={mortgage.loan_type}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>Applicant Ethnicity</label>
                    <input
                        type="number"
                        name="applicant_ethnicity"
                        value={mortgage.applicant_ethnicity}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>Location ID</label>
                    <input
                        type="number"
                        name="locationId"
                        value={mortgage.locationId}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className="center-button">
                    <button type="submit">Add Mortgage</button>
                </div>
            </form>
        </div>
    );
}

export default AddMortgage;
