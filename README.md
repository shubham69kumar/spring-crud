# spring-crud
Assessment 

# Root ("/")
> This is the root url where the user is given the option to write their email and password . If the email and password is correct then only they are sent to the next page .

# ("/processlogin")
> This is the second page where the user is given two options either to list all the users or to create a new user . The auth key is also obtained in the page . 

# ("/createuser")
> If the user is selected to update a record then they are sent to this page . In this page a user writes their data and in the backend a post request is sent along with the authkey to create a new user . If the request success then the user is sent to the page to list the users if not then they have to reenter the data .

# ("/listuser")
> In this a get request is sent to the api and all the users are obtained and displayed on the screen . In the action section user is given two options either to delete the record or to update the record in case the user clicks on the delete record then the corresponding record is deleted and if the user choose to update the record then a form appears where the user id is same but the use is given option to update its values . Once the update is done then the user is sent back to the main page where they can list and see their changes .
 

