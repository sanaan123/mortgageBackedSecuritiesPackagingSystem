0. Sanaan Akhter(sa1420)
   Dayyan Hamid(dh820)

1.
   For the frontend, there are noticeably known issues within the user interface of the website as well as some backend spring boot issues. To start with, the add filter option for "owner occupied", "min income-to-debt ratio", "max income-to-debt ration", and "tract_to_msamd_income" min and max don't function properly on the website. Owner occupied is the only that shows the values associated with our database in the filters mentioned. Users are not able to get accurate search results, let alone any search results, when filling out any of the filters mentioned. An error is mentioned in the backlogs of the spring boot saying invalid input syntax and column not found for the mentioned filter attributes. Add Mortgage is also not working unfortunately, and although no errors pop up in the console, the data that the user enters for each option in add mortgage doesn't correctly associate with the values those attributes consist of within the database, like msamd or applicant sex and ethnicity. Users can input any number freely which clearly presents issues, it should have been a dropdown list of options that correlate with the database. Other than that, for add filter, the filter choices for the dropdown input lists aren't changing to what the user selects even though the backend is retrieving the users choice then finding the results based on the filters chosen. Also, the frontend won't allow for multiple selections of the same filter when adding filters, so can't add multiple msamds. Search  

   For the backend, there wasnt any issues with the code, however the database we made in part 1 (our score 26/30). The way our preliminary and applications tables were formed, caused the tables to be unjoinable. I explained the querying extensively in the comments of the MortgageBackend.java and any issues with querying should stem from the database not our backend. Dayyan (me) focused primarily on the backend and left comments to explain what I did and how. The database queries, especially those involving multiple filters and joins, may be slow for large datasets.  
   
2. We collaborated with each other as well as online resources such as Microsoft copilot for the frontend. We consulted with Microsoft copilot ai to help build the frontend. Most of the frontend was done with the help of Microsoft copilot, to help construct the Rest API controllers that allow the frontend to communicate with the database, to help construct the site visually, as well as styling it. The chat logs for all AI help is provided in the ZIP. Refered to online tutorials for setting up JDBC as well as different methods of queirying our database through java (used this video and others from this creator https://www.youtube.com/watch?v=7v2OnUti2eM&ab_channel=Telusko)

3.
Implemented a command line interface with a main menu offering the following options:
   Search for Mortgages: Added and removed filters across various parameters like county, loan type, income-to-debt ratio, etc.
   Calculate Rate: Computed the weighted average rate and allowed users to accept or reject the securitization cost.
   Package Mortgages: Updated the database to reflect securitized loans for eligible mortgages.
   Add New Mortgage: Provided functionality to insert new mortgage entries into the database (extra credit).
   Filter Display: Displayed active filters in a readable format, dynamically updating as the user added or removed filters.
   Included functionality for all seven filters (extra credit).
   Ensured database operations followed serializable transactions (extra credit).

Part 3 we partially completed as not all the filters for add filters function, nor does add a mortgage function correctly. However, the searching for 5 filters works as well as calculate rate and the option to accept or decline the package.

4.
Challenges Faced
   Understanding Transaction Isolation: Configuring the PostgreSQL database for serializable transactions and ensuring proper rollback mechanisms was challenging but rewarding.
   Handling Missing Data: Ensuring correct assumptions for missing rate spreads required integrating additional logic based on lien status.
   Complex Query Filtering: Dynamic query building for combining multiple filters while maintaining efficiency.
   Time Management: Balancing debugging with meeting assignment requirements, particularly for the extra credit sections.
Time Spent was approximately 30 hours.

