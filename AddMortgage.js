// src/components/AddMortgage.js
import React, { useState } from 'react';
import api from '../services/api';

function AddMortgage() {
    const [mortgage, setMortgage] = useState({
        income: '',
        loanAmount: '',
        msamd: '',
        sex: '',
        loanType: '',
        ethnicity: ''
    });

    const handleAdd = () => {
        api.post('/api/mortgages/add', mortgage)
            .then(response => console.log('Mortgage added:', response.data))
            .catch(error => console.error('Error:', error));
    };

    const handleChange = (e) => {
        setMortgage({ ...mortgage, [e.target.name]: e.target.value });
    };

    return (
        <div>
            <h2>Add Mortgage</h2>
            <form>
                <div>
                    <label>Income:</label>
                    <input
                        type="number"
                        name="income"
                        value={mortgage.income}
                        onChange={handleChange}
                    />
                </div>
                <div>
                    <label>Loan Amount:</label>
                    <input
                        type="number"
                        name="loanAmount"
                        value={mortgage.loanAmount}
                        onChange={handleChange}
                    />
                </div>
                <div>
                    <label>MSAMD:</label>
                    <input
                        type="text"
                        name="msamd"
                        value={mortgage.msamd}
                        onChange={handleChange}
                    />
                </div>
                <div>
                    <label>Sex:</label>
                    <input
                        type="text"
                        name="sex"
                        value={mortgage.sex}
                        onChange={handleChange}
                    />
                </div>
                <div>
                    <label>Loan Type:</label>
                    <input
                        type="text"
                        name="loanType"
                        value={mortgage.loanType}
                        onChange={handleChange}
                    />
                </div>
                <div>
                    <label>Ethnicity:</label>
                    <input
                        type="text"
                        name="ethnicity"
                        value={mortgage.ethnicity}
                        onChange={handleChange}
                    />
                </div>
                <button type="button" onClick={handleAdd}>Add Mortgage</button>
            </form>
        </div>
    );
}

export default AddMortgage;
