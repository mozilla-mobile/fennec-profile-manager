# fennec-profile-manager
Fennec Profile Manager (Backup / Restore)

## Prerequisite 
All backups are encypted / decrypted using the DES algorithm and provided secret key.  
This secret key is read from the "secret.key" file from project's root.

###### IMPORTANT NOTES!
###### All backups must be signed with the same secret key, otherwise they will be invalid!
###### The existence and validity of the provided secret key is verified at compile time.
###### The "secret.key" file should never be commited to CVS!

For local builds you can add your key using the following steps:  
1. open a terminal in the root project  
2. ```~ touch secret.key```   
3. ```~ echo "your own / mozilla shared secret key value" > secret.key```   
4. ```~ ./gradlew build```

