MERGE CUSTOMER              AS TARGET                                                                       
USING CUSTOMER_TEMP         AS SOURCE                                                                        
ON (TARGET.ID           =   SOURCE.ID)                                                              
WHEN NOT MATCHED BY TARGET THEN                                                                              
   INSERT (ID, FIRST_NAME, LAST_NAME, EMAIL, COUNTRY, APP_INSTALL)                                       
   VALUES (SOURCE.ID, SOURCE.FIRST_NAME, SOURCE.LAST_NAME, SOURCE.EMAIL, SOURCE.COUNTRY, SOURCE.APP_INSTALL)    
WHEN MATCHED THEN UPDATE SET                                                                                 
   TARGET.FIRST_NAME    =   SOURCE.FIRST_NAME,
   TARGET.LAST_NAME     =   SOURCE.LAST_NAME,                                                                  
   TARGET.EMAIL         =   SOURCE.EMAIL,                                                                  
   TARGET.COUNTRY       =   SOURCE.COUNTRY,                                                             
   TARGET.APP_INSTALL   =   SOURCE.APP_INSTALL;