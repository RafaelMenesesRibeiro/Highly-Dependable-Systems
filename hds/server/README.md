##### Documentation regarding the available HDSNotary-webServer endpoints
##### Summarized below:

* Get State of Good
* Express Intention to Sell
* Transfer Good

All endpoint parameters are specified as Data params.  
If one or both sections are not available, then the endpoint accepts no such params. 

This documentation also includes a generalized error session, most endpoints can throw all or part of these errors.  
Errors specific to an endpoint are listed on its corresponding section

--------------------------------

**Get State of Good**
----

* **URL**

    /stateofgood

* **Method:**

    `GET`
    
* **Data Params**
    ```
    {
        "goodID" : "a_good_id"
    }
     ```

* **Success Response:**
  
    **Code:** 200  
    
    **Content:**
    ```
	 {
			"payload":
				{
					"operation" : "getStateOfGood",
					"code" : 200,
					"message" : "OK",
					"ownerId" : "a_owner_id",
					"onSale" : "a_boolean"
				},
			"signature" : "a_signature"
	 }
    ```
 
* **Error Response:**

	The error responses this request can return are the ones defined in the General HDSNotary API Errors section.  	    
        		
--------------------------------

**Express Intention to Sell**
----

* **URL**

   /intentionToSell

* **Method:**

    `POST`
   
* **Data Params**
   ```
   {
        "signature": "a_signature",
        "payload": 
            {
	            "sellerID" : "a_seller_ID",
	            "goodID" : "a_good_ID"
            }
   }
    ```

* **Success Response:**
 
    **Code:** 200  
    
    **Content:**
    ```
    {  
		"payload":
		        {
		            "operation" : "markForSale",
		            "code" : 200,
		            "message" : "OK"
		        },
		"signature" : "a_signature"
	 }	    
    ```

* **Error Response:**

	**Code:** 403
	 
	 **Content:**
	 ```
	 {
	     "code" : 403,
	     "operation" : markForSale),
	     "message" : "You do not have permission to put this item on sale.",
	     "reason" : "The user <sellerID> does not own the good <goodID>."
	 }
	 ```

* **Error Response:**

	Every other error response this request can return are the ones defined in the General HDSNotary API Errors section.
       
--------------------------------

**Transfer Good**
----
  
* **URL**

    /transferGood

* **Method:**

    `POST`
   
* **Data Params**
   ```
   {
       "signature": "a_signature",
       "payload": 
           {
               "sellerID" : "a_seller_ID",
               "buyerID" : "a_buyer_ID",
               "goodID" : "a_good_ID"
           }
   }
    ```
    
* **Success Response:**
 
	**Code:** 200  
	
	**Content:**
	```
	{
	    "payload" : 
	        {
		        "operation" : "transferGood",
		        "code" : 200,
		        "message" : "OK"
		},
		"signature" : "a_signature"   
	}
	```
    
* **Error Response:**

    **Code:** 403
     
     **Content:**
     ```
     {
         "code" : 403,
         "operation" : markForSale),
         "message" : "The transaction is not valid.",
         "reason" : "NOT IMPLEMENTED YET."
     }
     ```

* **Error Response:**

	Every other error response this request can return are the ones defined in the General HDSNotary API Errors section.

 --------------------------------
 
 # **General HDSNotary API Errors**
 
**Code:** 400 
 
**Content:**
 ```
 {
     "code" : 400,
     "operation" : (OPERATION),
     "message" : "No service - server could not connect to databases, try again later.",
     "reason" : (the error message from the causing error)
 }
 ```
 
**Code:** 400 
     
**Content:**
 ```
 {
     "code" : 400,
     "operation" : (OPERATION),
     "message" : "The parameters sent are either NULL or empty.",
     "reason" : (specifies with parameter is either causing the error)
 }
 ```

**Code:** 401
         
**Content:**
 ```
 {
     "code" : 401,
     "operation" : (OPERATION),
     "message" : "The connection to the database was refused.",
     "reason" : (the error message from the causing error)
 }
 ```

**Code:** 500             

**Content:**
 ```
 {
     "code" : 500,
     "operation" : (OPERATION),
     "message" : "The database did not return a response for the query.",
     "reason" : (the error message from the causing error)
 }
 ```

**Code:** 500

**Content:**
 ```
 {
     "code" : 500,
     "operation" : (OPERATION),
     "message" : "Caught an SQL Exception.",
     "reason" : (the error message from the causing error)
 }
 ```

**Code:** 503
                 
**Content:**
 ```
 {
     "code" : 503,
     "operation" : (OPERATION),
     "message" : "The connection to the database was closed.",
     "reason" : (the error message from the causing error)
 }
 ```
 ----
 