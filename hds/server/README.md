##### Documentation regarding the available HDSNotary-webServer endpoints
##### Summarized below:

* Get State of Good
* Express Intention to Sell
* Transfer Good

This documentation also includes a generalized error session, most endpoints can throw all or part of these errors.  
Errors specific to an endpoint are listed on its corresponding section

--------------------------------

**Get State of Good**
----

* **URL**

    /stateOfGood

* **Method:**

    `GET`
    
* **URL Params**
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
        "timestamp" : "a_long",
        "requestID" : "0",
        "operation" : "getStateOfGood",
        "from" : "server",
        "to" : "a_client_ID",
        "signature" : "a_signature",
        "ownerID" : "a_client_ID",
        "onSale" : "a_boolean"
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
	   "timestamp" : "a_long",
	   "requestID" : "a_request_ID",
	   "operation" : "markForSale",
	   "from" : "a_client_ID",
	   "to" : "server",
	   "signature" : "a_signature",
	   "goodID" : "a_good_ID",
	   "owner" : "a_client_ID"
   }
   ```

* **Success Response:**
 
    **Code:** 200  
    
    **Content:**
    ```
    {
		"timestamp" : "a_long",
		"requestID" : "a_request_ID",
		"operation" : "markForSale",
		"from" : "server",
		"to" : "a_client_ID",
		"signature" : "a_signature"
    }
    ```
    
* **Error Response:**

	**Code:** 401
     
    **Content:**
     ```
    {
		"timestamp" : "a_long",
		"requestID" : "a_request_ID",
		"operation" : "transferGood",
		"from" : "server",
		"to" : "a_client_ID",
		"signature" : "a_signature",
		"message" : "The signatures do not match the received data.",
		"reason" : "The Seller's signature is not valid.""The Seller's signature is not valid."
    }
    ```

* **Error Response:**

    **Code:** 403
     
    **Content:**
     ```
    {
		"timestamp" : "a_long",
		"requestID" : "a_request_ID",
		"operation" : "transferGood",
		"from" : "server",
		"to" : "a_client_ID",
		"signature" : "a_signature",
		"message" : "You do not have permission to put this item on sale.",
		"reason" : "The user '" + sellerID + "' does not own the good '" + goodID + "'."
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
		"timestamp" : "a_long",
		"requestID" : "a_request_ID",
		"operation" : "buyGood",
		"from" : "a_buyer_ID",
		"to" : "a_seller_ID",
		"signature" : "a_buyer_signature",
		"goodID" : "a_good_ID",
		"buyerID" : "a_client_ID",
		"sellerID" : "a_client_ID",
		"wrappingTimestamp" : "a_long",
		"wrappingOperation" : "transferGood",
		"wrappingFrom" : "a_seller_ID",
		"wrappingTo" : "server",
		"wrappingSignature" : "a_seller_signature"
	}
  ```
    
* **Success Response:**
 
	**Code:** 200  
	
	**Content:**
	```
    {
		"timestamp" : "a_long",
		"requestID" : "a_request_ID",
		"operation" : "transferGood",
		"from" : "server",
		"to" : "a_client_ID",
		"signature" : "a_signature",
		"notaryServer" : "Certified by Notary",
		"goodID" : "a_good_ID",
		"previousOwner" : "a_client_ID",
		"newOwner" : "a_client_ID"
    }
    ```
   
* **Error Response:**

    **Code:** 403
     
    **Content:**
     ```
    {
		"timestamp" : "a_long",
		"requestID" : "a_request_ID",
		"operation" : "transferGood",
		"from" : "server",
		"to" : "a_client_ID",
		"signature" : "a_signature",
		"message" : "The transaction is not valid.",
		"reason" : "a_reason"
    }
    ```

* **Error Response:**

    **Code:** 500
     
    **Content:**
     ```
    {
		"timestamp" : "a_long",
		"requestID" : "a_request_ID",
		"operation" : "transferGood",
		"from" : "server",
		"to" : "a_client_ID",
		"signature" : "a_signature",
		"message" : "The server cannot continue.",
		"reason" : "a_reason"
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
	"timestamp" : "a_long",
	"requestID" : "a_request_ID",
	"operation" : (OPERATION),
	"from" : "server",
	"to" : "a_client_ID",
	"signature" : "a_signature",
	"message" : "The parameters are not correct.",
	"reason" : "a_reason"
}
```

**Code:** 401
         
**Content:**
```
{
	"timestamp" : "a_long",
	"requestID" : "a_request_ID",
	"operation" : (OPERATION),
	"from" : "server",
	"to" : "a_client_ID",
	"signature" : "a_signature",
	"message" : "The connection to the database was refused.",
	"reason" : "a_reason"
}
```

**Code:** 500             

**Content:**
```
{
	"timestamp" : "a_long",
	"requestID" : "a_request_ID",
	"operation" : (OPERATION),
	"from" : "server",
	"to" : "a_client_ID",
	"signature" : "a_signature",
	"message" : "The database did not return a response for the query.",
	"reason" : "a_reason"
}
```

**Code:** 500

**Content:**
```
{
	"timestamp" : "a_long",
	"requestID" : "a_request_ID",
	"operation" : (OPERATION),
	"from" : "server",
	"to" : "a_client_ID",
	"signature" : "a_signature",
	"message" : "Caught an SQL Exception.",
	"reason" : "a_reason"
}
```

**Code:** 500

**Content:**
```
{
	"timestamp" : "a_long",
	"requestID" : "a_request_ID",
	"operation" : (OPERATION),
	"from" : "server",
	"to" : "a_client_ID",
	"signature" : "a_signature",
	"message" : "The server cannot continue.",
	"reason" : "a_reason"
}
```

**Code:** 503
                 
**Content:**
```
{
	"timestamp" : "a_long",
	"requestID" : "a_request_ID",
	"operation" : (OPERATION),
	"from" : "server",
	"to" : "a_client_ID",
	"signature" : "a_signature",
	"message" : "The connection to the database was closed.",
	"reason" : "a_reason"
}
```
 ----
 
