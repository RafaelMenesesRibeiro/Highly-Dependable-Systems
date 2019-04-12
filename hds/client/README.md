##### Documentation regarding the available HDSNotary-webServer endpoints
##### Summarized below:

* Buy Good

This documentation also includes a generalized error session, most endpoints can throw all or part of these errors.  
Errors specific to an endpoint are listed on its corresponding section

--------------------------------

**Buy Good**
----

* **URL**

    /wantToBuy

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
		"signature" : "",
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

    **Code:** 500
     
    **Content:**
     ```
    {
		"timestamp" : "a_long",
		"requestID" : "a_request_ID",
		"operation" : "wantToBuy",
		"from" : "seller_ID",
		"to" : "a_client_ID",
		"signature" : "",
		"message" : "Bad request.",
		"reason" : "a_reason"
    }
    ```

* **Error Response:**

    **Code:** 401
     
    **Content:**
     ```
    {
		"timestamp" : "a_long",
		"requestID" : "a_request_ID",
		"operation" : "wantToBuy",
		"from" : "seller_ID",
		"to" : "a_client_ID",
		"signature" : "",
		"message" : "Bad request.",
		"reason" : "a_reason"
    }
    ```

* **Error Response:**

	The other error responses this request can return are the ones defined in the General HDSNotary API Errors section defined on:

		~/Highly-Dependable-Systems/hds/server/README.md

   
